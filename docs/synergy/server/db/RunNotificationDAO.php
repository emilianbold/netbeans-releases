<?php

namespace Synergy\DB;

use PDO;
use Synergy\Model\TestAssignment;
use Synergy\Model\Exception\GeneralException;
/**
 * Description of RunNotificationDAO
 *
 * @author vriha
 */
class RunNotificationDAO {

    /**
     * Returns all assignments that are unfinished (cases total != cases completed) for given test run
     * @param String $testRunId
     * @return TestAssignment[] Description
     */
    public function getUnfihisedAssignments($testRunId) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("SELECT u.username AS uname,p.name as pname, u.email, rn.title as rntitle,a.id as aid, sp.title as sptitle FROM test_assignement a, user u, specification sp, test_run rn, platform p WHERE a.platform_id=p.id AND a.number_of_cases!=a.number_of_completed_cases AND a.test_run_id=rn.id AND a.specification_id=sp.id AND a.user_id=u.id AND rn.id=:u GROUP BY a.id ");
        $handler->bindParam(":u", $testRunId);
        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }

        $assignments = array();
        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            $tr = new TestAssignment($row["uname"], $row["pname"], $testRunId, "", -1);
            $tr->setEmail($row["email"]);
            $tr->id = intval($row["aid"]);
            $tr->runTitle = $row['rntitle'];
            $tr->specification = $row['sptitle'];
            array_push($assignments, $tr);
        }
        return $assignments;
    }
    /**
     * Returns all assignments that are unfinished (cases total != cases completed) for given test run and have notification_sent set to 0
     * @param String $testRunId
     * @return TestAssignment[] Description
     */
    public function getUnfinishedAssignmentsWONotif($testRunId) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("SELECT u.username AS uname,p.name as pname, rn.title as rntitle,a.id as aid, sp.title as sptitle FROM test_assignement a, user u, specification sp, test_run rn, platform p WHERE a.platform_id=p.id AND a.number_of_cases!=a.number_of_completed_cases AND a.test_run_id=rn.id AND a.specification_id=sp.id AND a.user_id=u.id AND rn.id=:u AND a.notification_sent=0 GROUP BY a.id ");
        $handler->bindParam(":u", $testRunId);
        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }

        $assignments = array();
        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            $tr = new TestAssignment($row["uname"], $row["pname"], $testRunId, "", -1);
            $tr->id = intval($row["aid"]);
            $tr->runTitle = $row['rntitle'];
            $tr->specification = $row['sptitle'];
            array_push($assignments, $tr);
        }
        return $assignments;
    }

    public function getRunEndDateAndNotificationLimit($testRunId) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("SELECT end, notifications_deadline FROM test_run WHERE id=:i");
        $handler->bindParam(":i", $testRunId);
        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }
        
        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            return array(0 => $row["end"], 1 => intval($row["notifications_deadline"]));
        }
        throw new GeneralException("Test run not found", "test run with id ".$testRunId." not found, notifications not sent","RunNotificationDAO");
    }

}
