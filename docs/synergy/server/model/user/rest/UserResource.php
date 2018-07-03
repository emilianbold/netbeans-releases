<?php

namespace Synergy\Model\User\Rest;

use Synergy\App\Synergy;
use Synergy\Model\Assignment\Rest\AssignmentListItemResource;
use Synergy\Model\Specification\Rest\SpecificationListItemResource;
use Synergy\Model\User;

/**
 * Description of UserResource
 *
 * @author vriha
 */
class UserResource {

    public $username;
    public $firstName;
    public $lastName;
    public $role;
    public $id;
    public $membership;
    public $favorites;
    public $authorOf;
    public $ownerOf;
    public $emailNotifications;
    public $profileImg;
    public $assignments;
    public $email;

    public static function createFromUser($user) {
        $i = new UserResource();
        $i->username = $user->username;
        $i->firstName = $user->firstName;
        $i->lastName = $user->lastName;
        $i->role = $user->role;
        $i->id = $user->id;
        $i->emailNotifications = $user->emailNotifications;
        $i->membership = MembershipResource::createFromMemberships($user->membership);
        $i->favorites = SpecificationListItemResource::createFromSpecifications($user->favorites);
        $i->assignments = AssignmentListItemResource::createFromAssignments($user->assignments);
        $i->authorOf = SpecificationListItemResource::createFromSpecifications($user->authorOf);
        $i->ownerOf = SpecificationListItemResource::createFromSpecifications($user->ownerOf);
        if((Synergy::getSessionProvider()->sessionExists() && $user->username === Synergy::getSessionProvider()->getUsername()) || User::canEdit()){
            $i->email = $user->email;
            if(is_null($i->email) || strlen($i->email) < 1){
                $i->email = $i->username.'@'.DOMAIN;
            }
        }
        $i->profileImg = $user->profileImg;

        return $i;
    }

    public static function createFromUsers($users) {
        $list = array();
        for ($i = 0, $max = count($users); $i < $max; $i++) {
            array_push($list, UserResource::createFromUser($users[$i]));
        }
        return $list;
    }

}
