<?php

namespace Synergy\Model;

use Synergy\App\Synergy;
use Synergy\Misc\Util;
use Synergy\Controller\SpecRelationCtrl;
use Synergy\Model\Project\ProjectListItem;

/**
 * Description of Specification
 *
 * @author lada
 */
class Specification {

    //put your code here
    public $id;
    public $desc;
    public $title;
    public $simpleName = "";
    public $versionId;
    public $controls;
    public $authorId;
    public $author;
    public $authorName;
    public $ownerId;
    public $owner;
    public $ownerName;
    public $version;
    public $similar;
    public $isUsed;

    /**
     *
     * @var Suite[]
     */
    public $testSuites;
    public $attachments;
    public $estimation;
    public $url;
    public $isFavorite = 0;
    public $lastUpdated;
    public $userIsRelated;
    public $ownerRole;

    /**
     * Associative array that holds information for extensions
     * @var array 
     */
    public $ext;

    function __construct($id, $desc, $title, $version, $author, $ownerId) {
        $this->url = BASER_URL . "specification.php?id=" . $id;
        $this->id = intval($id);
        $this->desc = $desc;
        $this->title = $title;
        $this->versionId = intval($version);
        $this->testSuites = array();
        //$this->simpleName = $this->stringToSimpleName($this->title);
        $this->similar = array();
        $this->authorId = intval($author);
        if (intval($ownerId) === -1) {// why 0??
            $this->ownerId = $this->authorId;
        } else {
            $this->ownerId = intval($ownerId);
        }
        $this->controls = array();
        $this->ext = array();
        $this->isUsed = false;
    }

    function addControls($role) {
        $this->controls = array();
        $this->userIsRelated = false;
        switch ($role) {
            case "tester":
                $ctrl = new SpecRelationCtrl();
                if ($ctrl->isUserRelatedToSpec($this->id, Synergy::getSessionProvider()->getUsername())) {
                    array_push($this->controls, new Action("Edit", "edit", "icon-pencil"));
                    array_push($this->controls, new Action("Delete", "delete", "icon-trash"));
                    array_push($this->controls, new Action("Clone", "clone", "icon-repeat"));
                    $this->userIsRelated = true;
                }
                break;
            case "privilegedTester":
            case "admin":
            case "manager":
            case "leader":
                array_push($this->controls, new Action("Edit", "edit", "icon-pencil"));
                array_push($this->controls, new Action("Delete", "delete", "icon-trash"));
                array_push($this->controls, new Action("Clone", "clone", "icon-repeat"));
                $this->userIsRelated = true;
                break;
            default:
                break;
        }
    }

    public function setIsUsed($isUsed) {
        $this->isUsed = ($isUsed === 1) ? true : false;
        if ($this->isUsed) {
            // disable actions
            for ($i = 0, $max = count($this->controls); $i < $max; $i++) {
                switch ($this->controls[$i]->onClick) {
                    case "delete":
                        $this->controls[$i]->isEnabled = false;
                        break;
                    default:
                        break;
                }
            }
            $this->disableSuiteEditActions();
        }
    }

    private function disableSuiteEditActions() {
        for ($i = 0, $max = count($this->testSuites); $i < $max; $i++) {
            $this->testSuites[$i]->disableEditActions();
        }
    }

    public function stringToSimpleName($title) {
        $title = preg_replace("/\s/", "_", trim($title));
        $title = preg_replace("/\"/", "", ($title));
        $title = preg_replace("/'/", "", ($title));
        $title = strtolower($title);
        return $title;
    }

    public static function canEdit($specificationId = -1) {
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

    public static function canDelete($specificationId = -1) {
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

    public static function canCreate() {
        $role = Synergy::getSessionProvider()->getUserRole();
        switch ($role) {
            case "tester" :
            case "privilegedTester":
            case "manager" :
            case "admin" :
                return true;
            default :
                return false;
        }
    }

    public function setLastUpdated($lastUpdated) {
        $this->lastUpdated = "";
        if (isset($lastUpdated) && !is_null($lastUpdated)) {
            date_default_timezone_set('UTC');
            $str = strtotime($lastUpdated);
            $this->lastUpdated = gmdate("d M Y H:i:s", $str) . " UTC";
        }
    }

    public function toString() {

        $s = $this->title . Util::$NEW_LINE;
        $s .= "Description: " . $this->desc . Util::$NEW_LINE;
        $s .= "Author: " . $this->author . Util::$NEW_LINE;
        $s .= "Owner: " . $this->owner . Util::$NEW_LINE;
        $s .= "Version: " . $this->version . Util::$NEW_LINE;
        $s.= Util::$NEW_LINE;
        for ($i = 0, $max = count($this->testSuites); $i < $max; $i++) {
            $s .= $this->testSuites[$i]->toString() . Util::$NEW_LINE;
        }

        return $s;
    }

    public function getRemovalEmailSubject() {
        return "Request to remove specification from Synergy";
    }

    public function removalRequestBody($users) {
        return "Specification " . $this->title . " (see [1]) has been requested to be removed by following users: " . $users . "\r\n [1] " . SYNERGY_URL . "client/app/#/specification/" . $this->id;
    }

    public function removalRequestBodyHTML($users) {
        return "Specification <strong>" . $this->title . "</strong> (see [1]) has been requested to be removed by following users: " . $users . "<br/><br/> [1] <a href='" . SYNERGY_URL . "synergy/client/app/#/specification/" . $this->id . "'>" . SYNERGY_URL . "client/app/#/specification/" . $this->id . "</a>";
    }

    public function ownershipRequestBodyHTML($msg, $displayName, $username) {
        return "User <strong>" . $displayName . "</strong> (" . $username . ") has requested to be owner of specification <code>" . $this->title . "</code> (see [1]). To change owner, please go to edit specification page and change Owner field.<br/><br/> Request message:<br/><i>" . $msg . "</i><br/><br/>[1] <a href='" . SYNERGY_URL . "client/app/#/specification/" . $this->id . "'>" . SYNERGY_URL . "client/app/#/specification/" . $this->id . "</a>";
    }

    public function ownershipRequestBody($msg, $displayName, $username) {
        return "User " . $displayName . " (" . $username . ") has requested to be owner of specification " . $this->title . " (see [1]). To do so, please go to edit specification page and change Owner field.\r\n \r\n Request message:\r\n " . $msg . " \r\n \r\n [1] " . SYNERGY_URL . "client/app/#/specification/" . $this->id;
    }

    public function getOwnershipRequestSubject() {
        return "Request to change specification ownership in Synergy";
    }

    public function getSingleProject() {
        if (count($this->ext["projects"]) > 0) {
            return $this->ext["projects"][0]->name;
        }
        return null;
    }

    public function setProjects($ids, $names) {
        if ($ids !== NULL && strlen($ids) > 0) {
            $this->ext["projects"] = array();
            $idsA = explode(";", $ids);
            $namesA = explode(";", $names);

            foreach ($idsA as $index => $value) {
                $this->ext["projects"][] = new ProjectListItem($namesA[$index], intval($value, 10));
            }
        }
    }

}

?>