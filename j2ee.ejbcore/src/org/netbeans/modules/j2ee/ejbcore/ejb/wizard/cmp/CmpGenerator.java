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

import java.beans.PropertyChangeListener;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException;
import org.netbeans.modules.j2ee.dd.api.ejb.AssemblyDescriptor;
import org.netbeans.modules.j2ee.dd.api.ejb.CmpField;
import org.netbeans.modules.j2ee.dd.api.ejb.CmrField;
import org.netbeans.modules.j2ee.dd.api.ejb.ContainerTransaction;
import org.netbeans.modules.j2ee.dd.api.ejb.DDProvider;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJar;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbRelation;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbRelationshipRole;
import org.netbeans.modules.j2ee.dd.api.ejb.EnterpriseBeans;
import org.netbeans.modules.j2ee.dd.api.ejb.Entity;
import org.netbeans.modules.j2ee.dd.api.ejb.MethodParams;
import org.netbeans.modules.j2ee.dd.api.ejb.Query;
import org.netbeans.modules.j2ee.dd.api.ejb.QueryMethod;
import org.netbeans.modules.j2ee.dd.api.ejb.RelationshipRoleSource;
import org.netbeans.modules.j2ee.dd.api.ejb.Relationships;
import org.netbeans.modules.j2ee.deployment.common.api.OriginalCMPMapping;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.ejbcore.api.codegeneration.EjbGenerationUtil;
import org.netbeans.modules.j2ee.ejbcore.ejb.wizard.cmp.CMPMapping;
import org.netbeans.modules.j2ee.ejbcore.spi.ProjectPropertiesSupport;
import org.netbeans.modules.j2ee.ejbcore.ejb.wizard.gen.Bean;
import org.netbeans.modules.j2ee.ejbcore.ejb.wizard.gen.Method;
import org.netbeans.modules.j2ee.ejbcore.ejb.wizard.gen.Methods;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import org.openide.loaders.TemplateWizard;
import org.openide.util.NbBundle;

/**
 *
 * @author Pavel Buzek
 */
public class CmpGenerator /*implements PersistenceGenerator */{
    
    private static final String KEY_TEMPLATE = EjbGenerationUtil.TEMPLATE_BASE+"Key.xml"; //NOI18N
    private static final String CMP_TEMPLATE = EjbGenerationUtil.TEMPLATE_BASE+"CMPBean.xml"; //NOI18N
//    private static final Identifier FINDER_EXCEPTION = Identifier.create("javax.ejb.FinderException"); //NOI18N
    private static final String FINDER_EXCEPTION_CLASS = "javax.ejb.FinderException"; //NOI18N
    private static final List PRIMITIVE_CLASS_NAMES = Arrays.asList(
            "boolean", "byte", "char", "short", "int", "long", "float", "double"); //NOI18N
     
    private HashMap genDataCache = new HashMap(13);
    private Project project;
    private org.netbeans.modules.j2ee.api.ejbjar.EjbJar ejbModule;
    private EjbGenerationUtil genHelper;
    private EjbJar dd;
    
    public CmpGenerator() {
        genHelper = new EjbGenerationUtil();
    }
    
    private String generateLocal(String pkg, FileObject foPkg, String className, String ejbName) throws IOException {
        className = EjbGenerationUtil.getBaseName(className);
        Methods genData = new Methods();
        genData.setClassname(className);
        genData.setEjbName(ejbName);
        genData.setPackage(pkg);
        genData.setGenInterface("local");
        return genHelper.genClass(EjbGenerationUtil.INTERFACE_TEMPLATE, foPkg, genData, false);
    }
    
