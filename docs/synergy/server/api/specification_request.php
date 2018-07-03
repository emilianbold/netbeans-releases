<?php

use Synergy\App\Synergy;
use Synergy\Controller\SpecificationCtrl;
use Synergy\Controller\UserCtrl;
use Synergy\Misc\HTTP;
use Synergy\Controller\NotificationCtrl;

require_once '../setup/conf.php';

switch ($_SERVER['REQUEST_METHOD']) {
    case "POST":
        $data = json_decode(file_get_contents("php://input"));

        if (Synergy::getSessionProvider()->getUserId() < 0) {
            HTTP::Unauthorized("");
            die();
        }

        if (!isset($data->specificationId) || !isset($data->text) || !isset($data->authorUsername)) {
            HTTP::BadRequest("Missing parameters");
            die();
        }

        $specCtrl = new SpecificationCtrl();
        $userCtrl = new UserCtrl();
        $specification = $specCtrl->getSpecificationOverview(intval($data->specificationId));
        $requester = $userCtrl->getUser($data->authorUsername);
        if (!is_null($specification) && !is_null($requester)) {
            $notificationCtrl = new NotificationCtrl();
            if ($specification->owner === "import" && $specification->ownerId === 6) {
                $managers = $userCtrl->getManagers();
                foreach ($managers as $man) {
                    $specification->owner = $man->username;
                    $notificationCtrl->sendOwnershipRequest($specification, $data->text, $requester->firstName . " " . $requester->lastName, $data->authorUsername);
                }
            } else {
                $notificationCtrl->sendOwnershipRequest($specification, $data->text, $requester->firstName . " " . $requester->lastName, $data->authorUsername);
            }
        } else {
            HTTP::PreconditionFailed("Specification or owner not found");
        }


        break;
    default:
        HTTP::BadRequest("Method not allowed");
        break;
}

