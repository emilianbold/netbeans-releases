<?php
namespace Synergy\Model;
/**
 * Description of SpecificationSkeleton
 *
 * @author lada
 */
class SpecificationSkeleton {

    public $id;

    /**
     *
     * @var SuiteSkeleton[] 
     */
    public $testSuites;

    function __construct($id) {
        $this->testSuites = array();
        $this->id = $id;
    }


}

?>
