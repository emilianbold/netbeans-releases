<?php

use Synergy\App\Synergy;
use Synergy\Controller\RunCtrl;
use Synergy\Controller\SpecificationCtrl;
use Synergy\Controller\TribeCtrl;
use Synergy\Controller\UserCtrl;
use Synergy\Misc\HTTP;
use Synergy\Model\Exception\UserException;
use Synergy\Model\User;
use Synergy\Model\User\Rest\UserResource;

require_once '../setup/conf.php';

switch ($_SERVER['REQUEST_METHOD']) {
    case "GET":
        if (!isset($_REQUEST['user'])) {
            HTTP::BadRequest("Missing parameters");
            die();
        }
        $userCtrl = new UserCtrl();
        $runCtrl = new RunCtrl();
        $user = $userCtrl->getUser($_REQUEST['user']);
        if (is_null($user)) {
            HTTP::NotFound('');
            die();
        }

        if (!isset($_REQUEST['short'])) {
            $tribeCtrl = new TribeCtrl();
            $specCtrl = new SpecificationCtrl();
            $user->membership = $tribeCtrl->getUserMembership($user->username);
            // favorites
            $user->favorites = $specCtrl->getFavoriteSpecifications($user->username);
            $user->assignments = $runCtrl->getUsersAssignments($user->username);
            $user->authorOf = $specCtrl->getSpecificationsByAuthor($user->username);
            $user->ownerOf = $specCtrl->getSpecificationsByOwner($user->username);
            // runs TODO
        }
        HTTP::OK(json_encode(UserResource::createFromUser($user)), 'Content-type: application/json');
        break;
    case "PUT":
        if (!Synergy::getSessionProvider()->sessionExists()) {
            HTTP::Unauthorized("");
            die();
        }

        if (!isset($_REQUEST['action'])) {
            HTTP::BadRequest("Missing parameters");
            die();
        }

        switch ($_REQUEST['action']) {
            case "editUser":

                $put = file_get_contents('php://input');
                $data = json_decode($put);
                $selfUpdate = false;
                if (isset($data->username) && $data->username === Synergy::getSessionProvider()->getUsername() && !User::canEdit()) { // update from profile page
                    $selfUpdate = true;
                }
                if (!$selfUpdate && !User::canEdit()) {
                    HTTP::Unauthorized("");
                    die();
                }

                if (!isset($data->firstName) || !isset($data->lastName) || !isset($data->role) || !isset($data->username) || !isset($data->oldUsername) || !filter_var($data->email, FILTER_VALIDATE_EMAIL)) {
                    HTTP::BadRequest("Missing parameters");
                    die();
                }
                $userCtrl = new UserCtrl();
                try {
                    if ($selfUpdate) {
                        $userCtrl->editUserSimple($data->firstName, $data->lastName, $data->username, $data->emailNotifications, $data->email, $data->password);
                    } else {
                        $userCtrl->editUser($data->firstName, $data->lastName, $data->role, $data->username, $data->oldUsername, $data->emailNotifications, $data->email, $data->password);
                    }
                    HTTP::OK('');
                } catch (UserException $ue) {
                    HTTP::BadRequest($ue->title . ": " . $ue->message);
                    die();
                }
                break;
            case "toggleFavorite":
                $put = file_get_contents('php://input');
                $data = json_decode($put);

                if (!isset($data->id) || !isset($data->isFavorite)) {
                    HTTP::BadRequest("Missing parameters " . $put);
                    die();
                }
                $userCtrl = new UserCtrl();
                if (intval($data->isFavorite) < 1) {
                    $userCtrl->removeFavorite(Synergy::getSessionProvider()->getUserId(), intval($data->id));
                } else {
                    $userCtrl->addFavorite(Synergy::getSessionProvider()->getUserId(), intval($data->id));
                }
                HTTP::OK('');
                break;
            default :
                HTTP::BadRequest("Unknown action");
                die();
                break;
        }

        break;
    case "DELETE":
        if (!User::canDelete()) {
            HTTP::Unauthorized("");
            die();
        }
        if (!isset($_REQUEST['username'])) {
            HTTP::BadRequest("Missing parameters");
            die();
        }
        $userCtrl = new UserCtrl();
        if ($userCtrl->deleteUser($_REQUEST['username']))
            HTTP::OK("");
        else
            HTTP::InternalServerError("");
        break;
    case "POST":
        $put = file_get_contents('php://input');
        $data = json_decode($put);

        if (!User::canCreate()) {
            HTTP::Unauthorized("");
            die();
        }

        if (!isset($data->firstName) || !isset($data->lastName) || !isset($data->role) || !isset($data->username) || !filter_var($data->email, FILTER_VALIDATE_EMAIL)) {
            HTTP::BadRequest("Missing parameters");
            die();
        }
        $userCtrl = new UserCtrl();
        try {
            $id = $userCtrl->createUser($data->firstName, $data->lastName, $data->role, $data->username, $data->email, $data->emailNotifications, $data->password);
            HTTP::OK($id);
        } catch (UserException $e) {
            HTTP::BadRequest($e->title . ": " . $e->message);
            die();
        }

        break;

    default :
        HTTP::MethodNotAllowed("");
        break;
}
?>
