<?php

namespace Synergy\Controller;

use Synergy\App\Synergy;
use Synergy\DB\SpecificationDAO;
use Synergy\Extensions\Tribe\TribeSpecificationExtension;
use Synergy\Interfaces\Observer;
use Synergy\Model\Specification;
use Synergy\Model\SpecificationAttachment;
use Synergy\Model\Exception\SpecificationDuplicateException;
use Synergy\Model\SpecificationsSimpleNameList;
use Synergy\Model\Suite;
use Synergy\Extensions\Specification\RemovalRequestExtension;
use Synergy\Misc\Util;

/**
 * Description of SpecificationCtrl
 *
 * @author vriha
 */
class SpecificationCtrl extends ExtensionCtrl implements Observer {

    /**
     *
     * @var SuiteCtrl 
     */
    private $suiteCtrl;
    private $specDao;
    private $versionCtrl;
    private $userCtrl;
    private $runCtrl;
    private $tribeCtrl;
    private $attachmentCtrl;
    private $lockCtrl;

    /**
     *
     * @var CaseCtrl 
     */
    private $caseCtrl;

    function __construct() {
        parent::__construct('specification');
        $this->specDao = new SpecificationDAO();
    }

    private function getCaseCtrl() {
        if (is_null($this->caseCtrl)) {
            $this->caseCtrl = new CaseCtrl();
        }
        return $this->caseCtrl;
    }

    private function getTribeCtrl() {
        if (is_null($this->tribeCtrl)) {
            $this->tribeCtrl = new TribeCtrl();
        }
        return $this->tribeCtrl;
    }

    /**
     * 
     * @return SpecificationLockCtrl
     */
    public function getLockCtrl() {
        if (is_null($this->lockCtrl)) {
            $this->lockCtrl = new SpecificationLockCtrl();
        }
        return $this->lockCtrl;
    }

    /**
     * 
     * @return AttachmentCtrl
     */
    private function getAttachmentCtrl() {
        if (is_null($this->attachmentCtrl)) {
            $this->attachmentCtrl = new AttachmentCtrl();
        }
        return $this->attachmentCtrl;
    }

    private function getSuiteCtrl() {
        if (is_null($this->suiteCtrl)) {
            $this->suiteCtrl = new SuiteCtrl();
        }
        return $this->suiteCtrl;
    }

    private function getVersionCtrl() {
        if (is_null($this->versionCtrl)) {
            $this->versionCtrl = new VersionCtrl();
        }
        return $this->versionCtrl;
    }

    private function getUserCtrl() {
        if (is_null($this->userCtrl)) {
            $this->userCtrl = new UserCtrl();
        }
        return $this->userCtrl;
    }

    private function getRunCtrl() {
        if (is_null($this->runCtrl)) {
            $this->runCtrl = new RunCtrl();
        }
        return $this->runCtrl;
    }

    /**
     * Returns all specifications for version given by ID, if versionID is -1, returns all specifications
     * @param int $versionID version ID
     * @return array
     */
    public function getSpecifications($versionID, $userId = -1) {
        if ($versionID < 0) {
            return parent::get($this->specDao->getAllSpecifications($userId));
        }
        return parent::get($this->specDao->getSpecifications($versionID, $userId));
    }

    public function getFavoriteSpecifications($username) {
        return parent::get($this->specDao->getFavoriteSpecifications($username));
    }

    /**
     * Returns complete specification with test suites and attachments (but without cases)
     * @param type $id
     * @param String $label not required, if given, only cases with this label will be counted
     * @return \Specification|null
     */
    public function getSpecification($id, $label = '', $userId = -1) {
        if ($userId === -1) {
            $userId = Synergy::getSessionProvider()->getUserId();
        }

        $spec = $this->specDao->getSpecification($id, $label, $userId);
        if (!is_null($spec)) {
            $u = $this->getUserCtrl()->getUserById($spec->ownerId);
            $spec->owner = $u->username;
            $spec->ownerName = $u->firstName . " " . $u->lastName;
            $spec->attachments = $this->specDao->getAttachments($id);
            $spec->testSuites = $this->specDao->getTestSuites($id);
            $spec->estimation = $this->specDao->getEstimatedTime($id, $label);
            $spec->similar = $this->getSimilarSpecs($spec->simpleName, $id);
        }
        return parent::get($spec);
    }

