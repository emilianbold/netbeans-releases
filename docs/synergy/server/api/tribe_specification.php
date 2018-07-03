<?php

use Synergy\App\Synergy;
use Synergy\Controller\TribeCtrl;
use Synergy\Misc\HTTP;
use Synergy\Misc\Util;
use Synergy\Model\Tribe;
use Synergy\Extensions\Tribe\TribeSpecificationExtension;

require_once "../setup/conf.php";

if (!isset($_REQUEST["id"])) {
    HTTP::BadRequest("Missing parameters");
    die();
}

$tribeCtrl = new TribeCtrl();
$leader = $tribeCtrl->getLeader(intval($_REQUEST["id"]));
$leaderUsername = $leader->username;
$isLeader = false;
$a =1;
if (strlen($leaderUsername) > 0 && $leaderUsername === Synergy::getSessionProvider()->getUsername()) {
    $isLeader = true;
    $a=0;
}

if (!$isLeader && !Tribe::canEdit()) {
    HTTP::Unauthorized("");
    die();
}

switch ($_SERVER["REQUEST_METHOD"]) {
    case "DELETE":
        $ext = new TribeSpecificationExtension();
        if ($ext->removeSpecificationFromTribe(intval($_REQUEST["id"]), intval($_REQUEST["specificationId"]))) {
            HTTP::OK("Specification removed from tribe");
        } else {
            HTTP::InternalServerError("Specification not added to tribe");
        }
        break;
    case "POST":
        $ext = new TribeSpecificationExtension();
        if ($ext->addSpecificationToTribe(intval($_REQUEST["id"]), intval($_REQUEST["specificationId"]))) {
            HTTP::OK("Specification added to tribe");
        } else {
            HTTP::InternalServerError("Specification not added to tribe");
        }
        break;
    default :
        HTTP::MethodNotAllowed("");
        break;
}
?>
