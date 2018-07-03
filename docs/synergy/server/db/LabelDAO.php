<?php

namespace Synergy\DB;

use PDO;
use Synergy\Model\Label;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * Description of LabelDAO
 *
 * @author vriha
 */
class LabelDAO {

    public function findMatchingLabels($label) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("SELECT id, keyword FROM keyword WHERE keyword LIKE :username ORDER BY keyword ASC LIMIT 0,15");
        $label = "%" . $label . "%";
        $handler->bindParam(':username', $label, PDO::PARAM_STR);

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }

        $results = array();
        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            array_push($results, new Label($row['keyword'], $row['id']));
        }
        return $results;
    }
    
    /**
     * Returns all labels
     * @return Label[]
     */
    public function getAllLabels() {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("SELECT id, keyword FROM keyword ORDER BY keyword ASC");

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }

        $results = array();
        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            array_push($results, new Label($row['keyword'], $row['id']));
        }
        return $results;
    }

    //put your code here
}

?>
