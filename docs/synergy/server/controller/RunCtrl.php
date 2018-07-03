<?php

namespace Synergy\Controller;

use DateTime;
use Synergy\App\Synergy;
use Synergy\DB\RunDAO;
use Synergy\Model\BlobSpecification;
use Synergy\Model\AssignmentProgress;
use Synergy\Model\Exception\AssignmentException;
use Synergy\Model\RunAttachment;
use Synergy\Model\Specification;
use Synergy\Model\SpecificationSkeleton;
use Synergy\Model\SuiteSkeleton;
use Synergy\Model\TestAssignment;
use Synergy\Model\TestCaseSkeleton;
use Synergy\Model\TestRun;
use Synergy\Model\TestRunStatistics;
use Synergy\Model\User;
use Synergy\Model\UserStatistics;
use Synergy\Model\Exception\AssignmentConflictException;
use Synergy\Model\Bug;
use Synergy\Model\Exception\CorruptedAssignmentException;

/**
 * Description of RunCtrl
 *
 * @author lada
 */
class RunCtrl {

    private $specCtrl;
    private $userCtrl;

    /** @var TribeCtrl */
    private $tribeCtrl;
    private $reviewCtrl;
    private $runDao;
    private $attachmentCtrl;

    /**
     *
     * @var CaseCtrl 
     */
    private $caseCtrl;

