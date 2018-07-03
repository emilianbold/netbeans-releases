<?php

namespace Synergy\Model\Exception;

use Exception;

/**
 * Description of AssignmentConflictException
 *
 * @author vriha
 */
class AssignmentConflictException extends Exception {

    public $title;
    public $message;
    public $location;

    public function __construct($title, $message, $location) {
        parent::__construct($message, -1);
        $this->title = $title;
        $this->message = $message;
        $this->location = $location;
    }

}
