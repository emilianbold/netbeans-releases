<?php

namespace Synergy\Controller;

use Exception;
use Synergy\App\Synergy;
use Synergy\DB\CaseDAO;
use Synergy\DB\SpecificationDAO;
use Synergy\DB\SuiteDAO;
use Synergy\Interfaces\Observer;
use Synergy\Model\TestCase;
use Synergy\Model\TestCaseImage;
use Synergy\DB\DB_DAO;

/**
 * Description of CaseCtrl
 * 
 * @author lada
 */
class CaseCtrl extends ExtensionCtrl implements Observer {

    private $caseDao;
    private $specificationDao;
    private $suiteDao;
    private $suiteCtrl;

    function __construct() {
        parent::__construct('testcase');
        $this->caseDao = new CaseDAO();
        $this->specificationDao = new SpecificationDAO();
        $this->suiteDao = new SuiteDAO();
    }

    public function getSuiteCtrl() {
        if (is_null($this->suiteCtrl)) {
            $this->suiteCtrl = new SuiteCtrl();
        }
        return $this->suiteCtrl;
    }

    /**
     * for tests only
     * @return Bug|Array
     */
    public function getIssuesForCase($caseId) {
        return $this->caseDao->getIssues($caseId);
    }

    /**
     * for tests only
     * @return Bug|Array
     */
    public function getLabelsForCase($caseId) {
        return $this->caseDao->getLabels($caseId);
    }

    /**
     * for tests only
     * @return Bug|Array
     */
    public function getImagesForCase($caseId) {
        return $this->caseDao->getImages($caseId);
    }

    /**
     * Returns test case for the given ID in perspective of suite with given suite ID
     * @param int $id case ID
     * @param int $suiteId suite ID where the case belongs
     * @return TestCase
     */
    public function getCase($id, $suiteId) {
        if ($suiteId > 0) {
            $spec = $this->specificationDao->getSpecificationTitleIdBySuiteId($suiteId);
            $projectCtrl = new ProjectCtrl();

            $projects = $projectCtrl->getProjectsForSpecification($spec[1]);

            $bugTracking = "other";
            if (count($projects) > 0) {
                $bugTracking = $projects[0]->bugTrackingSystem;
            }

            $case = $this->caseDao->getCaseForSuite($id, $suiteId);
            if (is_null($case)) {
                return null;
            }
            $case->issues = $this->caseDao->getIssues($id);

            try {
                for ($i = 0, $max = count($case->issues); $i < $max; $i++) {
                    $issueCtrl = Synergy::getProvider("issue_" . $bugTracking);
                    $issue = $issueCtrl->getIssue($case->issues[$i]->bugId, true);
                    $issue->id = $case->issues[$i]->id;
                    if (!is_null($issue)) {
                        $case->issues[$i] = $issue;
                    }
                }
            } catch (Exception $e) {
                
            }

            $case->images = $this->caseDao->getImages($id);
            $case->suiteTitle = $this->suiteDao->getSuiteTitle($suiteId);
            $case->specificationTitle = $spec[0];
            $case->version = $spec[2];
            $case->specificationId = $spec[1];
            return parent::get($case);
        } else {
            $case = $this->caseDao->getCase($id);
            if (is_null($case)) {
                return null;
            }
            $case->issues = $this->caseDao->getIssues($id);
            $case->images = $this->caseDao->getImages($id);
            return parent::get($case);
        }
    }

    /**
     * Returns test cases that have given label. Results are paginated and page parameter is used to limit results. Page size is LABEL_PAGE
     * @param string $label
     * @param int $page page number, first page is 1
     * @return TestCase[]
     */
    public function getCasesByFilter($label, $page) {
        return parent::get($this->caseDao->getCasesByFilter($label, $page));
    }

