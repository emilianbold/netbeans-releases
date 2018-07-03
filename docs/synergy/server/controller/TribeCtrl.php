<?php

namespace Synergy\Controller;

use Synergy\App\Synergy;
use Synergy\DB\TribeDAO;
use Synergy\Misc\Util;
use Synergy\Model\CurlRequestResult;
use Synergy\Model\Exception\AssignmentException;
use Synergy\Model\Exception\AssignmentSecurityException;
use Synergy\Model\Exception\CurlRequestException;
use Synergy\Model\Membership;
use Synergy\Model\Tribe;
use Synergy\Model\User;
use Synergy\Extensions\Tribe\TribeSpecificationExtension;

/**
 * Description of TribeCtrl
 *
 * @author lada
 */
class TribeCtrl extends ExtensionCtrl {

    private $tribeDao;
    private $userCtrl;
    private $tribesCreated;
    private $projectCtrl;
    public static $tribes = array();
    private $tribesCached = false;

    function __construct() {
        parent::__construct('tribe');
        $this->tribeDao = new TribeDAO();
    }

    private function getUserCtrl() {
        if (is_null($this->userCtrl)) {
            $this->userCtrl = new UserCtrl();
        }
        return $this->userCtrl;
    }

    /**
     * 
     * @return ProjectCtrl
     */
    private function getProjectCtrl() {
        if (is_null($this->projectCtrl)) {
            $this->projectCtrl = new ProjectCtrl();
        }
        return $this->projectCtrl;
    }

    /**
     * Returns array of user's memberships in tribes
     * @param string $username username
     * @return Membership[]
     */
    public function getUserMembership($username) {
        $memberOf = $this->tribeDao->getUserMembership($username);
        $leaderOf = $this->getUserLeadership($username);
        $result = array_merge($memberOf, $leaderOf);
        return $result;
    }

    /**
     * Returns arrays of user's leaderships in tribes
     * @param string $username username
     * @return Membership|Array
     */
    public function getUserLeadership($username) {
        $userId = $this->getUserCtrl()->getUserIDbyUsername($username);
        return $this->tribeDao->getUserLeadership($userId);
    }

    /**
     * Returns tribe
     * @param int $id tribe ID
     * @return Tribe
     */
    public function getTribe($id) {
        $tribe = $this->tribeDao->getTribe($id);
        if (!is_null($tribe)) {
            $tribe->members = $this->getMembers($id);
            $img = $this->getUserCtrl()->getProfileImg($tribe->leader_id);
            $tribe->leaderImg = (strlen($img) > 0) ? IMAGE_BASE . $img : "./img/user.png";
        }
        return parent::get($tribe);
    }

    /**
     * Returns array of members for tribe with given ID
     * @param int $tribeId tribe ID
     * @return User[]
     */
    public function getMembers($tribeId) {
        return $this->tribeDao->getTribeMembers($tribeId);
    }

    /**
     * Removes user from tribe
     * @param string $username username
     * @param int $tribeId tribe ID
     */
    public function removeMember($username, $tribeId) {
        $user_id = $this->getUserCtrl()->getUserIDbyUsername($username);
        $this->tribeDao->removeMember($username, $tribeId, $user_id);
    }

    /**
     * Returns tribe leader's username
     * @param int $id tribe ID
     * @return User
     */
    public function getLeader($id) {
        return $this->tribeDao->getLeader($id);
    }

    /**
     * Edits tribe
     * @param Tribe $tribe tribe
     * @return boolean true if successful
     */
    public function editTribe($tribe) {
        $uid = $this->getUserCtrl()->getUserIDbyUsername($tribe->leaderUsername);
        if ($uid < 0) {
            return false;
        }
        $tribe->leader_id = $uid;
        $result = $this->tribeDao->editTribe($tribe->name, $tribe->description, $tribe->id, $tribe->leader_id);
        if ($result) {
            parent::edit($tribe);
        }
        return $result;
    }

    /**
     * Adds user to tribe
     * @param string $username username
     * @param int $id tribe ID
     * @return boolean true if successful
     */
    public function addMember($username, $id) {
        $uid = $this->getUserCtrl()->getUserIDbyUsername($username);
        if ($uid < 0) {
            return false;
        }
        if (!$this->getUserCtrl()->isMemberOfTribe($uid, $id)) {
            return $this->tribeDao->addMember($uid, $id);
        }
        return false;
    }

    /**
     * Returns all tribes
     * @return Tribe[]
     */
    public function getTribes() {
        $tribes = $this->tribeDao->getTribes();
        for ($index = 0, $max = count($tribes); $index < $max; $index++) {
            $tribes[$index]->leaderUsername = $this->getUserCtrl()->getUsernameById($tribes[$index]->leader_id);
        }
        return $tribes;
    }

