<?php
namespace First;

use Second\Delegate;
use Second as S;

class ClassName {

    function info($param) {
        Delegate::FOO;
        \Second\Delegate::FOO;
        S\Delegate::FOO;
    }

}

namespace Second;

class Delegate {

    /** @deprecated */
    const FOO = 123;

}


?>