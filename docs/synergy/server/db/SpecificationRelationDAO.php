<?php

namespace Synergy\DB;

use PDO;

/**
 * Description of SpecificationRelationDAO
 *
 * @author vriha
 */
class SpecificationRelationDAO {

    public function isDirectlyRelated($username, $specificationId) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("SELECT s.id FROM specification s, user u WHERE s.id=:sid AND (s.owner_id=u.id OR s.author_id=u.id) AND u.username=:u");
        $handler->bindParam(':u', $username);
        $handler->bindParam(':sid', $specificationId);

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }

        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            return true;
        }
        return false;
    }

}
