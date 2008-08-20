<?php
  class TestOptionalArgsClass{
      static function test($a = 1, $b, $c = 1, $d){}
  }

  function testOptionalArgsFunc($a, $b = 1){}

  TestOptionalArgsClass::test($b, $d);
  $foo = testOptionalArgsFunc($a);
?>