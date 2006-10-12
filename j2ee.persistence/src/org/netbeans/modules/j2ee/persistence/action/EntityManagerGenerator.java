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

package org.netbeans.modules.j2ee.persistence.action;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.jmi.reflect.JmiException;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.jmi.javamodel.Annotation;
import org.netbeans.jmi.javamodel.AnnotationType;
import org.netbeans.jmi.javamodel.AttributeValue;
import org.netbeans.jmi.javamodel.Field;
import org.netbeans.jmi.javamodel.JavaClass;
import org.netbeans.jmi.javamodel.JavaModelPackage;
import org.netbeans.jmi.javamodel.Method;
import org.netbeans.jmi.javamodel.Parameter;
import org.netbeans.jmi.javamodel.ParameterizedType;
import org.netbeans.jmi.javamodel.Type;
import org.netbeans.jmi.javamodel.UnresolvedClass;
import org.netbeans.modules.j2ee.common.JMIUtils;
import org.netbeans.modules.j2ee.common.queries.api.InjectionTargetQuery;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.common.JMIGenerationUtil;
import org.netbeans.modules.j2ee.persistence.api.PersistenceScope;
import org.netbeans.modules.j2ee.persistence.dd.PersistenceMetadata;
import org.netbeans.modules.j2ee.persistence.dd.persistence.model_1_0.PersistenceUnit;
import org.netbeans.modules.javacore.api.JavaModel;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

/**
 * TODO: move this class to different package if anybody else wants to use it
 * @author Martin Adamek
 */
public class EntityManagerGenerator {
    
    private static final ErrorManager LOGGER = ErrorManager.getDefault().getInstance("org.netbeans.modules.j2ee.persistence.action.EntityManagerGenerator"); // NOI18N
    private static final boolean LOG = LOGGER.isLoggable(ErrorManager.INFORMATIONAL);

    private static final String COMMENT_TODO = 
            "// TODO:\n" + 
            "// ";
    
    // following rule applies to creating new calls:
    // 1st paramater - parameter name
    // 2nd parameter - parameter type (FQN)
    // 3rd parameter - parameter type (FQN)
    public static final String OPERATION_PERSIST = "em.persist({0});";
    public static final String OPERATION_MERGE = "em.merge({0});";
    public static final String OPERATION_REMOVE = 
            "em.merge({0});\n" + 
            "em.remove({0});";
    public static final String OPERATION_FIND = "return ({2}) em.find({2}.class, {0});";
    public static final String OPERATION_FIND_ALL = "return em.createQuery(\"select object(o) from {2} as o\").getResultList();";
    
    private enum Initialization {INJECT, EMF, INIT}
    
    public EntityManagerGenerator() {
    }
    
    /**
     * Generate sample usage of EntityManager in given class.
     * Generated code will can be different, depending on environment in which the target class is used
     * @param javaClass target class for generation
     */
    public static void generate(JavaClass javaClass) {
        Parameter p = JMIGenerationUtil.createParameter(javaClass, "object", Object.class.getName());
        String methodName = computeMethodName(javaClass, "persist", p);
        generate(javaClass, OPERATION_PERSIST, methodName, "void", p, null, true);
    }
    
    /**
     * Generate one of nasic basic CRUD usages of EntityManager in given class
     * Generated code will can be different, depending on environment in which the target class is used
     * 
     * @param javaClass target class for generation
     * @param operation generated operation call, see predefined OPERATION_* constants
     * @param methodName name of generated method
     * @param returnTypeFqn return type, "void" if nothing will be returned
     * @param parameter parameter to pass, null if not needed
     * @param queryAttribute attribute for query, if needed, typically Entity class FQN, or Entity name
     * @param comment if generated method call should be commented in code
     */
    public static void generate(JavaClass javaClass, String operation, String methodName, String returnTypeFqn, Parameter parameter, String queryAttribute, boolean comment) {
        generate(javaClass, operation, methodName, returnTypeFqn, parameter, queryAttribute, comment, true);
        fixImports(javaClass);
    }
    