    /**
     * Removes tribe
     * @param int $id tribe ID
     * @return boolean true if successful
     */
    public function removeTribe($id) {
        parent::delete($id);
        $this->tribeDao->removeMembersOfTribe($id);
        return $this->tribeDao->removeTribe($id);
    }

    /**
     * Creates new tribe
     * @param Tribe $tribe tribe
     * @return int new tribe ID
     */
    public function createTribe($tribe) {
        $uid = $this->getUserCtrl()->getUserIDbyUsername($tribe->leaderUsername);

        if ($uid < 0) {
            return 0;
        }
        $tribe->leader_id = $uid;
        $newId = $this->tribeDao->createTribe($uid, $tribe->description, $tribe->name);
        $tribe->id = $newId;
        parent::create($tribe, $newId);
        return $newId;
    }

    /**
     * Creates a new tribe, in case $tribe->leaderUsername is not found, current user is set as leader
     * @param Tribe $tribe
     * @return int new tribe ID
     */
    public function createTribeFallbackLeader($tribe) {
        $uid = $this->getUserCtrl()->getUserIDbyUsername($tribe->leaderUsername);
        if ($uid < 0) {
            $uid = Synergy::getSessionProvider()->getUserId();
        }
        $tribe->leader_id = $uid;
        $newId = $this->tribeDao->createTribe($uid, $tribe->description, $tribe->name);
        $tribe->id = $newId;
        parent::create($tribe, $newId);
        return $newId;
    }

    public function importTribes($url) {

        $data = $this->requestUrlForTribes($url);

        if ($data->headers['http_code'] !== 200) {
            throw new CurlRequestException("Curl request failed", "Response from URL was " . $data->headers['http_code'], "");
        }

        $tribes = json_decode($data->data);
        
        // empty all existing tribes
        $this->tribeDao->removeMembersOfAllTribes();

        $this->tribesCreated = 0;
        foreach ($tribes as $name => $tribe) {
            $t = Tribe::init($name, $tribe->leader);
            $newId = $this->createTribeIfNotExists($t);
            if ($newId > 0) {
                $this->addMembers($newId, $tribe->members);
            }
        }

        return $this->tribesCreated;
    }

    private function addMembers($tribeId, $members) {
        foreach ($members as $member) {
            $this->addMember($member->username, $tribeId);
        }
    }

    public function tribeExists($tribeName) {
        return $this->tribeDao->tribeExists($tribeName);
    }

    /**
     * 
     * @param Tribe $tribe
     */
    private function createTribeIfNotExists($tribe) {
        if (!$this->tribeExists($tribe->name)) {
            $newId = $this->createTribeFallbackLeader($tribe);
            if ($newId > 0) {
                $this->tribesCreated++;
            }
            return $newId;
        }
        return $this->getTribeIdByName($tribe->name);
    }

    public function getTribeIdByName($tribeName) {
        return $this->tribeDao->getTribeIdByName($tribeName);
    }

