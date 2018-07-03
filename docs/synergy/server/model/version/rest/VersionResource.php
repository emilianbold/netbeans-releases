<?php

namespace Synergy\Model\Version\Rest;

/**
 * Description of VersionRest
 *
 * @author vriha
 */
class VersionResource {

    public $id;
    public $name;
    public $isObsolete;
    public $controls;

    public static function createFromVersion($version) {
        $i = new VersionResource();
        $i->id = $version->id;
        $i->name = $version->name;
        $i->isObsolete = $version->isObsolete;
        $i->controls = $version->controls;
        return $i;
    }

    public static function createFromVersions($versions) {
        $list = array();
        for ($i = 0, $max = count($versions); $i < $max; $i++) {
            array_push($list, VersionResource::createFromVersion($versions[$i]));
        }
        return $list;
    }

}