    /**
     * Edit particular test case. If suite with $suiteId is the only one that has
     * this case, the case is updated. Otherwise cases is clonned, updated and reference
     * to the new case replaces reference to the old one in suite_has_case
     * @param TestCase $testCase test case
     * @param int $mode 1 if simply update case, 0 if safely create new case for this suite
     */
    public function updateCase($testCase, $mode) {
        if ($mode === 1) {
            // simply edit 
            if ($testCase->duration === $testCase->originalDuration) {
                $this->caseDao->edit($testCase->id, $testCase->title, $testCase->steps, $testCase->result, $testCase->order);
            } else {
                $this->caseDao->editWithDuration($testCase->id, $testCase->title, $testCase->steps, $testCase->result, $testCase->duration, $testCase->order);
            }

            parent::edit($testCase);
            return true;
        } else {
            if ($this->suiteDao->onlySuiteHasCase($testCase->id, $testCase->suiteId)) {
                return $this->updateCase($testCase, 1);
            } else {
                $newId = $this->createDuplicitCase($testCase);
                $p = $this->caseDao->cloneLabels($testCase->id, $newId);
                $p2 = $this->caseDao->cloneIssues($testCase->id, $newId);
                DB_DAO::executeQuery($p.$p2);
                
                $this->caseDao->cloneImages($testCase->id, $newId);
                $this->removeCaseFromSuite($testCase->suiteId, $testCase->id);
                return true;
            }
        }
    }

    public static function on($name, $data) {
        
    }

    /**
     * Adds issue to the given case
     * @param int $bugId bug ID
     * @param int $caseId case ID
     * @return boolean true on success
     */
    public function addIssue($bugId, $caseId) {
        return $this->caseDao->addIssue($bugId, $caseId);
    }

    /**
     * Removes issue to the given case
     * @param int $bugId bug ID
     * @param int $caseId case ID
     * @return boolean true on success
     */
    public function removeIssue($bugId, $caseId) {
        return $this->caseDao->removeIssue($bugId, $caseId);
    }

    /**
     * Adds label to the given case
     * @param string $label label
     * @param int $caseId case ID
     * @return boolean true on success
     */
    public function addLabel($label, $caseId) {
        $kid = $this->createGetKeyword($label);
        if (!$this->caseHasLabel($kid, $caseId)) {
            return $this->caseDao->addLabel($label, $caseId, $kid);
        }
        return true;
    }

    /**
     * Returns ID of given label. If the label does not exist, it is created
     * @param string $label label
     * @return int ID of label
     */
    public function createGetKeyword($label) {
        $id = $this->caseDao->getKeywordId($label);
        if ($id < 0) {
            $id = $this->caseDao->createKeyword($label);
        }
        return $id;
    }

    /**
     * Removes label to the given case
     * @param string $label label
     * @param int $caseId case ID
     * @return boolean true on success
     */
    public function removelabel($label, $caseId) {
        $kid = $this->createGetKeyword($label);
        return $this->caseDao->removelabel($label, $caseId, $kid);
    }

    /**
     * Creates new test case
     * @param TestCase $testCase test case
     * @return int new case ID
     */
    public function createCase($testCase) {
        $caseId = $this->caseDao->createCase($testCase->title, $testCase->steps, $testCase->result, $testCase->duration, $testCase->order);
        Mediator::emit("caseCreated", $caseId);
        $this->suiteDao->addCaseToSuite($caseId, $testCase->suiteId);
        parent::create($testCase, $caseId);
        return $caseId;
    }

    /**
     * Clones given test case
     * @param TestCase $testCase test case
     * @return int new case ID
     */
    public function createDuplicitCase($testCase) {
        $caseId = $this->caseDao->createDuplicitCase($testCase->title, $testCase->steps, $testCase->result, $testCase->duration, $testCase->order);
        $this->suiteDao->addCaseToSuite($caseId, $testCase->suiteId);
        parent::create($testCase, $caseId);
        return $caseId;
    }

    /**
     * Removes case from test suite
     * @param int $suiteId suite ID
     * @param int $caseId case ID
     * @return boolean true on success
     */
    public function removeCaseFromSuite($suiteId, $caseId) {
        $result = $this->caseDao->removeCaseFromSuite($suiteId, $caseId);
        if ($result) {
            $ctrl = $this->getSuiteCtrl();
            Mediator::emit("specificationUpdated", $ctrl->getSpecificationId($suiteId));
        }
        return $result;
    }

