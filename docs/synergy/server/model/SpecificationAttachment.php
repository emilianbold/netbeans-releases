<?php
namespace Synergy\Model;

use Exception;
use Synergy\App\Synergy;
use Synergy\Controller\SpecRelationCtrl;

/**
 * Description of Attachment
 *
 * @author vriha
 */
class SpecificationAttachment {

    //put your code here
    private $path;
    public $url;
    public $id;
    public $name;
    public $controls;
    public $specId;

    function __construct($path, $id, $spec_id) {
        $this->url = BASER_URL . "attachment.php?id=" . $id;
        $this->path = $path;
        $this->id = intval($id);
        $path_exploaded = explode(DIRECTORY_SEPARATOR, $path);
        $this->name = $path_exploaded[count($path_exploaded) - 1];
        try {
            $this->name = substr($this->name, strpos($this->name, "_") + 1);
        } catch (Exception $e) {
            $r = print_r($e, true);
            $logger = Synergy::getProvider("logger");
            $logger::log($r);
        }
        $this->specId = intval($spec_id);
    }

    public function getPath() {
        return $this->path;
    }

    function addControls($role) {
        $this->controls = array();
        switch ($role) {
            case "tester":
                $ctrl = new SpecRelationCtrl();
                if ($ctrl->isUserRelatedToSpec($this->specId, Synergy::getSessionProvider()->getUsername())) {
                    array_push($this->controls, new Action("Delete", "delete", "icon-trash"));
                }
                break;
            case "privilegedTester":
            case "admin":
            case "manager":
            case "leader":
                array_push($this->controls, new Action("Delete", "delete", "icon-trash"));
                break;
            default:
                break;
        }
    }

    public static function deleteFile($path) {
        unlink($path);
    }

}

?>
