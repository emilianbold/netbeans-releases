<?php

use Synergy\Controller\RunNotificationCtrl;
use Synergy\Misc\HTTP;
use Synergy\Misc\Util;
use Synergy\Model\TestRun;

require_once '../setup/conf.php';


switch ($_SERVER['REQUEST_METHOD']) {
    case "GET":
        if (!isset($_REQUEST['id']) || !isset($_REQUEST["p"])) {
            HTTP::BadRequest("Missing parameters");
            die();
        }
        $v = intval($_REQUEST["p"]);
        if($v !== 42){
            HTTP::BadRequest("Wrong value");
            die();
        }
        
        $runNotification = new RunNotificationCtrl();
        $count = $runNotification->countNotifications(intval($_REQUEST["id"]));
        HTTP::OK("Notifications would be sent to ".$count[0]." tester(s). \n List of users: ".$count[1]);
        break;
    default :
        HTTP::MethodNotAllowed("");
        break;
}