<?php

namespace Synergy\DB;

use PDO;
use Synergy\Model\CachedSession;
use Synergy\Model\Session;

/**
 * Description of SessionDAO
 *
 * @author lada
 */
class SessionDAO {

    /**
     * Acts like authenticate() only does not require any hashed password
     * @param type $username
     * @return null|\Synergy\Model\Session
     */
    public static function getUser($username) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("SELECT username, role, first_name, last_name, id FROM user WHERE username=:username");
        $handler->bindParam(':username', $username);
        //    $handler->bindParam(':p', $hash);

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }
        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            date_default_timezone_set('UTC');
            $s = new Session($username, $row["role"], date("Y-m-d H:i:s"), $row["id"]);
            $s->firstName = $row["first_name"];
            $s->lastName = $row["last_name"];
            return $s;
        }
        return null;
    }

    /**
     * This is dummy function and authenticates only upon username. This is only for
     * development purposes. In production, there should be some external identity provider
     * and it could be called from here
     * @param String $username
     * @param String $hash
     * @return Session|null instance of session in case of success, null otherwise
     */
    public static function authenticate($username, $hash) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("SELECT username, role, first_name, last_name, id FROM user WHERE username=:username AND passwd=:p");
        $handler->bindParam(':username', $username);
        $handler->bindParam(':p', $hash);

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }
        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            date_default_timezone_set('UTC');
            $s = new Session($username, $row["role"], date("Y-m-d H:i:s"), $row["id"]);
            $s->firstName = $row["first_name"];
            $s->lastName = $row["last_name"];
            return $s;
        }
        return null;
    }

    public function removeCachedData($username) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("DELETE FROM `session` WHERE username=:u");
        $handler->bindParam(':u', $username);

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
            return false;
        }

        return true;
    }

    /**
     * 
     * @param String $cookie SSO cookie value to look for
     * @return CachedSession|null
     */
    public function getCachedSession($cookie) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("SELECT username, timestamp, cookie FROM `session` WHERE cookie=:c");
        $handler->bindParam(':c', $cookie);

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }
        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            return new CachedSession($row['username'], $row['timestamp'], $row['cookie']);
        }
        return null;
    }

    public function cacheSession($username, $cookie) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("INSERT INTO `session` (username, cookie, timestamp) VALUES (:u, :c, :t)");
        $handler->bindParam(':u', $username);
        $handler->bindParam(':c', $cookie);
        $now = intval(microtime(true));
        $handler->bindParam(':t', $now);
        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
            return false;
        }        return true;
    }

}

?>