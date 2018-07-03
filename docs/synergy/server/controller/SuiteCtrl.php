<?php
namespace Synergy\Controller;

use Synergy\DB\CaseDAO;
use Synergy\DB\SuiteDAO;
use Synergy\Misc\Util;
use Synergy\Model\Suite;

/**
 * Description of SuiteCtrl
 *
 * @author lada
 */
class SuiteCtrl extends ExtensionCtrl {

    private $suiteDao;
    private $caseDao;
    private $caseCtrl;

    function __construct() {
        parent::__construct('suite');
        $this->suiteDao = new SuiteDAO();
        $this->caseDao = new CaseDAO();
    }

    public function getCaseCtrl() {
        if(is_null($this->caseCtrl)){
            $this->caseCtrl = new CaseCtrl();
        }
        return $this->caseCtrl;
    }

        
    /**
     * Removes suites for given specification 
     * @param int $spec_id specification ID
     * @param int[] $obsolete_references array of IDs of suites that belongs to given specification and which will be removed)
     */
    public function deleteSuitesForSpecification($spec_id, $obsolete_references) {
        $obsolete = Util::arrayToSQLOR($obsolete_references, "suite_id");
        $this->suiteDao->deleteSuiteRefFromSpec($obsolete);
        $this->suiteDao->deleteSuitesForSpecification($spec_id);
    }

    /**
     * Returns suite by ID
     * @param int $id suite ID
     * @return Suite
     */
    public function getSuite($id) {
        $suite = $this->suiteDao->getSuite($id);
        if (!is_null($suite))
            $suite->setCases($this->suiteDao->getTestCases($id));
        return parent::get($suite);
    }

    /**
     * Clones given suite for given specification
     * @param int $targetSpecificationId target specification ID
     * @param Suite $suite suite to be clonned
     * @return int new suite ID
     */
    public function cloneSpecificationSuite($targetSpecificationId, $suite) {
        $newId = $this->suiteDao->cloneSpecificationSuite($targetSpecificationId, $suite);
        $suite->specificationId = $targetSpecificationId;
        parent::create($suite, $newId);
        return $newId;
    }
    
    /**
     * Returns array of cases' ID for given suite
     * @param int $id suite ID
     * @return int[]
     */
    public function getTestCasesIds($id) {
        return $this->suiteDao->getTestCasesIds($id);
    }

    /**
     * Updates suite
     * @param Suite $suite
     * @return boolean true if success
     */
    public function updateSuite($suite) {
        $result = $this->suiteDao->updateSuite($suite->id, $suite->title, $suite->desc, $suite->product, $suite->component, $suite->order);
        if ($result) {
            Mediator::emit("specificationUpdated", $this->getSpecificationId($suite->id));
            parent::edit($suite);
        }
        return $result;
    }

    public function getSpecificationId($suiteId){
        return $this->suiteDao->getSpeficiationId($suiteId);
    }
    
    /**
     * Removes suite with given ID
     * @param int $id
     * @return boolean true if success
     */
    public function deleteSuite($id) {
        parent::delete($id);
        $specId = $this->getSpecificationId($id);
        $this->suiteDao->deleteReferencesToCases($id);
        $result = $this->suiteDao->deleteSuite($id);
        if($result){
            Mediator::emit("specificationUpdated", $specId);
        }
        $this->getCaseCtrl()->removeUnusedCases();
        return $result;
    }

    /**
     * Creates a new suite
     * @param Suite $suite suite
     * @return int ID of new suite
     */
    public function createSuite($suite) {
        $newId = $this->suiteDao->createSuite($suite->specificationId, $suite->title, $suite->desc, $suite->product, $suite->component, $suite->order);
        $suite->id = $newId;
        Mediator::emit("specificationUpdated", $suite->specificationId);
        parent::create($suite, $newId);
        return $newId;
    }
    
    /**
     * Adds case to the suite
     * @param int $caseId case ID
     * @param int $suiteId suite ID
     * @return boolean true if success
     */
    public function addCaseToSuite($caseId, $suiteId) {
        $result = $this->suiteDao->addCaseToSuite($caseId, $suiteId);
        if($result){
            Mediator::emit("specificationUpdated", $this->getSpecificationId($suiteId));
        }
        return $result;
    }

    /**
     * Returns true if given case is already part of the suite
     * @param int $caseId case ID
     * @param int $suiteId suite ID
     * @return boolean true if case is part of suite
     */
    public function suiteAlreadyHasCase($caseId, $suiteId) {
        return $this->suiteDao->suiteAlreadyHasCase($caseId, $suiteId);
    }
    
    /**
     * Returns true if only given suite has this case, in case multiple suites have the same case, returns false
     * @param int $id case ID
     * @param int $suiteId suite ID
     * @return boolean true if only this suite has the case
     */
    public function onlySuiteHasCase($id, $suiteId) {
        return $this->suiteDao->onlySuiteHasCase($id, $suiteId);
    }

    /**
     * Returns list of suites with title matching given term
     * @param String $term
     * @param int $limit maximum number of results
     * @return Suite[]
     */
    public function findMatchingSuites($term, $limit = 15) {
        return parent::get($this->suiteDao->findMatchingSuites($term, $limit));
    }
    
    /**
     * Returns array of specifications IDs where given case is used
     * @param type $caseId
     * @return array
     */
    public function getAllSpecificationsForCase($caseId){
        return $this->caseDao->getSpecificationsIdForCase($caseId);
    }

}

?>
