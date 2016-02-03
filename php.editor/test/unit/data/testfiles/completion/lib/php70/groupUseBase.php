<?php
namespace A;

class ClsA {
    public function test() {
        echo 'ClsA' . PHP_EOL;
    }
}

namespace A\B;

class ClsAB {
    public function test() {
        echo 'ClsAB' . PHP_EOL;
    }
}

namespace A\B\C;

class ClsABC {
    public function test() {
        echo 'ClsABC' . PHP_EOL;
    }
}
