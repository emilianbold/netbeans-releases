<?php

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

namespace Synergy\Model;

/**
 * Description of TestRunStatistics
 *
 * @author vriha
 */
class TestRunStatistics {
    public $issues;
    public $testRun;
    public $assigneesOverview;
    public $reviews;
    public $tribes;
    
    function __construct($issues, $testRun) {
        $this->issues = $issues;
        $this->testRun = $testRun;
        $this->assigneesOverview = array();
        $this->reviews = array();
    }

}
