<?php
namespace Synergy\Model;
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * Description of Bug
 *
 * @author lada
 */
class Bug {
   
    public $id;
    public $bugId;
    public $title='';
    public $resolution='';
    public $reporter;
    public $priority;
    public $created;
    public $status;
    public $isStillValid;
    
    function __construct($id, $bugId) {
        $this->id = intval($id);
        $this->bugId = $bugId;
        $this->title = "";
        $this->resolution = "";
        $this->isStillValid = true;
    }
    
    public static function createBug($bugId, $reporter, $priority, $created, $status, $resolution){
        $instance = new self;
        $instance->bugId = $bugId;
        $instance->title = "";
        $instance->status = $status;
        $instance->resolution =$resolution;
        if(!strpos($reporter, "@")){
            $instance->reporter = $reporter;
        }else{
            $instance->reporter = substr($reporter, 0, strpos($reporter, "@"));    
        }
        
        $instance->priority = $priority;
        $instance->created = $created;
        return $instance;
    }

}

?>
