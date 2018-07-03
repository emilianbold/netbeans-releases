<?php

namespace Synergy\Model\Review;

use Synergy\App\Synergy;
use Synergy\Model\Action;
use Synergy\Model\TestAssignment;

/**
 * Description of ReviewAssignment
 *
 * @author vriha
 */
class ReviewAssignment {

    public $userId;
    public $userDisplayName;
    public $username;
    public $testRunId;
    public $testRunTitle;
    public $info;
    public $id;
    public $comments;
    public $deadline;
    public $lastUpdated;
    public $started;
    public $createdBy;
    public $reviewUrl;
    public $title;
    public $owner;
    public $isFinished;
    public $weight;
    public $timeTaken;
    public $numberOfComments;
    private $email;
    const CREATED_BY_TESTER = 2;
    const CREATED_BY_MANAGER_ADMIN = 1;
    const CREATED_BY_TRIBE_LEADER = 3;

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
    
    public function getEmail() {
        return $this->email;
    }

    public function setEmail($email) {
        $this->email = $email;
    }

    
    function addControls($role) {
        $this->controls = array();
        array_push($this->controls, new Action("View comments", "view", "icon-eye-open"));
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
            default:
                break;
        }
    }

    /**
     * Returns body of email that is supposed to be sent on new assignment creation
     * @return String
     */
    public function getCreatedEmailBody() {
        return "Review URL: " . $this->reviewUrl . "\r\n Test run: " . $this->testRunTitle . " \r\n Finished by: " . $this->deadline;
    }

    public function getCreatedEmailBodyHTML() {
        return "<ul><li><b>Review URL:</b> <a href='" . $this->reviewUrl . "'>" . $this->reviewUrl . "</a></li><li><b>Test run:</b> <a href='" . SYNERGY_URL . "client/app/index.html#/run/" . $this->testRunId . "'>" . $this->testRunTitle . "</a> </li><li><b>Finished by:</b> " . $this->deadline . "</li></ul>";
    }

    public function getFinishedEmailBody() {
        return "Title: " . $this->title . " \r\n URL: " . $this->reviewUrl . "\r\n  Reviewed by: " . $this->userDisplayName . " (" . $this->username . ") \r\n \r\n Comments are available at " . SYNERGY_URL . "client/app/index.html#/review/" . $this->id . "/view";
    }

    public function getFinishedEmailBodyHTML() {
        return "<ul><li><b>Title: </b> " . $this->title . "</li><li><b>URL:</b> <a href='" . $this->reviewUrl . "'>" . $this->reviewUrl . "</a></li><li><b>Reviewed by:</b> " . $this->userDisplayName . "(" . $this->username . ")</li></ul><br/>Comments are available <a href='" . SYNERGY_URL . "client/app/index.html#/review/" . $this->id . "/view'>here</a>";
    }

    /**
     * Returns subject of email that is supposed to be sent on new assignment creation
     * @return String
     */
    public function getCreatedEmailSubject() {
        return "New review assignment";
    }

    public function getFinishedEmailSubject() {
        return "Tutorial reviewed";
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
