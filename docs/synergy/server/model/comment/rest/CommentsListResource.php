<?php

namespace Synergy\Model\Comment\Rest;

/**
 * Description of CommentsListResource
 *
 * @author vriha
 */
class CommentsListResource {

    public $comments;
    public $testRunId;
    public $testRunTitle;

    public static function create($data) {
        $i = new CommentsListResource();
        $i->testRunId = $data->testRunId;
        $i->testRunTitle = $data->testRunTitle;
        $i->comments = CommentResource::createFromComments($data->comments);
        return $i;
    }

}
