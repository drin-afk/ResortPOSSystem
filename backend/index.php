<?php
declare(strict_types=1);

date_default_timezone_set('Asia/Manila');

require_once __DIR__ . '/config/database.php';
require_once __DIR__ . '/utils/Response.php';
require_once __DIR__ . '/utils/Validator.php';
require_once __DIR__ . '/middleware/AuthMiddleware.php';
require_once __DIR__ . '/services/AuditService.php';
require_once __DIR__ . '/controllers/AuthController.php';
require_once __DIR__ . '/controllers/StaffController.php';
require_once __DIR__ . '/controllers/CustomerController.php';
require_once __DIR__ . '/controllers/FacilityController.php';
require_once __DIR__ . '/controllers/ReservationController.php';
require_once __DIR__ . '/controllers/TransactionController.php';

// Narrow CORS for Vite dev server & local Apache
$origin = $_SERVER['HTTP_ORIGIN'] ?? '';
$allowedOrigins = ['http://localhost:5173', 'http://127.0.0.1:5173', 'http://localhost'];
if (in_array($origin, $allowedOrigins, true)) {
    header("Access-Control-Allow-Origin: {$origin}");
    header('Access-Control-Allow-Credentials: true');
    header('Access-Control-Allow-Methods: GET, POST, PUT, PATCH, DELETE, OPTIONS');
    header('Access-Control-Allow-Headers: Content-Type, X-CSRF-Token');
}

if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(204);
    exit;
}

set_exception_handler(function (Throwable $e) {
    if ($e instanceof PDOException) {
        Response::error('Cannot connect to or query the database. Please make sure MySQL is started in XAMPP.', 500);
    }
    Response::error('Internal server error.', 500);
});

$method = $_SERVER['REQUEST_METHOD'];
$route  = $_GET['route'] ?? '';
if ($route === '') {
    $uriPath = parse_url($_SERVER['REQUEST_URI'], PHP_URL_PATH) ?: '';
    $pos = strpos($uriPath, '/api/');
    if ($pos !== false) {
        $route = substr($uriPath, $pos + 5);
    }
}
$route = trim($route, '/');

$rawBody = file_get_contents('php://input');
$body    = $rawBody ? (json_decode($rawBody, true) ?? []) : [];

// Route Dispatcher
switch (true) {
    // Auth
    case $route === 'auth/login' && $method === 'POST':
        AuthController::login($body);
    case $route === 'auth/me' && $method === 'GET':
        AuthController::me();
    case $route === 'auth/logout' && $method === 'POST':
        AuthController::logout();

    // Cashiers (Admin)
    case $route === 'cashiers' && $method === 'GET':
        StaffController::listCashiers();
    case $route === 'cashiers' && $method === 'POST':
        StaffController::createCashier($body);
    case preg_match('#^cashiers/(\d+)/status$#', $route, $m) && $method === 'PATCH':
        StaffController::toggleStatus((int) $m[1], $body);
    case preg_match('#^cashiers/(\d+)/password$#', $route, $m) && $method === 'PATCH':
        StaffController::resetPassword((int) $m[1], $body);

    // Customers
    case $route === 'customers' && $method === 'GET':
        CustomerController::list();
    case $route === 'customers' && $method === 'POST':
        CustomerController::create($body);
    case preg_match('#^customers/(\d+)$#', $route, $m) && $method === 'PUT':
        CustomerController::update((int) $m[1], $body);

    // Cottages
    case $route === 'cottages' && $method === 'GET':
        FacilityController::listCottages();
    case $route === 'cottages' && $method === 'POST':
        FacilityController::createCottage($body);
    case preg_match('#^cottages/(\d+)$#', $route, $m) && $method === 'PUT':
        FacilityController::updateCottage((int) $m[1], $body);
    case preg_match('#^cottages/(\d+)$#', $route, $m) && $method === 'DELETE':
        FacilityController::deleteCottage((int) $m[1]);

    // Rooms
    case $route === 'rooms' && $method === 'GET':
        FacilityController::listRooms();
    case $route === 'rooms' && $method === 'POST':
        FacilityController::createRoom($body);
    case preg_match('#^rooms/(\d+)$#', $route, $m) && $method === 'PUT':
        FacilityController::updateRoom((int) $m[1], $body);
    case preg_match('#^rooms/(\d+)$#', $route, $m) && $method === 'DELETE':
        FacilityController::deleteRoom((int) $m[1]);

    // Availability
    case $route === 'availability' && $method === 'GET':
        FacilityController::checkAvailability($_GET);

    // Reservations & Bookings
    case $route === 'reservations' && $method === 'GET':
        ReservationController::listAll();
    case preg_match('#^reservations/bookings/(ROOM|COTTAGE)$#i', $route, $m) && $method === 'GET':
        ReservationController::listByItemType($m[1]);
    case preg_match('#^reservations/(\d+)/status$#', $route, $m) && $method === 'PATCH':
        ReservationController::updateStatus((int) $m[1], $body);

    // POS Transactions & Reports
    case $route === 'pos/calculate' && $method === 'POST':
        TransactionController::calculateOrSave($body, false);
    case $route === 'pos/checkout' && $method === 'POST':
        TransactionController::calculateOrSave($body, true);
    case $route === 'transactions' && $method === 'GET':
        TransactionController::listHistory($_GET);
    case preg_match('#^transactions/([A-Za-z0-9-]+)$#', $route, $m) && $method === 'GET':
        TransactionController::getReceipt($m[1]);
    case $route === 'reports/sales' && $method === 'GET':
        TransactionController::salesReport($_GET);

    default:
        Response::error("Endpoint not found: {$method} /{$route}", 404);
}
