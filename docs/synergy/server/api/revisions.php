<?php

use Synergy\Controller\RevisionCtrl;
use Synergy\Misc\HTTP;
use Synergy\Model\Revision\Rest\RevisionListItemResource;
use Synergy\Model\Revision\Rest\RevisionResource;

require_once '../setup/conf.php';

switch ($_SERVER['REQUEST_METHOD']) {
    case "GET":
        switch ($_REQUEST['mode']) {
            case "compare":
                if (!isset($_REQUEST['id1']) || !isset($_REQUEST['id2']) || !isset($_REQUEST['specification'])) {
                    HTTP::BadRequest("Missing parameters");
                    die();
                }
                 $revisionCtrl = new RevisionCtrl();
                 $data = array();
                 array_push($data, $revisionCtrl->getRevisionById(intval($_REQUEST['id1']), intval($_REQUEST['specification'])));
                 array_push($data, $revisionCtrl->getRevisionById(intval($_REQUEST['id2']), intval($_REQUEST['specification'])));
                 HTTP::OK(json_encode(RevisionResource::createFromRevisions($data)), 'Content-type: application/json');
                break;

            default:
                if (!isset($_REQUEST['id'])) {
                    HTTP::BadRequest("Missing parameters");
                    die();
                }

                $revisionCtrl = new RevisionCtrl();
                $revisions = $revisionCtrl->getListOfRevisions(intval($_REQUEST['id']));
                HTTP::OK(json_encode(RevisionListItemResource::createFromRevisions($revisions)), 'Content-type: application/json');
                break;
        }

        break;
    default:
        HTTP::MethodNotAllowed('');
        break;
}