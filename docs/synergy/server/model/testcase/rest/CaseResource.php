<?php

namespace Synergy\Model\Testcase\Rest;

use Synergy\Model\Bug\Rest\BugResource;
use Synergy\Model\Image\Rest\ImageResource;

/**
 * Description of CaseResource
 *
 * @author vriha
 */
class CaseResource extends CaseSnippetResource {

    public $duration;
    public $originalDuration;
    public $issues;
    public $steps;
    public $result;
    public $images;

    function __construct($testCase) {
        parent::__construct($testCase);
        $this->duration = $testCase->duration;
        $this->originalDuration = $testCase->originalDuration;
        $this->issues = BugResource::createFromBugs($testCase->issues);
        $this->steps = $testCase->steps;
        $this->result = $testCase->result;
        $this->images = ImageResource::createFromImages($testCase->images);
    }

    public static function createFromCase($testCase) {
        return new CaseResource($testCase);
    }

    public static function createFromCases($cases) {
        $list = array();
        foreach ($cases as $testCase) {
            array_push($list, CaseResource::createFromCase($testCase));
        }
        return $list;
    }

}
