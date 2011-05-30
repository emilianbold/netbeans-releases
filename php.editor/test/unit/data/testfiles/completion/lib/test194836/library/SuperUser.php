<?php
namespace User;
class SuperUser extends UserAbstract{
    public function isSuperUser(){
        return true;
    }
}

?>
