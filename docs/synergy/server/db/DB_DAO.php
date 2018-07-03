<?php
namespace Synergy\DB;

use Exception;
use mysqli;
use PDO;
use Synergy\App\Synergy;
use Synergy\Model\Exception\GeneralException;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * Description of DB_DAO
 *
 * @author vriha
 */
class DB_DAO {

    /**
     *
     * @var PDO 
     */
    private static $db = null;
    private static $mysqli = null;

    public static function connectDatabase() {
        if (DB_DAO::$db !== null) {
            return;
        }
        try {
//            DB_DAO::$db = new PDO(DHOST, DUSER, DPASS, array(PDO::ATTR_PERSISTENT => true, PDO::ATTR_EMULATE_PREPARES => true));
            DB_DAO::$db = new PDO(DHOST, DUSER, DPASS, array(PDO::ATTR_EMULATE_PREPARES => true));
            DB_DAO::$db->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_WARNING);
        } catch (Exception $ex) {
            throw new GeneralException('Database error', 'DB error [' . $ex . ']:', '');
        }
    }

    public static function getDB() {
        return DB_DAO::$db;
    }

    public static function getMysqli() {
        if (is_null(DB_DAO::$mysqli)) {
            DB_DAO::$mysqli = new mysqli(DBHOST, DUSER, DPASS, DB);
            DB_DAO::$mysqli->set_charset("utf8");
        }
        return DB_DAO::$mysqli;
    }

    public static function secureString($a) {
        mysql_connect(DBHOST, DUSER, DPASS) or die('Not connected to MySQL.');
        mysql_select_db(DB) or die('Not connected to db.');
        return mysql_real_escape_string(htmlspecialchars(stripslashes($a)));
    }

    public static function throwDbError($errorInfo) {
        $d = print_r($errorInfo, true);
        $logger = Synergy::getProvider("logger");
        $logger::log($d);
        throw new GeneralException('Synergy database error', 'DB error  ' . $d, "DB");
    }
    
     public static function executeQuery($query) {
        DB_DAO::connectDatabase();
        try {
            DB_DAO::getDB()->exec($query);
            return true;
        } catch (Exception $e) {
            DB_DAO::throwDbError(DB_DAO::getDB()->errorInfo());
        }
    }
    
    public function getTables() {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("SHOW tables FROM " . DB);

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }
        $tables = array();
        while ($row = $handler->fetch(PDO::FETCH_NUM)) {
            $tables[] = $row[0];
        }
        return $tables;
    }

    public function listTable($table, $limit, $orderBy, $order) {
        DB_DAO::connectDatabase();
        $safe = DB_DAO::secureString($table);
        $safe2 = DB_DAO::secureString($order);
        $safe3 = DB_DAO::secureString($orderBy);
        $res = DB_DAO::getDB()->query("SELECT * FROM `" . $safe . "` ORDER BY " . $safe3 . " " . $safe2 . " LIMIT 0," . intval($limit));
        $columns = array();
        while ($row = $res->fetch(PDO::FETCH_ASSOC)) {
            $columns[] = $row;
        }
        return $columns;
    }

    public function getColumns($table) {
        DB_DAO::connectDatabase();
        $safe = DB_DAO::secureString($table);
        $handler = DB_DAO::getDB()->prepare("SHOW columns FROM `" . $safe . "` IN " . DB);
        $handler->bindParam(':table', $table);
        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }
        $columns = array();
        while ($row = $handler->fetch(PDO::FETCH_NUM)) {
            $columns[] = $row[0];
        }

        return $columns;
    }

}

?>