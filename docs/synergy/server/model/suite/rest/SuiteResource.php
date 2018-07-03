<?php

namespace Synergy\Model\Suite\Rest;

use Synergy\Model\Testcase\Rest\CaseResource;
use Synergy\Model\Testcase\Rest\CaseSnippetResource;

/**
 * Description of SuiteResource
 *
 * @author vriha
 */
class SuiteResource extends SuiteSnippetResource {

    public $testCases;
    
    function __construct($suite, $detailed) {
        parent::__construct($suite);
        $this->testCases = ($detailed) ? CaseResource::createFromCases($suite->testCases) : CaseSnippetResource::createFromCases($suite->testCases);
    }

    
    public static function createFromSuite($suite, $detailed) {
        return new SuiteResource($suite, $detailed);
    }

    /**
     * 
     * @param type $suites
     * @param boolean $detailed if true, suites also contain full information (including product/component, description etc.) and test cases contain steps/resolution/time, otherwise only case's title/labels/actions
     * @return array
     */
    public static function createFromSuites($suites, $detailed) {
        $list = array();
        foreach ($suites as $suite) {
            array_push($list, SuiteResource::createFromSuite($suite, $detailed));
        }
        return $list;
    }

}