    /**
     * Returns cases by their title (LIKE title)
     * @param string $title title
     * @return TestCase[]
     */
    public function findMatchingCases($title) {
        return parent::get($this->caseDao->findMatchingCases($title));
    }

    /**
     * Returns case's duration in minutes
     * @param int $c_id case ID
     * @return int duration of given case
     */
    public function getCasesDuration($c_id) {
        return $this->caseDao->getCasesDuration($c_id);
    }

    /**
     * Updates case's duration
     * @param int $id case ID
     * @param int $duration duration in minutes
     * @return boolean true on success
     */
    public function updateDuration($id, $duration) {
        return $this->caseDao->updateDuration($id, intval($duration));
    }

    /**
     * Saves record about image to database
     * @param int $id case ID
     * @param string $name image file name
     * @param string $title image title
     * @return boolean true on success
     */
    public function saveImage($id, $name, $title) {
        return $this->caseDao->saveImage($id, $name, $title);
    }

    /**
     * Removes image and if the file is not used in other run, deletes file as well
     * @param int $id image ID
     * @return boolean true on success
     */
    public function deleteImage($id) {

        $path = $this->caseDao->getImageAttachment($id);
        if (strlen($path) < 1)
            return false;
        $this->caseDao->deleteImage($id);
        if ($this->caseDao->usedInCasesCount($path) === 0) {
            TestCaseImage::deleteFile($path);
        }
        return true;
    }

    /**
     * Returns all cases with detailed information for suite with given ID
     * @param int $id suite ID
     * @return TestCase[]
     */
    public function getTestCasesDetailed($id, $label = '', $bugTrackingSystem) {
        if (strlen($label) > 0) {
            $cases = $this->suiteDao->getTestCasesDetailedByLabel($id, $label);
        } else {
            $cases = $this->suiteDao->getTestCasesDetailed($id);
        }
        $issueCtrl = Synergy::getProvider("issue_" . $bugTrackingSystem);
        for ($index = 0, $max = count($cases); $index < $max; $index++) {
            $case = $cases[$index];
            $case->images = $this->caseDao->getImages($case->id);
            $case->issues = $this->caseDao->getIssues($case->id);
            try {
                for ($i = 0, $max2 = count($case->issues); $i < $max2; $i++) {
                    $issue = $issueCtrl->getIssue($case->issues[$i]->bugId, true);
                    if (!is_null($issue))
                        $case->issues[$i] = $issue;
                }
            } catch (Exception $e) {
                
            }

            $cases[$index] = $case;
        }
        return parent::get($cases);
    }

    /**
     * Goes through all cases that are not in any suite and removes them
     */
    public function removeUnusedCases() {
        $unusedIds = $this->caseDao->getUnusedCases();
        for ($i = 0, $max = count($unusedIds); $i < $max; $i++) {
            $this->removeCase($unusedIds[$i]);
        }
    }

    /**
     * Removes case (+ all references in issues, labels, images...)
     * @param type $caseId
     */
    public function removeCase($caseId) {
        // bug
        $this->caseDao->removeIssuesForCase($caseId);
        // case_has_keyword
        $this->caseDao->removeLabelsForCase($caseId);
        // case_image
        $allImages = $this->caseDao->getImages($caseId);

        for ($i = 0, $max = count($allImages); $i < $max; $i++) {
            $this->deleteImage($allImages[$i]->id);
        }
        // case
        $this->caseDao->removeCase($caseId);
    }

    /**
     * Returns true if given case is in specification that is used (exists assignment progress for given specification)
     * @param type $caseId
     * @return type
     */
    public function isCaseInUsedSpecification($caseId) {
        return $this->caseDao->isCaseInUsedSpecification($caseId);
    }

    public function caseHasLabel($labelId, $caseId) {
        return $this->caseDao->caseHasLabel($labelId, $caseId);
    }

}

?>