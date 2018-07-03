<?php
namespace Synergy\Model;

use Exception;
use Synergy\App\Synergy;

/**
 * Description of RunAttachment
 *
 * @author vriha
 */
class RunAttachment {

    private $path;
    public $url;
    public $id;
    public $name;
    public $controls;
    public $runId;

    function __construct($path, $id, $runId) {
        $this->url = BASER_URL . "run_attachment.php?id=" . $id;
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
        $this->runId = intval($runId);
    }

    public function getPath() {
        return $this->path;
    }

    function addControls($role) {
        $this->controls = array();
        switch ($role) {
            case "admin":
            case "manager":
                array_push($this->controls, new Action("Delete", "delete", "icon-trash"));
                break;
            default:
                break;
        }
    }

}

?>
