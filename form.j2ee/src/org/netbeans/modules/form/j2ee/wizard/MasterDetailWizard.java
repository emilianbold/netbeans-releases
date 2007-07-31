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

package org.netbeans.modules.form.j2ee.wizard;

import com.sun.source.tree.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.db.explorer.JDBCDriver;
import org.netbeans.api.db.explorer.JDBCDriverManager;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.form.j2ee.J2EEUtils;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.j2ee.persistence.api.PersistenceScope;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.Entity;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.EntityMappingsMetadata;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.ManyToOne;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.OneToMany;
import org.netbeans.modules.j2ee.persistence.dd.persistence.model_1_0.PersistenceUnit;
import org.netbeans.spi.java.project.support.ui.templates.JavaTemplates;
import org.openide.WizardDescriptor;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.NbBundle;

/**
 * Master/detail wizard.
 *
 * @author Jan Stola
 */
public class MasterDetailWizard implements WizardDescriptor.InstantiatingIterator {
    /** Key for the description of the wizard content. */
    private static final String WIZARD_PANEL_CONTENT_DATA = "WizardPanel_contentData"; // NOI18N
    /** Key for the description of the wizard panel's position. */
    private static final String WIZARD_PANEL_CONTENT_SELECTED_INDEX = "WizardPanel_contentSelectedIndex"; // NOI18N
    /** Index of the current panel. */
    private int panelIndex;
    /** Panels of this wizard. */
    private WizardDescriptor.Panel[] panels;
    /** Names of wizard steps. */
    private String[] steps;
    /** Wizard descriptor. */
    private WizardDescriptor wizard;
    /** Wizard iterator preceding our steps. */
    private transient WizardDescriptor.InstantiatingIterator delegateIterator;
    /** Number of steps of the delegate iterator (for configuring new file) */
    private int beforeStepsNo;
    private String[] joinInfo;
    private String joinColumn;
    private String referencedColumn;

    /**
     * Creates new <code>MasterDetailWizard</code>.
     * 
     * @param createNewFile determines whether this wizard should create
     * a new master/detail form or whether it should add the master/detail
     * functionality into an existing file.
     */
    MasterDetailWizard(boolean createNewFile) {
        if (createNewFile)
            delegateIterator = JavaTemplates.createJavaTemplateIterator();
    }

    /**
     * Creates new master/detail wizard that creates new file.
     *
     * @return new instance of <code>MasterDetailWizard</code>.
     */
    public static MasterDetailWizard create() {
        return new MasterDetailWizard(true);
    }

    /**
     * Creates new master/detail wizard that processes existing file.
     *
     * @return new instance of <code>MasterDetailWizard</code>.
     */
    public static MasterDetailWizard createForExisting() {
        return new MasterDetailWizard(false);
    }

    /**
     * Initializes the wizard.
     *
     * @param wizard descriptor of this wizard.
     */
    public void initialize(WizardDescriptor wizard) {
        this.wizard = wizard;
        if (delegateIterator != null) {
            delegateIterator.initialize(wizard);
        }
        panelIndex = 0;
        panels = createPanels();
    }

    /**
     * Creates panels of this wizard.
     *
     * @return panels of this wizard.
     */
    private WizardDescriptor.Panel[] createPanels() {
        panels = new WizardDescriptor.Panel[] {
            new MasterPanel(delegateIterator == null),
            new DetailPanel()
        };
        return panels;
    }