    function __construct() {
        $this->runDao = new RunDAO();
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

    /**
     * 
     * @return SpecificationCtrl
     */
    private function getSpecCtrl() {
        if (is_null($this->specCtrl)) {
            $this->specCtrl = new SpecificationCtrl();
        }
        return $this->specCtrl;
    }

    /**
     * 
     * @return ReviewCtrl
     */
    private function getReviewCtrl() {
        if (is_null($this->reviewCtrl)) {
            $this->reviewCtrl = new ReviewCtrl();
        }
        return $this->reviewCtrl;
    }

    private function getTribeCtrl() {
        if (is_null($this->tribeCtrl)) {
            $this->tribeCtrl = new TribeCtrl();
        }
        return $this->tribeCtrl;
    }

    private function getCaseCtrl() {
        if (is_null($this->caseCtrl)) {
            $this->caseCtrl = new CaseCtrl();
        }
        return $this->caseCtrl;
    }

    private function getUserCtrl() {
        if (is_null($this->userCtrl)) {
            $this->userCtrl = new UserCtrl();
        }
        return $this->userCtrl;
    }

    /**
     * Returns all information about given test run
     * @param int $id run ID
     * @param Bool $userCentric true if assignments should be grouped by specification and user (so run->assignment is array of User), false if assignments should be simple array of TestAssignment
     * @return TestRun|null
     */
    public function getRun($id, $userCentric) {
        $tr = $this->getRunOverview(intval($id));
        if (is_null($tr)) {
            return null;
        }
        $allIssues = array();
        if ($userCentric) {
            $tr->assignments = $this->getUserAssignments($tr->id, true);
            foreach ($tr->assignments as $user) {
                foreach ($user->assignments as $assignment) {
                    $tr->completed+=$assignment->completed;
                    $tr->total+=$assignment->total;
                    $allIssues = array_merge($allIssues, $assignment->issues);
                }
            }
        } else {
            $tr->assignments = $this->getAssignments($tr->id, true);
            foreach ($tr->assignments as $v) {
                $tr->completed+=$v->completed;
                $tr->total+=$v->total;
                $allIssues = array_merge($allIssues, $v->issues);
            }
        }
        $checkedIssues = Synergy::getProvider("issue_" . $tr->getBugTrackingSystem())->validateIssuesWithRunAssoc(array_unique($allIssues));
        $this->replaceIssuesWithObjects($tr, $checkedIssues, $userCentric);
        $tr->attachments = $this->getAttachments($tr->id);
        $tr->reviewAssignments = $this->getReviewCtrl()->getAssignments($id);
        return $tr;
    }

    /**
     * Replaces array of issue numbers in each assignment with array of Bug instances with proper bug information
     * @param TestRun $testRun
     * @param Bug[] $bugs 
     * @param boolean $userCentric
     */
    private function replaceIssuesWithObjects($testRun, $bugs, $userCentric) {
        if ($userCentric) {
            $testRun->assignments = $this->getUserAssignments($testRun->id, true);
            foreach ($testRun->assignments as $user) {
                foreach ($user->assignments as $assignment) {
                    $assignment->issues = $this->findIssuesObjects($assignment->issues, $bugs);
                }
            }
        } else {
            $testRun->assignments = $this->getAssignments($testRun->id, true);
            foreach ($testRun->assignments as $v) {
                $v->issues = $this->findIssuesObjects($v->issues, $bugs);
            }
        }
    }

    /**
     * Returns all Bug instances from $bugs parameter which ids are in $bugIds array
     * @param int[] $bugIds array of bugIds to be found
     * @param Bug[] $bugs array of all bugs which should be searched
     * @return Bug[] found issues
     */
    private function findIssuesObjects($bugIds, $bugs) {
        $records = array();
        foreach ($bugIds as $b) {
            if (array_key_exists("id" . $b, $bugs)) {
                array_push($records, $bugs["id" . $b]);
            } else {
                $x = new Bug(-1, $b);
                $x->status = "unknown";
                $x->priority = "unknown";
                array_push($records, $x);
            }
        }

        return $records;
    }

    /**
     * Returns array of RunAttachment for given test run
     * @param int $id run ID
     * @return RunAttachment[]
     */
    public function getAttachments($id) {
        return $this->runDao->getAttachments($id);
    }

    /**
     * Saves assignment progress
     * @param AssignmentProgress $data assignment progress
     * @param JSON $json JSON data
     * @param int $id assignment ID
     */
    public function saveAssignmentProgress($data, $json, $id) {

        // update record in test_assignment - number_of_cases, number_of_cases_completed, failed_cases, passed_cases, skipped_cases
        $numberOfCases = 0;
        $numberOfCasesCompleted = 0;
        $failedCases = 0;
        $passedCases = 0;
        $skippedCases = 0;

        $issues = array();
        $c_id = "";
        $durations = array();
        foreach ($data->specification->testSuites as $ts) {// each suite
            foreach ($ts->testCases as $c) { // each case
                if (intval($c->issue) > 0) {
                    $issues["_" . intval($c->id)] = $c->issue;
                }

                if (intval($c->duration) > 0 && intval($c->finished) === 1) {
                    $c_id = $c_id . " id=" . intval($c->id) . " OR";
                    $durations["_" . intval($c->id)] = intval($c->duration);
                }

                $numberOfCases++;
                if (intval($c->finished) === 1) {
                    $numberOfCasesCompleted++;
                }
                switch ($c->result) {
                    case "passed":
                        $passedCases++;
                        break;
                    case "skipped":
                        $skippedCases++;
                        break;
                    case "failed":
                        $failedCases++;
                        break;
                    default:
                        break;
                }
            }
        }

        if ($numberOfCases < 1 || $numberOfCasesCompleted < 1) {
            throw new CorruptedAssignmentException("Corrupted assignment", "Number of all cases is " . $numberOfCases . ", number of completed cases is " . $numberOfCasesCompleted);
        }

// save blob data
        $this->saveBlob($data);

        if ($numberOfCases === $numberOfCasesCompleted) {
            $this->deleteAssignmentProgress($id);
            $lockCtrl = new SpecificationLockCtrl();
            $lockCtrl->removeLockForAssignment($id);
        } else if ($numberOfCases > $numberOfCasesCompleted) {
            $this->saveProgress($json, $id);
        }

        $totalTime = 0;
        // add new issues
        // update estimated time for cases
        $issuesString = "";
        if (strlen($c_id) > 0) {
            $c_id = substr($c_id, 0, strlen($c_id) - 3);
            $c_id = "(" . $c_id . ")";
            $caseCtrl = new CaseCtrl();
            $cases = $caseCtrl->getCasesDuration($c_id);

            for ($i = 0, $maxi = count($cases); $i < $maxi; $i++) {
                if (isset($durations["_" . $cases[$i]->id]) && intval($durations["_" . $cases[$i]->id]) > 0) {
                    $newDurationInMinutes = intval(round($durations["_" . $cases[$i]->id] / (1000 * 60)));
                    $newDurationFloat = ($durations["_" . $cases[$i]->id] / (1000 * 60));
                    if ($newDurationInMinutes < 1) {
                        $newDurationInMinutes = 1;
                    }
                    $totalTime += $newDurationFloat;
                    $caseCtrl->updateDuration($cases[$i]->id, $newDurationInMinutes);
                }
                if (isset($issues["_" . $cases[$i]->id])) {
                    for ($m = 0, $maxm = count($issues["_" . $cases[$i]->id]); $m < $maxm; $m++) {
                        $caseCtrl->addIssue($issues["_" . $cases[$i]->id][$m], $cases[$i]->id);
                        $issuesString = $issuesString . ";" . $issues["_" . $cases[$i]->id][$m];
                    }
                }
            }
        }
        $totalTime = round($totalTime);
        if ($totalTime < 1) {
            $totalTime = 1;
        }
        $this->updateAssignment($id, $numberOfCases, $numberOfCasesCompleted, $failedCases, $skippedCases, $passedCases, $totalTime);
        $this->addIssues($id, $this->getIssues($issuesString));

        $ctrl = new AssignmentCommentsCtrl();
        $ctrl->replaceComments($id, $data);

        Mediator::emit("assignmentProgressUpdated", $id);
    }

    public function getBlobs($runId){
        return $this->runDao->getBlobs($runId);
    }
    public function getDurations($runId){
        return $this->runDao->getDurations($runId);
    }
    
    /**
     * Save static information for run view 3
     * @param type $assignmentData
     */
    private function saveBlob($assignmentData) {
        $assignment = $this->getAssignmentNoProgress($assignmentData->assignmentId);
        $label = $assignment->label;
        $spec = $this->getSpecCtrl()->getSpecificationFull($assignmentData->specification->id);
        $bl = new BlobSpecification($assignmentData, $spec);

        $s = '{';
        $s .= '"id" : ' . $assignmentData->assignmentId . ',';
        $s .= '"label" : ' . json_encode($label) . ',';
        $s .= '"user" : ' . json_encode($assignment->username) . ',';
        $s .= '"platform" : ' . json_encode($assignment->platform) . ',';
        $s .= '"specification" : ' . $bl->toBlob();
        $s .= '}';
        $this->runDao->removeBlob($assignment->id);
        $this->runDao->insertBlob($assignment->id, $assignment->testRunId, $s);
    }

    private function getIssues($issuesString) {
        if (strlen($issuesString) < 1) {
            return "";
        }
        return substr($issuesString, 1);
    }

    /**
     * Removes all records about performed test assignment (issues will not be removed!)
     * @param int $id assignment ID
     */
    public function restartAssignment($id) {
        // reset test_assignment
        $this->updateAssignment($id, -1, 0, 0, 0, 0, 0);
        Mediator::emit("assignmentRestarted", $id);
        // remove progress
        $this->deleteAssignmentProgress($id);
        date_default_timezone_set('UTC');
        $localTime = date('Y-m-d H:i:s');
        $this->startAssignment($id, $localTime);
    }

    /**
     * Returns test assignment without progress or specification data
     * @param type $id
     * @return TestAssignment
     */
    public function getAssignmentNoProgress($id) {
        $assignment = $this->runDao->getAssignment(intval($id));
        if (is_null($assignment)) {
            return null;
        }

        $assignment->testRunTitle = $this->getRunTitleForAssignment(intval($id));
        return $assignment;
    }

    /**
     * Returns assigment with test run, progress etc.
     * @param int $id assignment ID
     * @return TestAssignment|null
     */
    public function getAssignment($id) {
        $assignment = $this->runDao->getAssignment(intval($id));
        if (is_null($assignment)) {
            return null;
        }
        $assignment->total = $this->getSpecCtrl()->getCasesCount($assignment->specificationId, $assignment->labelId); // need to refresh total in case someone modified number of cases in spec since assignment creation
        $assignment->testRunTitle = $this->getRunTitleForAssignment(intval($id));

        $specification = $this->getSpecCtrl()->getSpecification($assignment->specificationId, $assignment->label, -1);
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
            $ts->testCases = $this->getCaseCtrl()->getTestCasesDetailed($ts->id, $assignment->label, $bugTrackingSystem);
        }
        $assignment->specificationData = $specification;

        $p = $this->getAssigmentProgress(intval($id), $assignment->label);
        if (is_null($p)) {
            if (intval($assignment->completed, 10) > 0) {
                throw new AssignmentConflictException("Conflict", "Structure of specification has changed since last testing and testing cannot be resumed. Please use Restart instead", "");
            } else {
                $assignment->progress = new AssignmentProgress($id, $this->buildSpecificationSkeleton($specification), $assignment->userId);
                $assignment->reset();
            }
        } else {
            $assignment->progress = $p;
            $assignment->progress = $this->synchronizeProgressAndSpecification($assignment->progress, $assignment->specificationData);
        }

        return $assignment;
    }

