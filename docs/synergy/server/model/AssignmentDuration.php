<?php

namespace Synergy\Model;

/**
 * Description of AssignmentDuration
 *
 * @author vlriha
 */
class AssignmentDuration {

    public $id;
    public $duration;

    function __construct($id, $duration) {
        $this->id = intval($id, 10);
        $this->duration = intval($duration, 10);
    }

}
