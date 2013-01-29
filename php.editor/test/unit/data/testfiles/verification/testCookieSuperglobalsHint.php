<?php
//START

// OK
$foo->bar(htmlspecialchars($_COOKIE));

// HINT
$foo->bar($_COOKIE);

// HINT
echo $_COOKIE["foo"];

// OK
is_numeric($_COOKIE["foo"]);

// OK
if (is_numeric($_COOKIE["foo"])) {
    // OK
    return $_COOKIE["foo"];
}

do {
    // HINT
    echo $_COOKIE["foo"];
// OK
} while (is_numeric($_COOKIE["foo"]));

// OK
while (is_numeric($_COOKIE["foo"])) {
    // OK
    return $_COOKIE["foo"];
}

// OK
echo is_numeric($_COOKIE["foo"]) ? $_COOKIE[""] : $_COOKIE[""];

echo is_numeric($foo)
    // HINT
    ? $_COOKIE[""]
    // HINT
    : $_COOKIE[""];

//END
?>