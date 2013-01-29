<?php
//START

// OK
$foo->bar(htmlspecialchars($_SERVER));

// HINT
$foo->bar($_SERVER);

// HINT
echo $_SERVER["foo"];

// OK
is_numeric($_SERVER["foo"]);

// OK
if (is_numeric($_SERVER["foo"])) {
    // OK
    return $_SERVER["foo"];
}

do {
    // HINT
    echo $_SERVER["foo"];
// OK
} while (is_numeric($_SERVER["foo"]));

// OK
while (is_numeric($_SERVER["foo"])) {
    // OK
    return $_SERVER["foo"];
}

// OK
echo is_numeric($_SERVER["foo"]) ? $_SERVER[""] : $_SERVER[""];

echo is_numeric($foo)
    // HINT
    ? $_SERVER[""]
    // HINT
    : $_SERVER[""];

//END
?>