    /**
     * Returns complete specification with test suites and attachments (without cases)
     * @param type $simpleName
     * @param int $versionID version ID, if < 0, latest version for given specification is used
     * @return \Specification|null
     */
    private function getSpecificationFromAlias($simpleName, $versionID) {
        if ($versionID < 0) {
            $spec = $this->specDao->getSpecificationAliasLatest($simpleName);
        } else {
            $spec = $this->specDao->getSpecificationAlias($simpleName, $versionID);
        }
        $spec->isFavorite = $this->specDao->isSpecificationFavorite($spec->id, Synergy::getSessionProvider()->getUserId());
        if (!is_null($spec)) {
            $u = $this->getUserCtrl()->getUserById($spec->ownerId);
            $spec->owner = $u->username;
            $spec->ownerName = $u->firstName . " " . $u->lastName;
            $spec->attachments = $this->specDao->getAttachments($spec->id);
            $spec->testSuites = $this->specDao->getTestSuites($spec->id);
            $spec->estimation = $this->specDao->getEstimatedTime($spec->id, "");
            $spec->similar = $this->getSimilarSpecs($spec->simpleName, $spec->id);
        }
        return parent::get($spec);
    }

    /**
     * Returns specification with all suites and cases
     * @param type $id
     * @return Specification|null
     */
    public function getSpecificationFull($id) {
        $specification = $this->getSpecification(intval($id), '', -1);
        $bugTrackingSystem = "other";
        if (count($specification->ext["projects"]) > 0) {
            $projectCtrl = new ProjectCtrl();
            $project = $projectCtrl->getProjectDetailed($specification->ext["projects"][0]->id);
            $bugTrackingSystem = $project->bugTrackingSystem;
        }

        if (is_null($specification)) {
            return null;
        }

        foreach ($specification->testSuites as $ts) {
            $ts->testCases = $this->getCaseCtrl()->getTestCasesDetailed($ts->id, '', $bugTrackingSystem);
        }
        return $specification; // no need to call extensions as it is done in $this->getSpecification()
    }

    /**
     * Returns all attachments for given specification
     * @param int $id specification ID
     * @return SpecificationAttachment[]
     */
    public function getAttachments($id) {
        return $this->specDao->getAttachments($id);
    }

    /**
     * Returns sum of duration of all cases for given specification
     * @param type $id
     * @param String $label not required, if given, only cases with this label will be counted
     * @return int
     */
    public function getEstimatedTime($id, $label = '') {
        return $this->specDao->getEstimatedTime($id, $label);
    }

    /**
     * Updates specification
     * @param Specification $specification
     * @return boolean
     */
    public function updateSpecification($specification, $keepSimpleName = true) {

        $versionID = $this->getVersionID($specification->id);

        if ($this->simpleNameAndVersionUsed($specification->id, $specification->simpleName, $versionID)) {
            throw new SpecificationDuplicateException('attempt to create a duplicate specification', 'Specification with given simple name already exists for given version', '');
        }

        $originalSimpleName = $specification->simpleName;
        $newSimpleName = $specification->stringToSimpleName($specification->title);

        if ($originalSimpleName !== $newSimpleName && $keepSimpleName) {
            $this->specDao->updateSimpleNames($originalSimpleName, $newSimpleName);
        }

        $result = $this->specDao->updateSpecification($specification->id, $specification->title, $specification->desc, $specification->ownerId, $newSimpleName);
        if ($result) {
            parent::edit($specification);
        }
        return $result;
    }

    public function getOwnerId($specificationId) {
        return $this->specDao->getOwnerId($specificationId);
    }

    /**
     * If user who attempts to remove specification is also owner (or admin/manager), removes specification. 
     * Otherwise send notification to owner and create removal request
     * @param type $id
     * @return boolean true if deleted, false if not
     */
    public function deleteSpecification($id) {
        // TODO check if user is owner, if not, send email to owner
        if ($this->specDao->getOwnerId($id) === Synergy::getSessionProvider()->getUserId() || Util::isAuthorized("admin") || Util::isAuthorized("manager")) {
            parent::delete($id);
            $obsoletes = $this->getSuitesIDs($id);
            $this->deleteAttachments($id);
            if (count($obsoletes) > 0) {
                $this->getSuiteCtrl()->deleteSuitesForSpecification($id, $obsoletes);
            }
            $this->getUserCtrl()->deleteFavoriteSpecification($id);
            //  $this->getRunCtrl()->deleteAssignmentsForSpecification($id);
            $this->getCaseCtrl()->removeUnusedCases();
            return $this->specDao->deleteSpecification($id);
        } else {
            RemovalRequestExtension::createRemovalRequest($id, Synergy::getSessionProvider()->getUserId());
            Mediator::emit("requestedRemoval", $id);
            return false;
        }
    }

    /**
     * Returns array of IDs of suites that belongs to given specification
     * @param type $spec_id
     * @return int[]
     */
    public function getSuitesIDs($spec_id) {
        return $this->specDao->getSuitesIDs($spec_id);
    }

