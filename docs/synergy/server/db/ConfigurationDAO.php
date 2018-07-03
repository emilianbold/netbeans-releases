<?php

namespace Synergy\DB;

use PDO;
use Synergy\Misc\Util;
use Synergy\Model\Setting;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * Description of ConfigurationDao
 *
 * @author lada
 */
class ConfigurationDAO {
    //put your code here

    /**
     * Returns array of settings
     * @return Setting[]
     */
    public function loadSettings() {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("SELECT `key`, `value`, `label` FROM settings");

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }
        $result = array();
        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            array_push($result, new Setting($row['key'], $row['value'], $row['label']));
        }
        return $result;
    }

    /**
     * Updates given setting
     * @param Setting $s
     */
    public function saveSetting($s) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("UPDATE `settings` SET `value`=:v WHERE `key`=:k");
        $handler->bindParam(':k', $s->key);
        $handler->bindValue(':v', Util::purifyHTML($s->value));
        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }
    }

    public function keyExists($key) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("SELECT `key` FROM settings WHERE `key`=:k");
        $handler->bindParam(':k', $key);
        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }
        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            return true;
        }
        return false;
    }

    public function addSetting($s) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("INSERT INTO `settings` (`key`, `value`,`label`) VALUES (:k, :v, :l)");
        $handler->bindParam(':k', $s->key);
        $handler->bindValue(':v', Util::purifyHTML($s->value));
        $handler->bindValue(':l', Util::purifyHTML($s->label));
        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }
    }

}

?>
