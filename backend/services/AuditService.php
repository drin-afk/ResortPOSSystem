<?php
declare(strict_types=1);

class AuditService
{
    public static function log(?int $userId, string $action, string $details = ''): void
    {
        try {
            $pdo = Database::getConnection();
            $stmt = $pdo->prepare('INSERT INTO audit_logs (user_id, action, details) VALUES (?, ?, ?)');
            $stmt->execute([$userId, $action, mb_substr($details, 0, 255)]);
        } catch (Throwable) {
            // Non-blocking audit logging
        }
    }
}
