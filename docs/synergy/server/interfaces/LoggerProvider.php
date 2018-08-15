<?php
namespace Synergy\Interfaces;
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 */
interface LoggerProvider {

    /**
     * Writes message to log
     * TODO: no need to be static anymore, need refactoring
     * @param String $message message to be logged
     */
    public static function log($message);
    /**
     * Returns content of log file
     */
    public function read();
    /**
     * Deletes content of log file
     */
    public function delete();
}

?>
