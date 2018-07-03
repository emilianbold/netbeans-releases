<?php

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

namespace Synergy\Model;

/**
 * Description of Product
 *
 * @author lada
 */
class Product {
    
    public $components;
    public $name;
    
    /**
     * Creates a new product from name of product and string of its components separated by ~
     * @param String $name name of product
     * @param String $comp string of components separated by ~
     * @return type
     */
    public static function invokePlainComponents($name, $comp){
        $instance = new self;
        $instance->name = $name;
        $instance->components = explode("~", $comp);
        return $instance;
    }

}

?>
