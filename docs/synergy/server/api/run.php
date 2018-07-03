<?php

use Synergy\App\Synergy;
use Synergy\Controller\RunCtrl;
use Synergy\Misc\HTTP;
use Synergy\Model\TestRun;
use Synergy\Model\Assignment\Rest\RichAssignmentListItemResource;
use Synergy\Model\Assignment\Rest\AssignmentLineResource;
use Synergy\Model\Run\Rest\RunResource;
use Synergy\Model\Run\Rest\RunBlobsResource;

require_once '../setup/conf.php';


switch ($_SERVER['REQUEST_METHOD']) {
    case "GET":
        if (!isset($_REQUEST['id'])) {
            HTTP::BadRequest("Missing parameters");
            die();
        }
        $mode = "";
        if (isset($_REQUEST['mode'])) {
            $mode = $_REQUEST['mode'];
        }

        switch ($mode) {
            case "blob":

                if (!isset($_REQUEST["id"])) {
                    HTTP::BadRequest("Missing parameters");
                    die();
                }
                $runCtrl = new RunCtrl();
                $tr = $runCtrl->getRunOverview(intval($_REQUEST['id']));
                $tr->blobs = $runCtrl->getBlobs($tr->id);
                $tr->durations = $runCtrl->getDurations($tr->id);
                HTTP::OK(json_encode(RunBlobsResource::create($tr)), 'Content-type: application/json');


                break;
            case "full":
                $runCtrl = new RunCtrl();
                $tr = $runCtrl->getRun(intval($_REQUEST['id']), false);
                if (is_null($tr)) {
                    HTTP::NotFound("Test run not found");
                    die();
                }
                if (Synergy::getSessionProvider()->sessionExists()) {
                    $role = Synergy::getSessionProvider()->getUserRole();
                    $tr->addControls($role);
                    foreach ($tr->assignments as $v) {
                        $v->addControls($role);
                    }
                    foreach ($tr->reviewAssignments as $v) {
                        $v->addControls($role);
                    }
                    foreach ($tr->attachments as $v) {
                        $v->addControls($role);
                    }
                }
                $tr->assignments = RichAssignmentListItemResource::createFromAssignments($tr->assignments);
                HTTP::OK(json_encode($tr), 'Content-type: application/json');
                break;
            case "peruser":
                $runCtrl = new RunCtrl();
                $tr = $runCtrl->getRun(intval($_REQUEST['id']), true);
                if (is_null($tr)) {
                    HTTP::NotFound("Test run not found");
                    die();
                }
                if (Synergy::getSessionProvider()->sessionExists()) {
                    $role = Synergy::getSessionProvider()->getUserRole();
                    $tr->addControls($role);
                    foreach ($tr->assignments as $v) {
                        foreach ($v->assignments as $a) {
                            $a->addControls($role);
                        }
                        $v->assignments = RichAssignmentListItemResource::createFromAssignments($v->assignments);
                    }
                    foreach ($tr->reviewAssignments as $v) {
                        $v->addControls($role);
                    }
                    foreach ($tr->attachments as $v) {
                        $v->addControls($role);
                    }
                    $tr->assignments = AssignmentLineResource::createFromUsers($tr->assignments, true);
                }
                HTTP::OK(json_encode($tr), 'Content-type: application/json');
                break;
            case "simple":
                $runCtrl = new RunCtrl();
                $tr = $runCtrl->getRunOverview(intval($_REQUEST['id']));
                if (Synergy::getSessionProvider()->sessionExists()) {
                    $role = Synergy::getSessionProvider()->getUserRole();
                    foreach ($tr->attachments as $v) {
                        $v->addControls($role);
                    }
                }
                if (is_null($tr)) {
                    HTTP::NotFound("Test run not found");
                    die();
                }
                HTTP::OK(json_encode(RunResource::create($tr)), 'Content-type: application/json');
                break;
            default:
                HTTP::BadRequest('Unknown mode');
                break;
        }

        break;
    case "PUT":
        if (!isset($_REQUEST['id'])) {
            HTTP::BadRequest("Missing parameters");
            die();
        }

        if (!TestRun::canEdit()) {
            HTTP::Unauthorized("");
            die();
        }
        $mode = (isset($_REQUEST["mode"])) ? $_REQUEST["mode"] : "default";
        $runCtrl = new RunCtrl();
        switch ($mode) {
            case "freeze":

                if (intval($_REQUEST["freeze"]) === 1) {
                    $freeze = false;
                } else {
                    $freeze = true;
                }
                $runCtrl->setActive(intval($_REQUEST["id"]), $freeze);
                HTTP::OK("Test run frozen");
                break;
            default:
                $put = file_get_contents('php://input');
                $data = json_decode($put);
                if (!isset($data->title) || !isset($data->desc) || !isset($data->start) || !isset($data->end) || !isset($data->notifications) || !isset($data->projectId)) {
                    HTTP::BadRequest("Missing parameters");
                    die();
                }
                $id = $runCtrl->editRun(intval($_REQUEST['id']), $data->title, $data->desc, $data->start, $data->end, intval($data->notifications), intval($data->projectId, 10));
                $url = BASER_URL . "run.php?id=" . $id;
                HTTP::OK(json_encode($url), 'Content-type: application/json');
                break;
        }

        break;
    case "POST":
        if (!TestRun::canCreate()) {
            HTTP::Unauthorized("");
            die();
        }

        $put = file_get_contents('php://input');
        $data = json_decode($put);
        if (!isset($data->title) || !isset($data->desc) || !isset($data->start) || !isset($data->end) || !isset($data->notifications) || !isset($data->projectId)) {
            HTTP::BadRequest("Missing parameters");
            die();
        }
        $runCtrl = new RunCtrl();
        $id = $runCtrl->createRun($data->title, $data->desc, $data->start, $data->end, intval($data->notifications, 10), intval($data->projectId, 10));
        $url = BASER_URL . "run.php?id=" . $id;
        HTTP::OK(json_encode($url), 'Content-type: application/json');

        break;
    case "DELETE":
        if (!TestRun::canDelete()) {
            HTTP::Unauthorized("");
            die();
        }
        if (!isset($_REQUEST['id'])) {
            HTTP::BadRequest("Missing parameters");
            die();
        }
        $runCtrl = new RunCtrl();
        if ($runCtrl->deleteRun(intval($_REQUEST['id'])))
            HTTP::OK("");
        else
            HTTP::InternalServerError("");

        break;
    default :
        HTTP::MethodNotAllowed("");
        break;
}
?>
