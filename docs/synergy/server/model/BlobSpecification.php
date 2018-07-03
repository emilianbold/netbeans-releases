<?php

namespace Synergy\Model;

/**
 * Description of BlobSpecification
 *
 * @author vlriha
 */
class BlobSpecification {

    private $assigmentData;
    private $specification;

    public function __construct($assigmentData, $specification) {
        $this->assigmentData = $assigmentData;
        $this->specification = $specification;
    }

    public function toBlob() {
        $s = '{';
        $s .= '"id" : ' . $this->specification->id . ',';
        $s .= '"name" : ' . json_encode($this->specification->title) . ',';
        $s .= '"version" : ' . json_encode($this->specification->version) . ',';
        $s .= '"suites" : [ ';
        for ($i = 0, $max = count($this->assigmentData->specification->testSuites); $i < $max; $i++) {
            $aSuiteId = $this->assigmentData->specification->testSuites[$i]->id;
            for ($j = 0, $maxj = count($this->specification->testSuites); $j < $maxj; $j++) {
                if ($aSuiteId === $this->specification->testSuites[$j]->id) {
                    $ts = new BlobTestSuite($this->assigmentData->specification->testSuites[$i], $this->specification->testSuites[$j]);
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
