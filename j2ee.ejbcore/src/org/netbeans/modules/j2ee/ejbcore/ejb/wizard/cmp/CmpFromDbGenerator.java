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

package org.netbeans.modules.j2ee.ejbcore.ejb.wizard.cmp;

import java.util.Arrays;
import java.util.Collections;
import java.util.Collections;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.dd.api.ejb.DDProvider;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJar;
import org.netbeans.modules.j2ee.ejbcore.EjbGenerationUtil;
import org.netbeans.modules.j2ee.persistence.entitygenerator.EntityClass;
import org.netbeans.modules.j2ee.persistence.entitygenerator.EntityMember;
import org.openide.filesystems.FileObject;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.lang.model.element.Modifier;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.modules.j2ee.common.method.MethodModel;
import org.netbeans.modules.j2ee.common.source.GenerationUtils;
import org.netbeans.modules.j2ee.dd.api.ejb.CmpField;
import org.netbeans.modules.j2ee.dd.api.ejb.CmrField;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbRelation;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbRelationshipRole;
import org.netbeans.modules.j2ee.dd.api.ejb.EnterpriseBeans;
import org.netbeans.modules.j2ee.dd.api.ejb.Entity;
import org.netbeans.modules.j2ee.dd.api.ejb.RelationshipRoleSource;
import org.netbeans.modules.j2ee.dd.api.ejb.Relationships;
import org.netbeans.modules.j2ee.deployment.common.api.OriginalCMPMapping;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.ejbcore.action.CmFieldGenerator;
import org.netbeans.modules.j2ee.ejbcore.action.FinderMethodGenerator;
import org.netbeans.modules.j2ee.ejbcore.api.codegeneration.EntityGenerator;
import org.netbeans.modules.j2ee.ejbcore.api.methodcontroller.EntityMethodController;
import org.netbeans.modules.j2ee.ejbcore.naming.EJBNameOptions;
import org.netbeans.modules.j2ee.ejbcore.spi.ProjectPropertiesSupport;
import org.netbeans.modules.j2ee.persistence.entitygenerator.EntityRelation;
import org.netbeans.modules.j2ee.persistence.entitygenerator.RelationshipRole;
import org.netbeans.modules.j2ee.persistence.wizard.fromdb.RelatedCMPHelper;
import org.netbeans.modules.j2ee.persistence.wizard.fromdb.TableSource;
import org.openide.util.NbBundle;

/**
 *
 * @author Martin Adamek
 */
public class CmpFromDbGenerator {
    
    private static final String FINDER_EXCEPTION_CLASS = "javax.ejb.FinderException"; //NOI18N
    private static final List PRIMITIVE_CLASS_NAMES = Arrays.asList(
            "boolean", "byte", "char", "short", "int", "long", "float", "double"); //NOI18N
    
    private final Project project;
    private final FileObject ddFileObject;
    private final EjbJar dd;
    
    public CmpFromDbGenerator(Project project, FileObject ddFileObject) throws IOException {
        this.project = project;
        this.ddFileObject = ddFileObject;
        this.dd = DDProvider.getDefault().getDDRoot(ddFileObject);
    }
    
