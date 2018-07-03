<?php

use Synergy\Controller\SessionRefreshCtrl;
use Synergy\Misc\HTTP;

require_once '../setup/conf.php';

switch ($_SERVER['REQUEST_METHOD']) {
    case "PUT":
        HTTP::Unauthorized("");
        break;
    case "POST":
        $data = json_decode(file_get_contents('php://input'));

        if (!isset($data->token)) {
            HTTP::BadRequest("Missing parameters");
            die();
        }
        $ctrl = new SessionRefreshCtrl();
        $ctrl->saveToken($data->token);
        HTTP::OK("");
        break;
    case "GET":
        if (!isset($_REQUEST["token"])) {
            HTTP::BadRequest("Missing token");
            die();
        }
        $ctrl = new SessionRefreshCtrl();
        $token = $ctrl->getToken($_REQUEST["token"]);
        if ($token === null) {
            HTTP::NotFound("");
        } else {
            $ctrl->removeToken($_REQUEST["token"]);
            HTTP::OK("");
        }
        break;

    default:
        HTTP::MethodNotAllowed("");
        break;
}