    /**
     * Removes all attachments for given specification
     * @param int $id specification ID
     */
    public function deleteAttachments($id) {
        $attachments = $this->getAttachments($id);
        foreach ($attachments as $att) {
            $this->getAttachmentCtrl()->deleteSpecificationAttachment($att->id);
        }
    }

    /**
     * 
     * @param type $username
     * @return Specification[]
     */
    public function getSpecificationsByAuthor($username) {
        return parent::get($this->specDao->getSpecificationsByAuthor($username));
    }

    /**
     * 
     * @param type $username
     * @return Specification[]
     */
    public function getSpecificationsByOwner($username) {
        return parent::get($this->specDao->getSpecificationsByOwner($username));
    }

    /**
     * Returns specifications where given user is owner restricted to given version
     * @return Specification[]
     */
    public function getSpecificationsByOwnerAndVersion($username, $versionId, $projectId) {
        return parent::get($this->specDao->getSpecificationsByOwnerAndVersion($username, $versionId, $projectId));
    }

    /**
     * Returns $limit latest specfications. Age of specification is derived from ID (auto increment in DB)
     * @param type $limit
     * @return array
     */
    public function getLatestSpecifications($limit) {
        return parent::get($this->specDao->getLatestSpecifications($limit));
    }

    /**
     * Quazi removes authorship. Because DB constraints have to correct, current user is set as auhtor
     * @param type $userId
     */
    public function deleteAuthorship($userId) {
        return $this->specDao->deleteAuthorship($userId);
    }

    /**
     * Quazi removes authorship. Because DB constraints have to correct, current user is set as auhtor
     * @param type $userId
     */
    public function deleteOwnership($userId) {
        return $this->specDao->deleteOwnership($userId);
    }

    /**
     * Returns specifications with title matching given term
     * @param String $title
     * @param int $limit maximum number of results to be returned
     * @return Specification[]
     */
    public function findMatchingSpecifications($title, $limit = 15) {
        return parent::get($this->specDao->findMatchingSpecifications($title, $limit));
    }

    /**
     * Clones specification
     * @param type $specId ID of specification to be clonned
     * @param type $version target version
     * @param type $newname name of the created clonned specification
     * @return int new specification ID
     */
    public function cloneSpecification($specId, $version, $newname) {
        $v = $this->getVersionCtrl()->getVersionByName($version);
        $versionId = $v->getId();
        if (is_null($versionId)) {
            return -1;
        }

        $spec = $this->getSpecification(intval($specId), '', -1);
        if ($spec->title !== $newname) {
            $newSimpleName = $spec->stringToSimpleName($newname);
            if ($this->isUniqueSimpleName($newSimpleName)) {
                $oldSimpleName = $spec->simpleName;
                $this->specDao->updateSimpleNames($oldSimpleName, $newSimpleName);
            }
        }

        $spec->title = $newname;
        $spec->simpleName = $spec->stringToSimpleName($newname);
        $spec->version = $version;

        $newSpecId = $this->createSpecification($spec, true);
        //insert new
        if ($newSpecId > 0) {
            $this->specDao->cloneSpecificationAttachment($spec, $newSpecId);

            for ($i = 0, $max = count($spec->testSuites); $i < $max; $i++) {
                /* @var $suite Suite              */
                $suite = $spec->testSuites[$i];
                $newSuiteId = $this->getSuiteCtrl()->cloneSpecificationSuite($newSpecId, $suite);
                $cases = $this->getSuiteCtrl()->getTestCasesIds($suite->id);
                $this->specDao->cloneSpecificationCases($cases, $newSuiteId);
            }
        }

        Mediator::emit("specificationCreated", $newSpecId);
        return $newSpecId;
    }

