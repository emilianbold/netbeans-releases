<?php

use Synergy\Controller\ReviewsCtrl;
use Synergy\Misc\HTTP;
use Synergy\Model\Exception\CurlRequestException;
use Synergy\Model\Review\ReviewPage;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

require_once '../setup/conf.php';

switch ($_SERVER['REQUEST_METHOD']) {
    case "POST":
        if (!ReviewPage::canCreate()) {
            HTTP::Unauthorized("");
            die();
        }

        $config = json_decode(file_get_contents("php://input"));

        if (!isset($config->url) || is_null($config->url) || strlen($config->url) < 1) {
            HTTP::BadRequest("Missing or short URL");
            die();
        }

        $ctrl = new ReviewsCtrl();
        try {
            $ctrl->import($config->url);
            HTTP::OK("");
        } catch (CurlRequestException $e) {
            HTTP::PreconditionFailed($e->message);
        }
        break;
    case "GET":
        $ctrl = new ReviewsCtrl();
        if (isset($_REQUEST['id'])) {
            HTTP::OK(json_encode($ctrl->getAllNotStarted(intval($_REQUEST['id'], 10))), 'Content-type: application/json');
        } else {
            HTTP::OK(json_encode($ctrl->getAll()), 'Content-type: application/json');
        }

        break;
    default :
        HTTP::MethodNotAllowed("");
}