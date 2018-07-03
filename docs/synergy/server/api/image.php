<?php

use Synergy\App\Synergy;
use Synergy\Controller\CaseCtrl;
use Synergy\Misc\HTTP;
use Synergy\Misc\Util;
use Synergy\Model\TestCase;
use Synergy\Model\TestCaseImage;

require_once '../setup/conf.php';
switch ($_SERVER['REQUEST_METHOD']) {
    case "DELETE":

        if (!isset($_REQUEST['id']) || !isset($_REQUEST["suiteId"])) {
            HTTP::BadRequest("Missing parameters");
            die();
        }
        
        if (!TestCase::canEdit(intval($_REQUEST["suiteId"]))) {
            HTTP::Unauthorized("");
            die();
        }
        
        $caseCtrl = new CaseCtrl();

        if ($caseCtrl->deleteImage(intval($_REQUEST['id'])))
            HTTP::OK("");
        else
            HTTP::InternalServerError("");

        break;
    case "POST":

        if (Synergy::getSessionProvider()->sessionExists()) {
            try {
                $newId = -1;
                $name = basename($_FILES['myfile']['name']);
                $type = basename($_FILES['myfile']['type']);
                if ($type === "png" || $type === "jpg" || $type === "gif" || $type === "jpeg" || Util::isSupportedImage($name)) {
                    $id = -1;
                    if (!isset($_GET['id']) || !isset($_REQUEST["suiteId"])) {
                        HTTP::BadRequest("Missing argument");
                        die();
                    } else {
                        $title = "Image";
                        if (isset($_GET['title'])) {
                            $title = rawurldecode($_GET['title']);
                        }
                        $id = intval($_GET['id']);
                    }
                    $d = (file_get_contents($_FILES['myfile']['tmp_name']));
                    if (!TestCase::canEdit(intval($_REQUEST["suiteId"]))) {
                        HTTP::Unauthorized("");
                        die();
                    } else {
                        $newId = TestCaseImage::saveImage($d, $name, $id, $title);
                    }


                    HTTP::OK($newId);
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
        HTTP::MethodNotAllowed("");
        break;
}
?>
