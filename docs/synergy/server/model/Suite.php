<?php

namespace Synergy\Model;

use Synergy\App\Synergy;
use Synergy\Misc\Util;
use Synergy\Controller\SpecRelationCtrl;

/**
 * Description of Suite
 *
 * @author vriha
 */
class Suite {

    //put your code here
    public $id;
    public $desc;
    public $title;
    public $product;
    public $component;
    public $controls;
    public $specificationId;
    public $specificationTitle;
    public $version;
    public $isUsed;

    /**
     *
     * @var TestCase[] 
     */
    public $testCases;
    public $estimation;
    public $url;
    public $order;

    /**
     * Associative array that holds information for extensions
     * @var array 
     */
    public $ext;

    function __construct($id, $desc, $title, $product, $component, $specification_id, $order = 1) {
        $this->url = BASER_URL . "suite.php?id=" . $id;
        $this->id = intval($id);
        $this->estimation = 0;
        $this->testCases = array();
        $this->desc = $desc;
        $this->title = $title;
        $this->product = $product;
        $this->component = $component;
        $this->specificationId = intval($specification_id);
        $this->order = intval($order);
        $this->ext = array();
        $this->isUsed = false;
    }

    function addControls($role) {
        $this->controls = array();
        switch ($role) {
            case "tester":
                $ctrl = new SpecRelationCtrl();
                if ($ctrl->isUserRelatedToSpec($this->specificationId, Synergy::getSessionProvider()->getUsername())) {
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

        if (count($this->testCases) > 0) {
            foreach ($this->testCases as $tcase) {
                $tcase->addControls($role);
            }
        }
    }

    /**
     * 
     * @param TestCase[] $cases
     */
    function setCases($cases) {
        $this->testCases = $cases;
        for ($i = 0, $max = count($cases); $i < $max; $i++) {
            $this->estimation+=$cases[$i]->duration;
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

    public static function canDelete($suiteId) {
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

    public static function canCreate($specificationId) {
        $role = Synergy::getSessionProvider()->getUserRole();
        switch ($role) {
            case "tester" :
                $ctrl = new SpecRelationCtrl();
                return $ctrl->isUserRelatedToSpec($specificationId, Synergy::getSessionProvider()->getUsername());
            case "privilegedTester":
            case "manager" :
            case "admin" :
                return true;
            default :
                return false;
        }
    }

    public function toString() {
        $s = "Test suite: " . $this->title . Util::$NEW_LINE;
        $s .= $this->desc . Util::$NEW_LINE;
        $s .= $this->product . " : " . $this->component;
        $s.= Util::$NEW_LINE;
        for ($i = 0, $max = count($this->testCases); $i < $max; $i++) {
            $s .= $this->testCases[$i]->toString() . Util::$NEW_LINE;
        }

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
        $this->disableCaseEditActions();
    }

    private function disableCaseEditActions() {
        foreach ($this->testCases as $tcase) {
            $tcase->disableEditActions();
        }
    }

}

?>
