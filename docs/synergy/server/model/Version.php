<?php

namespace Synergy\Model;

use Synergy\App\Synergy;

/**
 * Description of Version
 *
 * @author vriha
 */
class Version {

    //put your code here

    public $id;
    public $name;
    public $isObsolete;
    public $controls;

    const VERSION_PATTERN = '/(^\d+\.)(.*)/';

    function __construct($id, $name, $isObsolete = 0) {
        $this->id = intval($id);
        $this->name = $name;
        $this->isObsolete = intval($isObsolete);
    }

    public function getId() {
        return $this->id;
    }

    public function setId($id) {
        $this->id = $id;
        return $this;
    }

    public function getName() {
        return $this->name;
    }

    public function setName($name) {
        $this->name = $name;
        return $this;
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

    /**
     * Returns float representation of version string. If version string contains multiple
     * dots, the first dot is used as floating point and rest of the dots are removed (8.0.1 becomes 8.01)
     * @param String $version string representation of version (e.g. 8.0, 8.0.1)
     * @return float float value of version
     */
    public static function toFloat($version) {
        $r = preg_match(Version::VERSION_PATTERN, $version, $matches);
        if ($r) {
            $p = str_replace(".", "", $matches[2]);
            return floatval(preg_replace(Version::VERSION_PATTERN, '${1}' . $p, $version));
        }
        return floatval($version);
    }

}

?>
