<?php

use Synergy\Controller\SpecificationCtrl;
use Synergy\Misc\HTTP;

require_once '../setup/conf.php';

switch ($_SERVER['REQUEST_METHOD']) {
    case "POST":
        $data = json_decode(file_get_contents('php://input'));
        $results = array();
        $specCtrl = new SpecificationCtrl();
        $specId = -1;
        for ($index = 0; $index < count($data->ids); $index++) {
            $specId = intval($data->ids[$index], 10);
            $results["" . $specId] = intval($specCtrl->getCasesCount($specId, -1), 10);
        }


        HTTP::OK(json_encode($results), 'Content-type: application/json');
        break;

    default :
        HTTP::MethodNotAllowed("");
        break;
}
?>