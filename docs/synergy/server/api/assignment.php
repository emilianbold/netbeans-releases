<?php

use Synergy\App\Synergy;
use Synergy\Controller\Mediator;
use Synergy\Controller\RunCtrl;
use Synergy\Controller\AssignmentCtrl;
use Synergy\Misc\HTTP;
use Synergy\Model\TestAssignment;
use Synergy\Controller\SpecificationLockCtrl;
use Synergy\Model\Exception\AssignmentConflictException;
use Synergy\Model\Exception\CorruptedAssignmentException;

require_once '../setup/conf.php';

switch ($_SERVER['REQUEST_METHOD']) {
    case "POST":
        if (isset($_REQUEST['volunteer'])) {
            if (!Synergy::getSessionProvider()->sessionExists()) {
                HTTP::Unauthorized("");
                die();
            }
        } else {
            if (!TestAssignment::canCreate()) {
                HTTP::Unauthorized("");
                die();
            }
        }
        $put = file_get_contents('php://input');
        $data = json_decode($put);

        if (!isset($data->specificationId) || !isset($data->platformId) || !isset($data->username) || !isset($data->labelId) || !isset($data->testRunId)) {
            HTTP::BadRequest("Missing parameters");
            die();
        }

        if (!RunCtrl::runIsActive(intval($data->testRunId))) {
            HTTP::PreconditionFailed("Test run is closed");
            die();
        }

        $runCtrl = new RunCtrl();
        $username = (isset($_REQUEST['volunteer'])) ? $username = Synergy::getSessionProvider()->getUsername() : $data->username;
        $createdBy = (isset($_REQUEST['volunteer'])) ? TestAssignment::CREATED_BY_TESTER : TestAssignment::CREATED_BY_MANAGER_ADMIN;

        if ($runCtrl->createAssignment(intval($data->specificationId), intval($data->platformId), intval($data->labelId), intval($data->testRunId), $username, intval($data->tribeId), $createdBy))
            HTTP::OK("");
        else
            HTTP::BadRequest("Oops");
        break;
    case "DELETE":
        if (!isset($_REQUEST['id'])) {
            HTTP::BadRequest("Missing parameters");
            die();
        }
        $assignmentCtrl = new AssignmentCtrl();
        $assignmentId = intval($_REQUEST['id']);
        if (!$assignmentCtrl->userCanDeleteAssignmentById($assignmentId)) {
            HTTP::Unauthorized("");
            die();
        }

        $runCtrl = new RunCtrl();
        $runId = $runCtrl->getRunIdByAssignmentId($assignmentId);
        if (!$runCtrl->runIsActive($runId)) {
            HTTP::BadRequest("Removing assignments is not allowed when test run is closed");
            die();
        }
        
        $assignmentToRemove = $runCtrl->getAssignmentNoProgress($assignmentId);
        if ($runCtrl->deleteAssignment($assignmentId)) {
            if (AssignmentCtrl::$latestRemovalType === TestAssignment::CREATED_BY_TRIBE_LEADER) {
                $headers = array();
                foreach ($_SERVER as $name => $value) {
                    if (substr($name, 0, 5) == 'HTTP_') {
                        $headers[str_replace(' ', '-', ucwords(strtolower(str_replace('_', ' ', substr($name, 5)))))] = $value;
                    }
                }
                $assignmentToRemove->addRemovalComment(array_key_exists("Synergy-Comment", $headers) ? urldecode($headers["Synergy-Comment"]) : "");
                Mediator::emit("assignmentRemovedByLeader", $assignmentToRemove);
            }
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

        $runCtrl = new RunCtrl();

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

        try {
            $runCtrl->saveAssignmentProgress($data, $put, intval($_REQUEST['id']));
            HTTP::OK("");
        } catch (CorruptedAssignmentException $exc) {
            HTTP::BadRequest($exc->title.": ".$exc->message);
        }
        break;
    case "GET":
        if (!isset($_REQUEST['id'])) {
            HTTP::BadRequest("Missing parameters");
            die();
        }
        $mode = "start";
        if (isset($_REQUEST['mode']))
            $mode = $_REQUEST['mode'];

        $runCtrl = new RunCtrl();
        $runId = $runCtrl->getRunIdByAssignmentId(intval($_REQUEST["id"]));
        if (!RunCtrl::runIsActive($runId)) {
            HTTP::PreconditionFailed("Test run is closed");
            die();
        }

        $run = $runCtrl->getRunOverview($runId);
        date_default_timezone_set('UTC');
        $now = time();
        if ($now < strtotime($run->start) || $now > strtotime($run->end)) {
            HTTP::PreconditionFailed("Test run is closed");
            die();
        }


        switch ($mode) {
            case "start":
                try {
                    $assignment = $runCtrl->getAssignment(intval($_REQUEST['id']));
                } catch (AssignmentConflictException $e) {
                    HTTP::Conflict($e->title . ": " . $e->message);
                    die();
                }
                if (is_null($assignment)) {
                    HTTP::NotFound("Assignment not found");
                    die();
                }

                if ($assignment->username !== Synergy::getSessionProvider()->getUsername()) {
                    HTTP::Unauthorized($msg);
                    die();
                }

                $localTime = date('Y-m-d H:i:s');
                $runCtrl->startAssignmentConditional($assignment->id, $localTime);

                if (intval($assignment->completed) < 1) {
                    $lockCtrl = new SpecificationLockCtrl();
                    $lockCtrl->addLock($assignment->specificationId, $assignment->id);
                }

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
                $lockCtrl = new SpecificationLockCtrl();
                Mediator::emit("assignmentRestartedByUser", $id);
                try {
                    $assignment = $runCtrl->getAssignment(intval($_REQUEST['id']));
                } catch (AssignmentConflictException $e) {
                    HTTP::Conflict($e->title . ": " . $e->message);
                    die();
                }
                $lockCtrl->addLock($assignment->specificationId, intval($_REQUEST["id"]));
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