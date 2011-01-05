<?php

/**
 * 
 *
 * @author Filip Zamboj (fzamboj@netbeans.org)
 */
class Application_Model_PropertyMapper {

    /**
     *
     * @global String
     */
    protected $_dbTable;

    /**
     *
     * @param type $dbTable
     * @return Application_Model_PropertyMapper 
     */
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

    /**
     *
     * @return Application_Model_DbTable_Property
     */
    public function getDbTable() {

        if (null === $this->_dbTable) {

            $this->setDbTable('Application_Model_DbTable_Property');
        }

        return $this->_dbTable;
    }

    /**
     *
     * @param Application_Model_Property $property
     * @return type 
     */
    public function save(Application_Model_Property $property) {

        $data = array(
            'reference_no' => $property->getReference_no(),
            'title_en' => $property->getTitle_en(),
            'text_en' => $property->getText_en(),
            'disposition_id' => $property->getDisposition_id(),
            'area' => $property->getArea(),
            'floor' => $property->getFloor(),
            'lift' => $property->getLift(),
            'cellar' => $property->getCellar(),
            'balcony' => $property->getBalcony(),
            'location_id' => $property->getLocation_id(),
            'price' => $property->getPrice(),
            'created_on' => date('Y-m-d H-i-s'),
            'street' => $property->getStreet(),
            'property_build_id' => $property->getProperty_build_id(),
            'terace' => $property->getTerace(),
            'loggia' => $property->getLoggia(),
            'garden' => $property->getGarden(),
            'garage' => $property->getGarage(),
            'parking_place' => $property->getParking_place(),
        );



        if (null === ($id = $property->getId())) {

            unset($data['id']);

            return $this->getDbTable()->insert($data);
        } else {

            $this->getDbTable()->update($data, array('id = ?' => $id));
            return $id;
        }
    }

    /**
     *
     * @param type $id
     * @param Application_Model_Property $property
     * @return type 
     */
    public function find($id, Application_Model_Property $property) {

        $result = $this->getDbTable()->find($id);

        if (0 == count($result)) {

            return;
        }

        $row = $result->current();

        $property->setId($row->id)
                ->setTitle_en($row->title_en)
                ->setText_en($row->text_en)
           
                ->setDisposition_id($row->diposition_id)
                ->setArea($row->area)
                ->setFloor($row->floor)
                ->setLift($row->lift)
                ->setCellar($row->cellar)
                ->setBalcony($row->balcony)
                ->setProperty_type_id($row->property_type_id)
                ->setLocation_id($row->location_id)
                ->setPrice($row->price)
                ->setCreated_on($row->created_on)
                ->setDisabled($row->disabled)
                ->setStreet($row->street);
    }

    /**
     *
     * @param type $resultSet
     * @return Application_Model_Property 
     */
    private function processResultSet($resultSet) {
        $entries = array();
        foreach ($resultSet as $row) {

            $entry = new Application_Model_Property();

            $entry->setId($row->id);
            $entry->setReference_no($row->reference_no);
            $entry->setTitle_en($row->title_en);
            $entry->setText_en($row->text_en);
            $entry->setDisposition_id($row->disposition_id);
            $entry->setArea($row->area);
            $entry->setFloor($row->floor);
            $entry->setLift($row->lift);
            $entry->setCellar($row->cellar);
            $entry->setBalcony($row->balcony);
            $entry->setLocation_id($row->location_id);
            $entry->setPrice($row->price);
            $entry->setCreated_on($row->created_on);
            $entry->setStreet($row->street);
            $entry->setProperty_build_id($row->property_build_id);
            $entry->setTerace($row->terace);
            $entry->setLoggia($row->loggia);
            $entry->setGarden($row->garden);
            $entry->setGarage($row->garage);
            $entry->setParking_place($row->parking_place);

            $entries[] = $entry;
        }

        return $entries;
    }

    /**
     *
     * @return type 
     */
    public function fetchAll($query = NULL) {
        if ($query === NULL) {
            $resultSet = $this->getDbTable()->fetchAll();
            
        } else {
            $table = $this->getDbTable();
            $select = $table->select();
            $select->from($table)
                    ->where($query);

            $resultSet = $this->getDbTable()->fetchAll($select);
        }
        return $this->processResultSet($resultSet);
    }

}

?>
