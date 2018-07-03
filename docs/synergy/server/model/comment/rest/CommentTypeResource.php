<?php

namespace Synergy\Model\Comment\Rest;

/**
 * Description of CommentTypeResource
 *
 * @author vriha
 */
class CommentTypeResource {

    public $id;
    public $name;

    public static function createFromType($comment) {
        $i = new CommentTypeResource();
        $i->id = $comment->id;
        $i->name = $comment->name;
        return $i;
    }

    public static function createFromTypes($commentTypes) {
        $list = array();
        for ($i = 0, $max = count($commentTypes); $i < $max; $i++) {
            array_push($list, CommentTypeResource::createFromType($commentTypes[$i]));
        }
        return $list;
    }

}
