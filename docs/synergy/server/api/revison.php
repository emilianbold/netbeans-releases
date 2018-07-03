<?php

use Synergy\Misc\HTTP;

require_once '../setup/conf.php';

switch ($_SERVER['REQUEST_METHOD']) {
    case "GET":
        break;
    default:
        HTTP::MethodNotAllowed('');
        break;
}