    public static void generate(JavaClass javaClass, String operation, String methodName, String returnTypeFqn, Parameter parameter, String queryAttribute, boolean comment, boolean checkInjection) {
        boolean rollback = true;
        JMIUtils.beginJmiTransaction(true);
        try {
        
        J2eeModule j2eeModule = null;
        
        // try to get J2eeModule
        Project project = FileOwnerQuery.getOwner(JavaModel.getFileObject(javaClass.getResource()));
        J2eeModuleProvider j2eeModuleProvider = (J2eeModuleProvider) project.getLookup().lookup(J2eeModuleProvider.class);
        if (j2eeModuleProvider != null) {
            j2eeModule = j2eeModuleProvider.getJ2eeModule();
        }
        
        PersistenceUnit pu = getPersistenceUnit(javaClass);
        if (j2eeModule == null) {
            // Application-managed persistence context in J2SE project (Resource-transaction)
            applicationManagedResourceTransactionInJ2SE(javaClass, pu, operation, methodName, returnTypeFqn, parameter, queryAttribute, comment);
        } else {
            // it is Web or EJB, let's get all needed information
            String jtaDataSource = pu.getJtaDataSource();
            String nonJtaDataSource = pu.getNonJtaDataSource();
            String transactionType = pu.getTransactionType();
            boolean isInjectionTarget = !checkInjection || InjectionTargetQuery.isInjectionTarget(javaClass);
            boolean isContainerManaged = (jtaDataSource != null && !jtaDataSource.equals("")) && (transactionType != null && transactionType.equals("JTA"));
            boolean isJTA = (transactionType == null || transactionType.equals("JTA")); // JTA is default value for transaction type in non-J2SE projects
            
            if (j2eeModule.getModuleType().equals(J2eeModule.WAR)) { // Web project
                if (isContainerManaged) { // Container-managed persistence context
                    if (isInjectionTarget) { // servlet, JSF managed bean ...
                        containerManagedJTAInjectableInWeb(javaClass, pu, operation, methodName, returnTypeFqn, parameter, queryAttribute, comment);
                    } else { // other classes
                        containerManagedJTANonInjectableInWeb(javaClass, operation, methodName, returnTypeFqn, parameter, queryAttribute, comment);
                    }
                } else { // Application-managed persistence context (Resource-transaction)
                    if (isJTA) { // JTA
                        if (isInjectionTarget) { // servlet, JSF managed bean ...
                            applicationManagedJTAInjectableInWeb(javaClass, operation, methodName, returnTypeFqn, parameter, queryAttribute, comment);
                        } else { // other classes
                            applicationManagedJTANonInjectableInWeb(javaClass, operation, methodName, returnTypeFqn, parameter, queryAttribute, comment);
                        }
                    } else { // Resource-transaction
                        if (isInjectionTarget) { // servlet, JSF managed bean ...
                            applicationManagedResourceTransactionInjectableInWeb(javaClass, pu, operation, methodName, returnTypeFqn, parameter, queryAttribute, comment);
                        } else { // other classes
                            applicationManagedResourceTransactionNonInjectableInWeb(javaClass, pu, operation, methodName, returnTypeFqn, parameter, queryAttribute, comment);
                        }
                    }
                }
            } else if (j2eeModule.getModuleType().equals(J2eeModule.EJB)) { // EJB project
                if (isContainerManaged) { // Container-managed persistence context
                    if (isInjectionTarget) { // session, MessageDriven
                        containerManagedJTAInjectableInEJB(javaClass, operation, methodName, returnTypeFqn, parameter, queryAttribute, comment);
                    } else { // other classes
                        // ???
                    }
                } else { // Application-managed persistence context
                    if (isJTA) { // JTA
                        if (isInjectionTarget) { // session, MDB
                            applicationManagedJTAInjectableInEJB(javaClass, operation, methodName, returnTypeFqn, parameter, queryAttribute, comment);
                        } else { // other classes
                            applicationManagedJTANonInjectableInEJB(javaClass, operation, methodName, returnTypeFqn, parameter, queryAttribute, comment);
                        }
                    } else { // Resource-transaction
                        if (isInjectionTarget) { // session, MDB
                            applicationManagedResourceTransactionInjectableInEJB(javaClass, pu, operation, methodName, returnTypeFqn, parameter, queryAttribute, comment);
                        } else { // other classes
                            applicationManagedResourceTransactionNonInjectableInEJB(javaClass, pu, operation, methodName, returnTypeFqn, parameter, queryAttribute, comment);
                        }
                    }
                }
            }
        }
        
        rollback = false;
        } catch (JmiException jmie) {
            ErrorManager.getDefault().notify(jmie);
        } finally {
            JMIUtils.endJmiTransaction(rollback);
        }
    }
    
