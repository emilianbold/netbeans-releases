<?php

use Synergy\Controller\SearchCtrl;
use Synergy\Misc\HTTP;
use Synergy\Model\Search\Rest\SearchResultResource;

require_once '../setup/conf.php';

if (isset($_REQUEST['search'])) {
    $searchCtrl = new SearchCtrl();
    $specificationsLimit = 100;
    $suitesLimit = 100;
    if (isset($_REQUEST["specifications"]) && !is_null($_REQUEST["specifications"])) {
        $specificationsLimit = intval($_REQUEST["specifications"]);
    }
    if (isset($_REQUEST["suites"]) && !is_null($_REQUEST["suites"])) {
        $suitesLimit = intval($_REQUEST["suites"]);
    }

    $results = SearchResultResource::createFromResults($searchCtrl->search($_REQUEST['search'], $specificationsLimit, $suitesLimit));
    HTTP::OK(json_encode($results), 'Content-type: application/json');
} else {
    HTTP::BadRequest('Missing search parameter');
}
?>
