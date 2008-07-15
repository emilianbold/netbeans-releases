<?php
    class person {                                // class name
        private $name;                            // class field declaration
        public $me = "mydefaultname";             // class field declaration
        private $you;                             // unused private class field
        static private $test = 0;                 // static private class field

        public function __construct($name) {      // method name
            $this->name = $name;                  // usage of class field
            echo $this->$name."\n";               // $name is on class field
            echo $this->name."\n";                // usage of class field
            person::$test = person::$test + 1;
        }

        private function yourName() {             // unused method
            return "yourName";
        }

        public function name() {                  // method name
            return $this->name;                   // usage of class field
        }

        public static function getCount() {       // method name
            return person::$test;                 // usage of static field
        }
    }

    $p = new person("me");
    echo "persons: ".person::getCount();          // usage of static method
?>
