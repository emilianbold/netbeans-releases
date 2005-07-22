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
        sb.append("}\n");
    }
    
    /**
     * Add a new line code to the StringBuffer.
     * @param sb <CODE>StringBuffer</CODE> the StringBuffer to complete
     */
    public static void newLine(StringBuffer sb) {
        sb.append("\n");
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
    }
    
    public static boolean typeIsUsed(MBeanDO mbean, String type) {
        for (Iterator<MBeanAttribute> it = mbean.getAttributes().iterator(); it.hasNext();) {
            MBeanAttribute attribute = it.next();
            if (WizardConstants.DATE_OBJ_NAME.equals(attribute.getTypeName())) 
                return true;
        }
        for (Iterator<MBeanOperation> it = mbean.getOperations().iterator(); it.hasNext();) {
            MBeanOperation operation = it.next();
            if (WizardConstants.DATE_OBJ_NAME.equals(operation.getReturnTypeName())) 
                return true;
            for (int i = 0; i < operation.getParametersSize(); i++) {
                MBeanOperationParameter param = operation.getParameter(i);
                if (WizardConstants.DATE_OBJ_NAME.equals(param.getParamType())) 
                    return true;
            }
        }
        return false;
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
        return pkg.getType().resolve(typeName);
    }
    
    public static MultipartId getTypeRef(JavaModelPackage pkg, String typeName) {
        return pkg.getMultipartId().createMultipartId(
                    typeName,
                    null,
                    Collections.EMPTY_LIST);
    }
    
}