    /**
     * Synchronize progress and specification. The reason is that if someone paused test assignment, cases/suites could be modified since he started and thus
     * progress may be incomplete or obsolote. This method goes through fresh specification and checks if progress fits. If not it returns fresh new progress (restart)
     * @param int $userId ID of user to which is this assigned 
     * @param AssignmentProgress $progress assignment progress
     * @param Specification $specification specification
     * @return AssignmentProgress
     */
    private function synchronizeProgressAndSpecification($progress, $specification) {

        $shadow = $this->buildSpecificationSkeleton($specification);
        $suiteIterator = 0;
        foreach ($shadow->testSuites as $suite) {
            $caseIterator = 0;
            if (!isset($progress->specification->testSuites[$suiteIterator]) ||
                    is_null($progress->specification->testSuites[$suiteIterator]) ||
                    intval($suite->id) !== intval($progress->specification->testSuites[$suiteIterator]->id)) { // same suite on the same position
                //$this->restartAssignment($progress->assignmentId);
                throw new AssignmentConflictException("Conflict", "Structure of specification has changed since last testing and testing cannot be resumed. Please use Restart instead", "");
                // return $shadow;
            }
            foreach ($suite->testCases as $tc) {
                if (!isset($progress->specification->testSuites[$suiteIterator]->testCases[$caseIterator]) ||
                        is_null($progress->specification->testSuites[$suiteIterator]->testCases[$caseIterator]) ||
                        intval($tc->id) !== intval($progress->specification->testSuites[$suiteIterator]->testCases[$caseIterator]->id)) {// same case on the same position
                    //  $this->restartAssignment($progress->assignmentId);
                    throw new AssignmentConflictException("Conflict", "Structure of specification has changed since last testing and testing cannot be resumed. Please use Restart instead", "");
                    // return $shadow;
                }
                $caseIterator++;
            }
            $suiteIterator++;
        }

        return $progress;
    }

