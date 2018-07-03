<?php

use Synergy\Controller\VersionCtrl;
use Synergy\Misc\HTTP;
use Synergy\Model\Version;

require_once '../setup/conf.php';

switch ($_SERVER['REQUEST_METHOD']) {
    case "PUT":
        if (!Version::canEdit()) {
            HTTP::Unauthorized("");
            die();
        }

        $put = file_get_contents('php://input');
        $data = json_decode($put);

        if (!isset($data->name) || !isset($data->id) || !isset($data->isObsolete)) {
            HTTP::BadRequest("Missing parameters");
            die();
        }
        $versionCtrl = new VersionCtrl();
        $obsolete = ($data->isObsolete) ? 1:0;
        if ($versionCtrl->updateVersion(intval($data->id), $data->name, $obsolete))
            HTTP::OK("");
        else
            HTTP::BadRequest("Version with this name already exists");

        break;
    case "POST":
        if (!Version::canCreate()) {
            HTTP::Unauthorized("");
            die();
        }

        $put = file_get_contents('php://input');
        $data = json_decode($put);

        if (!isset($data->name)) {
            HTTP::BadRequest("Missing parameters");
            die();
        }
        $versionCtrl = new VersionCtrl();
        if ($versionCtrl->createVersion($data->name))
            HTTP::OK("");
        else
            HTTP::BadRequest("Version with this name already exists");
        break;
    case "DELETE":      
        if (!Version::canDelete()) {
            HTTP::Unauthorized("");
            die();
        }
        if (!isset($_REQUEST['id'])) {
            HTTP::BadRequest("Missing parameters");
            die();
        }
        $versionCtrl = new VersionCtrl();
        if ($versionCtrl->deleteVersion(intval($_REQUEST['id'])))
            HTTP::OK("");
        else
            HTTP::BadRequest("");
        break;
    default :
        HTTP::MethodNotAllowed("");
        break;
}
?>
