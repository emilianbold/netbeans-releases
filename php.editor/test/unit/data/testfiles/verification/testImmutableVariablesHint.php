<?php
//START

Cls::method('x', function () {
    $component = "";
    return $component;
});
Cls::method('y', function () {
    $component = "";
    return $component;
});

try {
    $foo = "";
} catch (Exception $e) {
    $foo = false;
}

$test = "ok";
$test = "2 - should fail";

class y {
    private static function x() {
        $h = '';
        self::$h = 1;
    }
}

//END
?>