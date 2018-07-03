<?php

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

namespace Synergy\Providers;

use Synergy\DB\ProductDAO;

/**
 * Description of ProductCtrl
 *
 * @author lada
 */
class ProductCtrl {

    private $productDao;

    function __construct() {
        $this->productDao = new ProductDAO();
    }

    /**
     * Returns array of products with their components
     * @return Product[]
     */
    public function getProducts() {
        return $this->productDao->getProducts();
    }

}

?>
