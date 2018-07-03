<?php

use Synergy\App\Synergy;
use Synergy\Controller\TribeCtrl;
use Synergy\Misc\HTTP;
use Synergy\Model\Tribe;
use Synergy\Model\Tribe\Rest\TribeResource;
use Synergy\Model\User\Rest\UserListItemResource;
require_once '../setup/conf.php';


switch ($_SERVER['REQUEST_METHOD']) {
    case "GET":
        if (!isset($_REQUEST['id'])) {
            HTTP::BadRequest("Missing parameters");
            die();
        }
        $tribeCtrl = new TribeCtrl();
        $tribe = $tribeCtrl->getTribe(intval($_REQUEST['id']));
        if (is_null($tribe)) {
            HTTP::NotFound('Tribe not found');
            die();
        }
        if (Synergy::getSessionProvider()->sessionExists() && intval(Synergy::getSessionProvider()->getUserId()) === intval($tribe->leader_id)) {
            $tribe->addControls("leader");
        } else if (Synergy::getSessionProvider()->sessionExists()) {
            $tribe->addControls(Synergy::getSessionProvider()->getUserRole());
        }
        $tribe->members = UserListItemResource::createFromUsers($tribe->members);
        HTTP::OK(json_encode(TribeResource::createFromTribe($tribe)), 'Content-type: application/json');

        break;
    case "PUT":
        $tribeCtrl = new TribeCtrl();
        $leader= $tribeCtrl->getLeader(intval($_REQUEST['id']));
        $leaderUsername = $leader->username;
        $isLeader = false;
        if (strlen($leaderUsername) > 0 && $leaderUsername === Synergy::getSessionProvider()->getUsername()) {
            $isLeader = true;
        }
        if (!$isLeader){
            if (!Tribe::canEdit()) {
                HTTP::Unauthorized("");
                die();
            }
        }
        if (!isset($_REQUEST['id'])) {
            HTTP::BadRequest("Missing parameters");
            die();
        }

        if (!isset($_REQUEST['action'])) {
            HTTP::BadRequest("Missing parameters");
            die();
        }

        switch ($_REQUEST['action']) {
            case "addMember":
                $put = file_get_contents('php://input');
                $data = json_decode($put);

                if (!isset($data->username)) {
                    HTTP::BadRequest("Missing parameters");
                    die();
                }
                $tribeCtrl = new TribeCtrl();
                if ($isLeader || Tribe::canEdit()) {
                    if ($tribeCtrl->addMember($data->username, intval($_REQUEST['id']))){
                        HTTP::OK("User added");
                    } else {
                        HTTP::BadRequest('User not added, perhaps already a tribe member?');
                    }
                } else {
                    HTTP::Unauthorized("");
                    die();
                }


                break;
            case "removeMember":

                if (!isset($_REQUEST['username'])) {
                    HTTP::BadRequest("Missing parameters");
                    die();
                }
                if ($isLeader || Tribe::canEdit()) {
                    $tribeCtrl = new TribeCtrl();
                    $tribeCtrl->removeMember($_REQUEST['username'], intval($_REQUEST['id']));
                    HTTP::OK("User's membership revoked");
                } else {
                    HTTP::Unauthorized("");
                    die();
                }
                break;
            case "editTribe":
                $tribeCtrl = new TribeCtrl();
                if ($isLeader || Tribe::canEdit()) {
                    $put = file_get_contents('php://input');
                    $data = json_decode($put);

                    if (!isset($data->id) || !isset($data->name) || !isset($data->description) || !isset($data->leaderUsername)) {
                        HTTP::BadRequest("Missing parameters");
                        die();
                    }
                    $t = new Tribe(intval($_REQUEST['id']), $data->name, $data->description, -1);
                    $t->leaderUsername = $data->leaderUsername;
                    if (isset($data->ext)) {
                        $t->ext = $data->ext;
                    }
                    if ($tribeCtrl->editTribe($t))
                        HTTP::OK('');
                    else
                        HTTP::BadRequest("Invalid leader");
                } else {
                    HTTP::Unauthorized("");
                    die();
                }

                break;
            default:
                HTTP::BadRequest('Action not defined');
                break;
        }

        break;
    case "DELETE":
        if (!Tribe::canDelete()) {
            HTTP::Unauthorized("");
            die();
        }
        if (!isset($_REQUEST['id'])) {
            HTTP::BadRequest("Missing parameters");
            die();
        }
        $tribeCtrl = new TribeCtrl();
        if ($tribeCtrl->removeTribe(intval($_REQUEST['id'])))
            HTTP::OK("");
        else
            HTTP::InternalServerError('');
        break;

        break;
    case "POST":
        if (!Tribe::canCreate()) {
            HTTP::Unauthorized("");
            die();
        }

        $put = file_get_contents('php://input');
        $data = json_decode($put);
        if (!isset($data->name) || !isset($data->description) || !isset($data->leaderUsername)) {
            HTTP::BadRequest("Missing parameters");
            die();
        }
        $tribeCtrl = new TribeCtrl();
        $t = new Tribe(-1, $data->name, $data->description, -1);
        $t->leaderUsername = $data->leaderUsername;
        if (isset($data->ext)) {
            $t->ext = $data->ext;
        }
        $id = $tribeCtrl->createTribe($t);
        $url = BASER_URL. "tribe.php?id=" . $id;
        HTTP::OK(json_encode($url), 'Content-type: application/json');
        break;
    default :
        HTTP::MethodNotAllowed("");
        break;
}
?>
