<?php

include '../misc/HTTP.php';

class Version {

    //put your code here

    public $id;
    public $name;
    public static $editRole = "manager";
    public static $createRole = "manager";
    public static $deleteRole = "manager";

    function __construct($id, $name) {
        $this->id = $id;
        $this->name = $name;
    }

}

$db = new PDO('mysql:host=localhost;dbname=synergy;charset=UTF8', 'password', 'password', array(PDO::ATTR_PERSISTENT => true, PDO::ATTR_EMULATE_PREPARES => true));
$db->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_WARNING);

$handler = $db->prepare("SELECT version, id FROM version ORDER BY version DESC LIMIT 0,100");

if (!$handler->execute()) {
    echo "error";
    print_r($handler->errorInfo());
    die();
}
$data = array();
$it = 0;
while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {

    $data[$it] = new Version($row['id'], $row['version']);
    $it++;
    
}

echo json_encode($data);
?>
