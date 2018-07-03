<?php

namespace Synergy\Model\Image\Rest;

/**
 * Description of ImageResource
 *
 * @author vriha
 */
class ImageResource {

    public $url;
    public $src;
    public $id;
    public $name;
    public $title;
    public $controls;

    public static function createFromImage($image) {
        $i = new ImageResource();
        $i->url = $image->url;
        $i->src = $image->src;
        $i->name = $image->name;
        $i->id = $image->id;
        $i->title = $image->title;
        $i->controls = $image->controls;
        return $i;
    }

    public static function createFromImages($images) {
        $list = array();
        for ($i = 0, $max = count($images); $i < $max; $i++) {
            array_push($list, ImageResource::createFromImage($images[$i]));
        }
        return $list;
    }

}
