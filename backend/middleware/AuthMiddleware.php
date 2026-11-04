<?php
declare(strict_types=1);

class AuthMiddleware
{
    public static function initSession(): void
    {
        if (session_status() === PHP_SESSION_NONE) {
            session_set_cookie_params([
                'lifetime' => 0,
                'path'     => '/',
                'httponly' => true,
                'samesite' => 'Lax',
                'secure'   => (!empty($_SERVER['HTTPS']) && $_SERVER['HTTPS'] !== 'off'),
            ]);
            session_start();
        }
    }

    public static function requireAuth(array $allowedRoles = []): array
    {
        self::initSession();
        if (empty($_SESSION['user'])) {
            Response::error('Authentication required. Please log in.', 401);
        }

        // Verify CSRF on state-changing requests
        $method = $_SERVER['REQUEST_METHOD'] ?? 'GET';
        if (in_array($method, ['POST', 'PUT', 'PATCH', 'DELETE'], true)) {
            $headerToken = $_SERVER['HTTP_X_CSRF_TOKEN'] ?? '';
            $sessionToken = $_SESSION['csrf_token'] ?? '';
            if (empty($sessionToken) || !hash_equals($sessionToken, $headerToken)) {
                Response::error('Invalid or missing CSRF token.', 403);
            }
        }

        $user = $_SESSION['user'];
        if (!empty($allowedRoles) && !in_array($user['role'], $allowedRoles, true)) {
            Response::error('You are not authorized to access this resource.', 403);
        }
        return $user;
    }
}
