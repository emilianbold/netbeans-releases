<?php

namespace Synergy\Controller;

use Synergy\Model\SearchResult;

/**
 * Description of SearchCtrl
 *
 * @author vriha
 */
class SearchCtrl {

    private $specCtrl;
    private $suiteCtrl;
    
    /**
     * Returns array of SearchResults which represents array of specifications and suites which titles match given term. 
     * @param String $term
     * @param int $limitSpecifications max number of specifications in search result
     * @param int $limitSuites max number of suites in search result
     * @return SearchResult[]
     */
    public function search($term, $limitSpecifications, $limitSuites) {
        $term = urldecode($term);
        $terms = explode(" ", $term);
        $results = array();
        $limit = 4;
        for ($i = 0, $max = count($terms); $i < $max && $i < $limit; $i++) {
            $specs = $this->getSpecCtrl()->findMatchingSpecifications($terms[$i], $limitSpecifications);
            $suites = $this->getSuiteCtrl()->findMatchingSuites($terms[$i], $limitSuites); // TODO fix project missing

            foreach ($specs as $spec) {
                array_push($results, new SearchResult("specification", $spec->id, $spec->title, $spec->version, $spec->getSingleProject()));
            }

            foreach ($suites as $suite) {
                array_push($results, new SearchResult("suite", $suite->id, $suite->title, "", ""));
            }
        }


        return $results;
    }

    /**
     * 
     * @return SpecificationCtrl
     */
    private function getSpecCtrl() {
        if (is_null($this->specCtrl)) {
            $this->specCtrl = new SpecificationCtrl();
        }
        return $this->specCtrl;
    }

    /**
     * 
     * @return SuiteCtrl
     */
    private function getSuiteCtrl() {
        if (is_null($this->suiteCtrl)) {
            $this->suiteCtrl = new SuiteCtrl();
        }
        return $this->suiteCtrl;
    }

}

?>
