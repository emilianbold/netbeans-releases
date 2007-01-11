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

import java.util.*;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.persistence.dd.persistence.model_1_0.PersistenceUnit;
import org.netbeans.modules.j2ee.persistence.entitygenerator.DbSchemaEjbGenerator;
import org.netbeans.modules.j2ee.persistence.entitygenerator.EntityClass;
import org.netbeans.modules.j2ee.persistence.entitygenerator.EntityRelation;
import org.netbeans.modules.j2ee.persistence.entitygenerator.GeneratedTables;
import org.netbeans.modules.j2ee.persistence.wizard.Util;
import org.openide.filesystems.*;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.dbschema.SchemaElement;

/**
 * This class provides a simple collector for information necessary to support
 * the CMP set wizard. 
 *
 * @author Chris Webster, Andrei Badea
 */
public class RelatedCMPHelper {

    private final Project project;
    private final FileObject configFilesFolder;
    private final PersistenceGenerator persistenceGen;
    private final DBSchemaFileList dbschemaFileList;
    
    private SchemaElement schemaElement;
    private DatabaseConnection dbconn;
    private FileObject dbschemaFile;
    private String datasourceName;

    private TableClosure tableClosure;
    private SelectedTables selectedTables;
    
    private SourceGroup location;
    private String packageName;
    
    private boolean cmpFieldsInInterface;
    private boolean generateFinderMethods;
    
    private DbSchemaEjbGenerator generator;
    
    private TableSource tableSource;
    
    private PersistenceUnit persistenceUnit;
    
    public RelatedCMPHelper(Project project, FileObject configFilesFolder, PersistenceGenerator persistenceGen) {
        this.project = project;
        this.configFilesFolder = configFilesFolder;
        this.persistenceGen = persistenceGen;
        
        tableSource = TableSource.get(project);
        dbschemaFileList = new DBSchemaFileList(project, configFilesFolder);
    }
    
    public Project getProject() {
        return project;
    }
    
    FileObject getConfigFilesFolder() {
        return configFilesFolder;
    }
    
    PersistenceGenerator getPersistenceGenerator() {
        return persistenceGen;
    }
    
    public DBSchemaFileList getDBSchemaFileList() {
        return dbschemaFileList;
    }
    
    public void setTableClosure(TableClosure tableClosure) {
        assert tableClosure != null;
        this.tableClosure = tableClosure;
    }
    
    public TableClosure getTableClosure() {
        return tableClosure;
    }
    
    public void setSelectedTables(SelectedTables selectedTables) {
        assert selectedTables != null;
        this.selectedTables = selectedTables;
    }

    public PersistenceUnit getPersistenceUnit() {
        return persistenceUnit;
    }

    public void setPersistenceUnit(PersistenceUnit persistenceUnit) {
        this.persistenceUnit = persistenceUnit;
    }
    
    /**
     * Sets the source of the tables when the source is a database connection
     * (possibly retrieved from a data source).
     *
     * @param  schemaElement the SchemaElement instance containing the database tables.
     * @param  dbconn the database connection which was used to retrieve <code>schemaElement</code>.
     * @param  dataSourceName the JNDI name of the {@link org.netbeans.modules.j2ee.deployment.common.api.Datasource data source}
     *         which was used to retrieve <code>dbconn</code> or null if the connection 
     *         was not retrieved from a data source.
     */
    public void setTableSource(SchemaElement schemaElement, DatabaseConnection dbconn, String datasourceName) {
        this.schemaElement = schemaElement;
        this.dbconn = dbconn;
        this.dbschemaFile = null;
        this.datasourceName = datasourceName;
        
        updateTableSource();
    }
    
    /**
     * Sets the source of the tables when the source is a dbschema file.
     *
     * @param  schemaElement the SchemaElement instance containing the database tables.
     * @param  dbschemaFile the dbschema file which was used to retrieve <code>schemaElement</code>.
     */
    public void setTableSource(SchemaElement schemaElement, FileObject dbschemaFile) {
        this.schemaElement = schemaElement;
        this.dbconn = null;
        this.dbschemaFile = dbschemaFile;
        this.datasourceName = null;
        
        updateTableSource();
    }
    