    public void generateBeans(RelatedCMPHelper helper, FileObject dbschemaFile, ProgressNotifier progressNotifier) throws IOException {
        
        disableSunCmpMappingsExclusion();
        J2eeModuleProvider pwm = project.getLookup().lookup(J2eeModuleProvider.class);
        pwm.getConfigSupport().ensureConfigurationReady();
        
        if (dd.getEnterpriseBeans()==null) {
            EnterpriseBeans eBeans = dd.newEnterpriseBeans();
            dd.setEnterpriseBeans(eBeans);
        }
        
        int max = 2 * helper.getBeans().length + 4;
        int entityClassIndex = 0;
        progressNotifier.switchToDeterminate(max);
        OriginalCMPMapping[] mappings = new OriginalCMPMapping[helper.getBeans().length];
        
        for (EntityClass entityClass : helper.getBeans()) {
            progressNotifier.progress(NbBundle.getMessage(CmpGenerator.class, "TXT_GeneratingClasses", entityClass.getClassName()));
            entityClassIndex++;
            String pkClassName = null;
            List<EntityMember> primaryKeys = new ArrayList<EntityMember>();
            for (EntityMember entityMember : entityClass.getFields()) {
                if (entityMember.isPrimaryKey()) {
                    pkClassName = entityMember.getMemberType();
                    primaryKeys.add(entityMember);
                }
            }
            if (primaryKeys.size() > 1) {
                String pkFieldName = entityClass.getPkFieldName();
                pkClassName = Character.toUpperCase(pkFieldName.charAt(0)) + pkFieldName.substring(1);
                GenerationUtils.createClass(
                        "Templates/J2EE/EJB21/CmpPrimaryKey.java",
                        entityClass.getPackageFileObject(),
                        pkClassName,
                        null,
                        Collections.singletonMap("seq", "")
                        );
            }
            String wizardTargetName = entityClass.getClassName();
            EntityGenerator generator = EntityGenerator.create(
                    wizardTargetName,
                    entityClass.getPackageFileObject(),
                    false,
                    true,
                    true,
                    pkClassName
                    );
            FileObject ejbClassFileObject = generator.generate();
            
            progressNotifier.progress(2*entityClassIndex+3);
            EJBNameOptions ejbnames = new EJBNameOptions();
            String ejbClassName = ejbnames.getEntityEjbClassPrefix() + wizardTargetName + ejbnames.getEntityEjbClassSuffix();
            Entity e = findEntity(ejbClassFileObject, ejbClassName);
            FinderMethodGenerator finderGenerator = FinderMethodGenerator.create(e, ejbClassFileObject, ddFileObject);
            //            if (helper.isGenerateFinderMethods()) { // is it possible to have CMP with finder method in impl class?
            progressNotifier.progress(NbBundle.getMessage(CmpGenerator.class, "TXT_GeneratingFinderMethods", wizardTargetName));
            addFinderMethods(finderGenerator, e, entityClass.getPackageFileObject(), entityClass, helper.isCmpFieldsInInterface());
            //            }
            
            addCmpFields(e, entityClass);
            addRelationshipFields(e, entityClass);
            populateEntity(entityClass, e, wizardTargetName);
            
            DatabaseConnection dbconn = helper.getDatabaseConnection();
            if(dbconn != null) {
                e.setDescription(dbconn.getName());
            }
            progressNotifier.progress(NbBundle.getMessage(CmpGenerator.class, "TXT_PersistingOriginalMapping", entityClass.getClassName()));
            mappings[entityClassIndex] = new CMPMapping(e.getEjbName(), entityClass.getCMPMapping(), dbschemaFile);
            progressNotifier.progress(2*entityClassIndex+4);
        }
        
        progressNotifier.progress(NbBundle.getMessage(CmpGenerator.class, "TXT_GeneratingRelationships"));
        EntityRelation[] relation = helper.getRelations();
        if (dd.getSingleRelationships() == null && relation.length > 0) {
            dd.setRelationships(dd.newRelationships());
        }
        Relationships rels = dd.getSingleRelationships();
        for (int i = 0; i < relation.length; i++) {
            EjbRelation ejbRel = rels.newEjbRelation();
            populateRelation(ejbRel, relation[i]);
            rels.addEjbRelation(ejbRel);
        }
        progressNotifier.progress(max - 1);
        progressNotifier.progress(NbBundle.getMessage(CmpGenerator.class, "TXT_SavingDeploymentDescriptor"));
        
        //push mapping information
        if (pwm != null) {
            pwm.getConfigSupport().setCMPMappingInfo(mappings);
        }
        dd.write(ddFileObject);
        if (pwm != null) {
            for (EntityClass entityClass : helper.getBeans()) {
                if (helper.getTableSource().getType() == TableSource.Type.DATA_SOURCE) {
                    pwm.getConfigSupport().ensureResourceDefinedForEjb(entityClass.getClassName(), "entity", helper.getTableSource().getName()); //NOI18N
                }
            }
        }
        progressNotifier.progress(max);
    }
    
