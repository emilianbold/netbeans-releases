<?php

namespace Datagrid {
    class Datagrid {}
    class Action {}
    interface IFace {}
}

namespace AdminModule {
    use \DataGrid\Action,
        \DataGrid\DataGrid,
        \DataGrid\IFace;
    class Presenter extends Action implements IFace {

    }

}

?>