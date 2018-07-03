<?php

namespace Synergy\Model\User\Rest;

/**
 * Description of UserListItemRest
 *
 * @author vriha
 */
class UserListItemResource {

    public $firstName;
    public $lastName;
    public $role;
    public $username;
    public $profileImg;
    public $controls;
    
    public static function createFromUser($user) {
        $i = new UserListItemResource();
        $i->username = $user->username;
        $i->firstName = $user->firstName;
        $i->lastName = $user->lastName;
        $i->role = $user->role;
        $i->controls = $user->controls;
        $i->profileImg = $user->profileImg;
        return $i;
    }

    public static function createFromUsers($users) {
        $list = array();
        for ($i = 0, $max = count($users); $i < $max; $i++) {
            array_push($list, UserListItemResource::createFromUser($users[$i]));
        }
        return $list;
    }

}
