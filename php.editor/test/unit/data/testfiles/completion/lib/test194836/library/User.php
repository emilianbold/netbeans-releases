<?php
namespace User;
require_once 'UserAbstract.php';

class User extends UserAbstract{
    public function getLastName() {
        return $this->_lastName;
    }
}