    /**
     * For assignment only
     * @param Specification $specification specification
     * @return SpecificationSkeleton skeleton
     */
    private function buildSpecificationSkeleton($specification) {
        $skeleton = new SpecificationSkeleton($specification->id);

        foreach ($specification->testSuites as $suite) {
            $skeletonSuite = new SuiteSkeleton($suite->id, $specification->id);

            foreach ($suite->testCases as $tcase) {
                $t = new TestCaseSkeleton($tcase->id);
                $t->duration = $tcase->duration;
                array_push($skeletonSuite->testCases, $t);
            }
            array_push($skeleton->testSuites, $skeletonSuite);
        }

        return $skeleton;
    }

    /**
     * Deletes all test assignments for given specification
     * @param int $id specification ID
     */
    public function deleteAssignmentsForSpecification($id) {
        $all = $this->getAssigmentsBySpecification($id);
        foreach ($all as $assignment) {
            $this->deleteAssignment($assignment->id);
        }
        Mediator::emit("refreshLocks", -1);
        $this->runDao->deleteAssignmentsForSpecification($id);
    }

    /**
     * Removes given test run and all assignments
     * @param int $id run ID
     * @return boolean true if successful
     */
    public function deleteRun($id) {
        $attachments = $this->getAttachments($id);
        foreach ($attachments as $att) {
            $this->getAttachmentCtrl()->deleteRunAttachment($att->id);
        }
        $assignments = $this->getAssignments($id);

        foreach ($assignments as $assignment) {
            $this->deleteAssignment($assignment->id);
        }

        $reviews = $this->getReviewCtrl()->getAssignments($id);
        foreach ($reviews as $rev) {
            $this->getReviewCtrl()->deleteAssignment($rev->id);
        }

        return $this->runDao->deleteRun($id);
    }

    /**
     * Returns array of assignments for given test run
     * @param int $id run ID
     * @return TestAssignment|Array
     */
    public function getAssignments($id, $withIssues = false) {
        $assignments = $this->runDao->getAssignments($id, $withIssues);
        for ($i = 0, $max = count($assignments); $i < $max; $i++) {
            $assignments[$i]->tribes = $this->getTribesForAssignment($assignments[$i]);
        }
        return $assignments;
    }

    /**
     * Returns array of assignments for given test run
     * @param int $id run ID
     * @return TestAssignment|Array
     */
    public function getAssignmentsInPeriod($id, $from, $to) {
        $assignments = $this->runDao->getAssignmentsInPeriod($id, $from, $to);
        for ($i = 0, $max = count($assignments); $i < $max; $i++) {
            $assignments[$i]->tribes = $this->getTribesForAssignment($assignments[$i]);
        }
        return $assignments;
    }

    /**
     * Unlike simple getAssignments(), this returns assignment group by specification and user to which it belongs to
     * @param int $id run ID
     * @param boolean $withIssues if issues should be retrieved as well
     */
    public function getUserAssignments($id, $withIssues = false) {
        $assignments = $this->getAssignments($id, $withIssues);
        $usersAssignments = array();
        foreach ($assignments as $assignment) {
            if (!isset($usersAssignments[$assignment->username . "_" . $assignment->specificationId . "_" . $assignment->labelId])) {
                $usersAssignments[$assignment->username . "_" . $assignment->specificationId . "_" . $assignment->labelId] = new User($assignment->username);
            }
            $usersAssignments[$assignment->username . "_" . $assignment->specificationId . "_" . $assignment->labelId]->addAsignment($assignment);
        }
        return $usersAssignments;
    }

