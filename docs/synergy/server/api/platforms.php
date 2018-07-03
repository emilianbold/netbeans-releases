<?php

use Synergy\App\Synergy;
use Synergy\Controller\PlatformCtrl;
use Synergy\Misc\HTTP;
use Synergy\Model\Platform\Rest\PlatformResource;

require_once '../setup/conf.php';


switch ($_SERVER['REQUEST_METHOD']) {
    case "GET":
        if (isset($_REQUEST['mode'])) {
            switch ($_REQUEST['mode']) {
                case "filter":
                    if (isset($_REQUEST['query'])) {
                        $ctrl = new PlatformCtrl();
                        $suggestions = $ctrl->findMatchingPlatform($_REQUEST['query']);
                        HTTP::OK(json_encode(PlatformResource::createFromPlatforms($suggestions)), 'Content-type: application/json');
                    } else {
                        HTTP::BadRequest('');
                    }
                    break;
                default:
                    break;
            }
        } else {

            $ctrl = new PlatformCtrl();
            $platforms = $ctrl->getPlatforms();

            if (Synergy::getSessionProvider()->sessionExists()) {
                $role = Synergy::getSessionProvider()->getUserRole();
                foreach ($platforms as $v) {
                    $v->addControls($role);
                }
            }
            if (count($platforms) > 0) {
                HTTP::OK(json_encode(PlatformResource::createFromPlatforms($platforms)), 'Content-type: application/json');
            } else {
                HTTP::NotFound('No version found');
            }
        }
        break;
    default :
        HTTP::MethodNotAllowed("");
        break;
}
?>
