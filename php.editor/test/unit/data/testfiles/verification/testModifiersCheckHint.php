<?php
//START

class FinalAndAbstractFields {

    private static final $finalStaticField;

    private static abstract $abstractStaticField;

    private final $finalField;

    private abstract $abstractField;

}

class ClassMethods {

    public abstract final function classAbstractAndFinal() {

    }

    public abstract function classWithBody() {

    }

    abstract private function classAbstractPrivate();

}

interface IfaceMethods {

    private function ifacePrivateMethod();

    protected function ifaceProtectedMethod();

    public final function ifaceFinalMethod();

    public function ifaceWithBody() {

    }

}

class PossibleAbstract {

    abstract public function possibleAbstract();

}

final class FinalAbstract {

    abstract public function finalAbstract();

}

//END
?>