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

import com.sun.source.tree.*;
import java.util.HashMap;
import org.netbeans.api.progress.aggregate.ProgressContributor;
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
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.persistence.util.AbstractTask;
import org.netbeans.modules.j2ee.persistence.util.GenerationUtils;
import org.netbeans.modules.j2ee.persistence.dd.persistence.model_1_0.PersistenceUnit;
import org.netbeans.modules.j2ee.persistence.entitygenerator.CMPMappingModel;
import org.netbeans.modules.j2ee.persistence.entitygenerator.EntityClass;
import org.netbeans.modules.j2ee.persistence.entitygenerator.EntityMember;
import org.netbeans.modules.j2ee.persistence.entitygenerator.RelationshipRole;
import org.netbeans.modules.j2ee.persistence.provider.InvalidPersistenceXmlException;
import org.netbeans.modules.j2ee.persistence.provider.ProviderUtil;
import org.netbeans.modules.j2ee.persistence.unit.PUDataObject;
import org.netbeans.modules.j2ee.persistence.util.JPAClassPathHelper;
import org.netbeans.modules.j2ee.persistence.wizard.Util;
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

        generateBeans(helper.getBeans(), helper.isGenerateFinderMethods(), handle, progressPanel);
    }

    // package private for tests
    void generateBeans(EntityClass[] entityClasses,
            boolean generateNamedQueries, ProgressContributor progressContributor, ProgressPanel panel) throws IOException {

        int progressMax = entityClasses.length * 2;
        progressContributor.start(progressMax);
        result = new Generator(entityClasses, generateNamedQueries, progressContributor, panel).run();
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
                Logger.getLogger(JavaPersistenceGenerator.class.getName()).log(Level.FINE, "Invalid persistence.xml: " + ipx.getPath(), ipx); //NO18N
            }
        }

    }

    public void init(WizardDescriptor wiz) {
        // get the table names for all entities in the project
        // Project project = Templates.getProject(wiz);
        // try {
        //     processEntities(PersistenceUtils.getEntityClasses(project));
        // } catch (IOException e) {
        //     ErrorManager.getDefault().notify(e);
        // }
        // processEntities(PersistenceUtils.getAnnotationEntityClasses(project));
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
        private final Set<FileObject> generatedEntityFOs;
        private final Set<FileObject> generatedFOs;

        public Generator(EntityClass[] entityClasses, boolean generateNamedQueries,
                ProgressContributor progressContributor, ProgressPanel progressPanel) {
            this.entityClasses = entityClasses;
            this.generateNamedQueries = generateNamedQueries;
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
                if (!entityClass.isUsePkField()) {
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
                    javaSource.runModificationTask(new AbstractTask<WorkingCopy>() {
                        public void run(WorkingCopy copy) throws IOException {
                            if (copy.getFileObject().equals(entityClassFO)) {
                                new EntityClassGenerator(copy, entityClass, generateNamedQueries).run();
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

            // the class tree of the class we are generating
            protected ClassTree classTree;

            public ClassGenerator(WorkingCopy copy, EntityClass entityClass) throws IOException {
                this.copy = copy;

                this.entityClass = entityClass;
                dbMappings = entityClass.getCMPMapping();
                needsPKClass = !entityClass.isUsePkField();
                pkClassName = needsPKClass ? createPKClassName(entityClass.getClassName()) : null;
                pkFQClassName = entityClass.getPackage() + "." + pkClassName; // NOI18N


                genUtils = GenerationUtils.newInstance(copy);
                if (genUtils == null) {
                    throw new IllegalStateException("Cannot find a public top-level class named " + entityClass.getClassName() +  // NOI18N
                            " in " + FileUtil.getFileDisplayName(copy.getFileObject())); // NOI18N
                }
                classTree = genUtils.getClassTree();
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
                }

                boolean isLobType = m.isLobType();
                if (isLobType) {
                    annotations.add(genUtils.createAnnotation("javax.persistence.Lob")); // NOI18N
                }

                List<ExpressionTree> columnAnnArguments = new ArrayList();
                String memberName = m.getMemberName();

                String columnName = (String) dbMappings.getCMPFieldMapping().get(memberName);
                columnAnnArguments.add(genUtils.createAnnotationArgument("name", columnName)); //NOI18N
                if (!m.isNullable()) {
                    columnAnnArguments.add(genUtils.createAnnotationArgument("nullable", false)); //NOI18N
                }
                annotations.add(genUtils.createAnnotation("javax.persistence.Column", columnAnnArguments)); //NOI18N

                String temporalType = getMemberTemporalType(m);
                if (temporalType != null) {
                    ExpressionTree temporalAnnValueArgument = genUtils.createAnnotationArgument(null, "javax.persistence.TemporalType", temporalType); //NOI18N
                    annotations.add(genUtils.createAnnotation("javax.persistence.Temporal", Collections.singletonList(temporalAnnValueArgument)));
                }

                return new Property(Modifier.PRIVATE, annotations, getMemberType(m), memberName);
            }

            /**
             * Like {@link #createProperty}, but it only creates a variable
             * with no modififers and no annotations. Useful to pass in
             * a parameter list when creating a method or constructor.
             */
            protected VariableTree createVariable(EntityMember m) {
                return genUtils.createVariable(m.getMemberName(), getMemberType(m));
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

            protected MethodTree createHashCodeMethod(List<VariableTree> fields) {
                StringBuilder body = new StringBuilder(20 + fields.size() * 30);
                body.append("{"); // NOI18N
                body.append("int hash = 0;"); // NOI18N
                for (VariableTree field : fields) {
                    body.append(createHashCodeLineForField(field));
                }
                body.append("return hash;"); // NOI18N
                body.append("}"); // NOI18N
                TreeMaker make = copy.getTreeMaker();
                // XXX Javadoc
                return make.Method(
                        make.Modifiers(EnumSet.of(Modifier.PUBLIC), Collections.singletonList(genUtils.createAnnotation("java.lang.Override"))),
                        "hashCode", // NOI18N
                        make.PrimitiveType(TypeKind.INT),
                        Collections.<TypeParameterTree>emptyList(),
                        Collections.<VariableTree>emptyList(),
                        Collections.<ExpressionTree>emptyList(),
                        body.toString(),
                        null);
            }

            private String createHashCodeLineForField(VariableTree field) {
                Name fieldName = field.getName();
                Tree fieldType = field.getType();
                if (fieldType.getKind() == Tree.Kind.PRIMITIVE_TYPE) {
                    if (((PrimitiveTypeTree)fieldType).getPrimitiveTypeKind() == TypeKind.BOOLEAN) {
                        return "hash += (" + fieldName + " ? 1 : 0"; // NOI18N
                    }
                    return "hash += (int)" + fieldName + ";"; // NOI18N
                }
                return "hash += (" + fieldName + " != null ? " + fieldName + ".hashCode() : 0);"; // NOI18N
            }

            protected MethodTree createEqualsMethod(String simpleClassName, List<VariableTree> fields) {
                StringBuilder body = new StringBuilder(50 + fields.size() * 30);
                body.append("{"); // NOI18N
                body.append("// TODO: Warning - this method won't work in the case the id fields are not set\n"); // NOI18N
                body.append("if (!(object instanceof "); // NOI18N
                body.append(simpleClassName + ")) {return false;}"); // NOI18N
                body.append(simpleClassName + " other = (" + simpleClassName + ")object;"); // NOI18N
                for (VariableTree field : fields) {
                    body.append(createEqualsLineForField(field));
                }
                body.append("return true;"); // NOI18N
                body.append("}"); // NOI18N
                TreeMaker make = copy.getTreeMaker();
                // XXX Javadoc
                return make.Method(
                        make.Modifiers(EnumSet.of(Modifier.PUBLIC), Collections.singletonList(genUtils.createAnnotation("java.lang.Override"))), // NOI18N
                        "equals", // NOI18N
                        make.PrimitiveType(TypeKind.BOOLEAN),
                        Collections.<TypeParameterTree>emptyList(),
                        Collections.singletonList(genUtils.createVariable("object", "java.lang.Object")), // NOI18N
                        Collections.<ExpressionTree>emptyList(),
                        body.toString(),
                        null);
            }

            private String createEqualsLineForField(VariableTree field){
                Name fieldName = field.getName();
                Tree fieldType = field.getType();
                if (fieldType.getKind() == Tree.Kind.PRIMITIVE_TYPE) {
                    return "if (this." + fieldName + " != other." + fieldName + ") return false;"; // NOI18N
                }
                return "if ((this." + fieldName + " == null && other." + fieldName + " != null) || " + // NOI18N
                        "(this." + fieldName + " != null && !this." + fieldName + ".equals(other." + fieldName + ")) return false;"; // NOI18N
            }

            protected MethodTree createToStringMethod(String simpleClassName, List<VariableTree> fields) {
                StringBuilder body = new StringBuilder(30 + fields.size() * 30);
                body.append("{"); // NOI18N
                body.append("return \"" + simpleClassName + "["); // NOI18N
                for (Iterator<VariableTree> i = fields.iterator(); i.hasNext();) {
                    String fieldName = i.next().getName().toString();
                    body.append(fieldName + "=\" + " + fieldName + " + \""); //NOI18N
                    body.append(i.hasNext() ? ", " : "]\";"); //NOI18N
                }
                body.append("}"); // NOI18N
                TreeMaker make = copy.getTreeMaker();
                // XXX Javadoc
                return make.Method(
                        make.Modifiers(EnumSet.of(Modifier.PUBLIC), Collections.singletonList(genUtils.createAnnotation("java.lang.Override"))),
                        "toString", // NOI18N
                        genUtils.createType("java.lang.String"), // NOI18N
                        Collections.<TypeParameterTree>emptyList(),
                        Collections.<VariableTree>emptyList(),
                        Collections.<ExpressionTree>emptyList(),
                        body.toString(),
                        null);
            }

            public void run() throws IOException {
                initialize();
                for (Object object : entityClass.getFields()) {
                    generateMember((EntityMember)object);
                }
                afterMembersGenerated();
                for (Object object : entityClass.getRoles()) {
                    generateRelationship((RelationshipRole)object);
                }
                finish();

                // add the generated members
                TreeMaker make = copy.getTreeMaker();
                int position = 0;
                for (Property property : properties) {
                    classTree = make.insertClassMember(classTree, position, property.getField());
                    position++;
                }
                for (MethodTree constructor : constructors) {
                    classTree = make.addClassMember(classTree, constructor);
                }
                for (Property property : properties) {
                    classTree = make.addClassMember(classTree, property.getGetter());
                    classTree = make.addClassMember(classTree, property.getSetter());
                }
                for (MethodTree method : methods) {
                    classTree = make.addClassMember(classTree, method);
                }
                copy.rewrite(genUtils.getClassTree(), classTree);
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
                    this(modifier, annotations, genUtils.createType(type), name);
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
            /**
             * Specifies whether named queries should be generated.
             */
            private final boolean generateNamedQueries;

            // the property for the primary key (or the primary key class)
            private Property pkProperty;
            // the prefix or all named queries ("select ... ")
            private String namedQueryPrefix;



            public EntityClassGenerator(WorkingCopy copy, EntityClass entityClass, boolean generateNamedQueries) throws IOException {
                super(copy, entityClass);
                this.generateNamedQueries = generateNamedQueries;
                entityClassName = entityClass.getClassName();
                assert genUtils.getTypeElement().getSimpleName().contentEquals(entityClassName);
                entityFQClassName = entityClass.getPackage() + "." + entityClassName;
            }

            protected void initialize() throws IOException {
                classTree = genUtils.ensureNoArgConstructor(classTree);
                if (genSerializableEntities) {
                    classTree = genUtils.addImplementsClause(classTree, "java.io.Serializable"); // NOI18N
                }
                classTree = genUtils.addAnnotation(classTree, genUtils.createAnnotation("javax.persistence.Entity")); // NOI18N
                ExpressionTree tableNameArgument = genUtils.createAnnotationArgument("name", dbMappings.getTableName()); // NOI18N
                classTree = genUtils.addAnnotation(classTree, genUtils.createAnnotation("javax.persistence.Table", Collections.singletonList(tableNameArgument)));

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

            protected void afterMembersGenerated() {
                classTree = genUtils.addAnnotation(classTree, genUtils.createAnnotation("javax.persistence.NamedQueries", // NOI18N
                        Collections.singletonList(genUtils.createAnnotationArgument(null, namedQueryAnnotations))));
            }

            protected void generateRelationship(RelationshipRole role) throws IOException {
                String memberName = role.getFieldName();

                // XXX getRelationshipFieldType() does not work well when entity classes
                // are not all generated to the same package
                String typeName = getRelationshipFieldType(role, entityClass.getPackage());
                TypeMirror fieldType = copy.getElements().getTypeElement(typeName).asType();
                if (role.isToMany()) {
                    // XXX this will probably not resolve imports
                    TypeElement collectionType = copy.getElements().getTypeElement("java.util.Collection"); // NOI18N
                    fieldType = copy.getTypes().getDeclaredType(collectionType, fieldType);
                }

                List<AnnotationTree> annotations = new ArrayList<AnnotationTree>();
                List<ExpressionTree> annArguments = new ArrayList<ExpressionTree>();
                if (role.isCascade()) {
                    annArguments.add(genUtils.createAnnotationArgument("cascade", "javax.persistence.CascadeType", "ALL")); // NOI18N
                }
                if (role.equals(role.getParent().getRoleB())) {
                    annArguments.add(genUtils.createAnnotationArgument("mappedBy", role.getParent().getRoleA().getFieldName())); // NOI18N
                } else {
                    if (role.isMany() && role.isToMany()) {
                        List<ExpressionTree> joinTableAnnArguments = new ArrayList<ExpressionTree>();
                        joinTableAnnArguments.add(genUtils.createAnnotationArgument("name", (String) dbMappings.getJoinTableMapping().get(role.getFieldName()))); //NOI18N

                        CMPMappingModel.JoinTableColumnMapping joinColumnMap = dbMappings.getJoinTableColumnMppings().get(role.getFieldName());

                        List<AnnotationTree> joinCols = new ArrayList<AnnotationTree>();
                        String[] colNames = joinColumnMap.getColumns();
                        String[] refColNames = joinColumnMap.getReferencedColumns();
                        for(int colIndex = 0; colIndex < colNames.length; colIndex++) {
                            List<ExpressionTree> attrs = new ArrayList<ExpressionTree>();
                            attrs.add(genUtils.createAnnotationArgument("name", colNames[colIndex])); //NOI18N
                            attrs.add(genUtils.createAnnotationArgument("referencedColumnName", refColNames[colIndex])); //NOI18N
                            joinCols.add(genUtils.createAnnotation("javax.persistence.JoinColumn", attrs)); //NOI18N
                        }
                        joinTableAnnArguments.add(genUtils.createAnnotationArgument("joinColumns", joinCols)); // NOI18N

                        List<AnnotationTree> inverseCols = new ArrayList<AnnotationTree>();
                        String[] invColNames = joinColumnMap.getInverseColumns();
                        String[] refInvColNames = joinColumnMap.getReferencedInverseColumns();
                        for(int colIndex = 0; colIndex < invColNames.length; colIndex++) {
                            List<ExpressionTree> attrs = new ArrayList<ExpressionTree>();
                            attrs.add(genUtils.createAnnotationArgument("name", invColNames[colIndex])); //NOI18N
                            attrs.add(genUtils.createAnnotationArgument("referencedColumnName", refInvColNames[colIndex])); //NOI18N
                            inverseCols.add(genUtils.createAnnotation("javax.persistence.JoinColumn", attrs)); // NOI18N
                        }
                        joinTableAnnArguments.add(genUtils.createAnnotationArgument("inverseJoinColumns", inverseCols)); // NOI18N

                        annotations.add(genUtils.createAnnotation("javax.persistence.JoinTable", joinTableAnnArguments)); // NOI18N
                    } else {
                        String[] colNames = (String[]) dbMappings.getCmrFieldMapping().get(role.getFieldName());
                        CMPMappingModel relatedMappings = beanMap.get(role.getParent().getRoleB().getEntityName()).getCMPMapping();
                        String[] invColNames = (String[]) relatedMappings.getCmrFieldMapping().get(role.getParent().getRoleB().getFieldName());
                        if (colNames.length == 1) {
                            List<ExpressionTree> attrs = new ArrayList<ExpressionTree>();
                            attrs.add(genUtils.createAnnotationArgument("name", colNames[0])); //NOI18N
                            attrs.add(genUtils.createAnnotationArgument("referencedColumnName", invColNames[0])); //NOI18N
                            makeReadOnlyIfNecessary(pkColumnNames, colNames[0], attrs);
                            annotations.add(genUtils.createAnnotation("javax.persistence.JoinColumn", attrs)); //NOI18N
                        } else {
                            List<AnnotationTree> joinCols = new ArrayList<AnnotationTree>();
                            for(int colIndex = 0; colIndex < colNames.length; colIndex++) {
                                List<ExpressionTree> attrs = new ArrayList<ExpressionTree>();
                                attrs.add(genUtils.createAnnotationArgument("name", colNames[colIndex])); //NOI18N
                                attrs.add(genUtils.createAnnotationArgument("referencedColumnName", invColNames[colIndex])); //NOI18N
                                makeReadOnlyIfNecessary(pkColumnNames, colNames[colIndex], attrs);
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
                annotations.add(genUtils.createAnnotation("javax.persistence." + relationAnn, annArguments)); // NOI18N

                properties.add(new Property(Modifier.PRIVATE, annotations, fieldType, memberName));
            }

            protected void finish() {
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
                methods.add(createHashCodeMethod(pkFieldParams));
                methods.add(createEqualsMethod(entityClassName, pkFieldParams));
                methods.add(createToStringMethod(entityFQClassName, pkFieldParams));
            }

            private String getRelationshipFieldType(RelationshipRole role, String pkg) {
                RelationshipRole rA = role.getParent().getRoleA();
                RelationshipRole rB = role.getParent().getRoleB();
                RelationshipRole otherRole = role.equals(rA) ? rB : rA;                
                return pkg.length() == 0 ? otherRole.getEntityName() : pkg + "." + otherRole.getEntityName(); // NOI18N
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
                classTree = genUtils.ensureNoArgConstructor(classTree);
                // primary key class must be serializable and @Embeddable
                classTree = genUtils.addImplementsClause(classTree, "java.io.Serializable"); //NOI18N
                classTree = genUtils.addAnnotation(classTree, genUtils.createAnnotation("javax.persistence.Embeddable")); // NOI18N
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
                methods.add(createHashCodeMethod(parameters));
                methods.add(createEqualsMethod(pkClassName, parameters));
                methods.add(createToStringMethod(pkFQClassName, parameters));
            }
        }
    }
}
