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

package org.netbeans.modules.j2ee.persistence.wizard.fromdb;

import com.sun.source.tree.*;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.progress.aggregate.ProgressContributor;
import org.netbeans.modules.j2ee.core.api.support.java.SourceUtils;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.Entity;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.Table;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.openide.filesystems.FileObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.*;
import javax.lang.model.type.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.core.api.support.classpath.ContainerClassPathModifier;
import org.netbeans.modules.j2ee.core.api.support.java.GenerationUtils;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.j2ee.persistence.api.EntityClassScope;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.EntityMappingsMetadata;
import org.netbeans.modules.j2ee.persistence.dd.common.PersistenceUnit;
import org.netbeans.modules.j2ee.persistence.entitygenerator.CMPMappingModel;
import org.netbeans.modules.j2ee.persistence.entitygenerator.CMPMappingModel.ColumnData;
import org.netbeans.modules.j2ee.persistence.entitygenerator.EntityClass;
import org.netbeans.modules.j2ee.persistence.entitygenerator.EntityMember;
import org.netbeans.modules.j2ee.persistence.entitygenerator.EntityRelation.CollectionType;
import org.netbeans.modules.j2ee.persistence.entitygenerator.EntityRelation.FetchType;
import org.netbeans.modules.j2ee.persistence.entitygenerator.RelationshipRole;
import org.netbeans.modules.j2ee.persistence.provider.InvalidPersistenceXmlException;
import org.netbeans.modules.j2ee.persistence.provider.ProviderUtil;
import org.netbeans.modules.j2ee.persistence.unit.PUDataObject;
import org.netbeans.modules.j2ee.persistence.util.EntityMethodGenerator;
import org.netbeans.modules.j2ee.persistence.util.JPAClassPathHelper;
import org.netbeans.modules.j2ee.persistence.util.MetadataModelReadHelper;
import org.netbeans.modules.j2ee.persistence.util.MetadataModelReadHelper.State;
import org.netbeans.modules.j2ee.persistence.wizard.Util;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 * Generator of Java Persistence API ORM classes from DB.
 *
 * @author Pavel Buzek, Andrei Badea
 */
public class JavaPersistenceGenerator implements PersistenceGenerator {

    // XXX Javadoc for generated code missing in many places - issue 90302
    // XXX createToStringMethod() could be moved to GenerationUtils
    // XXX init() commented out until annotation model is implemented

    // XXX empty lines in generated hashCode() - issue 90186
    // XXX comments are lost in method body passed as string - issue 89873
    // XXX return 0, 1 in generated equals() - issue 90183
    // XXX empty line in generated equals() - issue 90186

    private final Map<String, String> entityName2TableName = new HashMap<String, String>();

    // options (not currently exposed in UI)
    // field vs. property access
    private static boolean fieldAccess = true;
    // named input params for named queries vs. positional params
    private static boolean genNamedParams = true;
    // should generated Entity Classes implement Serializable?
    private static boolean genSerializableEntities = true;

    private Set<FileObject> result;

    /**
     * Specifies whether the generated enties should be added to the first
     * persistence unit found in the project. Note that this setting only
     * applies to non-Java EE 5 projects - for Java EE 5 projects the entities
     * are not added to a PU even if this is true.
     */
    private final boolean addToAutoDiscoveredPU;

    /**
     * The persistence unit to which the generated entities should
     * be added.
     */
    private PersistenceUnit persistenceUnit;

    /**
     * Creates a new instance of JavaPersistenceGenerator. Tries to add the
     * generated entities to the first persistence unit found (only in non-Java EE 5 projects).
     */
    public JavaPersistenceGenerator() {
        this.persistenceUnit = null;
        this.addToAutoDiscoveredPU = true;
    }


    /**
     * Creates a new instance of JavaPersistenceGenerator
     *
     * @param persistenceUnit the persistence unit to which the generated entities
     * should be added. Must exist in the project where the entities are generated.
     * Has no effect in Java EE 5 projects - in those
     * the entities are not added to any persistence unit regardless of this. May
     * be null, in which case the generated entities are not added any persistence unit.
     */
    public JavaPersistenceGenerator(PersistenceUnit persistenceUnit) {
        this.persistenceUnit = persistenceUnit;
        this.addToAutoDiscoveredPU = false;
    }


    public void generateBeans(final ProgressPanel progressPanel,
            final RelatedCMPHelper helper,
            final FileObject dbSchemaFile,
            final ProgressContributor handle) throws IOException {
        
        generateBeans(helper.getBeans(), helper.isGenerateFinderMethods(), 
                helper.isFullyQualifiedTableNames(), helper.isRegenTablesAttrs(),
                helper.getFetchType(), helper.getCollectionType(),
                handle, progressPanel, helper.getProject());
    }