    // use-cases ===============================================================

    private static void applicationManagedResourceTransactionInJ2SE(JavaClass javaClass, PersistenceUnit pu, String operation, String methodName, String returnTypeFqn, Parameter parameter, String queryAttributeFqn, boolean comment) {
        if (LOG) LOGGER.log("applicationManagedJTAInjectableInJ2SE");
        Field emf = getOrCreateEntityManagerFactory(javaClass, pu, false, true);
        String mName = computeMethodName(javaClass, methodName, parameter);
        Method m = JMIGenerationUtil.createMethod(javaClass, mName, Modifier.PUBLIC, returnTypeFqn);
        if (parameter != null) {
            m.getParameters().add(parameter);
        }
        String text =
                "javax.persistence.EntityManager em = emf.createEntityManager();\n" +
                "em.getTransaction().begin();\n" +
                "try {\n" +
                generateCallLines(operation, parameter, queryAttributeFqn, comment) +
                "    em.getTransaction().commit();\n" +
                "} catch (Exception e) {\n" +
                "    e.printStackTrace();\n" +
                "    em.getTransaction().rollback();\n" +
                "} finally {\n" +
                "    em.close();\n" +
                "}";
        m.setBodyText(text);
        javaClass.getFeatures().add(m);
        // TODO: add getter and setter for em
    }
    
    private static void containerManagedJTAInjectableInWeb(JavaClass javaClass, PersistenceUnit pu, String operation, String methodName, String returnTypeFqn, Parameter parameter, String queryAttributeFqn, boolean comment) {
        if (LOG) LOGGER.log("containerManagedJTAInjectableInWeb");
        // TODO: what should be value of LogicalName?
        String aTypeName = "javax.persistence.PersistenceContext"; // NOI18N
        List annotations = javaClass.getAnnotations();
        boolean found = false;
        for (Iterator iter = annotations.iterator(); iter.hasNext(); ) {
            Annotation anno = (Annotation) iter.next();
            AnnotationType aType = anno.getType();
            if (aTypeName.equals(aType.getName())) {
                found = true;
                break;
            }
        }
        if (!found) {
            // TODO: what should be value of LogicalName?
            AttributeValue nameAV = JMIGenerationUtil.createAttributeValue(javaClass, "name", "persistence/LogicalName");
            AttributeValue unitNameAV = JMIGenerationUtil.createAttributeValue(javaClass, "unitName", pu.getName());
            List list = new ArrayList();
            list.add(nameAV);
            list.add(unitNameAV);
            Annotation a = JMIGenerationUtil.createAnnotation(javaClass, aTypeName, list);
            annotations.add(a);
        }
        Field utx = getOrCreateUserTransaction(javaClass, true);
        String mName = computeMethodName(javaClass, methodName, parameter);
        Method m = JMIGenerationUtil.createMethod(javaClass, mName, Modifier.PROTECTED, returnTypeFqn);
        if (parameter != null) {
            m.getParameters().add(parameter);
        }
        String text =
            "try {\n" +
            "    javax.naming.Context ctx = (javax.naming.Context) new javax.naming.InitialContext().lookup(\"java:comp/env\");\n" +
            "    javax.persistence.EntityManager em =  (javax.persistence.EntityManager) ctx.lookup(\"persistence/LogicalName\");\n" +
            "    utx.begin();\n" +
            generateCallLines(operation, parameter, queryAttributeFqn, comment) +
            "    utx.commit();\n" +
            "} catch(Exception e) {\n" +
            "    java.util.logging.Logger.getLogger(getClass().getName()).log(java.util.logging.Level.SEVERE,\"exception caught\", e);\n" +
            "    throw new RuntimeException(e);\n" +
            "}";
        m.setBodyText(text);
        javaClass.getFeatures().add(m);
    }
    
