/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 2004-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.jmx.mbeanwizard.generator;

import java.lang.reflect.Modifier;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import org.netbeans.jmi.javamodel.Field;
import org.netbeans.jmi.javamodel.Import;
import org.netbeans.jmi.javamodel.Method;
import org.netbeans.jmi.javamodel.JavaClass;
import org.netbeans.jmi.javamodel.JavaModelPackage;
import org.netbeans.jmi.javamodel.MultipartId;
import org.netbeans.jmi.javamodel.Parameter;
import org.netbeans.jmi.javamodel.Resource;
import org.netbeans.jmi.javamodel.Type;
import org.netbeans.modules.javacore.api.JavaModel;
import org.netbeans.modules.jmx.MBeanNotification;
import org.netbeans.modules.jmx.WizardConstants;
import org.netbeans.modules.jmx.WizardHelpers;

/**
 *
 *  MBeanRegistration interface implementation code generator class.
 */
public class AddRegistIntfGenerator
{   
    private static final String PRE_REGISTER_JAVADOC = 
      "Allows the MBean to perform any operations it needs before being\n" + // NOI18N
      "registered in the MBean server. If the name of the MBean is not\n" + // NOI18N
      "specified, the MBean can provide a name for its registration. If\n" + // NOI18N
      "any exception is raised, the MBean will not be registered in the\n" + // NOI18N
      "MBean server.\n" + // NOI18N
      "@param server The MBean server in which the MBean will be registered.\n" + // NOI18N
      "@name name The object name of the MBean. This name is null if the\n" + // NOI18N
      "name parameter to one of the createMBean or registerMBean methods in\n" + // NOI18N
      "the MBeanServer interface is null. In that case, this method must\n" + // NOI18N
      "return a non-null ObjectName for the new MBean.\n" + // NOI18N
      "@return The name under which the MBean is to be registered. This value\n" + // NOI18N
      "must not be null. If the name parameter is not null, it will usually\n" + // NOI18N
      "but not necessarily be the returned value.\n" + // NOI18N
      "@trow Exception This exception will be caught by the MBean server and\n" + // NOI18N
      "re-thrown as an MBeanRegistrationException."; // NOI18N
    
    private static final String POST_REGISTER_JAVADOC = 
      "Allows the MBean to perform any operations needed after having\n" + // NOI18N
      "been registered in the MBean server or after the registration has\n" + // NOI18N
      "failed.\n" + // NOI18N
      "@param registrationDone Indicates wether or not the MBean has been\n" + // NOI18N
      "successfully registered in the MBean server. The value false means\n" + // NOI18N
      "that the registration has failed.\n"; // NOI18N
    
    private static final String PRE_DEREGISTER_JAVADOC = 
      "Allows the MBean to perform any operations it needs before being\n" + // NOI18N
      "unregistered by the MBean server.\n" + // NOI18N
      "@trow Exception This exception will be caught by the MBean server and\n" + // NOI18N
      "re-thrown as an MBeanRegistrationException."; // NOI18N
    
    private static final String POST_DEREGISTER_JAVADOC = 
      "Allows the MBean to perform any operations needed after having been\n" + // NOI18N
      "unregistered in the MBean server.\n"; // NOI18N
    
    /**
     * Entry point to generate Mbean registration code in mbean class.
     * @param mbeanClass <CODE>JavaClass</CODE> the MBean class to update
     * @param keepRefSelected <CODE>boolean</CODE> keep preRegister method 
     * parameters is selected
     * @param mbeanRes <CODE>Resource</CODE> represents MBean class
     * @throws java.io.IOException <CODE>IOException</CODE>
     * @throws java.lang.Exception <CODE>Exception</CODE>
     */
    public void update(JavaClass mbeanClass, Resource mbeanRes, 
            boolean keepRefSelected)
           throws java.io.IOException, Exception
    {
        boolean rollback = false;
        JavaModel.getJavaRepository().beginTrans(true);
        try {
            if (!WizardHelpers.getPackageName(mbeanClass.getName()).equals("")) // NOI18N
                MBeanFileGenerator.addManagementImport(mbeanRes);
          
            addMBeanRegistration(mbeanClass);
            addPreRegisterMethod(mbeanClass,keepRefSelected);
            addPostRegisterMethod(mbeanClass);
            addPreDeregisterMethod(mbeanClass);
            addPostDeregisterMethod(mbeanClass);
            if (keepRefSelected)
                addFields(mbeanClass);
        } catch (Exception e) {
            rollback = true;
            e.printStackTrace();
        } finally {
            JavaModel.getJavaRepository().endTrans(rollback);
        }
        
        rollback = false;
        JavaModel.getJavaRepository().beginTrans(true);
        try {
            if (WizardHelpers.getPackageName(mbeanClass.getName()).equals("")) // NOI18N
                MBeanFileGenerator.addManagementImport(mbeanRes);
        } catch (Exception e) {
            rollback = true;
        } finally {
            JavaModel.getJavaRepository().endTrans(rollback);
        }
    }
    
