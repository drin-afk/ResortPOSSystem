<?php
declare(strict_types=1);

class ReservationController
{
    /**
     * Mirrors ReservationDAO.findAllRows() for ManageReservationsFrame
     */
    public static function listAll(): never
    {
        AuthMiddleware::requireAuth(['ADMIN', 'CASHIER']);
        $pdo = Database::getConnection();
        $sql = "SELECT r.reservation_id, c.full_name AS customer_name, r.reservation_status, r.payment_status,
                       r.total_amount, r.amount_paid,
                       GROUP_CONCAT(IFNULL(ck.cottage_name, rm.room_name) SEPARATOR ', ') AS items,
                       DATE_FORMAT(MIN(ri.start_datetime), '%Y-%m-%d %H:%i') AS start_dt,
                       DATE_FORMAT(MAX(ri.end_datetime), '%Y-%m-%d %H:%i') AS end_dt
                FROM reservations r
                JOIN customers c ON c.customer_id = r.customer_id
                LEFT JOIN reservation_items ri ON ri.reservation_id = r.reservation_id
                LEFT JOIN cottages ck ON ck.cottage_id = ri.cottage_id
                LEFT JOIN rooms rm ON rm.room_id = ri.room_id
                GROUP BY r.reservation_id, c.full_name, r.reservation_status,
                         r.payment_status, r.total_amount, r.amount_paid
                ORDER BY r.reservation_id DESC";
        Response::ok($pdo->query($sql)->fetchAll());
    }

    /**
     * Mirrors ReservationDAO.findBookings(itemType) for BookingListFrame ("ROOM" or "COTTAGE")
     */
    public static function listByItemType(string $itemType): never
    {
        AuthMiddleware::requireAuth(['ADMIN', 'CASHIER']);
        $type = strtoupper(trim($itemType));
        if (!in_array($type, ['ROOM', 'COTTAGE'], true)) {
            Response::error('Invalid booking item type.');
        }

        $nameCol = $type === 'COTTAGE' ? 'ck.cottage_name' : 'rm.room_name';
        $sql = "SELECT r.reservation_id, c.full_name AS customer_name, {$nameCol} AS item_name,
                       DATE_FORMAT(ri.start_datetime, '%Y-%m-%d %H:%i') AS start_datetime,
                       DATE_FORMAT(ri.end_datetime, '%Y-%m-%d %H:%i') AS end_datetime,
                       ri.guests, ri.subtotal, r.reservation_status, r.payment_status
                FROM reservation_items ri
                JOIN reservations r ON r.reservation_id = ri.reservation_id
                JOIN customers c ON c.customer_id = r.customer_id
                LEFT JOIN cottages ck ON ck.cottage_id = ri.cottage_id
                LEFT JOIN rooms rm ON rm.room_id = ri.room_id
                WHERE ri.item_type = ?
                ORDER BY ri.start_datetime DESC";

        $pdo = Database::getConnection();
        $stmt = $pdo->prepare($sql);
        $stmt->execute([$type]);
        Response::ok($stmt->fetchAll());
    }

    /**
     * Mirrors ManageReservationsFrame status changes (CHECKED_IN, COMPLETED, CANCELLED)
     */
    public static function updateStatus(int $id, array $body): never
    {
        $user = AuthMiddleware::requireAuth(['ADMIN']);
        $newStatus = strtoupper(trim((string) ($body['status'] ?? '')));
        if (!in_array($newStatus, ['CHECKED_IN', 'COMPLETED', 'CANCELLED'], true)) {
            Response::error('Allowed status updates: CHECKED_IN, COMPLETED, CANCELLED.');
        }

        $pdo = Database::getConnection();
        $stmt = $pdo->prepare('SELECT reservation_status FROM reservations WHERE reservation_id = ?');
        $stmt->execute([$id]);
        $row = $stmt->fetch();
        if (!$row) {
            Response::error('Reservation not found.', 404);
        }

        $current = $row['reservation_status'];
        if (in_array($current, ['CANCELLED', 'COMPLETED'], true)) {
            Response::error("Reservations that are {$current} can no longer be modified.");
        }
        if ($newStatus === 'CHECKED_IN' && $current === 'CHECKED_IN') {
            Response::error('This reservation is already checked in.');
        }

        if ($newStatus === 'CANCELLED') {
            $upd = $pdo->prepare(
                "UPDATE reservations SET reservation_status = 'CANCELLED', payment_status = 'CANCELLED' WHERE reservation_id = ?"
            );
            $upd->execute([$id]);
            AuditService::log($user['user_id'], 'CANCEL_RESERVATION', "Cancelled reservation #{$id}");
            Response::ok(null, 'Reservation cancelled successfully.');
        }

        $upd = $pdo->prepare('UPDATE reservations SET reservation_status = ? WHERE reservation_id = ?');
        $upd->execute([$newStatus, $id]);
        AuditService::log($user['user_id'], 'UPDATE_RESERVATION_STATUS', "Reservation #{$id} -> {$newStatus}");
        Response::ok(null, 'Reservation status updated successfully.');
    }
}
