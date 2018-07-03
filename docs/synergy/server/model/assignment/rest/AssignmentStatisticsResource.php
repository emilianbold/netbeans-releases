<?php

namespace Synergy\Model\Assignment\Rest;

/**
 * Description of AssignmentStatisticsResource
 *
 * @author vriha
 */
class AssignmentStatisticsResource {

    public $issues;   
    public $id;
    public $specificationId;
    public $totalCases;
    public $completedCases;
    public $passedCases;
    public $totalTime;

    public static function createFromAssignment($assignment) {
        $i = new AssignmentStatisticsResource();
        $i->completedCases = $assignment->completedCases;
        $i->id = $assignment->id;
        $i->issues = $assignment->issues;
        $i->passedCases = $assignment->passedCases;
        $i->specificationId = $assignment->specificationId;
        $i->totalCases = $assignment->totalCases;
        $i->totalTime = $assignment->totalTime;
        return $i;
    }

    public static function createFromAssignments($assignments) {
        $list = array();
        foreach ($assignments as $key => $value) {
            array_push($list, AssignmentStatisticsResource::createFromAssignment($value));
        }
        return $list;
    }

}
