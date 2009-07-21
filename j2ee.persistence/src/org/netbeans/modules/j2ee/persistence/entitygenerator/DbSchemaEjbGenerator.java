/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.j2ee.persistence.entitygenerator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.dbschema.ColumnElement;
import org.netbeans.modules.dbschema.ColumnPairElement;
import org.netbeans.modules.dbschema.DBIdentifier;
import org.netbeans.modules.dbschema.ForeignKeyElement;
import org.netbeans.modules.dbschema.SchemaElement;
import org.netbeans.modules.dbschema.TableElement;
import org.netbeans.modules.dbschema.UniqueKeyElement;
import org.netbeans.modules.j2ee.persistence.entitygenerator.CMPMappingModel.ColumnData;
import org.netbeans.modules.j2ee.persistence.entitygenerator.EntityRelation.CollectionType;

/**
 * This class provides an algorithm to produce a set of cmp beans and relations
 * from a dbschema element.
 *
 * @author Chris Webster, Martin Adamek, Andrei Badea
 */
public class DbSchemaEjbGenerator {
    
    private GeneratedTables genTables;
    private Map beans = new HashMap();
    private List relations = new ArrayList();
    private SchemaElement schemaElement;
    private Set<String> tablesReferecedByOtherTables;
    private final CollectionType colectionType;
    
    /**
     * Creates a generator for a set of beans.
     *
     * @param genTables contains the tables to generate and their respective locations.
     * @param schemaElement the dbschema containing the tables to generate beans for.
     */
    public DbSchemaEjbGenerator(GeneratedTables genTables, SchemaElement schemaElement) {
        this(genTables, schemaElement, CollectionType.COLLECTION);
    }

    /**
     * Creates a generator for a set of beans.
     *
     * @param genTables contains the tables to generate and their respective locations.
     * @param schemaElement the dbschema containing the tables to generate beans for.
     * @param collectionType collection type is used in some names generation
     */
    public DbSchemaEjbGenerator(GeneratedTables genTables, SchemaElement schemaElement, CollectionType collectionType) {
        this.schemaElement = schemaElement;
        this.genTables = genTables;
        this.colectionType = collectionType;

        tablesReferecedByOtherTables = getTablesReferecedByOtherTables(schemaElement);
        buildCMPSet();
    }
    /**
     * 
     * @param schemaElement The schema
     * @return A set of tables that are referenced by at least one another table
     */
    public static Set<String> getTablesReferecedByOtherTables(SchemaElement schemaElement) {
        Set<String> tableNames = new HashSet<String>();
        TableElement[] allTables = schemaElement.getTables();
        for(int i = 0; i < allTables.length; i ++ ) {
            ForeignKeyElement[] fkElements = allTables[i].getForeignKeys();
            for(int fkix = 0; fkix < fkElements.length; fkix ++ ) {
                tableNames.add(fkElements[fkix].getReferencedTable().getName().getName());
            }
        }
        
        return tableNames;
    }
    
    /**
     * Returns true if the table is a join table. A table is considered
     * a join table regardless of whether the tables it joins are
     * included in the tables to generate.
     */
    public static boolean isJoinTable(TableElement e, Set<String> tablesReferecedByOtherTables) {
        ForeignKeyElement[] foreignKeys = e.getForeignKeys();
        if (foreignKeys == null ||
                foreignKeys.length != 2) {
            return false;
        }
        
        int foreignKeySize = foreignKeys[0].getColumns().length +
                foreignKeys[1].getColumns().length;
        
        if (foreignKeySize < e.getColumns().length) {
            return false;
        }
        
        // issue 89576: a table which references itself is not a join table
        String tableName = e.getName().getName();
        for (int i = 0; i < 2; i++) {
            if (tableName.equals(foreignKeys[i].getReferencedTable().getName().getName())) {
                return false;
            }
        }
        
        // issue 90962: a table whose foreign keys are unique is not a join table
        if (isFkUnique(foreignKeys[0]) || isFkUnique(foreignKeys[1])) {
            return false;
        }
        
        // issue 111397: a table which is referenced by another table is not a join table.
        if(tablesReferecedByOtherTables.contains(e.getName().getName())) {
            return false;
        }
        
        return true;
    }

    private boolean isForeignKey(ForeignKeyElement[] fks,
            ColumnElement col) {
        if (fks == null) {
            return false;
        }
        
        for (int i = 0; i < fks.length; i++) {
            if (fks[i].getColumn(col.getName()) != null) {
                return true;
            }
        }
        
        return false;
    }
    
    public EntityClass[] getBeans() {
        return (EntityClass[])beans.values().toArray(new EntityClass[beans.size()]);
    }
    
    public EntityRelation[] getRelations() {
        return (EntityRelation[])relations.toArray(new EntityRelation[relations.size()]);
    }
    
    
    private EntityClass getBean(String tableName) {
        return (EntityClass)beans.get(tableName);
    }
    
