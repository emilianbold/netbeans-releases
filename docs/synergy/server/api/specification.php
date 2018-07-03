<?php

use Synergy\App\Synergy;
use Synergy\Controller\Mediator;
use Synergy\Controller\SpecificationCtrl;
use Synergy\Controller\UserCtrl;
use Synergy\Controller\VersionCtrl;
use Synergy\Misc\HTTP;
use Synergy\Model\Exception\SpecificationDuplicateException;
use Synergy\Model\Specification;
use Synergy\Model\Specification\Rest\SpecificationResource;
use Synergy\Misc\Util;

require_once '../setup/conf.php';

switch ($_SERVER['REQUEST_METHOD']) {

    case "GET":
        if (!isset($_REQUEST['id'])) {
            HTTP::BadRequest("Missing parameters");
            die();
        }
        $view = (isset($_REQUEST["view"])) ? $_REQUEST['view'] : "";

        switch ($view) {
            case "cont":
                $specCtrl = new SpecificationCtrl();
                $specification = $specCtrl->getSpecificationFull(intval($_REQUEST['id']));
                if (is_null($specification)) {
                    HTTP::NotFound("Specification not found");
                    die();
                }

                if (Synergy::getSessionProvider()->sessionExists()) {
                    $role = Synergy::getSessionProvider()->getUserRole();
                    $specification->addControls($role);
                    foreach ($specification->testSuites as $ts) {
                        $ts->addControls($role);
                    }

                    foreach ($specification->attachments as $at) {
                        $at->addControls($role);
                    }
                }
                $specification->setIsUsed($specCtrl->isSpecificationUsed($specification->id));
                HTTP::OK(json_encode(SpecificationResource::createFromSpecification($specification, true)), 'Content-type: application/json');
                break;
            case "contAlias":
                $specCtrl = new SpecificationCtrl();

                if (isset($_REQUEST['simpleVersion']) && !is_null($_REQUEST['simpleVersion']) && strlen($_REQUEST['simpleVersion']) > 0 && $_REQUEST['simpleVersion']!== "latest") {
                    $versionCtrl = new VersionCtrl();
                    $version = $versionCtrl->getVersionByName($_REQUEST['simpleVersion']);
                    $specification = $specCtrl->getSpecificationFullByAlias($_REQUEST['simpleName'], $version->id);
                } else {
                    $specification = $specCtrl->getSpecificationFullByAlias($_REQUEST['simpleName'], -1);
                }

               
                if (is_null($specification)) {
                    HTTP::NotFound("Specification not found");
                    die();
                }
                
                if (Synergy::getSessionProvider()->sessionExists()) {
                    $role = Synergy::getSessionProvider()->getUserRole();
                    $specification->addControls($role);
                    foreach ($specification->testSuites as $ts) {
                        $ts->addControls($role);
                    }

                    foreach ($specification->attachments as $at) {
                        $at->addControls($role);
                    }
                }
                $specification->setIsUsed($specCtrl->isSpecificationUsed($specification->id));
                HTTP::OK(json_encode(SpecificationResource::createFromSpecification($specification, true)), 'Content-type: application/json');

                break;
            default :
                $specCtrl = new SpecificationCtrl();
                $specification = $specCtrl->getSpecification(intval($_REQUEST['id']), '', -1);
                if (is_null($specification)) {
                    HTTP::NotFound("Specification not found");
                    die();
                }

                if (Synergy::getSessionProvider()->sessionExists()) {
                    $role = Synergy::getSessionProvider()->getUserRole();
                    $specification->addControls($role);
                    foreach ($specification->testSuites as $ts) {
                        $ts->addControls($role);
                    }

                    foreach ($specification->attachments as $at) {
                        $at->addControls($role);
                    }
                }
                $specification->setIsUsed($specCtrl->isSpecificationUsed($specification->id));
                HTTP::OK(json_encode(SpecificationResource::createFromSpecification($specification, false)), 'Content-type: application/json');
                break;
        }



        break;

    case "PUT":

        $data = json_decode(file_get_contents('php://input'));

        if (!isset($data->id) || !isset($data->title) || !isset($data->desc) || !isset($data->owner) || !isset($data->simpleName)) {
            HTTP::BadRequest("Missing parameters");
            die();
        }
        
        if (!Specification::canEdit(intval($data->id))) {
            HTTP::Unauthorized("");
            die();
        }
        
        $keepSimpleName = true;
        if(isset($_REQUEST['keepSimpleName']) && !is_null($_REQUEST['keepSimpleName']) && strtolower($_REQUEST['keepSimpleName'])=== "false"){
            $keepSimpleName = false;
        }
        
        $specCtrl = new SpecificationCtrl();       
        $usersCtrl = new UserCtrl();
        
        $originalOwnerId = $specCtrl->getOwnerId(intval($data->id));
        $newOwnerId = $usersCtrl->getUserIDbyUsername($data->owner);
        
        if($originalOwnerId === Synergy::getSessionProvider()->getUserId() || Util::isAuthorized("admin") || Util::isAuthorized("manager")){
            $specification = new Specification(intval($data->id), $data->desc, $data->title, -1, -1, $newOwnerId);
        }else{
            $specification = new Specification(intval($data->id), $data->desc, $data->title, -1, -1, $originalOwnerId);
        }
        
        
        $specification->simpleName = $data->simpleName;
        if (isset($data->ext)) {
            $specification->ext = $data->ext;
        }

        try {
            $r = $specCtrl->updateSpecification($specification, $keepSimpleName);
            Mediator::emit("addRevision", $specification->id);
            Mediator::emit("updateProjectSpecification", array("specificationId" => $specification->id, "projects" => $data->ext->projects));
            if ($r) {
                HTTP::OK("");
            } else {
                HTTP::InternalServerError("");
            }
        } catch (SpecificationDuplicateException $ex) {
            HTTP::BadRequest($ex->message);
        }


        break;
    case "POST":
        if (!Specification::canCreate()) {
            HTTP::Unauthorized("");
            die();
        }
        $specCtrl = new SpecificationCtrl();
        $mode = '';
        if (isset($_REQUEST['mode']))
            $mode = $_REQUEST['mode'];

        switch ($mode) {
            case "create":
                $put = file_get_contents('php://input');
                $data = json_decode($put);
                if (!isset($data->title) || !isset($data->desc) || !isset($data->version) || !isset($data->simpleName)) {
                    HTTP::BadRequest("Missing parameters");
                    die();
                }
                $specification = new Specification(-1, $data->desc, $data->title, -1, -1, -1);
                $specification->simpleName = $data->simpleName;
                if (!Synergy::getSessionProvider()->sessionExists() || (defined('ANONYM') && Synergy::getSessionProvider()->getUsername() === ANONYM)) {
                    $specification->author = $data->author;
                } else {
                    $specification->author = Synergy::getSessionProvider()->getUsername();
                }
                $specification->version = $data->version;
                if (isset($data->ext)) {
                    $specification->ext = $data->ext;
                }

                try {
                    $id = $specCtrl->createSpecification($specification, false);
                } catch (SpecificationDuplicateException $ex) {
                    HTTP::BadRequest($ex->message);
                }

                break;
            case "clone":
                $put = file_get_contents('php://input');
                $data = json_decode($put);
                if (isset($_REQUEST['id']) && isset($data->version) && isset($data->newName)) {
                    try {
                        $id = $specCtrl->cloneSpecification(intval($_REQUEST['id']), $data->version, $data->newName);
                    } catch (SpecificationDuplicateException $ex) {
                        HTTP::BadRequest($ex->message);
                        die();
                    }
                } else {
                    $id = -1;
                }
                break;
            default:
                break;
        }

        if ($id > -1) {
            $url = BASER_URL . "specification.php?id=" . $id;
            HTTP::OK(json_encode($url), 'Content-type: application/json');
        } else {
            HTTP::InternalServerError('');
        }
        break;
    case "DELETE":
        
        if (!isset($_REQUEST['id'])) {
            HTTP::BadRequest("Missing parameters");
            die();
        }

        if (!Specification::canDelete(intval($_REQUEST["id"]))) {
            HTTP::Unauthorized("");
            die();
        }
        $specCtrl = new SpecificationCtrl();
        if ($specCtrl->isSpecificationUsed(intval($_REQUEST["id"]))) {
            HTTP::BadRequest("Specification is used in paused assignment, no changes are allowed until the assignment is finished");
            die();
        }

        if ($specCtrl->deleteSpecification(intval($_REQUEST['id']))) {
            Mediator::emit("removeRevisions", intval($_REQUEST['id']));
            HTTP::OK("");
        } else { // removal request created
            HTTP::Accepted("Request to remove specification has been sent to the owner");
        }

        break;
    default :
        HTTP::MethodNotAllowed("");
        break;
}
?>