<?php
declare(strict_types=1);

class Response
{
    public static function json(array $data, int $status = 200): never
    {
        http_response_code($status);
        header('Content-Type: application/json; charset=utf-8');
        echo json_encode($data, JSON_UNESCAPED_UNICODE);
        exit;
    }

    public static function error(string $message, int $status = 400): never
    {
        self::json(['ok' => false, 'error' => $message], $status);
    }

    public static function ok(mixed $data = null, string $message = 'OK'): never
    {
        self::json(['ok' => true, 'message' => $message, 'data' => $data], 200);
    }
}
