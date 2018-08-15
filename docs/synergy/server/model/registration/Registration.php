<?php

namespace Synergy\Model\Registration;

/**
 * Description of Registration
 *
 */
class Registration {

    public $email;
    public $username;
    public $password;
    public $firstname;
    public $lastname;

    function __construct($email, $username, $password, $lastname, $firstname) {
        $this->email = $email;
        $this->username = $username;
        $this->firstname = $firstname;
        $this->lastname = $lastname;
        $this->password = $password;
    }

}
