<?php

namespace Synergy\Model;

/**
 * Description of UserStatistics
 *
 * @author vriha
 */
class UserStatistics {

    //put your code here
    public $assignments;
    public $totals;
    public $tribes;
    private $processedTribes;
    public $name;
    
    function __construct() {
        $this->assignments = array();
        $this->tribes = array();
        $this->processedTribes = array();
    }

    /**
     * 
     * @param Membership[] $memberships
     */
    public function addMembership($memberships){
        for($i=0, $max = count($memberships); $i < $max;$i++){
            if(!array_key_exists($memberships[$i]->name."_", $this->processedTribes)){
                array_push($this->tribes, $memberships[$i]);
                $this->processedTribes[$memberships[$i]->name."_"] = 1;
            }
        }
    }
    
    /**
     * 
     * @param TestAssignment $assignment
     */
    public function addAssignment($assignment){
        $this->name = $assignment->userDisplayName;
        $sa = new SimpleAssignment($assignment->id, $assignment->issues, $assignment->specificationId);
        $sa->totalCases= intval($assignment->total);
        $sa->completedCases = intval($assignment->completed);
        $sa->totalTime = intval($assignment->timeToComplete);
        $sa->passedCases = intval($assignment->passed);
        $sa->lastUpdated = $assignment->lastUpdated;
        array_push($this->assignments,$sa);
    }
    
    /**
     * 
     * @param UserStatistics[] $assignments
     */
    public static function getDistinctTribeIds($assignments){
        $ids = array();
        foreach ($assignments as $key => $assignment) {
            for ($j = 0, $max = count($assignment->tribes); $j < $max; $j++) {
                if(!in_array($assignment->tribes[$j]->id, $ids)){
                    array_push($ids, $assignment->tribes[$j]->id);
                }
            }
        }
        return $ids;
    }
}

class SimpleAssignment{
    public $issues;
    public $id;
    public $specificationId;
    public $totalCases;
    public $completedCases;
    public $passedCases;
    public $totalTime;
    public $lastUpdated;
    
    
    function __construct($id, $issues, $specId) {
        $this->id = intval($id);
        $this->issues = $issues;
        $this->specificationId = intval($specId);
    }
}