    private EntityClass addBean(String tableName) {
        EntityClass bean = getBean(tableName);
        if (bean != null) {
            return bean;
        }
        
        bean = new EntityClass(
                genTables.getCatalog(),
                genTables.getSchema(),
                tableName,
                genTables.getRootFolder(tableName),
                genTables.getPackageName(tableName),
                genTables.getClassName(tableName),
                genTables.getUniqueConstraints(tableName) );
        beans.put(tableName, bean);
        
        return bean;
    }
    
    private void addAllTables() {
        List<TableElement> joinTables = new LinkedList<TableElement>();
        for (String tableName : genTables.getTableNames()) {
            TableElement tableElement =
                    schemaElement.getTable(DBIdentifier.create(tableName));
            if (isJoinTable(tableElement, tablesReferecedByOtherTables)) {
                joinTables.add(tableElement);
            } else {
                addBean(tableName);
            }
        }
        for (TableElement joinTable : joinTables) {
            addJoinTable(joinTable);
        }
    }
    
    private ColumnData[] getLocalColumnData(ForeignKeyElement key) {
        ColumnPairElement[] pkPairs = key.getColumnPairs();
        ColumnData[] localColumns = new ColumnData[pkPairs.length];
        for (int i = 0; i < pkPairs.length; i++) {
            localColumns[i] = new ColumnData(pkPairs[i].getLocalColumn().getName().getName(),
                    pkPairs[i].getLocalColumn().isNullable());
        }
        return localColumns;
    }
    
    private ColumnData[] getReferencedColumnData(ForeignKeyElement key) {
        ColumnPairElement[] pkPairs = key.getColumnPairs();
        ColumnData[] refColumns = new ColumnData[pkPairs.length];
        for (int i = 0; i < pkPairs.length; i++) {
            refColumns[i] = new ColumnData(pkPairs[i].getReferencedColumn().getName().getName(),
                    pkPairs[i].getReferencedColumn().isNullable());
        }
        return refColumns;
    }
    /**
     * Provide a role name based on the foreign key column.
     * @return role name based on foreign key column or default name
     */
    private String getRoleName(ForeignKeyElement fk, String defaultName) {
        ColumnPairElement[] pkPairs = fk.getColumnPairs();
        if (pkPairs == null || pkPairs.length > 1) {
            return defaultName;
        }
        return EntityMember.makeClassName(
                pkPairs[0].getLocalColumn().getName().getName());
    }
    
    private void addJoinTable(TableElement table) {
        ForeignKeyElement[] foreignKeys = table.getForeignKeys();
        String tableAName = foreignKeys[0].getReferencedTable().getName().getName();
        String tableBName = foreignKeys[1].getReferencedTable().getName().getName();
        
        // create role A
        EntityClass roleAHelper = getBean(tableAName);
        EntityClass roleBHelper = getBean(tableBName);
        
        String roleAname = getRoleName(foreignKeys[0], roleAHelper.getClassName());
        String roleBname = getRoleName(foreignKeys[1], roleBHelper.getClassName());
        
        String roleACmr = EntityMember.makeRelationshipFieldName(roleBHelper.getClassName(), colectionType, true);
        String roleBCmr = EntityMember.makeRelationshipFieldName(roleAHelper.getClassName(), colectionType, true);
        
        roleACmr = uniqueAlgorithm(getFieldNames(roleAHelper), roleACmr, null);
        List roleBFieldNames = getFieldNames(roleBHelper);
        if (tableAName.equals(tableBName)) {
            // Handle the special case when both parts of the join table reference
            // the same table -- in that case both roleACmr and roleBCmr
            // will be added to the same class, but they have the same name.
            // So pretend roleACmr was already added when computing an unique
            // name for roleBCmr
            roleBFieldNames.add(roleACmr);
        }
        roleBCmr = uniqueAlgorithm(roleBFieldNames, roleBCmr, null);
        
        RelationshipRole roleA = new RelationshipRole(
                roleAname,
                roleAHelper.getClassName(),
                roleACmr,
                true,
                true,
                false);
        roleA.setEntityPkgName(roleAHelper.getPackage());
        roleAHelper.addRole(roleA);
        
        RelationshipRole roleB = new RelationshipRole(
                roleBname,
                roleBHelper.getClassName(),
                roleBCmr,
                true,
                true, 
                false);
        roleB.setEntityPkgName(roleBHelper.getPackage());
        roleBHelper.addRole(roleB);
        
        EntityRelation relation = new EntityRelation(roleA, roleB);
        relations.add(relation);
        
        relation.setRelationName(EntityMember.makeClassName(table.getName().getName()));
        
        roleAHelper.getCMPMapping().getJoinTableMapping().put(roleACmr, table.getName().getName());
        CMPMappingModel.JoinTableColumnMapping joinColMapA = new CMPMappingModel.JoinTableColumnMapping();
        joinColMapA.setColumns(getColumnData(foreignKeys[0].getColumns()));
        joinColMapA.setReferencedColumns(getColumnData(foreignKeys[0].getReferencedColumns()));
        joinColMapA.setInverseColumns(getColumnData(foreignKeys[1].getColumns()));
        joinColMapA.setReferencedInverseColumns(getColumnData(foreignKeys[1].getReferencedColumns()));
        roleAHelper.getCMPMapping().getJoinTableColumnMppings().put(roleACmr, joinColMapA);
                
        roleBHelper.getCMPMapping().getJoinTableMapping().put(roleBCmr, table.getName().getName());
        CMPMappingModel.JoinTableColumnMapping joinColMapB = new CMPMappingModel.JoinTableColumnMapping();
        joinColMapB.setColumns(getColumnData(foreignKeys[1].getColumns()));
        joinColMapB.setReferencedColumns(getColumnData(foreignKeys[1].getReferencedColumns()));
        joinColMapB.setInverseColumns(getColumnData(foreignKeys[0].getColumns()));
        joinColMapB.setReferencedInverseColumns(getColumnData(foreignKeys[0].getReferencedColumns()));
        roleBHelper.getCMPMapping().getJoinTableColumnMppings().put(roleBCmr, joinColMapB);

    }
    
