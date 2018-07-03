<?php

namespace Synergy\Model\Specification\Ext;

/**
 * Description of RemovalRequest
 *
 * @author vriha
 */
class RemovalRequest {
    public $specificationId;
    public $username;
    private $id;
    
    function __construct($specificationId, $username, $id) {
        $this->specificationId = intval($specificationId);
        $this->username = $username;
        $this->id = intval($id);
    }

    
}
