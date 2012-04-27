<?php

namespace Datagrid {
    class Datagrid {}
    class Action {}
    interface IFace {}
}

namespace AdminModule {
    use \DataGrid\Action,
        \DataGrid\IFace,
        \DataGrid\DataGrid;
    class Presenter extends Action implements IFace {

    }

}

?>