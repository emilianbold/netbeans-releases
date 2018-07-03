<?php

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

namespace Synergy\DB;

use PDO;
use Synergy\Model\Product;

/**
 * Description of ProductDAO
 *
 * @author lada
 */
class ProductDAO {
    
    /**
     * Returns array of products with their components
     * @return Product[]
     */
    public function getProducts(){
         Bugzilla_DAO::connectDatabase();
        $handler = Bugzilla_DAO::getDB()->prepare("SELECT p.name as name, GROUP_CONCAT( c.name SEPARATOR  '~' ) as comps FROM products p, components c WHERE c.product_id = p.id GROUP BY p.name");

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }

        $results = array();
        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            array_push($results, Product::invokePlainComponents($row['name'], $row['comps']));
        }
        return $results;
    }
    
}

?>
