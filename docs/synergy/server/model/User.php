<?php

namespace Synergy\Model;

use Synergy\App\Synergy;

/**
 * Description of User
 *
 * @author lada
 */
class User {

    //put your code here
    public $username;
    public $firstName;
    public $lastName;
    public $role;
    public $url;
    public $id;
    public $membership;  
    public $favorites;
    public $authorOf;
    public $ownerOf;
    public $emailNotifications;
    public $assignments;  
    public $profileImg;
    public $email;

    function __construct($username) {
        $this->membership = array();
        $this->favorites = array();
        $this->authorOf = array();
        $this->ownerOf = array();
        $this->profileImg = "./img/user.png";
        $this->assignments = array();
        $this->url = BASER_URL . "user.php?user=" . $username;
        $this->username = $username;
        $this->emailNotifications = true;
    }

    public function addControls($role) {
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

    public function addAsignment($assignment) {
        array_push($this->assignments, $assignment);
    }

}

?>
