<?php
declare(strict_types=1);

class FacilityController
{
    // ================= COTTAGES =================
    public static function listCottages(): never
    {
        AuthMiddleware::requireAuth(['ADMIN', 'CASHIER']);
        $pdo = Database::getConnection();
        Response::ok($pdo->query('SELECT * FROM cottages ORDER BY cottage_id')->fetchAll());
    }

    public static function createCottage(array $body): never
    {
        $user = AuthMiddleware::requireAuth(['ADMIN']);
        $data = self::validateCottage($body);

        $pdo = Database::getConnection();
        $chk = $pdo->prepare('SELECT 1 FROM cottages WHERE cottage_name = ?');
        $chk->execute([$data['name']]);
        if ($chk->fetch()) {
            Response::error("A cottage with the name \"{$data['name']}\" already exists.");
        }

        $stmt = $pdo->prepare(
            'INSERT INTO cottages (cottage_name, cottage_type, capacity, rental_price, status) VALUES (?, ?, ?, ?, ?)'
        );
        $stmt->execute([$data['name'], $data['type'], $data['capacity'], $data['price'], $data['status']]);

        AuditService::log($user['user_id'], 'ADD_COTTAGE', "Added cottage {$data['name']}");
        Response::ok(['cottage_id' => (int) $pdo->lastInsertId()], 'Cottage added successfully.');
    }

    public static function updateCottage(int $id, array $body): never
    {
        $user = AuthMiddleware::requireAuth(['ADMIN']);
        $data = self::validateCottage($body);

        $pdo = Database::getConnection();
        $chk = $pdo->prepare('SELECT 1 FROM cottages WHERE cottage_name = ? AND cottage_id <> ?');
        $chk->execute([$data['name'], $id]);
        if ($chk->fetch()) {
            Response::error("Another cottage with the name \"{$data['name']}\" already exists.");
        }

        $stmt = $pdo->prepare(
            'UPDATE cottages SET cottage_name=?, cottage_type=?, capacity=?, rental_price=?, status=? WHERE cottage_id=?'
        );
        $stmt->execute([$data['name'], $data['type'], $data['capacity'], $data['price'], $data['status'], $id]);

        AuditService::log($user['user_id'], 'UPDATE_COTTAGE', "Updated cottage #{$id} ({$data['name']})");
        Response::ok(null, 'Cottage updated successfully.');
    }

    public static function deleteCottage(int $id): never
    {
        $user = AuthMiddleware::requireAuth(['ADMIN']);
        $pdo = Database::getConnection();
        $chk = $pdo->prepare('SELECT 1 FROM reservation_items WHERE cottage_id = ? LIMIT 1');
        $chk->execute([$id]);
        if ($chk->fetch()) {
            Response::error('This cottage cannot be deleted because it has a reservation record. Please set its status to UNAVAILABLE instead.');
        }

        $stmt = $pdo->prepare('DELETE FROM cottages WHERE cottage_id = ?');
        $stmt->execute([$id]);
        AuditService::log($user['user_id'], 'DELETE_COTTAGE', "Deleted cottage #{$id}");
        Response::ok(null, 'Cottage deleted successfully.');
    }

    private static function validateCottage(array $body): array
    {
        $name   = trim((string) ($body['cottage_name'] ?? ''));
        $type   = trim((string) ($body['cottage_type'] ?? ''));
        $cap    = $body['capacity'] ?? '';
        $price  = $body['rental_price'] ?? '';
        $status = strtoupper(trim((string) ($body['status'] ?? 'AVAILABLE')));

        if (Validator::isEmpty($name) || Validator::isEmpty($type)) {
            Response::error('Please enter Cottage Name and Type.');
        }
        if (!Validator::isPositiveInt($cap)) {
            Response::error('Capacity must be a whole number greater than 0.');
        }
        if (!Validator::isValidMoney($price) || (float) $price <= 0) {
            Response::error('Price must be a valid amount greater than 0 (e.g., 500.00).');
        }
        if (!in_array($status, ['AVAILABLE', 'UNAVAILABLE', 'MAINTENANCE'], true)) {
            Response::error('Invalid status.');
        }

        return [
            'name'     => $name,
            'type'     => $type,
            'capacity' => (int) $cap,
            'price'    => number_format((float) $price, 2, '.', ''),
            'status'   => $status,
        ];
    }

