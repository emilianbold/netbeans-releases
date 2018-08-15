<?php

namespace Synergy\Providers;

use Synergy\Interfaces\LoggerProvider;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * Description of LogerCtrl
 *
 */
class LoggerCtrl implements LoggerProvider {

    /**
     * Simply writes message using error_log()
     * @param String $message message to be logged
     */
    public static function log($message) {
        error_log("http://$_SERVER[HTTP_HOST]$_SERVER[REQUEST_URI] \n".$message);
    }

    public function delete() {
        $fp = fopen(ini_get('error_log'), 'w');
        fwrite($fp, '');
        fclose($fp);
    }

    public function read() {
        return file_get_contents(ini_get('error_log'));
    }

}

?>
