<?php

namespace Synergy\DB;

use PDO;
use Synergy\Controller\Mediator;
use Synergy\Misc\Util;
use Synergy\Model\Version;

/**
 * Description of VersionDAO
 *
 * @author vriha
 */
class VersionDAO {

    /**
     * Returns version by name (7.3, 7.2 etc.) or null if not found
     * @param String $name E.g. 7.3
     * @return \Version|null Either particular version or null if not found
     */
    public function getVersionByName($name) {
        DB_DAO::connectDatabase();

        $handler = DB_DAO::getDB()->prepare("SELECT id FROM version WHERE version=:version");
        $handler->bindParam(':version', $name);

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }
        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            return new Version(intval($row['id']), $name);
        }
        return null;
    }

    /**
     * Returns latest version (ordered by ID)
     * @return \Version
     */
    public function getLatestVersion() {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("SELECT id, version FROM version ORDER BY version DESC LIMIT 0,1");

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }
        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            return new Version(intval($row['id']), $row['version']);
        }
    }

    /**
     * Returns array of all versions
     */
    public function getVersions() {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("SELECT version, id, isObsolete FROM version ORDER BY version DESC LIMIT 0,100");

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }
        $data = array();
        $it = 0;
        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            $data[$it] = new Version($row['id'], $row['version'], $row['isObsolete']);
            $it++;
        }
        return $data;
    }

    /**
     * Renames version
     * @param type $id
     * @param type $newname
     * @return boolean true if success, false if given name is already used
     */
    public function updateVersion($id, $newname, $obsolete) {

        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("UPDATE version v SET v.version=:v, isObsolete=:o WHERE id=:id");
        $handler->bindValue(":v", Util::purifyHTML($newname));
        $handler->bindParam(":id", $id);
        $handler->bindParam(":o", $obsolete);
        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }
        Mediator::emit("versionUpdated", $id);

        return true;
    }

    public function makeObsolete($versionId) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("UPDATE version v SET isObsolete=1 WHERE id=:id");
        $handler->bindParam(":id", $versionId);

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }
        Mediator::emit("versionUpdated", $versionId);

        return true;
    }

    /**
     * Checks if given names is used
     * @param type $name
     */
    public function isNameUsed($name, $excludeID = -1) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("SELECT version FROM version WHERE version=:v AND id!=:i");
        $handler->bindValue(":v", Util::purifyHTML($name));
        $handler->bindValue(":i", $excludeID);
        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }

        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            return true;
        }
        return false;
    }

    /**
     * Creates a new version
     * @param type $newname new version's name
     * @return boolean true if successful, false if name is already used
     */
    public function createVersion($newname) {

        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("INSERT INTO version (version) VALUES (:v)");
        $handler->bindValue(":v", Util::purifyHTML($newname));

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }
        Mediator::emit("versionCreated", DB_DAO::getDB()->lastInsertId());

        return true;
    }

    /**
     * Removes given version and all associted data (specifications etc.) with given version
     * @param type $id
     * @return boolean true if successful, false otherwise
     */
    public function deleteVersion($id) {

        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("DELETE FROM version WHERE id=:id ");
        $handler->bindParam(':id', $id);

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }
        Mediator::emit("versionDeleted", $id);

        return true;
    }

    public function getCurrentVersions() {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("SELECT version, id FROM version WHERE isObsolete=0 ORDER BY version DESC LIMIT 0,100");

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }
        $data = array();
        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            array_push($data, new Version($row['id'], $row['version'], 0));
        }
        return $data;
    }

}

?>
