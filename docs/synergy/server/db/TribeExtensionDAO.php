<?php

namespace Synergy\DB;

use PDO;
use Synergy\Model\SpecificationListItem;

/**
 * Description of TribeExtensionDAO
 *
 * @author vriha
 */
class TribeExtensionDAO {

    public function getSpecifications($tribeId) {
        DB_DAO::connectDatabase(); 
        $handler = DB_DAO::getDB()->prepare("SELECT s.title, v.version, s.id, s.simpleName, GROUP_CONCAT( shp.project_id SEPARATOR  ';' ) AS pids, GROUP_CONCAT( project.name SEPARATOR  ';' ) AS pnames FROM (specification s, version v, tribe_has_specification h) LEFT JOIN specification_has_project shp ON shp.specification_id = s.id LEFT JOIN project on project.id=shp.project_id WHERE h.tribe_id =:id AND s.id = h.specification_id AND s.is_active =1 AND v.isObsolete =0 AND v.id = s.version_id GROUP BY s.id");
        $handler->bindParam(':id', $tribeId);
        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }

        $data = array();
        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            $s = new SpecificationListItem($row["id"], $row["title"], $row["version"]);
            $s->setSimpleName($row["simpleName"]);
            $s->setProjects($row["pids"], $row["pnames"]);
            array_push($data, $s);
        }
        return $data;
    }

    public function addSpecificationToTribe($tribeId, $specificationId) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("INSERT INTO tribe_has_specification (tribe_id, specification_id) VALUES (:t, :s)");
        $handler->bindValue(":t", $tribeId);
        $handler->bindValue(":s", $specificationId);

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }

        return true;
    }

    public function removeSpecificationFromTribe($tribeId, $specificationId) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("DELETE FROM tribe_has_specification WHERE tribe_id=:t AND specification_id=:s");
        $handler->bindValue(":t", $tribeId);
        $handler->bindValue(":s", $specificationId);

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }

        return true;
    }

    public function removeAllSpecifications($tribeId) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("DELETE FROM tribe_has_specification WHERE tribe_id=:t");
        $handler->bindValue(":t", $tribeId);

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }

        return true;
    }

    public function removeSpecificationFromTribes($specificationId) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("DELETE FROM tribe_has_specification WHERE specification_id=:t");
        $handler->bindValue(":t", $specificationId);

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }

        return true;
    }

    public function getLatestSpecification($excludeId, $simpleName, $excludeVersion) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("SELECT s.id FROM specification s, version v WHERE s.simpleName=:s AND s.version_id=v.id AND s.id!=:i AND s.is_active=1 AND v.isObsolete=0 AND v.version>:v ORDER BY v.version DESC LIMIT 0,1");
        $handler->bindParam(':s', $simpleName);
        $handler->bindParam(':i', $excludeId);
        $handler->bindParam(':v', $excludeVersion);
        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }
        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            return intval($row["id"]);
        }
        return -1;
    }

}
