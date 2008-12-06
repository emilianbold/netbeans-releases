<?php
class Author {
    public $name;
    function __construct() {}//Author
}

/**
 * @property Author $author hello this is doc
 */
class Book {
    public $Title;
    function __construct() {}//Book
}

/**
 * @param Book $hello
 * @return Author
 */
function test($hello) {
}
?>
