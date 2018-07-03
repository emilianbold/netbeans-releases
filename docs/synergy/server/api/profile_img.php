<?php

use Synergy\App\Synergy;
use Synergy\Controller\UserCtrl;
use Synergy\Misc\HTTP;
use Synergy\Misc\Util;

require_once '../setup/conf.php';

switch ($_SERVER['REQUEST_METHOD']) {
    case "DELETE": // remove
        $id = -1;
        if (!isset($_GET['id'])) {
            HTTP::BadRequest("Missing argument");
            die();
        } else {
            $id = intval($_GET['id']);
        }

        if ($id !== Synergy::getSessionProvider()->getUserId()) {
            HTTP::Unauthorized("");
            die();
        }

        $ctrl = new UserCtrl();
        $ctrl->deleteProfileImg($id);
        HTTP::OK("./img/user.png");
        break;
    case "POST":

        if (Synergy::getSessionProvider()->sessionExists()) {
            try {

                $name = basename($_FILES['myfile']['name']);
                $type = basename($_FILES['myfile']['type']);
                if ($type === "png" || $type === "jpg" || $type === "gif" || $type === "jpeg" || Util::isSupportedImage($name)) {
                    $id = -1;
                    if (!isset($_GET['id'])) {
                        HTTP::BadRequest("Missing argument");
                        die();
                    } else {
                        $id = intval($_GET['id']);
                    }

                    if ($id !== Synergy::getSessionProvider()->getUserId()) {
                        HTTP::Unauthorized("");
                        die();
                    }

                    $d = (file_get_contents($_FILES['myfile']['tmp_name']));

                    $ctrl = new UserCtrl();
                    $newPath = $ctrl->saveProfileImg($d, $name, $id);
                    if (strlen($newPath) > 0) {
                        HTTP::OK($newPath);
                    } else {
                        HTTP::InternalServerError("Unable to set profile image");
                    }
                } else {
                    HTTP::PreconditionFailed("Bad filetype, only JPG and PNG are supported");
                }
            } catch (Exception $e) {
                $status_header = 'HTTP/1.1 500';
                header($status_header);
                header('Content-type: text/plain');
                echo $e->getMessage();
            }
        }
        break;
    default:
        break;
}
?>