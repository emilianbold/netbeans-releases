<?php

namespace Synergy\Model\Statistics\Rest;

use Synergy\Model\Assignment\Rest\AssignmentStatisticsResource;

/**
 * Description of StatisticsLineResource
 *
 * @author vriha
 */
class StatisticsLineResource {

    public $assignments;
    public $tribes;
    public $name;

    public static function createFromUsers($users) {
        $list = array();
        foreach ($users as $key => $value) {
            $list[$key] = StatisticsLineResource::createFromUser($value);
        }
        return $list;
    }

    public static function createFromUser($user) {
        $i = new StatisticsLineResource();
        $i->name = $user->name;
        $i->tribes = $user->tribes;
        $i->assignments = AssignmentStatisticsResource::createFromAssignments($user->assignments);
        return $i;
    }

}
