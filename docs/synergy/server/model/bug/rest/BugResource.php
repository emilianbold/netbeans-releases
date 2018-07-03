<?php

namespace Synergy\Model\Bug\Rest;

/**
 * Description of BugResource
 *
 * @author vriha
 */
class BugResource {

    public $id;
    public $bugId;
    public $title;
    public $resolution;
    public $reporter;
    public $priority;
    public $created;
    public $status;
    public $isStillValid;

    public static function createFromBug($bug) {
        $i = new BugResource();
        $i->id = $bug->id;
        $i->bugId = $bug->bugId;
        $i->title= $bug->title;
        $i->resolution = $bug->resolution;
        $i->reporter = $bug->reporter;
        $i->priority = $bug->priority;
        $i->created = $bug->created;
        $i->status = $bug->status;
        $i->isStillValid = $bug->isStillValid;
        return $i;
    }

    public static function createFromBugs($bugs) {
        $list = array();
        for ($i = 0, $max = count($bugs); $i < $max; $i++) {
            array_push($list, BugResource::createFromBug($bugs[$i]));
        }
        return $list;
    }

}
