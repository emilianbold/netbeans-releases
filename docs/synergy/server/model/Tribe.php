<?php

namespace Synergy\Model;

use Synergy\App\Synergy;

/**
 * Description of Tribe
 *
 * @author vriha
 */
class Tribe {

    //put your code here
    public $id;
    public $name;
    public $description;
    public $leader_id;
    public $leaderUsername;
    public $url;
    public $members;
    public $controls;
    public $leaderDisplayName;
    public $leaderImg;

    /**
     * Associative array that holds information for extensions
     * @var array 
     */
    public $ext;

    public function __construct($id, $name, $description, $leader_id) {
        $this->url = BASER_URL . "tribe.php?id=" . $id;
        $this->id = intval($id);
        $this->name = $name;
        $this->members = array();
        $this->controls = array();
        $this->description = $description;
        $this->leader_id = intval($leader_id);
        $this->ext = array();
    }

    public function addControls($role) {
        $this->controls = array();
        switch ($role) {
            case "admin":
            case "manager":
            case "leader":
                array_push($this->controls, new Action("Edit", "edit", "icon-pencil"));
                break;
            default:
                break;
        }
    }

    public static function canEdit() {
        $role = Synergy::getSessionProvider()->getUserRole();
        switch ($role) {
            case "manager" :
            case "leader" :
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
    
    public static function init($name, $leaderUsername){
        $self = new self(-1, $name, '', -1);
        $self->leaderUsername = $leaderUsername;
        return $self;
    }

}

?>