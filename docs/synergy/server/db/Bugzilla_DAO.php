<?php
namespace Synergy\DB;

use Exception;
use mysqli;
use PDO;
use Synergy\Model\Exception\GeneralException;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * Description of Bugzilla_DAO
 *
 * @author vriha
 */
class Bugzilla_DAO {
    
        /**
     *
     * @var PDO 
     */
    private static $db = null;
    private static $mysqli = null;

    public static function connectDatabase() {
        if (Bugzilla_DAO::$db !== null) {
            return;
        }
        try {
//            Bugzilla_DAO::$db = new PDO(BZ_DHOST, BZ_DUSER, BZ_DPASS, array(PDO::ATTR_PERSISTENT => true, PDO::ATTR_EMULATE_PREPARES => true));
            Bugzilla_DAO::$db = new PDO(BZ_DHOST, BZ_DUSER, BZ_DPASS, array(PDO::ATTR_EMULATE_PREPARES => true));
            Bugzilla_DAO::$db->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_WARNING);
        } catch (Exception $ex) {
            throw new GeneralException('Database error', 'DB error [' . $ex . ']:', '');
        }
    }

    public static function getDB() {
        return Bugzilla_DAO::$db;
    }

    public static function getMysqli() {
        if (is_null(Bugzilla_DAO::$mysqli)) {
            Bugzilla_DAO::$mysqli = new mysqli(BZ_DBHOST, BZ_DUSER, BZ_DPASS, BZ_DB);
            Bugzilla_DAO::$mysqli->set_charset("utf8");
        }
        return Bugzilla_DAO::$mysqli;
    }
    
}

?>
