<?php
declare(strict_types=1);

class CustomerController
{
    public static function list(): never
    {
        AuthMiddleware::requireAuth(['ADMIN', 'CASHIER']);
        $pdo = Database::getConnection();
        $stmt = $pdo->query('SELECT * FROM customers ORDER BY full_name');
        Response::ok($stmt->fetchAll());
    }

    public static function create(array $body): never
    {
        $user = AuthMiddleware::requireAuth(['ADMIN', 'CASHIER']);
        $name    = trim((string) ($body['full_name'] ?? ''));
        $contact = trim((string) ($body['contact_number'] ?? ''));
        $email   = trim((string) ($body['email'] ?? ''));
        $address = trim((string) ($body['address'] ?? ''));

        if (!Validator::isValidName($name)) {
            Response::error('Please enter a valid name (letters only, at least 2 characters).');
        }
        if ($contact !== '' && !Validator::isValidPhone($contact)) {
            Response::error('Contact number must be 11 digits and start with 09 (e.g., 09171234567).');
        }
        if ($email !== '' && !Validator::isValidEmail($email)) {
            Response::error('Please enter a valid email address.');
        }

        $pdo = Database::getConnection();
        $stmt = $pdo->prepare(
            'INSERT INTO customers (user_id, full_name, contact_number, email, address) VALUES (NULL, ?, ?, ?, ?)'
        );
        $stmt->execute([
            $name,
            $contact !== '' ? $contact : null,
            $email !== '' ? $email : null,
            $address !== '' ? $address : null,
        ]);

        $id = (int) $pdo->lastInsertId();
        AuditService::log($user['user_id'], 'CREATE_CUSTOMER', "Registered customer #{$id} ({$name})");
        Response::ok(['customer_id' => $id], 'Customer registered successfully!');
    }

    public static function update(int $customerId, array $body): never
    {
        $user = AuthMiddleware::requireAuth(['ADMIN']);
        $name    = trim((string) ($body['full_name'] ?? ''));
        $contact = trim((string) ($body['contact_number'] ?? ''));
        $email   = trim((string) ($body['email'] ?? ''));
        $address = trim((string) ($body['address'] ?? ''));

        if (!Validator::isValidName($name)) {
            Response::error('Please enter a valid name.');
        }
        if ($contact !== '' && !Validator::isValidPhone($contact)) {
            Response::error('Contact number must be 11 digits and start with 09.');
        }
        if ($email !== '' && !Validator::isValidEmail($email)) {
            Response::error('Invalid email format.');
        }

        $pdo = Database::getConnection();
        $stmt = $pdo->prepare(
            'UPDATE customers SET full_name = ?, contact_number = ?, email = ?, address = ? WHERE customer_id = ?'
        );
        $stmt->execute([
            $name,
            $contact !== '' ? $contact : null,
            $email !== '' ? $email : null,
            $address !== '' ? $address : null,
            $customerId,
        ]);

        AuditService::log($user['user_id'], 'UPDATE_CUSTOMER', "Updated customer #{$customerId}");
        Response::ok(null, 'Customer updated successfully.');
    }
}
