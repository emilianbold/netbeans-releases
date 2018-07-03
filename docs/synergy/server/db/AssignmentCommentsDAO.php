<?php

namespace Synergy\DB;

use PDO;
use Synergy\Model\AssignmentComment;
use Synergy\Model\User;

/**
 * Description of AssignmentCommentsDAO
 *
 * @author vriha
 */
class AssignmentCommentsDAO {

    public function removeForAssignments($assignmentId) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("DELETE FROM assignment_comments WHERE assignment_id=:id");
        $handler->bindParam(':id', $assignmentId);
        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }

        return true;
    }

    public function getCommentsForRun($testRunId) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("SELECT ac.*, c.title as caseTitle, co.name, u.username, u.first_name, "
                . "u.last_name, sp.title as sptitle, sp.id as spid "
                . "FROM assignment_comments ac, test_assignement ta, user u, suite s, suite_has_case shc, `case` c, specification sp, comment co "
                . "WHERE co.id=ac.comment_type_id AND ta.test_run_id=:id AND ta.id=ac.assignment_id AND ac.case_id=c.id "
                . "AND shc.case_id=ac.case_id AND c.id=ac.case_id AND ac.suite_id=s.id AND  shc.suite_id=ac.suite_id AND s.specification_id=sp.id "
                . "AND ta.specification_id=sp.id AND u.id=ta.user_id AND shc.suite_id=ac.suite_id AND sp.is_active=1");
        $handler->bindParam(':id', $testRunId);

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }
        $comments = array();
        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            $c = new AssignmentComment($row["id"], $row["assignment_id"], $row["resolution"], $row["case_id"], $row["suite_id"], $row["resolver_id"], $row["comment_type_id"]);
            $c->commentText = $row["name"];
            $c->commentFreeText = $row["comment_free_text"];
            $c->specificationTitle = $row["sptitle"];
            $c->specificationId = intval($row["spid"]);
            $c->caseTitle = $row["caseTitle"];
            $c->authorDisplayName = $row["first_name"] . " " . $row["last_name"];
            $c->authorUsername = $row["username"];
            array_push($comments, $c);
        }
        return $comments;
    }

    public function getResolversForRun($testRunId) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("SELECT DISTINCT u.username, u.first_name, u.last_name, u.id "
                . "FROM user u, assignment_comments ac, test_assignement ta WHERE ac.resolver_id=u.id AND ac.assignment_id=ta.id AND ta.test_run_id=:id");
        $handler->bindParam(':id', $testRunId);

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }
        $resolvers = array();
        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            $c = new User($row["username"]);
            $c->firstName = $row["first_name"];
            $c->lastName = $row["last_name"];
            $resolvers["_" . $row["id"]] = $c;
        }
        return $resolvers;
    }

    /**
     * 
     * @param type $assignmentId
     * @param AssignmentComment[] $comments
     */
    public function insertComments($assignmentId, $comments) {
        DB_DAO::connectDatabase();
        $baseSql = "INSERT INTO assignment_comments (assignment_id, case_id, suite_id, resolution, comment_type_id, resolver_id, comment_free_text) VALUES ";
        for ($i = 0, $max = count($comments); $i < $max; $i++) {
            $key = time() . md5(intval($comments[$i]->assignmentId) . "," . intval($comments[$i]->caseId) . "," . intval($comments[$i]->suiteId) . ",'" . $comments[$i]->resolution . "'," . intval($comments[$i]->commentTypeId) . "," . intval($comments[$i]->resolverId) . ",'" . $comments[$i]->commentFreeText);
            $baseSql = $baseSql . "(:" . $key . "aid,:" . $key . "cid,:" . $key . "sid,:" . $key . "r,:" . $key . "cti,:" . $key . "rid,:" . $key . "cft),";
            $comments[$i]->setHash($key);
        }
        $baseSql = substr($baseSql, 0, strlen($baseSql)-1);
        $handler = DB_DAO::getDB()->prepare($baseSql);

        for ($i = 0, $max = count($comments); $i < $max; $i++) {
            $key = $comments[$i]->getHash();
            $handler->bindParam(':'.$key."aid", $assignmentId);
            $handler->bindParam(':'.$key."cid", $comments[$i]->caseId);
            $handler->bindParam(':'.$key."sid", $comments[$i]->suiteId);
            $handler->bindParam(':'.$key."r", $comments[$i]->resolution);
            $handler->bindParam(':'.$key."cti", $comments[$i]->commentTypeId);
            $handler->bindParam(':'.$key."rid", $comments[$i]->resolverId);
            $handler->bindParam(':'.$key."cft", $comments[$i]->commentFreeText);
        }

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }
    }

    //put your code here
}
