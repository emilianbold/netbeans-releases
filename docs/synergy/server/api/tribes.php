<?php

use Synergy\App\Synergy;
use Synergy\Controller\TribeCtrl;
use Synergy\Misc\HTTP;
use Synergy\Model\Exception\CurlRequestException;
use Synergy\Model\Tribe;
use Synergy\Model\Tribe\Rest\TribeListItemResource;

require_once '../setup/conf.php';

switch ($_SERVER['REQUEST_METHOD']) {
    case "GET":
        $tribeCtrl = new TribeCtrl();

        $mode = (isset($_REQUEST["mode"]) ? $_REQUEST["mode"] : "");
        $tribes = array();
        switch ($mode) {
            case "full":
                $leaderUsername = (isset($_REQUEST["leader"]) ? $_REQUEST["leader"] : "-1");
                if(Synergy::getSessionProvider()->getUserRole() === "admin" || Synergy::getSessionProvider()->getUserRole() === "manager"){
                    $tribes = $tribeCtrl->getTribesDetailed();
                }else{
                    $tribes = $tribeCtrl->getTribesByLeader($leaderUsername);
                }
                if (count($tribes) < 1) {
                    HTTP::NotFound('No tribe found, perhaps you are not leader of any tribe?');
                    die();
                }
                break;
            default:
                $tribes = $tribeCtrl->getTribes();
                if (Synergy::getSessionProvider()->sessionExists()) {
                    $role = Synergy::getSessionProvider()->getUserRole();
                    foreach ($tribes as $v) {
                        $v->addControls($role);
                    }
                }
                $tribes = TribeListItemResource::createFromTribes($tribes);
                break;
        }

        if (count($tribes) > 0) {
            HTTP::OK(json_encode($tribes), 'Content-type: application/json');
        } else {
            HTTP::NotFound('No tribe found');
        }
        break;
    case "POST":
        if (!Tribe::canCreate()) {
            HTTP::Unauthorized("");
            die();
        }
        $put = file_get_contents('php://input');
        $data = json_decode($put);
        $ctrl = new TribeCtrl();
        try {
            $result = $ctrl->importTribes($data->url);
            HTTP::OK($result);
        } catch (CurlRequestException $e) {
            HTTP::PreconditionFailed($e->message);
        }

        break;
    default :
        HTTP::MethodNotAllowed("");
        break;
}
?>
