<?php

namespace Synergy\DB;

use Synergy\Misc\Util;
use Synergy\Model\Registration\Registration;

/**
 * Description of RegistrationDAO
 *
 */
class RegistrationDAO {

    /**
     * 
     * @param Registration $reg
     * @return type
     */
    public function register($reg) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("INSERT INTO user (username, first_name, last_name, email, role, passwd) VALUES (:u, :f, :l, :e, 'tester', :p)");
        $handler->bindValue(":u", Util::purifyHTML($reg->username));
        $handler->bindValue(":f", Util::purifyHTML($reg->firstname));
        $handler->bindValue(":l", Util::purifyHTML($reg->lastname));
        $handler->bindValue(":e", Util::purifyHTML($reg->email));
        $handler->bindValue(":p", md5(Util::purifyHTML($reg->password) . SALT));

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }
        return DB_DAO::getDB()->lastInsertId();
    }

    //put your code here
}
