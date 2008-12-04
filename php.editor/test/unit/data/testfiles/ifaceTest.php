<?php
class mycls implements myface {
    const RECOVER_ORIG = 1;
    function mfnc() {}
}
interface myface {
    const RECOVER_ORIG = 2;
    function mfnc();
}

myface::RECOVER_ORIG;
mycls::RECOVER_ORIG;

function function_face(myface $a) {
    $a->mfnc();
}

function function_cls(mycls $a) {
    $a->mfnc();
}
?>
