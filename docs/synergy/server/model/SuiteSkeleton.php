<?php
namespace Synergy\Model;

/**
 * Description of SuiteSkeleton
 *
 * @author lada
 */
class SuiteSkeleton {

    public $id;
    private $specification_id;
    /**
     *
     * @var TestCaseSkeleton[]
     */
    public $testCases;

    function __construct($id, $sid) {
        $this->id = $id;
        $this->testCases = array();
        $this->specification_id = $sid;
    }

}

?>
