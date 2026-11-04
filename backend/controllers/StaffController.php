<?php
declare(strict_types=1);

class StaffController
{
    public static function listCashiers(): never
    {
        AuthMiddleware::requireAuth(['ADMIN']);
        $pdo = Database::getConnection();
        $stmt = $pdo->prepare(
            "SELECT user_id, username, full_name, role, status FROM users WHERE role = 'CASHIER' ORDER BY user_id"
        );
        $stmt->execute();
        Response::ok($stmt->fetchAll());
    }

    public static function createCashier(array $body): never
    {
        $admin = AuthMiddleware::requireAuth(['ADMIN']);
        $username = trim((string) ($body['username'] ?? ''));
        $fullName = trim((string) ($body['full_name'] ?? ''));
        $password = (string) ($body['password'] ?? '');

        if (!Validator::isValidUsername($username)) {
            Response::error('Username must be 4 to 20 characters: letters, numbers, and underscores only.');
        }
        if (!Validator::isValidName($fullName)) {
            Response::error('Please enter a valid full name.');
        }
        if (!Validator::isValidPassword($password)) {
            Response::error('Password must be at least 6 characters long.');
        }

        $pdo = Database::getConnection();
        $chk = $pdo->prepare('SELECT 1 FROM users WHERE username = ?');
        $chk->execute([$username]);
        if ($chk->fetch()) {
            Response::error("The username \"{$username}\" is already taken.");
        }

        $hash = password_hash($password, PASSWORD_DEFAULT);
        $ins = $pdo->prepare(
            "INSERT INTO users (username, password_hash, full_name, role, status) VALUES (?, ?, ?, 'CASHIER', 'ACTIVE')"
        );
        $ins->execute([$username, $hash, $fullName]);

        AuditService::log($admin['user_id'], 'ADD_CASHIER', "Added cashier {$username}");
        Response::ok(['user_id' => (int) $pdo->lastInsertId()], 'Cashier added successfully.');
    }

    public static function toggleStatus(int $userId, array $body): never
    {
        $admin = AuthMiddleware::requireAuth(['ADMIN']);
        $status = strtoupper(trim((string) ($body['status'] ?? '')));
        if (!in_array($status, ['ACTIVE', 'INACTIVE'], true)) {
            Response::error('Status must be ACTIVE or INACTIVE.');
        }

        $pdo = Database::getConnection();
        $stmt = $pdo->prepare("UPDATE users SET status = ? WHERE user_id = ? AND role = 'CASHIER'");
        $stmt->execute([$status, $userId]);

        AuditService::log($admin['user_id'], 'TOGGLE_CASHIER_STATUS', "Cashier #{$userId} -> {$status}");
        Response::ok(null, "Cashier status changed to {$status}.");
    }

    public static function resetPassword(int $userId, array $body): never
    {
        $admin = AuthMiddleware::requireAuth(['ADMIN']);
        $password = (string) ($body['password'] ?? '');
        if (!Validator::isValidPassword($password)) {
            Response::error('Password must be at least 6 characters long.');
        }

        $pdo = Database::getConnection();
        $hash = password_hash($password, PASSWORD_DEFAULT);
        $stmt = $pdo->prepare("UPDATE users SET password_hash = ? WHERE user_id = ? AND role = 'CASHIER'");
        $stmt->execute([$hash, $userId]);

        AuditService::log($admin['user_id'], 'RESET_CASHIER_PASSWORD', "Reset password for cashier #{$userId}");
        Response::ok(null, 'Password reset successfully.');
    }
}
