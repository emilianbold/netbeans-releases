<?php

namespace Synergy\Model\Run\Rest;

/**
 * Description of RunResource
 *
 * @author vriha
 */
class RunBlobsResource {

    public $title;
    public $desc;
    public $id;
    public $start;
    public $end;
    public $blobs;
    public $durations;
    public $projectName;
    public $projectId;

    /**
     * 
     * @param \Synergy\Model\TestRun $testRun
     * @return \Synergy\Model\Run\Rest\RunBlobsResource
     */
    public static function create($testRun) {
        $i = new RunBlobsResource();
        $i->id = $testRun->id;
        $i->title = $testRun->title;
        $i->desc = $testRun->desc;
        $i->start = $testRun->start;
        $i->projectId = $testRun->projectId;
        $i->projectName = $testRun->projectName;
        $i->end = $testRun->end;
        $i->blobs = $testRun->blobs;
        $i->durations = $testRun->durations;
        return $i;
    }

}
