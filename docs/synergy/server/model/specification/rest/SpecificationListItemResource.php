<?php

namespace Synergy\Model\Specification\Rest;

/**
 * Description of SpecificationListItem
 *
 * @author vriha
 */
class SpecificationListItemResource {

    public $title;
    public $simpleName;
    public $id;
    public $version;
    public $owner;
    public $ownerRole;
    public $ext;

    /**
     * Creates instance of SpecificationListItem from specification 
     * @param \Synergy\Model\Specification $specification
     */
    public static function createFromSpecification($specification) {
        $i = new SpecificationListItemResource();
        $i->title = $specification->title;
        $i->simpleName = $specification->simpleName;
        $i->id = $specification->id;
        $i->version = $specification->version;
        $i->owner = $specification->owner;
        $i->ownerRole = $specification->ownerRole;
        $i->ext = $specification->ext;  
        return $i;
    }

    public static function createFromSpecifications($specifications) {
        $list = array();
        for ($i = 0, $max = count($specifications); $i < $max; $i++) {
            array_push($list, SpecificationListItemResource::createFromSpecification($specifications[$i]));
        }
        return $list;
    }

}
