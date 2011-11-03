<?php
//START

$globVar = 5;

class ClassName {

    function functionName($param) {
        $assignment = "foo";

        try {

        } catch (Exception $ex) {
            echo $ex->getTraceAsString();
        }

        do {
            $doCond = false;
        } while ($doCond);

        foreach ($array as $key => $value) {

        }

        global $globVar;

        $this->foo();

        $GLOBALS["a"];
        $_SERVER["a"];
        $_GET["a"];
        $_POST["a"];
        $_FILES["a"];
        $_COOKIE["a"];
        $_SESSION["a"];
        $_REQUEST["a"];
        $_ENV["a"];
    }

    function foo(&$referenceParam = null) {
        $this->foo($uninit);

        if ($uninitIf) {

        }

        do {

        } while ($uninitDo);

        while ($uninitWhile) {

        }

        for ($index = 0; $index < $uninitFor; $index++) {

        }

        if (true) {

        } elseif ($uninitElseif) {

        }
    }

}

//END
?>