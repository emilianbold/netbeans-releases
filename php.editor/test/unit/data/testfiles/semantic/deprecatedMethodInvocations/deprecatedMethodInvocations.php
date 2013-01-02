<?php
namespace First;

use Second\Delegate;

class ClassName {

    /** @var Delegate */
    private $delegate;

    function info($param) {
        $this->delegate->deprecatedFunction($param);
        $this->delegate->deprecatedFunction($param)->foo();
        $this->delegate->deprecatedFunction($param)->bar;
        Delegate::deprecatedStaticFunction($param);
    }

}

namespace Second;

class Delegate {

    /** @deprecated */
    public function deprecatedFunction($param) {}

    /** @deprecated */
    public static function deprecatedStaticFunction($param) {}

}

?>