    // package private for tests
    void generateBeans(EntityClass[] entityClasses, boolean generateNamedQueries, 
            boolean fullyQualifiedTableNames, boolean regenTablesAttrs,
            FetchType fetchType, CollectionType collectionType,
            ProgressContributor progressContributor, ProgressPanel panel, Project prj) throws IOException {

        int progressMax = entityClasses.length * 2;
        progressContributor.start(progressMax);
        if (prj != null) {
            ContainerClassPathModifier modifier = prj.getLookup().lookup(ContainerClassPathModifier.class);
            if (modifier != null) {
                progressContributor.progress(NbBundle.getMessage(JavaPersistenceGenerator.class, "LBL_Progress_Adding_Classpath"));
                //TODO not project directory, but source root.
                modifier.extendClasspath(prj.getProjectDirectory(),
                        new String[] {
                            ContainerClassPathModifier.API_ANNOTATION,
                            ContainerClassPathModifier.API_PERSISTENCE,
                            ContainerClassPathModifier.API_TRANSACTION
                        });
            }
        }

        result = new Generator(entityClasses, generateNamedQueries, 
                fullyQualifiedTableNames, regenTablesAttrs,
                fetchType, collectionType,
                progressContributor, panel).run();
        addToPersistenceUnit(result);
        progressContributor.progress(progressMax);
    }

    
    /**
     * Adds the given entities to out persistence unit found in the project.
     */
    private void addToPersistenceUnit(Set<FileObject> entities){

        if (entities.isEmpty()){
            return;
        }

        if (persistenceUnit == null && !addToAutoDiscoveredPU){
            return;
        }
        
        Project project = FileOwnerQuery.getOwner(entities.iterator().next());
        if (project != null && !Util.isSupportedJavaEEVersion(project) && ProviderUtil.getDDFile(project) != null) {
            try {
                PUDataObject pudo = ProviderUtil.getPUDataObject(project);
                // no persistence unit was provider, we'll try find one
                if (persistenceUnit == null){
                    PersistenceUnit pu[] = pudo.getPersistence().getPersistenceUnit();
                    //only add if a PU exists, if there are more we do not know where to add - UI needed to ask
                    if (pu.length == 1) {
                        persistenceUnit = pu[0];
                    }
                }
                if (persistenceUnit != null){
                    ClassPathProvider classPathProvider = project.getLookup().lookup(ClassPathProvider.class);
                    if (classPathProvider != null) {
                        for(FileObject entity : entities){
                            String entityFQN = classPathProvider.findClassPath(entity, ClassPath.SOURCE).getResourceName(entity, '.', false);
                            pudo.addClass(persistenceUnit, entityFQN);
                        }
                    }
                }

            } catch (InvalidPersistenceXmlException ipx){
                // just log for debugging purposes, at this point the user has
                // already been warned about an invalid persistence.xml
                Logger.getLogger(JavaPersistenceGenerator.class.getName()).log(Level.FINE, "Invalid persistence.xml: " + ipx.getPath(), ipx); //NOI18N
            }
        }

    }

