<?php

use Synergy\Controller\CalendarCtrl;
use Synergy\Misc\HTTP;

require_once '../setup/conf.php';


switch ($_SERVER['REQUEST_METHOD']) {
    case "GET":
        $ctrl = new CalendarCtrl();
        $data = $ctrl->getEvents();
        HTTP::OK(json_encode($data), 'Content-type: application/json');
        break;
    default :
        HTTP::MethodNotAllowed('');
        break;
}
?>
