<?php

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * Description of CatalogueAntImplementation
 *
 * @author honza
 */
class CatalogueAntImplementation extends Catalogue {

    const ANT_CATALOG_FILE_XML = 'updates.xml';
    const BUILD_FILE = 'build.xml';

    private $_nbmPath;
    private $_tmpPath;

    /**
     * Generate catalogue.xml for specified netbebans version
     * @param string $version NetBeans Version (6.9, 7.0 etc)
     * @return void
     */
    public function generateCatalogue($version, $nbmPath, $tmpPath, $javaHome, $ant, $buidScrtiptPath) {
        $this->_nbmPath = $nbmPath;
        $this->_tmpPath = $tmpPath;
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
            // cleanup tmp dir now
            exec('rm -rf ' . $tmpPath . '*');
            @mkdir($tmpPath, 0777);
            @chmod($tmpPath, 0777);
            foreach ($verif as $v) {
                // move binaries into tmp folder so we can run ant task on them
                $plugin = $v->Plugin;
                $bin = $v->Binary;
                if (preg_match('#\.nbm$#',$bin->nbm_filename)) {
                    @copy($nbmPath . $bin->nbm_filename, $tmpPath . $bin->nbm_filename);
                    $this->logme('copying nbm module ' . $bin->nbm_filename . ' into ' . $tmpPath);
                } elseif (preg_match('#\.zip$#',$bin->nbm_filename)) {
                    $bundlePrefix = PpBinary::ZIP_MODULE_PREF . $bin->binary_id . '_*';
                    exec('cp ' . $nbmPath . $bundlePrefix . '  ' . $tmpPath);
                    $this->logme('copying .zip bundle submodules ' . $bundlePrefix . ' into ' . $tmpPath);
                }
                unset($plugin, $bin);
            }
            // chmod to 755
            exec('chmod -R 755 '.$tmpPath);
            $this->createCatalogue($tmpPath, $javaHome, $ant, $buidScrtiptPath);
        } else {
            $this->logme('No plugins found');
        }
        // return xml
        return $this->_xml;
    }

    public function generateExperimentalCatalogue($version, $nbmPath, $tmpPath, $javaHome, $ant, $buidScrtiptPath) {
        $this->_nbmPath = $nbmPath;
        $this->_tmpPath = $tmpPath;
        $vers = Doctrine_Query::create()->from('PpNetbeansVersion v')->where('v.id=?', $version)->execute();
        $this->_version = $vers[0]->version;
        $bins = Doctrine_Query::create()->from('PpBinary b')
                        ->leftJoin('b.Verification as v')
                        ->innerJoin('b.Plugin as p')
                        ->where('b.version_id=?', $version)
                        ->andWhere('v.id IS NULL or v.status=0')
                        ->andWhere('b.nbm_filename IS NOT NULL')
                        ->andWhere('b.historic=0')
                        ->andWhere('p.published>0')->execute();
        if ($bins->count() > 0) {
            // cleanup tmp dir now
            exec('rm -rf ' . $tmpPath . '*');
            @mkdir($tmpPath, 0777);
            @chmod($tmpPath, 0777);
            foreach ($bins as $bin) {
                // move binaries into tmp folder so we can run ant task on them
                if (preg_match('#\.nbm$#',$bin->nbm_filename)) {
                    @copy($nbmPath . $bin->nbm_filename, $tmpPath . $bin->nbm_filename);
                    $this->logme('copying nbm module ' . $bin->nbm_filename . ' into ' . $tmpPath);
                } elseif (preg_match('#\.zip$#',$bin->nbm_filename)) {
                    $bundlePrefix = PpBinary::ZIP_MODULE_PREF . $bin->binary_id . '_*';
                    exec('cp ' . $nbmPath . $bundlePrefix . '  ' . $tmpPath);
                    $this->logme('copying .zip bundle submodules ' . $bundlePrefix . ' into ' . $tmpPath);
                }
                unset($bin);
            }
            $this->createCatalogue($tmpPath, $javaHome, $ant, $buidScrtiptPath);
        } else {
            $this->logme('No plugins found');
        }
        // return xml
        return $this->_xml;
    }

    public function createCatalogue($tmpPath, $javaHome, $ant, $buidScrtiptPath) {
        // fire up ant to create the catalog
        $setJavaHome = '';
        if ($javaHome) {
            $this->logme('setting JAVA_HOME to ' . $javaHome);
            $setJavaHome = 'export JAVA_HOME=' . $javaHome . ';';
        }
        $cmd = "$setJavaHome cd $buidScrtiptPath; export ANT_OPTS=-Xmx256m;$ant -verbose generate-uc-catalog";
        $this->logme('runing ant task: ' . $cmd);
        exec($cmd, $output);
        // now load generated catal content
        if (file_exists($tmpPath . self::ANT_CATALOG_FILE_XML)) {
            $this->_xml = file_get_contents($tmpPath . self::ANT_CATALOG_FILE_XML);
            $this->logme('Catalog creation complete ' . implode('<br/>', $output));
        } else {
            $this->logme('ERROR: ant task did not generate ' . $tmpPath . self::ANT_CATALOG_FILE_XML . '!<br/><br/>' . implode('<br/>', $output));
        }
    }

    public function saveXml($savePath, $filename = 'catalog.xml', $validate = true) {
        $ucUrl = parent::saveXml($savePath, $filename, $validate);
        // now also copy over licences
        $cmd = 'cp -r ' . $this->_tmpPath . 'licenses ' . $this->_nbmPath;
        exec($cmd);
        $this->logme('copying licences to nbm place: ' . $cmd);
        return $ucUrl;
    }

}

?>
