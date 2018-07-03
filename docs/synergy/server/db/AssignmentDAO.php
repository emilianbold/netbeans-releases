<?php

namespace Synergy\DB;

use PDO;
use Synergy\Misc\Util;
use Synergy\Model\TestAssignment;

/**
 * Description of AssignmentDAO
 *
 * @author vriha
 */
class AssignmentDAO {

    /**
     * Returns assoc array with keys userId, specificationId, createdBy and values are proper values or -1 in case no record has been found
     * @param int $assignmentId
     * @return array
     */
    public function getAssignentInfo($assignmentId) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("SELECT user_id, specification_id, created_by FROM test_assignement WHERE id=:id");
        $handler->bindParam(':id', $assignmentId);

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }
        $data = array("userId" => -1, "specificationId" => -1, "createdBy" => -1);
        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            $data["userId"] = intval($row["user_id"]);
            $data["specificationId"] = intval($row["specification_id"]);
            $data["createdBy"] = intval($row["created_by"]);
        }
        return $data;
    }

    public function findSimilar($assigneeUsername, $platform, $labelId, $specificationId, $testRunId) {
        DB_DAO::connectDatabase();
        if (intval($labelId) > -1) {
            $handler = DB_DAO::getDB()->prepare("SELECT t.id FROM test_assignement t, user u WHERE u.username=:uu AND t.test_run_id=:tri AND t.specification_id=:si AND t.user_id=u.id AND t.platform_id=:pi AND t.keyword_id=:ki");
            $handler->bindParam(':ki', $labelId);
        } else {
            $handler = DB_DAO::getDB()->prepare("SELECT t.id FROM test_assignement t, user u WHERE u.username=:uu AND t.test_run_id=:tri AND t.specification_id=:si AND t.user_id=u.id AND t.platform_id=:pi");
        }
        $handler->bindParam(':uu', $assigneeUsername);
        $handler->bindParam(':pi', $platform);
        $handler->bindParam(':si', $specificationId);
        $handler->bindParam(':tri', $testRunId);

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }

        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            return intval($row["id"]);
        }
        return null;
    }

    public function updateIssues($issuesString, $passedCount, $failedCount, $assignmentId) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("UPDATE test_assignement SET issues=:is, passed_cases=:p, failed_cases=:f  WHERE id=:id");
        $handler->bindParam(':id', $assignmentId);
        $handler->bindParam(':p', $passedCount);
        $handler->bindParam(':f', $failedCount);
        $handler->bindParam(':is', Util::purifyHTML($issuesString));

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }
        return true;
    }

    public function getAssignment($assignmentId) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("SELECT GROUP_CONCAT( of.tribe_id SEPARATOR  ';' ) AS tribes_id, u.id AS uid, u.username, a.last_updated, a.started, a.issues, a.time_taken, u.first_name,a.keyword_id, u.last_name, p.name, a.state, a.number_of_cases, a.number_of_completed_cases,a.failed_cases,a.passed_cases, a.skipped_cases, k.keyword, a.specification_id,a.id as aid, sp.title as sptitle, a.created_by FROM (test_assignement a, user u, platform p, specification sp) LEFT JOIN keyword k ON k.id=a.keyword_id LEFT JOIN user_is_member_of of ON of.user_id = u.id WHERE a.id=:id AND a.specification_id=sp.id AND p.id=a.platform_id AND a.user_id=u.id GROUP BY a.id ");

        $handler->bindValue(":id", $assignmentId);
        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }

        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            $tr = new TestAssignment($row["username"], $row["name"], $assignmentId, $row["keyword"], $row["number_of_cases"]);
            $tr->completed = intval($row["number_of_completed_cases"]);
            $tr->createdBy = intval($row["created_by"]);
            if (strlen($row["keyword"]) < 1) {
                $tr->labelId = -1;
            } else {
                $tr->labelId = intval($row["keyword_id"]);
            }
            $tr->setLastUpdated($row["last_updated"]);
            $tr->setStarted($row["started"]);
            $tr->userDisplayName = $row["first_name"] . " " . $row["last_name"];
            $tr->failed = intval($row["failed_cases"]);
            $tr->passed = intval($row["passed_cases"]);
            $tr->skipped = intval($row["skipped_cases"]);
            $tr->userId = intval($row["uid"]);
            $tr->state = $row["state"];
            $tr->id = intval($row["aid"]);
            $tr->specification = $row["sptitle"];
            $tr->specificationId = intval($row["specification_id"]);

            if ($tr->completed === $tr->total) {
                if ($tr->failed > 0) {
                    $tr->info = "warning";
                } else {
                    $tr->info = "finished";
                }
            } else {
                if ($tr->completed > 0) {
                    $tr->info = "unfinished";
                } else {
                    $tr->info = "pending";
                }
            }
            $tr->timeToComplete = intval($row["time_taken"]);
            $tr->issues = explode(";", $row["issues"]);
            if (count($tr->issues) === 1 && strlen($tr->issues[0]) === 0) {
                $tr->issues = array();
            }

            $tr->setTribesId($row["tribes_id"]);
            return $tr;
        }
        return null;
    }

}
