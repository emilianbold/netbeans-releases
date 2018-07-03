<?php

use Synergy\App\Synergy;
use Synergy\Controller\CaseCtrl;
use Synergy\Controller\Mediator;
use Synergy\Controller\SpecificationCtrl;
use Synergy\Controller\SuiteCtrl;
use Synergy\Misc\HTTP;
use Synergy\Model\TestCase;
use Synergy\Model\Testcase\Rest\CaseResource;

require_once '../setup/conf.php';

switch ($_SERVER['REQUEST_METHOD']) {
    case "GET":

        if (!isset($_REQUEST['id']) || !isset($_REQUEST['suite'])) {
            HTTP::BadRequest("Missing parameters");
            die();
        }

        $caseCtrl = new CaseCtrl();
        $case = $caseCtrl->getCase(intval($_REQUEST['id']), intval($_REQUEST['suite']));
        if (is_null($case)) {
            HTTP::NotFound("Case not found");
            die();
        }

        if (Synergy::getSessionProvider()->sessionExists() && intval($_REQUEST['suite']) > 0) {
            $role = Synergy::getSessionProvider()->getUserRole();
            $case->suiteId = intval($_REQUEST["suite"]);
            $case->addControls($role);
            foreach ($case->images as $i) {
                $i->addControls($role, $case->suiteId);
            }
        }
        $suiteCtrl = new SuiteCtrl();
        $specCtrl = new SpecificationCtrl();
        $specificationId = $suiteCtrl->getSpecificationId(intval($_REQUEST['suite']));
        if ($specCtrl->isSpecificationUsed($specificationId)) {
            $case->disableEditActions();
        }
        HTTP::OK(json_encode(CaseResource::createFromCase($case)), 'Content-type: application/json');
        break;
    case "POST":
        $put = file_get_contents('php://input');
        $data = json_decode($put);

        if (!isset($data->suiteId) || !isset($data->order) || !isset($data->title) || !isset($data->steps) || !isset($data->result) || !isset($data->duration)) {
            HTTP::BadRequest("Missing parameters");
            die();
        }

        if (!TestCase::canCreate(intval($data->suiteId))) {
            HTTP::Unauthorized("");
            die();
        }

        $caseCtrl = new CaseCtrl();
        $testCase = new TestCase($data->title, intval($data->duration), -1);
        $testCase->steps = $data->steps;
        $testCase->result = $data->result;
        $testCase->order = intval($data->order);
        $testCase->suiteId = intval($data->suiteId);
        $specCtrl = new SpecificationCtrl();
        $suiteCtrl = new SuiteCtrl();
        $specificationId = $suiteCtrl->getSpecificationId($testCase->suiteId);
        if ($specCtrl->isSpecificationUsed($specificationId)) {
            HTTP::BadRequest("Specification is used in paused assignment, no changes are allowed until the assignment is finished");
            die();
        }

        if (isset($data->ext)) {
            $testCase->ext = $data->ext;
        }
        $id = $caseCtrl->createCase($testCase);
        Mediator::emit("addRevisionSuite", $testCase->suiteId);
        Mediator::emit("specificationUpdated", $specificationId);
        if ($id > -1) {
            $url = BASER_URL . "case.php?suite=-1&id=" . $id;
            HTTP::OK(json_encode($url), 'Content-type: application/json');
        } else {
            HTTP::InternalServerError('');
        }
        break;
    case "PUT":
        $put = file_get_contents('php://input');
        $data = json_decode($put);
        if (!isset($_REQUEST['id']) || !isset($data->id) || !isset($data->order) || !isset($data->title) || !isset($data->steps) || !isset($data->result) || !isset($data->duration) || !isset($data->orginalDuration) || !isset($data->suiteId)) {
            HTTP::BadRequest("Missing parameters");
            die();
        }

        if (!TestCase::canEdit(intval($data->suiteId))) {
            HTTP::Unauthorized("");
            die();
        }

        $mode = 0;
        if (isset($_REQUEST['mode'])) {
            $mode = intval($_REQUEST['mode']);
        }
        $caseCtrl = new CaseCtrl();
        $testCase = new TestCase($data->title, intval($data->duration), intval($_REQUEST['id']));
        $testCase->steps = $data->steps;
        $testCase->result = $data->result;
        $testCase->order = intval($data->order);
        $testCase->originalDuration = intval($data->orginalDuration);
        $testCase->suiteId = intval($data->suiteId);

        $specCtrl = new SpecificationCtrl();
        $suiteCtrl = new SuiteCtrl();
        $specificationId = $suiteCtrl->getSpecificationId($testCase->suiteId);


        if ($specCtrl->isSpecificationUsed($specificationId)) {
            $mode = 1;
            $testCase->order = null;
        }

        if (isset($data->ext)) {
            $testCase->ext = $data->ext;
        }
        if ($caseCtrl->updateCase($testCase, $mode)) {
            Mediator::emit("addRevisionCase", intval($_REQUEST['id']));
            Mediator::emit("caseUpdated", intval($_REQUEST['id']));
            Mediator::emit("specificationUpdated", $specificationId);
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