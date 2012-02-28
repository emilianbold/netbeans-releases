<?php

namespace Datagrid {
    interface IFace {}
    interface ISux {}
}

namespace AdminModule {
    use \DataGrid\IFace,
        \DataGrid\ISux;
    interface Moo extends IFace {}

}

?>