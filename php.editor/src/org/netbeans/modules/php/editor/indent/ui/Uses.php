<?php

use Foo\Bar\Baz; // VS
// "Start Use Statements with a Namespace Separator"
use \Foo\Bar\Bat;

// or "Prefer Multiple Use Statements Combined"

use \Foo\Bar\Baz,
    \Foo\Bar\Bat;

class Bat {

    function __construct() {
        Baz::getInstance(); // VS
        // "Prefer Fully Qualified Names over Use of Unqualified Names"
        \Foo\Bar\Baz::getInstance();
    }

}

?>