    public void init(WizardDescriptor wiz) {
        // get the table names for all entities in the project
        Project project = Templates.getProject(wiz);
        final MetadataModelReadHelper<EntityMappingsMetadata, Set<Entity>> readHelper;
        EntityClassScope entityClassScope = EntityClassScope.getEntityClassScope(project.getProjectDirectory());
        if(entityClassScope == null) {
            return;
        }
           
        MetadataModel<EntityMappingsMetadata> entityMappingsModel = entityClassScope.getEntityMappingsModel(true);
        readHelper = MetadataModelReadHelper.create(entityMappingsModel, new MetadataModelAction<EntityMappingsMetadata, Set<Entity>>() {
            public Set<Entity> run(EntityMappingsMetadata metadata) {
                Set<Entity> result = new HashSet<Entity>();
                for (Entity entity : metadata.getRoot().getEntity()) {
                    result.add(entity);
                }
                return result;
            }
        });
        
        readHelper.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                if (readHelper.getState() == State.FINISHED) {
                    try {
                        processEntities(readHelper.getResult());
                    } catch (ExecutionException ex) {
                        Logger.getLogger(JavaPersistenceGenerator.class.getName()).log(Level.FINE, "Failed to get entity classes: ", ex); //NOI18N
                    }
                }
            }
        });
        readHelper.start();
    }

    private void processEntities(Set<Entity> entityClasses) {
        for (Entity entity : entityClasses) {
            Table entityTable = entity.getTable();
            if (entityTable != null) {
                entityName2TableName.put(entityTable.getName(), entity.getClass2());
            }
        }
    }

    public void uninit() {
    }

    public String getFQClassName(String tableName) {
        return entityName2TableName.get(tableName);
    }

    public String generateEntityName(String name) {
        return name;
    }

    public Set<FileObject> createdObjects() {
        return result;
    }


    /**
     * Encapsulates the whole entity class generation process.
     */
    private static final class Generator {
        private final ProgressPanel progressPanel;
        private final ProgressContributor progressContributor;
        private final Map<String, EntityClass> beanMap = new HashMap<String, EntityClass>();
        private final EntityClass[] entityClasses;
        private final boolean generateNamedQueries;
        private final boolean fullyQualifiedTableNames;
        private final boolean regenTablesAttrs;
        private final FetchType fetchType;
        private final CollectionType collectionType;
        private final Set<FileObject> generatedEntityFOs;
        private final Set<FileObject> generatedFOs;

        public Generator(EntityClass[] entityClasses, boolean generateNamedQueries,
                boolean fullyQualifiedTableNames, boolean regenTablesAttrs, 
                FetchType fetchType, CollectionType collectionType,
                ProgressContributor progressContributor, ProgressPanel progressPanel) {
            this.entityClasses = entityClasses;
            this.generateNamedQueries = generateNamedQueries;
            this.fullyQualifiedTableNames = fullyQualifiedTableNames;
            this.regenTablesAttrs = regenTablesAttrs;
            this.fetchType = fetchType;
            this.collectionType = collectionType;
            this.progressContributor = progressContributor;
            this.progressPanel = progressPanel;
            generatedFOs = new HashSet<FileObject>();
            generatedEntityFOs = new HashSet<FileObject>();
        }

        public Set<FileObject> run() throws IOException {
            try {
                runImpl();
            } catch (IOException e) {
                for (FileObject generatedFO : generatedFOs) {
                    generatedFO.delete();
                }
                throw e;
            }
            return generatedEntityFOs;
        }

        public void runImpl() throws IOException {

            // first generate empty entity classes -- this is needed as
            // in the field and method generation it will be necessary to resolve
            // their types (e.g. entity A has a field of type Collection<B>, thus
            // while generating entity A we must be able to resolve type B).

            beanMap.clear();
            Set<FileObject> generationPackageFOs = new HashSet<FileObject>();
            Set<String> generatedEntityClasses = new HashSet<String>();

            for (int i = 0; i < entityClasses.length; i++) {
                final EntityClass entityClass = entityClasses[i];
                String entityClassName = entityClass.getClassName();
                FileObject packageFileObject = entityClass.getPackageFileObject();
                beanMap.put(entityClassName, entityClass);

                if (packageFileObject.getFileObject(entityClassName, "java") != null) { // NOI18N
                    progressContributor.progress(i);
                    continue;
                }
                String progressMsg = NbBundle.getMessage(JavaPersistenceGenerator.class, "TXT_GeneratingClass", entityClassName);

                progressContributor.progress(progressMsg, i);
                if (progressPanel != null){
                    progressPanel.setText(progressMsg);
                }

                generationPackageFOs.add(packageFileObject);
                generatedEntityClasses.add(entityClassName);

                // XXX Javadoc
                FileObject entity = GenerationUtils.createClass(packageFileObject, entityClassName, NbBundle.getMessage(JavaPersistenceGenerator.class, "MSG_Javadoc_Class"));
                generatedEntityFOs.add(entity);
                generatedFOs.add(entity);
                
                // NO PK classes for views
                if (entityClass.isForTable() && !entityClass.isUsePkField()) {
                    String pkClassName = createPKClassName(entityClassName);
                    if (packageFileObject.getFileObject(pkClassName, "java") == null) { // NOI18N
                        FileObject pkClass = GenerationUtils.createClass(packageFileObject, pkClassName, NbBundle.getMessage(JavaPersistenceGenerator.class, "MSG_Javadoc_PKClass", pkClassName, entityClassName));
                        generatedFOs.add(pkClass);
                    }
                }
            }

            // now generate the fields and methods for each entity class
            // and its primary key class


            for (int i = 0; i < entityClasses.length; i++) {
                final EntityClass entityClass = entityClasses[i];
                String entityClassName = entityClass.getClassName();

                if (!generatedEntityClasses.contains(entityClassName)) {
                    // this entity class already existed, we didn't create it, so we don't want to touch it
                    progressContributor.progress(entityClasses.length + i);
                    continue;
                }
                String progressMsg = NbBundle.getMessage(JavaPersistenceGenerator.class, "TXT_GeneratingClass", entityClassName);
                progressContributor.progress(progressMsg, entityClasses.length + i);
                if (progressPanel != null){
                    progressPanel.setText(progressMsg);
                }
                FileObject entityClassPackageFO = entityClass.getPackageFileObject();
                final FileObject entityClassFO = entityClassPackageFO.getFileObject(entityClassName, "java"); // NOI18N
                final FileObject pkClassFO = entityClassPackageFO.getFileObject(createPKClassName(entityClassName), "java"); // NOI18N
                try {

                    Set<ClassPath> bootCPs = getAllClassPaths(generationPackageFOs, ClassPath.BOOT);
                    Set<ClassPath> compileCPs = getAllClassPaths(generationPackageFOs, ClassPath.COMPILE);
                    Set<ClassPath> sourceCPs = getAllClassPaths(generationPackageFOs, ClassPath.SOURCE);

                    JPAClassPathHelper cpHelper = new JPAClassPathHelper(bootCPs, compileCPs, sourceCPs);

                    JavaSource javaSource = (pkClassFO != null) ?
                        JavaSource.create(cpHelper.createClasspathInfo(), entityClassFO, pkClassFO) :
                        JavaSource.create(cpHelper.createClasspathInfo(), entityClassFO);
                    javaSource.runModificationTask(new Task<WorkingCopy>() {
                        public void run(WorkingCopy copy) throws IOException {
                            if (copy.getFileObject().equals(entityClassFO)) {
                                EntityClassGenerator clsGen = new EntityClassGenerator(copy, entityClass);
                                clsGen.run();
                            } else {
                                new PKClassGenerator(copy, entityClass).run();
                            }
                        }
                    }).commit();
                } catch (IOException e) {
                    String message = e.getMessage();
                    String newMessage = ((message == null) ?
                        NbBundle.getMessage(JavaPersistenceGenerator.class, "ERR_GeneratingClass_NoExceptionMessage", entityClassName) :
                        NbBundle.getMessage(JavaPersistenceGenerator.class, "ERR_GeneratingClass", entityClassName, message));
                    IOException wrappedException = new IOException(newMessage);
                    wrappedException.initCause(e);
                    throw wrappedException;
                }

            }
        }


        private static String createPKClassName(String entityClassName) {
            return entityClassName + "PK"; // NOI18N
        }

        private static Set<ClassPath> getAllClassPaths(Set<FileObject> fileObjects, String id) {
            Set<ClassPath> classPaths = new HashSet<ClassPath>();
            for (FileObject fileObject : fileObjects) {
                classPaths.add(ClassPath.getClassPath(fileObject, id));
            }
            return classPaths;
        }


        /**
         * Encapsulates common logic for generating classes (be it
         * entity or primary key classes). Each instance generates a single
         * class.
         */
        private abstract class ClassGenerator {

            protected final WorkingCopy copy;
            protected final GenerationUtils genUtils;

            // the entity class we are generating
            protected final EntityClass entityClass;
            // the mapping of the entity class to the database
            protected final CMPMappingModel dbMappings;
            // true if a primary key class needs to be generated along with the entity class
            protected final boolean needsPKClass;
            // the simple class name of the primary key class
            protected final String pkClassName;
            // the fully-qualified name of the primary key class
            protected final String pkFQClassName;

            // generated properties
            protected final List<Property> properties = new ArrayList<Property>();
            // generated methods
            protected final List<MethodTree> methods = new ArrayList<MethodTree>();
            // generated constructors
            protected final List<MethodTree> constructors = new ArrayList<MethodTree>();
            // generated fields. does not include fields of properties, just plain fields 
            protected final List<VariableTree> fields = new ArrayList<VariableTree>();

            // the original class tree of the class we are generating
            protected ClassTree originalClassTree;
            // the modified class tree of the class we are generating
            protected ClassTree newClassTree;
            // the TypeElement corresponding to classTree
            protected TypeElement typeElement;

            public ClassGenerator(WorkingCopy copy, EntityClass entityClass) throws IOException {
                this.copy = copy;
                copy.toPhase(Phase.RESOLVED);

                this.entityClass = entityClass;
                dbMappings = entityClass.getCMPMapping();
                // NO PK for views
                needsPKClass = entityClass.isForTable() && !entityClass.isUsePkField();
                    
                pkClassName = needsPKClass ? createPKClassName(entityClass.getClassName()) : null;
                pkFQClassName = entityClass.getPackage() + "." + pkClassName; // NOI18N

                typeElement = SourceUtils.getPublicTopLevelElement(copy);
                if (typeElement == null) {
                    throw new IllegalStateException("Cannot find a public top-level class named " + entityClass.getClassName() +  // NOI18N
                            " in " + FileUtil.getFileDisplayName(copy.getFileObject())); // NOI18N
                }
                originalClassTree = (ClassTree)copy.getTrees().getTree(typeElement);
                assert originalClassTree != null;
                newClassTree = originalClassTree;
                genUtils = GenerationUtils.newInstance(copy);
            }

            protected String createFieldName(String capitalizedFieldName) {
                return createFieldNameImpl(capitalizedFieldName, false);
            }

            protected String createCapitalizedFieldName(String fieldName) {
                return createFieldNameImpl(fieldName, true);
            }

            private String createFieldNameImpl(String fieldName, boolean capitalized) {
                StringBuffer sb = new StringBuffer(fieldName);
                char firstChar = sb.charAt(0);
                sb.setCharAt(0, capitalized ? Character.toUpperCase(firstChar) : Character.toLowerCase(firstChar));
                return sb.toString();
            }

            /**
             * Creates a property for an entity member, that is, is creates
             * a field, a getter and a setter method.
             */
            protected Property createProperty(EntityMember m) throws IOException {
                boolean isPKMember = m.isPrimaryKey();
                List<AnnotationTree> annotations = new ArrayList<AnnotationTree>();

                //add @Id() only if not in an embeddable PK class
                if (isPKMember && !needsPKClass) {
                    annotations.add(genUtils.createAnnotation("javax.persistence.Id")); // NOI18N
                    if(m.isAutoIncrement()) {
                        // Can only support strategy=GenerationType.IDENTITY.
                        // See issue 76357 - desc 17
                        List<ExpressionTree> annArguments = new ArrayList<ExpressionTree>();
                        annArguments.add(genUtils.createAnnotationArgument("strategy", "javax.persistence.GenerationType", "IDENTITY")); // NOI18N
                        annotations.add(genUtils.createAnnotation("javax.persistence.GeneratedValue", annArguments)); //NOI18N
                    }
                } 
                
                // Add @Basic(optional=false) for not nullable columns
                if (!m.isNullable()) {
                    List<ExpressionTree> basicAnnArguments = new ArrayList();
                    basicAnnArguments.add(genUtils.createAnnotationArgument("optional", false)); //NOI18N
                    annotations.add(genUtils.createAnnotation("javax.persistence.Basic", basicAnnArguments)); //NOI18N
                }

                boolean isLobType = m.isLobType();
                if (isLobType) {
                    annotations.add(genUtils.createAnnotation("javax.persistence.Lob")); // NOI18N
                }

                List<ExpressionTree> columnAnnArguments = new ArrayList();
                String memberName = m.getMemberName();
                String memberType = getMemberType(m);

                String columnName = (String) dbMappings.getCMPFieldMapping().get(memberName);
                columnAnnArguments.add(genUtils.createAnnotationArgument("name", columnName)); //NOI18N
              
                if (regenTablesAttrs && !m.isNullable()) {
                    columnAnnArguments.add(genUtils.createAnnotationArgument("nullable", false)); //NOI18N
                }
                Integer length = m.getLength();
                Integer precision = m.getPrecision();
                Integer scale = m.getScale();
                if (regenTablesAttrs ) {
                    if(length != null && isCharacterType(memberType)) {
                        columnAnnArguments.add(genUtils.createAnnotationArgument("length", length)); // NOI18N
                    }
                    if(precision != null && isDecimalType(memberType)) {
                        columnAnnArguments.add(genUtils.createAnnotationArgument("precision", precision)); // NOI18N
                    }
                    if(scale != null && isDecimalType(memberType)) {
                        columnAnnArguments.add(genUtils.createAnnotationArgument("scale", scale)); // NOI18N
                    }
                }
                
                annotations.add(genUtils.createAnnotation("javax.persistence.Column", columnAnnArguments)); //NOI18N

                String temporalType = getMemberTemporalType(m);
                if (temporalType != null) {
                    ExpressionTree temporalAnnValueArgument = genUtils.createAnnotationArgument(null, "javax.persistence.TemporalType", temporalType); //NOI18N
                    annotations.add(genUtils.createAnnotation("javax.persistence.Temporal", Collections.singletonList(temporalAnnValueArgument)));
                }

                return new Property(Modifier.PRIVATE, annotations, memberType, memberName);
            }

            /**
             * Like {@link #createProperty}, but it only creates a variable
             * with no modififers and no annotations. Useful to pass in
             * a parameter list when creating a method or constructor.
             */
            protected VariableTree createVariable(EntityMember m) {
                return genUtils.createVariable(typeElement, m.getMemberName(), getMemberType(m));
            }

            private String getMemberType(EntityMember m) {
                String memberType = m.getMemberType();
                if ("java.sql.Date".equals(memberType)) { //NOI18N
                    memberType = "java.util.Date";
                } else if ("java.sql.Time".equals(memberType)) { //NOI18N
                    memberType = "java.util.Date";
                } else if ("java.sql.Timestamp".equals(memberType)) { //NOI18N
                    memberType = "java.util.Date";
                }
                return memberType;
            }
            
            private boolean isCharacterType(String type) {
                if ("java.lang.String".equals(type)) { // NOI18N
                    // XXX also need to check for char[] and Character[]
                    // (better to use TypeMirror)
                    return true;
                } 
                return false;
            }
            
            private boolean isDecimalType(String type) {
                if ("java.lang.Double".equals(type) || // NOI18N
                    "java.lang.Float".equals(type) || // NOI18N
                    "java.math.BigDecimal".equals(type)) { // NOI18N
                    return true;
                }
                return false;
            }

            private String getMemberTemporalType(EntityMember m) {
                String memberType = m.getMemberType();
                String temporalType = null;
                if ("java.sql.Date".equals(memberType)) { //NOI18N
                    temporalType = "DATE";
                } else if ("java.sql.Time".equals(memberType)) { //NOI18N
                    temporalType = "TIME";
                } else if ("java.sql.Timestamp".equals(memberType)) { //NOI18N
                    temporalType = "TIMESTAMP";
                }
                return temporalType;
            }

            public void run() throws IOException {
                initialize();
                for (Object object : entityClass.getFields()) {
                    generateMember((EntityMember)object);
                }
                afterMembersGenerated();
                for (RelationshipRole roleObject : entityClass.getRoles()) {
                    generateRelationship(roleObject);
                }
                finish();

                // add the generated members
                TreeMaker make = copy.getTreeMaker();
                int position = 0;
                for (VariableTree field : fields){
                    newClassTree = make.insertClassMember(newClassTree, position, field);
                    position++;
                }
                for (Property property : properties) {
                    newClassTree = make.insertClassMember(newClassTree, position, property.getField());
                    position++;
                }
                for (MethodTree constructor : constructors) {
                    newClassTree = make.addClassMember(newClassTree, constructor);
                }
                for (Property property : properties) {
                    newClassTree = make.addClassMember(newClassTree, property.getGetter());
                    newClassTree = make.addClassMember(newClassTree, property.getSetter());
                }
                for (MethodTree method : methods) {
                    newClassTree = make.addClassMember(newClassTree, method);
                }
                copy.rewrite(originalClassTree, newClassTree);
            }

            /**
             * Called at the beginning of the generation process.
             */
            protected abstract void initialize() throws IOException;

            /**
             * Called for each entity class member.
             */
            protected abstract void generateMember(EntityMember m) throws IOException;

            /**
             * Called after all members have been generated.
             */
            protected abstract void afterMembersGenerated() throws IOException;

            /**
             * Called for each relationship.
             */
            protected abstract void generateRelationship(RelationshipRole role) throws IOException;

            /**
             * Called at the end of the generation process.
             */
            protected abstract void finish() throws IOException;

            /**
             * Encapsulates a generated property, that is, its field, getter
             * and setter method.
             */
            protected final class Property {

                private final VariableTree field;
                private final MethodTree getter;
                private final MethodTree setter;

                public Property(Modifier modifier, List<AnnotationTree> annotations, String type, String name) throws IOException {
                    this(modifier, annotations, genUtils.createType(type, typeElement), name);
                }

                public Property(Modifier modifier, List<AnnotationTree> annotations, TypeMirror type, String name) throws IOException {
                    this(modifier, annotations, copy.getTreeMaker().Type(type), name);
                }

                private Property(Modifier modifier, List<AnnotationTree> annotations, Tree typeTree, String name) throws IOException {
                    TreeMaker make = copy.getTreeMaker();
                    field = make.Variable(
                            make.Modifiers(EnumSet.of(modifier), fieldAccess ? annotations : Collections.<AnnotationTree>emptyList()),
                            name,
                            typeTree,
                            null);
                    getter = genUtils.createPropertyGetterMethod(
                            make.Modifiers(EnumSet.of(Modifier.PUBLIC), fieldAccess ? Collections.<AnnotationTree>emptyList() : annotations),
                            name,
                            typeTree);
                    setter = genUtils.createPropertySetterMethod(
                            genUtils.createModifiers(Modifier.PUBLIC),
                            name,
                            typeTree);
                }

                public VariableTree getField() {
                    return field;
                }

                public MethodTree getGetter() {
                    return getter;
                }

                public MethodTree getSetter() {
                    return setter;
                }
            }
        }

        /**
         * An implementation of ClassGenerator which generates entity classes.
         */
        private final class EntityClassGenerator extends ClassGenerator {

            // the simple name of the entity class
            private final String entityClassName;
            // the fully-qualified name of the entity class
            private final String entityFQClassName;
            // the non-nullable properties (not including the primary key ones)
            private final List<Property> nonNullableProps = new ArrayList<Property>();
            // the names of the primary key columns
            private final List<String> pkColumnNames = new ArrayList<String>();
            // variables correspoding to the fields in the primary key classs (or empty if no primary key class)
            private final List<VariableTree> pkClassVariables = new ArrayList<VariableTree>();
            // the list of @NamedQuery annotations which will be added to the entity class
            private final List<ExpressionTree> namedQueryAnnotations = new ArrayList<ExpressionTree>();
            // the property for the primary key (or the primary key class)
            private Property pkProperty;
            // the prefix or all named queries ("select ... ")
            private String namedQueryPrefix;

            public EntityClassGenerator(WorkingCopy copy, EntityClass entityClass) throws IOException {
                super(copy, entityClass);
                entityClassName = entityClass.getClassName();
                assert typeElement.getSimpleName().contentEquals(entityClassName);
                entityFQClassName = entityClass.getPackage() + "." + entityClassName;
            }

            protected void initialize() throws IOException {
                newClassTree = genUtils.ensureNoArgConstructor(newClassTree);
                if (genSerializableEntities) {
                    newClassTree = genUtils.addImplementsClause(newClassTree, "java.io.Serializable"); // NOI18N
                }
                newClassTree = genUtils.addAnnotation(newClassTree, genUtils.createAnnotation("javax.persistence.Entity")); // NOI18N
                List<ExpressionTree> tableAnnArgs = new ArrayList<ExpressionTree>();
                tableAnnArgs.add(genUtils.createAnnotationArgument("name", dbMappings.getTableName())); // NOI18N
                if(fullyQualifiedTableNames) {
                    String schemaName = entityClass.getSchemaName();
                    String catalogName = entityClass.getCatalogName();
                    if(catalogName != null) {
                        tableAnnArgs.add(genUtils.createAnnotationArgument("catalog", catalogName)); // NOI18N
                    }
                    if(schemaName != null ) {
                        tableAnnArgs.add(genUtils.createAnnotationArgument("schema", schemaName)); // NOI18N
                    }
                }
                
                // UniqueConstraint annotations for the table
                if(regenTablesAttrs && entityClass.getUniqueConstraints() != null &&
                        entityClass.getUniqueConstraints().size() != 0) {
                    List<ExpressionTree> uniqueConstraintAnnotations = new ArrayList<ExpressionTree>();
                    for(List<String> constraintCols : entityClass.getUniqueConstraints()) {

                        List<ExpressionTree> colArgs = new ArrayList<ExpressionTree>();
                        for(String colName : constraintCols) {
                            colArgs.add(genUtils.createAnnotationArgument(null, colName));
                        }
                        ExpressionTree columnNamesArg = genUtils.createAnnotationArgument("columnNames", colArgs); // NOI18N
                        uniqueConstraintAnnotations.add(genUtils.createAnnotation("javax.persistence.UniqueConstraint", 
                                Collections.singletonList(columnNamesArg))); //NOI18N
                    }
                    
                    tableAnnArgs.add(genUtils.createAnnotationArgument("uniqueConstraints", uniqueConstraintAnnotations)); // NOI18N
                }
                
                newClassTree = genUtils.addAnnotation(newClassTree, genUtils.createAnnotation("javax.persistence.Table", tableAnnArgs));

                if (needsPKClass) {
                    String pkFieldName = createFieldName(pkClassName);
                    pkProperty = new Property(
                            Modifier.PROTECTED,
                            Collections.singletonList(genUtils.createAnnotation("javax.persistence.EmbeddedId")),
                            pkFQClassName,
                            pkFieldName);
                    properties.add(pkProperty);
                }

                //TODO: javadoc - generate or fake in test mode
                //        b.setCommentDataAuthor(authorOverride);
                //        b.setCommentDataDate(dateOverride);
            }

            protected void generateMember(EntityMember m) throws IOException {
                String memberName = m.getMemberName();
                boolean isPKMember = m.isPrimaryKey();
                Property property = null;
                if (isPKMember) {
                    if (needsPKClass) {
                        pkClassVariables.add(createVariable(m));
                    } else {
                        pkProperty = property = createProperty(m);
                    }
                    String pkColumnName = (String)dbMappings.getCMPFieldMapping().get(memberName);
                    pkColumnNames.add(pkColumnName);
                } else {
                    property = createProperty(m);
                    if (!m.isNullable()) {
                        nonNullableProps.add(property);
                    }
                }
                // we don't create the property only if the current member is
                // part of a primary key, in which case it will be put in the primary key class
                assert (property != null) || (property == null && isPKMember && needsPKClass);
                if (property != null) {
                    properties.add(property);
                }

                // generate equivalent of finder methods - named query annotations
                if (generateNamedQueries && !m.isLobType()) {
                    List<ExpressionTree> namedQueryAnnArguments = new ArrayList<ExpressionTree>();
                    namedQueryAnnArguments.add(genUtils.createAnnotationArgument("name", entityClassName + ".findBy" + createCapitalizedFieldName(memberName))); //NOI18N

                    if (namedQueryPrefix == null) {
                        char firstLetter = entityClassName.toLowerCase().charAt(0);
                        namedQueryPrefix = "SELECT " + firstLetter + " FROM " + entityClassName + " " + firstLetter + " WHERE " + firstLetter + "."; // NOI18N
                    }
                    // need a prefix of "pk_field_name." if this is part of a composite pk
                    String memberAccessString = ((needsPKClass && isPKMember) ? (pkProperty.getField().getName().toString() + "." + memberName) : memberName); // NOI18N
                    namedQueryAnnArguments.add(genUtils.createAnnotationArgument(
                            "query", namedQueryPrefix + //NOI18N
                            memberAccessString + ((genNamedParams) ? (" = :" + memberName) : "= ?1"))); //NOI18N
                    namedQueryAnnotations.add(genUtils.createAnnotation("javax.persistence.NamedQuery", namedQueryAnnArguments)); //NOI18N
                }
            }
           
            protected void addFindAllNamedQueryAnnotation() {
                // Add NamedQuery findAll here
                List<ExpressionTree> namedQueryAnnArguments = new ArrayList<ExpressionTree>();
                namedQueryAnnArguments.add(genUtils.createAnnotationArgument("name", entityClassName + ".findAll")); // NOI18N

                char firstLetter = entityClassName.toLowerCase().charAt(0);
                String queryString = "SELECT " + firstLetter + " FROM " + entityClassName + " " + firstLetter; // NOI18N
                namedQueryAnnArguments.add(genUtils.createAnnotationArgument("query", queryString)); // NOI18N
                
                // Have the findAll as the first NameQuery
                namedQueryAnnotations.add(0, genUtils.createAnnotation("javax.persistence.NamedQuery", namedQueryAnnArguments)); //NOI18N
            }

            protected void afterMembersGenerated() {
                addFindAllNamedQueryAnnotation();
                
                newClassTree = genUtils.addAnnotation(newClassTree, genUtils.createAnnotation("javax.persistence.NamedQueries", // NOI18N
                        Collections.singletonList(genUtils.createAnnotationArgument(null, namedQueryAnnotations))));
            }

            protected void generateRelationship(RelationshipRole role) throws IOException {
                String memberName = role.getFieldName();

                // XXX getRelationshipFieldType() does not work well when entity classes
                // are not all generated to the same package - fixed in issue 139804
                String typeName = getRelationshipFieldType(role, entityClass.getPackage());
                TypeMirror fieldType = copy.getElements().getTypeElement(typeName).asType();
                if (role.isToMany()) {
                    // Use the collection type the user wants
                    TypeElement collectionTypeElem = copy.getElements().getTypeElement(collectionType.className());
                    fieldType = copy.getTypes().getDeclaredType(collectionTypeElem, fieldType);
                }

                List<AnnotationTree> annotations = new ArrayList<AnnotationTree>();
                List<ExpressionTree> annArguments = new ArrayList<ExpressionTree>();
                if (role.isCascade()) {
                    annArguments.add(genUtils.createAnnotationArgument("cascade", "javax.persistence.CascadeType", "ALL")); // NOI18N
                }
                if (role.equals(role.getParent().getRoleB())) { // Role B
                    annArguments.add(genUtils.createAnnotationArgument("mappedBy", role.getParent().getRoleA().getFieldName())); // NOI18N
                } else {  // Role A
                    if (role.isMany() && role.isToMany()) { // ManyToMany
                        List<ExpressionTree> joinTableAnnArguments = new ArrayList<ExpressionTree>();
                        joinTableAnnArguments.add(genUtils.createAnnotationArgument("name", (String) dbMappings.getJoinTableMapping().get(role.getFieldName()))); //NOI18N

                        CMPMappingModel.JoinTableColumnMapping joinColumnMap = dbMappings.getJoinTableColumnMppings().get(role.getFieldName());

                        List<AnnotationTree> joinCols = new ArrayList<AnnotationTree>();
                        ColumnData[] columns = joinColumnMap.getColumns();
                        ColumnData[] refColumns = joinColumnMap.getReferencedColumns();
                        for(int colIndex = 0; colIndex < columns.length; colIndex++) {
                            List<ExpressionTree> attrs = new ArrayList<ExpressionTree>();
                            attrs.add(genUtils.createAnnotationArgument("name", columns[colIndex].getColumnName())); //NOI18N
                            attrs.add(genUtils.createAnnotationArgument("referencedColumnName", refColumns[colIndex].getColumnName())); //NOI18N
                            if(regenTablesAttrs && !columns[colIndex].isNullable()) {
                                attrs.add(genUtils.createAnnotationArgument("nullable", false)); //NOI18N
                            }
                            joinCols.add(genUtils.createAnnotation("javax.persistence.JoinColumn", attrs)); //NOI18N
                        }
                        joinTableAnnArguments.add(genUtils.createAnnotationArgument("joinColumns", joinCols)); // NOI18N

                        List<AnnotationTree> inverseCols = new ArrayList<AnnotationTree>();
                        ColumnData[] invColumns = joinColumnMap.getInverseColumns();
                        ColumnData[] refInvColumns = joinColumnMap.getReferencedInverseColumns();
                        for(int colIndex = 0; colIndex < invColumns.length; colIndex++) {
                            List<ExpressionTree> attrs = new ArrayList<ExpressionTree>();
                            attrs.add(genUtils.createAnnotationArgument("name", invColumns[colIndex].getColumnName())); //NOI18N
                            attrs.add(genUtils.createAnnotationArgument("referencedColumnName", refInvColumns[colIndex].getColumnName())); //NOI18N
                            if(regenTablesAttrs && !invColumns[colIndex].isNullable()) {
                                attrs.add(genUtils.createAnnotationArgument("nullable", false)); //NOI18N
                            }
                            inverseCols.add(genUtils.createAnnotation("javax.persistence.JoinColumn", attrs)); // NOI18N
                        }
                        joinTableAnnArguments.add(genUtils.createAnnotationArgument("inverseJoinColumns", inverseCols)); // NOI18N

                        annotations.add(genUtils.createAnnotation("javax.persistence.JoinTable", joinTableAnnArguments)); // NOI18N
                    } else { // ManyToOne, OneToMany, OneToOne
                        ColumnData[] columns = (ColumnData[]) dbMappings.getCmrFieldMapping().get(role.getFieldName());
                        CMPMappingModel relatedMappings = beanMap.get(role.getParent().getRoleB().getEntityName()).getCMPMapping();
                        ColumnData[] invColumns = (ColumnData[]) relatedMappings.getCmrFieldMapping().get(role.getParent().getRoleB().getFieldName());
                        if (columns.length == 1) {
                            List<ExpressionTree> attrs = new ArrayList<ExpressionTree>();
                            attrs.add(genUtils.createAnnotationArgument("name", columns[0].getColumnName())); //NOI18N
                            attrs.add(genUtils.createAnnotationArgument("referencedColumnName", invColumns[0].getColumnName())); //NOI18N
                            if(regenTablesAttrs && !columns[0].isNullable()) {
                                attrs.add(genUtils.createAnnotationArgument("nullable", false));
                            }
                            makeReadOnlyIfNecessary(pkColumnNames, columns[0].getColumnName(), attrs);
                            annotations.add(genUtils.createAnnotation("javax.persistence.JoinColumn", attrs)); //NOI18N
                        } else {
                            List<AnnotationTree> joinCols = new ArrayList<AnnotationTree>();
                            for(int colIndex = 0; colIndex < columns.length; colIndex++) {
                                List<ExpressionTree> attrs = new ArrayList<ExpressionTree>();
                                attrs.add(genUtils.createAnnotationArgument("name", columns[colIndex].getColumnName())); //NOI18N
                                attrs.add(genUtils.createAnnotationArgument("referencedColumnName", invColumns[colIndex].getColumnName())); //NOI18N
                                if(regenTablesAttrs && !columns[colIndex].isNullable()) {
                                    attrs.add(genUtils.createAnnotationArgument("nullable", false));
                                }
                                makeReadOnlyIfNecessary(pkColumnNames, columns[colIndex].getColumnName(), attrs);
                                joinCols.add(genUtils.createAnnotation("javax.persistence.JoinColumn", attrs)); // NOI18N
                            }
                            ExpressionTree joinColumnsNameAttrValue = genUtils.createAnnotationArgument(null, joinCols);
                            AnnotationTree joinColumnsAnnotation = genUtils.createAnnotation("javax.persistence.JoinColumns", Collections.singletonList(joinColumnsNameAttrValue)); //NOI18N
                            annotations.add(joinColumnsAnnotation);
                        }
                    }
                }
                String relationAnn;
                if (role.isMany() && role.isToMany()) {
                    relationAnn = "ManyToMany"; //NOI18N
                } else if (role.isMany()) {
                    relationAnn = "ManyToOne"; //NOI18N
                } else if (role.isToMany()) {
                    relationAnn = "OneToMany"; //NOI18N
                } else {
                    relationAnn = "OneToOne";  //NOI18N
                }
                
                if (!role.isToMany()) { // meaning ManyToOne or OneToOne
                    // Add optional=false on @ManyToOne or the owning side of @OneToOne
                    // if the relationship is non-optional (or non-nuallable in other words)  
                    if(!role.isOptional() && (role.isMany() || role.equals(role.getParent().getRoleA())) ) {
                        annArguments.add(genUtils.createAnnotationArgument("optional", false)); // NOI18N
                    }
                } 
                
                //FetchType
                if(fetchType.equals(FetchType.LAZY)) {
                    annArguments.add(genUtils.createAnnotationArgument("fetch", "javax.persistence.FetchType", "LAZY")); // NOI18N
                } else if(fetchType.equals(FetchType.EAGER)) {
                    annArguments.add(genUtils.createAnnotationArgument("fetch", "javax.persistence.FetchType", "EAGER")); // NOI18N
                }
                
                // Create the relationship annotation 
                annotations.add(genUtils.createAnnotation("javax.persistence." + relationAnn, annArguments)); // NOI18N

                properties.add(new Property(Modifier.PRIVATE, annotations, fieldType, memberName));
            }
            
            

            /**
             * Creates the <code>serialVersionUID</code> field with
             * the initial value of <code>1L</code>.
             * 
             * @return the created field.
             */ 
            private VariableTree createSerialVersionUID(){
                Set<Modifier> serialVersionUIDModifiers = new HashSet<Modifier>();
                serialVersionUIDModifiers.add(Modifier.PRIVATE);
                serialVersionUIDModifiers.add(Modifier.STATIC);
                serialVersionUIDModifiers.add(Modifier.FINAL);

                TreeMaker make = copy.getTreeMaker();
                VariableTree serialVersionUID = make.Variable(make.Modifiers(serialVersionUIDModifiers), 
                        "serialVersionUID", genUtils.createType("long", typeElement), make.Literal(Long.valueOf("1"))); //NOI18N
                
                return serialVersionUID;
            }
            
            protected void finish() {
                
                if(pkProperty != null) {
                    // create a constructor which takes the primary key field as argument    
                    VariableTree pkFieldParam = genUtils.removeModifiers(pkProperty.getField());
                    List<VariableTree> pkFieldParams = Collections.singletonList(pkFieldParam);
                    constructors.add(genUtils.createAssignmentConstructor(genUtils.createModifiers(Modifier.PUBLIC), entityClassName, pkFieldParams));

                    // if different than pk fields constructor, add constructor
                    // which takes all non-nullable non-relationship fields as args
                    if (nonNullableProps.size() > 0) {
                        List<VariableTree> nonNullableParams = new ArrayList<VariableTree>(nonNullableProps.size() + 1);
                        nonNullableParams.add(pkFieldParam);
                        for (Property property : nonNullableProps) {
                            nonNullableParams.add(genUtils.removeModifiers(property.getField()));
                        }
                        constructors.add(genUtils.createAssignmentConstructor(genUtils.createModifiers(Modifier.PUBLIC), entityClassName, nonNullableParams));
                    }

                    // create a constructor which takes the fields of the primary key class as arguments
                    if (pkClassVariables.size() > 0) {
                        StringBuilder body = new StringBuilder(30 + 30 * pkClassVariables.size());
                        body.append("{"); // NOI18N
                        body.append("this." + pkProperty.getField().getName() + " = new " + pkClassName + "("); // NOI18N
                        for (Iterator<VariableTree> i = pkClassVariables.iterator(); i.hasNext();) {
                            body.append(i.next().getName());
                            body.append(i.hasNext() ? ", " : ");"); // NOI18N
                        }
                        body.append("}"); // NOI18N
                        TreeMaker make = copy.getTreeMaker();
                        constructors.add(make.Constructor(
                                make.Modifiers(EnumSet.of(Modifier.PUBLIC), Collections.<AnnotationTree>emptyList()),
                                Collections.<TypeParameterTree>emptyList(),
                                pkClassVariables,
                                Collections.<ExpressionTree>emptyList(),
                                body.toString()));
                    }
                

                    // add equals and hashCode methods
                    EntityMethodGenerator methodGenerator = new EntityMethodGenerator(copy, genUtils, typeElement);
                    methods.add(methodGenerator.createHashCodeMethod(pkFieldParams));
                    methods.add(methodGenerator.createEqualsMethod(entityClassName, pkFieldParams));
                    methods.add(methodGenerator.createToStringMethod(entityFQClassName, pkFieldParams));
                }
                
                // add the serialVersionUID field
                fields.add(createSerialVersionUID());
            }

            private String getRelationshipFieldType(RelationshipRole role, String pkg) {
                RelationshipRole rA = role.getParent().getRoleA();
                RelationshipRole rB = role.getParent().getRoleB();
                RelationshipRole otherRole = role.equals(rA) ? rB : rA;
                
                // To address issue 139804
                // First, check if the entity package name is set in the role.
                // If yes, then that's the package
                // If no, then default to the passed in pkg
                if(role.getEntityPkgName() != null) {
                    return otherRole.getEntityPkgName() + "." + otherRole.getEntityName(); // NOI18N
                } else {
                    return pkg.length() == 0 ? otherRole.getEntityName() : pkg + "." + otherRole.getEntityName(); // NOI18N
                }
            }

            private void makeReadOnlyIfNecessary(List<String> pkColumnNames, String testColumnName, List<ExpressionTree> attrs) {
                // if the join column is a pk column, add insertable = false, updatable = false
                if (pkColumnNames.contains(testColumnName)) {
                    attrs.add(genUtils.createAnnotationArgument("insertable", false)); //NOI18N
                    attrs.add(genUtils.createAnnotationArgument("updatable", false)); //NOI18N
                }
            }
        }

        /**
         * An implementation of ClassGenerator which generates primary key
         * classes.
         */
        private final class PKClassGenerator extends ClassGenerator {

            public PKClassGenerator(WorkingCopy copy, EntityClass entityClass) throws IOException {
                super(copy, entityClass);
            }

            protected void initialize() throws IOException {
                newClassTree = genUtils.ensureNoArgConstructor(newClassTree);
                // primary key class must be serializable and @Embeddable
                newClassTree = genUtils.addImplementsClause(newClassTree, "java.io.Serializable"); //NOI18N
                newClassTree = genUtils.addAnnotation(newClassTree, genUtils.createAnnotation("javax.persistence.Embeddable")); // NOI18N
            }

            protected void generateMember(EntityMember m) throws IOException {
                if (!m.isPrimaryKey()) {
                    return;
                }
                Property property = createProperty(m);
                properties.add(property);
            }

            protected void afterMembersGenerated() {
            }

            protected void generateRelationship(RelationshipRole relationship) {
            }

            protected void finish() {
                // add a constructor which takes the fields of the primary key class as arguments
                List<VariableTree> parameters = new ArrayList<VariableTree>(properties.size());
                for (Property property : properties) {
                    parameters.add(genUtils.removeModifiers(property.getField()));
                }
                constructors.add(genUtils.createAssignmentConstructor(genUtils.createModifiers(Modifier.PUBLIC), pkClassName, parameters));

                // add equals and hashCode methods
                EntityMethodGenerator methodGenerator = new EntityMethodGenerator(copy, genUtils, typeElement);
                methods.add(methodGenerator.createHashCodeMethod(parameters));
                methods.add(methodGenerator.createEqualsMethod(pkClassName, parameters));
                methods.add(methodGenerator.createToStringMethod(pkFQClassName, parameters));
            }
        }
    }
}
