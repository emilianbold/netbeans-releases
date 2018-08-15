<?php

namespace Synergy\Model\Project;

use Synergy\App\Synergy;
use Synergy\Model\Action;

/**
 * Description of Project
 *
 */
class Project {

    public $id;
    public $name;
    public $controls;
    public $viewLink;
    public $multiViewLink;
    public $reportLink;
    public $bugTrackingSystem;

    function __construct($id, $name) {
        $this->id = intval($id, 10);
        $this->name = $name;
    }

    public function setBugTrackingSystem($bugTrackingSystem) {
        $this->bugTrackingSystem = $bugTrackingSystem;
        if (is_null($bugTrackingSystem) || strlen($bugTrackingSystem) < 1) {
            $this->bugTrackingSystem = "other";
        }
    }

    function addControls($role) {
        $this->controls = array();
        switch ($role) {
            case "admin":
            case "manager":
                array_push($this->controls, new Action("Edit", "edit", "icon-pencil"));
                array_push($this->controls, new Action("Delete", "delete", "icon-trash"));
                break;
            default:
                break;
        }
    }

    public static function canEdit() {
        $role = Synergy::getSessionProvider()->getUserRole();
        switch ($role) {
            case "manager" :
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

}
