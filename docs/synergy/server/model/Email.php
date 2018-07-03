<?php

namespace Synergy\Model;

/**
 * Description of Email
 *
 * @author vriha
 */
class Email {

    public $text;
    public $receiver;
    public $subject;

    function __construct($text, $receiver, $subject, $useHTML = false) {
        $this->text = $useHTML? $this->buildHTML($text): $text;
        $this->receiver = $receiver;
        $this->subject = $subject;
    }

    private function buildHTML($text) {
        return '<html><body>'.$text."</body></html>";
    }

}

?>
