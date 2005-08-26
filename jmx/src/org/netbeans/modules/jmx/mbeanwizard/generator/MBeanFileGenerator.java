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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.netbeans.jmi.javamodel.Import;
import org.netbeans.jmi.javamodel.JavaClass;
import org.netbeans.jmi.javamodel.JavaModelPackage;
import org.netbeans.jmi.javamodel.MultipartId;
import org.netbeans.jmi.javamodel.Resource;
import org.netbeans.jmi.javamodel.Type;
import org.netbeans.modules.jmx.MBeanAttribute;
import org.netbeans.modules.jmx.MBeanDO;
import org.netbeans.modules.jmx.MBeanOperation;
import org.netbeans.modules.jmx.MBeanOperationParameter;
import org.netbeans.modules.jmx.WizardConstants;
import org.netbeans.modules.jmx.WizardHelpers;
import org.openide.filesystems.FileObject;

/**
 * Generic MBean File generator.
 * @author thomas
 */
public abstract class MBeanFileGenerator {
    
    public static final String METHOD_SIGNATURE_DEF =
      "String[] methodSignature;\n\n"; // NOI18N 

    public static final String METHOD_SIGNATURE =
      "methodSignature = new String[] {\n"; // NOI18N 
    
    // {0} = operation name
    // {1} = operation code
    // {2} = operation name code to check
    public static final String OPERATION_CHECK_PATTERN =
      "if ({2}.equals(\"{0}\") && Arrays.equals(signature, methodSignature)) '{'\n" + // NOI18N 
      "    {1}\n" + // NOI18N 
      "'}'\n\n"; // NOI18N 
    
    /**
     * Generates all the files for the new MBean.
     * @param mbean <CODE>MBeanDO</CODE> the MBean to generate
     * @throws java.io.IOException <CODE>IOException</CODE>
     * @throws java.lang.Exception <CODE>Exception</CODE>
     * @return <CODE>FileObject</CODE> the generated file which is MBean class.
     */
    public abstract FileObject generateMBean(MBeanDO mbean)
            throws java.io.IOException, Exception;
    
    /**
     * Returns the MBean class.
     */
    public abstract JavaClass getMBeanClass();
    
    /**
     * Add the block close code to the StringBuffer.
     * @param sb <CODE>StringBuffer</CODE> the StringBuffer to complete
     */
    public static void closeBloc(StringBuffer sb) {
        sb.append("}\n");// NOI18N
    }
    
    /**
     * Add a new line code to the StringBuffer.
     * @param sb <CODE>StringBuffer</CODE> the StringBuffer to complete
     */
    public static void newLine(StringBuffer sb) {
        sb.append("\n");// NOI18N
    }
    
    public static void addManagementImport(Resource tgtRes){
        JavaModelPackage pkg = (JavaModelPackage)tgtRes.refImmediatePackage();
        
        // look for the import among all imports in the target file
        Iterator it = tgtRes.getImports().iterator();
        boolean found = false;
        while (it.hasNext()) {
            Import i = (Import) it.next();
            if (i.getName().equals("javax.management") && // NOI18N
                i.isStatic() == false &&
                i.isOnDemand() == true) { found = true; break;}
        }

        if (!found) // not found
            tgtRes.getImports().add(createManagementImport(pkg));
        
    }
    
    public static void addNeededImport(MBeanDO mbean, Resource mbeanRes) {
        boolean dateImport = typeIsUsed(mbean, WizardConstants.DATE_OBJ_NAME);
        if (dateImport)
            addImport(mbeanRes, WizardConstants.DATE_OBJ_FULLNAME);
        boolean objectNameImport = typeIsUsed(mbean, WizardConstants.OBJECTNAME_NAME);
        if (objectNameImport)
            addImport(mbeanRes, WizardConstants.OBJECTNAME_FULLNAME);
    }
    
    public static void addArraysImport(MBeanDO mbean, Resource mbeanRes) {
        boolean arraysImport = mbean.getType().equals(WizardConstants.MBEAN_DYNAMICMBEAN) &&
                (mbean.getOperations().size() > 0);
        if (arraysImport)
            addImport(mbeanRes, WizardConstants.ARRAYS_FULLNAME);
    }
    