    // ================= ROOMS =================
    public static function listRooms(): never
    {
        AuthMiddleware::requireAuth(['ADMIN', 'CASHIER']);
        $pdo = Database::getConnection();
        Response::ok($pdo->query('SELECT * FROM rooms ORDER BY room_id')->fetchAll());
    }

    public static function createRoom(array $body): never
    {
        $user = AuthMiddleware::requireAuth(['ADMIN']);
        $data = self::validateRoom($body);

        $pdo = Database::getConnection();
        $chk = $pdo->prepare('SELECT 1 FROM rooms WHERE room_name = ?');
        $chk->execute([$data['name']]);
        if ($chk->fetch()) {
            Response::error("A room with the name \"{$data['name']}\" already exists.");
        }

        $stmt = $pdo->prepare(
            'INSERT INTO rooms (room_name, room_type, max_capacity, price_per_night, status) VALUES (?, ?, ?, ?, ?)'
        );
        $stmt->execute([$data['name'], $data['type'], $data['capacity'], $data['price'], $data['status']]);

        AuditService::log($user['user_id'], 'ADD_ROOM', "Added room {$data['name']}");
        Response::ok(['room_id' => (int) $pdo->lastInsertId()], 'Room added successfully.');
    }

    public static function updateRoom(int $id, array $body): never
    {
        $user = AuthMiddleware::requireAuth(['ADMIN']);
        $data = self::validateRoom($body);

        $pdo = Database::getConnection();
        $chk = $pdo->prepare('SELECT 1 FROM rooms WHERE room_name = ? AND room_id <> ?');
        $chk->execute([$data['name'], $id]);
        if ($chk->fetch()) {
            Response::error("Another room with the name \"{$data['name']}\" already exists.");
        }

        $stmt = $pdo->prepare(
            'UPDATE rooms SET room_name=?, room_type=?, max_capacity=?, price_per_night=?, status=? WHERE room_id=?'
        );
        $stmt->execute([$data['name'], $data['type'], $data['capacity'], $data['price'], $data['status'], $id]);

        AuditService::log($user['user_id'], 'UPDATE_ROOM', "Updated room #{$id} ({$data['name']})");
        Response::ok(null, 'Room updated successfully.');
    }

    public static function deleteRoom(int $id): never
    {
        $user = AuthMiddleware::requireAuth(['ADMIN']);
        $pdo = Database::getConnection();
        $chk = $pdo->prepare('SELECT 1 FROM reservation_items WHERE room_id = ? LIMIT 1');
        $chk->execute([$id]);
        if ($chk->fetch()) {
            Response::error('This room cannot be deleted because it has an active reservation. Please change its status to UNAVAILABLE instead.');
        }

        $stmt = $pdo->prepare('DELETE FROM rooms WHERE room_id = ?');
        $stmt->execute([$id]);
        AuditService::log($user['user_id'], 'DELETE_ROOM', "Deleted room #{$id}");
        Response::ok(null, 'Room deleted successfully.');
    }

    private static function validateRoom(array $body): array
    {
        $name   = trim((string) ($body['room_name'] ?? ''));
        $type   = trim((string) ($body['room_type'] ?? ''));
        $cap    = $body['max_capacity'] ?? '';
        $price  = $body['price_per_night'] ?? '';
        $status = strtoupper(trim((string) ($body['status'] ?? 'AVAILABLE')));

        if (Validator::isEmpty($name) || Validator::isEmpty($type)) {
            Response::error('Please enter the room name and room type.');
        }
        if (!Validator::isPositiveInt($cap)) {
            Response::error('Maximum capacity must be a whole number greater than 0.');
        }
        if (!Validator::isValidMoney($price) || (float) $price <= 0) {
            Response::error('Price must be a valid amount greater than 0 (e.g., 1500.00).');
        }
        if (!in_array($status, ['AVAILABLE', 'UNAVAILABLE', 'MAINTENANCE'], true)) {
            Response::error('Invalid status.');
        }

        return [
            'name'     => $name,
            'type'     => $type,
            'capacity' => (int) $cap,
            'price'    => number_format((float) $price, 2, '.', ''),
            'status'   => $status,
        ];
    }