    private void addPreRegisterMethod(JavaClass tgtClass, boolean keepRefSelected) {
        JavaModelPackage pkg = (JavaModelPackage)tgtClass.refImmediatePackage();
        
        String methodBody =
                "return name;\n"; // NOI18N
        if (keepRefSelected)
            methodBody = "objectName = name;\n" + // NOI18N 
                         "mbeanServer = server;\n" +  // NOI18N
                         methodBody;
        ArrayList params = new ArrayList();
        Parameter server = pkg.getParameter().createParameter("server", // NOI18N
                Collections.EMPTY_LIST, // annotations
                false, // is final
                MBeanFileGenerator.getTypeRef(pkg, "MBeanServer"), // NOI18N
                0, // dimCount
                false);
        params.add(server);
        Parameter name = pkg.getParameter().createParameter("name", // NOI18N
                Collections.EMPTY_LIST, // annotations
                false, // is final
                MBeanFileGenerator.getTypeRef(pkg, "ObjectName"), // NOI18N
                0, // dimCount
                false);
        params.add(name);
        
        ArrayList exceptions = new ArrayList();
        exceptions.add(MBeanFileGenerator.getTypeRef(pkg, "Exception")); // NOI18N
        
        Method method = pkg.getMethod().createMethod(
                "preRegister", // NOI18N
                Collections.EMPTY_LIST,
                Modifier.PUBLIC,
                PRE_REGISTER_JAVADOC, // javadoc text
                null, // jvadoc
                null, // object body
                methodBody, // string body
                Collections.EMPTY_LIST, // type params
                params, // parameters
                exceptions, // exceptions
                MBeanFileGenerator.getTypeRef(pkg, "ObjectName"), // NOI18N
                0);
        tgtClass.getFeatures().add(method);

    }
    
    private void addPostRegisterMethod(JavaClass tgtClass) {
        JavaModelPackage pkg = (JavaModelPackage)tgtClass.refImmediatePackage();
        
        String methodBody =
                "//TODO postRegister implementation;\n"; // NOI18N
        
        ArrayList params = new ArrayList();
        Parameter registrationDone = pkg.getParameter().createParameter(
                "registrationDone", // NOI18N
                Collections.EMPTY_LIST, // annotations
                false, // is final
                MBeanFileGenerator.getTypeRef(pkg, "Boolean"), // NOI18N
                0, // dimCount
                false);
        params.add(registrationDone);
        
        Method method = pkg.getMethod().createMethod(
                "postRegister", // NOI18N
                Collections.EMPTY_LIST,
                Modifier.PUBLIC,
                POST_REGISTER_JAVADOC, // javadoc text
                null, // jvadoc
                null, // object body
                methodBody, // string body
                Collections.EMPTY_LIST, // type params
                params, // parameters
                Collections.EMPTY_LIST, // exceptions
                MBeanFileGenerator.getTypeRef(pkg, "void"), // NOI18N
                0);
        tgtClass.getFeatures().add(method);

    } 
    
    private void addPreDeregisterMethod(JavaClass tgtClass) {
        JavaModelPackage pkg = (JavaModelPackage)tgtClass.refImmediatePackage();
        
        String methodBody =
                "//TODO preDeregister implementation;\n"; // NOI18N
        
        ArrayList exceptions = new ArrayList();
        exceptions.add(MBeanFileGenerator.getTypeRef(pkg, "Exception")); // NOI18N
        
        Method method = pkg.getMethod().createMethod(
                "preDeregister", // NOI18N
                Collections.EMPTY_LIST,
                Modifier.PUBLIC,
                PRE_DEREGISTER_JAVADOC, // javadoc text
                null, // jvadoc
                null, // object body
                methodBody, // string body
                Collections.EMPTY_LIST, // type params
                Collections.EMPTY_LIST, // parameters
                exceptions, // exceptions
                MBeanFileGenerator.getTypeRef(pkg, "void"), // NOI18N
                0);
        tgtClass.getFeatures().add(method);

    }  
    
    private void addPostDeregisterMethod(JavaClass tgtClass) {
        JavaModelPackage pkg = (JavaModelPackage)tgtClass.refImmediatePackage();
        
        String methodBody =
                "//TODO postDeregister implementation;\n"; // NOI18N
        
        Method method = pkg.getMethod().createMethod(
                "postDeregister", // NOI18N
                Collections.EMPTY_LIST,
                Modifier.PUBLIC,
                POST_DEREGISTER_JAVADOC, // javadoc text
                null, // jvadoc
                null, // object body
                methodBody, // string body
                Collections.EMPTY_LIST, // type params
                Collections.EMPTY_LIST, // parameters
                Collections.EMPTY_LIST, // exceptions
                MBeanFileGenerator.getTypeRef(pkg, "void"), // NOI18N
                0);
        tgtClass.getFeatures().add(method);

    }
    
    private static void addFields(JavaClass tgtClass) {
        JavaModelPackage pkg = (JavaModelPackage)tgtClass.refImmediatePackage();
        Field mbeanServer = pkg.getField().createField("mbeanServer", Collections.EMPTY_LIST, // NOI18N
                    Modifier.PRIVATE, null, null, false, 
                    pkg.getMultipartId().createMultipartId("MBeanServer", null, Collections.EMPTY_LIST), // NOI18N
                    0, null, null);
        tgtClass.getFeatures().add(mbeanServer);
        Field objectName = pkg.getField().createField("objectName", Collections.EMPTY_LIST, // NOI18N
                    Modifier.PRIVATE, null, null, true, 
                    pkg.getMultipartId().createMultipartId(
                        "ObjectName", null, Collections.EMPTY_LIST), // NOI18N
                    0, null, null);
        tgtClass.getFeatures().add(objectName);
    }
    
    private static void addMBeanRegistration(JavaClass tgtClass) {
        JavaModelPackage pkg = (JavaModelPackage)tgtClass.refImmediatePackage();
        tgtClass.getInterfaceNames().add(pkg.getMultipartId().createMultipartId(
                    "MBeanRegistration", // NOI18N
                    null,
                    Collections.EMPTY_LIST));
    }
    
}
