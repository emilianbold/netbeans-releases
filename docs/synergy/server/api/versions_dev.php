<?php

use Synergy\App\Synergy;
use Synergy\Controller\VersionCtrl;
use Synergy\Misc\HTTP;

require_once '../setup/conf.php';

error_log(ini_get('session.gc_maxlifetime'));
switch ($_SERVER['REQUEST_METHOD']) {
    case "GET":
        $versionCtrl = new VersionCtrl();
        $versions = $versionCtrl->getVersions();
        if (Synergy::getSessionProvider()->sessionExists()) {
            $role = Synergy::getSessionProvider()->getUserRole();
            foreach ($versions as $v) {
                $v->addControls($role);
            }
        }
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
