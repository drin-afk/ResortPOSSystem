<?php
declare(strict_types=1);

class TransactionController
{
    /**
     * Server-side calculation & validation + optional transactional save.
     * Mirrors POSFrame.calculate(), POSFrame.confirmPayment(), and TransactionDAO.saveTransaction().
     */
    public static function calculateOrSave(array $body, bool $commitPayment): never
    {
        $user = AuthMiddleware::requireAuth(['ADMIN', 'CASHIER']);
        $pdo  = Database::getConnection();

        $customerId = (int) ($body['customer_id'] ?? 0);
        if ($customerId <= 0) {
            Response::error('Please select a customer first. Register the customer if they are new.');
        }

        $chkCust = $pdo->prepare('SELECT customer_id, full_name FROM customers WHERE customer_id = ?');
        $chkCust->execute([$customerId]);
        $customer = $chkCust->fetch();
        if (!$customer) {
            Response::error('Selected customer does not exist.');
        }

        $items    = [];
        $bookings = [];
        $total    = 0.0;

        // 1. Optional Cottage Booking
        $cottageId = (int) ($body['cottage_id'] ?? 0);
        if ($cottageId > 0) {
            $cStmt = $pdo->prepare("SELECT * FROM cottages WHERE cottage_id = ? AND status = 'AVAILABLE'");
            $cStmt->execute([$cottageId]);
            $cottage = $cStmt->fetch();
            if (!$cottage) {
                Response::error('The selected cottage is not available.');
            }

            $cStart = Validator::parseDateTime((string) ($body['cottage_start'] ?? ''));
            $cEnd   = Validator::parseDateTime((string) ($body['cottage_end'] ?? ''));
            if (!$cStart || !$cEnd) {
                Response::error('Invalid date format for cottage. Please use yyyy-MM-dd HH:mm.');
            }
            if (strtotime($cEnd) <= strtotime($cStart)) {
                Response::error('For cottage, the end date and time must be after the start date and time.');
            }

            $cottageGuestsRaw = $body['cottage_guests'] ?? '1';
            if (!Validator::isPositiveInt($cottageGuestsRaw)) {
                Response::error('Cottage Guests must be a whole number greater than 0.');
            }
            $cottageGuests = (int) $cottageGuestsRaw;
            if ($cottageGuests > (int) $cottage['capacity']) {
                Response::error(
                    "Number of guests ({$cottageGuests}) exceeds the capacity of {$cottage['cottage_name']} ({$cottage['capacity']} pax)."
                );
            }

            $cPrice = round((float) $cottage['rental_price'], 2);
            $items[] = [
                'item_type'   => 'COTTAGE',
                'item_ref_id' => (int) $cottage['cottage_id'],
                'description' => 'Cottage - ' . $cottage['cottage_name'],
                'quantity'    => 1,
                'unit_price'  => number_format($cPrice, 2, '.', ''),
                'subtotal'    => number_format($cPrice, 2, '.', ''),
            ];
            $bookings[] = [
                'type'       => 'COTTAGE',
                'ref_id'     => (int) $cottage['cottage_id'],
                'start'      => $cStart,
                'end'        => $cEnd,
                'guests'     => $cottageGuests,
                'unit_price' => number_format($cPrice, 2, '.', ''),
                'subtotal'   => number_format($cPrice, 2, '.', ''),
            ];
            $total += $cPrice;
        }

        // 3. Optional Room Booking
        $roomId = (int) ($body['room_id'] ?? 0);
        if ($roomId > 0) {
            $rStmt = $pdo->prepare("SELECT * FROM rooms WHERE room_id = ? AND status = 'AVAILABLE'");
            $rStmt->execute([$roomId]);
            $room = $rStmt->fetch();
            if (!$room) {
                Response::error('The selected room is not available.');
            }

            $rStart = Validator::parseDateTime((string) ($body['check_in'] ?? ''));
            $rEnd   = Validator::parseDateTime((string) ($body['check_out'] ?? ''));
            if (!$rStart || !$rEnd) {
                Response::error('Invalid date format for room. Please use yyyy-MM-dd HH:mm.');
            }
            if (strtotime($rEnd) <= strtotime($rStart)) {
                Response::error('For room, the end date and time must be after the start date and time.');
            }

            $roomGuestsRaw = $body['room_guests'] ?? '1';
            if (!Validator::isPositiveInt($roomGuestsRaw)) {
                Response::error('Room Guests must be a whole number greater than 0.');
            }
            $roomGuests = (int) $roomGuestsRaw;
            if ($roomGuests > (int) $room['max_capacity']) {
                Response::error(
                    "The number of guests exceeds the maximum capacity of {$room['room_name']} ({$room['max_capacity']} pax)."
                );
            }

            // Mirrors ChronoUnit.DAYS.between(range[0].toLocalDate(), range[1].toLocalDate())
            $d1 = new DateTimeImmutable(substr($rStart, 0, 10));
            $d2 = new DateTimeImmutable(substr($rEnd, 0, 10));
            $nights = max(1, (int) $d1->diff($d2)->days);

            $unitPrice = round((float) $room['price_per_night'], 2);
            $sub       = round($unitPrice * $nights, 2);

            $items[] = [
                'item_type'   => 'ROOM',
                'item_ref_id' => (int) $room['room_id'],
                'description' => "Room - {$room['room_name']} ({$nights} night(s))",
                'quantity'    => $nights,
                'unit_price'  => number_format($unitPrice, 2, '.', ''),
                'subtotal'    => number_format($sub, 2, '.', ''),
            ];
            $bookings[] = [
                'type'       => 'ROOM',
                'ref_id'     => (int) $room['room_id'],
                'start'      => $rStart,
                'end'        => $rEnd,
                'guests'     => $roomGuests,
                'unit_price' => number_format($unitPrice, 2, '.', ''),
                'subtotal'   => number_format($sub, 2, '.', ''),
            ];
            $total += $sub;
        }

        if (empty($items)) {
            Response::error('Nothing selected. Please select a cottage or a room.');
        }

        $total = round($total, 2);

        // If calculate-only, check availability without locking and return summary
        if (!$commitPayment) {
            foreach ($bookings as $b) {
                $col = $b['type'] === 'COTTAGE' ? 'cottage_id' : 'room_id';
                $stmt = $pdo->prepare(
                    "SELECT 1 FROM reservation_items ri
                     JOIN reservations r ON r.reservation_id = ri.reservation_id
                     WHERE ri.{$col} = ? AND r.reservation_status <> 'CANCELLED'
                     AND ? < ri.end_datetime AND ? > ri.start_datetime LIMIT 1"
                );
                $stmt->execute([$b['ref_id'], $b['start'], $b['end']]);
                if ($stmt->fetch()) {
                    Response::error('The selected ' . strtolower($b['type']) . ' is not available for the selected date and time.');
                }
            }

            Response::ok([
                'items'        => $items,
                'bookings'     => $bookings,
                'total_amount' => number_format($total, 2, '.', ''),
            ], 'Total calculated successfully.');
        }

        // Confirm Payment validation
        $receivedRaw = $body['amount_received'] ?? '';
        if (!Validator::isValidMoney($receivedRaw)) {
            Response::error('Please enter the amount received. Example: 1000');
        }
        $received = round((float) $receivedRaw, 2);
        if ($received < $total) {
            Response::error(
                'Insufficient payment. Total: ' . number_format($total, 2, '.', '') .
                ' | Received: ' . number_format($received, 2, '.', '')
            );
        }

        $paymentMethod = trim((string) ($body['payment_method'] ?? 'Cash'));
        $allowedMethods = ['Cash', 'GCash', 'Maya', 'MariBank', 'BDO', 'Credit/Debit Card'];
        if (!in_array($paymentMethod, $allowedMethods, true)) {
            Response::error('Invalid payment method.');
        }

        $refNo = trim((string) ($body['reference_no'] ?? ''));
        if (strcasecmp($paymentMethod, 'Cash') !== 0 && $refNo === '') {
            Response::error("Please enter the reference number for {$paymentMethod}.");
        }

        $changeAmount = round($received - $total, 2);

        // Execute database transaction with FOR UPDATE row locks (mirrors TransactionDAO.saveTransaction)
        $pdo->beginTransaction();
        try {
            foreach ($bookings as $b) {
                $col = $b['type'] === 'COTTAGE' ? 'cottage_id' : 'room_id';
                $lockStmt = $pdo->prepare(
                    "SELECT 1 FROM reservation_items ri
                     JOIN reservations r ON r.reservation_id = ri.reservation_id
                     WHERE ri.{$col} = ? AND r.reservation_status <> 'CANCELLED'
                     AND ? < ri.end_datetime AND ? > ri.start_datetime
                     LIMIT 1 FOR UPDATE"
                );
                $lockStmt->execute([$b['ref_id'], $b['start'], $b['end']]);
                if ($lockStmt->fetch()) {
                    throw new RuntimeException(
                        'The selected ' . strtolower($b['type']) .
                        ' is no longer available at that time. Someone else has already booked it.'
                    );
                }
            }

            $reservationId = null;
            if (!empty($bookings)) {
                $bookingTotal = 0.0;
                foreach ($bookings as $b) {
                    $bookingTotal += (float) $b['subtotal'];
                }
                $bookingTotalFmt = number_format($bookingTotal, 2, '.', '');
                $resStmt = $pdo->prepare(
                    "INSERT INTO reservations (customer_id, reservation_status, payment_status, total_amount, amount_paid, created_by)
                     VALUES (?, 'CONFIRMED', 'PAID', ?, ?, ?)"
                );
                $resStmt->execute([$customerId, $bookingTotalFmt, $bookingTotalFmt, $user['user_id']]);
                $reservationId = (int) $pdo->lastInsertId();

                $riStmt = $pdo->prepare(
                    'INSERT INTO reservation_items (reservation_id, item_type, cottage_id, room_id, start_datetime, end_datetime, guests, unit_price, subtotal)
                     VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)'
                );
                foreach ($bookings as $b) {
                    $riStmt->execute([
                        $reservationId,
                        $b['type'],
                        $b['type'] === 'COTTAGE' ? $b['ref_id'] : null,
                        $b['type'] === 'ROOM' ? $b['ref_id'] : null,
                        $b['start'],
                        $b['end'],
                        $b['guests'],
                        $b['unit_price'],
                        $b['subtotal'],
                    ]);
                }
            }

            // Transaction number sequence: RS-yyyyMMdd-0001
            $prefix = 'RS-' . date('Ymd') . '-';
            $seqStmt = $pdo->prepare('SELECT COUNT(*) FROM transactions WHERE transaction_no LIKE ? FOR UPDATE');
            $seqStmt->execute([$prefix . '%']);
            $nextSeq = ((int) $seqStmt->fetchColumn()) + 1;
            $txNo = $prefix . sprintf('%04d', $nextSeq);

            $txStmt = $pdo->prepare(
                'INSERT INTO transactions (transaction_no, customer_id, reservation_id, cashier_id, subtotal, discount,
                 total_amount, amount_received, change_amount, payment_method, reference_no)
                 VALUES (?, ?, ?, ?, ?, 0.00, ?, ?, ?, ?, ?)'
            );
            $totalFmt    = number_format($total, 2, '.', '');
            $receivedFmt = number_format($received, 2, '.', '');
            $changeFmt   = number_format($changeAmount, 2, '.', '');