    private static void containerManagedJTANonInjectableInWeb(JavaClass javaClass, String operation, String methodName, String returnTypeFqn, Parameter parameter, String queryAttributeFqn, boolean comment) {
        if (LOG) LOGGER.log("containerManagedJTANonInjectableInWeb");
        String mName = computeMethodName(javaClass, methodName, parameter);
        Method m = JMIGenerationUtil.createMethod(javaClass, mName, Modifier.PUBLIC, returnTypeFqn);
        if (parameter != null) {
            m.getParameters().add(parameter);
        }
        String text =
            "// add this to web.xml:\n" +
            "// <persistence-context-ref>\n" +
            "//    <persistence-context-ref-name>persistence/LogicalName</persistence-context-ref-name>\n" +
            "//    < persistence-unit-name>PUName</persistence-unit-name>\n" +
            "// </persistence-context-ref>\n" +
            "// <resource-ref>\n" +
            "//     <res-ref-name>UserTransaction</res-ref-name>\n" +
            "//     <res-type>javax.transaction.UserTransaction</res-type>\n" +
            "//     <res-auth>Container</res-auth>\n" +
            "// </resource-ref>\n" +
            "try {\n" +
            "    javax.naming.Context ctx = new javax.naming.InitialContext();\n" +
            "    javax.persistence.EntityManager em =  (javax.persistence.EntityManager) ctx.lookup(\"java:comp/env/persistence/LogicalName\");\n" +
            "    javax.transaction.UserTransaction utx = (javax.transaction.UserTransaction) ctx.lookup(\"java:comp/env/UserTransaction\");\n" +
            "    utx.begin();\n" +
            generateCallLines(operation, parameter, queryAttributeFqn, comment) +
            "    utx.commit();\n" +
            "} catch(Exception e) {\n" +
            "    java.util.logging.Logger.getLogger(getClass().getName()).log(java.util.logging.Level.SEVERE,\"exception caught\", e);\n" +
            "    throw new RuntimeException(e);\n" +
            "}";
        m.setBodyText(text);
        javaClass.getFeatures().add(m);
    }
    
    // not supported
    private static void applicationManagedJTAInjectableInWeb(JavaClass javaClass, String operation, String methodName, String returnTypeFqn, Parameter parameter, String queryAttributeFqn, boolean comment) {
        if (LOG) LOGGER.log("applicationManagedJTAInjectableInWeb");
        NotifyDescriptor d = new NotifyDescriptor.Message(
                NbBundle.getMessage(EntityManagerGenerator.class, "ERR_NotSupportedAMJTA"), NotifyDescriptor.INFORMATION_MESSAGE);
        DialogDisplayer.getDefault().notify(d);
    }
    
    // not supported
    private static void applicationManagedJTANonInjectableInWeb(JavaClass javaClass, String operation, String methodName, String returnTypeFqn, Parameter parameter, String queryAttributeFqn, boolean comment) {
        if (LOG) LOGGER.log("applicationManagedJTANonInjectableInWeb");
        NotifyDescriptor d = new NotifyDescriptor.Message(
                NbBundle.getMessage(EntityManagerGenerator.class, "ERR_NotSupportedAMJTA"), NotifyDescriptor.INFORMATION_MESSAGE);
        DialogDisplayer.getDefault().notify(d);
    }
    
    private static void applicationManagedResourceTransactionInjectableInWeb(JavaClass javaClass, PersistenceUnit pu, String operation, String methodName, String returnTypeFqn, Parameter parameter, String queryAttributeFqn, boolean comment) {
        if (LOG) LOGGER.log("applicationManagedResourceTransactionInjectableInWeb");
        Field emf = getOrCreateEntityManagerFactory(javaClass, pu, true, false);
        String mName = computeMethodName(javaClass, methodName, parameter);
        Method m = JMIGenerationUtil.createMethod(javaClass, mName, Modifier.PUBLIC, returnTypeFqn);
        if (parameter != null) {
            m.getParameters().add(parameter);
        }
        String text =
            "javax.persistence.EntityManager em = emf.createEntityManager();\n" +
            "try {\n" +
            "    em.getTransaction().begin();\n" +
            generateCallLines(operation, parameter, queryAttributeFqn, comment) +
            "    em.getTransaction().commit();\n" +
            "} catch(Exception e) {\n" +
            "    java.util.logging.Logger.getLogger(getClass().getName()).log(java.util.logging.Level.SEVERE,\"exception caught\", e);\n" +
            "    em.getTransaction().rollback();\n" +
            "} finally {\n" +
            "    em.close();\n" +
            "}";
        m.setBodyText(text);
        javaClass.getFeatures().add(m);
    }
    
