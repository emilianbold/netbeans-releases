<?php
namespace Synergy\Model;
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * Description of TestCaseSkeleton
 *
 * @author lada
 */
class TestCaseSkeleton {
    //put your code here
    public $id;
    public $finished;
    public $result; // passed/skipped/failed
    public $duration;
    public $issue;
    
    function __construct($id) {
        $this->id = $id;
        $this->finished = 0;
        $this->result = "";
        $this->duration = -1;
        $this->issue = array();
    }

}

?>
