<?php
namespace Synergy\Interfaces;
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 */
interface IssueProvider {
    
    /**
     * Returns information about issue
     * @param int $id issue id
     * @return \Bug
     */
    public static function getIssue($id, $filter);
}

?>
