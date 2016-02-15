<?php
namespace My;

$anon = new class {
    public function testAnon() {
        $this->testBnon();
    }

    private function testBnon() {
        echo 'testBnon' . PHP_EOL;
    }
};
$anon->testAnon();
