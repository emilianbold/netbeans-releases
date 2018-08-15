<?php

namespace Synergy\Extensions\Specification;

use Synergy\Controller\ProjectCtrl;
use Synergy\Interfaces\ExtensionInterface;
use Synergy\Model\Specification;

/**
 * Description of ProjectExtension
 *
 */
class ProjectExtension implements ExtensionInterface {

    private $projectCtrl;

    /**
     * 
     * @return ProjectCtrl
     */
    public function getProjectCtrl() {
        if (is_null($this->projectCtrl)) {
            $this->projectCtrl = new ProjectCtrl();
        }
        return $this->projectCtrl;
    }

    public function create($object, $newId) {
        if (is_array($object->ext)) {
            $this->getProjectCtrl()->addProjectToSpecification($newId, $object->ext["projects"][0]->id);
        } else {
            $this->getProjectCtrl()->addProjectToSpecification($newId, $object->ext->projects[0]->id);
        }
    }

    public function delete($deletedId) {
        $this->getProjectCtrl()->removeProjectsForSpecification($deletedId);
    }

    public function edit($object) {

        $this->getProjectCtrl()->deleteProjectForSpecification($object->id);
        if(is_array($object->ext)){
            $project = $object->ext["projects"][0];
        }else{
            $project = $object->ext->projects[0];
        }
        
        
        
        $projectId = $project->id;
        if ($projectId === -2) {
            $projectId = $this->getProjectCtrl()->createProject($project->name);
        }
        $this->getProjectCtrl()->addProjectToSpecification($object->id, $projectId);
    }

    /**
     * 
     * @param Specification $object
     */
    public function get($object) {
        if (is_null($object)) {
            return $object;
        }
        $projects = $this->getProjectCtrl()->getProjectsForSpecification($object->id);
        if (!array_key_exists("projects", $object->ext)) {
            $object->ext["projects"] = $projects;
        }

        return $object;
    }

//put your code here
}
