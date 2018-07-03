<?php

namespace Synergy\Misc;

use HTMLPurifier;
use HTMLPurifier_Config;
use Synergy\App\Synergy;
use Synergy\Model\CurlRequestResult;
use Synergy\Model\Exception\GeneralException;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * Description of Util
 *
 * @author vriha
 */
class Util {

    public static $NEW_LINE = "\n";

    /**
     * Takes array of values and returns part of SQL query as sequence of ORs closed in braces. Sample output for arrayToSQLOR([0,1], "string") is (string=1 OR string=2)
     * @param type $data
     * @param type $string
     * @return String
     */
    public static function arrayToSQLOR($data, $string) {
        $sql = "";
        for ($i = 0, $max = count($data); $i < $max; $i++) {

            $sql = $sql . $string . "=" . $data[$i] . " OR ";
        }

        $sql = substr($sql, 0, strlen($sql) - 3);
        return "(" . $sql . ")";
    }

    /**
     * Uses HTML Purifier to safe input
     * @param String $dirtyHTML
     * @return String
     */
    public static function purifyHTML($dirtyHTML) {
//        $r = htmLawed($dirtyHTML, array('safe'=>1));
//        return $r;
        $dirtyHTML = str_replace("\\n", "\n", $dirtyHTML);
        if (defined('ANONYM') && isset($_REQUEST['anonym']) && $_REQUEST['anonym'] === ANONYM) {
            if ($dirtyHTML === "\n") {
                return "";
            }
            return $dirtyHTML;
        }

        $config = HTMLPurifier_Config::createDefault();
        $purifier = new HTMLPurifier($config);
        $r = $purifier->purify($dirtyHTML);
        if ($r === "\n") {
            return "";
        }
        return $r;
    }

    /**
     * Returns request url
     * @return string
     */
    public static function getRequestURL() {
        $pageURL = (@$_SERVER["HTTPS"] == "on") ? "https://" : "http://";
        if ($_SERVER["SERVER_PORT"] != "80") {
            $pageURL .= $_SERVER["SERVER_NAME"] . ":" . $_SERVER["SERVER_PORT"] . $_SERVER["REQUEST_URI"];
        } else {
            $pageURL .= $_SERVER["SERVER_NAME"] . $_SERVER["REQUEST_URI"];
        }
        return $pageURL;
    }

    /**
     * Checks if request comes from administration and if so, checks if user is admin or manager. If authorization fails, sends HTTP::Unauthorized and dies
     * @return type
     */
    public static function authorize($requiredRole) {
        if (strlen($requiredRole) < 1) {
            return;
        }
        switch ($requiredRole) {
            case "manager":
            case "admin":
                if ((Synergy::getSessionProvider()->sessionExists() &&
                        (Synergy::getSessionProvider()->getUserRole() === "admin" || Synergy::getSessionProvider()->getUserRole() === "manager"))) {
                    return;
                }
                break;
            default:
                if ((Synergy::getSessionProvider()->sessionExists() && (Synergy::getSessionProvider()->getUserRole() === $requiredRole))) {
                    return;
                }
                break;
        }
        HTTP::Unauthorized('');
        die();
    }

    /**
     * Checks if request comes from administration and if so, checks if user is admin or manager. If authorization fails, returns false
     * @return type
     */
    public static function isAuthorized($requiredRole) {
        if (strlen($requiredRole) < 1) {
            return true;
        }

        switch ($requiredRole) {
            case "manager":
            case "admin":
                if ((Synergy::getSessionProvider()->sessionExists() &&
                        (Synergy::getSessionProvider()->getUserRole() === "admin" || Synergy::getSessionProvider()->getUserRole() === "manager"))) {
                    return true;
                }
                break;
            default:
                if ((Synergy::getSessionProvider()->sessionExists() && (Synergy::getSessionProvider()->getUserRole() === $requiredRole))) {
                    return true;
                }
                break;
        }
        return false;
    }

    public static function endsWith($file, $preffix) {
        $length = strlen($preffix);
        if ($length == 0) {
            return true;
        }
        return (substr($file, -$length) === $preffix);
    }

    public static function isSupportedImage($name) {
        if (Util::endsWith($name, ".jpg") || Util::endsWith($name, ".png") || Util::endsWith($name, ".gif") || Util::endsWith($name, ".jpeg")) {
            return true;
        }
        return false;
    }

    public static function writeTxtFile($filename, $path, $content) {
        if (!file_exists(dirname($path . $filename))) {
            $e = mkdir(dirname($path . $filename), 0777, true);
            if (!$e) {
                Synergy::log("Cannot create folder " . dirname($path . $filename));
            }
        }

        $suffix = 0;
        $fileAbsolute = $path . $filename;
        if (file_exists($fileAbsolute)) {
            $fileAbsolute = $fileAbsolute . $suffix;
            $suffix++;
            while (file_exists($fileAbsolute)) {
                $fileAbsolute = $path . $filename . $suffix;
                $suffix++;
                error_log("   " . $fileAbsolute);
            }
        }

        if ($suffix > 0) {
            if (!rename($path . $filename, $fileAbsolute)) {
                throw new GeneralException("Problem moving old file ", "Util::writeTxtFile", "");
            }
        }
        $fp = fopen($path . $filename, 'w');
        fwrite($fp, $content);
        $e = fclose($fp);
        if (!$e) {
            Synergy::log("Cannot create folder " . dirname($path . $filename));
        }
    }

    public static function requestUrlByCurl($url) {
        $ch = curl_init();
        curl_setopt($ch, CURLOPT_HEADER, 0);
        curl_setopt($ch, CURLOPT_URL, $url);
        curl_setopt($ch, CURLOPT_RETURNTRANSFER, TRUE);
        curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, false);
        curl_setopt($ch, CURLOPT_SSL_VERIFYHOST, false);
        curl_setopt($ch, CURLOPT_FOLLOWLOCATION, 1);
        if (Synergy::hasProxy()) {
            curl_setopt($ch, CURLOPT_PROXY, Synergy::getProxy());
        }
//        curl_setopt($ch, CURLOPT_PROXYPORT, 80);

        $data = curl_exec($ch);
        $result = new CurlRequestResult($data, curl_getinfo($ch));
        curl_close($ch);
        return $result;
    }

}

?>