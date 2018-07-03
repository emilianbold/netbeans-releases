<?php

namespace Synergy\DB;

use PDO;

/**
 * Description of LockDAO
 *
 * @author vriha
 */
class LockDAO {

    public function getLock($specificationId, $assignmentId) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("SELECT timestamp FROM specification_lock WHERE specification_id=:id AND test_assignment_id=:a");
        $handler->bindParam(':id', $specificationId);
        $handler->bindParam(':a', $assignmentId);

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }


        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            return intval($row["timestamp"]);
        }
        return -1;
    }

    public function addLock($specificationId, $assignmentId) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("INSERT INTO specification_lock (specification_id, timestamp, test_assignment_id) VALUES (:id, :t, :a)");
        $handler->bindParam(':id', $specificationId);
        $handler->bindParam(':a', $assignmentId);
        $handler->bindParam(':t', time());
        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }
    }

    public function refreshLock($specificationId, $assignmentId) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("UPDATE specification_lock SET timestamp=:t WHERE specification_id=:id AND test_assignment_id=:a");
        $handler->bindParam(':id', $specificationId);
        $handler->bindParam(':a', $assignmentId);
        $handler->bindParam(':t', time());
        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }
    }

    public function removeLock($lockId) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("DELETE FROM specification_lock WHERE id=:id");
        $handler->bindParam(':id', $lockId);
        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }
    }

    public function getLocks($specificationId) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("SELECT timestamp FROM specification_lock WHERE specification_id=:id");
        $handler->bindParam(':id', $specificationId);


        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }

        $locks = array();
        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            array_push($locks, intval($row["timestamp"]));
        }
        return $locks;
    }

    public function removeOldLocks($limit) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("DELETE FROM specification_lock WHERE timestamp<:l");
        $handler->bindParam(':l', $limit);
        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }
    }

    public function removeLocksForAssignment($data) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("DELETE FROM specification_lock WHERE test_assignment_id=:l");
        $handler->bindParam(':l', $data);
        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }
    }

    public function removeLocksWithoutAssignment() {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("DELETE FROM specification_lock WHERE test_assignment_id NOT IN (SELECT id FROM test_assignement)");
        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }
    }

}
