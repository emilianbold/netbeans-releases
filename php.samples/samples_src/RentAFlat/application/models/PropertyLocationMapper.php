<?php

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * Description of PropertyLocationMapper
 *
 * @author Filip Zamboj (fzamboj@netbeans.org)
 */
class Application_Model_PropertyLocationMapper {

    protected $_dbTable;

    public function setDbTable($dbTable) {

        if (is_string($dbTable)) {

            $dbTable = new $dbTable();
        }

        if (!$dbTable instanceof Zend_Db_Table_Abstract) {

            throw new Exception('Invalid table data gateway provided');
        }

        $this->_dbTable = $dbTable;

        return $this;
    }

    public function getDbTable() {

        if (null === $this->_dbTable) {

            $this->setDbTable('Application_Model_DbTable_Location');
        }

        return $this->_dbTable;
    }

    public function save(Application_Model_PropertyLocation $location) {

        $data = array(
            'city_part' => $location->getCityPart(),
            'city' => $location->getCity(),
            
        );



        if (null === ($id = $location->getId())) {

            unset($data['id']);

            $this->getDbTable()->insert($data);
        } else {

            $this->getDbTable()->update($data, array('id = ?' => $id));
        }
    }

    public function find($id, Application_Model_PropertyLocation $location) {

        $result = $this->getDbTable()->find($id);

        if (0 == count($result)) {

            return;
        }

        $row = $result->current();

        
        $location->setCityPart($row->city_part);
        
        $location->setCity($row->city);
        
    }

    
    public function fetchAll($query = null) {

        if ($query == null)
            $resultSet = $this->getDbTable()->fetchAll();
        else
            $resultSet = $this->getDbTable()->fetchAll($query);

        return $this->processResults($resultSet);
    }

    private function processResults($resultSet) {
        $entries = array();

        foreach ($resultSet as $row) {

            $entry = new Application_Model_PropertyLocation();

            $entry->setId($row->id);
            $entry->setCityPart($row->city_part);
            $entry->setCity($row->city);

            $entries[] = $entry;
        }

        return $entries;
    }

    

}

?>
