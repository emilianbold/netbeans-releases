<?php

use Synergy\Controller\ReviewCtrl;
use Synergy\Controller\RunCtrl;
use Synergy\Controller\TribeCtrl;
use Synergy\Misc\HTTP;
use Synergy\Model\Run\Rest\RunStatisticsResource;
use Synergy\Model\UserStatistics;

require_once '../setup/conf.php';

switch ($_SERVER['REQUEST_METHOD']) {
    case "POST":

        $data = json_decode(file_get_contents("php://input"));
        if (!isset($_REQUEST['id']) || !isset($data->from) || !isset($data->to)) {
            HTTP::BadRequest("Missing parameters");
            die();
        }

        $runCtrl = new RunCtrl();
        $tr = $runCtrl->getRunWithIssuesInPeriod(intval($_REQUEST['id']), $data->from, $data->to);
        $reviewCtrl = new ReviewCtrl();
        $tr->reviews = $reviewCtrl->getAssignmentsInPeriod(intval($_REQUEST['id']), $data->from, $data->to);

        if (is_null($tr)) {
            HTTP::NotFound("Test run not found");
            die();
        }
        $tr->assigneesOverview = $runCtrl->getUserCentricData($tr);
        $tribesId = UserStatistics::getDistinctTribeIds($tr->assigneesOverview);
        $tribeCtrl = new TribeCtrl();
        $tr->tribes = $tribeCtrl->getTribesSpecificationsForTribes($tribesId);
        HTTP::OK(json_encode(RunStatisticsResource::create($tr)), 'Content-type: application/json');
        break;
    default :
        HTTP::MethodNotAllowed("Method not allowed");
        break;
}