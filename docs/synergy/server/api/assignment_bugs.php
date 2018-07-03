<?php

use Synergy\App\Synergy;
use Synergy\Controller\RunCtrl;
use Synergy\Controller\AssignmentCtrl;
use Synergy\Misc\HTTP;
use Synergy\Model\Exception\CorruptedAssignmentException;

require_once '../setup/conf.php';

switch ($_SERVER['REQUEST_METHOD']) {
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
            HTTP::Conflict("Record was modified after this request was made ");
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
            $aCtrl = new AssignmentCtrl();
            $aCtrl->updateIssues($data, intval($_REQUEST['id']));
            HTTP::OK('');
        } catch (CorruptedAssignmentException $exc) {
            HTTP::BadRequest($exc->title.": ".$exc->message);
        }
        break;
    default:
        HTTP::MethodNotAllowed('');
        break;
}
?>