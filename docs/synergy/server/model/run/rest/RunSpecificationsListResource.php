<?php

namespace Synergy\Model\Run\Rest;

use Synergy\Model\Specification\Rest\SpecificationListItemResource;

/**
 * Description of RunSpecificationsListResource
 *
 */
class RunSpecificationsListResource {

    public $projectName;
    public $specifications;

    public static function create($specifications, $projectName) {
        $i = new RunSpecificationsListResource();
        $i->projectName = $projectName;
        $i->specifications = SpecificationListItemResource::createFromSpecifications($specifications);
        return $i;
    }

}