    /**
     * Deletes all test assignments for test run
     * @param int $id run ID
     * @return boolean true if successful
     */
    public function deleteAssignmentsForTestRun($id) {
        $r = $this->runDao->deleteAssignmentsForTestRun($id);
        Mediator::emit("refreshLocks", -1);
        return $r;
    }

    /**
     * Removes given test assignment
     * @param int $id assignment ID
     * @return boolean true if successful
     */
    public function deleteAssignment($id) {
        $this->deleteAssignmentProgress($id);
        return $this->runDao->deleteAssignment($id);
    }

    /**
     * Returns all test runs ordered by date descending. Results are paginated, first page is 1
     * @param int $page page number
     * @return TestRunList 
     */
    public function getRuns($page) {
        $tr = $this->runDao->getRuns($page);
        if (!is_null($tr)) {

            foreach ($tr->testRuns as $run) {
                $run->membersCount += $this->getReviewCtrl()->countAssignmentsForRun($run->id);
            }
        }
        return $tr;
    }

    /**
     * Returns list of latest test runs (maximum number of runs is $limit) where end date is >= today, ordered by start date DESC
     * @param int $limit
     * @return \TestRunList
     */
    public function getLatestRuns($limit) {
        return $this->runDao->getLatestRuns($limit);
    }

    /**
     * Creates new test run
     * @param string $title title
     * @param string $desc description
     * @param string $start start date
     * @param string $stop stop date
     * @return int ID of new test run
     */
    public function createRun($title, $desc, $start, $stop, $notifications, $projectId = -1) {
        return $this->runDao->createRun($title, $desc, $start, $stop, $notifications, $projectId);
    }

    /**
     * Returns basic test run info
     * @param int $id run ID
     * @return TestRun|null $id
     */
    public function getRunOverview($id) {
        $tr = $this->runDao->getRun($id);
        if (is_null($tr)) {
            return null;
        }
        $tr->attachments = $this->getAttachments($id);
        return $tr;
    }

    /**
     * Updates basic test run information
     * @param int $id run ID
     * @param string $title title
     * @param string $desc description
     * @param string $start start date
     * @param string $stop stop date
     * @param itn $notifications notifications
     * @return boolean true if successful
     */
    public function editRun($id, $title, $desc, $start, $stop, $notifications, $projectId = -1) {
        return $this->runDao->editRun($id, $title, $desc, $start, $stop, $notifications, $projectId);
    }

    /**
     * Creates new test assignment
     * @param int $specificationId specification ID
     * @param int $platformId platform ID
     * @param int $labelId label ID
     * @param int $testRunId test run ID
     * @param string $username username
     * @param int $tribeId tribe ID
     * @param int $createdBy flag to indicate who created this assignment (in order to allow to delete assignment by user/tribe leader if he created it), possible values: 1 (for admin/manager), 2 (for volunteer), 3 (for tribe leader)
     * @return boolean true if successful
     */
    public function createAssignment($specificationId, $platformId, $labelId, $testRunId, $username, $tribeId, $createdBy) {
        if (strlen($username) < 1) {
            if ($tribeId < 0) {
                return false;
            }

            $users = $this->getTribeCtrl()->getMembers($tribeId);
            $numberOfCases = $this->getSpecCtrl()->getCasesCount($specificationId, $labelId);

            for ($i = 0, $maxi = count($users); $i < $maxi; $i++) {
                $this->runDao->createAssignment($specificationId, $platformId, $labelId, $testRunId, $users[$i]->id, $numberOfCases, $createdBy);
            }

            return true;
        } else {
            $userId = $this->getUserCtrl()->getUserIDbyUsername($username);
            $numberOfCases = $this->getSpecCtrl()->getCasesCount($specificationId, $labelId);
            if ($userId < 0) {
                return false;
            }
            return $this->runDao->createAssignment($specificationId, $platformId, $labelId, $testRunId, $userId, $numberOfCases, $createdBy);
        }
    }

    /**
     * For each assignment, get list of cases ordered by SUITE_TITLE + CASE_TITLE. If these data are not yet in DB, it will be 
     * created (not stored though) and returned to user so he'll get empty/blank progress
     * @param int $id assignment id
     * @return AssignmentProgress $data 
     */
    public function getAssigmentProgress($id, $label = '') { // FIXME typo :(
        return $this->runDao->getAssigmentProgress($id, $label);
    }

    /**
     * Checks if given user is assigned to given test assignment
     * @param int $id assignment ID
     * @param string $username username
     * @return boolean true if user is assigned for given assignment
     */
    public function checkUserIsAssigned($id, $username) {
        return $this->runDao->checkUserIsAssigned($id, $username);
    }

    /**
     * Saves assignment progress to DB
     * @param JSON $json data
     * @param int $id assignment ID
     */
    private function saveProgress($json, $id) {
        $this->deleteAssignmentProgress($id);
        $this->runDao->saveAssignmentProgress($json, $id);
    }

