<?php

use Synergy\Controller\CaseCtrl;
use Synergy\Misc\HTTP;
use Synergy\Model\Testcase\Rest\CaseListItemResource;

require_once '../setup/conf.php';

switch ($_SERVER['REQUEST_METHOD']) {
    case "GET":
        if (!isset($_REQUEST['case'])) {
            HTTP::BadRequest("Missing parameters");
            die();
        }
        $caseCtrl = new CaseCtrl();
        $suggestions = $caseCtrl->findMatchingCases($_REQUEST['case']);
        HTTP::OK(json_encode(CaseListItemResource::createFromCases($suggestions)), 'Content-type: application/json');
        break;
    default :
        HTTP::MethodNotAllowed("");
        break;
}
?>
