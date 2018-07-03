<?php

use Synergy\App\Synergy;
use Synergy\Controller\CaseCtrl;
use Synergy\Controller\Mediator;
use Synergy\Controller\SpecificationCtrl;
use Synergy\Controller\SuiteCtrl;
use Synergy\Misc\HTTP;
use Synergy\Model\Suite;
use Synergy\Model\Suite\Rest\SuiteResource;

require_once '../setup/conf.php';

switch ($_SERVER['REQUEST_METHOD']) {

    case "GET":

        if (!isset($_REQUEST['id'])) {
            HTTP::BadRequest("Missing parameters");
            die();
        }
        $suiteCtrl = new SuiteCtrl();
        $suite = $suiteCtrl->getSuite(intval($_REQUEST['id']));
        if (is_null($suite)) {
            HTTP::NotFound("Suite not found");
            die();
        }

        if (Synergy::getSessionProvider()->sessionExists()) {
            $role = Synergy::getSessionProvider()->getUserRole();
            $suite->addControls($role);

            foreach ($suite->testCases as $at) {
                $at->addControls($role);
            }
        }
        $specCtrl = new SpecificationCtrl();
        if ($specCtrl->isSpecificationUsed($suite->specificationId)) {
            $suite->disableEditActions();
        }
        HTTP::OK(json_encode(SuiteResource::createFromSuite($suite, false)), 'Content-type: application/json');
        break;

    case "PUT":

        if (!Suite::canEdit(intval($_REQUEST['id']))) {
            HTTP::Unauthorized("");
            die();
        }

        $suiteCtrl = new SuiteCtrl();
        $specCtrl = new SpecificationCtrl();
        $specificationId = $suiteCtrl->getSpecificationId(intval($_REQUEST["id"]));
        $action = "default";
        if (isset($_REQUEST['action'])) {
            $action = $_REQUEST['action'];
        }

        switch ($action) {

            case "addCase":

                if ($specCtrl->isSpecificationUsed($specificationId)) {
                    HTTP::BadRequest("Specification is used in paused assignment, no changes are allowed until the assignment is finished");
                    die();
                }

                if (!isset($_REQUEST['id']) || !isset($_REQUEST['caseId'])) {
                    HTTP::BadRequest("Missing parametersl");
                    die();
                }
                if ($suiteCtrl->suiteAlreadyHasCase(intval($_REQUEST['caseId']), intval($_REQUEST['id']))) {
                    HTTP::BadRequest("Suite already has this case");
                    die();
                }

                if ($suiteCtrl->addCaseToSuite(intval($_REQUEST['caseId']), intval($_REQUEST['id']))) {
                    Mediator::emit("addRevisionSuite", intval($_REQUEST['id']));
                    HTTP::OK("");
                } else {
                    HTTP::InternalServerError("");
                }

                break;

            case "deleteCase":

                if ($specCtrl->isSpecificationUsed($specificationId)) {
                    HTTP::BadRequest("Specification is used in paused assignment, no changes are allowed until the assignment is finished");
                    die();
                }

                if (!isset($_REQUEST['id']) || !isset($_REQUEST['caseId'])) {
                    HTTP::BadRequest("Missing parametersl");
                    die();
                }
                $caseCtrl = new CaseCtrl();

                if ($caseCtrl->removeCaseFromSuite(intval($_REQUEST['id']), intval($_REQUEST['caseId']))) {
                    Mediator::emit("addRevisionSuite", intval($_REQUEST['id']));
                    HTTP::OK("");
                } else {
                    HTTP::InternalServerError("");
                }
                break;


            default:
                $put = file_get_contents('php://input');

                $data = json_decode($put);
                if (!isset($_REQUEST['id']) || !isset($data->order) || !isset($data->id) || !isset($data->title) || !isset($data->desc) || !isset($data->product) || !isset($data->component)) {
                    HTTP::BadRequest("Missing parameters");
                    die();
                }
                $suite = new Suite(intval($_REQUEST['id']), $data->desc, $data->title, $data->product, $data->component, -1, $data->order);
                if (isset($data->ext)) {
                    $suite->ext = $data->ext;
                }

                if ($specCtrl->isSpecificationUsed($specificationId)) {
                    $suite->order = null;
                }

                if ($suiteCtrl->updateSuite($suite)) {
                    Mediator::emit("addRevisionSuite", $suite->id);
                    HTTP::OK("");
                } else {
                    HTTP::InternalServerError("");
                }
                break;
        }
        break;
    case "DELETE":

        if (!isset($_REQUEST['id'])) {
            HTTP::BadRequest("Missing parameters");
            die();
        }

        if (!Suite::canDelete(intval($_REQUEST['id']))) {
            HTTP::Unauthorized("");
            die();
        }

        $suiteCtrl = new SuiteCtrl();
        $specCtrl = new SpecificationCtrl();
        $specId = $suiteCtrl->getSpecificationId(intval($_REQUEST['id']));
        if ($specCtrl->isSpecificationUsed($specId)) {
            HTTP::BadRequest("Specification is used in paused assignment, no changes are allowed until the assignment is finished");
            die();
        }

        if ($suiteCtrl->deleteSuite(intval($_REQUEST['id']))) {
            Mediator::emit("addRevision", $specId);
            HTTP::OK("");
        } else {
            HTTP::InternalServerError("");
        }
        break;
    case "POST":

        $put = file_get_contents('php://input');
        $data = json_decode($put);
        if (!isset($data->specificationId) || !isset($data->title) || !isset($data->order) || !isset($data->desc) || !isset($data->product) || !isset($data->component)) {
            HTTP::BadRequest("Missing parameters");
            die();
        }

        if (!Suite::canCreate(intval($data->specificationId))) {
            HTTP::Unauthorized("");
            die();
        }

        $suiteCtrl = new SuiteCtrl();
        $suite = new Suite(-1, $data->desc, $data->title, $data->product, $data->component, intval($data->specificationId), $data->order);
        $specCtrl = new SpecificationCtrl();
        if ($specCtrl->isSpecificationUsed($suite->specificationId)) {
            HTTP::BadRequest("Specification is used in paused assignment, no changes are allowed until the assignment is finished");
            die();
        }

        if (isset($data->ext)) {
            $suite->ext = $data->ext;
        }
        $id = $suiteCtrl->createSuite($suite);
        Mediator::emit("addRevision", $suite->specificationId);
        $url = BASER_URL . "suite.php?id=" . $id;
        HTTP::OK(json_encode($url), 'Content-type: application/json');

        break;
    default :
        HTTP::MethodNotAllowed("");
        break;
}
?>
