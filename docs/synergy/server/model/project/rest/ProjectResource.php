<?php

namespace Synergy\Model\Project\Rest;

/**
 * Description of ProjectResource
 *
 */
class ProjectResource {

    public $id;
    public $name;
    public $controls;
    public $reportLink;
    public $viewLink;
    public $multiViewLink;
    public $bugTrackingSystem;
    
    public static function createFromProject($project) {
        $i = new ProjectResource();
        $i->id = $project->id;
        $i->name = $project->name;
        $i->controls = $project->controls;
        $i->viewLink = $project->viewLink;
        $i->multiViewLink = $project->multiViewLink;
        $i->reportLink = $project->reportLink;
        $i->bugTrackingSystem = $project->bugTrackingSystem;
        return $i;
    }

    public static function createFromProjects($projects) {
        $list = array();
        for ($i = 0, $max = count($projects); $i < $max; $i++) {
            array_push($list, ProjectResource::createFromProject($projects[$i]));
        }
        return $list;
    }

}
