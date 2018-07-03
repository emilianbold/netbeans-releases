<?php

namespace Synergy\Model;

use Synergy\Model\Project\ProjectListItem;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * Description of SpecificationSimpleName
 *
 * @author vriha
 */
class SpecificationListItem {

    public $id;
    public $title;
    public $version;
    public $newerId;
    private $simpleName;
    public $projects;

    function __construct($id, $title, $version) {
        $this->id = intval($id);
        $this->title = $title;
        $this->version = $version;
        $this->newerId = -1;
        $this->projects = array();
    }

    public function setProjects($ids, $names) {
        if ($ids !== NULL && strlen($ids) > 0) {
            $idsA = explode(";", $ids);
            $namesA = explode(";", $names);

            foreach ($idsA as $index => $value) {
                $this->projects[] = new ProjectListItem($namesA[$index], intval($value, 10));
            }
        }
    }

    public function getSimpleName() {
        return $this->simpleName;
    }

    public function setSimpleName($simpleName) {
        $this->simpleName = $simpleName;
    }

}
