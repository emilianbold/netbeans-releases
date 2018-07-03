<?php

use Synergy\Controller\ConfigurationCtrl;
use Synergy\Misc\HTTP;
use Synergy\Model\Setting;
use Synergy\Model\Setting\Rest\SettingResource;

require_once '../setup/conf.php';


switch ($_SERVER['REQUEST_METHOD']) {
    case "GET":
        if (!Setting::canEdit()) {
            HTTP::Unauthorized("");
            die();
        }
        $ctrl = new ConfigurationCtrl();
        $data = $ctrl->loadSettings();
        HTTP::OK(json_encode(SettingResource::createFromSettings($data)), 'Content-type: application/json');
        break;
    case "PUT":
        if (!Setting::canEdit()) {
            HTTP::Unauthorized("");
            die();
        }
        $put = file_get_contents('php://input');
        $data = json_decode($put);
        $ctrl = new ConfigurationCtrl();
        $ctrl->saveSettings($data);
        HTTP::OK('');
        break;
    default :
        HTTP::MethodNotAllowed('');
        break;
}
?>
