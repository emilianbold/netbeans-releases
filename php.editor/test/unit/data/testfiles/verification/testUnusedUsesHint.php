<?php
//START
use My\Space\Whatever;
use Your\Space\Something,
        My\Space\Something as S2;
use Unused\Simple\Statement;

class Foo1 {

    function functionName() {
        Whatever::bar();
        S2::blah();
    }

}

use What\MyClass;
use Faces\IFace;

class Foo2 extends MyClass implements IFace {}
//END
?>