    private static void applicationManagedResourceTransactionNonInjectableInWeb(JavaClass javaClass, PersistenceUnit pu, String operation, String methodName, String returnTypeFqn, Parameter parameter, String queryAttributeFqn, boolean comment) {
        if (LOG) LOGGER.log("applicationManagedResourceTransactionNonInjectableInWeb");
        Field emf = getOrCreateEntityManagerFactory(javaClass, pu, false, false);
        Field em = getOrCreateEntityManager(javaClass, emf, Initialization.EMF);
        String mName = computeMethodName(javaClass, methodName, parameter);
        Method m = JMIGenerationUtil.createMethod(javaClass, mName, Modifier.PUBLIC, returnTypeFqn);
        if (parameter != null) {
            m.getParameters().add(parameter);
        }
        String text =
            "try {\n" +
            "    em.getTransaction().begin();\n" +
            generateCallLines(operation, parameter, queryAttributeFqn, comment) +
            "    em.getTransaction().commit();\n" +
            "} catch(Exception e) {\n" +
            "    java.util.logging.Logger.getLogger(getClass().getName()).log(java.util.logging.Level.SEVERE,\"exception caught\", e);\n" +
            "    em.getTransaction().rollback();\n" +
            "} finally {\n" +
            "    em.close();\n" +
            "}";
        m.setBodyText(text);
        javaClass.getFeatures().add(m);
        // TODO: add getter and setter for em
    }
    
    private static void containerManagedJTAInjectableInEJB(JavaClass javaClass, String operation, String methodName, String returnTypeFqn, Parameter parameter, String queryAttributeFqn, boolean comment) {
        if (LOG) LOGGER.log("containerManagedJTAInjectableInEJB");
        Field em = getOrCreateEntityManager(javaClass, null, Initialization.INJECT);
        Method m = JMIGenerationUtil.createMethod(javaClass, methodName, Modifier.PUBLIC, returnTypeFqn);
        if (parameter != null) {
            m.getParameters().add(parameter);
        }
        String text = generateCallLines(operation, parameter, queryAttributeFqn, comment);
        m.setBodyText(text);
        javaClass.getFeatures().add(m);
    }
    
    // not supported
    private static void applicationManagedJTAInjectableInEJB(JavaClass javaClass, String operation, String methodName, String returnTypeFqn, Parameter parameter, String queryAttributeFqn, boolean comment) {
        if (LOG) LOGGER.log("applicationManagedJTAInjectableInEJB");
        NotifyDescriptor d = new NotifyDescriptor.Message(
                NbBundle.getMessage(EntityManagerGenerator.class, "ERR_NotSupportedAMJTA"), NotifyDescriptor.INFORMATION_MESSAGE);
        DialogDisplayer.getDefault().notify(d);     
    }
    
    // not supported
    private static void applicationManagedJTANonInjectableInEJB(JavaClass javaClass, String operation, String methodName, String returnTypeFqn, Parameter parameter, String queryAttributeFqn, boolean comment) {
        if (LOG) LOGGER.log("applicationManagedJTANonInjectableInEJB");
        NotifyDescriptor d = new NotifyDescriptor.Message(
                NbBundle.getMessage(EntityManagerGenerator.class, "ERR_NotSupportedAMJTA"), NotifyDescriptor.INFORMATION_MESSAGE);
        DialogDisplayer.getDefault().notify(d);
    }
    
    private static void applicationManagedResourceTransactionInjectableInEJB(JavaClass javaClass, PersistenceUnit pu, String operation, String methodName, String returnTypeFqn, Parameter parameter, String queryAttributeFqn, boolean comment) {
        if (LOG) LOGGER.log("applicationManagedResourceTransactionInjectableInEJB");
        Field emf = getOrCreateEntityManagerFactory(javaClass, pu, true, false);
        Field em = getOrCreateEntityManager(javaClass, emf, Initialization.INIT);
        Method m = JMIGenerationUtil.createMethod(javaClass, methodName, Modifier.PUBLIC, returnTypeFqn);
        if (parameter != null) {
            m.getParameters().add(parameter);
        }
        String text =
            "try {\n" +
            "    em.getTransaction().begin();\n" +
            generateCallLines(operation, parameter, queryAttributeFqn, comment) +
            "    em.getTransaction().commit();\n" +
            "} catch(Exception e) {\n" +
            "    java.util.logging.Logger.getLogger(getClass().getName()).log(java.util.logging.Level.SEVERE,\"exception caught\", e);\n" +
            "    em.getTransaction().rollback();\n" +
            "}";
        m.setBodyText(text);
        javaClass.getFeatures().add(m);
    }
    