    /**
     * Updates cases count based on data retrieved from finished test assignment
     * @param int $id assignment ID
     * @param int $numberOfCases number of total cases
     * @param int $numberOfCasesCompleted number of completed cases
     * @param int $failedCases number of failed cases
     * @param int $skippedCases number of skipped cases
     * @param int $passedCases number of passed cases
     * @param string $totalTime int minutes taken to complete assignment
     */
    public function updateAssignment($id, $numberOfCases, $numberOfCasesCompleted, $failedCases, $skippedCases, $passedCases, $totalTime) {
        $this->runDao->updateAssignment($id, $numberOfCases, $numberOfCasesCompleted, $failedCases, $skippedCases, $passedCases, $totalTime);
    }

    /**
     * Removes assignment progress JSON dump from DB
     * @param int $id assignment ID
     * @return boolean true if successful
     */
    public function deleteAssignmentProgress($id) {
        return $this->runDao->deleteAssignmentProgress($id);
    }

    /**
     * Returns title of test run for given assignment
     * @param int $assignmentId
     * @return string
     */
    public function getRunTitleForAssignment($assignmentId) {
        return $this->runDao->getRunTitleForAssignment($assignmentId);
    }

    /**
     * Returns all users assignments that are either unfinished (cases total != cases completed) or are still running (test run's end date >= today)
     * @param String $username
     * @return TestAssignment[] Description
     */
    public function getUsersAssignments($username) {
        return $this->runDao->getUsersAssignments($username);
    }

    /**
     * Returns all users assignments
     * @param String $username
     * @return TestAssignment[] Description
     */
    public function getAllUsersAssignments($username) {
        return $this->runDao->getAllUsersAssignments($username);
    }

    /**
     * removes all assignments for given user
     * @param String $username
     */
    public function deleteUsersAssignments($username) {
        $a = $this->getAllUsersAssignments($username);
        foreach ($a as $assignment) {
            $this->deleteAssignment($assignment->id);
        }
    }

    /**
     * Returns all test runs with start date greater than or equal $startDate and smaller or equal $stopDate
     * @param DateTime $startDate bottom limit
     * @param DateTime $stopDate upper limit
     * @return TestRun[] matching test runs
     */
    public function getRunsByDate($startDate, $stopDate) {
        return $this->runDao->getRunsByDate($startDate, $stopDate);
    }

    /**
     * Returns only basic information about assignment
     * @param int $id assignment ID
     * @return TestAssignment Description
     */
    public function getBasicAssignment($id) {
        return $this->runDao->getBasicAssignment($id);
    }

    /**
     * Creates a matrix assignment, parameter $data is expected to have properties: tribes (array), users (array), platforms (array), runId and versionId
     * @param type $data
     * @param int $createdBy flag to indicate who created this assignment (in order to allow to delete assignment by user/tribe leader if he created it), possible values: 1 (for admin/manager), 2 (for volunteer), 3 (for tribe leader)
     * @return type
     */
    public function createMatrixAssignment($data, $createdBy) {
        $labelId = -1;
        $project = $this->getProject($data->runId);
        if (isset($data->labelId) && !empty($data->labelId)) {
            $labelId = intval($data->labelId);
        }
        $allOwners = array();
        $allMembers = array();
        for ($k = 0, $maxk = count($data->tribes); $k < $maxk; $k++) {
            $members = $this->getTribeCtrl()->getMembers($data->tribes[$k]->id);
            $leader = $this->getTribeCtrl()->getLeader($data->tribes[$k]->id);
            if (!$this->leaderIsMember($members, $leader)) {
                array_push($members, $leader);
            }
            $result = array_merge($allMembers, $members);
            $allMembers = $result;
            for ($l = 0, $maxl = count($members); $l < $maxl; $l++) {
                if (!isset($allOwners[$members[$l]->username])) {
                    $allOwners[$members[$l]->username] = $this->getSpecCtrl()->getSpecificationsByOwnerAndVersion($members[$l]->username, $data->versionId, $project->id);
                }
            }
        }

        for ($j = 0, $maxj = count($data->users); $j < $maxj; $j++) {
            if (!isset($allOwners[$data->users[$j]->username])) {
                $allOwners[$data->users[$j]->username] = $this->getSpecCtrl()->getSpecificationsByOwnerAndVersion($data->users[$j]->username, $data->versionId, $project->id);
            }
        }

        $processedUsers = array();
        $failures = array();
        for ($i = 0, $maxi = count($data->platforms); $i < $maxi; $i++) {
            for ($j = 0, $maxj = count($data->users); $j < $maxj; $j++) {// iterate over users
                if (!$this->createMatrixAssignmentForUser($allOwners[$data->users[$j]->username], $data->platforms[$i]->id, $data->users[$j]->username, $data->runId, $labelId, $createdBy)) {
                    array_push($failures, $data->users[$j]->username);
                }
                $processedUsers[$data->users[$j]->username] = 1;
            }

            for ($k = 0, $maxk = count($allMembers); $k < $maxk; $k++) {
                if (!isset($processedUsers[$allMembers[$k]->username])) { // so not yet processed in iteration above
                    if (!$this->createMatrixAssignmentForUser($allOwners[$allMembers[$k]->username], $data->platforms[$i]->id, $allMembers[$k]->username, $data->runId, $labelId, $createdBy)) {
                        array_push($failures, $allMembers[$k]->username);
                    }
                }
            }
        }
        return $failures;
    }

