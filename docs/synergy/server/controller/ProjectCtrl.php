<?php

namespace Synergy\Controller;

use Synergy\DB\ProjectDAO;

/**
 * Description of ProjectCtrl
 *
 */
class ProjectCtrl {

    private $projectDao;

    function __construct() {
        $this->projectDao = new ProjectDAO();
    }

    function getProjects() {
        return $this->projectDao->getProjects();
    }

    public function getProjectsForSpecification($specificationId) {
        return $this->projectDao->getProjectsForSpecification($specificationId);
    }

    public function removeProjectsForSpecification($specificationId) {
        return $this->projectDao->removeProjectsForSpecification($specificationId);
    }

    public function updateProject($projectId, $newname, $reportLink, $viewLink, $multiViewLink, $bugTrackingSystem) {
        if ($this->projectDao->isNameUsed($newname, $projectId)) {
            return false;
        }
        return $this->projectDao->updateProject($projectId, $newname, $reportLink, $viewLink, $multiViewLink, strtolower($bugTrackingSystem));
    }

    public function createProject($projectName) {
        if ($this->projectDao->isNameUsed($projectName, -1)) {
            return false;
        }
        return $this->projectDao->createProject($projectName);
    }

    public function deleteProject($projectId) {
        return $this->projectDao->deleteProject($projectId);
    }

    public function deleteProjectForSpecification($specificationId) {
        return $this->projectDao->deleteProjectForSpecification($specificationId);
    }

    public function addProjectToSpecification($specificationId, $projectId) {
        return $this->projectDao->addProjectToSpecification(intval($specificationId, 10), intval($projectId, 10));
    }

    /**
     * 
     * @param array $latestSpecs array of specifications IDs
     * @return type
     */
    public function getProjectsForSpecifications($latestSpecs) {
      return $this->projectDao->getProjectsForSpecifications($latestSpecs);
    }

    public function getProjectDetailed($projectId) {
        return $this->projectDao->getProjectDetailed($projectId);
    }

    public function getSpecificationsIds($projectId) {
        return $this->projectDao->getSpecificationsIds($projectId);
    }

}
