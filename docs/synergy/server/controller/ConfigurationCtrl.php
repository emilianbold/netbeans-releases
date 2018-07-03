<?php
namespace Synergy\Controller;

use Synergy\DB\ConfigurationDAO;
use Synergy\Model\Setting;

/**
 * Description of ConfigurationCtrl
 *
 * @author lada
 */
class ConfigurationCtrl {

    //put your code here
    private $contrDao;

    function __construct() {
        $this->contrDao = new ConfigurationDAO();
    }

    /**
     * Returns array of settings
     * @return Setting[]
     */
    public function loadSettings() {
        return $this->contrDao->loadSettings();
    }

    /**
     * Saves settings
     * @param Setting[] $data
     */
    public function saveSettings($data) {
        foreach ($data as $s) {
            if (Setting::isValid($s)){
                if($this->contrDao->keyExists($s->key)){
                    $this->contrDao->saveSetting($s);
                }else{
                    $this->contrDao->addSetting($s);
                }
            }
        }
    }

}

?>
