<?php

class Catalogue {

    protected $_xml;
    protected $_version;
    public $log = array();

    private function getCatalogueContent($plugin, $bin, $nbmPath, $tmpPath, $unzip, $wwwNbmPath) {
        $catalPlugins = '';
        $catalLicense = '';
        $this->logme('<br/><b>Plugin ' . $plugin->plugin_name . '</b> (' . $plugin->pluginid . ')');
        // now distinguish if the plugin is nbm or zip as there is difference in processing
        if (strstr($bin->nbm_filename, '.nbm')) {
            $this->logme(' * file is .nbm ' . $bin->nbm_filename);
            $bin->unpackAndLoadInfoForCatalogue($nbmPath, $tmpPath, $unzip);
            $catalPlugins.=$bin->getModuleChunkForUcXml($wwwNbmPath) . "\n";
            $catalLicense.=$bin->getLicenseChunkForUcXml();
            $this->log = array_merge($this->log, $bin->log);
        } elseif (strstr($bin->nbm_filename, '.zip')) {
            $this->logme('file is .zip ' . $bin->nbm_filename);
            // get content of zip
            if (file_exists($nbmPath . $bin->nbm_filename)) {
                $cmd = escapeshellcmd($unzip . ' -l ' . $nbmPath . $bin->nbm_filename) . ' | grep nbm | sed "s/^ *//;s/ *$//;s/ \{1,\}/ /g" | cut -d\' \' -f4';
                //$this->logme($cmd);
                $out = null;
                exec($cmd, $out);
                if (!empty($out)) {
                    foreach ($out as $file) {
                        if (!empty($file)) {
                            $this->logme(' * found submodule <b>' . $file . '</b>');
                            $file = PpBinary::ZIP_MODULE_PREF . $bin->binary_id . '_' . $file;
                            $p = new PpBinary();
                            $p->nbm_filename = $file;
                            $p->plugin_id = $plugin->pluginid;
                            @$p->download_size = filesize($nbmPath . $file);
                            //$p->author_userid = $plugin->author_userid;
                            $p->unpackAndLoadInfoForCatalogue($nbmPath, $tmpPath, $unzip);
                            $catalPlugins.=$p->getModuleChunkForUcXml($wwwNbmPath) . "\n";
                            $catalLicense.=$p->getLicenseChunkForUcXml();
                            $this->log = array_merge($this->log, $p->log);
                        }
                    }
                } else {
                    $this->logme(' * <span class="red">ERROR</span>: unzip unsuccessfull ' . $cmd);
                }
            } else {
                $this->logme(' * <span class="red">ERROR</span>: missing plugin .zip file ' . $cmd);
            }
        }
        return array($catalLicense, $catalPlugins);
    }

    public function generateExperimentalCatalogue($version, $nbmPath, $tmpPath, $unzip, $wwwNbmPath) {
        $vers = Doctrine_Query::create()->from('PpNetbeansVersion v')->where('v.id=?', $version)->execute();
        $this->_version = $vers[0]->version;
        $bins = Doctrine_Query::create()->from('PpBinary b')
                        ->leftJoin('b.Verification as v')
                        ->innerJoin('b.Plugin as p')
                        ->where('b.version_id=?', $version)
                        ->andWhere('v.id IS NULL')
                        ->andWhere('b.nbm_filename IS NOT NULL')
                        ->andWhere('b.historic=0')
                        ->andWhere('p.published>0')->execute();
        if ($bins->count() > 0) {
            $catalPlugins = '';
            $catalLicense = '';
            foreach ($bins as $bin) {
                $plugin = $bin->Plugin;
                list($lic, $plug) = $this->getCatalogueContent($plugin, $bin, $nbmPath, $tmpPath, $unzip, $wwwNbmPath);
                $catalLicense.=$lic;
                $catalPlugins.=$plug;
                unset($plugin, $bin);
            }
        } else {
            $this->logme('No plugins found');
        }
        //die($log);
        $out = '<?xml version="1.0"?>
<!DOCTYPE module_updates PUBLIC "-//NetBeans//DTD Autoupdate Catalog 2.6//EN" "http://plugins.netbeans.org/data/catalogue/autoupdate-catalog-2_6.dtd">
<module_updates timestamp="' . date('s/m/h/j/n/Y') . '">
';
        $out.=$catalPlugins;
        $out.=$catalLicense;
        $out.='</module_updates>';
        $this->_xml = $out;
    }

