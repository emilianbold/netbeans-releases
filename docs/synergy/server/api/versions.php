<?php

use Synergy\App\Synergy;
use Synergy\Controller\VersionCtrl;
use Synergy\Misc\HTTP;
use Synergy\Model\Version\Rest\VersionResource;

require_once '../setup/conf.php';

switch ($_SERVER['REQUEST_METHOD']) {
    case "GET":
        $versionCtrl = new VersionCtrl();
        if(isset($_REQUEST['all'])) {
            $versions = $versionCtrl->getAllVersions();
        } else {
            $versions = $versionCtrl->getVersions();
        }

        if (Synergy::getSessionProvider()->sessionExists()) {
            $role = Synergy::getSessionProvider()->getUserRole();
            foreach ($versions as $v) {
                $v->addControls($role);
            }
        }
        $versions = VersionResource::createFromVersions($versions);
        if (count($versions) > 0) {
            HTTP::OK(json_encode($versions), 'Content-type: application/json');
        } else {
            HTTP::NotFound('No version found');
        }
        break;
    default :
        HTTP::MethodNotAllowed("");
        break;
}
?>
