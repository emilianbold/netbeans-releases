<?php

use Synergy\Controller\RunCtrl;
use Synergy\Misc\HTTP;
use Synergy\Model\Exception\AssignmentException;
use Synergy\Model\TestAssignment;

require_once '../setup/conf.php';

switch ($_SERVER['REQUEST_METHOD']) {
    case "POST":
        if (!TestAssignment::canCreate()) {
            HTTP::Unauthorized("");
            die();
        }

        $put = file_get_contents('php://input');
        $data = json_decode($put);
        $ctrl = new RunCtrl();
        switch ($_REQUEST["mode"]) {
            case "user":
                try {
                    $ctrl->validateAssignments($data);

                    if (count($data) > 0) {
                        if (!RunCtrl::runIsActive(intval($data[0]->testRunId))) {
                            HTTP::PreconditionFailed("Test run is closed");
                            die();
                        }
                    }

                    for ($i = 0, $max = count($data); $i < $max; $i++) {
                        $ctrl->createAssignment(intval($data[$i]->specificationId), intval($data[$i]->platformId), intval($data[$i]->labelId), intval($data[$i]->testRunId), $data[$i]->username, -1, TestAssignment::CREATED_BY_MANAGER_ADMIN);
                    }
                    HTTP::OK("Created");
                } catch (AssignmentException $e) {
                    HTTP::BadRequest($e->title . " " . $ex->message);
                }
                break;
            case "matrix":

                if (isset($data->runId) > 0) {
                    if (!RunCtrl::runIsActive(intval($data->runId))) {
                        HTTP::PreconditionFailed("Test run is closed");
                        die();
                    }
                }

                $failures = $ctrl->createMatrixAssignment($data, TestAssignment::CREATED_BY_MANAGER_ADMIN);
                if (count($failures) < 1) {
                    HTTP::OK('created');
                } else {
                    HTTP::InternalServerError(json_encode($failures));
                }
                break;

            default:
                HTTP::BadRequest("Wrong action");
                break;
        }
        break;
    default:
        break;
}
?>
