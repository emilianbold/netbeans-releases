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
        if ($this->_dbTable === null) {
            $this->setDbTable('Application_Model_DbTable_Disposition');
        }
        return $this->_dbTable;
    }

    public function save(Application_Model_Disposition $disposition) {
        $data = array(
            'text' => $disposition->getText()
        );
        $id = $disposition->getId();
        if ($id === null) {
            unset($data['id']);
            $this->getDbTable()->insert($data);
        } else {
            $this->getDbTable()->update($data, array('id = ?' => $id));
        }
    }

    public function fetchAll($query = null) {
        $table = $this->getDbTable();
        $select = $table->select();
        $select->from($table)
                ->order("text asc");
        if ($query !== null) {
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
            $entry->setText($row->text);
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

    public function delete($id) {
        $table = $this->getDbTable();
        $table->delete("id = $id");
    }

}

