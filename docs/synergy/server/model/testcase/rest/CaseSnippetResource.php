<?php

namespace Synergy\Model\Testcase\Rest;

/**
 * Description of CaseSnippetResource
 *
 * @author vriha
 */
class CaseSnippetResource {

    public $title;
    public $id;
    public $version_id;
    public $keywords;
    public $controls;
    public $suiteId;
    public $url;
    public $isUsed;
    public $order;
    public $specificationTitle;
    public $specificationId;
    public $suiteTitle;
    public $version;
    public $ext;

    function __construct($testCase) {
        $this->title = $testCase->title;
        $this->id = $testCase->id;
        $this->version_id = $testCase->version_id;
        $this->keywords = $testCase->keywords;
        $this->controls = $testCase->controls;
        $this->suiteId = $testCase->suiteId;
        $this->url = $testCase->url;
        $this->isUsed = $testCase->isUsed;
        $this->order = $testCase->order;
        $this->specificationTitle = $testCase->specificationTitle;
        $this->specificationId = $testCase->specificationId;
        $this->suiteTitle = $testCase->suiteTitle;
        $this->version = $testCase->version;
        $this->ext = $testCase->ext;
    }

    public static function createFromCase($testCase) {
        return new CaseSnippetResource($testCase);
    }

    public static function createFromCases($cases) {
        $list = array();
        foreach ($cases as $testCase) {
            array_push($list, CaseSnippetResource::createFromCase($testCase));
        }
        return $list;
    }

}
