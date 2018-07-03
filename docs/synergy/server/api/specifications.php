<?php

use Synergy\App\Synergy;
use Synergy\Controller\SpecificationCtrl;
use Synergy\Model\Specification\Rest\SpecificationListItemResource;
use Synergy\Controller\VersionCtrl;
use Synergy\Misc\HTTP;

require_once '../setup/conf.php';

switch ($_SERVER['REQUEST_METHOD']) {

    case "GET":

        if (isset($_REQUEST['mode'])) {
            switch ($_REQUEST['mode']) {
                case "filter":
                    if (isset($_REQUEST['query'])) {
                        $specCtrl = new SpecificationCtrl();
                        $suggestions = $specCtrl->findMatchingSpecifications($_REQUEST['query']);
                        $list = array();
                        for ($i = 0, $max = count($suggestions); $i < $max; $i++) {
                            array_push($list, SpecificationListItemResource::createFromSpecification($suggestions[$i]));
                        }

                        HTTP::OK(json_encode($list), 'Content-type: application/json');
                    } else {
                        HTTP::BadRequest('');
                    }
                    break;
                case "latest":
                    $limit = 5;
                    if (isset($_REQUEST['limit'])) {
                        $limit = intval($_REQUEST['limit']);
                    }

                    if ($limit > 20)
                        $limit = 5;
                    $specCtrl = new SpecificationCtrl();
                    $specifications = $specCtrl->getLatestSpecifications($limit);
                    $list = array();
                    for ($i = 0, $max = count($specifications); $i < $max; $i++) {
                        array_push($list, SpecificationListItemResource::createFromSpecification($specifications[$i]));
                    }
                    if (count($list) > 0) {
                        HTTP::OK(json_encode($list), "Content-type: application/json");
                    } else {
                        HTTP::NotFound('');
                    }

                    break;
                default:
                    break;
            }
        } else {
            $versionCtrl = new VersionCtrl();
            $specCtrl = new SpecificationCtrl();

            if (isset($_REQUEST['version']) && !is_null($_REQUEST['version']) && strlen($_REQUEST['version']) > 0) {
                $version = $versionCtrl->getVersionByName($_REQUEST['version']);
            } else {
                $version = $versionCtrl->getLatestVersion();
            }

            $specifications = array();
            if (is_null($version) && $_REQUEST['version'] !== "all" && $_REQUEST['version'] !== "allRaw") {
                HTTP::NotFound("Version not found");
                die();
            }
            if ($_REQUEST['version'] === "all") {
                $specifications = $specCtrl->getSpecificationsByVersion();
                for ($i = 0, $max = count($specifications); $i < $max; $i++) {
                    $list = array();
                    for ($j = 0, $max2 = count($specifications[$i]->specifications); $j < $max2; $j++) {
                        array_push($list, SpecificationListItemResource::createFromSpecification($specifications[$i]->specifications[$j]));
                    }
                    $specifications[$i]->specifications = $list;
                }
            } elseif ($_REQUEST['version'] === "allRaw") {
                $specifications = $specCtrl->getSpecifications(-1);
                $list = array();
                for ($j = 0, $max2 = count($specifications); $j < $max2; $j++) {
                    array_push($list, SpecificationListItemResource::createFromSpecification($specifications[$j]));
                }
                $specifications = $list;
            } else {
                if (Synergy::getSessionProvider()->sessionExists()) {
                    $specifications = $specCtrl->getSpecifications($version->getId(), Synergy::getSessionProvider()->getUserId());
                } else {
                    $specifications = $specCtrl->getSpecifications($version->getId());
                }

                $list = array();
                for ($j = 0, $max2 = count($specifications); $j < $max2; $j++) {
                    array_push($list, SpecificationListItemResource::createFromSpecification($specifications[$j]));
                }
                $specifications = $list;
            }

            if (count($specifications) > 0)
                HTTP::OK(json_encode($specifications), 'Content-type: application/json');
            else
                HTTP::NotFound('');
        }



        break;
    default :
        HTTP::MethodNotAllowed("");
        break;
}
?>