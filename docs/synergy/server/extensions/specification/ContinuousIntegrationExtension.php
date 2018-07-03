<?php

namespace Synergy\Extensions\Specification;

use Synergy\DB\CiDAO;
use Synergy\Interfaces\ExtensionInterface;
use Synergy\Model\Job;
use Synergy\Model\Specification;

/**
 * Description of ContinuousIntegrationExtension
 *
 * @author vriha
 */
class ContinuousIntegrationExtension implements ExtensionInterface {

    private $ciDao;

    function __construct() {
        $this->ciDao = new CiDAO();
    }

    public function create($object, $newId) {
    }

    public function delete($specificationId) {
        $this->ciDao->deleteJobsForSpecification($specificationId);
    }

    public function edit($object) {
        
    }

    public function jobAlreadyExists($jobUrl, $specificationId){
        $j = new Job(-1, $specificationId, $jobUrl);
        return $this->ciDao->jobUrlExist($j->specificationId, $j->jobUrl);
    }
    
    /**
     * 
     * @param Specification $object
     */
    public function get($object) {
        if (!is_null($object)) {
            $object->ext['continuous_integration'] = $this->getJobsUrl($object->id);
            return $object;
        }
        return null;
    }
    /**
     * Returns all jobs for given spec
     * @param type $specificationId
     * @return Job
     */
    private function getJobsUrl($specificationId) {
        return $this->ciDao->getJobsUrl($specificationId);
    }

    public function createNewJob($jobUrl, $specificationId) {
        $j = new Job(-1, $specificationId, $jobUrl);
        $this->ciDao->createJobUrl($j->specificationId, $j->jobUrl);
    }

    public function deleteJob($jobId) {
        $this->ciDao->deleteJob($jobId);
    }
}
?>