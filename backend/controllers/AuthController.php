<?php
declare(strict_types=1);

class AuthController
{
    public static function login(array $body): never
    {
        AuthMiddleware::initSession();

        $username = trim((string) ($body['username'] ?? ''));
        $password = (string) ($body['password'] ?? '');

        if (Validator::isEmpty($username) || Validator::isEmpty($password)) {
            Response::error('Please enter both username and password.');
        }

        $pdo = Database::getConnection();
        $stmt = $pdo->prepare(
            'SELECT user_id, username, password_hash, full_name, role, status FROM users WHERE username = ?'
        );
        $stmt->execute([$username]);
        $row = $stmt->fetch();

        if (!$row) {
            Response::error('Invalid username or password, or the account is disabled.', 401);
        }

        $storedHash = (string) $row['password_hash'];
        $verified = false;

        // 1. Standard PHP password_verify (bcrypt/argon2)
        if (str_starts_with($storedHash, '$2y$') || str_starts_with($storedHash, '$argon2')) {
            $verified = password_verify($password, $storedHash);
        } elseif (strlen($storedHash) === 64 && ctype_xdigit($storedHash)) {
            // 2. Legacy Java SHA-256 compatibility + automatic upgrade to password_hash()
            $sha = hash('sha256', $password);
            if (hash_equals(strtolower($storedHash), strtolower($sha))) {
                $verified = true;
                $newHash = password_hash($password, PASSWORD_DEFAULT);
                $upd = $pdo->prepare('UPDATE users SET password_hash = ? WHERE user_id = ?');
                $upd->execute([$newHash, $row['user_id']]);
            }
        }

        if (!$verified || $row['status'] !== 'ACTIVE') {
            Response::error('Invalid username or password, or the account is disabled.', 401);
        }

        if (!in_array($row['role'], ['ADMIN', 'CASHIER'], true)) {
            Response::error('This account is not allowed to use the POS. Please contact the administrator.', 403);
        }

        session_regenerate_id(true);
        $_SESSION['csrf_token'] = bin2hex(random_bytes(32));
        $_SESSION['user'] = [
            'user_id'   => (int) $row['user_id'],
            'username'  => $row['username'],
            'full_name' => $row['full_name'],
            'role'      => $row['role'],
            'status'    => $row['status'],
        ];

        AuditService::log((int) $row['user_id'], 'LOGIN', "User {$row['username']} logged in");

        Response::ok([
            'user'       => $_SESSION['user'],
            'csrf_token' => $_SESSION['csrf_token'],
        ], 'Logged in successfully.');
    }

    public static function me(): never
    {
        AuthMiddleware::initSession();
        if (empty($_SESSION['user'])) {
            Response::ok(['user' => null, 'csrf_token' => null]);
        }
        Response::ok([
            'user'       => $_SESSION['user'],
            'csrf_token' => $_SESSION['csrf_token'] ?? '',
        ]);
    }

    public static function logout(): never
    {
        AuthMiddleware::initSession();
        $userId = $_SESSION['user']['user_id'] ?? null;
        if ($userId) {
            AuditService::log((int) $userId, 'LOGOUT', 'User logged out');
        }
        $_SESSION = [];
        if (ini_get('session.use_cookies')) {
            $params = session_get_cookie_params();
            setcookie(session_name(), '', time() - 42000, $params['path'], $params['domain'] ?? '', $params['secure'], $params['httponly']);
        }
        session_destroy();
        Response::ok(null, 'Logged out.');
    }
}
