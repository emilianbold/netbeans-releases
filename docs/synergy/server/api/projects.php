<?php

use Synergy\App\Synergy;
use Synergy\Misc\HTTP;
use Synergy\Controller\ProjectCtrl;
use Synergy\Model\Project\Rest\ProjectResource;

require_once '../setup/conf.php';
switch ($_SERVER['REQUEST_METHOD']) {
    case "GET":
        $ctrl = new ProjectCtrl();
        $projects = $ctrl->getProjects();
        $role = Synergy::getSessionProvider()->getUserRole();
        foreach ($projects as $pr) {
            $pr->addControls($role);
        }
        HTTP::OK(json_encode(ProjectResource::createFromProjects($projects)), 'Content-type: application/json');
        break;
    default :
        HTTP::MethodNotAllowed("");
        break;
}