    /**
     * Creates new specification
     * @param Specification $specification
     * @return type
     */
    public function createSpecification($specification, $isCloning) {
        $v = $this->getVersionCtrl()->getVersionByName($specification->version);
        if (is_null($v)) {
            if (defined('ANONYM')) {
                $this->getVersionCtrl()->createVersion($specification->version);
                $v = $this->getVersionCtrl()->getVersionByName($specification->version);
            } else {
                return -1;
            }
        }
        $versionId = $v->getId();
        $userId = -1;
        $matchedUser = $this->getUserCtrl()->getUser($specification->author);
        if (is_null($matchedUser)) {
            $userId = $this->getUserCtrl()->getUserIDbyUsername(ANONYM);
        } else {
            $userId = $matchedUser->id;
        }


        if ($userId < 0)
            return -1;
//        error_log(Util::purifyHTML($specfication->desc));

        $specification->simpleName = $specification->stringToSimpleName($specification->title);

        if ($this->simpleNameAndVersionUsed(-1, $specification->simpleName, $versionId)) {
            throw new SpecificationDuplicateException('attempt to create a duplicate specification', 'Specification with given simple name already exists for given version', '');
        }
        $specification->authorId = $userId;
        if ($isCloning) {
            $newId = $this->specDao->createSpecification($specification->title, $specification->desc, $versionId, $userId, Synergy::getSessionProvider()->getUserId(), $specification->simpleName);
            $specification->ownerId = Synergy::getSessionProvider()->getUserId();
        } else {
            $newId = $this->specDao->createSpecification($specification->title, $specification->desc, $versionId, $userId, $userId, $specification->simpleName);
            $specification->ownerId = $userId;
        }

        $specification->versionId = $versionId;
        $specification->id = $newId;
        parent::create($specification, $newId);
        return $newId;
    }

    public static function on($name, $data) {
        
    }

    /**
     * Returns number of cases in specification with given label
     * @param type $specificationId
     * @param type $labelId
     */
    public function getCasesCount($specificationId, $labelId) {
        return $this->specDao->getCasesCount($specificationId, $labelId);
    }

    /**
     * Refreshes specification last update date
     * @param type $specificationId
     */
    public function refreshSpecification($specificationId) {
        date_default_timezone_set('UTC');
        $localTime = date('Y-m-d H:i:s');
        $this->specDao->setLastUpdatedDate($localTime, $specificationId);
    }

    /**
     * Changes authorship of specification
     * @param type $userId new author ID
     * @param type $specificationId specification to be updated
     */
    public function changeAuthor($userId, $specificationId) {
        $this->specDao->setAuthorship($userId, $specificationId);
    }

    /**
     * Returns full specification including test cases based on provided simple name
     * @param String $simpleName
     * @param int $versionID if < 0, latest version is used
     * @return Specification
     */
    public function getSpecificationFullByAlias($simpleName, $versionID) {
        $specification = $this->getSpecificationFromAlias($simpleName, $versionID);
        if (is_null($specification)) {
            return null;
        }

        $bugTrackingSystem = "other";
        if (count($specification->ext["projects"]) > 0) {
            $projectCtrl = new ProjectCtrl();
            $project = $projectCtrl->getProjectDetailed($specification->ext["projects"][0]->id);
            $bugTrackingSystem = $project->bugTrackingSystem;
        }

        foreach ($specification->testSuites as $ts) {
            $ts->testCases = $this->getCaseCtrl()->getTestCasesDetailed($ts->id, '', $bugTrackingSystem);
        }
        return $specification; // no need to call extensions as it is done in $this->getSpecification()
    }

    /**
     * Returns version ID of given specification
     * @param type $specificationID
     * @return type
     */
    public function getVersionID($specificationID) {
        return $this->specDao->getSpecificationVersionID($specificationID);
    }

    /**
     * Checks if given simple name is already used for any specification in given version
     * @param type $specificationID ID of specification to be excluded from search
     * @param type $simpleName
     * @param type $versionID
     * @return boolean true if simple name is already used in given version
     */
    public function simpleNameAndVersionUsed($specificationID, $simpleName, $versionID) {
        if ($this->specDao->findMatchingSpecification($specificationID, $simpleName, $versionID) === -1) {
            return false;
        }
        return true;
    }

    /**
     * Returns specifications with the same simple name across versions
     * @param type $simpleName
     * @param type $excludedId ID of specification to be excluded from search
     * @return Specification[]
     */
    public function getSimilarSpecs($simpleName, $excludedId) {
        return $this->specDao->findSimilar($simpleName, $excludedId);
    }

    public function getSpecificationsByVersion() {
        SpecificationsSimpleNameList::$versions = $this->getVersionCtrl()->getVersions();
        return $this->specDao->getAllSpecificationsGroupedBySimpleName();
    }

    public function createSimpleName($id, $title) {
        $this->specDao->setSimpleName($id, $title);
    }

    public function isUniqueSimpleName($newSimpleName) {
        return $this->specDao->isUniqueSimpleName($newSimpleName);
    }

    public function isSpecificationUsed($specificationId) {
        if ($this->getLockCtrl()->isSpecificationLocked($specificationId)) {
            return 1;
        }
        return $this->specDao->isSpecificationUsed($specificationId);
    }

    public function getSpecificationOverview($specificationId) {
        return $this->specDao->getSpecificationOverview($specificationId);
    }

    public function getSpecificationsForProject($projectId) {
        return $this->specDao->getSpecificationsForProject($projectId);
    }

}

?>