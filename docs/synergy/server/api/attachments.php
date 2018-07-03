<?php

use Synergy\App\Synergy;
use Synergy\Controller\SpecificationCtrl;
use Synergy\Misc\HTTP;
use Synergy\Model\Specification\Rest\SpecificationAttachmentResource;

require_once '../setup/conf.php';

switch ($_SERVER['REQUEST_METHOD']) {
    case "GET":

        if (!isset($_REQUEST['id'])) {
            HTTP::BadRequest("Missing parameters");
            die();
        }
        $ctrl = new SpecificationCtrl();
        $attachments = $ctrl->getAttachments(intval($_GET['id']));

        if (Synergy::getSessionProvider()->sessionExists()) {
            $role = Synergy::getSessionProvider()->getUserRole();
            foreach ($attachments as $at) {
                $at->addControls($role);
            }
        }


        HTTP::OK(json_encode(SpecificationAttachmentResource::createFromAttachments($attachments)), 'Content-type: application/json');
        break;
    default:
        break;
}
?>