    /**
     * Returns current panel of the wizard.
     *
     * @return current panel of the wizard.
     */
    public WizardDescriptor.Panel current() {
        String title = NbBundle.getMessage(MasterDetailWizard.class, "TITLE_MasterDetail");  // NOI18N
        wizard.putProperty("NewFileWizard_Title", title); // NOI18N
        if (steps == null) {
            initSteps();
        }
        WizardDescriptor.Panel panel;
        if (panelIndex < beforeStepsNo) {
            panel = delegateIterator.current();
        } else {
            panel = panels[panelIndex-beforeStepsNo];
        }
        JComponent comp = (JComponent)panel.getComponent();
        if ((panelIndex < beforeStepsNo) || (comp.getClientProperty(WIZARD_PANEL_CONTENT_DATA) == null)) {
            comp.putClientProperty(WIZARD_PANEL_CONTENT_DATA, steps);
        }
        if (comp.getClientProperty(WIZARD_PANEL_CONTENT_SELECTED_INDEX) == null) {
            comp.putClientProperty(WIZARD_PANEL_CONTENT_SELECTED_INDEX, panelIndex);
        }
        return panel;
    }

    /**
     * Initializes array of steps of this wizard.
     */
    private void initSteps() {
        String[] thisSteps = new String[] {
            NbBundle.getMessage(MasterDetailWizard.class, "TITLE_MasterPanel"), // NOI18N
            NbBundle.getMessage(MasterDetailWizard.class, "TITLE_DetailPanel") // NOI18N
        };

        Object prop;
        if (delegateIterator != null) {
            JComponent comp = (JComponent)delegateIterator.current().getComponent();
            prop = comp.getClientProperty("WizardPanel_contentData"); // NOI18N
        }
        else prop = null;

        String[] beforeSteps;
        int stepsStartPos;
        
        if (prop instanceof String[]) {
            beforeSteps = (String[]) prop;
            beforeStepsNo = beforeSteps.length;
            stepsStartPos = beforeSteps.length;
            if (stepsStartPos > 0 && ("...".equals(beforeSteps[stepsStartPos - 1]))) { // NOI18N
                stepsStartPos--;
            }
        } else {
            beforeStepsNo = 0;
            beforeSteps = null;
            stepsStartPos = 0;
        }
        
        steps = new String[stepsStartPos + thisSteps.length];
        if (beforeSteps != null)
            System.arraycopy(beforeSteps, 0, steps, 0, stepsStartPos);
        System.arraycopy(thisSteps, 0, steps, stepsStartPos, thisSteps.length);
    }

    /**
     * Returns name of the current panel.
     *
     * @return name of the current panel.
     */
    public String name() {
        return current().getComponent().getName();
    }

    /**
     * Determines whether there is a next panel.
     *
     * @return <code>true</code> if there is a next panel,
     * returns <code>false</code> otherwise.
     */
    public boolean hasNext() {
        if ((panelIndex+1 < beforeStepsNo) && !delegateIterator.hasNext()) {
            // Delegate iterator doesn't manage all previous steps
            beforeStepsNo = panelIndex+1;
        }
        return panelIndex+1 < beforeStepsNo + panels.length;
    }

    /**
     * Determines whether there is a previous panel.
     *
     * @return <code>true</code> if there is a previous panel,
     * returns <code>false</code> otherwise.
     */
    public boolean hasPrevious() {
        return panelIndex > 0;
    }

    /**
     * Moves to the next panel.
     */
    public void nextPanel() {
        panelIndex++;
        if (panelIndex < beforeStepsNo)
            delegateIterator.nextPanel();
    }

    /**
     * Moves to the previous panel.
     */
    public void previousPanel() {
        panelIndex--;
        if (panelIndex < beforeStepsNo - 1)
            delegateIterator.previousPanel();
    }

