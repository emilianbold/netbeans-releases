<?php

namespace Synergy\Controller\Test;

use Synergy\Controller\StatisticsCtrl;
use Synergy\DB\Test\FixtureTestCase;

/**
 * Description of StatisticsCtrlTest
 *
 * @author vriha
 */
class StatisticsCtrlTest extends FixtureTestCase {

    /**
     * @var StatisticsCtrl
     */
    protected $object;

    public function setUp() {
        $this->object = new StatisticsCtrl();
        parent::setUp();
    }

    public function testGetStatistics() {
        $data = $this->object->getStatistics();
        $this->assertEquals(5, $data[0]->value);
        $this->assertEquals(8, $data[1]->value);
        $this->assertEquals(28, $data[2]->value);
        $this->assertEquals(2, $data[3]->value);
        $this->assertEquals(2, $data[4]->value);
    }

}
