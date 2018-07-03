<?php

namespace Synergy\DB;

use PDO;
use Synergy\Controller\Mediator;
use Synergy\Model\Review\ReviewAssignment;
use Synergy\Model\Review\ReviewComment;
use Synergy\Model\User;

/**
 * Description of ReviewDAO
 *
 * @author vriha
 */
class ReviewDAO {

    public function createAssignment($testRunId, $userId, $reviewUrl, $createdBy, $title, $owner) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("INSERT INTO review_assignment (user_id, test_run_id, review_url, created_by, title, owner, is_finished, time_taken, weight) VALUES (:u, :t, :r, :x, :i, :o, 0, 0, 0)");

        $handler->bindValue(':t', $testRunId);
        $handler->bindValue(':u', $userId);
        $handler->bindValue(':r', $reviewUrl);
        $handler->bindValue(':x', $createdBy);
        $handler->bindValue(':i', $title);
        $handler->bindValue(':o', $owner);

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }

        Mediator::emit("reviewAssignmentCreated", DB_DAO::getDB()->lastInsertId());
        return true;
    }

    /**
     * Returns only basic information about assignment
     * @param int $id assignment ID
     * @return ReviewAssignment|null Description
     */
    public function getBasicAssignment($id) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("SELECT u.username as uusername, rn.id as rnid, rn.title as rntitle, rn.end as rnend, a.id as aid, a.review_url, a.owner as aowner, a.title as atitle FROM review_assignment a, user u, test_run rn WHERE a.id=:id AND a.test_run_id=rn.id AND a.user_id=u.id");
        $handler->bindParam(":id", $id);
        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }

        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            $tr = new ReviewAssignment();
            $tr->testRunTitle = $row["rntitle"];
            $tr->testRunId = intval($row["rnid"]);
            $tr->id = intval($row["aid"]);
            $tr->deadline = $row["rnend"];
            $tr->username = $row["uusername"];
            $tr->reviewUrl = $row["reviewUrl"];
            $tr->title = $row["atitle"];
            $tr->owner = $row["aowner"];
            return $tr;
        }
        return null;
    }

    public function countAssignmentsForRun($testRunId) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("SELECT count(a.id) as aid FROM review_assignment a WHERE a.test_run_id=:id");
        $handler->bindParam(":id", $testRunId);
        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }

        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            return intval($row["aid"]);
        }
        return 0;
    }

    public function getAssignments($testRunId) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("SELECT u.username,u.email, a.review_url,a.is_finished as afi, a.title as atitle,a.time_taken as ataken, a.weight as aweight, a.owner as aowner, u.id AS uid, a.last_updated, a.started, u.first_name, u.last_name, a.created_by, a.id as aid FROM review_assignment a, user u WHERE a.test_run_id=:id AND a.user_id=u.id GROUP BY a.id ");
        $handler->bindParam(":id", $testRunId);
        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }
        $results = array();
        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            $r = new ReviewAssignment();
            $r->createdBy = intval($row["created_by"]);
            $r->setEmail($row["email"]);
            $r->title = $row["atitle"];
            $r->owner = substr($row["aowner"], 0, strpos($row["aowner"], "@"));
            $r->setLastUpdated($row["last_updated"]);
            $r->setStarted($row["started"]);
            $r->username = $row["username"];
            $r->userDisplayName = $row["first_name"] . " " . $row["last_name"];
            $r->reviewUrl = $row["review_url"];
            $r->userId = intval($row["uid"]);
            $r->isFinished = $row["afi"] === "0" ? false : true;
            $r->id = intval($row["aid"]);
            $r->info = ($r->isFinished) ? "finished" : "pending";
            $r->weight = intval($row["aweight"], 10);
            $r->timeTaken = intval($row["ataken"], 10);
            array_push($results, $r);
        }
        return $results;
    }

    public function getAssignees($testRunId) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("SELECT DISTINCT u.username, u.email_notifications,u.email FROM review_assignment t, user u WHERE u.id=t.user_id AND t.test_run_id=:tid");
        $handler->bindParam(":tid", $testRunId);

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }
        $data = array();
        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            $u = new User($row['username']);
            $u->email = $row['email'];
            $u->emailNotifications = (intval($row['email_notifications']) === 1 ? true : false);
            $data[$row['username']] = $u;
        }
        return $data;
    }

    /**
     * Returns assoc array with keys userId, specificationId, createdBy and values are proper values or -1 in case no record has been found
     * @param int $assignmentId
     * @return array
     */
    public function getAssignmentInfo($assignmentId) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("SELECT user_id, created_by FROM review_assignment WHERE id=:id");
        $handler->bindParam(':id', $assignmentId);

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }
        $data = array("userId" => -1, "specificationId" => -1, "createdBy" => -1);
        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            $data["userId"] = intval($row["user_id"]);
            $data["createdBy"] = intval($row["created_by"]);
        }
        return $data;
    }

    public function deleteAssignment($assignmentId) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("DELETE FROM review_assignment WHERE id=:id");
        $handler->bindParam(':id', $assignmentId);

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }
        return true;
    }

    public function deleteComments($assignmentId) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("DELETE FROM review_comments WHERE review_assignment_id=:id");
        $handler->bindParam(':id', $assignmentId);

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }
        return true;
    }

    public function getAssignment($assignmentId, $escapeEmail = true) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("SELECT t.id as tid, t.title as ttitle, u.username, a.owner as aowner, a.title as atitle, a.review_url, u.id AS uid, a.last_updated, a.started, u.first_name, u.last_name, a.created_by, a.id as aid FROM review_assignment a, user u, test_run t WHERE a.test_run_id=t.id AND a.id=:id AND a.user_id=u.id");
        $handler->bindParam(":id", $assignmentId);
        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }
        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            $r = new ReviewAssignment();
            $r->createdBy = intval($row["created_by"]);
            $r->title = $row["atitle"];
            $r->owner = $escapeEmail ? substr($row["aowner"], 0, strpos($row["aowner"], "@")) : $row["aowner"];
            $r->setLastUpdated($row["last_updated"]);
            $r->setStarted($row["started"]);
            $r->username = $row["username"];
            $r->userDisplayName = $row["first_name"] . " " . $row["last_name"];
            $r->reviewUrl = $row["review_url"];
            $r->userId = intval($row["uid"]);
            $r->id = intval($row["aid"]);
            $r->testRunId = intval($row["tid"]);
            $r->testRunTitle = $row["ttitle"];
            $r->info = strlen($r->lastUpdated) > 0 ? "finished" : "pending";
            return $r;
        }
        return null;
    }

    public function getComments($assignmentId) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("SELECT u.username, u.first_name, u.last_name, c.elements, c.text, c.id as cid FROM review_assignment a, review_comments c, user u WHERE c.review_assignment_id=:id AND a.user_id=u.id AND c.review_assignment_id=a.id");
        $handler->bindParam(":id", $assignmentId);
        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }
        $comments = array();
        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            array_push($comments, new ReviewComment($row["username"], $row["first_name"] . " " . $row["last_name"], $row["text"], $row["cid"], explode(";", $row["elements"])));
        }
        return $comments;
    }

    public function setLastUpdated($localTime, $assignmentId) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("UPDATE review_assignment a SET a.last_updated=:l WHERE a.id=:id");
        $handler->bindParam(":id", $assignmentId);
        $handler->bindParam(":l", $localTime);
        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }
    }

    public function isRequestUpToDate($assignmentId, $requestedTimestamp) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("SELECT id FROM review_assignment WHERE id=:id AND (last_updated<:da OR last_updated IS NULL)");
        $handler->bindValue(":id", $assignmentId);
        $handler->bindValue(":da", $requestedTimestamp);
        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }

        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            return true; // existing record is older
        }
        return false;
    }

    public function checkUserIsAssigned($id, $username) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("SELECT a.user_id FROM review_assignment a, user u WHERE a.user_id=u.id AND u.username=:u AND a.id=:id");
        $handler->bindParam(':id', $id);
        $handler->bindParam(':u', $username);

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }

        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            return true;
        }
        return false;
    }

    public function getRunIdByAssignmentId($assignmentId) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("SELECT test_run_id FROM review_assignment WHERE id=:id");
        $handler->bindValue(":id", $assignmentId);
        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }

        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            return intval($row["test_run_id"]);
        }
        return -1;
    }

    public function insertComments($comments, $assignmentId) {
        DB_DAO::connectDatabase();
        if (count($comments) < 1) {
            return;
        }
        $baseSql = "INSERT INTO review_comments (review_assignment_id, text, elements) VALUES ";

        for ($i = 0, $max = count($comments); $i < $max; $i++) {
            $key = $i . time();
            $baseSql = $baseSql . "(:" . $key . "aid,:" . $key . "text,:" . $key . "elements),";
            $comments[$i]->setHash($key);
        }
        $baseSql = substr($baseSql, 0, strlen($baseSql) - 1);
        $handler = DB_DAO::getDB()->prepare($baseSql);

        for ($i = 0, $max = count($comments); $i < $max; $i++) {
            $key = $comments[$i]->getHash();
            $handler->bindValue(':' . $key . "aid", $assignmentId);
            $handler->bindValue(':' . $key . "text", $comments[$i]->text);

            $el = "";
            for ($j = 0; $j < count($comments[$i]->elements); $j++) {
                $el = $el . $comments[$i]->elements[$j] . ";";
            }
            $el = substr($el, 0, strlen($el) - 1);
            $handler->bindValue(':' . $key . "elements", $el);
        }

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }
    }

    public function updateStatus($assignmentId, $localTime, $isFinished, $timeTaken, $pageValue) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("UPDATE review_assignment a SET a.time_taken=a.time_taken+:t, a.last_updated=:l, a.is_finished=:f, a.weight=:p WHERE a.id=:id");
        $handler->bindParam(":id", $assignmentId);
        $handler->bindParam(":l", $localTime);
        $handler->bindParam(":t", $timeTaken);
        $handler->bindParam(":p", $pageValue);
        $finished = $isFinished ? 1 : 0;
        $handler->bindParam(":f", $finished);
        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }
    }

    /**
     * 
     * @param int $time time in minutes
     */
    public function setTimeTaken($assignmentId, $time) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("UPDATE review_assignment a SET a.time_taken=:f WHERE a.id=:id");
        $handler->bindParam(":id", $assignmentId);
        $handler->bindParam(":f", $time);
        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }
    }

    public static function getCommentCounts($cond) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("SELECT count(rc.id) as c, ra.id as id FROM review_comments rc, review_assignment ra WHERE " . $cond . " AND rc.review_assignment_id=ra.id GROUP BY ra.id");
        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }
        $result = array();
        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            $result["id" . $row["id"]] = intval($row["c"]);
        }
        return $result;
    }

    public function getAssignmentsInPeriod($testRunId, $from, $to) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("SELECT u.username, a.review_url,a.is_finished as afi, a.title as atitle,a.time_taken as ataken, a.weight as aweight, a.owner as aowner, u.id AS uid, a.last_updated, a.started, u.first_name, u.last_name, a.created_by, a.id as aid FROM review_assignment a, user u WHERE a.test_run_id=:id AND a.user_id=u.id AND a.last_updated<=:t AND a.last_updated>=:f GROUP BY a.id ");
        $handler->bindParam(":id", $testRunId);
        $handler->bindParam(":t", $to);
        $handler->bindParam(":f", $from);
        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }
        $results = array();
        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            $r = new ReviewAssignment();
            $r->createdBy = intval($row["created_by"]);
            $r->title = $row["atitle"];
            $r->owner = substr($row["aowner"], 0, strpos($row["aowner"], "@"));
            $r->setLastUpdated($row["last_updated"]);
            $r->setStarted($row["started"]);
            $r->username = $row["username"];
            $r->userDisplayName = $row["first_name"] . " " . $row["last_name"];
            $r->reviewUrl = $row["review_url"];
            $r->userId = intval($row["uid"]);
            $r->isFinished = $row["afi"] === "0" ? false : true;
            $r->id = intval($row["aid"]);
            $r->info = ($r->isFinished) ? "finished" : "pending";
            $r->weight = intval($row["aweight"], 10);
            $r->timeTaken = intval($row["ataken"], 10);
            array_push($results, $r);
        }
        return $results;
    }

    /**
     * 
     * @param \Synergy\Model\Review\ReviewPage $reviewPage
     */
    public function createReviewPage($reviewPage) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("INSERT INTO review_pages (url, title, owner) VALUES (:u, :t, :o)");
        $handler->bindParam(":u", $reviewPage->url);
        $handler->bindParam(":t", $reviewPage->title);
        $handler->bindParam(":o", $reviewPage->owner);
        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }
        return true;
    }

    //put your code here
}
