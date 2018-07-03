<?php
namespace Synergy\Interfaces;

use Synergy\Model\Suite;
use Synergy\Model\TestCase;
use Synergy\Model\Tribe;
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * Extension interface. All extensions must implement it. 
 * @author vriha
 */
interface ExtensionInterface {

    /**
     * Receives instance of Specification|Suite|TestCase|Tribe class, modifies it somehow (if you want to add new information/properties, add a 
     * new key to associative array $ext of these classes. You can modify all object's properties, but be carefull. After the new information is added,
     * return the modified object. It is called at last after just before controller would return found object
     * @param \Specification|Suite|TestCase|Tribe $object
     * @return \Specification|Suite|TestCase|Tribe Modified object, new information should be added to $ext property
     */
    public function get($object);

    /**
     * Receives instance of Specification|Suite|TestCase|Tribe class. It is called after controller edits given object
     * @param \Specification|Suite|TestCase|Tribe $object
     */
    public function edit($object);

    /**
     * It is called after a new object is created by controllers
     * @param \Specification|Suite|TestCase|Tribe $object
     * @param int $newId id of the new created object in database
     */
    public function create($object, $newId);

    /**
     * It is called before a object is deleted from database. The reason why before is to allow extension to remove possible db records and avoid
     * problems with foreign keys, dependencies...
     * @param int $deletedId id of the new created object in database
     */
    public function delete($deletedId);
}

?>
