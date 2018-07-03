<?php
namespace Synergy\Model;

use Exception;
use Synergy\App\Synergy;
use Synergy\DB\CaseDAO;
use Synergy\Misc\HTTP;
use Synergy\Controller\SpecRelationCtrl;

/**
 * Description of TestCaseImage
 *
 * @author lada
 */
class TestCaseImage {

    private $path;
    public $url;
    public $src;
    public $id;
    public $name;
    public $title;
    public $controls;
    public $caseId;

    function __construct($path, $id, $caseId, $title) {
        $this->url = BASER_URL . "image.php?id=" . $id;
        $this->title = $title;
        $this->path = $path;
        $this->id = intval($id);
        $path_exploaded = explode(DIRECTORY_SEPARATOR, $path);
        $this->name = $path_exploaded[count($path_exploaded) - 1];
        $this->src = IMAGE_BASE.  $this->name;
        $this->caseId = intval($caseId);
    }

    public function getPath() {
        return $this->path;
    }

    function addControls($role, $suiteId) {
        $this->controls = array();
        switch ($role) {
            case "tester":
                $ctrl = new SpecRelationCtrl();
                if ($ctrl->isUserRelatedToSuite($suiteId, Synergy::getSessionProvider()->getUsername())) {
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

    public static function saveImage($data, $fileName, $id, $title) {
        try {
            date_default_timezone_set('UTC');
            $timestamp = strtotime(date("Y-m-d H:i:s"));
            $fp = fopen(IMAGE_PATH . $timestamp . "_" . $fileName, 'w');
            fwrite($fp, $data);
            fclose($fp);
        } catch (Exception $e) {
            $er = print_r($e, true);
            $logger = Synergy::getProvider("logger");
            $logger::log($er);
            HTTP::InternalServerError("Unable to write to file");
            return;
        }


        // save record to DB
        $caseDao = new CaseDAO();
        return $caseDao->saveImage($id, $timestamp . "_" . $fileName, $title);
    }

    public static function deleteFile($path) {
        unlink($path);
    }

}

?>
