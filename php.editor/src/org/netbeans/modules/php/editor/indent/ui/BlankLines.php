<?php
namespace MyProject;
use YourProject;
use My\Full\Classname as Another;
// this is the same as use My\Full\NSname as NSname
use My\Full\NSname;
// importing a global class
use \ArrayObject;
const CONNECT_OK = 1;
class Connection {
private $field1;
private $field3 = "example";
public function method($text, $number){
}
}
function connect() {
}
namespace AnotherProject;
const CONNECT_OK = 1;
class Connection {
    /**
     * comment
     */
    public $field;
}
function connect() {
}
?>
