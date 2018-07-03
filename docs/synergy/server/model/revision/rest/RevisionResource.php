<?php

namespace Synergy\Model\Revision\Rest;

/**
 * Description of RevisionRest
 *
 * @author vriha
 */
class RevisionResource {

    public $author;
    public $id;
    public $date;
    public $content;

    public static function createFromRevision($revision) {
        $i = new RevisionResource();
        $i->author = $revision->author;
        $i->id = $revision->id;
        $i->date = $revision->date;
        $i->content = $revision->content;
        return $i;
    }

    public static function createFromRevisions($revisions) {
        $list = array();
        for ($i = 0, $max = count($revisions); $i < $max; $i++) {
            array_push($list, RevisionResource::createFromRevision($revisions[$i]));
        }
        return $list;
    }

}
