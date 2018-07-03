<?php

use Synergy\DB\DB_DAO;
use Synergy\Misc\Util;
use Synergy\Model\TestRun;

require_once '../setup/conf.php';
Util::authorize("admin");
DB_DAO::executeQuery("UPDATE `review_pages` SET url='https://netbeans.org/kb/74/websvc/jaxb.html' WHERE url='https://netbeans.org/kb/docs/websvc/jaxb.html'");
echo "done23";
?>
