<?php
namespace Synergy\Providers;

use Synergy\Interfaces\EmailProvider;
use Synergy\Model\Email;
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * Description of EmailCtrl
 *
 * @author vriha
 */
class EmailCtrl implements EmailProvider {

    public $useHTML = true;
    
    public function compose($text, $subject, $receiver) {
        return new Email($text, $receiver, $subject, $this->useHTML);
    }

    /**
     * Sends email
     * @param Email $email email
     */
    public function send($email) {
        $headers = "From: notification@".DOMAIN;
        if($this->useHTML){
            $headers = $headers."\r\nContent-Type: text/html; charset=UTF-8\r\n";
        }
        mail($email->receiver, $email->subject, $email->text, $headers);
        }

//put your code here
}

?>
