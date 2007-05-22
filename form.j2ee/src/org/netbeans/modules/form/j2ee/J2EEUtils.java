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
package org.netbeans.modules.form.j2ee;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionStatementTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.swing.Action;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.db.explorer.JDBCDriver;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.progress.aggregate.AggregateProgressFactory;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.form.FormEditor;
import org.netbeans.modules.form.FormModel;
import org.netbeans.modules.form.FormProperty;
import org.netbeans.modules.form.RADComponent;
import org.netbeans.modules.form.codestructure.CodeStructure;
import org.netbeans.modules.form.project.ClassPathUtils;
import org.netbeans.modules.form.project.ClassSource;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.j2ee.persistence.api.PersistenceScope;
import org.netbeans.modules.j2ee.persistence.api.entity.generator.EntitiesFromDBGenerator;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.Entity;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.EntityMappingsMetadata;
import org.netbeans.modules.j2ee.persistence.dd.PersistenceMetadata;
import org.netbeans.modules.j2ee.persistence.dd.persistence.model_1_0.Persistence;
import org.netbeans.modules.j2ee.persistence.dd.persistence.model_1_0.PersistenceUnit;
import org.netbeans.modules.j2ee.persistence.provider.InvalidPersistenceXmlException;
import org.netbeans.modules.j2ee.persistence.provider.Provider;
import org.netbeans.modules.j2ee.persistence.provider.ProviderUtil;
import org.netbeans.modules.j2ee.persistence.wizard.fromdb.*;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ui.support.FileSensitiveActions;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;

/**
 * Utility methods.
 *
 * @author Jan Stola
 */
public class J2EEUtils {
    /** Determines whether related entities should be generated.  */
    public static final boolean TABLE_CLOSURE = true;

    /**
     * Returns persistence unit that corresponds to given database URL.
     *
     * @param persistence persistence context.
     * @param dbURL database URL.
     * @return persistence unit that corresponds to given database URL.
     */
    public static PersistenceUnit findPersistenceUnit(Persistence persistence, String dbURL) {
        for (PersistenceUnit unit : persistence.getPersistenceUnit()) {
            Provider provider = ProviderUtil.getProvider(unit);
            String unitURL = ProviderUtil.getProperty(unit, provider.getJdbcUrl()).getValue();
            if (dbURL.equals(unitURL)) {
                return unit;
            }
        }
        return null;
    }

    /**
     * Creates persistence unit according to given DB <code>connection</code>.
     *
     * @param project target project for the new persistence unit.
     * @param connection database connection.
     * @throws IOException when something goes wrong.
     * @throws InvalidPersistenceXmlException
     * @return persistence unit that corresponds to given DB <code>connection</code>.
     */
    public static PersistenceUnit createPersistenceUnit(Project project, DatabaseConnection connection) throws IOException, InvalidPersistenceXmlException {
        FileObject persistenceXML = ProviderUtil.getDDFile(project);
        Persistence persistence = PersistenceMetadata.getDefault().getRoot(persistenceXML);
        String dbURL = connection.getDatabaseURL();
        
        // Determine name of the PU
        String dbName = dbURL.substring(dbURL.lastIndexOf('/')+1);
        String puName = dbName + "PU"; // NOI18N
        PersistenceUnit unit = findPersistenceUnit(persistence, puName);
        int count = 0;
        while (unit != null) {
           count++;
           puName = dbName + "PU" + count; // NOI18N
           unit = findPersistenceUnit(persistence, puName);
        }

        // Determine the provider
        Provider provider;
        if (persistence.getPersistenceUnit().length > 0) {
            // Use the same provider as the existing persistence unit
            provider = ProviderUtil.getProvider(persistence.getPersistenceUnit(0));
        } else {
            // The first persistence unit - use TopLink provider
            // (it is delivered as a part of NetBeans J2EE support)
            provider = ProviderUtil.TOPLINK_PROVIDER;
        }

        unit = ProviderUtil.buildPersistenceUnit(puName, provider, connection);
        unit.setTransactionType("RESOURCE_LOCAL"); // NOI18N
        ProviderUtil.addPersistenceUnit(unit, project);

        return unit;
    }

