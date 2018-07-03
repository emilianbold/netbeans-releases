<?php

namespace Synergy\Model\Tribe\Rest;

/**
 * Description of TribeListItemRest
 *
 * @author vriha
 */
class TribeListItemResource {

    public $id;
    public $leader_id;
    public $description;
    public $controls;
    public $ext;
    public $leaderUsername;
    public $name;

    public static function createFromTribe($tribe) {
        $i = new TribeListItemResource();
        $i->id = $tribe->id;
        $i->leader_id = $tribe->leader_id;
        $i->description = $tribe->description;
        $i->controls = $tribe->controls;
        $i->ext = $tribe->ext;
        $i->leaderUsername = $tribe->leaderUsername;
        $i->name = $tribe->name;
        return $i;
    }

    public static function createFromTribes($tribes) {
        $list = array();
        for ($i = 0, $max = count($tribes); $i < $max; $i++) {
            array_push($list, TribeListItemResource::createFromTribe($tribes[$i]));
        }
        return $list;
    }

}
