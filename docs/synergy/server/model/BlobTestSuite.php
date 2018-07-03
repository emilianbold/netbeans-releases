<?php

namespace Synergy\Model;

/**
 * Description of BlobTestSuite
 *
 * @author vlriha
 */
class BlobTestSuite {

    private $assigmentSuiteData;
    private $testSuite;

    public function __construct($assigmentSuiteData, $testSuite) {
        $this->assigmentSuiteData = $assigmentSuiteData;
        $this->testSuite = $testSuite;
    }

    public function toBlob() {
        $s = '{';
        $s .= '"id" : ' . $this->testSuite->id . ',';
        $s .= '"name" : ' . json_encode($this->testSuite->title) . ',';
        $s .= '"testCases" : [ ';
        for ($i = 0, $max = count($this->assigmentSuiteData->testCases); $i < $max; $i++) {
            $aCaseId = $this->assigmentSuiteData->testCases[$i]->id;
            
            for ($j = 0, $maxj = count($this->testSuite->testCases); $j < $maxj; $j++) {
                if ($aCaseId === $this->testSuite->testCases[$j]->id) {
                    $ts = new BlobTestCase($this->assigmentSuiteData->testCases[$i], $this->testSuite->testCases[$j]);
                    $s .= $ts->toBlob();
                    if ($j < $maxj - 1) {
                        $s .= ',';
                    }
                    break;
                }
            }
        }
        $s .= ']';
        $s .= '}';

        return $s;
    }

}
