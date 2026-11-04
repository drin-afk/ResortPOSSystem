<?php
declare(strict_types=1);

/**
 * Mirrors util/ValidationUtil.java rules on the authoritative server side.
 */
class Validator
{
    public static function isEmpty(?string $text): bool
    {
        return $text === null || trim($text) === '';
    }

    public static function isValidUsername(?string $username): bool
    {
        return $username !== null && (bool) preg_match('/^[A-Za-z0-9_]{4,20}$/', $username);
    }

    public static function isValidPassword(?string $password): bool
    {
        return $password !== null && strlen($password) >= 6;
    }

    public static function isValidEmail(?string $email): bool
    {
        return $email !== null && (bool) preg_match('/^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/', $email);
    }

    public static function isValidPhone(?string $phone): bool
    {
        return $phone !== null && (bool) preg_match('/^09\d{9}$/', $phone);
    }

    public static function isValidName(?string $name): bool
    {
        if ($name === null) {
            return false;
        }
        $trimmed = trim($name);
        return mb_strlen($trimmed) >= 2 && (bool) preg_match('/^[A-Za-zÑñ .\'-]+$/u', $trimmed);
    }

    public static function isPositiveInt(mixed $val): bool
    {
        return filter_var($val, FILTER_VALIDATE_INT) !== false && (int) $val > 0;
    }

    public static function isNonNegativeInt(mixed $val): bool
    {
        return filter_var($val, FILTER_VALIDATE_INT) !== false && (int) $val >= 0;
    }

    public static function isValidMoney(mixed $val): bool
    {
        return is_numeric($val) && (float) $val >= 0;
    }

    public static function parseDateTime(?string $val): ?string
    {
        if (self::isEmpty($val)) {
            return null;
        }
        $raw = str_replace('T', ' ', trim($val));
        if (preg_match('/^\d{4}-\d{2}-\d{2} \d{2}:\d{2}(:\d{2})?$/', $raw)) {
            $dt = strtotime($raw);
            return $dt !== false ? date('Y-m-d H:i:s', $dt) : null;
        }
        return null;
    }
}
