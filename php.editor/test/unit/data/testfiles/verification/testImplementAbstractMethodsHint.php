<?php
//START

abstract class AbstractClass {
    abstract public function abstractFoo();
}

class ExtendingClass extends AbstractClass {

}

interface InterfaceName {
    public function abstractBar();
}

class ImplementingClass implements InterfaceName {

}

//END
?>