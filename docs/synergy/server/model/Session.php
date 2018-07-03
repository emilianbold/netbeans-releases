<?php

namespace Synergy\Model;

use Exception;
use Synergy\Model\Exception\AuthenticationException;

/**
 * Description of Session
 *
 * @author lada
 */
class Session {

    public $username;
    public $role;
    public $created;
    public $userId;
    public $token = '';
    public $session_id;
    public $firstName;
    public $lastName;
    private $secretKey;

    function __construct($username, $role, $created, $userId) {
        $this->username = $username;
        $this->role = $role; // TODO if user in any tribe === tester, otherwise === viewer
        date_default_timezone_set('UTC');
        $this->created = strtotime($created);
        $this->userId = intval($userId);
        $this->session_id = md5($username . ":" . $created . ":" . SALT_SESSION);
        $this->secretKey = $this->getKey(8);
    }

    private function getKey($length) {
        // get 256 pseudorandom bits in a string of 32 bytes
        $pr_bits = '';
        // Unix/Linux platform?
        $fp = @fopen('/dev/urandom', 'rb');
        if ($fp !== FALSE) {
            $pr_bits .= @fread($fp, $length);
            @fclose($fp);
        }

        // MS-Windows platform?
        if (class_exists('COM', false)) {
            
            // http://msdn.microsoft.com/en-us/library/aa388176(VS.85).aspx
            try {
                $CAPI_Util = new COM('CAPICOM.Utilities.1');
                $pr_bits .= $CAPI_Util->GetRandom($length, 0);
                if ($pr_bits) {
                    $pr_bits = md5($pr_bits, TRUE);
                }
            } catch (Exception $ex) {
                throw new AuthenticationException("Cannot create key: " . $ex->getMessage());
            }
        }
        if (strlen($pr_bits) < $length) {
            throw new AuthenticationException("Cannot create key");
        }
        return $pr_bits;
    }

}

?>