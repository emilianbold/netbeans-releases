<?php

namespace Synergy\Model\Comment\Rest;

/**
 * Description of CommentResource
 *
 * @author vriha
 */
class CommentResource {

    public $id;
    public $assignmentId;
    public $resolution;
    public $caseId;
    public $suiteId;
    public $resolverId;
    public $resolverUsername;
    public $resolverDisplayName;
    public $commentText;
    public $commentFreeText;
    public $specificationTitle;
    public $specificationId;
    public $caseTitle;
    public $authorUsername;
    public $authorDisplayName;

    public static function createFromComment($comment) {
        $i = new CommentResource();
        $i->id = $comment->id;
        $i->assignmentId = $comment->assignmentId;
        $i->authorDisplayName = $comment->authorDisplayName;
        $i->authorUsername = $comment->authorUsername;
        $i->caseId = $comment->caseId;
        $i->caseTitle = $comment->caseTitle;
        $i->commentText = $comment->commentText;
        $i->commentFreeText = $comment->commentFreeText;
        $i->resolution = $comment->resolution;
        $i->resolverId = $comment->resolverId;
        $i->specificationId = $comment->specificationId;
        $i->specificationTitle = $comment->specificationTitle;
        $i->suiteId = $comment->suiteId;
        $i->resolverUsername = $comment->resolverUsername;
        $i->resolverDisplayName = $comment->resolverDisplayName;
        return $i;
    }

    public static function createFromComments($comments) {
        $list = array();
        for ($i = 0, $max = count($comments); $i < $max; $i++) {
            array_push($list, CommentResource::createFromComment($comments[$i]));
        }
        return $list;
    }

}
