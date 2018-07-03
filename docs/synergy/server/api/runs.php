<?php

use Synergy\App\Synergy;
use Synergy\Controller\RunCtrl;
use Synergy\Misc\HTTP;
use Synergy\Model\Run\Rest\RunListItemResource;

require_once '../setup/conf.php';


switch ($_SERVER['REQUEST_METHOD']) {
    case "GET":
        $mode = "all";

        if (isset($_REQUEST['mode'])) {
            $mode = $_REQUEST['mode'];
        }

        switch ($mode) {
            case "all":
                $page = 1;
                if (isset($_REQUEST['page'])) {
                    $page = intval($_REQUEST['page']);
                }

                $runCtrl = new RunCtrl();
                $runs = $runCtrl->getRuns($page);

                if (Synergy::getSessionProvider()->sessionExists()) {
                    $role = Synergy::getSessionProvider()->getUserRole();
                    foreach ($runs->testRuns as $v) {
                        $v->addControls($role);
                    }
                }
                $runs->testRuns = RunListItemResource::createFromTestRuns($runs->testRuns);
                if (count($runs->testRuns) > 0) {
                    HTTP::OK(json_encode($runs), 'Content-type: application/json');
                } else {
                    HTTP::NotFound('No test run found');
                }
                break;
            case "latest":
                $limit = 5;
                if (isset($_REQUEST['limit'])) {
                    $limit = intval($_REQUEST['limit']);
                }

                if ($limit > 20)
                    $limit = 5;
                $runCtrl = new RunCtrl();
                $runs = $runCtrl->getLatestRuns($limit);
                $runs->testRuns = RunListItemResource::createFromTestRuns($runs->testRuns);
                if (count($runs->testRuns) > 0) {
                    HTTP::OK(json_encode($runs), 'Content-type: application/json');
                } else {
                    HTTP::NotFound('No test run found');
                }

                break;
            default :
                HTTP::BadRequest('Unknown action');
                break;
        }


        break;
    default :
        HTTP::MethodNotAllowed("");
        break;
}
?>
