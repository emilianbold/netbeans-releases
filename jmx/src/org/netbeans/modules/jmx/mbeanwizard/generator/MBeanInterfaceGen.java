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
import java.util.List;
import org.netbeans.jmi.javamodel.JavaClass;
import org.netbeans.jmi.javamodel.JavaModelPackage;
import org.netbeans.jmi.javamodel.Method;
import org.netbeans.jmi.javamodel.Parameter;
import org.netbeans.jmi.javamodel.Resource;
import org.netbeans.modules.javacore.api.JavaModel;
import org.netbeans.modules.jmx.Introspector;
import org.netbeans.modules.jmx.MBeanAttribute;
import org.netbeans.modules.jmx.MBeanDO;
import org.netbeans.modules.jmx.MBeanOperation;
import org.netbeans.modules.jmx.MBeanOperationException;
import org.netbeans.modules.jmx.MBeanOperationParameter;
import org.netbeans.modules.jmx.WizardConstants;
import org.netbeans.modules.jmx.WizardHelpers;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle;

/**
 * MBean Interface code generator class
 * @author tl156378
 */
public class MBeanInterfaceGen extends MBeanFileGenerator {
    
    /** Creates a new instance of MBeanInterfaceGen */
    public MBeanInterfaceGen() {
    }
    
    /**
     * Generates all the files for the new MBean Interface.
     * @param mbean <CODE>MBeanDO</CODE> the MBean to generate
     * @throws java.io.IOException <CODE>IOException</CODE>
     * @throws java.lang.Exception <CODE>Exception</CODE>
     * @return <CODE>FileObject</CODE> the generated file which is MBean class.
     */
    public FileObject generateMBean(MBeanDO mbean)
            throws java.io.IOException, Exception {
        FileObject createdFile = null;
        String mbeanName = mbean.getName();
        String mbeanIntfName = mbeanName + WizardConstants.MBEAN_ITF_SUFFIX;
        DataFolder mbeanFolder = mbean.getDataFolder();
        DataObject intfDObj = null;
        Resource intfRc = null;
        
        boolean rollback = false;
        JavaModel.getJavaRepository().beginTrans(true);
        try {
            DataObject intfTemplate = getInterfaceTemplate();
            intfDObj = intfTemplate.createFromTemplate( 
                    mbeanFolder, mbeanIntfName );
            FileObject intfFile = intfDObj.getPrimaryFile();
            intfRc = JavaModel.getResource(intfFile);
            JavaClass mbeanIntf = WizardHelpers.getJavaClass(intfRc,mbeanIntfName);
            
            if (!mbean.getPackageName().equals("")) // NOI18N
                addNeededImport(mbean, intfRc);
            updateDescription(mbean,mbeanIntf);
            List attrList = mbean.getAttributes();
            MBeanAttribute[] attributes = (MBeanAttribute[]) 
                    attrList.toArray(new MBeanAttribute[attrList.size()]);
            updateAttributesInIntf(mbeanIntf, attributes);
            List opList = mbean.getOperations();
            MBeanOperation[] operations = (MBeanOperation[]) 
                    opList.toArray(new MBeanOperation[opList.size()]);
            updateOperationsInIntf(mbeanIntf, operations);
            
            createdFile = intfFile;
        } catch (Exception e) {
            rollback = true;
            e.printStackTrace();
        } finally {
            JavaModel.getJavaRepository().endTrans(rollback);
        }
        
        rollback = false;
        JavaModel.getJavaRepository().beginTrans(true);
        try {
            if ((intfRc != null) && (mbean.getPackageName().equals(""))) // NOI18N
                addNeededImport(mbean, intfRc);
        } catch (Exception e) {
            rollback = true;
        } finally {
            JavaModel.getJavaRepository().endTrans(rollback);
        }
        return createdFile;
    }
    
    public JavaClass getMBeanClass() {
        return null;
    }