    /**
     * Generate catalogue.xml for specified netbebans version
     * @param string $version NetBeans Version (6.9, 7.0 etc)
     * @return void
     */
    public function generateCatalogue($version, $nbmPath, $tmpPath, $unzip, $wwwNbmPath) {
        $this->_version = $version;
        // get all plugins verified for this version which has status 1 or 2
        $verif = Doctrine_Query::create()->from('PpVerification v')
                        ->innerJoin('v.Binary as b')
                        ->innerJoin('v.Plugin as p')
                        ->where('v.version=?', $version)
                        ->andWhere('v.status=1')
                        ->andWhere('b.nbm_filename IS NOT NULL')
                        ->andWhere('p.published>0')->execute();
        if ($verif->count() > 0) {
            $catalPlugins = '';
            $catalLicense = '';
            foreach ($verif as $v) {
                unset($plugin, $cmd);
                $plugin = $v->Plugin;
                $bin = $v->Binary;
                list($lic, $plug) = $this->getCatalogueContent($plugin, $bin, $nbmPath, $tmpPath, $unzip, $wwwNbmPath);
                $catalLicense.=$lic;
                $catalPlugins.=$plug;
                unset($plugin, $bin);
            }
        } else {
            $this->logme('No plugins found');
        }
        //die($log);
        $out = '<?xml version="1.0"?>
<!DOCTYPE module_updates PUBLIC "-//NetBeans//DTD Autoupdate Catalog 2.6//EN" "http://plugins.netbeans.org/data/catalogue/autoupdate-catalog-2_6.dtd">
<module_updates timestamp="' . date('s/m/h/j/n/Y') . '">
';
        $out.=$catalPlugins;
        $out.=$catalLicense;
        $out.='</module_updates>';


        $this->_xml = $out;
    }

    public function renderXml() {
        header('Content-Type: text/xml; charset=utf-8');
        echo $this->_xml;
    }

    public function saveXml($savePath, $filename = 'catalog.xml', $validate = true) {
        PpLog::logMe(null, NbSsoUser::getInstance()->getUsername(), 'Saving just generated UC for version ' . $this->_version . ' to ' . $savePath . $this->_version);
        if (!is_dir($savePath . $this->_version)) {
            PpLog::logMe(null, NbSsoUser::getInstance()->getUsername(), 'Missing target folder for version ' . $this->_version . ' trying to create that');
            if (mkdir($savePath . $this->_version, 0775, true) != true) {
                PpLog::logMe(null, NbSsoUser::getInstance()->getUsername(), 'ERROR: target folder for the cataloge, version ' . $this->_version . ' can\'t be created');
            }
            chmod($savePath . $this->_version, 0775);
        }
        // replace nb.org dtd with one from PP
        $this->_xml=str_replace('http://www.netbeans.org/dtds/autoupdate-catalog-2_6.dtd','http://plugins.netbeans.org/data/catalogue/autoupdate-catalog-2_6.dtd',$this->_xml);
        // save it now
        if (is_dir($savePath . $this->_version)) {
            // do the xml validation at the last moment - save the xml as _catalog.xml and if valid save it as catalog.xml
            if (!file_put_contents($savePath . $this->_version . '/_' . $filename, $this->_xml)) {
                PpLog::logMe(null, NbSsoUser::getInstance()->getUsername(), 'ERROR: Catalogue file was not written to the ' . $savePath . $this->_version . '/.' . $filename);
            } else {
                if ($validate) {
                    $dom = new DOMDocument;
                    $dom->Load($savePath . $this->_version . '/_' . $filename);
                    $myDom = new MyDOMDocument($dom);
                    if ($myDom->validate()) {
                        file_put_contents($savePath . $this->_version . '/' . $filename, $this->_xml);
                        $this->logme('catalog validated against DTD and saved');
                        PpLog::logMe(null, NbSsoUser::getInstance()->getUsername(), 'OK: Catalogue file was  written to the ' . $savePath . $this->_version . '/' . $filename);
                        // save also gz
                        $gz = gzopen($savePath . $this->_version . '/' . $filename . '.gz', 'w9');
                        gzwrite($gz, $this->_xml);
                        gzclose($gz);
                    } else {
                        PpLog::logMe(null, NbSsoUser::getInstance()->getUsername(), 'ERROR: Catalogue file for UC ' . $this->version . ' was not valid - do not save it, keep original version.<br/>Issue: ' . print_r($myDom->errors, true));
                        $this->logme('---------');
                        $this->logme('ERROR: Catalogue file for UC ' . $this->version . ' was not valid - do not save it, keep original version.<br/>Issue: ' . print_r($myDom->errors, true));
                    }
                } else {
                    file_put_contents($savePath . $this->_version . '/' . $filename, $this->_xml);
                    PpLog::logMe(null, NbSsoUser::getInstance()->getUsername(), 'OK: Catalogue file was  written to the ' . $savePath . $this->_version . '/' . $filename);
                    // save also gz
                    $gz = gzopen($savePath . $this->_version . '/' . $filename . '.gz', 'w9');
                    gzwrite($gz, $this->_xml);
                    gzclose($gz);
                }
                unlink($savePath . $this->_version . '/_' . $filename);
            }
        }
        return '/nbpluginportal/updates/' . $this->_version . '/' . $filename;
    }

    protected function logme($msg) {
        $this->log[] = $msg;
    }

}

?>
