<?php

namespace Synergy\DB;

use DateTime;
use PDO;
use Synergy\Controller\Mediator;
use Synergy\Misc\Util;
use Synergy\Model\AssignmentProgress;
use Synergy\Model\AssignmentDuration;
use Synergy\Model\RunAttachment;
use Synergy\Model\TestAssignment;
use Synergy\Model\TestRun;
use Synergy\Model\TestRunList;
use Synergy\Model\User;
use Synergy\App\Synergy;
use Synergy\Model\Project\Project;

/**
 * Description of RunDAO
 *
 * @author vriha
 */
class RunDAO {

    /**
     * Deletes all test assignments for given specification
     * @param type $id
     */
    public function deleteAssignmentsForSpecification($id) {

        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("DELETE FROM test_assignement WHERE specification_id=:id ");
        $handler->bindParam(':id', $id);

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }
    }

    /**
     * Deletes all test assignments for given platform
     * @param type $id
     */
    public function deleteAssignmentsForPlatform($id) {

        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("DELETE FROM test_assignement WHERE platform_id=:id ");
        $handler->bindParam(':id', $id);

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }
    }

    /**
     * Deletes all test assignments for test run
     * @param type $id
     */
    public function deleteAssignmentsForTestRun($id) {

        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("DELETE FROM test_assignement WHERE test_run_id=:id ");
        $handler->bindParam(':id', $id);

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }
        return true;
    }

    /**
     * Removes given test assignment
     * @param type $id
     * @return boolean
     */
    public function deleteAssignment($id) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("DELETE FROM test_assignement WHERE id=:id ");
        $handler->bindParam(':id', $id);

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }
        Mediator::emit("assignmentDeleted", $id);
        return true;
    }

    /**
     * Returns all test runs
     * @param type $page
     */
    public function getRuns($page) {
        $start = intval((($page - 1) * RUNS_PAGE));
        $stop = intval((RUNS_PAGE));
// sum total cases, cases finished, members
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("SELECT count(a.user_id) as members, sum(a.number_of_cases) as cases, sum(a.number_of_completed_cases) as completed, r.title, r.start, r.end, r.id, r.is_active, project.name as pname FROM test_run r LEFT JOIN test_assignement a ON r.id=a.test_run_id LEFT JOIN project ON project.id = r.project_id GROUP BY r.id ORDER BY r.start DESC LIMIT " . $start . "," . $stop);

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }
        $runs = array();
        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            $tr = new TestRun($row['title'], $row['id'], $row['start'], $row['end']);
            $tr->membersCount = intval($row['members']);
            $tr->total = intval($row['cases']);
            $tr->completed = intval($row['completed']);
            $tr->isActive = intval($row["is_active"]);
            $tr->projectName = $row["pname"];
            if ($tr->completed === $tr->total) {
                $tr->status = "finished";
            }
            if ($tr->total > $tr->completed) {
                date_default_timezone_set('UTC');
                $today = strtotime(date("Y-m-d H:i:s"));
                $end = strtotime($row['end']);
                if ($today > $end)
                    $tr->status = "unfinished";
                else
                    $tr->status = "pending";
            }

            array_push($runs, $tr);
        }

        $result = new TestRunList($page, $runs);
        if (count($result->testRuns) < RUNS_PAGE) {
            $result->nextUrl = "";
        }

        return $result;
    }

    /**
     * Returns list of latest test runs (maximum number of runs is $limit) where end date is >= today, ordered by start date DESC
     * @param int $limit
     * @return \TestRunList
     */
    public function getLatestRuns($limit) {

        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("SELECT count(a.user_id) as members, sum(a.number_of_cases) as cases, sum(a.number_of_completed_cases) as completed, r.title, r.start, r.end, r.id, project.name as pname FROM test_run r LEFT JOIN test_assignement a ON r.id=a.test_run_id LEFT JOIN project ON project.id = r.project_id WHERE r.end>=NOW() GROUP BY r.id ORDER BY r.start DESC LIMIT " . intval($limit));

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }
        $runs = array();
        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            $tr = new TestRun($row['title'], $row['id'], $row['start'], $row['end']);
            $tr->membersCount = intval($row['members']);
            $tr->total = intval($row['cases']);
            $tr->completed = intval($row['completed']);
            $tr->projectName = $row["pname"];
            if ($tr->completed === $tr->total) {
                $tr->status = "finished";
            }
            if ($tr->total > $tr->completed) {
                date_default_timezone_set('UTC');
                $today = strtotime(date("Y-m-d H:i:s"));
                $end = strtotime($row['end']);
                if ($today > $end)
                    $tr->status = "unfinished";
                else
                    $tr->status = "pending";
            }

            array_push($runs, $tr);
        }

        $result = new TestRunList($limit, $runs);
        if (count($result->testRuns) !== RUNS_PAGE) {
            $result->nextUrl = "";
        }

        return $result;
    }

    /**
     * Creates new test run
     * @param type $title
     * @param type $desc
     * @param type $start
     * @param type $stop
     * @return int ID of new test run
     */
    public function createRun($title, $desc, $start, $stop, $notifications, $projectId) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("INSERT INTO test_run (title, description, start, end, notifications_deadline,project_id) VALUES (:title, :desc, :s, :e, :n,:p)");
        $handler->bindValue(':title', Util::purifyHTML($title));
        $handler->bindValue(':n', $notifications);
        $handler->bindValue(':p', $projectId);
        $handler->bindValue(':desc', Util::purifyHTML($desc));
        date_default_timezone_set('UTC');
        $handler->bindValue(':s', date("Y-m-d H:i:s", strtotime($start)));
        $handler->bindValue(':e', date("Y-m-d H:i:s", strtotime($stop)));

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }
        $newid = DB_DAO::getDB()->lastInsertId();
        Mediator::emit("testRunCreated", $newid);

        return $newid;
    }

    /**
     * Returns basic test run info
     * @param \TestRun|null $id
     */
    public function getRun($id) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("SELECT title, description, start, end, is_active, notifications_deadline, p.bug_tracking_system, p.name, p.id as pid FROM test_run LEFT JOIN project p ON p.id=test_run.project_id WHERE test_run.id=:id");
        $handler->bindParam(":id", $id);
        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }

        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            $tr = new TestRun($row["title"], $id, $row["start"], $row["end"]);
            $tr->projectName = $row["name"];
            $tr->id = intval($id, 10);
            $tr->projectId = intval($row["pid"], 10);
            $tr->setBugTrackingSystem($row["bug_tracking_system"]);
            $tr->notifications = intval($row["notifications_deadline"]);
            $tr->desc = $row["description"];
            $tr->isActive = intval($row["is_active"]);
            return $tr;
        }
        return null;
    }

    /**
     * Updates basic test run information
     * @param type $id
     * @param type $title
     * @param type $desc
     * @param type $start
     * @param type $stop
     * @return boolean true if successful
     */
    public function editRun($id, $title, $desc, $start, $stop, $notifications, $projectId) {
        DB_DAO::connectDatabase();
        date_default_timezone_set('UTC');
        $handler = DB_DAO::getDB()->prepare("UPDATE test_run SET title=:t, description=:d, start=:s, end=:e, notifications_deadline=:n, project_id=:p WHERE id=:id");
        $handler->bindValue(':id', ($id));
        $handler->bindValue(':n', $notifications);
        $handler->bindValue(':p', $projectId);
        $handler->bindValue(':t', Util::purifyHTML($title));
        $handler->bindValue(':d', Util::purifyHTML($desc));
        $handler->bindValue(':s', date("Y-m-d H:i:s", strtotime($start)));
        $handler->bindValue(':e', date("Y-m-d H:i:s", strtotime($stop)));

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }
        Mediator::emit("testRunEdited", $id);
        return true;
    }

    /**
     * Removes given test run and all assignments
     * @param type $id
     * @return boolean true if successful
     */
    public function deleteRun($id) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("DELETE FROM test_run WHERE id=:id ");
        $handler->bindParam(':id', $id);

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }
        Mediator::emit("testRunDeleted", $id);

        return true;
    }

    /**
     * Returns array of assignments for given test run
     * @param type $id
     */
    public function getAssignments($id, $withIssues) {
        DB_DAO::connectDatabase();
        if ($withIssues) {
            $handler = DB_DAO::getDB()->prepare("SELECT GROUP_CONCAT( of.tribe_id SEPARATOR  ';' ) AS tribes_id, u.id AS uid, u.username, a.last_updated, a.started, a.issues, a.time_taken, u.first_name,a.keyword_id, u.last_name, p.name, a.state, a.number_of_cases, a.number_of_completed_cases,a.failed_cases,a.passed_cases, a.skipped_cases, k.keyword, a.specification_id,a.id as aid, sp.title as sptitle, a.created_by FROM (test_assignement a, user u, platform p, specification sp) LEFT JOIN keyword k ON k.id=a.keyword_id LEFT JOIN user_is_member_of of ON of.user_id = u.id WHERE a.test_run_id=:id AND a.specification_id=sp.id AND p.id=a.platform_id AND a.user_id=u.id GROUP BY a.id ");
        } else {
            $handler = DB_DAO::getDB()->prepare("SELECT GROUP_CONCAT( of.tribe_id SEPARATOR  ';' ) AS tribes_id, u.username, u.id AS uid, a.last_updated, a.started, u.first_name,a.keyword_id, u.last_name, p.name, a.state, a.number_of_cases, a.number_of_completed_cases,a.failed_cases,a.passed_cases, a.skipped_cases, k.keyword, a.specification_id,a.id as aid, sp.title as sptitle, a.created_by FROM (test_assignement a, user u, platform p, specification sp) LEFT JOIN keyword k ON k.id=a.keyword_id LEFT JOIN user_is_member_of of ON of.user_id = u.id WHERE a.test_run_id=:id AND a.specification_id=sp.id AND p.id=a.platform_id AND a.user_id=u.id GROUP BY a.id ");
        }

        $handler->bindValue(":id", $id);
        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }

        $assignments = array();
        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            $tr = new TestAssignment($row["username"], $row["name"], $id, $row["keyword"], $row["number_of_cases"]);
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
            if ($withIssues) {
                $tr->timeToComplete = intval($row["time_taken"]);
                $tr->issues = explode(";", $row["issues"]);
                if (count($tr->issues) === 1 && strlen($tr->issues[0]) === 0) {
                    $tr->issues = array();
                }
            }
            $tr->setTribesId($row["tribes_id"]);
            array_push($assignments, $tr);
        }
        return $assignments;
    }

    /**
     * Returns array of assignments for given test run
     * @param type $id
     */
    public function getAssignmentsInPeriod($id, $from, $to) {
        date_default_timezone_set('UTC');
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("SELECT u.id AS uid, u.username, a.last_updated, a.started, a.issues, a.time_taken, u.first_name,a.keyword_id, u.last_name, p.name, a.state, a.number_of_cases, a.number_of_completed_cases,a.failed_cases,a.passed_cases, a.skipped_cases, k.keyword, a.specification_id,a.id as aid, sp.title as sptitle, a.created_by FROM (test_assignement a, user u, platform p, specification sp) LEFT JOIN keyword k ON k.id=a.keyword_id WHERE a.test_run_id=:id AND a.specification_id=sp.id AND p.id=a.platform_id AND a.user_id=u.id AND a.last_updated<=:t AND a.last_updated>=:f GROUP BY a.id ");
        $handler->bindValue(":id", $id);
        $handler->bindValue(":t", $to);
        $handler->bindValue(":f", $from);
        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }

        $assignments = array();
        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            $tr = new TestAssignment($row["username"], $row["name"], $id, $row["keyword"], $row["number_of_cases"]);
            $tr->completed = $row["number_of_completed_cases"];
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

            $tr->setTribesId($row["tribes_id"]);
            array_push($assignments, $tr);
        }
        return $assignments;
    }

    /**
     * Returns plain assignment - no progress or test run information included
     * @param type $assignmentId
     * @return \TestAssignment|null
     */
    public function getAssignment($assignmentId) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("SELECT a.last_updated as lup, u.username, p.name, a.state, a.number_of_cases, a.number_of_completed_cases, k.keyword, k.id AS kid, a.specification_id,a.id as aid, sp.title as sptitle, t.id as tid, t.end AS finishedBy FROM (test_assignement a, user u, platform p, specification sp, test_run t) LEFT JOIN keyword k ON k.id=a.keyword_id WHERE a.id=:id AND a.test_run_id=t.id AND a.specification_id=sp.id AND p.id=a.platform_id AND a.user_id=u.id GROUP BY a.id ");
        $handler->bindValue(":id", $assignmentId);
        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }

        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            $tr = new TestAssignment($row['username'], $row['name'], $row['tid'], $row['keyword'], $row['number_of_cases']);
            $tr->completed = intval($row['number_of_completed_cases']);
            $tr->state = $row['state'];
            $tr->id = intval($row['aid']);
            $tr->labelId = (!is_null($row["kid"]) && strlen($row["kid"]) > 0) ? intval($row["kid"]) : -1;
            $tr->specification = $row['sptitle'];

            if (is_null($row["lup"])) {
                date_default_timezone_set('UTC');
                $tr->lastUpdated = date('Y-m-d H:i:s');
            } else {
                $tr->lastUpdated = $row["lup"];
            }
            $tr->specificationId = intval($row['specification_id']);
            $tr->deadline = $row["finishedBy"];
            if ($tr->completed === $tr->total) {
                $tr->info = "finished";
            } else {
                $tr->info = "pending";
            }

            return $tr;
        }
        return null;
    }

    /**
     * Returns array of RunAttachment
     * @param type $id
     */
    public function getAttachments($id) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("SELECT s.id, s.path FROM run_attachement s WHERE s.test_run_id=:id");
        $handler->bindParam(':id', $id);

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }

        $data = array();
        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            array_push($data, new RunAttachment($row['path'], $row['id'], $id));
        }
        return $data;
    }

    /**
     * Creates new assignment
     * @param type $specificationId
     * @param type $platformId
     * @param type $labelId
     * @param type $testRunId
     * @param type $userId
     * @param type $numberOfCases s
     * @return boolean true if successful
     */
    public function createAssignment($specificationId, $platformId, $labelId, $testRunId, $userId, $numberOfCases, $createdBy) {

        DB_DAO::connectDatabase();
        if (intval($labelId) < 1) {
            $handler = DB_DAO::getDB()->prepare("INSERT INTO test_assignement (user_id, platform_id, specification_id, test_run_id, number_of_cases, number_of_completed_cases, state, created_by) VALUES (:u, :p, :s, :t, :c, 0, '', :x)");
        } else {
            $handler = DB_DAO::getDB()->prepare("INSERT INTO test_assignement (user_id, platform_id, specification_id, test_run_id, keyword_id, number_of_cases, number_of_completed_cases, state, created_by) VALUES (:u, :p, :s, :t, :k, :c, 0, '', :x)");
            $handler->bindValue(':k', intval($labelId));
        }
        $handler->bindValue(':s', intval($specificationId));
        $handler->bindValue(':p', intval($platformId));
        $handler->bindValue(':t', intval($testRunId));
        $handler->bindValue(':u', $userId);
        $handler->bindValue(':c', $numberOfCases);
        $handler->bindValue(':x', $createdBy);


        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }

        Mediator::emit("assignmentCreated", DB_DAO::getDB()->lastInsertId());
        return true;
    }

    /**
     * For each assignment, get list of cases ordered by SUITE_TITLE + CASE_TITLE. If these data are not yet in DB, it will be 
     * created (not stored though) and returned to user so he'll get empty/blank progress
     * @param int $id assignment id
     * @return AssignmentProgress $data 
     */
    public function getAssigmentProgress($id, $label = '') {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("SELECT data FROM assignment_progress s WHERE test_assignement_id=:id");
        $handler->bindParam(':id', $id);

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }

        $handler->bindColumn(1, $data, PDO::PARAM_LOB);
        while ($handler->fetch()) {
            return json_decode(gzuncompress($data));
        }

        return null;

