<?php

use Synergy\Controller\RunCtrl;
use Synergy\Controller\SpecificationCtrl;
use Synergy\Controller\VersionCtrl;
use Synergy\Misc\HTTP;
use Synergy\Model\Run\Rest\RunSpecificationsListResource;

require_once '../setup/conf.php';

switch ($_SERVER['REQUEST_METHOD']) {

    case "GET":
        $versionCtrl = new VersionCtrl();
        $specCtrl = new SpecificationCtrl();

        if (!isset($_REQUEST["testRunId"])) {
            HTTP::BadRequest("Missing test run ID");
            die();
        }

        $runCtrl = new RunCtrl();
        $project = $runCtrl->getProject(intval($_REQUEST["testRunId"], 10));
        $specifications = $specCtrl->getSpecificationsForProject($project->id);

        if (count($specifications) > 0) {
            HTTP::OK(json_encode(RunSpecificationsListResource::create($specifications, $project->name)), 'Content-type: application/json');
        } else {
            HTTP::NotFound('');
        }

        break;
    default :
        HTTP::MethodNotAllowed("");
        break;
}
?>