    private void addFinderMethods(FinderMethodGenerator generator, Entity e, FileObject pkg, EntityClass genData, boolean generateLocal) throws IOException {
        FileObject ejbClassFO = pkg.getFileObject(EjbGenerationUtil.getBaseName(e.getEjbClass()), "java"); // NOI18N
        assert ejbClassFO != null: "interface class "+ e.getLocalHome() + " not found in " + pkg;
        
        Iterator<EntityMember> it = genData.getFields().iterator();
        while (it.hasNext()) {
            EntityMember em = (EntityMember) it.next();
            String type = em.getMemberType();
            if (em.supportsFinder()) { // TODO consider not generating for primary key
                String methodName = "findBy" + EntityMember.makeClassName(em.getMemberName()); // NOI18N
                MethodModel.Variable parameter = MethodModel.Variable.create(em.getMemberType(), em.getMemberName());
                MethodModel methodModel = MethodModel.create(
                        methodName,
                        "java.util.Collection",
                        null,
                        Collections.singletonList(parameter),
                        Collections.singletonList(FINDER_EXCEPTION_CLASS),
                        Collections.<Modifier>emptySet()
                        );
                String ejbql  = MessageFormat.format(
                        "SELECT OBJECT({0}) " +
                        "FROM {1} AS {0} " + // abstract schema name
                        "WHERE {0}.{2} = ?1", // cmp field
                        new Object[] {
                    Character.toLowerCase(e.getAbstractSchemaName().charAt(0)) + "",
                    e.getAbstractSchemaName(),
                    em.getMemberName()
                }
                );
                generator.generate(methodModel, generateLocal, false, false, ejbql);
            }
        }
        
    }
    
//    /**
//     * Determine if <code>className</code> is of a primitive type or not.
//     * @return true if <code>className</code> is of a primitive type
//     */
//    private static boolean isPrimitive(String className) {
//        return PRIMITIVE_CLASS_NAMES.contains(className);
//    }
    
    private void populateEntity(EntityClass bean, Entity e, String wizardTargetName) {
        if (bean.isUsePkField()) {
            e.setPrimkeyField(bean.getPkFieldName());
        }
        e.setAbstractSchemaName(wizardTargetName);
        
        Iterator it = bean.getFields().iterator();
        while (it.hasNext()) {
            EntityMember m = (EntityMember) it.next();
            CmpField f = e.newCmpField();
            f.setFieldName(m.getMemberName());
            e.addCmpField(f);
        }
    }
    
    private void populateRelation(EjbRelation ejbR, EntityRelation r) {
        RelationshipRole roleA = r.getRoleA();
        RelationshipRole roleB = r.getRoleB();
        
        EjbRelationshipRole ejbRoleA = ejbR.newEjbRelationshipRole();
        EjbRelationshipRole ejbRoleB = ejbR.newEjbRelationshipRole();
        
        populateRole(ejbRoleA, roleA);
        populateRole(ejbRoleB, roleB);
        
        ejbR.setEjbRelationName(r.getRelationName());
        ejbR.setEjbRelationshipRole(ejbRoleA);
        ejbR.setEjbRelationshipRole2(ejbRoleB);
    }
    
    private static void populateRole(EjbRelationshipRole ejbR, RelationshipRole role) {
        ejbR.setCascadeDelete(role.isCascade());
        RelationshipRoleSource source = ejbR.newRelationshipRoleSource();
        source.setEjbName(role.getEntityName());
        ejbR.setRelationshipRoleSource(source);
        CmrField f = ejbR.newCmrField();
        f.setCmrFieldName(role.getFieldName());
        if (role.isMany()) {
            ejbR.setMultiplicity(ejbR.MULTIPLICITY_MANY);
        } else {
            ejbR.setMultiplicity(ejbR.MULTIPLICITY_ONE);
        }
        if (role.isToMany()) {
            f.setCmrFieldType(java.util.Collection.class.getName());
        }
        ejbR.setCmrField(f);
        ejbR.setEjbRelationshipRoleName(role.getEntityName());
    }
    