            $txStmt->execute([
                $txNo,
                $customerId,
                $reservationId,
                $user['user_id'],
                $totalFmt,
                $totalFmt,
                $receivedFmt,
                $changeFmt,
                strtoupper($paymentMethod),
                $refNo !== '' ? $refNo : null,
            ]);
            $transactionId = (int) $pdo->lastInsertId();

            $tiStmt = $pdo->prepare(
                'INSERT INTO transaction_items (transaction_id, item_type, item_ref_id, description, quantity, unit_price, subtotal)
                 VALUES (?, ?, ?, ?, ?, ?, ?)'
            );
            foreach ($items as $it) {
                $tiStmt->execute([
                    $transactionId,
                    $it['item_type'],
                    $it['item_ref_id'],
                    $it['description'],
                    $it['quantity'],
                    $it['unit_price'],
                    $it['subtotal'],
                ]);
            }

            $pdo->commit();

            AuditService::log($user['user_id'], 'POS_TRANSACTION', "Saved {$txNo} Total PHP {$totalFmt}");

            Response::ok([
                'transaction_no'  => $txNo,
                'reservation_id'  => $reservationId,
                'payment_method'  => $paymentMethod,
                'reference_no'    => $refNo !== '' ? $refNo : null,
                'total_amount'    => $totalFmt,
                'amount_received' => $receivedFmt,
                'change_amount'   => $changeFmt,
            ], 'Payment successful!');
        } catch (RuntimeException $re) {
            if ($pdo->inTransaction()) {
                $pdo->rollBack();
            }
            Response::error($re->getMessage(), 409);
        } catch (Throwable $e) {
            if ($pdo->inTransaction()) {
                $pdo->rollBack();
            }
            Response::error('Transaction could not be saved: ' . $e->getMessage(), 500);
        }
    }

    /**
     * Mirrors TransactionDAO.findAll(from, to) for CashierTransactionHistoryFram
     */
    public static function listHistory(array $query): never
    {
        AuthMiddleware::requireAuth(['ADMIN', 'CASHIER']);
        $from = isset($query['from']) && trim($query['from']) !== '' ? trim($query['from']) : null;
        $to   = isset($query['to']) && trim($query['to']) !== '' ? trim($query['to']) : null;

        if ($from !== null && !preg_match('/^\d{4}-\d{2}-\d{2}$/', $from)) {
            Response::error('Invalid From date format. Please use yyyy-MM-dd.');
        }
        if ($to !== null && !preg_match('/^\d{4}-\d{2}-\d{2}$/', $to)) {
            Response::error('Invalid To date format. Please use yyyy-MM-dd.');
        }
        if ($from !== null && $to !== null && $from > $to) {
            Response::error('The From date cannot be later than the To date.');
        }

        $pdo = Database::getConnection();
        $sql = "SELECT t.*, DATE_FORMAT(t.transaction_date, '%Y-%m-%d %H:%i') AS formatted_date,
                       c.full_name AS customer_name, u.full_name AS cashier_name
                FROM transactions t
                JOIN customers c ON c.customer_id = t.customer_id
                JOIN users u ON u.user_id = t.cashier_id
                WHERE (? IS NULL OR DATE(t.transaction_date) >= ?)
                  AND (? IS NULL OR DATE(t.transaction_date) <= ?)
                ORDER BY t.transaction_id DESC";
        $stmt = $pdo->prepare($sql);
        $stmt->execute([$from, $from, $to, $to]);
        Response::ok($stmt->fetchAll());
    }

    /**
     * Mirrors TransactionDAO.findByNo(transactionNo) for ReceiptFrame
     */
    public static function getReceipt(string $transactionNo): never
    {
        AuthMiddleware::requireAuth(['ADMIN', 'CASHIER']);
        $pdo = Database::getConnection();
        $stmt = $pdo->prepare(
            "SELECT t.*, DATE_FORMAT(t.transaction_date, '%Y-%m-%d %H:%i') AS formatted_date,
                    c.full_name AS customer_name, u.full_name AS cashier_name
             FROM transactions t
             JOIN customers c ON c.customer_id = t.customer_id
             JOIN users u ON u.user_id = t.cashier_id
             WHERE t.transaction_no = ?"
        );
        $stmt->execute([trim($transactionNo)]);
        $tx = $stmt->fetch();
        if (!$tx) {
            Response::error('Transaction not found.', 404);
        }

        $itemStmt = $pdo->prepare(
            'SELECT * FROM transaction_items WHERE transaction_id = ? ORDER BY transaction_item_id'
        );
        $itemStmt->execute([$tx['transaction_id']]);
        $tx['items'] = $itemStmt->fetchAll();

        Response::ok($tx);
    }

    /**
     * Mirrors ReportDAO.salesByDay() & SalesReportFrame (Daily / Weekly / Monthly)
     */
    public static function salesReport(array $query): never
    {
        AuthMiddleware::requireAuth(['ADMIN']);
        $period  = ucfirst(strtolower(trim((string) ($query['period'] ?? 'Daily'))));
        $refDate = trim((string) ($query['date'] ?? date('Y-m-d')));

        if (!preg_match('/^\d{4}-\d{2}-\d{2}$/', $refDate) || strtotime($refDate) === false) {
            Response::error('Invalid date format. Use yyyy-MM-dd (e.g., 2026-10-26).');
        }

        $ref = new DateTimeImmutable($refDate);
        if ($period === 'Daily') {
            $from = $ref->format('Y-m-d');
            $to   = $ref->format('Y-m-d');
        } elseif ($period === 'Weekly') {
            // Monday to Sunday of the reference week
            $monday = $ref->modify('monday this week');
            $from   = $monday->format('Y-m-d');
            $to     = $monday->modify('+6 days')->format('Y-m-d');
        } elseif ($period === 'Monthly') {
            $from = $ref->modify('first day of this month')->format('Y-m-d');
            $to   = $ref->modify('last day of this month')->format('Y-m-d');
        } else {
            Response::error('Invalid report period. Choose Daily, Weekly, or Monthly.');
        }

        $pdo = Database::getConnection();
        $sql = "SELECT DATE(t.transaction_date) AS date,
                       COUNT(DISTINCT t.transaction_id) AS transactions,
                       SUM(CASE WHEN ti.item_type = 'COTTAGE' THEN ti.subtotal ELSE 0 END) AS cottage,
                       SUM(CASE WHEN ti.item_type = 'ROOM' THEN ti.subtotal ELSE 0 END) AS room
                FROM transactions t
                JOIN transaction_items ti ON ti.transaction_id = t.transaction_id
                WHERE DATE(t.transaction_date) BETWEEN ? AND ?
                GROUP BY DATE(t.transaction_date)
                ORDER BY date";
        $stmt = $pdo->prepare($sql);
        $stmt->execute([$from, $to]);
        $rows = $stmt->fetchAll();

        $totalCount = 0;
        $sumCot = 0.0;
        $sumRm  = 0.0;

        foreach ($rows as &$r) {
            $cot = (float) $r['cottage'];
            $rm  = (float) $r['room'];
            $rowTotal = $cot + $rm;

            $r['transactions'] = (int) $r['transactions'];
            $r['cottage']      = number_format($cot, 2, '.', '');
            $r['room']         = number_format($rm, 2, '.', '');
            $r['total']        = number_format($rowTotal, 2, '.', '');

            $totalCount += $r['transactions'];
            $sumCot += $cot;
            $sumRm  += $rm;
        }
        unset($r);

        $grand = $sumCot + $sumRm;

        Response::ok([
            'period' => $period,
            'from'   => $from,
            'to'     => $to,
            'rows'   => $rows,
            'totals' => [
                'transactions' => $totalCount,
                'cottage'      => number_format($sumCot, 2, '.', ''),
                'room'         => number_format($sumRm, 2, '.', ''),
                'grand_total'  => number_format($grand, 2, '.', ''),
            ],
        ]);
    }
}
