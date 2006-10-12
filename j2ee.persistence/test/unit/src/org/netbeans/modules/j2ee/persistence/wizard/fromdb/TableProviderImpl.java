/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.persistence.wizard.fromdb;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.dbschema.ForeignKeyElement;
import org.netbeans.modules.dbschema.TableElement;
import org.netbeans.modules.j2ee.persistence.wizard.fromdb.Table.DisabledReason;

/**
 *
 * @author Andrei Badea
 */
public class TableProviderImpl implements TableProvider {

    private final Set<Table> tables;
    private final Map<String, Table> name2Table = new HashMap<String, Table>();

    public TableProviderImpl(Map<String, Set<String>> tablesAndRefs) {
        this(tablesAndRefs, emptyDisabledReasonMap());
    }

    public TableProviderImpl(Map<String, Set<String>> tablesAndRefs, Map<String, DisabledReason> disabledReasons) {
        Map<String, TableImpl> name2Table = new HashMap<String, TableImpl>();
        Map<String, Set<Table>> name2Referenced = new HashMap<String, Set<Table>>();
        Map<String, Set<Table>> name2ReferencedBy = new HashMap<String, Set<Table>>();
        Map<String, Set<Table>> name2Join = new HashMap<String, Set<Table>>();

        // need to create all the tables first
        for (String tableName : tablesAndRefs.keySet()) {
            DisabledReason disabledReason = disabledReasons.get(tableName);
            boolean join = tableName.contains("_");
            TableImpl table = new TableImpl(tableName, join, disabledReason);

            name2Table.put(tableName, table);
            name2Referenced.put(tableName, new HashSet<Table>());
            name2ReferencedBy.put(tableName, new HashSet<Table>());
            name2Join.put(tableName, new HashSet<Table>());
        }

        // referenced, referenced by and join tables
        for (String tableName : tablesAndRefs.keySet()) {
            Table table = name2Table.get(tableName);

            for (String referencedTableName : tablesAndRefs.get(tableName)) {
                Table referencedTable = name2Table.get(referencedTableName);

                name2Referenced.get(tableName).add(referencedTable);
                name2ReferencedBy.get(referencedTableName).add(table);

                if (table.isJoin()) {
                    name2Join.get(referencedTableName).add(table);
                }
            }
        }

        Set<Table> tmpTables = new HashSet<Table>();
        for (TableImpl table : name2Table.values()) {
            String tableName = table.getName();

            table.setReferencedTables(Collections.unmodifiableSet(name2Referenced.get(tableName)));
            table.setReferencedByTables(Collections.unmodifiableSet(name2ReferencedBy.get(tableName)));
            table.setJoinTables(Collections.unmodifiableSet(name2Join.get(tableName)));

            tmpTables.add(table);
            this.name2Table.put(table.getName(), table);
        }
        tables = Collections.unmodifiableSet(tmpTables);
    }

    public Set<Table> getTables() {
        return tables;
    }

    Table getTableByName(String name) {
        return name2Table.get(name);
    }

    private static Map<String, DisabledReason> emptyDisabledReasonMap() {
        return Collections.emptyMap();
    }

    private static final class TableImpl extends Table {

        private Set<Table> referencedTables;
        private Set<Table> referencedByTables;
        private Set<Table> joinTables;

        public TableImpl(String name, boolean join, DisabledReason disabledReason) {
            super(name, join, disabledReason);
        }

        private void setReferencedTables(Set<Table> referencedTables) {
            this.referencedTables = referencedTables;
        }

        public Set<Table> getReferencedTables() {
            return referencedTables;
        }

        private void setReferencedByTables(Set<Table> referencedByTables) {
            this.referencedByTables = referencedByTables;
        }

        public Set<Table> getReferencedByTables() {
            return referencedByTables;
        }

        private void setJoinTables(Set<Table> joinTables) {
            this.joinTables = joinTables;
        }

        public Set<Table> getJoinTables() {
            return joinTables;
        }
    }
}
