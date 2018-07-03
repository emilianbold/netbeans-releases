<?php

namespace Synergy\Model;

/**
 * Description of CurlRequestResult
 *
 * @author vriha
 */
class CurlRequestResult {
    public $data;
    public $headers;

    function __construct($data, $headers) {
        $this->data = $data;
        $this->headers = $headers;
    }
}
