<?php

namespace Synergy\Model;

use Synergy\App\Synergy;
use Synergy\Misc\Util;
use Synergy\Controller\SpecRelationCtrl;

/**
 * Description of TestCase
 *
 * @author lada
 */
class TestCase {

    public $title;
    public $duration;
    public $originalDuration;
    public $id;
    public $version_id;
    public $keywords;
    public $controls;
    public $issues;
    public $steps;
    public $result;
    public $specificationTitle;
    public $specificationId;
    public $suiteTitle;
    public $suiteId;
    public $version;
    public $url;
    public $images;
    public $isUsed;

    /**
     * Associative array that holds information for extensions
     * @var array 
     */
    public $ext;
    public $order;

    function __construct($title, $duration, $id, $order = 1) {
        $this->isUsed = false;
        $this->url = BASER_URL . "case.php?id=" . $id;
        $this->title = $title;
        $this->duration = $duration;
        $this->originalDuration = $duration;
        $this->id = intval($id);
        $this->ext = array();
        $this->images = array();
        $this->keywords = array();
        $this->controls = array();
        $this->issues = array();
        $this->order = intval($order);
    }

    /**
     * Split $keywords string to array by $separator and sets this array to $this->keywords
     * @param String $keywords
     * @param String $separator
     */
    public function setKeywords($keywords, $separator) {
        if (isset($keywords) && strlen($keywords) > 0) {
            $this->keywords = explode($separator, $keywords);
        }
    }

    function addControls($role) {
        $this->controls = array();
        switch ($role) {
            case "tester":
                $ctrl = new SpecRelationCtrl();
                if ($ctrl->isUserRelatedToSuite($this->suiteId, Synergy::getSessionProvider()->getUsername())) {
                    array_push($this->controls, new Action("Edit", "edit", "icon-pencil"));
                    array_push($this->controls, new Action("Delete", "delete", "icon-trash"));
                }
                break;
            case "privilegedTester":
            case "admin":
            case "manager":
            case "leader":
                array_push($this->controls, new Action("Edit", "edit", "icon-pencil"));
                array_push($this->controls, new Action("Delete", "delete", "icon-trash"));
                break;
            default:
                break;
        }

        if (isset($this->images) && is_array($this->images) && count($this->images) > 0) {
            foreach ($this->images as $img) {
                $img->addControls($role, $this->suiteId);
            }
        }
    }

    public static function canEdit($suiteId) {
        $role = Synergy::getSessionProvider()->getUserRole();
        switch ($role) {
            case "tester" :
                $ctrl = new SpecRelationCtrl();
                return $ctrl->isUserRelatedToSuite($suiteId, Synergy::getSessionProvider()->getUsername());
            case "privilegedTester":
            case "manager" :
            case "admin" :
                return true;
            default :
                return false;
        }
    }

    public static function canCreate($suiteId) {
        $role = Synergy::getSessionProvider()->getUserRole();
        switch ($role) {
            case "tester" :
                $ctrl = new SpecRelationCtrl();
                return $ctrl->isUserRelatedToSuite($suiteId, Synergy::getSessionProvider()->getUsername());
            case "manager" :
            case "privilegedTester":
            case "admin" :
                return true;
            default :
                return false;
        }
    }

    public function toString() {
        $s = "Test case: " . $this->title . Util::$NEW_LINE;
        $s .= "Duration: " . $this->duration . Util::$NEW_LINE;
        $s.= Util::$NEW_LINE;
        for ($i = 0, $max = count($this->keywords); $i < $max; $i++) {
            $s.= $this->keywords[$i] . ";";
        }

        $s.= Util::$NEW_LINE;
        $s .= "Steps: " . $this->steps . Util::$NEW_LINE;
        $s .= "Expected result: " . $this->result . Util::$NEW_LINE;
        return $s;
    }

    public function disableEditActions() {
        $this->isUsed = true;
        for ($i = 0, $max = count($this->controls); $i < $max; $i++) {
            switch ($this->controls[$i]->onClick) {
                case "delete":
                    $this->controls[$i]->isEnabled = false;
                    break;
                default:
                    break;
            }
        }
    }

}

?>
