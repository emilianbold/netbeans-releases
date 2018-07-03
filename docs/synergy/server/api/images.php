<?php

use Synergy\App\Synergy;
use Synergy\Controller\CaseCtrl;
use Synergy\Misc\HTTP;
use Synergy\Model\Image\Rest\ImageResource;

require_once '../setup/conf.php';

switch ($_SERVER['REQUEST_METHOD']) {
    case "GET":

        if (!isset($_REQUEST['id'])) {
            HTTP::BadRequest("Missing parameters");
            die();
        }
        $ctrl = new CaseCtrl();
        $images = $ctrl->getImagesForCase(intval($_GET['id']));

        if (Synergy::getSessionProvider()->sessionExists()) {
            $role = Synergy::getSessionProvider()->getUserRole();
            foreach ($images as $at) {
                $at->addControls($role, $_REQUEST["suiteId"]);
            }
        }

        HTTP::OK(json_encode(ImageResource::createFromImages($images)), 'Content-type: application/json');
        break;
    default:
        break;
}
?>