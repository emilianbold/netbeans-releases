<?php

use Synergy\Controller\RegistrationCtrl;
use Synergy\Misc\HTTP;
use Synergy\Model\Exception\UserException;
use Synergy\Model\Registration\Registration;

require_once '../setup/conf.php';


switch ($_SERVER['REQUEST_METHOD']) {

    case "POST":

        $put = file_get_contents('php://input');
        $data = json_decode($put);

        if (!isset($data->lastname) || !isset($data->firstname) || !isset($data->username) || !isset($data->email) || !isset($data->password) || !filter_var($data->email, FILTER_VALIDATE_EMAIL)) {
            HTTP::BadRequest("Missing or incorrect parameters");
            die();
        }
        
        $registration = new Registration($data->email, $data->username, $data->password, $data->lastname, $data->firstname);
        $regCtrl = new RegistrationCtrl();

        try {
            if ((bool) $regCtrl->register($registration)) {

                HTTP::OK("");
            } else
                HTTP::BadRequest("Project with this name already exists");
        } catch (UserException $ex) {
            HTTP::BadRequest($ex->title . " - " . $ex->message);
        }



        break;
    default :
        HTTP::MethodNotAllowed("");
        break;
}