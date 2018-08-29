<?php

function configFileExists()
{
    try {
        return file_exists('/usr/local/db_config.php');
    } catch (Exception $e) {
        return false;
    }
}


// SYNERGY DATABASE
// dev
if (configFileExists()) {
    require_once '/usr/local/db_config.php';
} else {
    define('DHOST', 'mysql:host=localhost;dbname=synergy;charset=UTF8');
    define('user', 'password');
    define('user', 'password');
    define('DB', 'synergy');
    define('DBHOST', 'localhost');
}


// BUGZILLA DATABASE
// dev
define('BZ_DHOST', 'mysql:host=localhost;dbname=bugzilla;charset=UTF8');
define('BZ_DUSER', 'user');
define('BZ_DPASS', 'password');
define('BZ_DB', 'bugzilla');
define('BZ_DBHOST', 'localhost');

require_once '../app/Synergy.php';

Synergy\App\Synergy::init();
session_start();
session_write_close();
require_once '../lib/htmlpurifier-4.4.0-lite/library/HTMLPurifier.auto.php';

use Synergy\Controller\Mediator;

// FALLBACK IF initSettings() fails to declare these
// SALT
define("SALT", "synergy_server_salt");
define("SALT_SESSION", "synergy_session_@_");
Synergy\App\Synergy::setAnonymousCreateDelete();
define('CACHE', '../cache/'); // cache folder
// PAGE SIZE FOR SEARCH BY LABEL
if (!defined('LABEL_PAGE')) {
    define("LABEL_PAGE", 25);
// PAGE SIZE FOR RUNS OVERVIEW
    define("RUNS_PAGE", 25);
// PAGE SIZE FOR USERS
    define("USERS_PAGE", 50);
// DOMAIN FOR SENDING EMAILS TO USERS
    define("DOMAIN", 'localhost.com');

// WHERE TO SAVE ATTACHMENTS - must ends with system path delimiter
    define('ATTACHMENT_PATH', '/var/www/att/');
    define('IMAGE_PATH', '/var/www/media/');
    define('IMAGE_BASE', 'http://localhost/media/');
}
$u = $_SERVER['SERVER_NAME'];
$uu = $_SERVER['REQUEST_URI'];
$base = "http://" . $u . substr($uu, 0, stripos($uu, "/", 1)) . "/api/";
define("BASER_URL", $base);

$mediator = new Mediator();


set_exception_handler('custom_handler');
set_error_handler('custom_error_handler', E_ERROR);

function custom_handler($exc)
{
    $log = print_r($exc, true);
    $logger = Synergy\App\Synergy::getProvider("logger");
    $logger::log($log);
    $status_header = 'HTTP/1.1 500';
    header($status_header);
    echo "";
    // TODO add error page
}

function custom_error_handler($exc)
{
    $log = print_r($exc, true);
    $logger = Synergy\App\Synergy::getProvider("logger");
    $logger::log($log);
    $status_header = 'HTTP/1.1 500';
    header($status_header);
    echo "";
}

function custom_error_handler_from_fatal($code, $msg, $file, $line)
{
    $logger = Synergy\App\Synergy::getProvider("logger");
    $logger::log("Fatal error: " . $code . " :" . $msg . " @" . $file . ":" . $line);
    $status_header = 'HTTP/1.1 500';
    header($status_header);
    echo "Fatal error: " . $code . " :" . $msg . " @" . $file . ":" . $line;
}

function fatal_error_handler()
{

    if (@is_array($e = @error_get_last())) {
        $code = isset($e['type']) ? $e['type'] : 0;
        $msg = isset($e['message']) ? $e['message'] : '';
        $file = isset($e['file']) ? $e['file'] : '';
        $line = isset($e['line']) ? $e['line'] : '';

        if ($code > 0)
            custom_error_handler_from_fatal($code, $msg, $file, $line);
    }
}

//register_shutdown_function('handleShutdown');

function handleShutdown()
{
    $error = error_get_last();
    if ($error !== NULL) {
        $info = "[SHUTDOWN] file:" . $error['file'] . " | ln:" . $error['line'] . " | msg:" . $error['message'] . PHP_EOL;
        $logger = Synergy\App\Synergy::getProvider("logger");
        $logger::log($info);
        $status_header = 'HTTP/1.1 500';
        header($status_header);
        echo $info;
    }
}

?>