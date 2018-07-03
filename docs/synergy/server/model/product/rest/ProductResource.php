<?php

namespace Synergy\Model\Product\Rest;

/**
 * Description of ProductRest
 *
 * @author vriha
 */
class ProductResource {

    public $components;
    public $name;

    public static function createFromProduct($product) {
        $i = new ProductResource();
        $i->components = $product->components;
        $i->name = $product->name;
        return $i;
    }

    public static function createFromProducts($products) {
        $list = array();
        for ($i = 0, $max = count($products); $i < $max; $i++) {
            array_push($list, ProductResource::createFromProduct($products[$i]));
        }
        return $list;
    }

}
