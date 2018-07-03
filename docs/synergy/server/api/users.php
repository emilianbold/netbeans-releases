<?php

use Synergy\App\Synergy;
use Synergy\Controller\UserCtrl;
use Synergy\Misc\HTTP;
use Synergy\Model\Exception\CurlRequestException;
use Synergy\Model\User;
use Synergy\Model\User\Rest\UserListItemResource;

require_once '../setup/conf.php';

switch ($_SERVER['REQUEST_METHOD']) {
    case "GET":
        $userCtrl = new UserCtrl();
        if (isset($_REQUEST['user'])) {
            $suggestions = UserListItemResource::createFromUsers($userCtrl->findMatchingUsers($_REQUEST['user']));
        } else {
            $page = -1;
            if (isset($_REQUEST['page'])) {
                $page = intval($_REQUEST['page']);
            }
            if ($page > -1) {
                $suggestions = $userCtrl->findUsers($page);
            } else {
                $suggestions = $userCtrl->getAllUsers();
            }
            $role = Synergy::getSessionProvider()->getUserRole();
            foreach ($suggestions->users as $user) {
                $user->addControls($role);
            }
            $suggestions->users = UserListItemResource::createFromUsers($suggestions->users);
        }
        HTTP::OK(json_encode($suggestions), 'Content-type: application/json');
        break;
    case "POST":
        if (!User::canCreate()) {
            HTTP::Unauthorized("");
            die();
        }
        $put = file_get_contents('php://input');
        $data = json_decode($put);
        $ctrl = new UserCtrl();
        try {
            $result = $ctrl->importUsers($data->url);
            HTTP::OK($result);
        } catch (CurlRequestException $e) {
            HTTP::PreconditionFailed($e->message);
        }

        break;
    case "PUT":
        if (!User::canEdit()) {
            HTTP::Unauthorized("");
            die();
        }
        $ctrl = new UserCtrl();
        if (isset($_REQUEST['role']) && !is_null($_REQUEST['role']) && strlen($_REQUEST['role']) > 1) {
            $ctrl->retireUsers($_REQUEST['role']);
            HTTP::OK("Users with role '" . $_REQUEST['role'] . "' have been retired");
        } else {
            HTTP::BadRequest("Wrong parameter");
        }

        break;
    default :
        HTTP::MethodNotAllowed("");
        break;
}
?>
