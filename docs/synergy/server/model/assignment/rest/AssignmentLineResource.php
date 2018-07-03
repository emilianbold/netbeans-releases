<?php

namespace Synergy\Model\Assignment\Rest;

/**
 * Description of AssignmentLineResource
 *
 * @author vriha
 */
class AssignmentLineResource {

    public $username;
    public $assignments;

    public static function createFromUser($user) {
        $i = new AssignmentLineResource();
        $i->username = $user->username;
        $i->assignments = RichAssignmentListItemResource::createFromAssignments($user->assignments);
        return $i;
    }

    public static function createFromUsers($users, $preserveKeys = false) {
        $list = array();
        if (!$preserveKeys) {
            for ($i = 0, $max = count($users); $i < $max; $i++) {
                array_push($list, AssignmentLineResource::createFromUser($users[$i]));
            }
        } else {
            foreach ($users as $key => $value) {
                $list[$key] = AssignmentLineResource::createFromUser($value);
            }
        }
        return $list;
    }

}
