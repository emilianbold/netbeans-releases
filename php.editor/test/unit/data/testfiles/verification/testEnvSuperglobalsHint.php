<?php
//START

// OK
$foo->bar(htmlspecialchars($_ENV));

// HINT
$foo->bar($_ENV);

// HINT
echo $_ENV["foo"];

// OK
is_numeric($_ENV["foo"]);

// OK
if (is_numeric($_ENV["foo"])) {
    // OK
    return $_ENV["foo"];
}

do {
    // HINT
    echo $_ENV["foo"];
// OK
} while (is_numeric($_ENV["foo"]));

// OK
while (is_numeric($_ENV["foo"])) {
    // OK
    return $_ENV["foo"];
}

// OK
echo is_numeric($_ENV["foo"]) ? $_ENV[""] : $_ENV[""];

echo is_numeric($foo)
    // HINT
    ? $_ENV[""]
    // HINT
    : $_ENV[""];

//END
?>