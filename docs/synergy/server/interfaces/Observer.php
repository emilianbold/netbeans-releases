<?php
namespace Synergy\Interfaces;
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author vriha
 */
interface Observer {
   
    /**
     * Receives message (event)
     * @param String $name message (event) name
     * @param object $data any type 
     */
    public static function on($name, $data);
}

?>
