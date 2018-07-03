<?php

use Synergy\App\Synergy;
use Synergy\Misc\HTTP;
use Synergy\Misc\Util;

require_once '../setup/conf.php';
Util::authorize("manager");
if (!Synergy::getSessionProvider()->sessionExists()) {
    HTTP::Unauthorized("");
    die();
}
switch ($_SERVER['REQUEST_METHOD']) {
    case "GET":
        $logger = Synergy::getProvider("logger");
        $text = $logger->read();
        if(strlen($text)<1){
            $text=" ";
        }
        HTTP::OK($text);
        break;
    case "DELETE":
        $logger = Synergy::getProvider("logger");
        $logger->delete();
        HTTP::OK('Log deleted');
        break;
    default:
        break;
}
?>
