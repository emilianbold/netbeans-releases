<?php

namespace Foo;

use Nette\Utils as NU;
use My\Unused as MU;

/**
 * Homepage presenter.
 *
 * @author     John Doe
 * @package    MyApplication
 */
class HomepagePresenter extends BasePresenter {

    public function renderDefault() {
        NU\Strings::capitalize();
    }

}

?>