// OBSOLETE - but to be sure let it be here until its all fixed
//// so nothing found here :/
//        $assignment = RunDAO::getSkeletonAssignment($id);
//        if (is_null($assignment))
//            return null;
//
//        // no we need to get all test suits and for each suit its cases
//
//        $specification = SpecificationDAO::getSkeletonSpecificationWithSuites($assignment->specificationId);
//        if (is_null($specification)) {
//            return null;
//        }
//
//        foreach ($specification->testSuites as $ts) {
//            $ts->testCases = SuiteDAO::getSkeletonTestCases($ts->id, $label);
//        }
//        return new AssignmentProgress($id, $specification, $assignment->userId);
    }

    /**
     * Returns basic information about assignment
     * @deprecated
     * @param int $id assignment ID
     * @return \TestAssignment|null
     */
    public static function getSkeletonAssignment($id) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("SELECT a.user_id, a.specification_id FROM test_assignement a WHERE a.id=:id");
        $handler->bindParam(':id', $id);

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }

        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            $ta = new TestAssignment('', '', $id, '', 0);
            $ta->specificationId = intval($row['specification_id']);
            $ta->userId = intval($row['user_id']);
            return $ta;
        }
        return null;
    }

    /**
     * Checks if given user is assigned to given test assignment
     * @param type $id
     * @param type $username
     * @return boolean true if user is assigned for given assignment
     */
    public function checkUserIsAssigned($id, $username) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("SELECT a.user_id FROM test_assignement a, user u WHERE a.user_id=u.id AND u.username=:u AND a.id=:id");
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

    /**
     * Saves assignment progress to DB
     * @param type $json data
     * @param type $id assignment ID
     */
    public function saveAssignmentProgress($json, $id) {

        DB_DAO::connectDatabase();
        $handler2 = DB_DAO::getDB()->prepare("INSERT INTO assignment_progress (`data`, test_assignement_id) VALUES (:d,:id)");
        $handler2->bindParam(':id', $id);
        $c = gzcompress($json);
        $handler2->bindParam(':d', $c, PDO::PARAM_LOB);
        if (!$handler2->execute()) {
            DB_DAO::throwDbError($handler2->errorInfo());
        }
    }

    /**
     * Updates cases count based on data retrieved from finished test assignment
     * @param type $id assignment ID
     * @param type $numberOfCases
     * @param type $numberOfCasesCompleted
     * @param type $failedCases
     * @param type $skippedCases
     * @param type $passedCases
     */
    public function updateAssignment($id, $numberOfCases, $numberOfCasesCompleted, $failedCases, $skippedCases, $passedCases, $totalTime = 0) {
        DB_DAO::connectDatabase();
        if ($numberOfCases < 0) {
            $handler2 = DB_DAO::getDB()->prepare("UPDATE test_assignement SET last_updated=:lt, number_of_completed_cases=:ncom, failed_cases=:fc, passed_cases=:pc, skipped_cases=:sc, time_taken=:ti WHERE id=:id");
        } else {
            $handler2 = DB_DAO::getDB()->prepare("UPDATE test_assignement SET last_updated=:lt, number_of_cases=:noc, number_of_completed_cases=:ncom, failed_cases=:fc, passed_cases=:pc, skipped_cases=:sc, time_taken=:ti WHERE id=:id");
            $handler2->bindParam(':noc', $numberOfCases);
        }
        Synergy::log("[" . Synergy::getSessionProvider()->getUsername() . "] updated test assignment " . $id . "; completed cases: " . $numberOfCasesCompleted . "; time taken " . $totalTime . "; total cases " . $numberOfCases);
        date_default_timezone_set('UTC');
        $localTime = date('Y-m-d H:i:s');
        $handler2->bindParam(':lt', $localTime);
        $handler2->bindParam(':id', $id);
        $handler2->bindParam(':ncom', $numberOfCasesCompleted);
        $handler2->bindParam(':fc', $failedCases);
        $handler2->bindParam(':sc', $skippedCases);
        $handler2->bindParam(':pc', $passedCases);
        $handler2->bindParam(':ti', $totalTime);

        if (!$handler2->execute()) {
            DB_DAO::throwDbError($handler2->errorInfo());
        }
    }

    /**
     * Removes assignment progress JSON dump from DB
     * @param type $id
     * @return boolean
     */
    public function deleteAssignmentProgress($id) {

        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("DELETE FROM assignment_progress WHERE test_assignement_id=:id ");
        $handler->bindParam(':id', $id);

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }
        return true;
    }

    /**
     * Returns title of test run for given assignment
     * @param type $assignmentId
     * @return string
     */
    public function getRunTitleForAssignment($assignmentId) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("SELECT t.title FROM test_assignement a, test_run t WHERE a.id=:id AND a.test_run_id=t.id");
        $handler->bindParam(':id', $assignmentId);


        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }

        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            return $row['title'];
        }
        return '';
    }

    /**
     * Returns all users assignments 
     * @param String $username
     * @return TestAssignment[] Description
     */
    public function getAllUsersAssignments($username) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("SELECT p.name, a.state,rn.id as rnid, rn.title as rntitle, a.number_of_cases, a.number_of_completed_cases,a.failed_cases,a.passed_cases, a.skipped_cases, k.keyword, a.specification_id,a.id as aid, sp.title as sptitle FROM (test_assignement a, user u, platform p, specification sp, test_run rn) LEFT JOIN keyword k ON k.id=a.keyword_id WHERE a.test_run_id=rn.id AND a.specification_id=sp.id AND p.id=a.platform_id AND a.user_id=u.id AND u.username=:u GROUP BY a.id ");
        $handler->bindParam(":u", $username);
        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }

        $assignments = array();
        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            $tr = new TestAssignment($username, $row['name'], $row['rnid'], $row['keyword'], $row['number_of_cases']);
            $tr->completed = intval($row['number_of_completed_cases']);
            $tr->testRunTitle = $row['rntitle'];
            $tr->failed = intval($row['failed_cases']);
            $tr->passed = intval($row['passed_cases']);
            $tr->skipped = intval($row['skipped_cases']);
            $tr->state = $row['state'];
            $tr->id = intval($row['aid']);
            $tr->specification = $row['sptitle'];
            $tr->specificationId = intval($row['specification_id']);

            if ($tr->completed === $tr->total) {
                $tr->info = "finished";
            } else {
                $tr->info = "pending";
            }

            array_push($assignments, $tr);
        }
        return $assignments;
    }

    /**
     * Returns all users assignments that are either unfinished (cases total != cases completed) or are still running (test run's end date >= today)
     * @param String $username
     * @return TestAssignment[] Description
     */
    public function getUsersAssignments($username) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("SELECT project.name AS prname,p.name, a.state,rn.id as rnid, rn.title as rntitle, a.number_of_cases, a.number_of_completed_cases,a.failed_cases,a.passed_cases, a.skipped_cases, k.keyword, a.specification_id,a.id as aid, sp.title as sptitle FROM (test_assignement a, user u, platform p, specification sp, test_run rn) LEFT JOIN keyword k ON k.id=a.keyword_id LEFT JOIN project ON project.id = rn.project_id WHERE (rn.end>=NOW() AND a.number_of_cases!=a.number_of_completed_cases) AND a.test_run_id=rn.id AND a.specification_id=sp.id AND p.id=a.platform_id AND a.user_id=u.id AND u.username=:u GROUP BY a.id ");
        $handler->bindParam(":u", $username);
        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }

        $assignments = array();
        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            $tr = new TestAssignment($username, $row['name'], $row['rnid'], $row['keyword'], $row['number_of_cases']);
            $tr->completed = intval($row['number_of_completed_cases']);
            $tr->testRunProjectName = $row["prname"];
            $tr->testRunTitle = $row['rntitle'];
            $tr->failed = intval($row['failed_cases']);
            $tr->passed = intval($row['passed_cases']);
            $tr->skipped = intval($row['skipped_cases']);
            $tr->state = $row['state'];
            $tr->id = intval($row['aid']);
            $tr->specification = $row['sptitle'];
            $tr->specificationId = intval($row['specification_id']);

            if ($tr->completed === $tr->total) {
                $tr->info = "finished";
            } else {
                $tr->info = "pending";
            }

            array_push($assignments, $tr);
        }
        return $assignments;
    }

    /**
     * Returns all test runs with start date greater than or equal $startDate and smaller or equal $stopDate
     * @param DateTime $startDate
     * @param DateTime $stopDate
     * @return TestRun[]
     */
    public function getRunsByDate($startDate, $stopDate) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("SELECT title, id, description, start, end FROM test_run WHERE start>=:startDate OR end<=:stopDate");
        $handler->bindParam(":startDate", $startDate);
        $handler->bindParam(":stopDate", $stopDate);

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }
        $runs = array();
        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            $tr = new TestRun($row['title'], $row['id'], $row['start'], $row['end']);
            $tr->desc = $row['description'];
            array_push($runs, $tr);
        }
        return $runs;
    }

    /**
     * Returns only basic information about assignment
     * @param int $id assignment ID
     * @return TestAssignment|null Description
     */
    public function getBasicAssignment($id) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("SELECT u.username as uusername, p.name, a.state,rn.id as rnid, rn.title as rntitle, rn.end as rnend, k.keyword, a.specification_id,a.id as aid, sp.title as sptitle FROM (test_assignement a, platform p, specification sp, user u, test_run rn) LEFT JOIN keyword k ON k.id=a.keyword_id WHERE a.id=:id AND a.test_run_id=rn.id AND a.specification_id=sp.id AND p.id=a.platform_id AND a.user_id=u.id");
        $handler->bindParam(":id", $id);
        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }

        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            $tr = new TestAssignment($row['uusername'], $row['name'], $row['rnid'], $row['keyword'], -1);
            $tr->testRunTitle = $row['rntitle'];
            $tr->id = intval($row['aid']);
            $tr->deadline = $row['rnend'];
            $tr->specification = $row['sptitle'];
            $tr->specificationId = intval($row['specification_id']);
            return $tr;
        }
        return null;
    }

    /**
     * Adds start timestamp to assignment
     * @param int $assignmentId
     * @param DateTime $startTime
     */
    public function startAssignment($assignmentId, $startTime) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("UPDATE test_assignement SET started=:s WHERE id=:id");
        $handler->bindParam(':id', $assignmentId);
        $handler->bindParam(':s', $startTime);

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }
    }

    public function assignmentAlreadyStarted($assignmentId) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("SELECT started FROM test_assignement WHERE id=:id");
        $handler->bindParam(":id", $assignmentId);
        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }

        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            if (isset($row['started']) && !is_null($row['started'])) {
                return true;
            }
        }
        return false;
    }

    public function getAssignees($testRunId) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("SELECT DISTINCT u.username, u.email_notifications,u.email FROM test_assignement t, user u WHERE u.id=t.user_id AND t.test_run_id=:tid");
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

    public function deleteAssignmentProgressForPlatform($id) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("DELETE a.* FROM assignment_progress a, test_assignement b WHERE a.test_assignement_id=b.id AND b.platform_id=:id ");
        $handler->bindParam(':id', $id);

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }
        return true;
    }

    public function getIncompleteAssignmIdBySpecId($specificationId) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("SELECT ta.id FROM test_assignement ta, assignment_progress ap WHERE ta.specification_id=:id AND ta.number_of_cases > ta.number_of_completed_cases AND ta.id=ap.test_assignement_id"); // not yet completed
        $handler->bindParam(":id", $specificationId);

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }
        $data = array();
        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            array_push($data, intval($row['id']));
        }
        return $data;
    }

    public function getAssignmentsBySpecification($id) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("SELECT a.id FROM test_assignement a WHERE a.specification_id=:id");
        $handler->bindValue(":id", $id);
        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }

        $assignments = array();
        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            $t = new TestAssignment('', '', -1, '', '');
            $t->id = intval($row['id']);
            array_push($assignments, $t);
        }
        return $assignments;
    }

    public function setActive($testRunId, $isFrozen) {
        DB_DAO::connectDatabase();
        if ($isFrozen) {
            $handler = DB_DAO::getDB()->prepare("UPDATE test_run SET is_active=0 WHERE id=:id");
        } else {
            $handler = DB_DAO::getDB()->prepare("UPDATE test_run SET is_active=1 WHERE id=:id");
        }
        $handler->bindParam(':id', $testRunId);

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }
        return true;
    }

    public static function runIsActive($testRunId) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("SELECT is_active FROM test_run WHERE id=:id");
        $handler->bindValue(":id", $testRunId);
        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }

        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            return (intval($row["is_active"]) === 1) ? true : false;
        }
        return false;
    }

    public function getRunIdByAssignmentId($assignmentId) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("SELECT test_run_id FROM test_assignement WHERE id=:id");
        $handler->bindValue(":id", $assignmentId);
        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }

        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            return intval($row["test_run_id"]);
        }
        return -1;
    }

    public function deleteAssignmentProgressForRun($testRunId) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("DELETE a.* FROM assignment_progress a, test_assignement b WHERE a.test_assignement_id=b.id AND b.test_run_id=:id ");
        $handler->bindParam(':id', $testRunId);

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }
        return true;
    }

    public function addIssues($id, $issuesString) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("UPDATE test_assignement SET issues=:is WHERE id=:id");
        $handler->bindParam(':id', $id);
        $handler->bindParam(':is', Util::purifyHTML($issuesString));

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }
        return true;
    }

    public function getRunTitle($testRunId) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("SELECT title FROM test_run WHERE id=:id");
        $handler->bindValue(":id", $testRunId);
        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }

        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            return $row["title"];
        }
        return "";
    }

    public function isRequestUpToDate($assignmentId, $requestedTimestamp) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("SELECT id FROM test_assignement WHERE id=:id AND (last_updated<:da OR last_updated IS NULL)");
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

    public function getProject($testRunId) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("SELECT project_id, p.name as pname FROM test_run, project p WHERE p.id=project_id AND test_run.id=:id");
        $handler->bindValue(":id", $testRunId);
        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }

        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            return new Project(intval($row["project_id"], 10), $row["pname"]);
        }
        return null;
    }

    public function insertBlob($assignmentId, $runId, $blob) {
        DB_DAO::connectDatabase();
        date_default_timezone_set('UTC');
        $handler = DB_DAO::getDB()->prepare("INSERT INTO assignment_blob (assignment_id, test_run_id, data, created) VALUES (:a, :r, :d, :c)");
        $handler->bindValue(':a', $assignmentId);
        $handler->bindValue(':r', $runId);
        $handler->bindValue(':d', $blob);
        $localTime = date('Y-m-d H:i:s');
        $handler->bindValue(':c', $localTime);

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }
        $newid = DB_DAO::getDB()->lastInsertId();

        return $newid;
    }

    public function removeBlob($assignmentId) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("DELETE FROM assignment_blob WHERE assignment_id=:id ");
        $handler->bindParam(':id', $assignmentId);

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }
    }

    public function getBlobs($runId) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("SELECT data FROM assignment_blob WHERE test_run_id=:id ");
        $handler->bindParam(':id', $runId);

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }
        $d = array();
        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            array_push($d, json_decode($row["data"]));
        }

        return $d;
    }

    public function getDurations($runId) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("SELECT time_taken, id FROM test_assignement WHERE test_run_id=:id ");
        $handler->bindParam(':id', $runId);

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }
        $d = array();
        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            array_push($d, new AssignmentDuration($row["id"], $row["time_taken"]));
        }

        return $d;
    }

}

?>