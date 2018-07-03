<?php

use Synergy\Controller\AssignmentCtrl;
use Synergy\Misc\HTTP;

require_once '../setup/conf.php';

switch ($_SERVER['REQUEST_METHOD']) {
    case "POST":
        $data = json_decode(file_get_contents('php://input'));
        $ctrl = new AssignmentCtrl();
        if($ctrl->assignmentExists($data->username, $data->platformId, $data->labelId, $data->specificationId, $data->testRunId)){
            HTTP::OK("Exists");
        }else{
            HTTP::NotFound("Not found");
        }
        break;
    default:
        HTTP::MethodNotAllowed("");
        break;
}
?>