    /**
     * Makes curl request to specified url
     * @param String $url
     * @return CurlRequestResult
     */
    private function requestUrlForTribes($url) {
        $ch = curl_init();
        curl_setopt($ch, CURLOPT_HEADER, 0);
        curl_setopt($ch, CURLOPT_URL, $url);
        curl_setopt($ch, CURLOPT_RETURNTRANSFER, TRUE);
        curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, false);
        curl_setopt($ch, CURLOPT_SSL_VERIFYHOST, false);
        curl_setopt($ch, CURLOPT_FOLLOWLOCATION, 1);
        if (Synergy::hasProxy()) {
            curl_setopt($ch, CURLOPT_PROXY, Synergy::getProxy());
        }
//        curl_setopt($ch, CURLOPT_PROXYPORT, 80);
        $data = curl_exec($ch);
        $result = new CurlRequestResult($data, curl_getinfo($ch));
        curl_close($ch);
        return $result;
    }

    /**
     * Returns all tribes where given user is leader
     * @param String $leaderUsername
     * @return Tribe[]
     */
    public function getTribesByLeader($leaderUsername) {
        $leaderships = $this->getUserLeadership($leaderUsername);
        $tribes = array();
        for ($i = 0, $max = count($leaderships); $i < $max; $i++) {
            $t = $this->getTribe($leaderships[$i]->id);
            $l = new User($t->leaderUsername);
            $l->firstName = $t->leaderDisplayName;
            $l->lastName = "";
            $l->id = $t->leader_id;
            array_push($t->members, $l);
            array_push($tribes, $t);
        }
        return $tribes;
    }

    /**
     * Validates received assignments (permission and structure) and throws AssignmentException or AssignmentSecurityException in case some condition is not met.
     * @param \Synergy\Model\TestAssignment[] $assignments
     * @param Tribe[] $tribes
     */
    public function validateAssignments($assignments, $tribes) {
        for ($i = 0, $max = count($assignments); $i < $max; $i++) {
            $this->validateModel($assignments[$i]);
            $this->validatePermission($assignments[$i], $tribes);
        }
    }

    /**
     * Checks that object has all required properties so new assignment can be created based on it.
     * @param \Synergy\Model\TestAssignment $assignment
     * @throws AssignmentException in case some property is not set
     */
    private function validateModel($assignment) {
        if (!isset($assignment->specificationId) || !isset($assignment->platformId) || !isset($assignment->tribeId) || !isset($assignment->username) || !isset($assignment->labelId) || !isset($assignment->testRunId)) {
            throw new AssignmentException("Wrong model", "Missing some assignment properties", "");
        }
    }

    /**
     * Makes sure that specification in given assignment and assignees belongs to the same tribe
     * and that current user is tribe leader of this tribe
     * @param \Synergy\Model\TestAssignment $assignment
     * @param Tribe[] $tribes
     * @throws AssignmentSecurityException in case logged in user has no permission to create this assignment
     */
    public function validatePermission($assignment, $tribes) {
        if (Synergy::getSessionProvider()->getUserRole() === "admin" || Synergy::getSessionProvider()->getUserRole() === "manager") {
            return;
        }
        $assignment->tribeId = intval($assignment->tribeId);
        $tribeOk = false;
        $assigneeOk = false;
        $specificationOk = false;
        for ($i = 0, $maxi = count($tribes); $i < $maxi; $i++) {
            if (intval($tribes[$i]->id) === intval($assignment->tribeId)) {
                $tribeOk = true;
                // check assignee

                for ($j = 0, $maxj = count($tribes[$i]->members); $j < $maxj; $j++) {
                    if ($assignment->username === $tribes[$i]->members[$j]->username) {
                        $assigneeOk = true;
                    }
                }

                for ($j = 0, $maxj = count($tribes[$i]->ext["specifications"]); $j < $maxj; $j++) {
                    if (intval($assignment->specificationId) === intval($tribes[$i]->ext["specifications"][$j]->id)) {
                        $specificationOk = true;
                    }
                }

                // check specification
                break;
            }
        }
        if (!$tribeOk || !$assigneeOk || !$specificationOk) {
            $msg = "Specification check: " . ($specificationOk ? "OK" : "Failed") . " Tribe check: " . ($tribeOk ? "OK" : "Failed") . " Assignee check: " . ($assigneeOk ? "OK" : "Failed");
            throw new AssignmentSecurityException("Not allowed", "You don't have permissions for this action. " . $msg, "");
        }
    }

    /**
     * Returns all tribes with all possible information (members, specifications etc.)
     * @return Tribe[]
     */
    public function getTribesDetailed() {
        $tribes = $this->getTribes();
        $fullData = array();
        for ($i = 0, $max = count($tribes); $i < $max; $i++) {
            $t = $this->getTribe($tribes[$i]->id);
            $l = new User($t->leaderUsername);
            $l->firstName = $t->leaderDisplayName;
            $l->lastName = "";
            array_push($t->members, $l);
            array_push($fullData, $t);
        }
        return $fullData;
    }

    public function initTribesNameAndLeaders() {
        if (!$this->tribesCached) {
            $allTribes = $this->tribeDao->getTribesNameAndLeaders();
            $tribeExt = new TribeSpecificationExtension();
            for ($i = 0, $max = count($allTribes); $i < $max; $i++) {
                $allTribes[$i]->ext["specifications"] = $tribeExt->getSpecifications($allTribes[$i]->id);
                TribeCtrl::$tribes["t" . $allTribes[$i]->id] = $allTribes[$i];
            }
            $this->tribesCached = true;
        }
        return TribeCtrl::$tribes;
    }

    /**
     * Returns array of instances of TribeOverview with set specificationIDs
     * @param itn $tribesId array of tribe IDs
     * @return TribeOverview[]
     */
    public function getTribesSpecificationsForTribes($tribesId) {
        if (count($tribesId) < 1) {
            return array();
        }
        $tribeSqlString = Util::arrayToSQLOR($tribesId, "t.id");
        return $this->tribeDao->getTribeAndSpecifications($tribeSqlString);
    }

    /**
     * 
     * @param Tribe $tribes
     * @return type
     */
    public function filterByProjectId($tribes, $projectId) {
        foreach ($tribes as $tribe) {
            if (array_key_exists("specifications", $tribe->ext)) {
                $this->filter($projectId, $tribe);
            }
        }
        return $tribes;
    }

    private function filter($projectId, $tribe) {
        foreach ($tribe->ext["specifications"] as $index => $spec) {
            $hasProject = false;
            foreach ($spec->projects as $pr) {
                if ($pr->id === $projectId) {
                    $hasProject = true;
                }
            }
            if (!$hasProject) {
                unset($tribe->ext["specifications"][$index]);
            }
        }
        $tribe->ext["specifications"] = array_values($tribe->ext["specifications"]);
    }

}

?>