    // returns the MBean Interface file template
    private DataObject getInterfaceTemplate() throws Exception {
        /* get the template DataObject... */
        String templatePath = NbBundle.getMessage(Introspector.class,"PROP_MBeanInterfaceTemplate"); // NOI18N
        FileObject template = Repository.getDefault().getDefaultFileSystem()
                              .findResource(templatePath);
        return DataObject.find(template);
    }
    
    private void updateDescription(MBeanDO mbean,JavaClass mbeanIntf) {
        MessageFormat formDoc = new MessageFormat(mbeanIntf.getJavadocText());
        Object[] args = new Object[] { mbean.getDescription() };
        mbeanIntf.setJavadocText(formDoc.format(args));
    }
    
    public void updateAttributes(JavaClass mbeanIntf, MBeanAttribute[] attributes)
           throws java.io.IOException, Exception
    {
        boolean rollback = false;
        JavaModel.getJavaRepository().beginTrans(true);
        try {
            // add methods declaration in mbean interface
            if (!WizardHelpers.getPackageName(mbeanIntf.getName()).equals("")) // NOI18N
                addNeededImport(attributes, mbeanIntf.getResource());
            updateAttributesInIntf(mbeanIntf,attributes);
        } catch (Exception e) {
            rollback = true;
            e.printStackTrace();
        } finally {
            JavaModel.getJavaRepository().endTrans(rollback);
        }
        
        rollback = false;
        JavaModel.getJavaRepository().beginTrans(true);
        try {
            if (WizardHelpers.getPackageName(mbeanIntf.getName()).equals("")) // NOI18N
                addNeededImport(attributes, mbeanIntf.getResource());
        } catch (Exception e) {
            rollback = true;
        } finally {
            JavaModel.getJavaRepository().endTrans(rollback);
        }
    }
    
    private void updateAttributesInIntf(JavaClass mbeanIntf, MBeanAttribute[] attributes)
    throws java.io.IOException, Exception {
        for (int i = 0; i < attributes.length; i++) {
            if (attributes[i].isReadable())
                addGetAttrMethodDecl(mbeanIntf, attributes[i]);
            if (attributes[i].isWritable())
                addSetAttrMethodDecl(mbeanIntf, attributes[i]);
        }
    }
    
    private void addGetAttrMethodDecl(JavaClass tgtIntf, MBeanAttribute attribute) {
        JavaModelPackage pkg = (JavaModelPackage) tgtIntf.refImmediatePackage();
        
        String prefix;
        if (attribute.getIsMethodExits())
            prefix = "is"; // NOI18N
        else
            prefix = "get"; // NOI18N
        
        Method method = pkg.getMethod().createMethod(
                prefix + attribute.getName(),
                Collections.EMPTY_LIST,
                Modifier.PUBLIC,
                "Get " + attribute.getDescription(), // NOI18N
                null, // jvadoc
                null, // object body
                null, // string body
                Collections.EMPTY_LIST, // type params
                Collections.EMPTY_LIST, // parameters
                Collections.EMPTY_LIST, // exceptions
                getTypeRef(pkg, attribute.getTypeName()), // type
                0);
        tgtIntf.getFeatures().add(method);
    }
    
    private void addSetAttrMethodDecl(JavaClass tgtIntf, MBeanAttribute attribute) {
        JavaModelPackage pkg = (JavaModelPackage) tgtIntf.refImmediatePackage();
        
        ArrayList params = new ArrayList();
        Parameter attrType = pkg.getParameter().createParameter(
                "value", // NOI18N
                Collections.EMPTY_LIST, // annotations
                false, // is final
                getTypeRef(pkg, attribute.getTypeName()), // typename
                0, // dimCount
                false);
        params.add(attrType);
        
        Method method = pkg.getMethod().createMethod(
                "set" + attribute.getName(), // NOI18N
                Collections.EMPTY_LIST, // annotations
                Modifier.PUBLIC,
                "Set " + attribute.getDescription(), // NOI18N
                null, // jvadoc
                null, // object body
                null, // string body
                Collections.EMPTY_LIST, // type params
                params, // parameters
                Collections.EMPTY_LIST, // exceptions
                getTypeRef(pkg, WizardConstants.VOID_NAME), // type
                0);
        tgtIntf.getFeatures().add(method);
    }
    
