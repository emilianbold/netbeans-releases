<?php

class ClassName {
    /**
     * Nejaky popis.
     *
     * @var int
     */
    public $fieldWithDesc;

    /**
     * @var int
     */
    public $fieldWithoutDesc;

    /**
     * Nejaky popis.
     *
     * @var int[]
     */
    public $fieldWithDescAndArray;

    /**
     * @var int[]
     */
    public $fieldWithoutDescAndArray;
}

$c = new ClassName();
$c->fieldWithDesc;
$c->fieldWithoutDesc;
$c->fieldWithDescAndArray;
$c->fieldWithoutDescAndArray;

?>