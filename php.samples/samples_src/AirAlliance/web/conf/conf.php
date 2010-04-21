<?php

/*
 * B Configuration information is stored here.
 * deally you should read these vaules from an
 * xternal properties file.
 */

class AAConf {

    private $databaseURL = "localhost";
    private $databaseUName = "root";
    private $databasePWord = "";
    private $databaseName = "AirAlliance";

    function get_databaseURL() {
        return $this->databaseURL;
    }

    function get_databaseUName() {
        return $this->databaseUName;
    }

    function get_databasePWord() {
        return $this->databasePWord;
    }

    function get_databaseName() {
        return $this->databaseName;
    }

}
?>