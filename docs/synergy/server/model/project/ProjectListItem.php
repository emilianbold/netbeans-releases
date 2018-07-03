<?php

namespace Synergy\Model\Project;

/**
 * Description of ProjectListItem
 *
 * @author vriha
 */
class ProjectListItem {

    public $name;
    public $id;

    function __construct($name, $id) {
        $this->name = $name;
        $this->id = $id;
    }

}
