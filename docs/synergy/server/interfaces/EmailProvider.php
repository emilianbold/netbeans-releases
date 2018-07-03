<?php
namespace Synergy\Interfaces;

use Synergy\Model\Email;
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * Description of AssignmentNotificationInterface
 *
 * @author vriha
 */
interface EmailProvider {

    /**
     * @return Email Description
     */
    public function compose($text, $subject, $receiver);

    /**
     * @return boolean true if success
     */
    public function send($email);
}

?>
