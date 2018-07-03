<?php

use Synergy\Controller\CaseCtrl;
use Synergy\Controller\Mediator;
use Synergy\Misc\HTTP;
use Synergy\Model\Label\Rest\LabelSearchResource;
use Synergy\Model\TestCase;

require_once '../setup/conf.php';

switch ($_SERVER['REQUEST_METHOD']) {
    case "GET":
        if (!isset($_REQUEST['label'])) {
            HTTP::BadRequest("Missing parameters");
            die();
        }

        $label = rawurldecode($_REQUEST['label']);

        $page = 1;
        if (isset($_REQUEST['page'])) {
            $page = intval($_REQUEST['page']);
        }
        $caseCtrl = new CaseCtrl();

        $result = $caseCtrl->getCasesByFilter(strtolower($label), $page);
        if (count($result->cases) > 0) {
            HTTP::OK(json_encode(LabelSearchResource::create($result)), 'Content-type: application/json');
        } else {
            HTTP::NotFound('No results found');
        }

        break;
    case "POST":
        $data = json_decode(file_get_contents('php://input'));    

        if (!isset($data->label) || strlen($data->label) < 1 || !isset($data->testCaseId) || !isset($data->suiteId)) {
            HTTP::BadRequest("Missing parameters");
            die();
        } 
        
        if (!TestCase::canEdit(intval($data->suiteId))) {
            HTTP::Unauthorized("");
            die();
        }
        $caseCtrl = new CaseCtrl();
        if ($caseCtrl->isCaseInUsedSpecification(intval($data->testCaseId))) {
            HTTP::BadRequest("Specification is used in paused assignment, no changes are allowed until the assignment is finished");
            die();
        }
        if ($caseCtrl->addLabel(strtolower($data->label), intval($data->testCaseId))) {
            Mediator::emit("addRevisionCase", intval($data->testCaseId));
            HTTP::OK("");
        } else {
            HTTP::InternalServerError("");
        }
        break;
    case "PUT":
        $data = json_decode(file_get_contents('php://input'));
        if (!isset($data->label) || strlen($data->label) < 1 || !isset($data->testCaseId) || !isset($data->suiteId)) {
            HTTP::BadRequest("Missing parameters label");
            die();
        }
        
        if (!TestCase::canEdit(intval($data->suiteId))) {
            HTTP::Unauthorized("");
            die();
        }
        
        $caseCtrl = new CaseCtrl();

        if ($caseCtrl->isCaseInUsedSpecification(intval($data->testCaseId))) {
            HTTP::BadRequest("Specification is used in paused assignment, no changes are allowed until the assignment is finished");
            die();
        }

        if ($caseCtrl->removelabel(strtolower($data->label), intval($data->testCaseId))) {
            Mediator::emit("addRevisionCase", intval($data->testCaseId));
            HTTP::OK("");
        } else {
            HTTP::InternalServerError("");
        }
        break;
    default :
        HTTP::MethodNotAllowed("");
        break;
}
?>