    private String generateLocalHome(String pkg, FileObject foPkg, String className, String local, String ejbName, String keyClass) throws IOException {
        className = EjbGenerationUtil.getBaseName(className);
        Methods genData = new Methods();
        genData.setClassname(className);
        genData.setEjbName(ejbName);
        genData.setPackage(pkg);
        genData.setGenInterface("local-home");
        Method findByPrimaryKey = new Method();
        findByPrimaryKey.setName("findByPrimaryKey");
        findByPrimaryKey.addException(true);
        findByPrimaryKey.addExceptionType("javax.ejb.FinderException");
        findByPrimaryKey.addParam(true);
        findByPrimaryKey.addParamName("key");
        findByPrimaryKey.addParamType(keyClass);
        findByPrimaryKey.setLocalReturn(local);
        genData.addMethod(findByPrimaryKey);
        
        return genHelper.genClass(EjbGenerationUtil.INTERFACE_TEMPLATE, foPkg, genData, false);
    }

    //TODO: RETOUCHE
    
//    private static void addFinderMethods(Entity e, FileObject pkg, EntityClass genData) throws IOException {
//        FileObject intfFo = pkg.getFileObject(EjbGenerationUtil.getBaseName(e.getLocalHome()), "java"); // NOI18N
//        assert intfFo != null: "interface class "+e.getLocalHome()+" not found in "+pkg;
//        
//        Resource res = JavaModel.getResource(intfFo);
//        List/*<JavaClass>*/ classes = res.getClassifiers();
//        assert classes.size() == 1: "" + res + " should contain just one class";
//        JavaClass infi = (JavaClass)classes.get(0);
//        
//        JavaMetamodel.getDefaultRepository().beginTrans(true);
//        boolean err = true;
//        try {
//            JavaMetamodel.getManager().setClassPath(intfFo);
//            JavaModelPackage jmp = (JavaModelPackage)res.refImmediatePackage();
//            MethodClass methodClass = jmp.getMethod();
//            ParameterClass paramClass = jmp.getParameter();
//            TypeReference typeName = jmp.getMultipartId().createMultipartId("java.util.Collection", null, null); // NOI18N
//            
//            List/*<Feature>*/ infFeatures = infi.getFeatures();
//            Iterator/*<EntityMember>*/ it = genData.getFields().iterator();
//            while (it.hasNext()) {
//                EntityMember em = (EntityMember) it.next();
//                String type = em.getMemberType();
//                if (em.supportsFinder()) { // TODO consider not generating for primary key
//                    String methodName = "findBy"+EntityMember.makeClassName(em.getMemberName()); // NOI18N
//                    Parameter param = paramClass.createParameter(em.getMemberName(),
//                            null, // annotations
//                            false, // isFinal
//                            jmp.getMultipartId().createMultipartId(type, null, null), // type name
//                            0, // dim count
//                            false); // varargs
//                    List/*<Parameter>*/ params = new ArrayList();
//                    params.add(param);
//                    List/*<TypeReference>*/ excNames = new ArrayList();
//                    excNames.add(jmp.getMultipartId().createMultipartId(FINDER_EXCEPTION_CLASS, null, null));
//                    infFeatures.add(methodClass.createMethod(methodName,
//                            null, // annotations
//                            0, // TODO modifiers
//                            "", //null, // TODO can add javadoc text
//                            null, // javadoc
//                            null, // body
//                            null, // body text
//                            null, // type parameters
//                            params, // parameters
//                            excNames, // exc names
//                            (TypeReference)typeName.duplicate(), // type name
//                            0 // dimCount
//                            ));
//                    
//                    // generate query method
//                    Query q = e.newQuery();
//                    QueryMethod qm = q.newQueryMethod();
//                    qm.setMethodName(methodName);
//                    MethodParams mp = qm.newMethodParams();
//                    mp.addMethodParam(type);
//                    qm.setMethodParams(mp);
//                    q.setQueryMethod(qm);
//                    q.setDescription("auto generated method");
//                    String tableVarName =
//                            Character.toLowerCase(
//                            e.getAbstractSchemaName().charAt(0))+"";
//                    
//                    String ejbql = "SELECT OBJECT({0}) " +
//                            "FROM {1} AS {0} " + // abstract schema name
//                            "WHERE {0}.{2} = ?1"; // cmp field
//                    q.setEjbQl(MessageFormat.format(ejbql,
//                            new Object[] {
//                        tableVarName,
//                                e.getAbstractSchemaName(),
//                                em.getMemberName()
//                    }
//                    ));
//                    e.addQuery(q);
//                }
//            }
//            err = false;
//        } finally {
//            JavaMetamodel.getDefaultRepository().endTrans(err);
//        }
//        
//        /*
//        SaveCookie sc = (SaveCookie) localHomeCls.getCookie(SaveCookie.class);
//        if (sc != null) {
//            sc.save();
//        }
//         */
//    }
//    
//    /** Adds all getter and setter methods from <code>beanClassName</code>
//     * into <code>businessMethodInterface</code> interface
//     */
//    private static void addFieldsToComponentInterface(FileObject pkg, String beanClassName, String businessMethodInterface, List pkFields) throws IOException {
//        Set pkFieldsSet = new HashSet(pkFields.size());
//        for (int i = 0; i < pkFields.size(); i++) {
//            pkFieldsSet.add(pkFields.get(i));
//        }
//        FileObject beanFo = pkg.getFileObject(EjbGenerationUtil.getBaseName(beanClassName), "java");
//        assert beanFo != null: "bean class "+beanClassName+" not found in "+pkg;
//        FileObject intfFo = pkg.getFileObject(EjbGenerationUtil.getBaseName(businessMethodInterface), "java"); // NOI18N
//        assert intfFo != null: "interface class "+businessMethodInterface+" not found in "+pkg;
//        
//        Resource res = JavaModel.getResource(beanFo);
//        List classes = res.getClassifiers();
//        assert classes.size() == 1: "" + res + " should contain just one class";
//        JavaClass beani = (JavaClass)classes.get(0);
//        res = JavaModel.getResource(intfFo);
//        classes = res.getClassifiers();
//        assert classes.size() == 1: "" + res + " should contain just one class";
//        JavaClass infi = (JavaClass)classes.get(0);
//        
//        JavaMetamodel.getDefaultRepository().beginTrans(true);
//        boolean err = true;
//        try {
//            JavaMetamodel.getManager().setClassPath(beanFo);
//            List/*<Feature>*/ features = beani.getFeatures();
//            List/*<Feature>*/ infFeatures = infi.getFeatures();
//            Iterator fIter = features.iterator();
//            while(fIter.hasNext()) {
//                Feature f = (Feature)fIter.next();
//                String name = f.getName();
//                String fieldName = name.substring(3);
//                fieldName = fieldName.substring(0, 1).toLowerCase() + fieldName.substring(1);
//                if ((f instanceof org.netbeans.jmi.javamodel.Method) &&
//                        (name.startsWith("get") || (name.startsWith("set") && !pkFieldsSet.contains(fieldName))) &&  // NOI18N
//                        !name.equals("setEntityContext")) { // NOI18N
//                    org.netbeans.jmi.javamodel.Method m = (org.netbeans.jmi.javamodel.Method)f;
//                    org.netbeans.jmi.javamodel.Method newM = JMIUtils.duplicate(m);
//                    infFeatures.add(newM);
//                }
//            }
//            err = false;
//        } finally {
//            JavaMetamodel.getDefaultRepository().endTrans(err);
//        }
//        
//        /*
//        SaveCookie sc = (SaveCookie) compIntf.getCookie(SaveCookie.class);
//        if (sc != null) {
//            sc.save();
//        }
//         */
//    }
//
//   /**
//     * Determine if <code>className</code> is of a primitive type or not.
//     * @return true if <code>className</code> is of a primitive type
//     */
//    private static boolean isPrimitive(String className) {
//        return PRIMITIVE_CLASS_NAMES.contains(className);
//    }
//
//    /** Adds all create methods from <code>beanClass</code>
//     * into <code>homeClass</code> interface to make them available for clients.
//     */
//    private static void propagateCreateMethod(FileObject pkg, String beanClass, String homeClass, String localClass) throws IOException {
//        FileObject beanFo = pkg.getFileObject(EjbGenerationUtil.getBaseName(beanClass), "java");
//        assert beanFo != null: "bean class "+beanClass+" not found in "+pkg;
//        FileObject homeFo = pkg.getFileObject(EjbGenerationUtil.getBaseName(homeClass), "java"); // NOI18N
//        assert homeFo != null: "interface class "+homeClass+" not found in "+pkg;
//        
//        Resource res = JavaModel.getResource(beanFo);
//        List classes = res.getClassifiers();
//        assert classes.size() == 1: "" + res + " should contain just one class";
//        JavaClass beani = (JavaClass)classes.get(0);
//        res = JavaModel.getResource(homeFo);
//        classes = res.getClassifiers();
//        assert classes.size() == 1: "" + res + " should contain just one class";
//        JavaClass homei = (JavaClass)classes.get(0);
//        
//        JavaMetamodel.getDefaultRepository().beginTrans(true);
//        boolean err = true;
//        try {
//            JavaMetamodel.getManager().setClassPath(beanFo);
//            JavaModelPackage jmp = (JavaModelPackage)res.refImmediatePackage();
//            MethodClass methodClass = jmp.getMethod();
//            
//            List/*<Feature>*/ features = beani.getFeatures();
//            List/*<Feature>*/ homeFeatures = homei.getFeatures();
//            Iterator fIter = features.iterator();
//            while(fIter.hasNext()) {
//                Feature f = (Feature)fIter.next();
//                String name = f.getName();
//                if ((f instanceof org.netbeans.jmi.javamodel.Method) &&
//                        "ejbCreate".equals(name)) {
//                    org.netbeans.jmi.javamodel.Method m = (org.netbeans.jmi.javamodel.Method)f;
//                    org.netbeans.jmi.javamodel.Method newM = JMIUtils.duplicate(m);
//                    newM.setName("create");
//                    newM.setTypeName(jmp.getMultipartId().createMultipartId(localClass, null, null));
//                    homeFeatures.add(newM);
//                }
//            }
//            err = false;
//        } finally {
//            JavaMetamodel.getDefaultRepository().endTrans(err);
//        }
//        
//        /*
//            SaveCookie sc = (SaveCookie) homeCls.getCookie(SaveCookie.class);
//            if (sc != null) {
//                sc.save();
//            }
//         */
//    }
//    
//    /** Builds and retuns a list of primary keys of a bean.
//     */
//    private static List getPkFields(EntityClass bean) {
//        List fields = bean.getFields();
//        List l = new LinkedList();
//        Iterator allFields = fields.iterator();
//        while (allFields.hasNext()) {
//            EntityMember field = (EntityMember) allFields.next();
//            if(field.isPrimaryKey()) {
//                l.add(field.getMemberName());
//            }
//        }
//        return l;
//    }
//    
//    private void populateEntity(EntityClass bean, Entity e) {
//        Bean genData = getGenData(bean);
//        e.setEjbName(genData.getCommentDataEjbName());
//        String ejbNameBase = EjbGenerationUtil.getEjbNameBase(e.getEjbName());
//        e.setDisplayName(ejbNameBase+"EB");
//        e.setEjbClass(genHelper.getFullClassName(genData.getClassnamePackage(), 
//                         genData.getClassnameName()));
//        
//        e.setLocal(genHelper.getLocalName(genData.getClassnamePackage(), ejbNameBase));
//        e.setLocalHome(genHelper.getLocalHomeName(genData.getClassnamePackage(), ejbNameBase));
//        e.setPrimKeyClass(genData.getKeyFullname()); 
//        e.setReentrant(false);
//        // TODO constants
//        e.setPersistenceType("Container"); 
//        if (bean.isUsePkField()) {
//            e.setPrimkeyField(bean.getPkFieldName());
//        }
//        e.setAbstractSchemaName(ejbNameBase);
//        
//        Iterator it = bean.getFields().iterator();
//        while (it.hasNext()) {
//            EntityMember m = (EntityMember) it.next();
//            CmpField f = e.newCmpField();
//            f.setFieldName(m.getMemberName());
//            e.addCmpField(f);
//        }
//    }
//    
//    private void populateRelation(EjbRelation ejbR, EntityRelation r) {
//        RelationshipRole roleA = r.getRoleA();
//        RelationshipRole roleB = r.getRoleB();
//        
//        EjbRelationshipRole ejbRoleA = ejbR.newEjbRelationshipRole();
//        EjbRelationshipRole ejbRoleB = ejbR.newEjbRelationshipRole();
//        
//        populateRole(ejbRoleA, roleA);
//        populateRole(ejbRoleB, roleB);
//        
//        ejbR.setEjbRelationName(r.getRelationName());
//        ejbR.setEjbRelationshipRole(ejbRoleA);
//        ejbR.setEjbRelationshipRole2(ejbRoleB);
//    }
//    
//    private static void populateRole(EjbRelationshipRole ejbR, RelationshipRole role) {
//        ejbR.setCascadeDelete(role.isCascade());
//        RelationshipRoleSource source = ejbR.newRelationshipRoleSource();
//        source.setEjbName(role.getEntityName());
//        ejbR.setRelationshipRoleSource(source);
//        CmrField f = ejbR.newCmrField();
//        f.setCmrFieldName(role.getFieldName());
//        if (role.isMany()) {
//            ejbR.setMultiplicity(ejbR.MULTIPLICITY_MANY);
//        } else {
//            ejbR.setMultiplicity(ejbR.MULTIPLICITY_ONE);
//        }
//        if (role.isToMany()) {
//            f.setCmrFieldType(java.util.Collection.class.getName());
//        }
//        ejbR.setCmrField(f);
//        ejbR.setEjbRelationshipRoleName(role.getEntityName());
//    }
//    
//    private String getCmrFieldType(RelationshipRole role, String pkg) {
//        if (role.isToMany()) {
//            return java.util.Collection.class.getName();
//        } else {
//            RelationshipRole rA = role.getParent().getRoleA();
//            RelationshipRole rB = role.getParent().getRoleB();
//            RelationshipRole otherRole = role.equals(rA) ? rB : rA;
//            return genHelper.getLocalName(pkg, otherRole.getEntityName());
//        }
//    }
//    
//    /**
//     * Temporary fix for #53475. By default the sun-cmp-mappings.xml file is
//     * excluded from the JAR. It is again included when the user goes through this
//     * wizard.
//     */
//    private void disableSunCmpMappingsExclusion() {
//        if (org.netbeans.modules.j2ee.api.ejbjar.EjbJar.getEjbJars(project).length == 0) {
//            return;
//        }
//        ProjectPropertiesSupport ps = (ProjectPropertiesSupport) project.getLookup().lookup(ProjectPropertiesSupport.class);
//        if (ps != null) {
//            ps.disableSunCmpMappingExclusion();
//        }
//    }
//    
//    public void generateBeans(final ProgressPanel progressPanel,
//            final RelatedCMPHelper helper,
//            final FileObject dbschemaFile,
//            final ProgressHandle handle,
//            boolean justTesting) throws IOException {
//
//        disableSunCmpMappingsExclusion();
//        J2eeModuleProvider pwm = (J2eeModuleProvider) project.getLookup ().lookup (J2eeModuleProvider.class);
//        pwm.getConfigSupport().ensureConfigurationReady();
//        
//        if (dd.getEnterpriseBeans()==null) {
//            EnterpriseBeans eBeans = dd.newEnterpriseBeans();
//            dd.setEnterpriseBeans(eBeans);
//        }
//        
//        EntityClass[] genBeans = helper.getBeans();
//        int max = 2*genBeans.length+4;
//        handle.switchToDeterminate(max);
//        String pkgName = helper.getPackageName();
//        OriginalCMPMapping[] mappings = new OriginalCMPMapping[genBeans.length];
//        for (int i = 0; i < genBeans.length; i++) {
//            handle.progress(NbBundle.getMessage(CmpGenerator.class, "TXT_GeneratingClasses", genBeans[i].getClassName()));
//            progressPanel.setText(NbBundle.getMessage(CmpGenerator.class, "TXT_GeneratingClasses", genBeans[i].getClassName()));
//            FileObject pkg = genBeans[i].getPackageFileObject();
//            Bean keyGen = getKeyGenData(genBeans[i]);
//            Bean beanGen = justTesting ? getGenData(genBeans[i], "Kdysi", "Kdosi") : getGenData(genBeans[i]); //NOI18N
//            String keyName = beanGen.getKeyFullname();
//            if (keyGen != null) {
//                keyName = genHelper.generateBeanClass(KEY_TEMPLATE, keyGen, pkgName, pkg, false);
//            }
//            
//            beanGen.setKeyFullname(keyName);
//            String ejbName = genBeans[i].getClassName();
//            String ejbNameBase = EjbGenerationUtil.getEjbNameBase(ejbName);
//            String beanClassName = genHelper.generateBeanClass(CMP_TEMPLATE, beanGen, pkgName, pkg, false);
//            String localName = EjbGenerationUtil.getFullClassName(pkgName, generateLocal(pkgName, pkg,
//                    EjbGenerationUtil.getLocalName(pkgName, ejbNameBase), ejbNameBase));
//            String localHomeName =
//                    EjbGenerationUtil.getFullClassName(pkgName, generateLocalHome(pkgName, pkg,
//                    EjbGenerationUtil.getLocalHomeName(helper.getPackageName(), ejbNameBase),
//                    localName, ejbNameBase, keyName));
//            String businessInterfaceName = EjbGenerationUtil.getLocalBusinessInterfaceName(pkgName, ejbNameBase);
//            genHelper.generateBusinessInterfaces(pkgName, pkg, businessInterfaceName, ejbNameBase, beanClassName, localName);
//            propagateCreateMethod(pkg, beanClassName, localHomeName, localName);
//            handle.progress(2*i+3);
//            if (helper.isCmpFieldsInInterface()) {
//                handle.progress(NbBundle.getMessage(CmpGenerator.class, "TXT_AddingFieldsToInteface", ejbNameBase));
//                progressPanel.setText(NbBundle.getMessage(CmpGenerator.class, "TXT_AddingFieldsToInteface", ejbNameBase));
//                List pkFields = getPkFields(genBeans[i]);
//                addFieldsToComponentInterface(pkg, beanClassName, businessInterfaceName, pkFields);
//            }
//            Entity e = dd.getEnterpriseBeans().newEntity();
//            populateEntity(genBeans[i], e);
//            e.setEjbClass(beanClassName);
//            e.setLocal(localName);
//            e.setLocalHome(localHomeName);
//            if (helper.isGenerateFinderMethods()) {
//                handle.progress(NbBundle.getMessage(CmpGenerator.class, "TXT_GeneratingFinderMethods", ejbNameBase));
//                progressPanel.setText(NbBundle.getMessage(CmpGenerator.class, "TXT_GeneratingFinderMethods", ejbNameBase));
//                addFinderMethods(e, pkg, genBeans[i]);
//            }
//            DatabaseConnection dbconn = helper.getDatabaseConnection();
//            if(dbconn != null) {
//                e.setDescription(dbconn.getName());
//            }
//            dd.getEnterpriseBeans().addEntity(e);
//            AssemblyDescriptor ad = dd.getSingleAssemblyDescriptor();
//            if (ad == null) {
//                ad = dd.newAssemblyDescriptor();
//                dd.setAssemblyDescriptor(ad);
//            }
//            ContainerTransaction ct = ad.newContainerTransaction();
//            ct.setTransAttribute("Required"); //NOI18N
//            org.netbeans.modules.j2ee.dd.api.ejb.Method m = ct.newMethod();
//            m.setEjbName(ejbName);
//            m.setMethodName("*"); //NOI18N
//            ct.addMethod(m);
//            ad.addContainerTransaction(ct);
//            
//            JMIUtils.saveClass(beanClassName, pkg);
//            JMIUtils.saveClass(localName, pkg);
//            JMIUtils.saveClass(localHomeName, pkg);
//            JMIUtils.saveClass(businessInterfaceName, pkg);
//            
//            handle.progress(NbBundle.getMessage(CmpGenerator.class, "TXT_PersistingOriginalMapping", genBeans[i].getClassName()));
//            progressPanel.setText(NbBundle.getMessage(CmpGenerator.class, "TXT_PersistingOriginalMapping", genBeans[i].getClassName()));
//            mappings[i] = new CMPMapping(ejbName, genBeans[i].getCMPMapping(), dbschemaFile);
//            
//            handle.progress(2*i+4);
//        }
//        
//        handle.progress(NbBundle.getMessage(CmpGenerator.class, "TXT_GeneratingRelationships"));
//        progressPanel.setText(NbBundle.getMessage(CmpGenerator.class, "TXT_GeneratingRelationships"));
//        EntityRelation[] relation = helper.getRelations();
//        if (dd.getSingleRelationships() == null && relation.length > 0) {
//            dd.setRelationships(dd.newRelationships());
//        }
//        Relationships rels = dd.getSingleRelationships();
//        for (int i = 0; i < relation.length; i++) {
//            EjbRelation ejbRel = rels.newEjbRelation();
//            populateRelation(ejbRel, relation[i]);
//            rels.addEjbRelation(ejbRel);
//        }
//        handle.progress(max - 1);
//        handle.progress(NbBundle.getMessage(CmpGenerator.class, "TXT_SavingDeploymentDescriptor"));
//        progressPanel.setText(NbBundle.getMessage(CmpGenerator.class, "TXT_SavingDeploymentDescriptor"));
//        
//        //push mapping information
//        if (pwm != null) {
//            pwm.getConfigSupport().setCMPMappingInfo(mappings);
//        }
//        dd.write(ejbModule.getDeploymentDescriptor());
//        if (pwm != null) {
//            for (int i = 0; i < genBeans.length; i++){
//                if (helper.getTableSource().getType() == TableSource.Type.DATA_SOURCE) {
//                    pwm.getConfigSupport().ensureResourceDefinedForEjb(genBeans[i].getClassName(), "entity", helper.getTableSource().getName()); //NOI18N
//                }
//            }
//        }
//        
//        transactionHelper.write();
//        handle.progress(max);
//    }
//    
//    
//    /**
//     * This method is used for testing to allow file differences. 
//     */
//    private Bean getGenData(EntityClass entity, String dateOverride, String authorOverride) {
//        Bean b = getGenData(entity);
//        b.setCommentDataAuthor(authorOverride);
//        b.setCommentDataDate(dateOverride);
//        return b;
//    }
//    
//    private Bean getGenData(EntityClass entity) {
//        Bean genData = (Bean) genDataCache.get(entity);
//        if (genData != null) {
//            return genData;
//        }
//        genData = genHelper.getDefaultBean();
//        genData.setCommentDataEjbName(entity.getClassName());
//        genData.setClassname(true);
//        genData.setClassnameName(genHelper.getBeanClassName(EjbGenerationUtil.getEjbNameBase(entity.getClassName()))); //NOI18N
//        if (entity.getPackage()!=null) {
//            genData.setClassnamePackage(entity.getPackage());
//        }
//        addCmpFields(entity, genData);
//        addRelationshipFields(entity, genData);
//        return genData;
//    }
//    
//    /**
//     * @return Bean for key or null if no generation required
//     */
//    private Bean getKeyGenData(EntityClass entity) {
//        Bean b = null;
//        if (!entity.isUsePkField() && !getGenData(entity).getKeyFullname().equals("java.lang.Object")) {
//            b = new Bean(getGenData(entity), true);
//            String key = genHelper.getKeyName(entity.getPackage(), entity.getClassName());
//            int pkgIndex = key.lastIndexOf('.'); //NOI18N
//            key = key.substring(pkgIndex+1);
//            b.setClassnameName(key);
//            b.setClassnamePackage(entity.getPackage());
//        } 
//        return b;
//    }
//    
//    private void addCmpFields(EntityClass entity, Bean genData) {
//        Iterator fieldIt = entity.getFields().iterator();
//        boolean pkDefined = false;
//        String pkFieldClass = null;
//        while (fieldIt.hasNext()) {
//            EntityMember m = (EntityMember) fieldIt.next();
//            String memberName = m.getMemberName();
//            String memberType = m.getMemberType();
//            StringBuffer sb = new StringBuffer(memberName);
//            sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));
//            int pos = genData.addCmField(true);
//            genData.setCmFieldMethodName(pos,sb.toString());
//            genData.setCmFieldClassname(pos, memberType);
//            if (m.isPrimaryKey()) {
//                pkDefined = true;
//                entity.setPkFieldName(memberName);
//                pkFieldClass = m.getMemberType();
//                genData.setCmFieldInKey(pos, "");  
//            }
//            genData.setCmFieldName(pos, memberName);
//            if (m.isNullable()) {
//                genData.setCmFieldIsNullable(pos,"true");
//            }
//            if (isPrimitive(memberType)) {
//                genData.setCmFieldIsPrimitive(pos,"true");
//            }
//        }
//        genData.setKey(true);
//        if (entity.isUsePkField()) {
//            genData.setKeyFullname(pkFieldClass);
//        } else {
//            if (pkDefined) {
//                genData.setKeyFullname(genHelper.getKeyName(entity.getPackage(), entity.getClassName()));
//            } else {
//                genData.setKeyFullname(Object.class.getName());
//            }
//        }
//    }
//    
//    private void addRelationshipFields(EntityClass entity, Bean genData) {
//        for (Iterator it = entity.getRoles().iterator(); it.hasNext();) {
//            RelationshipRole role = (RelationshipRole) it.next();
//            String cmrField = role.getFieldName();
//            boolean cascadeDelete = role.isCascade();
//            StringBuffer sb = new StringBuffer(cmrField);
//            sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));
//            int pos = genData.addCmField(true);
//            genData.setCmFieldMethodName(pos,sb.toString());
//            String rv = getCmrFieldType(role, entity.getPackage());
//            genData.setCmFieldClassname(pos,rv);
//            genData.setCmFieldName(pos,cmrField);
//            genData.setCmFieldCmrField(pos,"");
//            if (cascadeDelete) {
//                genData.setCmFieldCascadeDelete(pos,"");
//            }
//        }
//    }

    public void init(WizardDescriptor wiz) {
        project = Templates.getProject(wiz);
        DDProvider provider = DDProvider.getDefault();
        ejbModule = org.netbeans.modules.j2ee.api.ejbjar.EjbJar.getEjbJars (project)[0];
        dd = null;
        try {
            dd = provider.getMergedDDRoot(ejbModule.getMetadataUnit());
        } catch (IOException ioe) {
            NotifyDescriptor nb = new NotifyDescriptor.Message(ioe.getLocalizedMessage(),NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(nb);
        }
    }

    public String getFQClassName(String tableName) {
        return null;
    }

    public String generateEntityName(String name) {
        return genHelper.getBaseName(name) + "Bean"; //NOI18N
    }

    public Set createdObjects() {
        return Collections.singleton(ejbModule.getDeploymentDescriptor());
    }
}
