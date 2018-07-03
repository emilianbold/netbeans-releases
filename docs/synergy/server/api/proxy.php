<?php

use Synergy\Misc\HTTP;

require_once '../setup/conf.php';

switch ($_SERVER['REQUEST_METHOD']) {
    case 'POST':
        $post = file_get_contents('php://input');
        $data = json_decode($post);

        if (!isset($data->method) || !isset($data->url)) {
            HTTP::BadRequest("Missing parameters");
            die();
        }

        switch ($data->method) {
            case "GET":
                $result = file_get_contents(urlencode($data->url));
                if (!$result) {
                    HTTP::BadGateway('Unable to retrieve data from url ' . $data->url);
                } else {
                    HTTP::OK($result, 'Content-type: application/json');
                }
                break;
            default:
                HTTP::BadRequest('Proxy does not support this HTTP method');
                break;
        }

        break;
    default :
        HTTP::MethodNotAllowed('');
        break;
}
?>
