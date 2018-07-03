<?php

namespace Synergy\Controller;

use Synergy\DB\VersionDAO;
use Synergy\Model\Version;

/**
 * Description of VersionCtrl
 *
 * @author lada
 */
class VersionCtrl {

    private $versionDao;
    private $specDao;

    function __construct() {
        $this->specDao = new SpecificationCtrl();
        $this->versionDao = new VersionDAO();
    }

    /**
     * Returns version by name
     * @param string $name version name
     * @return Version
     */
    public function getVersionByName($name) {
        if (isset($name) && strlen($name)) {
            return $this->versionDao->getVersionByName($name);
        } else {
            return $this->getLatestVersion();
        }
    }

    /**
     * Returns latest version (searched by name)
     * @return Version
     */
    public function getLatestVersion() {
        return $this->versionDao->getLatestVersion();
    }

    /**
     * Returns all versions (even obsolete)
     * @return Version[]
     */
    public function getAllVersions() {
        return $this->versionDao->getVersions();
    }

    /**
     * Updates version
     * @param int $id version ID
     * @param string $newname new version name
     * @return boolean true if successful
     */
    public function updateVersion($id, $newname, $obsolete) {

        if ($this->versionDao->isNameUsed($newname, $id))
            return false;
        return $this->versionDao->updateVersion($id, $newname, $obsolete);
    }

    /**
     * Removes version
     * @param int $id version ID
     * @return boolean true if successful
     */
    public function deleteVersion($id) {
        // first delete
        $specifications = $this->specDao->getSpecifications($id);
        foreach ($specifications as $spec) {
            $this->specDao->deleteSpecification($spec->id);
        }

        $this->versionDao->makeObsolete($id);
        return true;
    }

    /**
     * Creates new version
     * @param string $newname new version name
     * @return boolean true if successful
     */
    public function createVersion($newname) {
        if ($this->versionDao->isNameUsed($newname))
            return false;
        return $this->versionDao->createVersion($newname);
    }

    /**
     * Returns all versions that are not obsolete
     * @return type
     */
    public function getVersions() {
        return $this->versionDao->getCurrentVersions();
    }

}

?>