    private static void applicationManagedResourceTransactionNonInjectableInEJB(JavaClass javaClass, PersistenceUnit pu, String operation, String methodName, String returnTypeFqn, Parameter parameter, String queryAttributeFqn, boolean comment) {
        if (LOG) LOGGER.log("applicationManagedResourceTransactionNonInjectableInEJB");
        Field emf = getOrCreateEntityManagerFactory(javaClass, pu, false, false);
        Field em = getOrCreateEntityManager(javaClass, emf, Initialization.EMF);
        String mName = computeMethodName(javaClass, methodName, parameter);
        Method m = JMIGenerationUtil.createMethod(javaClass, mName, Modifier.PUBLIC, returnTypeFqn);
        if (parameter != null) {
            m.getParameters().add(parameter);
        }
        String text =
            "try {\n" +
            "    em.getTransaction().begin();\n" +
            generateCallLines(operation, parameter, queryAttributeFqn, comment) +
            "    em.getTransaction().commit();\n" +
            "} catch(Exception e) {\n" +
            "    java.util.logging.Logger.getLogger(getClass().getName()).log(java.util.logging.Level.SEVERE,\"exception caught\", e);\n" +
            "    em.getTransaction().rollback();\n" +
            "} finally {\n" +
            "    em.close();\n" +
            "}";
        m.setBodyText(text);
        javaClass.getFeatures().add(m);
        // TODO: add getter and setter for em
    }
    
    // helpers =================================================================
    
    private static String generateCallLines(String operation, Parameter parameter, String queryAttributeFqn, boolean comment) {
        return (comment ? COMMENT_TODO : "") + MessageFormat.format(operation, new Object[] {
            parameter == null ? null : parameter.getName(), 
            parameter == null ? null : parameter.getType().getName(), 
            queryAttributeFqn});
    }
    
    private static PersistenceUnit getPersistenceUnit(JavaClass jc) {
        PersistenceScope persistenceScope = PersistenceScope.getPersistenceScope(JavaModel.getFileObject(jc.getResource()));
        try {
            // TODO: fix ASAP! 1st PU is taken, needs to find the one which realy owns given file
            return PersistenceMetadata.getDefault().getRoot(persistenceScope.getPersistenceXml()).getPersistenceUnit(0);
        } catch (IOException ex) {
            ErrorManager.getDefault().notify(ex);
        }
        return null;
    }
    
    private static Field getOrCreateEntityManagerFactory(JavaClass javaClass, PersistenceUnit unit, boolean inject, boolean isStatic) {
        Field emfField = JMIUtils.findFieldByType(javaClass, "javax.persistence.EntityManagerFactory");
        if (emfField != null) {
            return emfField;
        }
        boolean rollback = true;
        JMIUtils.beginJmiTransaction(true);
        try {
            int modifier = (isStatic ? Modifier.PRIVATE | Modifier.STATIC : Modifier.PRIVATE);
            emfField = JMIGenerationUtil.createField(javaClass, createUniqueName("emf"), modifier, "javax.persistence.EntityManagerFactory");
            if (inject) {
                Annotation pu = JMIGenerationUtil.createAnnotation(javaClass, "javax.persistence.PersistenceUnit", Collections.EMPTY_LIST);
                emfField.getAnnotations().add(pu);
            } else {
                emfField.setInitialValueText("javax.persistence.Persistence.createEntityManagerFactory(\"" + unit.getName() + "\")");
            }
            javaClass.getFeatures().add(0, emfField);
            rollback = false;
        } finally {
            JMIUtils.endJmiTransaction(rollback);
        }
        return emfField;
    }
    
