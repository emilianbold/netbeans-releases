<?php
namespace Run;

use A\{
    ClsA,
    B\ClsAB,
    B\C\ClsABC
};

$a = new ClsA();
$a->test();
$ab = new ClsAB();
$ab->test();
$abc = new ClsABC();
$abc->test();
