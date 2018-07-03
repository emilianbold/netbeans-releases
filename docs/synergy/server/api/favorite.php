<?php

use Synergy\App\Synergy;
use Synergy\Controller\SpecificationCtrl;
use Synergy\Misc\HTTP;
use Synergy\Model\Specification\Rest\SpecificationListItemResource;

//header("Access-Control-Allow-Origin: *");
require_once '../setup/conf.php';

switch ($_SERVER['REQUEST_METHOD']) {

    case "GET":
        if (!isset($_GET['username']) || is_null($_GET['username'])) {
            HTTP::BadRequest("Missing parameter: username");
            die();
        }
         $specCtrl = new SpecificationCtrl();
        $specifications = $specCtrl->getFavoriteSpecifications($_GET['username']);

        if (Synergy::getSessionProvider()->sessionExists()) {
            $role = Synergy::getSessionProvider()->getUserRole();
            foreach ($specifications as $spec) {
                $spec->addControls($role);
            }
        }

        HTTP::OK(json_encode(SpecificationListItemResource::createFromSpecifications($specifications)), 'Content-type: application/json');
        break;
    default :
        HTTP::MethodNotAllowed("");
        break;
}
?>
