<?php
namespace User;
require 'UserInterface.php';

abstract class UserAbstract implements UserInterface{
    protected $_name;
    protected $_lastName;
    public function setName($name) {
        $this->_name = $name;
    }
    
    public function setLastName($lastName) {
        $this->_lastName = $lastName;
    }

    public function getName() {
        return $this->_name;
    }



}

?>
