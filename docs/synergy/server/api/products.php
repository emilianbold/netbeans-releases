<?php

use Synergy\App\Synergy;
use Synergy\Misc\HTTP;
use Synergy\Model\Product\Rest\ProductResource;

require_once '../setup/conf.php';

$productCtrl = Synergy::getProvider('products');
$products = array();
if (!is_null($productCtrl)) {
    $products = $productCtrl->getProducts();
}

HTTP::OK(json_encode(ProductResource::createFromProducts($products)), 'Content-type: application/json');
?>
