<?php

use Synergy\Controller\PlatformCtrl;
use Synergy\Misc\HTTP;
use Synergy\Model\Platform;

require_once '../setup/conf.php';

switch ($_SERVER['REQUEST_METHOD']) {
    case "PUT":
        if (!Platform::canEdit()) {
            HTTP::Unauthorized("");
            die();
        }

        if (!isset($_REQUEST['id'])) {
            HTTP::BadRequest("Missing parameters");
            die();
        }

        $put = file_get_contents('php://input');
        $data = json_decode($put);
        $isActive = true;
        if (!isset($data->name)) {
            HTTP::BadRequest("Missing parameters");
            die();
        }
        
        if(isset($data->isActive)){
            $isActive = $data->isActive;
        }
        
        $ctrl = new PlatformCtrl();
        if ($ctrl->updatePlatform(intval($_REQUEST['id']), $data->name, $isActive))
            HTTP::OK("");
        else
            HTTP::BadRequest("Platform with this name already exists");

        break;
    case "POST":
        if (!Platform::canCreate()) {
            HTTP::Unauthorized("");
            die();
        }

        $put = file_get_contents('php://input');
        $data = json_decode($put);

        if (!isset($data->name)) {
            HTTP::BadRequest("Missing parameters");
            die();
        }
        $ctrl = new PlatformCtrl();
        if ($ctrl->createPlatform($data->name))
            HTTP::OK("");
        else
            HTTP::BadRequest("Platform with this name already exists");
        break;
    case "DELETE":
        if (!Platform::canDelete()) {
            HTTP::Unauthorized("");
            die();
        }

        if (!isset($_REQUEST['id'])) {
            HTTP::BadRequest("Missing parameters");
            die();
        }
        $ctrl = new PlatformCtrl();
        if ($ctrl->deletePlatform(intval($_REQUEST['id'])))
            HTTP::OK("");
        else
            HTTP::BadRequest("");

        break;
    default :
        HTTP::MethodNotAllowed("");
        break;
}
?>
