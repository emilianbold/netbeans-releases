<?php

class Application_Model_PriceTypeMapper {

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

            $this->setDbTable('Application_Model_DbTable_PriceType');
        }

        return $this->_dbTable;
    }

    public function save(Application_Model_PriceType $price) {

        $data = array(
            'text_en' => $price->getText_en(),
            'priority' => $price->getPriority()
        );



        if (null === ($id = $price->getId())) {

            unset($data['id']);

            $this->getDbTable()->insert($data);
        } else {

            $this->getDbTable()->update($data, array('id = ?' => $id));
        }
    }

    public function find($id, Application_Model_PropertyBuildType $price) {

        $result = $this->getDbTable()->find($id);

        if (0 == count($result)) {

            return;
        }

        $row = $result->current();

        $price->setId($row->id);
        $price->setText_en($row->text_en);
        $price->setPriority($row->priority);
    }

    public function fetchAll() {

        $table = $this->getDbTable(); 
        $select = $table->select(); 
        $select->from($table)
                ->order("priority asc"); 
        $resultSet = $table->fetchAll($select); 

        foreach ($resultSet as $row) {

            $entry = new Application_Model_PriceType($options);

            $entry->setId($row->id);
            $entry->setText_en($row->text_en);
            $entry->setPriority($row->priority);
            

            $entries[] = $entry;
        }

        return $entries;
    }
}

?>
