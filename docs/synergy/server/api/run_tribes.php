<?php

use Synergy\App\Synergy;
use Synergy\Controller\TribeCtrl;
use Synergy\Controller\RunCtrl;
use Synergy\Misc\HTTP;

require_once '../setup/conf.php';

switch ($_SERVER['REQUEST_METHOD']) {
    case "GET":
        $tribeCtrl = new TribeCtrl();
        $tribes = array();

        if (!isset($_REQUEST["testRunId"])) {
            HTTP::BadRequest("Missing test run ID");
            die();
        }

        $leaderUsername = (isset($_REQUEST["leader"]) ? $_REQUEST["leader"] : "-1");
        $runCtrl = new RunCtrl();
        $project = $runCtrl->getProject(intval($_REQUEST["testRunId"], 10));


        if (Synergy::getSessionProvider()->getUserRole() === "admin" || Synergy::getSessionProvider()->getUserRole() === "manager") {
            $tribes = $tribeCtrl->getTribesDetailed();
        } else {
            $tribes = $tribeCtrl->getTribesByLeader($leaderUsername);
        }
        if (count($tribes) < 1) {
            HTTP::NotFound('No tribe found, perhaps you are not leader of any tribe?');
            die();
        }


        if (count($tribes) > 0) {
            HTTP::OK(json_encode($tribeCtrl->filterByProjectId($tribes, $project->id)), 'Content-type: application/json');
        } else {
            HTTP::NotFound('No tribe found');
        }
        break;
    default :
        HTTP::MethodNotAllowed("");
        break;
}
?>
