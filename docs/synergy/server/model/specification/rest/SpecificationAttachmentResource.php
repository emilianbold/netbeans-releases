<?php

namespace Synergy\Model\Specification\Rest;

/**
 * Description of SpecificationAttachmentResource
 *
 * @author vriha
 */
class SpecificationAttachmentResource {

    public $url;
    public $id;
    public $name;
    public $controls;

    public static function createFromAttachment($attachment) {
        $i = new SpecificationAttachmentResource();
        $i->url = $attachment->url;
        $i->name = $attachment->name;
        $i->id = $attachment->id;
        $i->controls = $attachment->controls;
        return $i;
    }

    public static function createFromAttachments($attachments) {
        $list = array();
        for ($i = 0, $max = count($attachments); $i < $max; $i++) {
            array_push($list, SpecificationAttachmentResource::createFromAttachment($attachments[$i]));
        }
        return $list;
    }

}
