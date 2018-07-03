<?php

namespace Synergy\Model\Assignment\Rest;

use Synergy\Model\Bug\Rest\BugResource;

/**
 * Description of RichTAListItemResource
 *
 * @author vriha
 */
class RichAssignmentListItemResource extends AssignmentListItemResource {

    public $tribes;
    public $controls;
    public $issues;

    function __construct($assignment) {
        parent::__construct($assignment);
    }

    public static function createFromAssignment($assignment) {
        $i = new RichAssignmentListItemResource($assignment);
        $i->tribes = $assignment->tribes;
        $i->controls = $assignment->controls;
        $vars = get_object_vars($assignment);
        if(array_key_exists("issues", $vars)) {
            $i->issues = BugResource::createFromBugs($assignment->issues);
        } else {
            $i->issues = array();
        }
        return $i;
    }

    public static function createFromAssignments($assignments) {
        $list = array();
        for ($i = 0, $max = count($assignments); $i < $max; $i++) {
            array_push($list, RichAssignmentListItemResource::createFromAssignment($assignments[$i]));
        }
        return $list;
    }

}
