<?php
namespace Synergy\Model;
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * Description of SearchResult
 *
 * @author vriha
 */
class SearchResult {
   
    public $type;
    public $id;
    public $title;
    public $version;
    public $project;
    
    function __construct($type, $id, $title, $version, $project) {
        $this->type = $type;
        $this->project = $project;
        $this->id = $id;
        $this->title = $title;
        $this->version = $version;
    }

    
    
}

?>
