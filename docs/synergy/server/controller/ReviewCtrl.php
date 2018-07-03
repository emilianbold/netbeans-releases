<?php

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

namespace Synergy\Controller;

use Synergy\App\Synergy;
use Synergy\DB\ReviewDAO;
use Synergy\Model\Review\ReviewAssignment;
use Synergy\Model\Review\ReviewComment;

/**
 * Description of TutorialCtrl
 *
 * @author vriha
 */
class ReviewCtrl {

    public static $latestRemovalType;
    private $userCtrl;
    private $reviewDao;

    function __construct() {
        $this->reviewDao = new ReviewDAO();
    }

    private function getUserCtrl() {
        if (is_null($this->userCtrl)) {
            $this->userCtrl = new UserCtrl();
        }
        return $this->userCtrl;
    }

    /**
     * Fetches given URL
     * @param type $url
     * @return type
     */
    public function getReviewPage($url) {
        $rawPage = file_get_contents($url);
        $formatter = Synergy::getProvider("review");
        $base = "<base href='" . $url . "'></base>"; // to resolve relative links correctly
        $content = $formatter->prepare($rawPage, $url);
        $content = preg_replace("/<\/body>/", "<script src=\"http://netbeans-vm.apache.org/synergy/client/app/js/excl/inspectx.js\"></script><script src=\"http://netbeans-vm.apache.org/synergy/client/app/js/excl/wgxpath.install.js\"></script>wgxpath.install();<script></script></body>", $content); // scripts
        $content = preg_replace("/body{display:none !important;}/", "", $content); // scripts
        $content = preg_replace("/<\/head>/", "<style>.sn_selected {    background: #99ccff;    border: 1px solid red;} * { cursor:pointer}</style>" . $base . "</head>", $content); // scripts
        return $content;
    }

    /**
     * Creates new review assignment
     * @param int $testRunId test run ID
     * @param string $username username
     * @param string $reviewUrl tribe ID
     * @param int $createdBy flag to indicate who created this assignment (in order to allow to delete assignment by user/tribe leader if he created it), possible values: 1 (for admin/manager), 2 (for volunteer), 3 (for tribe leader)
     * @return boolean true if successful
     */
    public function createAssignment($testRunId, $username, $reviewUrl, $createdBy, $title, $owner) {
        $userId = $this->getUserCtrl()->getUserIDbyUsername($username);
        if ($userId < 0) {
            return false;
        }
        return $this->reviewDao->createAssignment(intval($testRunId), $userId, $reviewUrl, $createdBy, $title, $owner);
    }

    public function getBasicAssignment($assignmentId) {
        return $this->reviewDao->getBasicAssignment($assignmentId);
    }

    public function countAssignmentsForRun($testRunId) {
        return $this->reviewDao->countAssignmentsForRun($testRunId);
    }

    public function getAssignments($testRunId) {
        return $this->reviewDao->getAssignments($testRunId);
    }

    public function getAssignees($testRunId) {
        return $this->reviewDao->getAssignees($testRunId);
    }

    /**
     * Returns true if logged in user can delete assignment with ID assignmentId. User can delete assignment if:
     * <ul>
     * <li>User is admin or manager</li>
     * <li>given assignment was created as voluntarily and assignee === userId</li>
     * <li>assignment was created by tribe leader and specification belongs to the same tribe and assignee is member of the same tribe</li>
     * </ul>
     * @param int $assignmentId
     * @return boolean
     */
    public function userCanDeleteAssignmentById($assignmentId) {
        $assignmentMeta = $this->reviewDao->getAssignmentInfo($assignmentId);
        switch ($assignmentMeta["createdBy"]) {
            case ReviewAssignment::CREATED_BY_MANAGER_ADMIN:
                ReviewCtrl::$latestRemovalType = ReviewAssignment::CREATED_BY_MANAGER_ADMIN;
                return ReviewAssignment::canDelete() || Synergy::getSessionProvider()->getUserId() === $assignmentMeta["userId"];
            case ReviewAssignment::CREATED_BY_TESTER:
                ReviewCtrl::$latestRemovalType = ReviewAssignment::CREATED_BY_TESTER;
                return ReviewAssignment::canDelete() || Synergy::getSessionProvider()->getUserId() === $assignmentMeta["userId"];
            default:
                return false;
        }
        return false;
    }

    public function deleteAssignment($assignmentId) {
        $this->deleteComments($assignmentId);
        return $this->reviewDao->deleteAssignment($assignmentId);
    }

    public function deleteComments($assignmentId) {
        return $this->reviewDao->deleteComments($assignmentId);
    }

    public function getAssignment($assignmentId) {
        $assignment = $this->reviewDao->getAssignment($assignmentId, $escapeEmail);
        $assignment->comments = $this->getComments($assignmentId);
        return $assignment;
    }

    public function getAssignmentWithoutComments($assignmentId, $escapeEmail = true) {
        $assignment = $this->reviewDao->getAssignment($assignmentId, $escapeEmail);
        return $assignment;
    }

    public function getComments($assignmentId) {
        return $this->reviewDao->getComments($assignmentId);
    }

    public function setLastUpdated($localTime, $assignmentId) {
        $this->reviewDao->setLastUpdated($localTime, $assignmentId);
    }

    public function isRequestUpToDate($assignmentId, $requestedTimestamp) {
        return $this->reviewDao->isRequestUpToDate($assignmentId, $requestedTimestamp);
    }

    public function checkUserIsAssigned($assignmentId, $username) {
        return $this->reviewDao->checkUserIsAssigned($assignmentId, $username);
    }

    public function getRunIdByAssignmentId($assignmentId) {
        return $this->reviewDao->getRunIdByAssignmentId($assignmentId);
    }

    public function saveAssignmentProgress($data, $assignmentId) {
        $this->deleteComments($assignmentId);
        date_default_timezone_set('UTC');
        $timeInMinutes = round(intval($data->timeTaken) / 60000);
        if ($timeInMinutes < 1) {
            $timeInMinutes = 1;
        }
        $this->updateStatus($assignmentId, date('Y-m-d H:i:s'), $data->isFinished, $timeInMinutes, $data->weight);
        $this->reviewDao->insertComments($this->getCommentsFromProgress($assignmentId, $data), $assignmentId);
        if ($data->isFinished) {
            Mediator::emit("reviewFinished", $assignmentId);
        }
    }

    /**
     * Returns array of comments from submitted progress
     * @return ReviewComment[]
     */
    private function getCommentsFromProgress($assignmentId, $progressData) {
        $comments = array();
        foreach ($progressData->comments as $c) {
            $r = new ReviewComment(Synergy::getSessionProvider()->getUsername(), "", $c->text, -1, $c->elements);
            $r->setAssignmentId($assignmentId);
            array_push($comments, $r);
        }
        return $comments;
    }

    public function restartAssignment($assignmentId) {
        $this->deleteComments($assignmentId);
        $this->reviewDao->setTimeTaken($assignmentId, 0);
    }

    public function updateStatus($assignmentId, $localTime, $isFinished, $timeTaken, $pageValue = 0) {
        $pageValue = $isFinished ? $pageValue : 0; // #254707
        $this->reviewDao->updateStatus($assignmentId, $localTime, $isFinished, $timeTaken, $pageValue);
    }

    public function getAssignmentsInPeriod($testRunId, $from, $to) {
        return $this->reviewDao->getAssignmentsInPeriod($testRunId, $from, $to);
    }
    
    public function createReviewPage($reviewPage){
        return $this->reviewDao->createReviewPage($reviewPage);
    }

}
