<?php
namespace Synergy\Model\Exception;

use Exception;
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * Description of GeneralException
 *
 * @author vriha
 */
class GeneralException extends Exception{

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

?>
