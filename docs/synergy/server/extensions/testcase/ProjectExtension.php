<?php

namespace Synergy\Extensions\Testcase;

use Synergy\Controller\ProjectCtrl;
use Synergy\Interfaces\ExtensionInterface;
use Synergy\Model\TestCase;

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
        
    }

    public function delete($deletedId) {
        
    }

    public function edit($object) {
        
    }

    /**
     * 
     * @param TestCase $object
     */
    public function get($object) {
        if (is_null($object)) {
            return $object;
        }
        $projects = $this->getProjectCtrl()->getProjectsForSpecification($object->specificationId);
        if (!array_key_exists("projects", $object->ext)) {
            $object->ext["projects"] = $projects;
        }

        return $object;
    }

//put your code here
}
