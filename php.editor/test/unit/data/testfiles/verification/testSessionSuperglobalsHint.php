<?php
//START

// OK
$foo->bar(htmlspecialchars($_SESSION));

// HINT
$foo->bar($_SESSION);

// HINT
echo $_SESSION["foo"];

// OK
is_numeric($_SESSION["foo"]);

// OK
if (is_numeric($_SESSION["foo"])) {
    // OK
    return $_SESSION["foo"];
}

do {
    // HINT
    echo $_SESSION["foo"];
// OK
} while (is_numeric($_SESSION["foo"]));

// OK
while (is_numeric($_SESSION["foo"])) {
    // OK
    return $_SESSION["foo"];
}

// OK
echo is_numeric($_SESSION["foo"]) ? $_SESSION[""] : $_SESSION[""];

echo is_numeric($foo)
    // HINT
    ? $_SESSION[""]
    // HINT
    : $_SESSION[""];

//END
?>