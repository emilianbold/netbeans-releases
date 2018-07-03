<?php

namespace Synergy\Controller;

use Synergy\App\Synergy;
use Synergy\DB\AssignmentCommentsDAO;
use Synergy\DB\DB_DAO;
use Synergy\Interfaces\Observer;
use Synergy\Misc\Util;
use Synergy\Model\AssignmentComment;
use Synergy\Model\AssignmentComments;
use Synergy\Model\Exception\AssignmentCommentException;

/**
 * Description of AssignmentsCommentCtrl
 *
 * @author vriha
 */
class AssignmentCommentsCtrl implements Observer {

    private $assignmentCommentsDao;
    private $runCtrl;
    private static $listening = array("assignmentDeleted", "assignmentRestartedByUser");
    private $ready = false;

    function __construct() {
        $this->ready = true;
        $this->assignmentCommentsDao = new AssignmentCommentsDAO();
    }

    /**
     * 
     * @return RunCtrl
     */
    public function getRunCtrl() {
        if (is_null($this->runCtrl)) {
            $this->runCtrl = new RunCtrl();
        }
        return $this->runCtrl;
    }

    public static function on($name, $data) {
        if (in_array($name, AssignmentCommentsCtrl::$listening)) {
            $instance = new self();
            if ($instance->ready) {
                $instance->handleEvent($name, $data);
            }
        }
    }

    public function handleEvent($name, $data) {

        switch ($name) {
            case "assignmentDeleted":
            case "assignmentRestartedByUser":
                $this->deleteCommentsForAssignment($data);
                break;
            default:
                break;
        }
    }

    private function deleteCommentsForAssignment($assignmentId) {
        $this->assignmentCommentsDao->removeForAssignments($assignmentId);
    }

    /**
     * Returns array of comments from submitted progress
     * @param int $assignmentId
     * @param \Synergy\Model\AssignmentProgress $progressData
     * @return AssignmentComment[]
     */
    private function getCommentsFromProgress($assignmentId, $progressData) {
        $comments = array();
        foreach ($progressData->specification->testSuites as $ts) {
            foreach ($ts->testCases as $c) {
                if (intval($c->comment) > 0) {
                    array_push($comments, new AssignmentComment(-1, $assignmentId, "new", $c->id, $ts->id, -1, $c->comment, $c->commentFreeText));
                }
            }
        }
        return $comments;
    }
    
    /**
     * When tester submits data, deletes all old comments for given assignment and inserts new ones
     * @param int $assignmentId
     * @param \Synergy\Model\AssignmentProgress $progressData
     */
    public function replaceComments($assignmentId, $progressData) {
        $this->deleteCommentsForAssignment($assignmentId);
        $comments = $this->getCommentsFromProgress($assignmentId, $progressData);
        if(count($comments) > 0){
            $this->assignmentCommentsDao->insertComments($assignmentId, $comments);
        }
    }
    
    /**
     * Returns all comments for given test run
     * @param type $testRunId
     * @return \Synergy\Model\AssignmentComments[]
     */
    public function getComments($testRunId) {
        $comments = $this->assignmentCommentsDao->getCommentsForRun($testRunId);
        $resolvers = $this->assignmentCommentsDao->getResolversForRun($testRunId);
        for ($i = 0, $max = count($comments); $i < $max; $i++) {
            if ($comments[$i]->resolverId > 0) {
                $comments[$i]->resolverUsername = $resolvers["_" . $comments[$i]->resolverId]->username;
                $comments[$i]->resolverDisplayName = $resolvers["_" . $comments[$i]->resolverId]->firstName . " " . $resolvers["_" . $comments[$i]->resolverId]->lastName;
            }
        }

        return new AssignmentComments(intval($testRunId), $this->getRunCtrl()->getRunTitle($testRunId), $comments);
    }
    
    /**
     * Makes sure that all comments have ID property
     * @param \Synergy\Model\AssignmentComments[] $comments
     * @throws AssignmentCommentException
     */
    public function validate($comments) {
        for ($i = 0, $max = count($comments); $i < $max; $i++) {
            if (!isset($comments[$i]->id)) {
                throw new AssignmentCommentException("Wrong model", "Missing parameter", "");
            }
        }
    }
    
    /**
     * Marks all given comments as resolved and sets resolver to logged user
     * @param \Synergy\Model\AssignmentComments $data
     * @return type
     */
    public function resolveComments($data) {
        if (count($data) < 1) {
            return;
        }
        $ids = array();
        for ($i = 0, $max = count($data); $i < $max; $i++) {
            array_push($ids, intval($data[$i]->id));
        }
        DB_DAO::executeQuery("UPDATE assignment_comments SET resolution='resolved', resolver_id=" . Synergy::getSessionProvider()->getUserId() . " WHERE " . Util::arrayToSQLOR($ids, "id"));
    }

}
