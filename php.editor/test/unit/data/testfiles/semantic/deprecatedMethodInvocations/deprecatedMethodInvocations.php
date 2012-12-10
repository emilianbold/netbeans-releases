<?php
namespace First;

use Second\Delegate;

class ClassName {

    /** @var Delegate */
    private $delegate;

    function info($param) {
        $this->delegate->deprecatedFunction($param);
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