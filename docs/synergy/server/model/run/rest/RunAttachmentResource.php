<?php

namespace Synergy\Model\Run\Rest;

/**
 * Description of RunAttachmentResource
 *
 * @author vriha
 */
class RunAttachmentResource {

    public $url;
    public $id;
    public $name;
    public $controls;
    public $runId;

    public static function createFromAttachment($attachment) {
        $i = new RunAttachmentResource();
        $i->id = $attachment->id;
        $i->url = $attachment->url;
        $i->name = $attachment->name;
        $i->runId = $attachment->runId;
        $i->controls = $attachment->controls;
        return $i;
    }

    public static function createFromAttachments($attachments) {
        $list = array();
        for ($i = 0, $max = count($attachments); $i < $max; $i++) {
            array_push($list, RunAttachmentResource::createFromAttachment($attachments[$i]));
        }
        return $list;
    }

}
