<?php

use Foo\Bar\Baz;

class Bat {

    function __construct() {
        Baz::getInstance();
    }

}

?>