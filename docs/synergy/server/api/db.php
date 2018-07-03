<?php

use Synergy\Controller\DatabaseCtrl;
use Synergy\Misc\HTTP;
use Synergy\Misc\Util;

require_once '../setup/conf.php';


switch ($_SERVER['REQUEST_METHOD']) {
    case "GET":
        Util::authorize("manager");
        $ctrl = new DatabaseCtrl();
        switch ($_REQUEST['what']) {
            case "tables":
                $data = $ctrl->getTables();
                break;
            case "table":
                $data = $ctrl->getColumns($_REQUEST['table']);
                break;
            case "list":
                $data = $ctrl->listTable($_REQUEST['table'], $_REQUEST['limit'], $_REQUEST['orderBy'], $_REQUEST['order']);
                break;
            case "dump":
                HTTP::OK($ctrl->getSQLDump());
                die();
            default :
                HTTP::BadRequest('Incorrect parameter');
                die();
                break;
        }
       
        HTTP::OK(json_encode($data), 'Content-type: application/json');
        break;
    default :
        HTTP::MethodNotAllowed('');
        break;
}
?>
