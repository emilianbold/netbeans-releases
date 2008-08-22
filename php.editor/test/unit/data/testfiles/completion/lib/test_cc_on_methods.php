<?php

  class ParentClass{
      /**
       * @return ParentClass
       */
      function parentInstance(){}
  }

  class TestCCOnMethods extends ParentClass {

  /**
   * @return TestCCOnMethods
   */
  function newInstance()
  {
      parent::parentInstance()->parentInstance();
      return self::create()->newInstance();
  }

  /**
   * @return TestCCOnMethods
   */
  static function create()
  {

  }
};

{
    $tst1 = new TestCCOnMethods();
    $tst2 = $tst1->newInstance()->newInstance();
}

TestCCOnMethods::create()->newInstance();

?>