    public void updateOperations(JavaClass mbeanIntf, MBeanOperation[] operations)
           throws java.io.IOException, Exception
    {
        boolean rollback = false;
        JavaModel.getJavaRepository().beginTrans(true);
        try {
            // add methods declaration in MBean interface
            if (!WizardHelpers.getPackageName(mbeanIntf.getName()).equals("")) // NOI18N
                addNeededImport(operations, mbeanIntf.getResource());
            updateOperationsInIntf(mbeanIntf,operations);
        } catch (Exception e) {
            rollback = true;
            e.printStackTrace();
        } finally {
            JavaModel.getJavaRepository().endTrans(rollback);
        }
        
        rollback = false;
        JavaModel.getJavaRepository().beginTrans(true);
        try {
            if (WizardHelpers.getPackageName(mbeanIntf.getName()).equals("")) // NOI18N
                addNeededImport(operations, mbeanIntf.getResource());
        } catch (Exception e) {
            rollback = true;
        } finally {
            JavaModel.getJavaRepository().endTrans(rollback);
        }
    }
    
    private void updateOperationsInIntf(JavaClass mbeanIntf, MBeanOperation[] operations)
    throws java.io.IOException, Exception {
        for (int i = 0; i < operations.length; i++) {
            addOpMethod(mbeanIntf, operations[i], true);
        }
    }
    
    private void addOpMethod(JavaClass tgtClass, MBeanOperation operation, 
            boolean isDeclaration) {
        JavaModelPackage pkg = (JavaModelPackage)tgtClass.refImmediatePackage();
        
        String methodBody = "\n// add your own implementation\n\n"; // NOI18N
        
        if  (!operation.getReturnTypeName().equals(WizardConstants.VOID_NAME)) {
            methodBody += "return " +  // NOI18N
                WizardHelpers.getDefaultValue(operation.getReturnTypeName()) + 
                ";\n"; // NOI18N
        }
        
        StringBuffer doc = new StringBuffer();
        doc.append(operation.getDescription() + "\n"); // NOI18N
        
        ArrayList params = new ArrayList();
        for (int i = 0; i < operation.getParametersSize(); i ++) {
            MBeanOperationParameter param = operation.getParameter(i);
            Parameter paramType = pkg.getParameter().createParameter(
                    param.getParamName(),
                    Collections.EMPTY_LIST, // annotations
                    false, // is final
                    getTypeRef(pkg, param.getParamType()), // typename
                    0, // dimCount
                    false);
            params.add(paramType);
            doc.append("@param " + param.getParamName() + " <code>" +  // NOI18N
                    param.getParamType() + "</code> " +  // NOI18N
                    param.getParamDescription() +"\n"); // NOI18N
        }
        
        ArrayList exceptions = new ArrayList();
        for (int i = 0; i < operation.getExceptionsSize(); i ++) {
            MBeanOperationException exception = operation.getException(i);
            exceptions.add(getTypeRef(pkg, exception.getExceptionClass()));
            doc.append("@throws <code>" + exception.getExceptionClass() + "</code> " + // NOI18N 
                    exception.getExceptionDescription() + "\n"); // NOI18N
        }
        
        if (!operation.getReturnTypeName().equals(WizardConstants.VOID_NAME)) {
            doc.append("@return <code>" + operation.getReturnTypeName() +  // NOI18N
                    "</code>\n"); // NOI18N
        }
        
        Method method = pkg.getMethod().createMethod(
                operation.getName(),
                Collections.EMPTY_LIST,
                Modifier.PUBLIC,
                doc.toString(), // javadoc text
                null, // jvadoc
                null, // object body
                isDeclaration ? null : methodBody, // string body
                Collections.EMPTY_LIST, // type params
                params, // parameters
                exceptions, // exceptions
                getTypeRef(pkg, operation.getReturnTypeName()), // type
                0);
        tgtClass.getFeatures().add(method);

    }
}
