<?php
interface ifaceDeclaration {}
interface ifaceDeclaration2 extends ifaceDeclaration  {}
class clsDeclaration implements ifaceDeclaration {}
class clsDeclaration2 implements ifaceDeclaration, ifaceDeclaration2 {}
class clsDeclaration3 extends clsDeclaration {}
class clsDeclaration4 extends clsDeclaration3 implements ifaceDeclaration4 {}
?>