    private static Field getOrCreateEntityManager(JavaClass javaClass, Field emf, Initialization init) {
        Field em = JMIUtils.findFieldByType(javaClass, "javax.persistence.EntityManager");
        if (em != null) {
            return em;
        }
        boolean rollback = true;
        JMIUtils.beginJmiTransaction(true);
        try {
            em = JMIGenerationUtil.createField(javaClass, createUniqueName("em"), Modifier.PRIVATE, "javax.persistence.EntityManager");
            switch (init) {
                case INJECT:
                    Annotation pu = JMIGenerationUtil.createAnnotation(javaClass, "javax.persistence.PersistenceContext", Collections.EMPTY_LIST);
                    em.getAnnotations().add(pu);
                    break;
                case EMF:
                    em.setInitialValueText(emf.getName() + ".createEntityManager()");
                    break;
                case INIT:
                    // init method with @PostConstruct annotation
                    Method initMethod = JMIGenerationUtil.createMethod(javaClass, createUniqueName("init"), Modifier.PUBLIC, "void");
                    Annotation postConstructAnnotation = JMIGenerationUtil.createAnnotation(javaClass, "javax.annotation.PostConstruct", Collections.EMPTY_LIST);
                    initMethod.getAnnotations().add(postConstructAnnotation);
                    initMethod.setBodyText(em.getName() + " = " + emf.getName() + ".createEntityManager();");
                    javaClass.getFeatures().add(initMethod);
                    // destroy method with @PreConstruct annotation
                    Method destroyMethod = JMIGenerationUtil.createMethod(javaClass, createUniqueName("destroy"), Modifier.PUBLIC, "void");
                    Annotation preDestroyAnnotation = JMIGenerationUtil.createAnnotation(javaClass, "javax.annotation.PreDestroy", Collections.EMPTY_LIST);
                    destroyMethod.getAnnotations().add(preDestroyAnnotation);
                    destroyMethod.setBodyText(em.getName()+ ".close();");
                    javaClass.getFeatures().add(destroyMethod);
                    break;
            }
            javaClass.getFeatures().add(emf == null ? 0 : 1, em);
            rollback = false;
        } finally {
            JMIUtils.endJmiTransaction(rollback);
        }
        return em;
    }
    
    private static Field getOrCreateUserTransaction(JavaClass javaClass, boolean inject) {
        Field utx = JMIUtils.findFieldByType(javaClass, "javax.transaction.UserTransaction");
        if (utx != null) {
            return utx;
        }
        boolean rollback = true;
        JMIUtils.beginJmiTransaction(true);
        try {
            utx = JMIGenerationUtil.createField(javaClass, createUniqueName("utx"), Modifier.PRIVATE, "javax.transaction.UserTransaction");
            if (inject) {
                Annotation pu = JMIGenerationUtil.createAnnotation(javaClass, "javax.annotation.Resource", Collections.EMPTY_LIST);
                utx.getAnnotations().add(pu);
            } else {
                // TODO: find all constructors and add following code in them:
                // Context ctx = new InitialContext();
                // utx = (UserTransaction)ctx.lookup("UserTransaction");
            }
            javaClass.getFeatures().add(0, utx);
            rollback = false;
        } finally {
            JMIUtils.endJmiTransaction(rollback);
        }
        return utx;
    }
    
    private static String createUniqueName(String name) {
        // TODO: implement me
        return name;
    }
    
    private static String computeMethodName(JavaClass jc, String methodName, Parameter parameter) {
        Set names = new HashSet();
        int parCount = parameter != null ? 1 : 0;
        for (Iterator iter = jc.getContents().iterator(); iter.hasNext(); ) {
            Object obj = iter.next();
            if (obj instanceof Method) {
                Method m = (Method) obj;
                List params = m.getParameters();
                if (parCount != params.size())
                    continue;
                if (parameter != null) {
                    Type type = parameter.getType();
                    if (type instanceof ParameterizedType) {
                        type = ((ParameterizedType) type).getDefinition();
                    }
                    if (type instanceof UnresolvedClass && "Object".equals(type.getName())) {
                        // [PENDING] java.lang.Object type should be passed as a correctly resolved class
                        type = ((JavaModelPackage)jc.refImmediatePackage()).getType().resolve("java.lang.Object"); // NOI18N
                    }
                    String pName = ((Parameter) params.get(0)).getType().getName();
                    if (!type.getName().equals(pName))
                        continue;
                }
                names.add(m.getName());
            } // if
        } // for
        
        if (!names.contains(methodName)) {
            return methodName;
        }
        for (int num = 2; num <= 50; num++) {
            String name = methodName + num;
            if (!names.contains(name)) {
                return name;
            }
        }
        return methodName;
    }

    public static void fixImports(JavaClass javaClass) {
        boolean rollback = true;
        JMIUtils.beginJmiTransaction(true);
        try {
            JMIUtils.fixImports(javaClass);
            rollback = false;
        } catch (JmiException jmie) {
            ErrorManager.getDefault().notify(jmie);
        } finally {
            JMIUtils.endJmiTransaction(rollback);
        }
    }
    
}
