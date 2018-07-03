<?php

namespace Synergy\Model;

use Synergy\App\Synergy;

/**
 * Description of TestRun
 *
 * @author vriha
 */
class TestRun {

    public $membersCount = 0;
    public $completed = 0;
    public $total = 0;
    public $title = '';
    public $desc = '';
    public $id = -1;
    public $start = '';
    public $end = '';
    public $assignments = array();
    public $reviewAssignments;
    public $attachments = array();
    public $status = "pending";
    public $isActive = 1;
    public $notifications;
    public $projectName;
    public $projectId;
    private $bugTrackingSystem;
    public $blobs = array();
    public $durations = array();
    
    function __construct($title, $id, $start, $end) {
        $this->title = $title;
        $this->id = intval($id);
        $this->start = $start;
        $this->end = $end;
        $this->bugTrackingSystem = "";
        $this->reviewAssignments = array();
    }
    
    public function getBugTrackingSystem() {
        if(is_null($this->bugTrackingSystem) || strlen($this->bugTrackingSystem) < 1){
            $this->bugTrackingSystem = "other";
        }
        return $this->bugTrackingSystem;
    }

    public function setBugTrackingSystem($bugTrackingSystem) {
        $this->bugTrackingSystem = $bugTrackingSystem;
    }

    
    function addControls($role) {
        $this->controls = array();
        switch ($role) {
            case "admin":
            case "manager":
                array_push($this->controls, new Action("Edit", "edit", "icon-pencil"));
                array_push($this->controls, new Action("Delete", "delete", "icon-trash"));
                array_push($this->controls, new Action("Notify", "notify", "icon-envelope"));
                array_push($this->controls, new Action("Toggle Freeze", "freeze", "icon-stop"));
                break;
            default:
                break;
        }
    }

    public static function canEdit() {
        $role = Synergy::getSessionProvider()->getUserRole();
        switch ($role) {
            case "manager":
            case "admin" :
                return true;
            default :
                return false;
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
    
    public function getUpdatedEmailBody(){
        return "Title: " . $this->title. " \r\n Description: " . $this->desc." \r\n Start: ".$this->start." \r\n End: ".$this->end;
    }

    public function getUpdatedEmailBodyHTML() {
        return "<ul><li><b>Title:</b> <a href='".SYNERGY_URL."client/app/index.html#/run/" . $this->id . "'>" . $this->title . "</a></li><li><b>Description:</b> " . $this->desc . " </li><li><b>Start:</b> " . $this->start . " </li><li><b> End:</b> " . $this->end . "</li></ul>";
    }

    public function getCUpdatedEmailSubject() {
        return "Test run updated";
    }

}

?>
