<?php
/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */

class Application_Model_PropertyMapper {

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

    /**
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
            'title' => $property->getTitle(),
            'text' => $property->getText(),
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
                ->setTitle($row->title)
                ->setText($row->text)
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
            $entry->setTitle($row->title);
            $entry->setText($row->text);
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

    public function fetchByIds(array $ids) {
        if (count($ids) == 0) {
            return array();
        }
        $in = "id IN(" . implode(", ", $ids) . ")";
        $select = $this->getDbTable()
                ->select()
                ->from($this->getDbTable())
                ->where($in);
        $resultSet = $this->getDbTable()->fetchAll($select);
        return $this->processResultSet($resultSet);
    }

}

