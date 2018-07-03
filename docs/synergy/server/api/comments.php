<?php

use Synergy\Controller\CommentsCtrl;
use Synergy\Misc\HTTP;
use Synergy\Model\Comment\Rest\CommentTypeResource;

require_once '../setup/conf.php';

$ctrl = new CommentsCtrl();
HTTP::OK(json_encode(CommentTypeResource::createFromTypes($ctrl->getCommentTypes())), "Content-type: application/json");

