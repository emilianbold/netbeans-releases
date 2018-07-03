<?php

namespace Synergy\DB;

use PDO;
use Synergy\Model\Specification\Ext\RemovalRequest;

/**
 * Used for specification extension RemovalRequestExtension
 *
 * @author vriha
 */
class RemovalDAO {

    public function getRequestsForSpecification($specificationId) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("SELECT r.id, u.username FROM removal_request r, user u WHERE r.specification_id=:id AND u.id=r.user_id");
        $handler->bindParam(":id", $specificationId);
        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }
        $results = array();
        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            array_push($results, new RemovalRequest($specificationId, $row["username"], $row["id"]));
        }
        return $results;
    }

    public function removeRequestsForSpecification($specificationId) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("DELETE FROM removal_request WHERE specification_id=:id");
        $handler->bindParam(":id", $specificationId);
        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }
    }

    public function removeRequestsForUser($userId) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("DELETE FROM removal_request r WHERE r.user_id=:id");
        $handler->bindParam(":id", $userId);
        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }
    }

    public function requestExists($specificationId, $userId) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("SELECT r.id FROM removal_request r WHERE r.specification_id=:id AND r.user_id=:ui");
        $handler->bindParam(":id", $specificationId);
        $handler->bindParam(":ui", $userId);
        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }
        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            return true;
        }
        return false;
    }

    public function createRequest($specificationId, $userId) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("INSERT INTO removal_request (specification_id, user_id) VALUES (:id, :ui)");
        $handler->bindParam(":id", $specificationId);
        $handler->bindParam(":ui", $userId);
        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }
    }

}
