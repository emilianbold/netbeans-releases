<?php

namespace Synergy\Model\Run\Rest;

/**
 * Description of RunListItemRest
 *
 * @author vriha
 */
class RunListItemResource {

    public $membersCount;
    public $completed;
    public $total;
    public $title;
    public $id;
    public $start;
    public $end;
    public $status;
    public $controls;
    public $isActive;
    public $projectName;

    /**
     * 
     * @param \Synergy\Model\TestRun $testRun
     * @return RunListItemResource
     */
    public static function createFromTestRun($testRun) {
        $i = new RunListItemResource();
        $i->membersCount = $testRun->membersCount;
        $i->completed = $testRun->completed;
        $i->total = $testRun->total;
        $i->title = $testRun->title;
        $i->id = $testRun->id;
        $i->start = $testRun->start;
        $i->end = $testRun->end;
        $i->status = $testRun->status;
        $i->controls = $testRun->controls;
        $i->isActive = $testRun->isActive;
        $i->projectName = $testRun->projectName;
        return $i;
    }

    public static function createFromTestRuns($runs) {
        $list = array();
        for ($i = 0, $max = count($runs); $i < $max; $i++) {
            array_push($list, RunListItemResource::createFromTestRun($runs[$i]));
        }
        return $list;
    }

}
