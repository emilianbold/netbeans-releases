<?php

class ClassExample extends AbstractClass
{
    public function printOut() {
        print $this->getValue() . "\n";
    }

    public function printValue($a) {
	if ($a) {
	    for ($i = 1; $i <= 10; $i++) {
    echo $i;
}
	}
else {
echo "NetBeans";
}
    }
}

function getFruit() {
    return "Apple";
}

?>
