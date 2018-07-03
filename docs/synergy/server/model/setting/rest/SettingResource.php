<?php

namespace Synergy\Model\Setting\Rest;

/**
 * Description of SettingResource
 *
 * @author vriha
 */
class SettingResource {

    public $key;
    public $value;
    public $label;

    public static function createFromSetting($setting) {
        $i = new SettingResource();
        $i->key = $setting->key;
        $i->value = $setting->value;
        $i->label = $setting->label;
        return $i;
    }

    public static function createFromSettings($settings) {
        $list = array();
        for ($i = 0, $max = count($settings); $i < $max; $i++) {
            array_push($list, SettingResource::createFromSetting($settings[$i]));
        }
        return $list;
    }

}
