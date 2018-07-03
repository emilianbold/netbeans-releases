<?php
namespace Synergy\Controller\Test;

use Synergy\Controller\ExtensionCtrl;
use Synergy\Model\Tribe;

/**
 * Description of TribeExtensionCtrlTest
 *
 * @author lada
 */
class TribeExtensionCtrlTest extends \PHPUnit_Framework_TestCase {

    /**
     * @var ExtensionCtrl
     */
    protected $object;

    /**
     * Sets up the fixture, for example, opens a network connection.
     * This method is called before a test is executed.
     */
    protected function setUp() {
        $this->object = new ExtensionCtrl('tribe');
    }

    /**
     * Tears down the fixture, for example, closes a network connection.
     * This method is called after a test is executed.
     */
    protected function tearDown() {
        
    }

    public function testGetSpecification() {
        $tribe = new Tribe(1, 'synergy tribe', 'desc', 2);
        /* @var $tribe2 Tribe */
        $tribe2 = $this->object->get($tribe);
        $this->assertEquals(1, $tribe2->id);
        $this->assertEquals('synergy tribe', $tribe2->name);
        $this->assertEquals('desc', $tribe2->description);
        $this->assertEquals(2, $tribe2->leader_id);
        
    }

}

?>