    /**
     * Takes platform, username and version and creates a new assignments for all specifications of the version where user is owner. 
     * @todo This could be simplified, the DB query behind retrieves a lot of extra information
     * @param int $createdBy flag to indicate who created this assignment (in order to allow to delete assignment by user/tribe leader if he created it), possible values: 1 (for admin/manager), 2 (for volunteer), 3 (for tribe leader)
     * @return boolean true if all went OK
     */
    private function createMatrixAssignmentForUser($specifications, $platformId, $username, $testRunId, $labelId, $createdBy) {
        if (!isset($specifications)) {
            return true;
        }
        $succeeded = 0;
        for ($i = 0, $max = count($specifications); $i < $max; $i++) {
            if ($this->createAssignment($specifications[$i]->id, $platformId, $labelId, $testRunId, $username, -1, $createdBy)) {
                $succeeded++;
            }
        }
        return $succeeded === count($specifications);
    }

    /**
     * Adds start timestamp to assignment
     * @param int $assignmentId
     * @param DateTime $startTime
     */
    public function startAssignment($assignmentId, $startTime) {
        $this->runDao->startAssignment($assignmentId, $startTime);
    }

    /**
     * Starts assignment only if it hasn't been started yet (e.g. in case of continue, started time is not modified)
     */
    public function startAssignmentConditional($assignmentId, $startTime) {
        if (!$this->runDao->assignmentAlreadyStarted($assignmentId)) {
            $this->runDao->startAssignment($assignmentId, $startTime);
        }
    }

    /**
     * Returns array of assignees of given test run
     * @param int $testRunId
     * @return User|Array
     */
    public function getAssignees($testRunId) {
        $users = $this->runDao->getAssignees($testRunId);
        $reviewers = $this->getReviewCtrl()->getAssignees($testRunId);
        foreach ($reviewers as $username => $reviewer) {
            if (!array_key_exists($username, $users)) {
                $users[$username] = $reviewer;
            }
        }
        return array_values($users);
    }

    /**
     * Checks if array of users contains also user with same username as leader
     * @param User|Array $members
     * @param User $leader
     * @return boolean true if leader is also member, false otherwise
     */
    private function leaderIsMember($members, $leader) {
        foreach ($members as $member) {
            if ($member->username === $leader->username) {
                return true;
            }
        }
        return false;
    }

    private function getAssigmentsBySpecification($id) {
        return $this->runDao->getAssignmentsBySpecification($id);
    }

    public function validateAssignments($assignments) {
        for ($i = 0, $maxi = count($assignments); $i < $maxi; $i++) {
            if (!isset($assignments[$i]->specificationId) || !isset($assignments[$i]->platformId) || !isset($assignments[$i]->username) || !isset($assignments[$i]->labelId) || !isset($assignments[$i]->testRunId)) {
                throw new AssignmentException("Wrong model", "Missing some assignment properties", "");
            }
        }
    }

    /**
     * Freezes/unfreezes test run. If test run is supposed to be freezen, creates JSON static file for statistics page
     * @param type $testRunId
     * @param boolean $isFrozen target test run status
     * @return type
     */
    public function setActive($testRunId, $isFrozen) {
        // if is frozen, drop progress
        $result = $this->runDao->setActive($testRunId, $isFrozen);
        if ($isFrozen) {
            $ctrl = new StatisticsCtrl();
            $ctrl->cacheStatistics($testRunId);
            $this->deleteAssignmentProgressForRun($testRunId);
        }
        return $result;
    }

    /**
     * Checks if test run is frozen
     * @param type $testRunId
     * @return boolean true if test run is not frozen
     */
    public static function runIsActive($testRunId) {
        return RunDAO::runIsActive($testRunId);
    }

    public function getRunIdByAssignmentId($assignmentId) {
        return $this->runDao->getRunIdByAssignmentId($assignmentId);
    }

