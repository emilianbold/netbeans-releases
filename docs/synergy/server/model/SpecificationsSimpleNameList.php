<?php

namespace Synergy\Model;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * Description of SpecificationsVersionList
 *
 * @author vriha
 */
class SpecificationsSimpleNameList {

    public $simpleName;
    public $title;
    public static $versions;
    public $specifications;
    public $print;
    private $versionOfTitle;
    private $latestId;
    public $projects;

    function __construct($simpleName) {
        $this->simpleName = $simpleName;
        $this->print = false;
        $this->specifications = array();
        $this->versionOfTitle = -1;
        $this->projects = array();
    }

    public function getVersionOfTitle() {
        return $this->versionOfTitle;
    }

    public function getLatestId() {
        return $this->latestId;
    }

    public function populate($ids, $titles, $versions, $owners, $ownersRoles) {
        $ids_a = explode(";", $ids);
        $coundIds_a = count($ids_a);
        $titles_a = explode(";", $titles);
        $versions_a = explode(";", $versions);
        $owners_a = explode(";", $owners);
        $roles_a = explode(";", $ownersRoles);
        $found = false;
        for ($j = 0, $max = count(SpecificationsSimpleNameList::$versions); $j < $max; $j++) {
            $found = false;
            for ($i = 0; $i < $coundIds_a && !$found; $i++) {
                $this->setTitle($titles_a[$i], Version::toFloat($versions_a[$i]), intval($ids_a[$i], 10));
                if ($versions_a[$i] === SpecificationsSimpleNameList::$versions[$j]->name) {
                    $s = new Specification($ids_a[$i], '', $titles_a[$i], -1, '', '');
                    $s->owner = $owners_a[$i];
                    $s->ownerRole = $roles_a[$i];
                    $s->version = $versions_a[$i];
                    array_push($this->specifications, $s);
                    $found = true;
                }
            }
            if (!$found) {
                $s = new Specification(-1, '', '', -1, '', '');
                $s->version = SpecificationsSimpleNameList::$versions[$j]->name;
                array_push($this->specifications, $s);
            }
        }
    }

    public function setTitle($title, $version, $id) {
        if ($this->versionOfTitle <= $version || !isset($this->title)) {
            $this->versionOfTitle = $version;
            $this->title = $title;
            $this->latestId = $id;
        }
    }

}
