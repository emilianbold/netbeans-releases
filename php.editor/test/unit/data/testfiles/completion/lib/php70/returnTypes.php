<?php

namespace My\Firm;

interface Comment {}
interface CommentsIterator extends Iterator {
    function current(): Comment;
}

function &my_array_sort(array &$data): array {
    return $data;
}

function foo(): Comment {
}

function &bar(): \My\Firm\Comment {
}