    /**
     * Returns names of persistence units in the specified project.
     *
     * @param project project to scan for persistence units.
     * @return names of persistence units in the specified project.
     */
    public static String[] getPersistenceUnitNames(Project project) {
        FileObject persistenceXML;
        try {
            persistenceXML = J2EEUtils.getPersistenceXML(project, false);
        } catch (InvalidPersistenceXmlException ipxex) {
            ipxex.printStackTrace();
            return new String[0];
        }
        if (persistenceXML == null) return new String[0];
        Persistence persistence = null;
        try {
             persistence = PersistenceMetadata.getDefault().getRoot(persistenceXML);
        } catch (IOException ioex) {
            ioex.printStackTrace();
            return new String[0];
        }
        PersistenceUnit[] unit = persistence.getPersistenceUnit();
        String[] names = new String[unit.length];
        for (int i=0; i<unit.length; i++) {
            names[i] = unit[i].getName();
        }
        return names;
    }

    /**
     * Updates project classpath with the TopLink library.
     *
     * @param fileInProject file in the project whose classpath should be updated.
     * @return <code>true</code> if the classpath has been updated,
     * returns <code>false</code> otherwise.
     */
    public static boolean updateProjectForTopLink(FileObject fileInProject) {
        try {
            ClassPath classPath = ClassPath.getClassPath(fileInProject, ClassPath.EXECUTE);
            FileObject fob = classPath.findResource("oracle/toplink/essentials/ejb/cmp3/EntityManagerFactoryProvider.class"); // NOI18N
            if (fob == null) {
                ClassSource cs = new ClassSource("", // class name is not needed // NOI18N
                    new String[] { ClassSource.LIBRARY_SOURCE },
                    new String[] { "toplink" }); // NOI18N
                return ClassPathUtils.updateProject(fileInProject, cs);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return false;
    }
    
    /**
     * Updates project classpath with the JARs specified by the <code>urls</code>.
     * The classpath is not updated if the given reference class (<code>refClassName</code>)
     * is on the classpath.
     *
     * @param urls URLs of the JARs that should be added to the classpath.
     * @param refClassName name of the class that determines wheter the classpath
     * should be updated or not.
     * @param fileInProject file in the project whose classpath should be updated.
     * @return <code>true</code> if the classpath has been updated,
     * returns <code>false</code> otherwise.
     */
    public static boolean updateProjectWithJARs(URL[] urls, String refClassName, FileObject fileInProject) {
        try {
            ClassPath classPath = ClassPath.getClassPath(fileInProject, ClassPath.EXECUTE);
            String resourceName = refClassName.replace('.', '/') + ".class"; // NOI18N
            FileObject fob = classPath.findResource(resourceName); // NOI18N
            if (fob == null) {
                String[] cpTypes = new String[urls.length];
                String[] cpRoots = new String[urls.length];

                for (int i=0; i<urls.length; i++) {
                    cpTypes[i] = ClassSource.JAR_SOURCE;
                    cpRoots[i] = FileUtil.toFile(URLMapper.findFileObject(urls[i])).getAbsolutePath();
                }

                ClassSource cs = new ClassSource("", cpTypes, cpRoots); // NOI18N
                return ClassPathUtils.updateProject(fileInProject, cs);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    /**
     * Returns persistence descriptor for the given project.
     *
     * @param project project where the persistence descriptor should be found.
     * @param create determines whether the persistence descriptor should be created
     * (if there is not one already)
     * @throws InvalidPersistenceXmlException
     * @return persistence descriptor for the given project.
     */
    public static FileObject getPersistenceXML(Project project, boolean create) throws InvalidPersistenceXmlException {
        FileObject persistenceXML = ProviderUtil.getDDFile(project);
        if ((persistenceXML == null) && create) {
            // Forces creation of persistence.xml
            ProviderUtil.getPUDataObject(project); 
            persistenceXML = ProviderUtil.getDDFile(project);
        }
        return persistenceXML;
    }

    /**
     * Returns entity manager RAD component that corresponds to the specified persistence unit.
     *
     * @param model form model where the RAD component should be found.
     * @param puName name of the persistence unit.
     * @return entity manager RAD component that corresponds to the specified persistence unit
     * or <code>null</code> if such entity manager is not in the given form model.
     */
    public static RADComponent findEntityManager(FormModel model, String puName) {
        for (RADComponent metacomp : model.getAllComponents()) {
            if ("javax.persistence.EntityManager".equals(metacomp.getBeanClass().getName())) {  // NOI18N
                try {
                    FormProperty prop = (FormProperty)metacomp.getPropertyByName("persistenceUnit"); // NOI18N
                    Object name = prop.getRealValue();
                    if (puName.equals(name)) {
                        return metacomp;
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
        return null;
    }
    
    /**
     * Creates entity manager RAD component that corresponds to the specified persistence unit.
     *
     * @param model form model where the RAD component should be created.
     * @param puName name of the persistence unit.
     * @return entity manager RAD component that corresponds to the specified persistence unit.
     * @throws Exception when something goes wrong ;-).
     */
    public static RADComponent createEntityManager(FormModel model, String puName) throws Exception {
        FileObject formFile = FormEditor.getFormDataObject(model).getFormFile();
        Class emClass = ClassPathUtils.loadClass("javax.persistence.EntityManager", formFile); // NOI18N
        RADComponent entityManager = new RADComponent();
        entityManager.initialize(model);
        entityManager.initInstance(emClass);
        entityManager.getPropertyByName("persistenceUnit").setValue(puName); // NOI18N
        renameComponent(entityManager, false, puName + "EntityManager", "entityManager"); // NOI18N
        model.addComponent(entityManager, null, true);
        return entityManager;
    }

    /**
     * Returns entity in the specified persistence unit that corresponds to the given table.
     *
     * @param mappings entity mapping information.
     * @param tableName name of the table for the searched entity.
     * @throws IOException when something goes wrong
     * @return entity in the specified persistence unit that corresponds to the given table
     * or <code>null</code> if such an entity doesn't exist.
     */
    public static String[] findEntity(MetadataModel<EntityMappingsMetadata> mappings, final String tableName) throws IOException {
        return mappings.runReadAction(new MetadataModelAction<EntityMappingsMetadata, String[]>() {
            public String[] run(EntityMappingsMetadata metadata) {
                Entity[] entity = metadata.getRoot().getEntity();
                for (int i=0; i<entity.length; i++) {
                    if (tableName.equals(entity[i].getTable().getName())) {
                        return new String[] {entity[i].getName(), entity[i].getClass2()};
                    }
                }
                return null;
            }
        });
    }

    /**
     * Generates entity classes for given tables.
     *
     * @param project project where the classes should be generated.
     * @param location source location.
     * @param packageName name of the package where the classes should be generated.
     * @param tableNames names of the tables.
     * @param dbconn connection to the DB with the tables.
     * @param unit persistence unit to add the generated classes into
     * @return entity class that corresponds to the specified table and names
     * of the entity classes for related tables (if <code>relatedTableNames</code>
     * parameter was non-<code>null</code>).
     */
    private static String[] generateEntityClass(final Project project, SourceGroup location, String packageName, DatabaseConnection dbconn, List tableNames, PersistenceUnit unit) {
        try {
            EntitiesFromDBGenerator generator = new EntitiesFromDBGenerator(tableNames, true, packageName, location, dbconn, project, unit);
            // PENDING
            final Set<FileObject> entities = generator.generate(AggregateProgressFactory.createProgressContributor("PENDING"));

            for (FileObject fob : entities) {
                makeEntityObservable(fob);
            }
            
            // Compile generated bean
            compileGeneratedEntities(entities);

            String[] result = new String[entities.size()];
            int count = 0;
            for (FileObject fob : entities) {
                result[count++] = packageName + '.' + fob.getName();
            }
            return result;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * Compiles the given set of entities. 
     * 
     * @param entities entities to compile.
     */
    private static void compileGeneratedEntities(Set<FileObject> entities) {
        try {
            Action action = FileSensitiveActions.fileCommandAction(ActionProvider.COMMAND_COMPILE_SINGLE, "", null); // NOI18N
            if (action instanceof ContextAwareAction) {
                DataObject[] dobs = new DataObject[entities.size()];
                int count = 0;
                for (FileObject fob : entities) {
                    dobs[count++] = DataObject.find(fob);
                }
                Lookup lookup = Lookups.fixed((Object[])dobs);
                ((ContextAwareAction)action).createContextAwareInstance(lookup).actionPerformed(new ActionEvent(new Object(), 0, null));
            }
        } catch (DataObjectNotFoundException dnfex) {
            dnfex.printStackTrace();
        }
    }

    /**
     * Creates persistence generator of the given type.
     *
     * @param type type of the generator.
     * @return persistence generator of the given type.
     */
    private static PersistenceGenerator createPersistenceGenerator(String type) {
        Lookup.Template template = new Lookup.Template(PersistenceGeneratorProvider.class);
        Collection<PersistenceGeneratorProvider> providers = Lookup.getDefault().lookup(template).allInstances();
        for (PersistenceGeneratorProvider provider : providers) {
            if (type.equals(provider.getGeneratorType())) {
                return provider.createGenerator();
            }
        }
        throw new AssertionError("Could not find a persistence generator of type " + type); // NOI18N
    }

    /**
     * Makes sure that the given database connection is established.
     *
     * @param connection connection that should be established.
     * @return established connection, may return <code>null</code> if the user cancel
     * the connection dialog.
     */
    public static Connection establishConnection(DatabaseConnection connection) {
        Connection con = connection.getJDBCConnection();
        if (con == null) { // connection not established yet
            ConnectionManager.getDefault().showConnectionDialog(connection);
            con = connection.getJDBCConnection();
        }
        return con;
    }

    /**
     * Updates project's classpath if necessary.
     *
     * @param fileInProject file in the project whose classpath should be updated.
     * @param unit persistence unit to be used in the project.
     * @param driver JDBC driver to be used in the project.
     */
    public static void updateProjectForUnit(FileObject fileInProject, PersistenceUnit unit, JDBCDriver driver) {
        // Make sure that TopLink JAR files are on the classpath (if using TopLink)
        if (ProviderUtil.TOPLINK_PROVIDER.equals(ProviderUtil.getProvider(unit))) {
            updateProjectForTopLink(fileInProject);
        }

        // Make sure that DB driver classes are on the classpath
        updateProjectWithJARs(driver.getURLs(), driver.getClassName(), fileInProject);
    }

    /**
     * Initializes persistence unit and persistence descriptor.
     *
     * @param persistenceXML persistence descriptor.
     * @param connection DB connection that specifies parameters of the persistence unit.
     * @return persistence unit that corresponds to the given DB connection.
     * @throws IOException if there is a problem with creation of the persistence unit.
     * @throws InvalidPersistenceXmlException
     */
    public static PersistenceUnit initPersistenceUnit(FileObject persistenceXML, DatabaseConnection connection) throws IOException, InvalidPersistenceXmlException {
        Project project = FileOwnerQuery.getOwner(persistenceXML);
        
        // Make sure the database connection is established
        J2EEUtils.establishConnection(connection);

        // Make sure there is a persistence unit that corresponds to our DB connection
        Persistence persistence = PersistenceMetadata.getDefault().getRoot(persistenceXML);
        PersistenceUnit unit = J2EEUtils.findPersistenceUnit(persistence, connection.getDatabaseURL());
        if (unit == null) {
            unit = J2EEUtils.createPersistenceUnit(project, connection);
        }
        return unit;
    }

    /**
     * Creates entity that corresponds to the specified table (accessible via given DB connection).
     * Possibly creates also entities for related tables.
     *
     * @param dir directory where the entity should be created.
     * @param scope persistence scope where the entity should be created.
     * @param unit persistence unit where the entity should be created.
     * @param connection connection through which the table is accessible.
     * @param tableName name of the table.
     * @param relatedTableNames names of related tables whose entity classes should be added
     * into the peristence unit.
     * @throws Exception when something goes wrong.
     */
    public static void createEntity(FileObject dir, PersistenceScope scope, PersistenceUnit unit, DatabaseConnection connection, String tableName, String[] relatedTableNames) throws Exception {
        Project project = FileOwnerQuery.getOwner(dir);
        String packageName = scope.getClassPath().getResourceName(dir, '.', false);

        SourceGroup[] groups = SourceGroupSupport.getJavaSourceGroups(project);
        SourceGroup location = groups[0];
        for (int i=0; i<groups.length; i++) {
            if (groups[i].contains(dir)) {
                location = groups[i];
                break;
            }
        }
        List<String> tableNames = new LinkedList<String>();
        tableNames.add(tableName);
        if (relatedTableNames != null) {
            tableNames.addAll(Arrays.asList(relatedTableNames));
        }
        J2EEUtils.generateEntityClass(project, location, packageName, connection, tableNames, unit);
        // PENDING ugly workaround for the fact that the generated entity is not immediately
        // in the model - will be removed as soon as the corresponding issue is fixed
        try {
            outer: for (int i=0; i<30; i++) {
                MetadataModel<EntityMappingsMetadata> mappings = scope.getEntityMappingsModel(unit.getName());
                for (String table : tableNames) {
                    String[] entityInfo = J2EEUtils.findEntity(mappings, table);
                    if (entityInfo == null) {
                        Thread.sleep(1000);
                        continue outer;
                    }
                }
                break;
            }
        } catch (InterruptedException iex) {
            iex.printStackTrace();
        }
    }

    // PENDING get rid of that - find correct mapping instead
    /**
     * Converts SQL column name to Java class field name.
     *
     * @param columnName name to covert.
     * @return field name that corresponds to the given column name.
     */
    public static String columnToField(String columnName) {
        StringBuilder sb = new StringBuilder(columnName.length());
        boolean toUpper = false;
        for (int i=0; i<columnName.length(); i++) {
            char c = columnName.charAt(i);
            if (!toUpper) {
                c = Character.toLowerCase(c);
            }
            toUpper = (c == '_');
            if (!toUpper) {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * Renames metacomponent.
     * 
     * @param comp component to rename.
     * @param inModel determines whether the component is already in the model.
     * @param name suggested new names for the component.
     */
    public static void renameComponent(RADComponent comp, boolean inModel, String... name) {
        String oldName = comp.getName();
        FormModel formModel = comp.getFormModel();
        int index = 0;
        while (!Utilities.isJavaIdentifier(name[index])) index++;
        String prefix = name[index];
        String newName;
        CodeStructure codeStructure = formModel.getCodeStructure();
        if (codeStructure.isVariableNameReserved(prefix) && !prefix.equals(oldName)) {
            index = 0;
            while (codeStructure.isVariableNameReserved(prefix+index) && !prefix.equals(oldName)) index++;
            newName = prefix + index;
        } else {
            newName = prefix;
        }
        if (inModel) {
            comp.setName(newName);
        } else {
            comp.setStoredName(newName);
        }
    }

    /**
     * Finds out tables in the DB represented by the given DB connection. 
     * 
     * @param connection DB connection to search for tables.
     * @return list of names of the tables.
     */
    public static List<String> tableNamesForConnection(DatabaseConnection connection) {
        Connection con = connection.getJDBCConnection();
        List<String> tableNames = new LinkedList<String>();
        try {
            ResultSet rs = con.getMetaData().getTables(con.getCatalog(), connection.getSchema(), "%",  new String[] {"TABLE"}); // NOI18N
            while (rs.next()) {
                String tableName = rs.getString("TABLE_NAME"); // NOI18N
                tableNames.add(tableName);
            }
            rs.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return tableNames;
    }

    /**
     * Makes the entity observable (e.g. adds property change support).
     * 
     * @param entity entity to make observable.
     */
    private static void makeEntityObservable(FileObject entity) {
        JavaSource source = JavaSource.forFileObject(entity);
        try {
            // PENDING merge into one task once it will be possible
            source.runModificationTask(new CancellableTask<WorkingCopy>() {

                public void run(WorkingCopy wc) throws Exception {
                    wc.toPhase(JavaSource.Phase.RESOLVED);
                    CompilationUnitTree cu = wc.getCompilationUnit();
                    ClassTree clazz = null;
                    for (Tree typeDecl : cu.getTypeDecls()) {
                        if (Tree.Kind.CLASS == typeDecl.getKind()) {
                            clazz = (ClassTree) typeDecl;
                            break;
                        }
                    }
                    TreeMaker make = wc.getTreeMaker();
                    
                    // changeSupport field
                    TypeElement transientElement = wc.getElements().getTypeElement("javax.persistence.Transient"); // NOI18N
                    TypeMirror transientMirror = transientElement.asType();
                    Tree transientType = make.Type(transientMirror);
                    AnnotationTree transientTree = make.Annotation(transientType , Collections.EMPTY_LIST);
                    ModifiersTree modifiers = make.Modifiers(Modifier.PRIVATE, Collections.singletonList(transientTree));
                    TypeElement changeSupportElement = wc.getElements().getTypeElement("java.beans.PropertyChangeSupport"); // NOI18N
                    TypeMirror changeSupportMirror = changeSupportElement.asType();
                    Tree changeSupportType = make.Type(changeSupportMirror);
                    NewClassTree changeSupportConstructor = make.NewClass(null, Collections.EMPTY_LIST, make.QualIdent(changeSupportElement), Collections.singletonList(make.Identifier("this")), null);
                    VariableTree changeSupport = make.Variable(modifiers, "changeSupport", changeSupportType, changeSupportConstructor); // NOI18N
                    ClassTree modifiedClass = make.insertClassMember(clazz, 0, changeSupport);

                    // property change notification
                    for (Tree clMember : modifiedClass.getMembers()) {
                        if (clMember.getKind() == Tree.Kind.METHOD) {
                            MethodTree method = (MethodTree)clMember;
                            // PENDING modify only setters that correspond to persistent properties
                            if (method.getName().toString().startsWith("set") && (method.getParameters().size() == 1)) { // NOI18N
                                BlockTree block = method.getBody();
                                VariableTree parameter = method.getParameters().get(0);
                                ExpressionTree persistentVariable = null;
                                for (StatementTree statement : block.getStatements()) {
                                    if (statement.getKind() == Tree.Kind.EXPRESSION_STATEMENT) {
                                        ExpressionTree expression =((ExpressionStatementTree)statement).getExpression();
                                        if (expression.getKind() == Tree.Kind.ASSIGNMENT) {
                                            AssignmentTree assignment = (AssignmentTree)expression;
                                            persistentVariable = assignment.getVariable();
                                        }
                                    }
                                }
                                if (persistentVariable == null) {
                                    continue;
                                }

                                // <Type> old<PropertyName> = this.<propertyName>
                                String parameterName = parameter.getName().toString();
                                String oldParameterName = "old" + Character.toUpperCase(parameterName.charAt(0)) + parameterName.substring(1); // NOI18N
                                Tree parameterTree = parameter.getType();
                                if (parameterTree.getKind() != Tree.Kind.PRIMITIVE_TYPE) {
                                    Element parameterType = wc.getTrees().getElement(wc.getTrees().getPath(cu, parameterTree));
                                    parameterTree = make.QualIdent(parameterType);
                                }
                                VariableTree oldParameter = make.Variable(make.Modifiers(Collections.EMPTY_SET), oldParameterName, parameterTree, persistentVariable);
                                BlockTree newBlock = make.insertBlockStatement(block, 0, oldParameter);

                                // changeSupport.firePropertyChange("<propertyName>", old<PropertyName>, <propertyName>);
                                MemberSelectTree fireMethod = make.MemberSelect(make.Identifier(changeSupport.getName()), "firePropertyChange"); // NOI18N
                                List<ExpressionTree> fireArgs = new LinkedList<ExpressionTree>();
                                // PENDING literal should be the name of the property
                                fireArgs.add(make.Literal(parameterName));
                                fireArgs.add(make.Identifier(oldParameterName));
                                fireArgs.add(make.Identifier(parameterName));
                                MethodInvocationTree notification = make.MethodInvocation(Collections.EMPTY_LIST, fireMethod, fireArgs);
                                newBlock = make.addBlockStatement(newBlock, make.ExpressionStatement(notification));
                                wc.rewrite(block, newBlock);
                            }
                        }
                    }
                    wc.rewrite(clazz, modifiedClass);
                }

                public void cancel() {
                }

            }).commit();
            source.runModificationTask(new CancellableTask<WorkingCopy>() {

                public void run(WorkingCopy wc) throws Exception {
                    wc.toPhase(JavaSource.Phase.RESOLVED);
                    CompilationUnitTree cu = wc.getCompilationUnit();
                    ClassTree clazz = null;
                    for (Tree typeDecl : cu.getTypeDecls()) {
                        if (Tree.Kind.CLASS == typeDecl.getKind()) {
                            clazz = (ClassTree) typeDecl;
                            break;
                        }
                    }
                    TreeMaker make = wc.getTreeMaker();

                    // addPropertyChange method
                    ModifiersTree parMods = make.Modifiers(Collections.EMPTY_SET, Collections.EMPTY_LIST);
                    TypeElement changeListenerElement = wc.getElements().getTypeElement("java.beans.PropertyChangeListener"); // NOI18N
                    VariableTree par = make.Variable(parMods, "listener", make.QualIdent(changeListenerElement), null); // NOI18N
                    TypeElement changeSupportElement = wc.getElements().getTypeElement("java.beans.PropertyChangeSupport"); // NOI18N
                    VariableTree changeSupport = make.Variable(parMods, "changeSupport", make.QualIdent(changeSupportElement), null); // NOI18N
                    MemberSelectTree addCall = make.MemberSelect(make.Identifier(changeSupport.getName()), "addPropertyChangeListener"); // NOI18N
                    MethodInvocationTree addInvocation = make.MethodInvocation(Collections.EMPTY_LIST, addCall, Collections.singletonList(make.Identifier(par.getName())));
                    MethodTree addMethod = make.Method(
                        make.Modifiers(Modifier.PUBLIC, Collections.EMPTY_LIST),
                        "addPropertyChangeListener", // NOI18N
                        make.PrimitiveType(TypeKind.VOID),
                        Collections.EMPTY_LIST,
                        Collections.singletonList(par),
                        Collections.EMPTY_LIST,
                        make.Block(Collections.singletonList(make.ExpressionStatement(addInvocation)), false),
                        null
                    );
                    ClassTree modifiedClass = make.addClassMember(clazz, addMethod);
                    wc.rewrite(clazz, modifiedClass);
                }

                public void cancel() {
                }

            }).commit();
            source.runModificationTask(new CancellableTask<WorkingCopy>() {

                public void run(WorkingCopy wc) throws Exception {
                    wc.toPhase(JavaSource.Phase.RESOLVED);
                    CompilationUnitTree cu = wc.getCompilationUnit();
                    ClassTree clazz = null;
                    for (Tree typeDecl : cu.getTypeDecls()) {
                        if (Tree.Kind.CLASS == typeDecl.getKind()) {
                            clazz = (ClassTree) typeDecl;
                            break;
                        }
                    }
                    TreeMaker make = wc.getTreeMaker();

                    // removePropertyChange method
                    ModifiersTree parMods = make.Modifiers(Collections.EMPTY_SET, Collections.EMPTY_LIST);
                    TypeElement changeListenerElement = wc.getElements().getTypeElement("java.beans.PropertyChangeListener"); // NOI18N
                    VariableTree par = make.Variable(parMods, "listener", make.QualIdent(changeListenerElement), null); // NOI18N
                    TypeElement changeSupportElement = wc.getElements().getTypeElement("java.beans.PropertyChangeSupport"); // NOI18N
                    VariableTree changeSupport = make.Variable(parMods, "changeSupport", make.QualIdent(changeSupportElement), null); // NOI18N
                    MemberSelectTree removeCall = make.MemberSelect(make.Identifier(changeSupport.getName()), "addPropertyChangeListener"); // NOI18N
                    MethodInvocationTree removeInvocation = make.MethodInvocation(Collections.EMPTY_LIST, removeCall, Collections.singletonList(make.Identifier(par.getName())));
                    MethodTree removeMethod = make.Method(
                        make.Modifiers(Modifier.PUBLIC, Collections.EMPTY_LIST),
                        "removePropertyChangeListener", // NOI18N
                        make.PrimitiveType(TypeKind.VOID),
                        Collections.EMPTY_LIST,
                        Collections.singletonList(par),
                        Collections.EMPTY_LIST,
                        make.Block(Collections.singletonList(make.ExpressionStatement(removeInvocation)), false),
                        null
                    );
                    ClassTree modifiedClass = make.addClassMember(clazz, removeMethod);
                    wc.rewrite(clazz, modifiedClass);
                }

                public void cancel() {
                }

            }).commit();
        } catch (IOException ioex) {
            ioex.printStackTrace();
        }
    }

}