    private ColumnData[] getColumnData(ColumnElement[] cols) {
        ColumnData[] columns = new ColumnData[cols.length];
        for (int i = 0; i < cols.length; i++) {
            columns [i] = new ColumnData(cols[i].getName().getName(), cols[i].isNullable());
        }
        return columns;
    }
    
    private static boolean containsSameColumns(ColumnElement[] fkColumns,
            UniqueKeyElement uk) {
        if (fkColumns.length == uk.getColumns().length) {
            for (int i = 0; i < fkColumns.length; i++) {
                if (uk.getColumn(fkColumns[i].getName())==null) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }
    
    private boolean containsColumns(ColumnElement[] fkColumns,
            UniqueKeyElement uk) {
        if (uk == null) {
            return false;
        }
        
        for (int i = 0; i < fkColumns.length; i++) {
            if (uk.getColumn(fkColumns[i].getName())!=null) {
                return true;
            }
        }
        return false;
    }
    
    private static boolean isFkUnique(ForeignKeyElement key) {
        UniqueKeyElement[] uk = key.getDeclaringTable().getUniqueKeys();
        if (uk == null) {
            return false;
        }
        
        ColumnElement[] columns = key.getColumns();
        for (int uin=0; uin < uk.length; uin++) {
            if (containsSameColumns(columns, uk[uin])) {
                return true;
            }
        }
        
        return false;
    }

    // returns true if all of the columns are nullable
    private boolean isNullable(ForeignKeyElement key) {
        ColumnElement[] columns = key.getColumns();
        int i, count = ((columns != null) ? columns.length : 0);

        for (i=0; i < count; i++) {
            if (!columns[i].isNullable()) {
                return false;
            }
        }
        
        return true;
    }

    private static UniqueKeyElement getPrimaryOrCandidateKey(TableElement table) {
        UniqueKeyElement pk = table.getPrimaryKey();
        if (pk != null) {
            return pk;
        }
        
        UniqueKeyElement[] keys = table.getUniqueKeys();
        if (keys == null || keys.length == 0) {
            return null;
        }
        
        pk = keys[0];
        for (int i = 1; i < keys.length; i++) {
            if (keys[i].getColumns().length < pk.getColumns().length) {
                pk = keys[i];
            }
        }
        return pk;
    }
    
    private void generatePkField(ColumnElement column, boolean inPk, boolean pkField) {
        EntityMember m = EntityMember.create(column);
        m.setPrimaryKey(inPk, pkField);
        EntityClass bean = getBean(column.getDeclaringTable().getName().getName());
        m.setMemberName(uniqueAlgorithm(getFieldNames(bean), m.getMemberName(), null));
        bean.getFields().add(m);
    }
    
    private void generateRelationship(ForeignKeyElement key) {
        String keyTableName = key.getDeclaringTable().getName().getName();
        String keyRefName = key.getReferencedTable().getName().getName();
        boolean oneToOne = isFkUnique(key);
        
        EntityClass roleBHelper = getBean(keyRefName);
        if (roleBHelper == null) {
            return;
        }
        EntityClass roleAHelper = getBean(keyTableName);
        if (roleAHelper == null) {
            return;
        }

        // create role B (it's the table which contains the foreign key)
        String roleBCmr = EntityMember.makeRelationshipFieldName(
                roleAHelper.getClassName(), colectionType, !oneToOne);
        roleBCmr = uniqueAlgorithm(getFieldNames(roleBHelper), roleBCmr, null);
        RelationshipRole roleB = new RelationshipRole(
                //TODO ask generator for default role name, do not assume it is EJB name
                getRoleName(key, roleBHelper.getClassName()),
                roleBHelper.getClassName(),
                roleBCmr,
                false,
                !oneToOne,
                !isNullable(key),
                isNullable(key));
        roleB.setEntityPkgName(roleBHelper.getPackage());
        roleBHelper.addRole(roleB);
        
        // role A
        String roleACmr = EntityMember.makeRelationshipFieldName(
                roleBHelper.getClassName(), colectionType, false);
        
        /* only use database column name if a column is not required by the
           primary key. If a column is already required by the primary key
           then executing this code would cause the cmr-field name to be
           named cmp-fieldname1. Therefore, we do not change the cmr-field
           name and instead use the name of the other ejb (default).
         */
        if (!containsColumns(key.getColumns(), getPrimaryOrCandidateKey(key.getDeclaringTable()))) {
            roleACmr = EntityMember.makeRelationshipFieldName(roleB.getRoleName(), colectionType, false);
        }
        
        roleACmr = uniqueAlgorithm(getFieldNames(roleAHelper), roleACmr, null);
        
        RelationshipRole roleA = new RelationshipRole(
                //TODO ask generator for default role name, do not assume it is EJB name
                getRoleName(key, roleAHelper.getClassName()),
                roleAHelper.getClassName(),
                roleACmr,
                !oneToOne,
                false,
                false,
                isNullable(key));
        roleA.setEntityPkgName(roleAHelper.getPackage());
        roleAHelper.addRole(roleA);
        
        EntityRelation relation = new EntityRelation(roleA, roleB);
        relation.setRelationName(roleA.getEntityName() + '-' + roleB.getEntityName()); // NOI18N
        relations.add(relation);
        
        roleAHelper.getCMPMapping().getCmrFieldMapping().put(roleACmr, getLocalColumnData(key));
        roleBHelper.getCMPMapping().getCmrFieldMapping().put(roleBCmr, getReferencedColumnData(key));
    }
    
    private void reset() {
        beans.clear();
        relations.clear();
    }
    
    private void buildCMPSet() {
        reset();
        addAllTables();
        for (Iterator it = beans.keySet().iterator(); it.hasNext();) {
            String tableName = it.next().toString();
            TableElement table = schemaElement.getTable(DBIdentifier.create(tableName));
            ColumnElement[] cols = table.getColumns();
            UniqueKeyElement pk = getPrimaryOrCandidateKey(table);
            ForeignKeyElement[] fkeys = table.getForeignKeys();
            for (int col = 0; col < cols.length; col++) {
                if (pk != null &&
                        pk.getColumn(cols[col].getName()) != null) {
                    generatePkField(cols[col],true, pk.getColumns().length==1);
                } else {
                    // TODO add check to see if table is included
                    if (!isForeignKey(fkeys, cols[col])){
                        generatePkField(cols[col], false, false);
                    }
                }
            }
            
            for (int fk = 0 ; fkeys != null && fkeys.length > fk; fk++) {
                generateRelationship(fkeys[fk]);
            }
            EntityClass helperData = getBean(tableName);
            helperData.usePkField(pk!= null && pk.getColumns().length == 1);
            helperData.setIsForTable(table.isTable());
        }
        makeRelationsUnique();
    }
    
    private List getFieldNames(EntityClass bean) {
        List result = new ArrayList();
        for (Iterator i = bean.getFields().iterator(); i.hasNext();) {
            EntityMember member = (EntityMember)i.next();
            result.add(member.getMemberName());
        }
        for (Iterator i = bean.getRoles().iterator(); i.hasNext();) {
            RelationshipRole role = (RelationshipRole)i.next();
            result.add(role.getFieldName());
        }
        return result;
    }
    
    /**
     * This method will make the relationships unique
     */
    private EntityRelation[] makeRelationsUnique() {
        EntityRelation[] r = getRelations();
        List relationNames = new ArrayList(r.length);
        for (int i = 0; i < r.length; i++) {
            r[i].makeRoleNamesUnique();
            String baseName = r[i].getRelationName();
            r[i].setRelationName(uniqueAlgorithm(relationNames, baseName, "-")); // NOI18N
        }
        return r;
    }
    
    /**
     * return name generated or base name if this was ok
     */
    private static String uniqueAlgorithm(List names, String baseName, String sep) {
        String newName = baseName;
        int unique = 0;
        while (names.contains(newName)) {
            String ins = (sep == null? "":sep); // NOI18N
            newName = baseName + ins + String.valueOf(++unique);
        }
        names.add(newName);
        return newName;
    }
}
