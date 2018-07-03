<?php

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

namespace Synergy\Model\Exception;

use Exception;

/**
 * Description of UserException
 *
 * @author vriha
 */
class UserException extends Exception {

    public $title;
    public $message;
    public $location;

    public function __construct($title, $message, $location) {
        // parent::__construct($message, $code, $previous); PHP >= 5.3 
        parent::__construct($message, -1);
        $this->title = $title;
        $this->message = $message;
        $this->location = $location;
    }

}
