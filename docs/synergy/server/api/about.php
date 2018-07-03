<?php

use Synergy\Controller\StatisticsCtrl;
use Synergy\Misc\HTTP;
require_once '../setup/conf.php';
$ctrl = new StatisticsCtrl();
$data = $ctrl->getStatistics();
HTTP::OK(json_encode($data), 'Content-type: application/json');
?>
