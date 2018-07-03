<?php

namespace Synergy\Model\Platform\Rest;

/**
 * Description of PlatformResource
 *
 * @author vriha
 */
class PlatformResource {

    public $id;
    public $name;
    public $isActive;
    public $controls;

    public static function createFromPlatform($platform) {
        $i = new PlatformResource();
        $i->id = $platform->id;
        $i->name = $platform->name;
        $i->isActive = $platform->isActive;
        $i->controls = $platform->controls;
        return $i;
    }

    public static function createFromPlatforms($platforms) {
        $list = array();
        for ($i = 0, $max = count($platforms); $i < $max; $i++) {
            array_push($list, PlatformResource::createFromPlatform($platforms[$i]));
        }
        return $list;
    }

}
