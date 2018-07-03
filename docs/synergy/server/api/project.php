<?php

use Synergy\Controller\ProjectCtrl;
use Synergy\Misc\HTTP;
use Synergy\Model\Project\Project;
use Synergy\Model\Project\Rest\ProjectResource;

require_once '../setup/conf.php';


switch ($_SERVER['REQUEST_METHOD']) {
    case "PUT":
        if (!Project::canEdit()) {
            HTTP::Unauthorized("");
            die();
        }

        $put = file_get_contents('php://input');
        $data = json_decode($put);

        if (!isset($data->name) || !isset($data->id) || !isset($data->bugTrackingSystem)) {
            HTTP::BadRequest("Missing parameters");
            die();
        }
        $projectCtrl = new ProjectCtrl();

        if ($projectCtrl->updateProject(intval($data->id), $data->name, $data->reportLink, $data->viewLink, $data->multiViewLink, $data->bugTrackingSystem)){
        } else {
            HTTP::BadRequest("Project with this name already exists");
        }

        break;
    case "POST":
        if (!Project::canCreate()) {
            HTTP::Unauthorized("");
            die();
        }

        $put = file_get_contents('php://input');
        $data = json_decode($put);

        if (!isset($data->name)) {
            HTTP::BadRequest("Missing parameters");
            die();
        }
        $projectCtrl = new ProjectCtrl();
        if ((bool) $projectCtrl->createProject($data->name))
            HTTP::OK("");
        else
            HTTP::BadRequest("Project with this name already exists");
        break;
    case "DELETE":
        if (!Project::canDelete()) {
            HTTP::Unauthorized("");
            die();
        }
        if (!isset($_REQUEST['id'])) {
            HTTP::BadRequest("Missing parameters");
            die();
        }
        $projectCtrl = new ProjectCtrl();
        if ($projectCtrl->deleteProject(intval($_REQUEST['id'])))
            HTTP::OK("");
        else
            HTTP::BadRequest("");
        break;
    case "GET":
        if (!Project::canEdit()) {
            HTTP::Unauthorized("");
            die();
        }
        if (!isset($_REQUEST['id'])) {
            HTTP::BadRequest("Missing parameters");
            die();
        }

        $projectCtrl = new ProjectCtrl();
        $project = $projectCtrl->getProjectDetailed(intval($_REQUEST["id"]));
        HTTP::OK(json_encode(ProjectResource::createFromProject($project)), 'Content-type: application/json');
        break;
    default :
        HTTP::MethodNotAllowed("");
        break;
}