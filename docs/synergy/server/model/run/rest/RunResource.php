<?php

namespace Synergy\Model\Run\Rest;

/**
 * Description of RunResource
 *
 * @author vriha
 */
class RunResource {

 
    public $title;
    public $desc;
    public $id;
    public $start;
    public $end;
    public $attachments;
    public $notifications;
    public $projectName;
    public $projectId;

    
    /**
     * 
     * @param \Synergy\Model\TestRun $testRun
     * @return \Synergy\Model\Run\Rest\RunResource
     */
    public static function create($testRun){
        $i = new RunResource();
        $i->title = $testRun->title;
        $i->desc = $testRun->desc;
        $i->start = $testRun->start;
        $i->projectId = $testRun->projectId;
        $i->projectName = $testRun->projectName;
        $i->end = $testRun->end;
        $i->notifications = $testRun->notifications;
        $i->attachments = RunAttachmentResource::createFromAttachments($testRun->attachments);
        return $i;
    }

}
