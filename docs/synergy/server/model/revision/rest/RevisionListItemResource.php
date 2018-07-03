<?php

namespace Synergy\Model\Revision\Rest;

/**
 * Description of RevisionListItemRest
 *
 * @author vriha
 */
class RevisionListItemResource {

    public $author;
    public $id;
    public $date;
    
    public static function createFromRevision($revision) {
        $i = new RevisionListItemResource();
        $i->author = $revision->author;
        $i->id = $revision->id;
        $i->date= $revision->date;
        return $i;
    }

    public static function createFromRevisions($revisions) {
        $list = array();
        for ($i = 0, $max = count($revisions); $i < $max; $i++) {
            array_push($list, RevisionListItemResource::createFromRevision($revisions[$i]));
        }
        return $list;
    }

}
