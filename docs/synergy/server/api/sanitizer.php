<?php

use Synergy\Misc\HTTP;
use Synergy\Misc\Util;

require_once '../setup/conf.php';

switch ($_SERVER['REQUEST_METHOD']) {
    case "POST":       
        $data = json_decode(file_get_contents('php://input'));
        if(!isset($data->data) || is_null($data->data)){
            HTTP::BadRequest('Missing data '.$put);
            die();
        }
        HTTP::OK(Util::purifyHTML($data->data));
        break;
    default :
        HTTP::MethodNotAllowed('Only POST method is allowed');
        break;
}