    // ================= AVAILABILITY CHECK (Mirrors AvailabilityFrame) =================
    public static function checkAvailability(array $query): never
    {
        AuthMiddleware::requireAuth(['ADMIN', 'CASHIER']);
        $startStr = Validator::parseDateTime($query['start'] ?? null);
        $endStr   = Validator::parseDateTime($query['end'] ?? null);

        if (!$startStr || !$endStr) {
            Response::error('Invalid date format. Use yyyy-MM-dd HH:mm.');
        }
        if (strtotime($endStr) <= strtotime($startStr)) {
            Response::error('The end date and time must be later than the start.');
        }

        $pdo = Database::getConnection();
        $records = [];
        $freeCount = 0;
        $bookedCount = 0;

        $cottageConflictStmt = $pdo->prepare(
            "SELECT 1 FROM reservation_items ri
             JOIN reservations r ON r.reservation_id = ri.reservation_id
             WHERE ri.cottage_id = ? AND r.reservation_status <> 'CANCELLED'
             AND ? < ri.end_datetime AND ? > ri.start_datetime LIMIT 1"
        );

        foreach ($pdo->query('SELECT * FROM cottages ORDER BY cottage_id')->fetchAll() as $c) {
            $cottageConflictStmt->execute([$c['cottage_id'], $startStr, $endStr]);
            $isFree = !$cottageConflictStmt->fetch();
            $adminStatus = $c['status'];
            if ($adminStatus !== 'AVAILABLE') {
                $status = $adminStatus;
                $avail = 'NOT BOOKABLE';
            } elseif ($isFree) {
                $status = 'AVAILABLE';
                $avail = 'FREE';
                $freeCount++;
            } else {
                $status = 'ALREADY BOOKED';
                $avail = 'BOOKED';
                $bookedCount++;
            }
            $records[] = [
                'id'           => (int) $c['cottage_id'],
                'type'         => 'Cottage',
                'name'         => $c['cottage_name'],
                'category'     => $c['cottage_type'],
                'capacity'     => (int) $c['capacity'],
                'price'        => $c['rental_price'],
                'admin_status' => $adminStatus,
                'status'       => $status,
                'availability' => $avail,
            ];
        }

        $roomConflictStmt = $pdo->prepare(
            "SELECT 1 FROM reservation_items ri
             JOIN reservations r ON r.reservation_id = ri.reservation_id
             WHERE ri.room_id = ? AND r.reservation_status <> 'CANCELLED'
             AND ? < ri.end_datetime AND ? > ri.start_datetime LIMIT 1"
        );

        foreach ($pdo->query('SELECT * FROM rooms ORDER BY room_id')->fetchAll() as $r) {
            $roomConflictStmt->execute([$r['room_id'], $startStr, $endStr]);
            $isFree = !$roomConflictStmt->fetch();
            $adminStatus = $r['status'];
            if ($adminStatus !== 'AVAILABLE') {
                $status = $adminStatus;
                $avail = 'NOT BOOKABLE';
            } elseif ($isFree) {
                $status = 'AVAILABLE';
                $avail = 'FREE';
                $freeCount++;
            } else {
                $status = 'ALREADY BOOKED';
                $avail = 'BOOKED';
                $bookedCount++;
            }
            $records[] = [
                'id'           => (int) $r['room_id'],
                'type'         => 'Room',
                'name'         => $r['room_name'],
                'category'     => $r['room_type'],
                'capacity'     => (int) $r['max_capacity'],
                'price'        => $r['price_per_night'],
                'admin_status' => $adminStatus,
                'status'       => $status,
                'availability' => $avail,
            ];
        }

        Response::ok([
            'start'   => substr($startStr, 0, 16),
            'end'     => substr($endStr, 0, 16),
            'free'    => $freeCount,
            'booked'  => $bookedCount,
            'records' => $records,
        ]);
    }
}
