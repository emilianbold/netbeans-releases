<?php

use Synergy\App\Synergy;
use Synergy\Misc\HTTP;
use Synergy\Controller\UserCtrl;
use Synergy\Model\Exception\UserException;

require_once '../setup/conf.php';

switch ($_SERVER['REQUEST_METHOD']) {
    case 'POST':
        $sessionCtrl = Synergy::getProvider("session");
        $sessionCtrl->setOrigin($_SERVER['HTTP_HOST'] . $_SERVER['REQUEST_URI']);
        $session = $sessionCtrl->loginPost($_POST['username'], $_POST['password']);
        if (!is_null($session)) {
            HTTP::OK(json_encode($session));
        } else {
            HTTP::Unauthorized("Incorrect credentials, login failed");
        }
        break;
    case 'PUT':
        $userCtrl = new UserCtrl();
        $put = file_get_contents('php://input');
        $data = json_decode($put);
        try {
            $resetResult = $userCtrl->resetPassword($data->username);
            if ($resetResult) {
                HTTP::OK("Password has been reset");
            } else {
                HTTP::InternalServerError("Action failed");
            }
        } catch (UserException $e) {
            HTTP::BadRequest($e->message);
        }
        break;
    case 'GET':
        $sessionCtrl = Synergy::getProvider("session");
        $sessionCtrl->setOrigin($_SERVER['HTTP_HOST'] . $_SERVER['REQUEST_URI']);
        if (isset($_GET['login']) && !isset($_REQUEST['return'])) { // when user clicks on Sign in button
            try {
                if (!$sessionCtrl->authenticate()) {
//                $sessionCtrl->login();
                    HTTP::Redirect();
                } else {
                    HTTP::OK(json_encode(Synergy::getSessionProvider()->getUser()), 'Content-type: application/json');
                }
            } catch (UserException $e) {
                HTTP::BadRequest($e->message);
            }
        } else { // passive check on page load
            try {
                if (!$sessionCtrl->authenticate()) {
                    HTTP::Unauthorized('');
                } else {
                    HTTP::OK(json_encode(Synergy::getSessionProvider()->getUser()), 'Content-type: application/json');
                }
            } catch (UserException $e) {
                HTTP::Unauthorized('');
            }
        }
        break;
    case 'DELETE':
        $sessionCtrl = Synergy::getProvider("session");
        $sessionCtrl->setOrigin($_SERVER['HTTP_HOST'] . $_SERVER['REQUEST_URI']);
        $sessionCtrl->logout(true);
        break;
    default :
        HTTP::MethodNotAllowed("");
        break;
}
?>