    public static void addNeededImport(MBeanAttribute[] attributes, Resource mbeanRes) {
        boolean dateImport = attrTypeIsUsed(attributes, WizardConstants.DATE_OBJ_NAME);
        if (dateImport)
            addImport(mbeanRes, WizardConstants.DATE_OBJ_FULLNAME);
        boolean objectNameImport = attrTypeIsUsed(attributes, WizardConstants.OBJECTNAME_NAME);
        if (objectNameImport)
            addImport(mbeanRes, WizardConstants.OBJECTNAME_FULLNAME);
    }
    
    public static void addNeededImport(MBeanOperation[] operations, Resource mbeanRes) {
        boolean dateImport = opTypeIsUsed(operations, WizardConstants.DATE_OBJ_NAME);
        if (dateImport)
            addImport(mbeanRes, WizardConstants.DATE_OBJ_FULLNAME);
        boolean objectNameImport = opTypeIsUsed(operations, WizardConstants.OBJECTNAME_NAME);
        if (objectNameImport)
            addImport(mbeanRes, WizardConstants.OBJECTNAME_FULLNAME);
    }
    
    public static boolean attrTypeIsUsed(MBeanAttribute[] attributes, String type) {
        for (int i = 0; i < attributes.length; i ++) {
            if (type.equals(attributes[i].getTypeName())) 
                return true;
        }
        return false;
    }
    
    public static boolean opTypeIsUsed(MBeanOperation[] operations, String type) {
        for (int i = 0 ; i < operations.length; i ++) {
            if (type.equals(operations[i].getReturnTypeName())) 
                return true;
            for (int j = 0; j < operations[i].getParametersSize(); j++) {
                MBeanOperationParameter param = operations[i].getParameter(j);
                if (type.equals(param.getParamType())) 
                    return true;
            }
        }
        return false;
    }
        
    public static boolean typeIsUsed(MBeanDO mbean, String type) {
        List attrList = mbean.getAttributes();
        MBeanAttribute[] attributes = (MBeanAttribute[])
            attrList.toArray(new MBeanAttribute[attrList.size()]);

        List opList = mbean.getOperations();
        MBeanOperation[] operations = (MBeanOperation[])
            opList.toArray(new MBeanOperation[opList.size()]);
            
        return attrTypeIsUsed(attributes,type) || opTypeIsUsed(operations,type);
    }
    
    public static void addImport(Resource tgtRes, String fullTypeName){
        JavaModelPackage pkg = (JavaModelPackage)tgtRes.refImmediatePackage();
        
        // look for the import among all imports in the target file
        Iterator it = tgtRes.getImports().iterator();
        boolean found = false;
        while (it.hasNext()) {
            Import i = (Import) it.next();
            if (i.getName().equals(fullTypeName) &&
                i.isStatic() == false &&
                i.isOnDemand() == false) { found = true; break;}
        }

        if (!found) // not found
            tgtRes.getImports().add(createImport(pkg,fullTypeName));
        
    }
    
    public static Import createImport(JavaModelPackage pkg, String fullTypeName) {
        return pkg.getImport().createImport(fullTypeName,null, false, false);
    }
    
    public static Import createManagementImport(JavaModelPackage pkg) {
        return pkg.getImport().createImport("javax.management",null, false, true); // NOI18N
    }
    
    public static Type getType(JavaModelPackage pkg, String typeName) {
        if (typeName.endsWith("[]")) { // NOI18N
            org.netbeans.jmi.javamodel.Array array = pkg.getArray().resolveArray(
                    getType(pkg,
                    WizardHelpers.getFullTypeName(
                    typeName.substring(0,typeName.length() - 2)))); // NOI18N
            return array;
        } else
            return pkg.getType().resolve(typeName);
    }
    
    public static MultipartId getTypeRef(JavaModelPackage pkg, String typeName) {
        return pkg.getMultipartId().createMultipartId(
                    typeName,
                    null,
                    Collections.EMPTY_LIST);
    }
    
}
