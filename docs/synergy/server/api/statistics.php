<?php

use Synergy\Controller\ReviewCtrl;
use Synergy\Controller\RunCtrl;
use Synergy\Controller\TribeCtrl;
use Synergy\Misc\HTTP;
use Synergy\Model\Run\Rest\RunStatisticsResource;
use Synergy\Model\UserStatistics;

require_once '../setup/conf.php';

switch ($_SERVER['REQUEST_METHOD']) {
    case "GET":
        if (!isset($_REQUEST['id'])) {
            HTTP::BadRequest("Missing parameters");
            die();
        }

        $runCtrl = new RunCtrl();
        $tr = $runCtrl->getRunWithIssues(intval($_REQUEST['id']), false);
        if (is_null($tr)) {
            HTTP::NotFound("Test run not found");
            die();
        }
        
        $reviewCtrl = new ReviewCtrl();
        
        $tr->assigneesOverview = $runCtrl->getUserCentricData($tr);
        $tr->reviews = $reviewCtrl->getAssignments(intval($_REQUEST['id']));
        $tribesId = UserStatistics::getDistinctTribeIds($tr->assigneesOverview);
        $tribeCtrl = new TribeCtrl();
        $tr->tribes = $tribeCtrl->getTribesSpecificationsForTribes($tribesId);
        HTTP::OK(json_encode(RunStatisticsResource::create($tr)), 'Content-type: application/json');
        break;
    default :
        HTTP::MethodNotAllowed("Method not allowed");
        break;
}