    public TableSource getTableSource() {
        return tableSource;
    }
    
    private void updateTableSource() {
        if (dbconn != null) {
            if (datasourceName != null) {
                tableSource = new TableSource(datasourceName, TableSource.Type.DATA_SOURCE);
            } else {
                tableSource = new TableSource(dbconn.getName(), TableSource.Type.CONNECTION);
            }
        } else if (dbschemaFile != null) {
            tableSource = new TableSource(FileUtil.toFile(dbschemaFile).getAbsolutePath(), TableSource.Type.SCHEMA_FILE);
        } else {
            tableSource = null;
        }
    }
    
    public SchemaElement getSchemaElement() {
        return schemaElement;
    }
    
    public DatabaseConnection getDatabaseConnection(){
        return dbconn;
    }
    
    public FileObject getDBSchemaFile() {
        return dbschemaFile;
    }
    
    /**
     * Returns the package for bean and module generation.
     */
    public String getPackageName() {
        return packageName;
    }
    
    /**
     * Sets the package for bean and module generation.
     */
    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }
    
    public SourceGroup getLocation() {
        return location;
    }
    
    public void setLocation(SourceGroup location) {
        this.location = location;
    }
    
    public boolean isCmpFieldsInInterface() {
        return cmpFieldsInInterface;
    }
    
    public void setCmpFieldsInInterface(boolean cmpFieldsInInterface) {
        this.cmpFieldsInInterface = cmpFieldsInInterface;
    }
    
    public boolean isGenerateFinderMethods() {
        return this.generateFinderMethods;
    }
    
    public void setGenerateFinderMethods(boolean generateFinderMethods) {
        this.generateFinderMethods = generateFinderMethods;
    }
    
    /**
     * Public because used in J2EE functional tests.
     */
    public void buildBeans() {
        TableSource.put(project, tableSource);
        
        GenerateTablesImpl genTables = new GenerateTablesImpl();
        FileObject rootFolder = getLocation().getRootFolder();
        String packageName = getPackageName();

        for (Table table : selectedTables.getTables()) {
            genTables.addTable(table.getName(), rootFolder, packageName, selectedTables.getClassName(table));
        }

        // add the (possibly related) disabled tables, so that the relationships are created correctly
        // XXX what if this adds related tables that the user didn't want, such as join tables?
        for (Table table : tableClosure.getAvailableTables()) {
            if (table.getDisabledReason() instanceof Table.ExistingDisabledReason) {
                Table.ExistingDisabledReason exDisReason = (Table.ExistingDisabledReason)table.getDisabledReason();
                String fqClassName = exDisReason.getFQClassName();
                SourceGroup sourceGroup = Util.getClassSourceGroup(getProject(), fqClassName); // NOI18N
                if (sourceGroup != null) {
                    genTables.addTable(table.getName(), sourceGroup.getRootFolder(), 
                            Util.getPackageName(fqClassName), Util.getClassName(fqClassName));
                }
            }
        }

        generator = new DbSchemaEjbGenerator(genTables, schemaElement);
    }
    
    public EntityClass[] getBeans() {
        return generator.getBeans();
    }
    
    public EntityRelation[] getRelations() {
        return generator.getRelations();
    }
    
    private static final class GenerateTablesImpl implements GeneratedTables {

        private final Set<String> tableNames = new HashSet<String>();
        private final Map<String, FileObject> rootFolders = new HashMap<String, FileObject>();
        private final Map<String, String> packageNames = new HashMap<String, String>();
        private final Map<String, String> classNames = new HashMap<String, String>();
        
        public Set<String> getTableNames() {
            return Collections.unmodifiableSet(tableNames);
        }
        
        private void addTable(String tableName, FileObject rootFolder, String packageName, String className) {
            tableNames.add(tableName);
            rootFolders.put(tableName, rootFolder);
            packageNames.put(tableName, packageName);
            classNames.put(tableName, className);
        }

        public FileObject getRootFolder(String tableName) {
            return rootFolders.get(tableName);
        }

        public String getPackageName(String tableName) {
            return packageNames.get(tableName);
        }
        
        public String getClassName(String tableName) {
            return classNames.get(tableName);
        }
    }
}
