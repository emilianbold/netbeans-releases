<?php

use Synergy\App\Synergy;
use Synergy\Controller\RunCtrl;
use Synergy\Controller\TribeCtrl;
use Synergy\Misc\HTTP;
use Synergy\Model\Exception\AssignmentException;
use Synergy\Model\Exception\AssignmentSecurityException;
use Synergy\Model\TestAssignment;

require_once '../setup/conf.php';


switch ($_SERVER['REQUEST_METHOD']) {
    case "POST":
        $data = json_decode(file_get_contents("php://input"));

        $currentUser = Synergy::getSessionProvider()->getUsername();
        $tribeCtrl = new TribeCtrl();
        $tribes = $tribeCtrl->getTribesByLeader($currentUser);
        try {
            $tribeCtrl->validateAssignments($data, $tribes);
            $runCtrl = new RunCtrl();
            if (count($data) > 0) {
                if (!RunCtrl::runIsActive(intval($data[0]->testRunId))) {
                    HTTP::PreconditionFailed("Test run is closed");
                    die();
                }
            }
            for ($i = 0, $max = count($data); $i < $max; $i++) {
                $runCtrl->createAssignment(intval($data[$i]->specificationId), intval($data[$i]->platformId), intval($data[$i]->labelId), intval($data[$i]->testRunId), $data[$i]->username, -1, TestAssignment::CREATED_BY_TRIBE_LEADER);
            }
            HTTP::OK("OK");
        } catch (AssignmentSecurityException $ex) {
            HTTP::Unauthorized($ex->title . " " . $ex->message);
        } catch (AssignmentException $ex) {
            HTTP::BadRequest($ex->title . " " . $ex->message);
        }

        break;
    default :
        HTTP::MethodNotAllowed("");
        break;
}
