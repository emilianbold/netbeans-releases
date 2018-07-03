<?php

use Synergy\App\Synergy;
use Synergy\Controller\AttachmentCtrl;
use Synergy\Misc\HTTP;
use Synergy\Model\Specification;

require_once '../setup/conf.php';

switch ($_SERVER['REQUEST_METHOD']) {
    case "GET":

        if (!isset($_REQUEST['id'])) {
            HTTP::BadRequest("Missing parameters");
            die();
        }
        $ctrl = new AttachmentCtrl();
        $attachment  = $ctrl->getSpecificationAttachment(intval($_GET['id']));
        if (strlen($attachment) < 1) {
            HTTP::NotFound("Attachment not found");
            die();
        }
        $path_exploaded = explode(DIRECTORY_SEPARATOR, $attachment);
        $name = $path_exploaded[count($path_exploaded) - 1];
        try {
            $name = substr($name, strpos($name, "_") + 1);
        } catch (Exception $e) {
            $name = $path_exploaded;
        }

        header("Content-Disposition: attachment; filename=" . basename($name));
        header("Content-Length: " . filesize($attachment));
        header("Pragma: public"); // required 
        header("Expires: 0");
        header("Cache-Control: must-revalidate, post-check=0, pre-check=0");
        header("Cache-Control: private", false); // required for certain browsers 
        header("Content-Type: application/force-download");
        header("Content-Transfer-Encoding: binary");
        readfile($attachment);


        break;
    case "DELETE":

        if (!isset($_REQUEST['id']) || !isset($_REQUEST["specificationId"])) {
            HTTP::BadRequest("Missing parameters");
            die();
        }

        if (!Specification::canEdit(intval($_REQUEST["specificationId"]))) {
            HTTP::Unauthorized("");
            die();
        }
        $ctrl = new AttachmentCtrl();
        
        $attachmentId = intval($_REQUEST["id"]);
        if ($ctrl->deleteSpecificationAttachment($attachmentId))
            HTTP::OK("");
        else
            HTTP::InternalServerError("");
        break;
    case "POST":
        
        if (Synergy::getSessionProvider()->sessionExists()) {
            try {

                $name = basename($_FILES['myfile']['name']);
                $newId = -1;
//$_FILES['myfile']['type'] == "text/plain"
                $id = -1;
                if (!isset($_GET['id'])) {
                    HTTP::BadRequest("Missing argument");
                    die();
                } else {
                    $id = intval($_GET['id']);
                }

                $d = (file_get_contents($_FILES['myfile']['tmp_name']));
                if (isset($_GET['type']) && $_GET['type'] !== "specification") {
                    
                } else {
                    if (!Specification::canEdit($id)) {
                        HTTP::Unauthorized("");
                        die();
                    } else {
                        $ctrl = new AttachmentCtrl();
                        $newId = $ctrl->saveSpecificationAttachment($d, $name, $id);
                    }
                }

                HTTP::OK($newId);
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