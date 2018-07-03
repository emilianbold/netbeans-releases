<?php

namespace Synergy\Model;

/**
 * Description of CommentType
 *
 * @author vriha
 */
class CommentType {
    public $name;
    public $id;
    
    function __construct($name, $id) {
        $this->name = $name;
        $this->id = intval($id);
    }

    
}
