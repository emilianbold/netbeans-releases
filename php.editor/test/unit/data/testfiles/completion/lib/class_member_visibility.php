<?php
  class ParentClass{
      public function publicParentMethod(){
          self::privateParentMethod();
      }

      protected function protectedParentMethod(){}

      private function privateParentMethod(){}
  }

  class ChildClass extends ParentClass{
      function publicChildMethod(){
          parent::protectedParentMethod();
      }

      private function privateChildMethod(){}
  }

  $tst = new ChildClass;
  $tst->publicChildMethod();
?>