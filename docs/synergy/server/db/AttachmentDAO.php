<?php
namespace Synergy\DB;

use PDO;
use Synergy\Controller\Mediator;
use Synergy\DB\DB_DAO;
use Synergy\Model\RunAttachment;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * Description of AttachmentDAO
 *
 * @author vriha
 */
class AttachmentDAO {

    /**
     * Removes specification attachment record
     * @param int $id specification attachment ID
     * @return boolean true on success
     */
    public function deleteSpecificationAttachment($id) {

        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("DELETE FROM specification_attachement WHERE id=:id ");
        $handler->bindParam(':id', $id);

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }

        Mediator::emit("specificationAttachmentDeleted", $id);
        return true;
    }

    /**
     * Creates new specification attachment record
     * @param int $id specification ID
     * @param string $name attachment name
     * @return boolean true if successful
     */
    public function createSpecificationAttachment($id, $name) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("INSERT INTO specification_attachement (specification_id, path) VALUES (:id, :name)");
        $handler->bindParam(':id', $id);
        $path = ATTACHMENT_PATH . $name;
        $handler->bindParam(':name', $path);
        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }
        Mediator::emit("specificationAttachmentCreated", $id);
        return DB_DAO::getDB()->lastInsertId();
    }

    /**
     * Creates a new run attachment
     * @param int $id test run ID
     * @param string $name file name
     * @return boolean true if successful
     */
    public function createRunAttachment($id, $name) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("INSERT INTO run_attachement (test_run_id, path) VALUES (:id, :name)");
        $handler->bindParam(':id', $id);
        $path = ATTACHMENT_PATH . $name;
        $handler->bindParam(':name', $path);
        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }
        Mediator::emit("runAttachmentCreated", array("runId" => $id, "attachmentId" => DB_DAO::getDB()->lastInsertId()));
        return true;
    }

    /**
     * Returns specification attachment's path
     * @param int $id specification attachment ID
     * @return string path to the file on disk
     */
    public function getSpecificationAttachment($id) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("SELECT path FROM specification_attachement WHERE id=:id");
        $handler->bindParam(':id', $id);
        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }
        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            return $row['path'];
        }
        return "";
    }

    /**
     * Returns Run attachment given by ID
     * @param int $id run attachment ID
     * @return RunAttachment
     */
    public function getRunAttachment($id) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("SELECT path FROM run_attachement WHERE id=:id");
        $handler->bindParam(':id', $id);
        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }
        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            return $row['path'];
        }
        return "";
    }

    /**
     * Returns number of attachment records for the same path (aka how many specifications has this attachment linked)
     * @param string $path attachment path
     * @return int
     */
    public function countAttachmnets($path) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("SELECT count(path) as usages FROM specification_attachement WHERE path=:path");
        $handler->bindParam(':path', $path);
        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }
        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            return intval($row['usages']);
        }
        return 0;
    }

    /**
     * 
     * @param type $path
     * @return int
     */
    public function countRunAttachmnets($path) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("SELECT count(path) as usages FROM run_attachement WHERE path=:path");
        $handler->bindParam(':path', $path);
        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }
        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            return intval($row['usages']);
        }
        return 0;
    }

    /**
     * Removes run attachment with given ID
     * @param type $id
     * @return boolean
     */
    public function deleteRunAttachment($id) {
        
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("DELETE FROM run_attachement WHERE id=:id ");
        $handler->bindParam(':id', $id);

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }

        Mediator::emit("runAttachmentDeleted", $id);
        return true;
    }

    public function getSpeficiationId($attachmentId) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("SELECT specification_id FROM specification_attachement WHERE id=:i");
        $handler->bindParam(':i', $attachmentId);
        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }
        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            return intval($row['specification_id']);
        }
        return -1;
    }

    /**
     * only for import!!!
     */
    public static function updateAttachmentSpecification($specId, $attId) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("UPDATE specification_attachement SET specification_id=:sid WHERE id=:aid ");
        $handler->bindParam(':sid', $specId);
        $handler->bindParam(':aid', $attId);
        
        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }

    }

    /**
     * only for import!!!
     */
    public static function updateImageCase($caseId, $imageId) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("UPDATE case_image SET case_id=:sid WHERE id=:aid ");
        $handler->bindParam(':sid', $caseId);
        $handler->bindParam(':aid', $imageId);
        
        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }
    }


    //put your code here
}

?>