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

class ImplementA {
    function foo() {}
}
interface ImplementB {
    function foo();
}
class ImplementC extends ImplementA implements ImplementB {

}

interface B {
    function example();
}

trait X {
    function example() { }
}

class A implements B {
    use X;
}

//END
?>