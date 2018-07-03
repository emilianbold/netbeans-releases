<?php

namespace Synergy\Model;

use Synergy\App\Synergy;
use Synergy\Controller\AssignmentCtrl;

/**
 * Description of TestAssignment
 *
 * @author vriha
 */
class TestAssignment {

    public $userId;
    public $userDisplayName;
    public $username;
    public $platform;
    public $testRunId;
    public $testRunTitle;
    public $label = '';
    public $labelId;
    public $completed = 0;
    public $total = 0;
    public $specification;
    public $specificationId;
    public $state;
    public $info;
    public $id;
    public $progress;
    public $specificationData;
    public $failed;
    public $passed;
    public $skipped;
    public $deadline;
    public $lastUpdated;
    public $started;
    public $timeToComplete;
    public $issues;
    private $tribesId;
    public $tribes;
    public $createdBy;
    private $removalComment;
    private static $assignmentCtrl;
    private $email;
    public $testRunProjectName;

    const CREATED_BY_TESTER = 2;
    const CREATED_BY_MANAGER_ADMIN = 1;
    const CREATED_BY_TRIBE_LEADER = 3;

    function __construct($username, $platform, $testRunId, $label, $total) {
        $this->username = $username;
        $this->platform = $platform;
        $this->testRunId = intval($testRunId);
        if (strlen($label) > 0) {
            $this->label = $label;
        } else {
            $this->label = "";
            $this->labelId = -1;
        }
        $this->total = intval($total);
        $this->issues = array();
        $this->timeToComplete = 0;
        $this->createdBy = TestAssignment::CREATED_BY_MANAGER_ADMIN;
    }

    public function getEmail() {
        return $this->email;
    }

    public function setEmail($email) {
        $this->email = $email;
    }

        
    public static function canCreate() {
        $role = Synergy::getSessionProvider()->getUserRole();
        switch ($role) {
            case "manager" :
            case "admin" :
                return true;
            default :
                return false;
        }
    }

    function addControls($role) {
        $this->controls = array();
        switch ($this->createdBy) {
            case TestAssignment::CREATED_BY_TESTER:
                if ($this->username === Synergy::getSessionProvider()->getUsername() || $role === "admin" || $role === "manager") {
                    array_push($this->controls, new Action("Delete", "delete", "icon-trash"));
                }
                break;
            case TestAssignment::CREATED_BY_MANAGER_ADMIN:
                if ($this->username === Synergy::getSessionProvider()->getUsername() || $role === "admin" || $role === "manager") {
                    array_push($this->controls, new Action("Delete", "delete", "icon-trash"));
                }
                break;
            case TestAssignment::CREATED_BY_TRIBE_LEADER:
                if (is_null(TestAssignment::$assignmentCtrl)) {
                    TestAssignment::$assignmentCtrl = new AssignmentCtrl();
                }
                if (TestAssignment::$assignmentCtrl->userCanDeleteAssignment($this)) {
                    array_push($this->controls, new Action("Delete", "delete", "icon-trash"));
                }
                break;
            default:
                break;
        }
    }

    /**
     * Returns body of email that is supposed to be sent on new assignment creation
     * @return String
     */
    public function getCreatedEmailBody() {
        return "Specification: " . $this->specification . " \r\n Platform: " . $this->platform . "\r\n Test run: " . $this->testRunTitle . " \r\n Finished by: " . $this->deadline;
    }

    public function getCreatedEmailBodyHTML() {
        return "<ul><li><b>Specification:</b> <a href='".SYNERGY_URL."client/app/index.html#/specification/" . $this->specificationId . "'>" . $this->specification . "</a></li><li><b>Platform:</b> " . $this->platform . "</li><li><b>Test run:</b> <a href='".SYNERGY_URL."client/app/index.html#/run/" . $this->testRunId . "'>" . $this->testRunTitle . "</a> </li><li><b>Finished by:</b> " . $this->deadline . "</li></ul>";
    }

    /**
     * Returns subject of email that is supposed to be sent on new assignment creation
     * @return String
     */
    public function getCreatedEmailSubject() {
        return "New test assignment";
    }

    public function getRemovedEmailSubject() {
        return "Tribe leader removed your assignment";
    }

    public function setLastUpdated($updatedDateTime) {
        $this->lastUpdated = "";
        if (isset($updatedDateTime) && !is_null($updatedDateTime)) {
            date_default_timezone_set('UTC');
            $str = strtotime($updatedDateTime);
            $this->lastUpdated = gmdate("d M Y H:i:s", $str) . " UTC";
        }
    }

    public function setStarted($startedDateTime) {
        $this->started = "";
        if (isset($startedDateTime) && !is_null($startedDateTime)) {
            date_default_timezone_set('UTC');
            $str = strtotime($startedDateTime);
            $this->started = gmdate("d M Y H:i:s", $str) . " UTC";
        }
    }

    public function getTribesId() {
        return isset($this->tribesId) ? $this->tribesId : array();
    }

    public function setTribesId($tribesId) {
        $this->tribesId = array();
        if (isset($tribesId)) {
            $expl = explode(";", $tribesId);
            for ($i = 0, $max = count($expl); $i < $max; $i++) {
                array_push($this->tribesId, intval($expl[$i]));
            }
        }
    }

    public function reset() {
        $this->completed = "0";
        $this->state = "";
        $this->info = "pending";
    }

    public function addRemovalComment($text) {
        $this->removalComment = $text;
    }

    public function getRemovalComment() {
        return $this->removalComment;
    }

    public static function canDelete() {
        $role = Synergy::getSessionProvider()->getUserRole();
        switch ($role) {
            case "manager" :
            case "admin" :
                return true;
            default :
                return false;
        }
    }

}

?>