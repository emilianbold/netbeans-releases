<?php

namespace Synergy\Model;

/**
 * Description of BlobTestCase
 *
 * @author vlriha
 */
class BlobTestCase {

    private $assigmentCaseData;
    private $testCase;

    public function __construct($assigmentCaseData, $testCase) {
        $this->assigmentCaseData = $assigmentCaseData;
        $this->testCase = $testCase;
    }

    public function toBlob() {
        $s = '{';
        $s .= '"id" : ' . $this->testCase->id . ',';
        $s .= '"labels" : ' . json_encode($this->testCase->keywords) . ',';
        $s .= '"name" : ' . json_encode($this->testCase->title) . ',';
        $s .= '"result" : ' . json_encode($this->assigmentCaseData->result) . ',';
        $s .= '"finished" : ' . $this->assigmentCaseData->finished . ',';
        $s .= '"issues" : ' . json_encode($this->assigmentCaseData->issue);
        $s .= '}';

        return $s;
    }

}
