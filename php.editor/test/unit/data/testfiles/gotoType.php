<?php
interface ifaceDeclaration {}
interface ifaceDeclaration2 extends ifaceDeclaration  {}
class clsDeclaration implements ifaceDeclaration {}
class clsDeclaration2 implements ifaceDeclaration, ifaceDeclaration2 {}
class clsDeclaration3 extends clsDeclaration {}
class clsDeclaration4 extends clsDeclaration3 implements ifaceDeclaration4 {}
function formalParamFuncCall1(
    ifaceDeclaration $ifaceDeclarationVar,
    ifaceDeclaration2 $ifaceDeclaration2Var,
    ifaceDeclaration4 $ifaceDeclaration4Var,
    clsDeclaration  $clsDeclarationVar,
    clsDeclaration2 $clsDeclaration2Var,
    clsDeclaration3 $clsDeclaration3Var,
    clsDeclaration4 $clsDeclaration4Var

) {}
?>
