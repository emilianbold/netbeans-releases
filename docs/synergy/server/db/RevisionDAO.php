<?php

namespace Synergy\DB;

use PDO;
use Synergy\Model\Revision;

/**
 * Description of RevisionDAO
 *
 * @author vriha
 */
class RevisionDAO {
    
    public function addNewRevision($specificationId, $content, $who, $when){
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("INSERT INTO specification_revisions (content, specification_id, date, author) VALUES (:c, :s, :d, :a) ");
        $handler->bindParam(':c', $content);
        $handler->bindParam(':s', $specificationId);
        $handler->bindParam(':d', $when);
        $handler->bindParam(':a', $who);

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
            return false;
        }
        
        return true;
    }
    
       public function deleteRevisions($specificationId){
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("DELETE FROM specification_revisions WHERE specification_id=:id");
        $handler->bindParam(':id', $specificationId);

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
            return false;
        }
        
        return true;
    }
    
    public function getListOfRevisions($specificationId) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("SELECT id, date, author FROM specification_revisions WHERE specification_id=:id ORDER BY date ASC");
        $handler->bindParam(':id', $specificationId);
        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }
        $results = array();
        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            array_push($results, new Revision($row['id'], $specificationId, '', $row['author'], $row['date']));
        }
        return $results;
    }

    public function getRevisionById($id) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("SELECT id, date, author, content, specification_id FROM specification_revisions WHERE id=:id");
        $handler->bindParam(':id', $id);
        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }
        
        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            return new Revision($row['id'], $row['specification_id'], $row['content'], $row['author'], $row['date']);
        }
        return null;
    }

}
