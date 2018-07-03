<?php

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

namespace Synergy\DB;

use PDO;
use Synergy\Model\Session\RefreshSession;

/**
 * Description of StateTokenDAO
 *
 * @author vriha
 */
class SessionRefreshDAO {

    public function saveToken($token) {
        date_default_timezone_set('UTC');
        $localTime = date('Y-m-d H:i:s');
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("INSERT INTO session_refresh (token, created) VALUES (:t,:c)");
        $handler->bindParam(':t', $token);
        $handler->bindParam(':c', $localTime);
        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }
    }


    public function getToken($token) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("SELECT token, created FROM session_refresh WHERE token=:t");
        $handler->bindParam(':t', $token);

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }


        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            return new RefreshSession($row["created"], $token);
        }
        return null;
    }

    public function removeToken($token) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("DELETE FROM session_refresh WHERE token=:t");
        $handler->bindParam(':t', $token);
        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }
    }

}
