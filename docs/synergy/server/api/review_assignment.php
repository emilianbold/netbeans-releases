<?php

use Synergy\App\Synergy;
use Synergy\Controller\AssignmentCtrl;
use Synergy\Controller\Mediator;
use Synergy\Controller\RunCtrl;
use Synergy\Controller\ReviewCtrl;
use Synergy\Controller\SpecificationLockCtrl;
use Synergy\Misc\HTTP;
use Synergy\Model\Exception\AssignmentConflictException;
use Synergy\Model\Review\ReviewAssignment;

require_once '../setup/conf.php';

switch ($_SERVER['REQUEST_METHOD']) {
    case "POST":
        if (isset($_REQUEST['volunteer'])) {
            if (!Synergy::getSessionProvider()->sessionExists()) {
                HTTP::Unauthorized("");
                die();
            }
        } else {
            if (!ReviewAssignment::canCreate()) {
                HTTP::Unauthorized("");
                die();
            }
        }
        $data = json_decode(file_get_contents('php://input'));

        if (!isset($data->reviewUrl) || !isset($data->username) || !isset($data->testRunId) || !isset($data->title) || !isset($data->owner)) {
            HTTP::BadRequest("Missing parameters");
            die();
        }

        if (!RunCtrl::runIsActive(intval($data->testRunId))) {
            HTTP::PreconditionFailed("Test run is closed");
            die();
        }

        $runCtrl = new RunCtrl();
        $username = (isset($_REQUEST['volunteer'])) ? $username = Synergy::getSessionProvider()->getUsername() : $data->username;
        $createdBy = (isset($_REQUEST['volunteer'])) ? ReviewAssignment::CREATED_BY_TESTER : ReviewAssignment::CREATED_BY_MANAGER_ADMIN;

        $reviewCtrl = new ReviewCtrl();
        if ($reviewCtrl->createAssignment(intval($data->testRunId), $username, $data->reviewUrl, $createdBy, $data->title, $data->owner))
            HTTP::OK("");
        else
            HTTP::BadRequest("Oops");
        break;
    case "DELETE":
        if (!isset($_REQUEST['id'])) {
            HTTP::BadRequest("Missing parameters");
            die();
        }
        $assignmentId = intval($_REQUEST['id']);
        $assignmentCtrl = new ReviewCtrl();
        if (!$assignmentCtrl->userCanDeleteAssignmentById($assignmentId)) {
            HTTP::Unauthorized("");
            die();
        }
        
        $runId = $assignmentCtrl->getRunIdByAssignmentId($assignmentId);
        $runCtrl = new RunCtrl();
        if(!$runCtrl->runIsActive($runId)){
            HTTP::BadRequest("Removing assignments is not allowed when test run is closed");
            die();
        }

        if ($assignmentCtrl->deleteAssignment($assignmentId)) {
            HTTP::OK("");
        } else {
            HTTP::BadRequest("");
        }
        break;
    case "PUT":
        if (!isset($_REQUEST['id'])) {
            HTTP::BadRequest("Missing parameters");
            die();
        }

        $headers = array();
        foreach ($_SERVER as $name => $value) {
            if (substr($name, 0, 5) == 'HTTP_') {
                $headers[str_replace(' ', '-', ucwords(strtolower(str_replace('_', ' ', substr($name, 5)))))] = $value;
            }
        }
        $requestedTimestamp = (array_key_exists("Synergy-Timestamp", $headers) ? urldecode($headers["Synergy-Timestamp"]) : "");

        if (strlen($requestedTimestamp) < 1) {
            if (is_null($_REQUEST["datetime"]) || !isset($_REQUEST["datetime"])) {
                HTTP::BadRequest("Missing timestamp");
                die();
            }
            $requestedTimestamp = $_REQUEST["datetime"];
        }

        $runCtrl = new ReviewCtrl();

        if (!$runCtrl->isRequestUpToDate(intval($_REQUEST["id"]), $requestedTimestamp)) {
            HTTP::Conflict("Exists record saved after this request was made");
            die();
        }

        if (!Synergy::getSessionProvider()->sessionExists() || (Synergy::getSessionProvider()->sessionExists() && !$runCtrl->checkUserIsAssigned(intval($_REQUEST['id']), Synergy::getSessionProvider()->getUsername()))) {
            HTTP::Unauthorized("");
            die();
        }
        if (!RunCtrl::runIsActive($runCtrl->getRunIdByAssignmentId(intval($_REQUEST["id"])))) {
            HTTP::PreconditionFailed("Test run is closed");
            die();
        }

        $put = file_get_contents('php://input');
        $data = json_decode($put);

        $runCtrl->saveAssignmentProgress($data, intval($_REQUEST['id']));
        HTTP::OK("");
        break;
    case "GET":
        if (!isset($_REQUEST['id'])) {
            HTTP::BadRequest("Missing parameters");
            die();
        }
        $mode = "view";
        if (isset($_REQUEST['mode']))
            $mode = $_REQUEST['mode'];

        $runCtrl = new ReviewCtrl();
        switch ($mode) {
            case "view":
                $assignment = $runCtrl->getAssignment(intval($_REQUEST['id']));
                if (is_null($assignment)) {
                    HTTP::NotFound("Assignment not found");
                    die();
                }
                HTTP::OK((json_encode($assignment)), 'Content-type: application/json');
                break;
            case "continue":
                $assignment = $runCtrl->getAssignment(intval($_REQUEST['id']));
                if (is_null($assignment)) {
                    HTTP::NotFound("Assignment not found");
                    die();
                }

                date_default_timezone_set('UTC');
                $localTime = date('Y-m-d H:i:s');
                $runCtrl->setLastUpdated($localTime, $assignment->id);
                HTTP::OK((json_encode($assignment)), 'Content-type: application/json');
                break;
            case "restart":

                $headers = array();
                foreach ($_SERVER as $name => $value) {
                    if (substr($name, 0, 5) == 'HTTP_') {
                        $headers[str_replace(' ', '-', ucwords(strtolower(str_replace('_', ' ', substr($name, 5)))))] = $value;
                    }
                }
                $requestedTimestamp = (array_key_exists("Synergy-Timestamp", $headers) ? urldecode($headers["Synergy-Timestamp"]) : "");

                if (strlen($requestedTimestamp) < 1) {
                    if (is_null($_REQUEST["datetime"]) || !isset($_REQUEST["datetime"])) {
                        HTTP::BadRequest("Missing timestamp");
                        die();
                    }
                    $requestedTimestamp = $_REQUEST["datetime"];
                }

                if (!$runCtrl->isRequestUpToDate(intval($_REQUEST["id"]), $requestedTimestamp)) {
                    HTTP::Conflict("Exists record saved after this request was made");
                    die();
                }

                $runCtrl->restartAssignment(intval($_REQUEST['id']));
                $assignment = $runCtrl->getAssignment(intval($_REQUEST['id']));

                if (is_null($assignment)) {
                    HTTP::NotFound("Assignment not found");
                    die();
                }

                if ($assignment->username !== Synergy::getSessionProvider()->getUsername()) {
                    HTTP::Unauthorized($msg);
                    die();
                }

                HTTP::OK((json_encode($assignment)), 'Content-type: application/json');

                break;
            default:
                break;
        }


        break;
    default:
        HTTP::MethodNotAllowed('');
        break;
}
?>