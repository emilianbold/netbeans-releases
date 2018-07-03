<?php
namespace Synergy\DB;

use PDO;
use Synergy\App\Synergy;
use Synergy\Controller\Mediator;
use Synergy\Misc\Util;
use Synergy\Model\Platform;



/**
 * Description of PlatformDAO
 *
 * @author vriha
 */
class PlatformDAO {

    /**
     * Updates given platform
     * @param int $id platform ID
     * @param String $newname new name
     * @return boolean true if successful, false otherwise
     */
    public function updatePlatform($id, $newname, $isActive) {
        $a = 1;
        if (!$isActive) {
            $a = 0;
        }

        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("UPDATE platform v SET v.name=:v, v.is_active=:a WHERE v.id=:id");
        $handler->bindValue(":v", Util::purifyHTML($newname));
        $handler->bindParam(":id", $id);
        $handler->bindParam(":a", $a);
        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }
        Mediator::emit("platformUpdated", $id);
        return true;
    }

    /**
     * Checks if given names is used
     * @param String $name
     */
    public function isNameUsed($name, $excludeId) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("SELECT name FROM platform WHERE name=:v AND id!=:i");
        $handler->bindValue(":v", Util::purifyHTML($name));
        $handler->bindValue(":i", $excludeId);
        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }

        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            return true;
        }
        return false;
    }

    /**
     * Returns all platforms
     * @return Platform[]
     */
    public function getPlatforms() {
        DB_DAO::connectDatabase();
        if(Synergy::getSessionProvider()->getUserRole()=== "admin" || Synergy::getSessionProvider()->getUserRole()=== "manager"){
            $handler = DB_DAO::getDB()->prepare("SELECT name, id,is_active FROM platform ORDER BY id DESC LIMIT 0,100");    
        }else{
            $handler = DB_DAO::getDB()->prepare("SELECT name, id,is_active FROM platform WHERE is_active=1 ORDER BY id DESC LIMIT 0,100");    
        }
        

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }
        $data = array();
        $it = 0;
        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            $data[$it] = new Platform($row['id'], $row['name'], intval($row['is_active']));
            $it++;
        }
        return $data;
    }

    /**
     * Creates a new platform
     * @param type $newname new platform's name
     * @return boolean
     */
    public function createPlatform($newname) {

        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("INSERT INTO platform (name, is_active) VALUES (:v, 1)");
        $handler->bindValue(":v", Util::purifyHTML($newname));

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }
        Mediator::emit("platfromCreated", DB_DAO::getDB()->lastInsertId());
        return true;
    }

    /**
     * Removes given platform and all associated test assignments
     * @param type $id
     * @return boolean true if successful
     */
    public function deletePlatform($id) {

        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("DELETE FROM platform WHERE id=:id ");
        $handler->bindParam(':id', $id);

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }
        Mediator::emit("platformDeleted", $id);
        return true;
    }

    public function findMatchingPlatform($name) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("SELECT id,name FROM platform WHERE name LIKE :t AND is_active=1 ORDER BY name ASC LIMIT 0,15");
        $name = "%" . $name . "%";
        $handler->bindParam(':t', $name, PDO::PARAM_STR);

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }

        $results = array();
        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            array_push($results, new Platform($row['id'], $row['name']));
        }
        return $results;
    }

}

?>
