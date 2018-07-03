<?php

namespace Synergy\Model\Tribe\Rest;

/**
 * Description of TribeRest
 *
 * @author vriha
 */
class TribeResource {

    public $id;
    public $leader_id;
    public $description;
    public $controls;
    public $ext;
    public $leaderUsername;
    public $name;
    public $leaderDisplayName;
    public $members;
    public $url;
    public $leaderImg;

    public static function createFromTribe($tribe) {
        $i = new TribeResource();
        $i->id = $tribe->id;
        $i->leader_id = $tribe->leader_id;
        $i->description = $tribe->description;
        $i->controls = $tribe->controls;
        $i->ext = $tribe->ext;
        $i->leaderUsername = $tribe->leaderUsername;
        $i->name = $tribe->name;
        $i->leaderDisplayName = $tribe->leaderDisplayName;
        $i->members = $tribe->members;
        $i->url = $tribe->url;
        $i->leaderImg = $tribe->leaderImg;
        return $i;
    }

    public static function createFromTribes($tribes) {
        $list = array();
        for ($i = 0, $max = count($tribes); $i < $max; $i++) {
            array_push($list, TribeResource::createFromTribe($tribes[$i]));
        }
        return $list;
    }

}
