<?php

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * Description of DispositionMapper
 *
 * @author Filip Zamboj (fzamboj@netbeans.org)
 */
class Application_Model_DispositionMapper {

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

            $this->setDbTable('Application_Model_DbTable_Disposition');
        }

        return $this->_dbTable;
    }

    public function save(Application_Model_Disposition $disposition) {

        $data = array(
            'text_en' => $disposition->getText_en()
        );



        if (null === ($id = $disposition->getId())) {

            unset($data['id']);

            $this->getDbTable()->insert($data);
        } else {

            $this->getDbTable()->update($data, array('id = ?' => $id));
        }
    }

    public function find($id, Application_Model_Content $content) {

        $result = $this->getDbTable()->find($id);

        if (0 == count($result)) {

            return;
        }

        $row = $result->current();

        $content->setId($row->id)
                ->setText_en($row->text_en);
    }

    public function fetchAllUsed($city, $cp, $pType, $offer_id, $cityMatch) {

        $locationQuery = "";
        if ($cityMatch == true) {
            if (count($cp) > 0) {
                foreach ($cp as $v) {
                    if (strlen($locationQuery) > 0)
                        $locationQuery .= " or ";
                    $locationQuery .= " location_id = " . $v . " ";
                }
            } else {
                $lm = new Application_Model_PropertyLocationMapper();
                $lmr = $lm->fetchUsedLocationsParts($city);

                foreach ($lmr as $v) {
                    if (strlen($locationQuery) > 0)
                        $locationQuery .= " or ";
                    $locationQuery .= " location_id = " . $v->getId() . " ";
                }
            }
        }


        $pm = new Application_Model_PropertyMapper();
        $dQuery = "";

        if (strlen($locationQuery) > 0 && !($pType == null))
            $fpQuery = " ($locationQuery) and property_type_id = $pType";
        else if (strlen($locationQuery) == 0 && !($pType == null))
            $fpQuery = " property_type_id = $pType";
        else if (strlen($locationQuery) > 0 && ($pType == null))
            $fpQuery = " $locationQuery ";
        else
            $fpQuery = "";

        if (strlen($fpQuery) > 0)
            $fpQuery .= " and offer_id = $offer_id";
        else
            $fpQuery = "offer_id = $offer_id";

//        var_dump($fpQuery);
        foreach ($pm->fetchAll($fpQuery. " and disabled = 0") as $v) {
            if (strlen($dQuery) > 0)
                $dQuery .= " or ";
            $dQuery .= " id = " . $v->getDisposition_id() . " ";
        }
        if (strlen($dQuery) == 0)
            return null;
        $table = $this->getDbTable();
        $select = $table->select();
        $select->from($table)
                ->where($dQuery)
                ->group("id");
        $final = $this->fetchAll($dQuery);

        return $final;
    }

    public function fetchAll($query = null) {
        $table = $this->getDbTable();
        $select = $table->select();
        $select->from($table)
                ->order("text_en asc");
        if (!($query == null)) {
            $select->where($query);
        }
        $select->order("id asc");

        $resultSet = $table->fetchAll($select);
        return $this->processResultSet($resultSet);
    }

    private function processResultSet($resultSet) {
        $entries = array();

        foreach ($resultSet as $row) {

            $entry = new Application_Model_Disposition();

            $entry->setId($row->id);
            $entry->setText_en($row->text_en);

            $entries[] = $entry;
        }

        return $entries;
    }

    public function getCount() {
        $table = $this->getDbTable();
        $select = $table->select();
        $select->from($table, array("count(id) as sum"));

        $resultSet = $table->fetchAll($select);
        $array = $resultSet->toArray();

        return $array[0]['sum'];
    }

    public function fetchAllFilterToArray($query) {
        $table = $this->getDbTable();
        $select = $table->select();
        $select->from($table)
                ->where($query);
        $resultSet = $table->fetchAll($select);
        $toArray = $resultSet->toArray();
        return $toArray[0];
    }

    public function delete($id) {
        $table = $this->getDbTable();
        $table->delete("id = $id");
    }

}

?>
