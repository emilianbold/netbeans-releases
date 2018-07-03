<?php

namespace Synergy\Controller;

use Synergy\DB\DB_DAO;
use Synergy\DB\StructureDAO;

/**
 * Description of DatabaseCtrl
 *
 * @author vriha
 */
class DatabaseCtrl {

    private $dbDao;

    function __construct() {
        $this->dbDao = new DB_DAO();
    }

    /**
     * Returns array of string of all tables names
     * @return string|Array
     */
    public function getTables() {
        return $this->dbDao->getTables();
    }

    /**
     * Returns records from given table, ordered by $orderBy and $order limited by $limit
     * @param string $table table name
     * @param type $limit number of records to be returned
     * @param type $orderBy column to order records by
     * @param type $order order of sorting (ASC or DESC)
     * @return array of assoc arrays
     */
    public function listTable($table, $limit, $orderBy, $order) {
        return $this->dbDao->listTable($table, $limit, $orderBy, $order);
    }

    /**
     * Returns array of columns names
     * @param string $table table name
     */
    public function getColumns($table) {
        return $this->dbDao->getColumns($table);
    }

    public function getSQLDump() {
        return StructureDAO::getSQLDump();
    }

}

?>
