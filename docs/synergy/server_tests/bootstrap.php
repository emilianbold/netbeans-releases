<?php

function autoload($className) {
    if (!strpos($className, "PHPUnit")) {
        $className = ltrim($className, '\\');
        $fileName = '';
        $namespace = '';

        if ($lastNsPos = strripos($className, '\\')) {
            $namespace = substr($className, 0, $lastNsPos);
            $namespace = strtolower($namespace);
            $className = substr($className, $lastNsPos + 1);
            $fileName = str_replace('\\', DIRECTORY_SEPARATOR, $namespace) . DIRECTORY_SEPARATOR;
            if (strpos($fileName, "synergy" . DIRECTORY_SEPARATOR) === 0) {
                $fileName = substr($fileName, 8);
            }
        }

        
        if(strpos($fileName, "test") && (strpos($className, "TestCase") || $className==="FixtureTestCase")){
            $fileName = str_replace("/test", "", $fileName);
            $fileName .= $className . '.php';
//            require "..". DIRECTORY_SEPARATOR .".." . DIRECTORY_SEPARATOR ."server".DIRECTORY_SEPARATOR. $fileName;
            require __DIR__ . DIRECTORY_SEPARATOR ."server".DIRECTORY_SEPARATOR. $fileName;
        }else{
            $fileName .= $className . '.php';
//            require "..". DIRECTORY_SEPARATOR ."..". DIRECTORY_SEPARATOR ."..". DIRECTORY_SEPARATOR ."server" . DIRECTORY_SEPARATOR . $fileName;
            require __DIR__. DIRECTORY_SEPARATOR ."..". DIRECTORY_SEPARATOR ."server" . DIRECTORY_SEPARATOR . $fileName;
        }
        
    }
}

// SALT
define("SALT", "synergy_server_salt");
define("SALT_SESSION", "synergy_session_@_");

define('CACHE', '/../server/cache/'); // cache folder
// PAGE SIZE FOR SEARCH BY LABEL
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
// SYNERGY DATABASE
// dev
define('DHOST', 'mysql:host=localhost;dbname=test;charset=UTF8');
define('DUSER', 'root');
define('DPASS', 'nbuser');
define('DB', 'test');
define('DBHOST', 'localhost');

// BUGZILLA DATABASE
// dev
define('BZ_DHOST', 'mysql:host=localhost;dbname=bugzilla;charset=UTF8');
define('BZ_DUSER', 'user');
define('BZ_DPASS', 'password');
define('BZ_DB', 'bugzilla');
define('BZ_DBHOST', 'localhost');

require_once __DIR__.DIRECTORY_SEPARATOR.'../server/lib/htmlpurifier-4.4.0-lite/library/HTMLPurifier.auto.php';
$base = "http://unittest/api/";
define("BASER_URL", $base);


require_once __DIR__.DIRECTORY_SEPARATOR.'../server/app/Synergy.php';
spl_autoload_register('autoload');
try{
Synergy\App\Synergy::initPHPUnit();
}  catch (Exception $e){
    
}

?>
