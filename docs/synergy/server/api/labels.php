<?php

use Synergy\Controller\CaseCtrl;
use Synergy\Controller\LabelCtrl;
use Synergy\Controller\SpecificationCtrl;
use Synergy\Controller\SuiteCtrl;
use Synergy\Misc\HTTP;
use Synergy\Model\TestCase;
use Synergy\Model\Label\Rest\LabelResource;

require_once '../setup/conf.php';

switch ($_SERVER['REQUEST_METHOD']) {
    case "POST":
        $data = json_decode(file_get_contents("php://input"));
        if (!isset($_REQUEST["id"]) || !isset($data->label)) {
            HTTP::BadRequest("Missing suite ID or label");
            die();
        }
        $suiteId = intval($_REQUEST["id"]);

        if (!TestCase::canEdit($suiteId)) {
            HTTP::Unauthorized("");
            die();
        }

        $suiteCtrl = new SuiteCtrl();
        $specificationCtrl = new SpecificationCtrl();
        $specificationId = $suiteCtrl->getSpecificationId($suiteId);
        if (!$specificationCtrl->isSpecificationUsed($specificationId)) {
            $cases = $suiteCtrl->getTestCasesIds($suiteId);
            $caseCtrl = new CaseCtrl();
            for ($i = 0, $max = count($cases); $i < $max; $i++) {
                $caseCtrl->addLabel(strtolower($data->label), $cases[$i]);
            }
            HTTP::OK("Done");
        } else {
            HTTP::BadRequest("Specification is used in paused assignment and cannot be updated right now");
        }

        break;
    case "PUT":
        $data = json_decode(file_get_contents("php://input"));
        if (!isset($_REQUEST["id"]) || !isset($data->label)) {
            HTTP::BadRequest("Missing suite ID or label");
            die();
        }
        $suiteId = intval($_REQUEST["id"]);

        if (!TestCase::canEdit($suiteId)) {
            HTTP::Unauthorized("");
            die();
        }

        $suiteCtrl = new SuiteCtrl();
        $specificationCtrl = new SpecificationCtrl();
        $specificationId = $suiteCtrl->getSpecificationId($suiteId);
        if (!$specificationCtrl->isSpecificationUsed($specificationId)) {
            $cases = $suiteCtrl->getTestCasesIds($suiteId);
            $caseCtrl = new CaseCtrl();
            for ($i = 0, $max = count($cases); $i < $max; $i++) {
                $caseCtrl->removelabel(strtolower($data->label), $cases[$i]);
            }
            HTTP::OK("Done");
        } else {
            HTTP::BadRequest("Specification is used in paused assignment and cannot be updated right now");
        }

        break;
    case "GET":
        if (!isset($_REQUEST['label']) && !isset($_REQUEST['all'])) {
            HTTP::BadRequest("Missing parameters");
            die();
        }
        $ctrl = new LabelCtrl();
        if (isset($_REQUEST['all'])) {
            $suggestions = $ctrl->getAllLabels();
        } else {
            $suggestions = $ctrl->findMatchingLabels(strtolower($_REQUEST['label']));
        }
        HTTP::OK(json_encode(LabelResource::createFromLabels($suggestions)), 'Content-type: application/json');
        break;
    default :
        HTTP::MethodNotAllowed("");
        break;
}
?>
