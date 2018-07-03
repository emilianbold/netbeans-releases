<?php
namespace Synergy\Extensions\Specification;
use Synergy\Interfaces\ExtensionInterface;



/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * Test extension
 *
 * @author vriha
 */
class TestExtension implements ExtensionInterface {

    public function create($object, $newId) {
        
    }

    public function delete($deletedId) {
        
    }

    public function edit($object) {
        
    }

    /**
     * 
     * @param \Specification $object
     */
    public function get($object) {
        $object->ext['test'] = 1;
        return $object;
    }

}

?>