    public Set instantiate() throws IOException {
        if (delegateIterator == null) {
            // postpone the instantiation until the project is opened
            final FileObject file = (FileObject) wizard.getProperty("mainForm"); // NOI18N
            final File projDir = (File)wizard.getProperty("projdir");
            OpenProjects.getDefault().addPropertyChangeListener(new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent evt) {
                    try {
                        for (Project p : OpenProjects.getDefault().getOpenProjects()) {
                            File dir = FileUtil.toFile(p.getProjectDirectory());
                            if (projDir.equals(dir)) {
                                try {
                                    instantiate0();
                                    // Open the generated frame
                                    DataObject dob = DataObject.find(file);
                                    OpenCookie cookie = dob.getCookie(OpenCookie.class);
                                    cookie.open();
                                } catch (IOException ioex) {
                                    Logger.getLogger(getClass().getName()).log(Level.INFO, ioex.getMessage(), ioex);
                                }
                                break;
                            } // else something went wrong - don't attempt to instantiate
                        }
                    } finally {
                        OpenProjects.getDefault().removePropertyChangeListener(this);
                    }
                }
            });
            return Collections.EMPTY_SET;
        } else {
            return instantiate0();
        }
    }
    
    /**
     * Returns set of instantiated objects.
     *
     * @return set of instantiated objects.
     * @throws IOException when the objects cannot be instantiated.
     */
    public Set instantiate0() throws IOException {
        Set resultSet = null;
        try {
            DatabaseConnection connection = (DatabaseConnection)wizard.getProperty("connection"); // NOI18N
            String masterTableName = (String)wizard.getProperty("master"); // NOI18N
            String detailFKTable = (String)wizard.getProperty("detailFKTable"); // NOI18N
            joinColumn = (String)wizard.getProperty("detailFKColumn"); // NOI18N
            referencedColumn = (String)wizard.getProperty("detailPKColumn"); // NOI18N

            FileObject javaFile;
            if (delegateIterator != null) {
                resultSet = delegateIterator.instantiate();
                javaFile = (FileObject)resultSet.iterator().next();
            }
            else {
                resultSet = new HashSet();
                javaFile = (FileObject) wizard.getProperty("mainForm"); // NOI18N
                resultSet.add(javaFile);
            }
            javaFile.setAttribute("justCreatedByNewWizard", Boolean.TRUE); // NOI18N
            DataObject dob = null;
            try {
                dob = DataObject.find(javaFile);
            } catch (DataObjectNotFoundException ex) {
                Logger.getLogger(getClass().getName()).log(Level.INFO, ex.getMessage(), ex); // should not happen
            }
            FileObject formFile = FileUtil.findBrother(dob.getPrimaryFile(), "form"); // NOI18N

            String[][] entity = instantiatePersitence(javaFile.getParent(), connection, masterTableName, detailFKTable);

            String masterClass = entity[0][1];
            String masterEntity = entity[0][0];
            String detailClass = null;
            String detailEntity = null;
            
            if (entity[1] != null) {
                detailClass = entity[1][1];
                detailEntity = entity[1][0];
            }
            MasterDetailGenerator generator = new MasterDetailGenerator(formFile, javaFile,
                masterClass, detailClass, masterEntity, detailEntity,
                (joinInfo == null) ? null : joinInfo[0], (joinInfo == null) ? null : joinInfo[1], unitName);

            List<String> masterColumnNames = (List<String>)wizard.getProperty("masterColumns"); // NOI18N
            List<String> masterColumns = J2EEUtils.propertiesForColumns(mappings, masterEntity, masterColumnNames);
            generator.setMasterColumns(masterColumns);

            List<String> masterColumnTypes = J2EEUtils.typesOfProperties(javaFile, masterClass, masterColumns);
            generator.setMasterColumnTypes(masterColumnTypes);

            List<String> detailColumnNames = (List<String>)wizard.getProperty("detailColumns"); // NOI18N
            List<String> detailColumns = J2EEUtils.propertiesForColumns(mappings, (detailEntity == null) ? masterEntity : detailEntity, detailColumnNames);;
            generator.setDetailColumns(detailColumns);

            if (detailClass != null) {
                List<String> detailColumnTypes = J2EEUtils.typesOfProperties(javaFile, detailClass, detailColumns);
                generator.setDetailColumnTypes(detailColumnTypes);
            }

            generator.generate();
        } catch (Exception ex) {
            Logger.getLogger(getClass().getName()).log(Level.INFO, ex.getMessage(), ex);
        }
        
        return resultSet;
    }

    private MetadataModel<EntityMappingsMetadata> mappings;
    /** Name of persistence unit that contains entity classes for master and detail tables. */
    private String unitName;
    
    /**
     * Creates or updates persistence descriptor and entity classes for master and detail table.
     * 
     * @param folder folder whether the entity classes should be generated.
     * @param connection connection to the database with master and detail table.
     * @param tableName name of the master table.
     * @param detailTable name of the detail table (can be <code>null</code>).
     * @return entities that correspond to master and detail tables.
     */
    private String[][] instantiatePersitence(FileObject folder, DatabaseConnection connection, String tableName, String detailTable) {
        Project project = FileOwnerQuery.getOwner(folder);
        try {
            // Make sure persistence.xml file exists
            FileObject persistenceXML = J2EEUtils.getPersistenceXML(project, true);
            
            // Initializes persistence unit and persistence descriptor
            PersistenceUnit unit = J2EEUtils.initPersistenceUnit(persistenceXML, connection);
            unitName = unit.getName();

            // Initializes project's classpath
            // PENDING solicit for DatabaseConnection.getJDBCDriver
            JDBCDriver[] driver = JDBCDriverManager.getDefault().getDrivers(connection.getDriverClass());
            J2EEUtils.updateProjectForUnit(persistenceXML, unit, driver[0]);

            // Obtain description of entity mappings
            PersistenceScope scope = PersistenceScope.getPersistenceScope(folder);
            mappings = scope.getEntityMappingsModel(unit.getName());
 
            String[] tables;
            if (J2EEUtils.TABLE_CLOSURE) {
                tables = new String[1];
                if (detailTable == null) {
                    tables[0] = tableName;
                } else {
                    tables[0] = detailTable;
                    String[] entityInfo = J2EEUtils.findEntity(mappings, detailTable);
                    if (entityInfo != null) {
                        // Detail table exists, make sure to create master table
                        tables[0] = tableName;
                    }
                }
            } else {
                if (detailTable == null) {
                    tables = new String[] {tableName, detailTable};
                } else {
                    tables = new String[] {tableName};
                }
            }
            
            for (String table : tables) {
                // Find entity that corresponds to the table
                String[] entityInfo = J2EEUtils.findEntity(mappings, table);

                // Create a new entity (if there isn't one that corresponds to the table)
                if (entityInfo == null) {
                    // Generates a Java class for the entity
                    J2EEUtils.createEntity(folder, scope, unit, connection, table,
                        (J2EEUtils.TABLE_CLOSURE && (detailTable != null)) ? new String[] {tableName} : null);

                    entityInfo = J2EEUtils.findEntity(mappings, table);
                } else {
                    // Add the entity into the persistence unit if it is not there already
                    J2EEUtils.addEntityToUnit(entityInfo[1], unit, project);
                }
            }

            String[][] result = new String[2][];
            result[0] = J2EEUtils.findEntity(mappings, tableName);
            if (detailTable != null)  {
                result[1] = J2EEUtils.findEntity(mappings, detailTable);
                joinInfo = findOneToManyRelationProperties(mappings, result[0][0], result[1][0], joinColumn);
                if ((joinInfo == null) || joinInfo[1] == null) {
                    // Determine whether there already exist field mapped to PK column
                    boolean anotherDetailFieldExists = !J2EEUtils.propertiesForColumns(mappings, result[1][0], Collections.singletonList(joinColumn)).isEmpty();

                    // Issue 102630 - missing relationship between entities
                    addRelationship(result[0], result[1], joinColumn, referencedColumn, (joinInfo == null) ? null : joinInfo[0], anotherDetailFieldExists, folder);
                    joinInfo = findOneToManyRelationProperties(mappings, result[0][0], result[1][0], joinColumn);
                }
            }
            
            J2EEUtils.makeEntityObservable(folder, result[0], mappings);
            if (detailTable != null) {
                J2EEUtils.makeEntityObservable(folder, result[1], mappings);
            }

            return result;
        } catch (Exception ex) {
            Logger.getLogger(getClass().getName()).log(Level.INFO, ex.getMessage(), ex);
        }
        return null;
    }

    /**
     * Uninitializes the wizard.
     *
     * @param wizard descriptor of the wizard.
     */
    public void uninitialize(WizardDescriptor wizard) {
        if (delegateIterator != null)
            delegateIterator.uninitialize(wizard);
    }

    /**
     * Adds change listener.
     *
     * @param listener change listener to add.
     */
    public void addChangeListener(ChangeListener listener) {
        // Not used
    }

    /**
     * Removes change listener.
     *
     * @param listener change listener to remove.
     */
    public void removeChangeListener(ChangeListener listener) {
        // Not used
    }

    private static String[] findOneToManyRelationProperties(MetadataModel<EntityMappingsMetadata> mappings,
            final String masterEntityName, final String detailEntityName, final String relationColumn) throws IOException {
        String[] properties = null;
        try {
            properties = mappings.runReadActionWhenReady(new MetadataModelAction<EntityMappingsMetadata, String[]>() {
                public String[] run(EntityMappingsMetadata metadata) {
                    Entity[] entities = metadata.getRoot().getEntity();
                    Entity masterEntity = null;
                    Entity detailEntity = null;
                    for (int i=0; i<entities.length; i++) {
                        String entityName = entities[i].getName();
                        if (masterEntityName.equals(entityName)) {
                            masterEntity = entities[i];
                        }
                        if (detailEntityName.equals(entityName)) {
                            detailEntity = entities[i];
                        }
                    }
                    String relationField = null;
                    for (ManyToOne manyToOne : detailEntity.getAttributes().getManyToOne()) {
                        // PENDING when there can be more JoinColumns?
                        String columnName = manyToOne.getJoinColumn(0).getName();
                        if (relationColumn.equals(columnName)) {
                            relationField = manyToOne.getName();
                            break;
                        }
                    }
                    for (OneToMany oneToMany : masterEntity.getAttributes().getOneToMany()) {
                        String targetEntity = oneToMany.getTargetEntity();
                        int index = targetEntity.lastIndexOf('.');
                        if (index != -1) {
                            targetEntity = targetEntity.substring(index+1);
                        }
                        if (detailEntityName.equals(targetEntity)
                                && relationField.equals(oneToMany.getMappedBy())) {
                            return new String[] {
                                J2EEUtils.fieldToProperty(relationField),
                                J2EEUtils.fieldToProperty(oneToMany.getName())
                            };
                        }
                    }
                    if (relationField != null) {
                        return new String[] { J2EEUtils.fieldToProperty(relationField), null };
                    } else {
                        return null;
                    }
                }
            }).get();
        } catch (InterruptedException iex) {
            Logger.getLogger(MasterDetailWizard.class.getName()).log(Level.INFO, iex.getMessage(), iex);
        } catch (ExecutionException eex) {
            Logger.getLogger(MasterDetailWizard.class.getName()).log(Level.INFO, eex.getMessage(), eex);
        }
        return properties;
    }
    
    private static void addRelationship(final String[] masterInfo, final String[] detailInfo,
            final String joinColumn, final String referencedColumn, String detailField,
            final boolean anotherDetailFieldExists, FileObject fileInProject) {
        ClassPath cp = ClassPath.getClassPath(fileInProject, ClassPath.SOURCE);
        final String[] df = new String[] {detailField};
        if (df[0] == null) {
            String detailJava = detailInfo[1].replace('.', '/') + ".java"; // NOI18N
            FileObject detailFob = cp.findResource(detailJava);
            if (detailFob == null) return;
            JavaSource source = JavaSource.forFileObject(detailFob);
            try {
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

                        // Find existing fields and methods
                        int idx = 0;
                        int fieldIndex = 0;
                        Set<String> methods = new HashSet<String>();
                        Set<String> fields = new HashSet<String>();
                        for (Tree member : clazz.getMembers()) {
                            idx++;
                            if (Tree.Kind.VARIABLE == member.getKind()) {
                                VariableTree variable = (VariableTree)member;
                                fields.add(variable.getName().toString());
                                fieldIndex = idx;
                            } else if (Tree.Kind.METHOD == member.getKind()) {
                                MethodTree method = (MethodTree)member;
                                methods.add(method.getName().toString());
                            }
                        }

                        // Determine preferred name of the field
                        StringBuilder sb = new StringBuilder();
                        boolean upper = false;
                        for (int i=0; i<joinColumn.length(); i++) {
                            char c = joinColumn.charAt(i);
                            if (c == '_') {
                                upper = true;
                            } else {
                                if (upper) {
                                    c = Character.toUpperCase(c);
                                } else {
                                    c = Character.toLowerCase(c);
                                }
                                sb.append(c);
                                upper = false;
                            }
                        }
                        String detailFieldName = sb.toString();
                        String detailMethodSuffix = Character.toUpperCase(detailFieldName.charAt(0)) + detailFieldName.substring(1);
                        String candFieldName = detailFieldName;
                        String candMethodSuffix = detailMethodSuffix;
                        int count = 1;
                        boolean ok = false;
                        while (!ok) {
                            ok = !fields.contains(candFieldName);
                            ok = ok && !methods.contains("set" + candMethodSuffix); // NOI18N
                            ok = ok && !methods.contains("get" + candMethodSuffix); // NOI18N
                            if (!ok) {
                                count++;
                                candFieldName = detailFieldName + count;
                                candMethodSuffix = detailMethodSuffix + count;
                            }
                        }
                        detailFieldName = candFieldName;
                        detailMethodSuffix = candMethodSuffix;
                        df[0] = detailFieldName;

                        // Add the field
                        TreeMaker make = wc.getTreeMaker();
                        AssignmentTree nameParameter = make.Assignment(make.Identifier("name"), make.Literal(joinColumn)); // NOI18N
                        AssignmentTree referencedParameter = make.Assignment(make.Identifier("referencedColumnName"), make.Literal(referencedColumn)); // NOI18N
                        List<ExpressionTree> parameters = new ArrayList<ExpressionTree>();
                        parameters.add(nameParameter);
                        parameters.add(referencedParameter);
                        if (anotherDetailFieldExists) {
                            AssignmentTree updatableParameter = make.Assignment(make.Identifier("updatable"), make.Identifier("false")); // NOI18N
                            AssignmentTree insertableParameter = make.Assignment(make.Identifier("insertable"), make.Identifier("false")); // NOI18N
                            parameters.add(updatableParameter);
                            parameters.add(insertableParameter);
                        }
                        TypeElement joinColumnElement = wc.getElements().getTypeElement("javax.persistence.JoinColumn"); // NOI18N
                        AnnotationTree joinColumnTree = make.Annotation(make.QualIdent(joinColumnElement), parameters);
                        TypeElement manyToOneElement = wc.getElements().getTypeElement("javax.persistence.ManyToOne"); // NOI18N
                        AnnotationTree manyToOneTree = make.Annotation(make.QualIdent(manyToOneElement), Collections.EMPTY_LIST); // NOI18N
                        List<AnnotationTree> annotations = new ArrayList<AnnotationTree>(2);
                        annotations.add(joinColumnTree);
                        annotations.add(manyToOneTree);
                        ModifiersTree modifiers = make.Modifiers(Collections.singleton(Modifier.PRIVATE), annotations);
                        TypeElement masterElement = wc.getElements().getTypeElement(masterInfo[1]);
                        VariableTree field = make.Variable(modifiers, detailFieldName, make.QualIdent(masterElement), null);
                        ClassTree modifiedClass = make.insertClassMember(clazz, fieldIndex, field);
                        wc.rewrite(clazz, modifiedClass);

                        // Add getter
                        ReturnTree returnExp = make.Return(make.Identifier(detailFieldName));
                        MethodTree getMethod = make.Method(
                            make.Modifiers(Collections.singleton(Modifier.PUBLIC), Collections.EMPTY_LIST),
                            "get" + detailMethodSuffix, // NOI18N
                            make.QualIdent(masterElement),
                            Collections.EMPTY_LIST,
                            Collections.EMPTY_LIST,
                            Collections.EMPTY_LIST,
                            make.Block(Collections.singletonList(returnExp), false),
                            null
                        );
                        modifiedClass = make.addClassMember(modifiedClass, getMethod);
                        wc.rewrite(clazz, modifiedClass);

                        // Add setter
                        ModifiersTree parMods = make.Modifiers(Collections.EMPTY_SET, Collections.EMPTY_LIST);
                        VariableTree par = make.Variable(parMods, detailFieldName, make.QualIdent(masterElement), null);
                        AssignmentTree assignExp = make.Assignment(make.Identifier("this." + detailFieldName), make.Identifier(detailFieldName));
                        MethodTree setMethod = make.Method(
                            make.Modifiers(Collections.singleton(Modifier.PUBLIC), Collections.EMPTY_LIST),
                            "set" + detailMethodSuffix, // NOI18N
                            make.PrimitiveType(TypeKind.VOID),
                            Collections.EMPTY_LIST,
                            Collections.singletonList(par),
                            Collections.EMPTY_LIST,
                            make.Block(Collections.singletonList(make.ExpressionStatement(assignExp)), false),
                            null
                        );
                        modifiedClass = make.addClassMember(modifiedClass, setMethod);
                        wc.rewrite(clazz, modifiedClass);
                    }
                    public void cancel() {
                    }

                }).commit();
            } catch (IOException ioex) {
                Logger.getLogger(MasterDetailWizard.class.getName()).log(Level.INFO, ioex.getMessage(), ioex);
            }
        }

        String masterJava = masterInfo[1].replace('.', '/') + ".java"; // NOI18N
        FileObject masterFob = cp.findResource(masterJava);
        if (masterFob == null) return;
        JavaSource source = JavaSource.forFileObject(masterFob);
        try {
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

                    // Find existing fields and methods
                    int idx = 0;
                    int fieldIndex = 0;
                    Set<String> methods = new HashSet<String>();
                    Set<String> fields = new HashSet<String>();
                    for (Tree member : clazz.getMembers()) {
                        idx++;
                        if (Tree.Kind.VARIABLE == member.getKind()) {
                            VariableTree variable = (VariableTree)member;
                            fields.add(variable.getName().toString());
                            fieldIndex = idx;
                        } else if (Tree.Kind.METHOD == member.getKind()) {
                            MethodTree method = (MethodTree)member;
                            methods.add(method.getName().toString());
                        }
                    }

                    // Determine preferred name of the field
                    String masterFieldName = detailInfo[0] + "Collection"; // NOI18N
                    masterFieldName = Character.toLowerCase(masterFieldName.charAt(0)) + masterFieldName.substring(1);
                    String masterMethodSuffix = Character.toUpperCase(masterFieldName.charAt(0)) + masterFieldName.substring(1);
                    String candFieldName = masterFieldName;
                    String candMethodSuffix = masterMethodSuffix;
                    int count = 1;
                    boolean ok = false;
                    while (!ok) {
                        ok = !fields.contains(candFieldName);
                        ok = ok && !methods.contains("set" + candMethodSuffix); // NOI18N
                        ok = ok && !methods.contains("get" + candMethodSuffix); // NOI18N
                        if (!ok) {
                            count++;
                            candFieldName = masterFieldName + count;
                            candMethodSuffix = masterMethodSuffix + count;
                        }
                    }
                    masterFieldName = candFieldName;
                    masterMethodSuffix = candMethodSuffix;

                    // Add the field
                    TreeMaker make = wc.getTreeMaker();
                    TypeElement cascadeTypeElement = wc.getElements().getTypeElement("javax.persistence.CascadeType"); // NOI18N
                    AssignmentTree cascadeParameter = make.Assignment(make.Identifier("cascade"), make.MemberSelect(make.QualIdent(cascadeTypeElement),"ALL")); // NOI18N
                    AssignmentTree mappedByParameter = make.Assignment(make.Identifier("mappedBy"), make.Literal(df[0])); // NOI18N
                    List<ExpressionTree> parameters = new ArrayList<ExpressionTree>();
                    parameters.add(cascadeParameter);
                    parameters.add(mappedByParameter);
                    TypeElement oneToManyElement = wc.getElements().getTypeElement("javax.persistence.OneToMany"); // NOI18N
                    AnnotationTree oneToManyTree = make.Annotation(make.QualIdent(oneToManyElement), parameters);
                    ModifiersTree modifiers = make.Modifiers(Collections.singleton(Modifier.PRIVATE), Collections.singletonList(oneToManyTree));
                    TypeElement collectionElement = wc.getElements().getTypeElement("java.util.Collection"); // NOI18N
                    TypeElement detailElement = wc.getElements().getTypeElement(detailInfo[1]);
                    ParameterizedTypeTree collectionTree = make.ParameterizedType(make.QualIdent(collectionElement), Collections.singletonList(make.QualIdent(detailElement)));
                    VariableTree field = make.Variable(modifiers, masterFieldName, collectionTree, null);
                    ClassTree modifiedClass = make.insertClassMember(clazz, fieldIndex, field);
                    wc.rewrite(clazz, modifiedClass);

                    // Add getter
                    ReturnTree returnExp = make.Return(make.Identifier(masterFieldName));
                    MethodTree getMethod = make.Method(
                        make.Modifiers(Collections.singleton(Modifier.PUBLIC), Collections.EMPTY_LIST),
                        "get" + masterMethodSuffix, // NOI18N
                        collectionTree,
                        Collections.EMPTY_LIST,
                        Collections.EMPTY_LIST,
                        Collections.EMPTY_LIST,
                        make.Block(Collections.singletonList(returnExp), false),
                        null
                    );
                    modifiedClass = make.addClassMember(modifiedClass, getMethod);
                    wc.rewrite(clazz, modifiedClass);
                    
                    // Add setter
                    ModifiersTree parMods = make.Modifiers(Collections.EMPTY_SET, Collections.EMPTY_LIST);
                    VariableTree par = make.Variable(parMods, masterFieldName, collectionTree, null);
                    AssignmentTree assignExp = make.Assignment(make.Identifier("this." + masterFieldName), make.Identifier(masterFieldName));
                    MethodTree setMethod = make.Method(
                        make.Modifiers(Collections.singleton(Modifier.PUBLIC), Collections.EMPTY_LIST),
                        "set" + masterMethodSuffix, // NOI18N
                        make.PrimitiveType(TypeKind.VOID),
                        Collections.EMPTY_LIST,
                        Collections.singletonList(par),
                        Collections.EMPTY_LIST,
                        make.Block(Collections.singletonList(make.ExpressionStatement(assignExp)), false),
                        null
                    );
                    modifiedClass = make.addClassMember(modifiedClass, setMethod);
                    wc.rewrite(clazz, modifiedClass);
                }
                public void cancel() {
                }

            }).commit();
        } catch (IOException ioex) {
            Logger.getLogger(MasterDetailWizard.class.getName()).log(Level.INFO, ioex.getMessage(), ioex);
        }
    }

}
