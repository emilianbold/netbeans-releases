<?php

use Synergy\Extensions\Specification\ContinuousIntegrationExtension;
use Synergy\Misc\HTTP;
use Synergy\Model\Specification;

require_once '../setup/conf.php';

switch ($_SERVER['REQUEST_METHOD']) {
    case 'POST':
        
        $data = json_decode(file_get_contents('php://input'));
        
        if (!isset($data->specificationId) || !isset($data->jobUrl)) {
            HTTP::BadRequest("Missing parameters");
            die();
        }
        
        if (!Specification::canEdit(intval($data->specificationId))) {
            HTTP::Unauthorized("");
            die();
        }

        $jobController = new ContinuousIntegrationExtension();
        if ($jobController->jobAlreadyExists($data->jobUrl, $data->specificationId)) {
            HTTP::BadRequest("Job already exists");
            die();
        } else {
            $jobController->createNewJob($data->jobUrl, $data->specificationId);
            HTTP::OK('Created');
        }
        break;
    case 'DELETE':
        
        if (!isset($_REQUEST['id']) || is_null($_REQUEST['id']) || !isset($_REQUEST["specificationId"]) || is_null($_REQUEST["specificationId"])) {
            HTTP::BadRequest('Missing parameters');
        }
        if (!Specification::canEdit(intval($_REQUEST["specificationId"]))) {
            HTTP::Unauthorized("");
            die();
        }
        $jobController = new ContinuousIntegrationExtension();
        $jobController->deleteJob(intval($_REQUEST['id']));
        HTTP::OK('Removed');
        break;
    default :
        HTTP::MethodNotAllowed('');
        break;
}
?>