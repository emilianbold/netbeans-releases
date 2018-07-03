<?php

namespace Synergy\DB;

use PDO;

/**
 * Description of StructureDAO
 *
 * @author vriha
 */
class StructureDAO {

    public static function getSQLDump() {
        $tables = StructureDAO::getTables();
        $dump = "";
        foreach ($tables as $table) {
            $dump .= StructureDAO::getTableDump($table) . "<br/><br/>";
        }
        echo "<br/>";
        echo "<br/>";
        echo $dump;
    }

    private static function getTableDump($table) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("SHOW CREATE TABLE `$table`");
        if (!$handler->execute()) {
            var_dump($handler->errorInfo());
            DB_DAO::throwDbError($handler->errorInfo());
        }

        while ($row = $handler->fetch(PDO::FETCH_NUM)) {
            return $row[1];
        }
    }

    private static function getTables() {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("SHOW TABLES");

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }

        $data = array();
        while ($row = $handler->fetch(PDO::FETCH_NUM)) {
            array_push($data, $row[0]);
        }

        return $data;
    }

}
