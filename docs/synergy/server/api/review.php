<?php

use Synergy\App\Synergy;
use Synergy\Controller\ReviewCtrl;
use Synergy\Misc\HTTP;
use Synergy\Model\Review\ReviewPage;

// "https://netbeans.org/kb/docs/java/quickstart.html"
require_once '../setup/conf.php';
switch ($_SERVER['REQUEST_METHOD']) {
    case "GET":
        $ctrl = new ReviewCtrl();
        if (isset($_REQUEST["url"]) && strlen($_REQUEST["url"]) > 0) {
            HTTP::OK($ctrl->getReviewPage($_REQUEST["url"]), "Content-type: text/html");
        } else {
            HTTP::BadRequest("Missing URL parameter");
        }
        break;
    case "POST":
        $ctrl = new ReviewCtrl();
        $data = json_decode(file_get_contents('php://input'));

        if (!isset($data->title) || !isset($data->url) || !isset($data->owner)) {
            HTTP::BadRequest("Missing parameters");
            die();
        }

        if (!ReviewPage::canCreate()) {
            HTTP::Unauthorized("");
            die();
        }
        
        $ctrl->createReviewPage(new ReviewPage($data->title, $data->owner, $data->url));
        HTTP::OK("");
        break;
    default:
        HTTP::MethodNotAllowed("");
        break;
}