    private String getCmrFieldType(RelationshipRole role, String pkg) {
        if (role.isToMany()) {
            return java.util.Collection.class.getName();
        } else {
            RelationshipRole rA = role.getParent().getRoleA();
            RelationshipRole rB = role.getParent().getRoleB();
            RelationshipRole otherRole = role.equals(rA) ? rB : rA;
            EJBNameOptions ejbNames = new EJBNameOptions();
            String ejbClassName = ejbNames.getEntityEjbClassPrefix() + otherRole.getEntityName() + ejbNames.getEntityEjbClassSuffix();
            Entity entity = (Entity) dd.getEnterpriseBeans().findBeanByName(EnterpriseBeans.ENTITY, Entity.EJB_CLASS, ejbClassName);
            return entity.getLocal();
        }
    }
    
    /**
     * Temporary fix for #53475. By default the sun-cmp-mappings.xml file is
     * excluded from the JAR. It is again included when the user goes through this
     * wizard.
     */
    private void disableSunCmpMappingsExclusion() {
        if (org.netbeans.modules.j2ee.api.ejbjar.EjbJar.getEjbJars(project).length == 0) {
            return;
        }
        ProjectPropertiesSupport ps = project.getLookup().lookup(ProjectPropertiesSupport.class);
        if (ps != null) {
            ps.disableSunCmpMappingExclusion();
        }
    }
    
    private void addCmpFields(Entity entity, EntityClass entityClass) throws IOException {
        EJBNameOptions ejbNames = new EJBNameOptions();
        String className = ejbNames.getEntityEjbClassPrefix() + entityClass.getClassName() + ejbNames.getEntityEjbClassSuffix();
        FileObject ejbClassFO = entityClass.getPackageFileObject().getFileObject(EjbGenerationUtil.getBaseName(className), "java"); // NOI18N
        EntityMethodController emc = (EntityMethodController) EntityMethodController.createFromClass(ejbClassFO, className);
        for (EntityMember m : entityClass.getFields()) {
            emc.addField(
                    MethodModel.Variable.create(m.getMemberType(), m.getMemberName()),
                    ddFileObject,
                    true,
                    true,
                    false,
                    false,
                    null
                    );
        }
    }
    
    /**
     * Doesn't write entry to deployment descriptor
     */
    private void addRelationshipFields(Entity entity, EntityClass entityClass) throws IOException {
        FileObject ejbClassFO = entityClass.getPackageFileObject().getFileObject(EjbGenerationUtil.getBaseName(entity.getEjbClass()), "java"); // NOI18N
        CmFieldGenerator generator = CmFieldGenerator.create(entity, ejbClassFO, ddFileObject);
        for (RelationshipRole role : entityClass.getRoles()) {
            String rv = getCmrFieldType(role, entityClass.getPackage());
            MethodModel.Variable field = MethodModel.Variable.create(rv, role.getFieldName());
            generator.addFieldToClass(field, true, true, false, false);
        }
    }
    
    private static Entity findEntity(FileObject fileObject, String className) throws IOException {
        org.netbeans.modules.j2ee.api.ejbjar.EjbJar ejbModule = org.netbeans.modules.j2ee.api.ejbjar.EjbJar.getEjbJar(fileObject);
        if (ejbModule != null) {
            EjbJar ejbJar = DDProvider.getDefault().getMergedDDRoot(ejbModule.getMetadataUnit());
            if (ejbJar != null) {
                EnterpriseBeans enterpriseBeans = ejbJar.getEnterpriseBeans();
                if (enterpriseBeans != null) {
                    return (Entity) enterpriseBeans.findBeanByName(EnterpriseBeans.ENTITY, Entity.EJB_CLASS, className);
                }
            }
        }
        return null;
    }
    
    public static interface ProgressNotifier {
        
        void switchToDeterminate(int workunits);
        
        void progress(int workunit);
        
        void progress(String message);
        
    }
    
}
