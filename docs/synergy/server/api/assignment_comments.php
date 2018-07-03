<?php

use Synergy\Controller\AssignmentCommentsCtrl;
use Synergy\Misc\HTTP;
use Synergy\Model\AssignmentComment;
use Synergy\Model\Comment\Rest\CommentsListResource;
use Synergy\Model\Exception\AssignmentCommentException;
require_once '../setup/conf.php';
switch ($_SERVER["REQUEST_METHOD"]) {
    case "GET":
        if (!isset($_REQUEST["id"])) {
            HTTP::BadRequest("Missing parameters");
            die();
        }

        $ctrl = new AssignmentCommentsCtrl();
        HTTP::OK(json_encode(CommentsListResource::create($ctrl->getComments(intval($_REQUEST["id"])))), "Content-type: application/json");
        break;
    case "PUT":
        if (!AssignmentComment::canEdit()) {
            HTTP::Unauthorized("");
            die();
        }
        
        $data = json_decode(file_get_contents("php://input"));
        $ctrl = new AssignmentCommentsCtrl();
        try{
            $ctrl->validate($data);
            $ctrl->resolveComments($data);
            HTTP::OK("");
        } catch (AssignmentCommentException $ex) {
            HTTP::BadRequest($ex->title.": ".$ex->message);
        }
        break;
    default :
        HTTP::MethodNotAllowed("Method not allowed");
        break;
}