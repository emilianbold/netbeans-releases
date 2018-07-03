<?php

namespace Synergy\Model\Label\Rest;

/**
 * Description of LabelResource
 *
 * @author vriha
 */
class LabelResource {

    public $id;
    public $label;

    public static function createFromLabel($label) {
        $i = new LabelResource();
        $i->id = $label->id;
        $i->label = $label->label;
        return $i;
    }

    public static function createFromLabels($labels) {
        $list = array();
        for ($i = 0, $max = count($labels); $i < $max; $i++) {
            array_push($list, LabelResource::createFromLabel($labels[$i]));
        }
        return $list;
    }

}