    public function deleteAssignmentProgressForRun($testRunId) {
        return $this->runDao->deleteAssignmentProgressForRun($testRunId);
    }

    /**
     * Saves issues (string separated by ;) to DB for given assignment
     * @param type $assignmentId
     * @param type $issuesString
     * @return boolean true on success
     */
    public function addIssues($assignmentId, $issuesString) {
        return $this->runDao->addIssues($assignmentId, $issuesString);
    }

    /**
     * Returns all information and statistics about test run
     * @param type $runId
     * @return \Synergy\Model\TestRunStatistics|null
     */
    public function getRunWithIssues($runId) {
        $tr = $this->getRunOverview(intval($runId));
        if (is_null($tr)) {
            return null;
        }
        $allIssues = array();
        $tr->assignments = $this->getAssignments($tr->id, true);
        foreach ($tr->assignments as $v) {
            $tr->completed+=$v->completed;
            $tr->total+=$v->total;
            $allIssues = array_merge($allIssues, $v->issues);
        }
        $allIssues = array_unique($allIssues);
        $checkedIssues = Synergy::getProvider("issue_" . $tr->getBugTrackingSystem())->validateIssuesWithRun($allIssues);
        return new TestRunStatistics($checkedIssues, $tr);
    }

    /**
     * Returns all information and statistics about test run where each assignment's last udpated date must be in between given dates
     * @param type $runId
     * @param String $from string in MySQL datetime format
     * @param String $to string in MySQL datetime format
     * @return \Synergy\Model\TestRunStatistics|null
     */
    public function getRunWithIssuesInPeriod($runId, $from, $to) {
        $tr = $this->getRunOverview(intval($runId));
        if (is_null($tr)) {
            return null;
        }
        $allIssues = array();
        $tr->assignments = $this->getAssignmentsInPeriod($tr->id, $from, $to);
        foreach ($tr->assignments as $v) {
            $tr->completed+=$v->completed;
            $tr->total+=$v->total;
            $allIssues = array_merge($allIssues, $v->issues);
        }
        $allIssues = array_unique($allIssues);
        $checkedIssues = Synergy::getProvider("issue_" . $tr->getBugTrackingSystem())->validateIssuesWithRun($allIssues);
        return new TestRunStatistics($checkedIssues, $tr);
    }

    /**
     * 
     * @param TestRunStatistics $tr
     */
    public function getUserCentricData($tr) {
        $users = array();
        $tribeCtrl = new TribeCtrl();
        for ($i = 0, $maxi = count($tr->testRun->assignments); $i < $maxi; $i++) {
            $assignment = $tr->testRun->assignments[$i];
            if (!array_key_exists($assignment->username, $users)) {
                $users[$assignment->username] = new UserStatistics();
                $users[$assignment->username]->addMembership($tribeCtrl->getUserMembership($assignment->username));
            }
            $users[$assignment->username]->addAssignment($assignment);
        }
        return $users;
    }

    public function getRunTitle($testRunId) {
        return $this->runDao->getRunTitle($testRunId);
    }

    /**
     * 
     * @param TestAssignment $assignment
     */
    private function getTribesForAssignment($assignment) {
        $tribes = array();
        $aTribes = $assignment->getTribesId(); // based on user_member_of
        $allTribes = $this->getTribeCtrl()->initTribesNameAndLeaders();

        for ($i = 0, $maxi = count($aTribes); $i < $maxi; $i++) {
            $t = $allTribes["t" . $aTribes[$i]];
            for ($j = 0, $m = count($t->ext["specifications"]); $j < $m; $j++) {
                if ($assignment->specificationId === $t->ext["specifications"][$j]->id && !in_array($t->name, $tribes)) {
                    array_push($tribes, $t->name);
                }
            }
        }

        // find leader
        foreach ($allTribes as $key => $tribe) {
            for ($j = 0, $m = count($tribe->ext["specifications"]); $j < $m; $j++) {
                if ($assignment->specificationId === $tribe->ext["specifications"][$j]->id && !in_array($tribe->name, $tribes) && $assignment->username === $tribe->leaderUsername) {
                    array_push($tribes, $tribe->name);
                }
            }
        }
        return $tribes;
    }

    /**
     * Returns true if given assignment was last updated before given date
     * @param type $assignmentId
     * @param String $requestedTimestamp string in MySQL datetime format
     * @return type
     */
    public function isRequestUpToDate($assignmentId, $requestedTimestamp) {
        return $this->runDao->isRequestUpToDate($assignmentId, $requestedTimestamp);
    }

    /**
     * 
     * @param type $testRunId
     * @return \Synergy\Model\Project\Project
     */
    public function getProject($testRunId) {
        return $this->runDao->getProject($testRunId);
    }

}

?>
