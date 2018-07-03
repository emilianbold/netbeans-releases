<?php

namespace Synergy\Model\Testcase\Rest;

/**
 * Description of CaseListItemResource
 *
 * @author vriha
 */
class CaseListItemResource {

    public $id;
    public $suiteId;
    public $title;
    public $suiteTitle;

    public static function createFromCase($testCase) {
        $i = new CaseListItemResource();
        $i->id = $testCase->id;
        $i->suiteId = $testCase->suiteId;
        $i->suiteTitle = $testCase->suiteTitle;
        $i->title = $testCase->title;
        return $i;
    }

    public static function createFromCases($testCases) {
        $list = array();
        foreach ($testCases as $key => $value) {
            $list[$key] = CaseListItemResource::createFromCase($value);
        }
        return $list;
    }

}
