<?php

use Synergy\Controller\RunNotificationCtrl;
use Synergy\Misc\HTTP;
use Synergy\Model\TestRun;

require_once '../setup/conf.php';


switch ($_SERVER['REQUEST_METHOD']) {
    case "GET":
        if (!isset($_REQUEST['id'])) {
            HTTP::BadRequest("Missing parameters");
            die();
        }

        if (!TestRun::canEdit()) {
            HTTP::Unauthorized("");
            die();
        }       
        
        $runNotification = new RunNotificationCtrl();
        $count = $runNotification->sendNotificationsNoLimits(intval($_REQUEST["id"]));
        HTTP::OK("Sent notifications to ".$count." tester(s)");
        break;
    default :
        HTTP::MethodNotAllowed("");
        break;
}