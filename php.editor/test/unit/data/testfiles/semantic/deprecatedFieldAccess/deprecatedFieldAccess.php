<?php
namespace First;

use Second\Delegate;

class ClassName {

    /**
     * @var Delegate
     * @deprecated
     */
    private $delegate;

    function info($param) {
        Delegate::$staticField;
        $this->delegate->deprPub;
    }

}

namespace Second;

class Delegate {

    /** @deprecated */
    public $deprPub;

    /** @deprecated */
    public static $staticField;

}


?>