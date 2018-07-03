<?php

namespace Synergy\Model\User\Rest;

/**
 * Description of MembershipResource
 *
 * @author vriha
 */
class MembershipResource {

    public $id;
    public $role;
    public $name;

    public static function createFromMembership($membership) {
        $i = new MembershipResource();
        $i->name = $membership->name;
        $i->id = $membership->id;
        $i->role = $membership->role;
        return $i;
    }

    public static function createFromMemberships($memberships) {
        $list = array();
        for ($i = 0, $max = count($memberships); $i < $max; $i++) {
            array_push($list, MembershipResource::createFromMembership($memberships[$i]));
        }
        return $list;
    }

}
