<?php

namespace Synergy\Model\Assignment\Rest;

/**
 * Description of TestAssignmentListItemRest
 *
 * @author vriha
 */
class AssignmentListItemResource {

    public $userId;
    public $userDisplayName;
    public $username;
    public $platform;
    public $testRunId;
    public $testRunTitle;
    public $label;
    public $labelId;
    public $completed;
    public $total;
    public $specification;
    public $specificationId;
    public $state;
    public $info;
    public $id;
    public $failed;
    public $passed;
    public $skipped;
    public $lastUpdated;
    public $started;
    public $createdBy;
    public $projectName;

    public function __construct($assignment) {
        $this->userId = $assignment->userId;
        $this->userDisplayName = $assignment->userDisplayName;
        $this->username = $assignment->username;
        $this->platform = $assignment->platform;
        $this->testRunId = $assignment->testRunId;
        $this->testRunTitle = $assignment->testRunTitle;
        $this->label = $assignment->label;
        $this->labelId = $assignment->labelId;
        $this->completed = $assignment->completed;
        $this->total = $assignment->total;
        $this->specification = $assignment->specification;
        $this->specificationId = $assignment->specificationId;
        $this->state = $assignment->state;
        $this->info = $assignment->info;
        $this->id = $assignment->id;
        $this->failed = $assignment->failed;
        $this->passed = $assignment->passed;
        $this->skipped = $assignment->skipped;
        $this->lastUpdated = $assignment->lastUpdated;
        $this->started = $assignment->started;
        $this->projectName = $assignment->testRunProjectName;
        $this->createdBy = $assignment->createdBy;
    }

    public static function createFromAssignment($assignment) {
        return new AssignmentListItemResource($assignment);
    }

    public static function createFromAssignments($assignments) {
        $list = array();

        for ($i = 0, $max = count($assignments); $i < $max; $i++) {
            array_push($list, AssignmentListItemResource::createFromAssignment($assignments[$i]));
        }
        return $list;
    }

}
