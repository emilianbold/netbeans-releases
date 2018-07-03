<?php

use Synergy\App\Synergy;
use Synergy\Controller\CaseCtrl;
use Synergy\Misc\HTTP;
use Synergy\Model\Bug\Rest\BugResource;

require_once '../setup/conf.php';

switch ($_SERVER['REQUEST_METHOD']) {
    case 'GET':

        if (!isset($_REQUEST['id'])) {
            HTTP::BadRequest("Missing parameters");
            die();
        }
        $issueCtrl = Synergy::getProvider("issue");
        $issue = $issueCtrl->getIssue($_REQUEST['id']);

        if (!is_null($issue)) {
            HTTP::OK(json_encode(BugResource::createFromBug($issue)), 'Content-type: application/json');
        } else {
            HTTP::NotFound('Issue not found');
        }
        break;
    case 'POST':
        $put = file_get_contents('php://input');
        $data = json_decode($put);
        if (!isset($data->testCaseId) || !isset($data->id)) {
            HTTP::BadRequest("Missing parameters");
            die();
        }
        $caseCtrl = new CaseCtrl();

        if ($caseCtrl->isCaseInUsedSpecification(intval($data->testCaseId))) {
            HTTP::BadRequest("Specification is used in paused assignment, no changes are allowed until the assignment is finished");
            die();
        }

        if ($caseCtrl->addIssue($data->id, intval($data->testCaseId))) {
            HTTP::OK("");
        } else {
            HTTP::BadRequest("Issue not added, maybe already exists in case?");
        }
        break;
    case 'PUT':
        $put = file_get_contents('php://input');
        $data = json_decode($put);
        if (!isset($data->id) || !isset($data->testCaseId)) {
            HTTP::BadRequest("Missing parameters");
            die();
        }
        $caseCtrl = new CaseCtrl();

        if ($caseCtrl->isCaseInUsedSpecification(intval($data->testCaseId))) {
            HTTP::BadRequest("Specification is used in paused assignment, no changes are allowed until the assignment is finished");
            die();
        }

        if ($caseCtrl->removeIssue($data->id, intval($data->testCaseId))) {
            HTTP::OK("");
        } else {
            HTTP::InternalServerError("");
        }
        break;
    default :
        HTTP::MethodNotAllowed('');
        break;
}
?>
