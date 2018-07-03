<?php

namespace Synergy\Controller;

use Synergy\App\Synergy;
use Synergy\DB\AssignmentDAO;
use Synergy\Controller\RunCtrl;
use Synergy\Misc\Util;
use Synergy\Model\TestAssignment;

/**
 * Description of AssignmentCtrl
 *
 * @author vriha
 */
class AssignmentCtrl {

    private $assignmentDao;
    private $tribeCtrl;
    private $runCtrl;
    public static $tribesByLeader = array();
    public static $latestRemovalType;

    function __construct() {
        $this->assignmentDao = new AssignmentDAO();
    }

    /**
     * 
     * @return TribeCtrl
     */
    public function getTribeCtrl() {
        if (is_null($this->tribeCtrl)) {
            $this->tribeCtrl = new TribeCtrl();
        }
        return $this->tribeCtrl;
    }

    /**
     * 
     * @return TribeCtrl
     */
    public function getRunCtrl() {
        if (is_null($this->runCtrl)) {
            $this->runCtrl = new RunCtrl();
        }
        return $this->runCtrl;
    }

    /**
     * Returns true if logged in user can delete assignment with ID assignmentId. User can delete assignment if:
     * <ul>
     * <li>User is admin or manager</li>
     * <li>given assignment was created as voluntarily and assignee === userId</li>
     * <li>assignment was created by tribe leader and specification belongs to the same tribe and assignee is member of the same tribe</li>
     * </ul>
     * @param int $assignmentId
     * @return boolean
     */
    public function userCanDeleteAssignmentById($assignmentId) {
        $assignmentMeta = $this->assignmentDao->getAssignentInfo($assignmentId);
        switch ($assignmentMeta["createdBy"]) {
            case TestAssignment::CREATED_BY_MANAGER_ADMIN:
                AssignmentCtrl::$latestRemovalType = TestAssignment::CREATED_BY_MANAGER_ADMIN;
                return TestAssignment::canDelete() || Synergy::getSessionProvider()->getUserId() === $assignmentMeta["userId"];
            case TestAssignment::CREATED_BY_TESTER:
                AssignmentCtrl::$latestRemovalType = TestAssignment::CREATED_BY_TESTER;
                return TestAssignment::canDelete() || Synergy::getSessionProvider()->getUserId() === $assignmentMeta["userId"];
            case TestAssignment::CREATED_BY_TRIBE_LEADER:
                AssignmentCtrl::$latestRemovalType = TestAssignment::CREATED_BY_TRIBE_LEADER;
                if (TestAssignment::canDelete()) {
                    return true;
                }
                // implicit check if assignee is member of tribe where logged in user is leader  - 2 checks passed
                $tribes = $this->getTribesForUser($this->getTribeForLeader(Synergy::getSessionProvider()->getUsername()), $assignmentMeta["userId"]);
                for ($i = 0, $max = count($tribes); $i < $max; $i++) {
                    for ($j = 0, $max2 = count($tribes[$i]->ext["specifications"]); $j < $max2; $j++) {
                        if (intval($tribes[$i]->ext["specifications"][$j]->id) === $assignmentMeta["specificationId"]) {
                            return true;
                        }
                    }
                }
                break;
            default:
                return false;
        }
        return false;
    }

    /**
     * Returns true if logged in user can delete assignment with ID assignmentId. User can delete assignment if:
     * <ul>
     * <li>User is admin or manager</li>
     * <li>given assignment was created as voluntarily and assignee === userId</li>
     * <li>assignment was created by tribe leader and specification belongs to the same tribe and assignee is member of the same tribe</li>
     * </ul>
     * @param TestAssignment $assignment
     * @return boolean
     */
    public function userCanDeleteAssignment($assignment) {
        switch ($assignment->createdBy) {
            case TestAssignment::CREATED_BY_MANAGER_ADMIN:
                return TestAssignment::canDelete() || Synergy::getSessionProvider()->getUserId() === intval($assignment->userId);
            case TestAssignment::CREATED_BY_TESTER:
                return TestAssignment::canDelete() || Synergy::getSessionProvider()->getUserId() === $assignment->userId;
            case TestAssignment::CREATED_BY_TRIBE_LEADER:
                if (TestAssignment::canDelete()) {
                    return true;
                }
                // implicit check if assignee is member of tribe where logged in user is leader  - 2 checks passed
                $tribes = $this->getTribesForUser($this->getTribeForLeader(Synergy::getSessionProvider()->getUsername()), $assignment->userId);

                for ($i = 0, $max = count($tribes); $i < $max; $i++) {
                    for ($j = 0, $max2 = count($tribes[$i]->ext["specifications"]); $j < $max2; $j++) {
                        if (intval($tribes[$i]->ext["specifications"][$j]->id) === intval($assignment->specificationId)) {
                            return true;
                        }
                    }
                }
                break;
            default:
                return false;
        }
        return false;
    }

    private function getTribesForUser($tribes, $userId) {
        $match = array();
        for ($i = 0, $max = count($tribes); $i < $max; $i++) {
            for ($j = 0, $max2 = count($tribes[$i]->members); $j < $max2; $j++) {
                if (intval($tribes[$i]->members[$j]->id) === $userId) {
                    array_push($match, $tribes[$i]);
                    break;
                }
            }
        }
        return $match;
    }

    private function getTribeForLeader($leaderUsername) {
        if (!array_key_exists($leaderUsername, AssignmentCtrl::$tribesByLeader)) {
            AssignmentCtrl::$tribesByLeader[$leaderUsername] = $this->getTribeCtrl()->getTribesByLeader($leaderUsername);
        }

        return AssignmentCtrl::$tribesByLeader[$leaderUsername];
    }

    /**
     * Checks if similar assignment exists in given test run
     * @param type $assigneeUsername
     * @param type $platformId
     * @param type $labelId
     * @param type $specificationId
     * @param type $testRunId
     * @return boolean true if similar assignment exists, false otherwise
     */
    public function assignmentExists($assigneeUsername, $platformId, $labelId, $specificationId, $testRunId) {
        return is_null($this->assignmentDao->findSimilar($assigneeUsername, $platformId, $labelId, $specificationId, $testRunId)) ? false : true;
    }

    public function updateIssues($data, $assignmentId) {
        $issues = $data->issues;
        $diff = intval($data->diffCount);
        $a = $this->assignmentDao->getAssignment($assignmentId);
        
        $passed = max(0, min($a->total, $a->passed + $diff));
        $failed = max(0, min($a->total, $a->failed - $diff));
        return $this->assignmentDao->updateIssues($issues, $passed, $failed, $assignmentId);
    }

}
