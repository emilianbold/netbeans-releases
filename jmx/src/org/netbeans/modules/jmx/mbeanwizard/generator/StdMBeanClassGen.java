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
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.jmi.reflect.RefClass;
import javax.jmi.reflect.RefException;
import javax.jmi.reflect.RefFeatured;
import javax.jmi.reflect.RefObject;
import javax.jmi.reflect.RefPackage;
import org.netbeans.jmi.javamodel.ClassDefinition;
import org.netbeans.jmi.javamodel.Element;
import org.netbeans.jmi.javamodel.ElementPartKind;
import org.netbeans.jmi.javamodel.Field;
import org.netbeans.jmi.javamodel.JavaClass;
import org.netbeans.jmi.javamodel.JavaDoc;
import org.netbeans.jmi.javamodel.JavaModelPackage;
import org.netbeans.jmi.javamodel.Method;
import org.netbeans.jmi.javamodel.MultipartId;
import org.netbeans.jmi.javamodel.Parameter;
import org.netbeans.jmi.javamodel.Resource;
import org.netbeans.jmi.javamodel.Constructor;
import org.netbeans.jmi.javamodel.TypeParameter;
import org.netbeans.jmi.javamodel.ParameterClass;
import org.netbeans.modules.javacore.api.JavaModel;
import org.netbeans.modules.jmx.Introspector;
import org.netbeans.modules.jmx.MBeanAttribute;
import org.netbeans.modules.jmx.MBeanDO;
import org.netbeans.modules.jmx.MBeanNotification;
import org.netbeans.modules.jmx.MBeanOperation;
import org.netbeans.modules.jmx.MBeanOperationException;
import org.netbeans.modules.jmx.MBeanOperationParameter;
import org.netbeans.modules.jmx.WizardConstants;
import org.netbeans.modules.jmx.WizardHelpers;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;

/**
 * Standard MBean code generator class
 * @author tl156378
 */
public class StdMBeanClassGen extends MBeanFileGenerator {
    
    private static final String OPERATION_SIGNATURE = 
        "MBeanParameterInfo[] params = info.getSignature();\n" + // NOI18N
        "String[] signature = new String[params.length];\n" + // NOI18N
        "for (int i = 0; i < params.length; i++)\n" + // NOI18N
        "    signature[i] = params[i].getType();\n"; // NOI18N
    
    private JavaClass mbeanClass;
    
    /** Creates a new instance of StdMBeanClassGen */
    public StdMBeanClassGen() {
    }
    
    /**
     * Generates all the files for the new Standard MBean.
     * @param mbean <CODE>MBeanDO</CODE> the MBean to generate
     * @throws java.io.IOException <CODE>IOException</CODE>
     * @throws java.lang.Exception <CODE>Exception</CODE>
     * @return <CODE>FileObject</CODE> the generated file which is MBean class.
     */
    public FileObject generateMBean(MBeanDO mbean)
            throws java.io.IOException, Exception {
        FileObject createdFile = null;
        String mbeanName = mbean.getName();
        DataFolder mbeanFolder = mbean.getDataFolder();
        DataObject mbeanDObj = null;
        JavaModel.getJavaRepository().beginTrans(true);
        try {
            DataObject dTemplate = mbean.getTemplate();                
            mbeanDObj = dTemplate.createFromTemplate( 
                    mbeanFolder, mbeanName );
            FileObject mbeanFile = mbeanDObj.getPrimaryFile();
            Resource mbeanRc = JavaModel.getResource(mbeanFile);
            mbeanClass = WizardHelpers.getJavaClass(mbeanRc,mbeanName);
            
            addNeededImport(mbean, mbeanRc);
            updateDescription(mbean,mbeanClass);
            updateMBeanType(mbean,mbeanClass);
            createAttributes(mbeanClass, mbeanRc, mbean);
            createOperations(mbeanClass, mbeanRc, mbean);
            
            MBeanInterfaceGen intfGen = new MBeanInterfaceGen();
            intfGen.generateMBean(mbean);
            
            createdFile = mbeanFile;
        } finally {
            JavaModel.getJavaRepository().endTrans();
        }
        return createdFile;
    }
    
    public JavaClass getMBeanClass() {
        return mbeanClass;
    }
    
    private void updateMBeanType(MBeanDO mbean, JavaClass tgtClass) {
        JavaModelPackage pkg = (JavaModelPackage) tgtClass.refImmediatePackage();
        //get ref to methods
        List params = new ArrayList();
        params.add(getType(pkg, "javax.management.MBeanInfo")); // NOI18N
        Method getDescMInfo = tgtClass.getMethod("getDescription", params,false); // NOI18N
        
        params = new ArrayList();
        params.add(getType(pkg, "javax.management.MBeanAttributeInfo")); // NOI18N
        Method getDescMAttrInfo = tgtClass.getMethod("getDescription", params,false); // NOI18N
        
        params = new ArrayList();
        params.add(getType(pkg, "javax.management.MBeanOperationInfo")); // NOI18N
        params.add(getType(pkg, "javax.management.MBeanParameterInfo")); // NOI18N
        params.add(getType(pkg, "int")); // NOI18N
        Method getDescMParam = tgtClass.getMethod("getDescription", params,false); // NOI18N
        
        Method getNameMParam = tgtClass.getMethod("getParameterName", params,false); // NOI18N
        
        params = new ArrayList();
        params.add(getType(pkg, "javax.management.MBeanOperationInfo")); // NOI18N
        Method getDescMOpInfo = tgtClass.getMethod("getDescription", params,false); // NOI18N
        
        Method getMInfo = tgtClass.getMethod("getMBeanInfo", new ArrayList(),false); // NOI18N
        
        Method getNotifInfo = tgtClass.getMethod("getNotificationInfo", new ArrayList(),false); // NOI18N
        
        if (WizardConstants.MBEAN_EXTENDED.equals(mbean.getType())) {
            // add super class
            tgtClass.setSuperClassName(getTypeRef(pkg, "javax.management.StandardMBean")); // NOI18N
            
            //update constructor and create wrapped object ref field
            if (mbean.isWrapppedClass()) {
                // remove throws of constructor and updates body text and parameters
                Constructor construct = tgtClass.getConstructor(new ArrayList(),false);
                ParameterClass paramClass = (ParameterClass)
                    Lookup.getDefault().lookup(ParameterClass.class);
                Parameter param = paramClass.createParameter(
                        "theRef", // NOI18N
                        Collections.EMPTY_LIST,
                        false,
                        getTypeRef(pkg,mbean.getWrappedClassName()),
                        0,
                        false);
                construct.getParameters().add(param);
                construct.getExceptionNames().clear();
                construct.setBodyText("this.theRef = theRef;\n"); // NOI18N
                
                //create ref field
                Field refField = pkg.getField().createField(
                    "theRef", // NOI18N
                    Collections.EMPTY_LIST,
                    Modifier.PRIVATE, 
                    null,
                    null, false, 
                    getTypeRef(pkg, mbean.getWrappedClassName()),
                    0, null, null);
                tgtClass.getFeatures().add(0,refField);
            }
            
            // update getDescription(MBeanInfo) method body
            MessageFormat formBody = new MessageFormat(getDescMInfo.getBodyText());
            Object[] args = new Object[] { mbean.getDescription() };  
            getDescMInfo.setBodyText(formBody.format(args).substring(1));
            
            // update getDescription(MBeanAttributeInfo) method body
            formBody = new MessageFormat(getDescMAttrInfo.getBodyText());
            args = new Object[] { getAttDescCode(mbean) };  
            getDescMAttrInfo.setBodyText(formBody.format(args).substring(1));
            
            // update getDescription(MBeanOperationInfo,MBeanParameterInfo,int) method body
            formBody = new MessageFormat(getDescMParam.getBodyText());
            args = new Object[] { getParamDescCode(mbean,true) };  
            getDescMParam.setBodyText(formBody.format(args).substring(1));
            
            // update getParameterName(MBeanOperationInfo,MBeanParameterInfo,
            // int) method body
            formBody = new MessageFormat(getNameMParam.getBodyText());
            args = new Object[] { getParamDescCode(mbean,false) };  
            getNameMParam.setBodyText(formBody.format(args).substring(1));
            
            // update getDescription(MBeanOperationInfo) method body
            formBody = new MessageFormat(getDescMOpInfo.getBodyText());
            args = new Object[] { getOpDescCode(tgtClass,mbean) };  
            getDescMOpInfo.setBodyText(formBody.format(args).substring(1));
        } else {
            // remove throws of constructor and clear body text
            Constructor construct = tgtClass.getConstructor(new ArrayList(),false);
            construct.getExceptionNames().clear();
            construct.setBodyText(""); // NOI18N
            // remove all get informations methods of StandardMBean class 
            getDescMInfo.refDelete();
            getDescMAttrInfo.refDelete();
            getDescMParam.refDelete();
            getNameMParam.refDelete();
            getDescMOpInfo.refDelete();
            getMInfo.refDelete();
            getNotifInfo.refDelete();
        }
        
    }
    
    private String getOpDescCode(JavaClass mbeanClass, MBeanDO mbean) {
        List opList = mbean.getOperations();
        MBeanOperation[] operations = (MBeanOperation[])
            opList.toArray(new MBeanOperation[opList.size()]);
        StringBuffer content = new StringBuffer();
        if (operations.length > 0) {
            content.append(OPERATION_SIGNATURE);
            content.append(METHOD_SIGNATURE_DEF);
            addImport(mbeanClass.getResource(),"java.util.Arrays"); // NOI18N
        }
        MessageFormat formOperation = 
                new MessageFormat(OPERATION_CHECK_PATTERN);
        for (int i = 0; i < operations.length; i++) {
            content.append(METHOD_SIGNATURE);
            for (int j = 0; j < operations[i].getParametersSize(); j ++) {
                MBeanOperationParameter param = operations[i].getParameter(j);
                content.append(WizardHelpers.getFullTypeNameCode(
                        param.getParamType()));
                if (j < operations[i].getParametersSize() - 1) {
                    content.append(","); // NOI18N
                }
                content.append("\n"); // NOI18N
            }
            content.append("};\n"); // NOI18N
            String descCode = "     description = \"" + // NOI18N 
                    operations[i].getDescription() + "\";"; // NOI18N
            Object[] arg = { operations[i].getName(), descCode, "info.getName()" }; // NOI18N
            content.append(formOperation.format(arg));
        }
        
        return content.toString();
    }
    
    private String getParamDescCode(MBeanDO mbean, boolean isGetDescription) {
        List opList = mbean.getOperations();
        MBeanOperation[] operations = (MBeanOperation[])
            opList.toArray(new MBeanOperation[opList.size()]);
        StringBuffer content = new StringBuffer();
        for (int i = 0; i < operations.length ; i++) {
            if (i!=0) {
                content.append("} else "); // NOI18N
            }
            content.append("if (op.getName().equals(\""); // NOI18N
            content.append(operations[i].getName());
            content.append("\")) {\n"); // NOI18N
            content.append("           switch (sequence) {\n"); // NOI18N
            for (int j = 0; j < operations[i].getParametersSize(); j++) {
                content.append("     case " + j + ": return \""); // NOI18N
                String stringToReturn = null;
                MBeanOperationParameter param = operations[i].getParameter(j);
                if (isGetDescription) {
                    stringToReturn = param.getParamDescription();
                } else {
                    stringToReturn = param.getParamName();
                }
                content.append(stringToReturn);
                content.append("\";\n"); // NOI18N
            }
            content.append("     default : return null;\n"); // NOI18N
            content.append("   }\n"); // NOI18N
        }
        if (operations.length != 0) {
            content.append("}\n"); // NOI18N
        }
        return content.toString();
    }
    
    private String getAttDescCode(MBeanDO mbean) {
        List attrList = mbean.getAttributes();
        MBeanAttribute[] attributes = (MBeanAttribute[])
            attrList.toArray(new MBeanAttribute[attrList.size()]);
        StringBuffer content = new StringBuffer();
        for (int i = 0; i < attributes.length ; i++) {
            if (i!=0)
                content.append("} else "); // NOI18N
            content.append("if (info.getName().equals(\""); // NOI18N
            content.append(attributes[i].getName());
            content.append("\")) {\n"); // NOI18N
            content.append("     description = \""); // NOI18N
            content.append(attributes[i].getDescription() + "\";\n"); // NOI18N
            if (i == attributes.length - 1) {
                content.append("}\n"); // NOI18N
            }
        }
        return content.toString();
    }
    
    private void updateDescription(MBeanDO mbean,JavaClass mbeanClass) {
        MessageFormat formDoc = new MessageFormat(mbeanClass.getJavadocText());
        Object[] args ;
        if (WizardConstants.MBEAN_EXTENDED.equals(mbean.getType()))
            args = new Object[] { mbean.getDescription() , 
                    "Dynamic MBean based on StandardMBean\n"}; // NOI18N
        else
            args = new Object[] { mbean.getDescription(), "" }; // NOI18N     
        mbeanClass.setJavadocText(formDoc.format(args));
    }
    
    private void createAttributes(JavaClass mbeanClass, Resource mbeanRes, 
            MBeanDO mbean)
           throws java.io.IOException, Exception
    {
        List attrList = mbean.getAttributes();
        MBeanAttribute[] attributes = (MBeanAttribute[])
            attrList.toArray(new MBeanAttribute[attrList.size()]);
        updateAttributesInClass(mbeanClass, mbeanRes, attributes);
    }
    
    private void createOperations(JavaClass mbeanClass, Resource mbeanRes, 
            MBeanDO mbean)
           throws java.io.IOException, Exception
    {
        List opList = mbean.getOperations();
        MBeanOperation[] operations = (MBeanOperation[])
        opList.toArray(new MBeanOperation[opList.size()]);
        updateOperationsInClass(mbeanClass, mbeanRes, operations);
    }
    
    private void updateAttributesInClass(JavaClass mbeanClass, Resource mbeanRes, 
            MBeanAttribute[] attributes)
           throws java.io.IOException, Exception
    {
         // add methods and fields in mbean class
        addManagementImport(mbeanRes);
        for (int i = 0; i < attributes.length; i++) {
            if (attributes[i].getTypeName().equals(
                    WizardConstants.DATE_OBJ_NAME))
                addImport(mbeanRes,WizardConstants.DATE_OBJ_FULLNAME);
        }
        for (int i = 0; i < attributes.length; i++) {
            boolean hasGetter = (attributes[i].getGetMethodExits() ||
                    attributes[i].getIsMethodExits()) && !attributes[i].isWrapped();
            boolean hasSetter = (attributes[i].getSetMethodExits() && 
                    !attributes[i].isWrapped());
            if (attributes[i].isReadable() && !hasGetter)
                addGetAttrMethod(mbeanClass, attributes[i]);
            if (attributes[i].isWritable() && !hasSetter)
                addSetAttrMethod(mbeanClass, attributes[i]);
        }
        for (int i = attributes.length - 1; i >= 0; i--) {
            if (!attributes[i].getIsMethodExits() && !attributes[i].getGetMethodExits() &&
                    !attributes[i].getSetMethodExits() && !attributes[i].isWrapped())
                addAttrField(mbeanClass, attributes[i]);
        }
    }
    
    public void updateAttributes(JavaClass mbeanClass, Resource mbeanRes, 
            MBeanAttribute[] attributes)
           throws java.io.IOException, Exception
    {
        JavaModel.getJavaRepository().beginTrans(true);
        try {
            for (int i = 0; i < attributes.length; i++) {
                if (attributes[i].getTypeName().equals(
                        WizardConstants.DATE_OBJ_NAME))
                    addImport(mbeanRes,WizardConstants.DATE_OBJ_FULLNAME);
            }
            updateAttributesInClass(mbeanClass,mbeanRes,attributes);
            
            // add methods declaration in mbean interface
            JavaClass mbeanIntf = Introspector.getStandardMBeanInterface(mbeanClass);
            JavaModelPackage pkg = (JavaModelPackage) mbeanIntf.refImmediatePackage();
            mbeanIntf = (JavaClass) pkg.getJavaClass().resolve(mbeanIntf.getName());
            MBeanInterfaceGen intfGen = new MBeanInterfaceGen();
            intfGen.updateAttributes(mbeanIntf, attributes);
        } finally {
            JavaModel.getJavaRepository().endTrans();
        }
    }
    
    private void addGetAttrMethod(JavaClass tgtClass, MBeanAttribute attribute) {
        JavaModelPackage pkg = (JavaModelPackage)tgtClass.refImmediatePackage();
        
        String methodBody = "return " +  // NOI18N
                WizardHelpers.forceFirstLetterLowerCase(attribute.getName()) + 
                ";\n"; // NOI18N
        
        String prefix = "get";
        
        if (attribute.isWrapped()) {
            if (attribute.getIsMethodExits())
                prefix = "is";
            methodBody = "return theRef." + prefix + attribute.getName() + "();\n"; // NOI18N
        } else if (attribute.getSetMethodExits())
            methodBody = "return " +  // NOI18N
                WizardHelpers.getDefaultValue(attribute.getTypeName()) + 
                ";\n"; // NOI18N
        
        Method method = pkg.getMethod().createMethod(
                prefix + attribute.getName(),
                Collections.EMPTY_LIST,
                Modifier.PUBLIC,
                "Get " + attribute.getDescription(), // NOI18N
                null, // jvadoc
                null, // object body
                methodBody, // string body
                Collections.EMPTY_LIST, // type params
                Collections.EMPTY_LIST, // parameters
                Collections.EMPTY_LIST, // exceptions
                getTypeRef(pkg, attribute.getTypeName()), // type
                0);
        tgtClass.getFeatures().add(method);

    }   
    
    private void addSetAttrMethod(JavaClass tgtClass, MBeanAttribute attribute) {
        JavaModelPackage pkg = (JavaModelPackage)tgtClass.refImmediatePackage();
        
        String methodBody = 
                WizardHelpers.forceFirstLetterLowerCase(attribute.getName()) + 
                " = value;\n"; // NOI18N
        
        if (attribute.isWrapped()) {
            methodBody = "theRef.set" + // NOI18N
                    WizardHelpers.capitalizeFirstLetter(attribute.getName()) + 
                    "(value);\n"; // NOI18N
        } else if (attribute.getGetMethodExits() || attribute.getIsMethodExits())
            methodBody = "//TODO add your own implementation.\n"; // NOI18N
        
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
                Collections.EMPTY_LIST,
                Modifier.PUBLIC,
                "Set " + attribute.getDescription(), // NOI18N
                null, // jvadoc
                null, // object body
                methodBody, // string body
                Collections.EMPTY_LIST, // type params
                params, // parameters
                Collections.EMPTY_LIST, // exceptions
                getTypeRef(pkg, WizardConstants.VOID_NAME), // type
                0);
        tgtClass.getFeatures().add(method);

    } 
    
    private static void addAttrField(JavaClass tgtClass, MBeanAttribute attribute) {
        JavaModelPackage pkg = (JavaModelPackage)tgtClass.refImmediatePackage();
        
        Field attrField = pkg.getField().createField(
                    WizardHelpers.forceFirstLetterLowerCase(attribute.getName()),
                    Collections.EMPTY_LIST,
                    Modifier.PRIVATE, 
                    "Attribute : " + attribute.getName(), // NOI18N
                    null, false, 
                    getTypeRef(pkg, attribute.getTypeName()),
                    0, null, null);
        tgtClass.getFeatures().add(0,attrField);
    }
    
    private void updateOperationsInClass(JavaClass mbeanClass, Resource mbeanRes, 
            MBeanOperation[] operations)
           throws java.io.IOException, Exception
    {
        // add methods in mbean class
        addManagementImport(mbeanRes);
        for (int i = 0; i < operations.length; i++) {
            if (operations[i].getReturnTypeName().equals(
                    WizardConstants.DATE_OBJ_NAME))
                addImport(mbeanRes,WizardConstants.DATE_OBJ_FULLNAME);
            for (int j = 0; j < operations[i].getParametersSize(); j++) {
                MBeanOperationParameter param = operations[i].getParameter(j);
                if (param.getParamType().equals(
                        WizardConstants.DATE_OBJ_NAME))
                    addImport(mbeanRes,WizardConstants.DATE_OBJ_FULLNAME);
            }
        }
        for (int i = 0; i < operations.length; i++) {
            if (!operations[i].isMethodExists() && !operations[i].isWrapped())
                addOpMethod(mbeanClass, operations[i], false);
        }
    }
    
    public void updateOperations(JavaClass mbeanClass, Resource mbeanRes, 
            MBeanOperation[] operations)
           throws java.io.IOException, Exception
    {
        JavaModel.getJavaRepository().beginTrans(true);
        try {
            for (int i = 0; i < operations.length; i++) {
                if (operations[i].getReturnTypeName().equals(
                        WizardConstants.DATE_OBJ_NAME))
                    addImport(mbeanRes,WizardConstants.DATE_OBJ_FULLNAME);
                for (int j = 0; j < operations[i].getParametersSize(); j++) {
                    MBeanOperationParameter param = operations[i].getParameter(j);
                    if (param.getParamType().equals(
                        WizardConstants.DATE_OBJ_NAME))
                        addImport(mbeanRes,WizardConstants.DATE_OBJ_FULLNAME);
                }
            }
            updateOperationsInClass(mbeanClass, mbeanRes, operations);
            
            // add methods declaration in MBean interface
            JavaClass mbeanIntf = Introspector.getStandardMBeanInterface(mbeanClass);
            JavaModelPackage pkg = (JavaModelPackage) mbeanIntf.refImmediatePackage();
            mbeanIntf = (JavaClass) pkg.getJavaClass().resolve(mbeanIntf.getName());
            MBeanInterfaceGen intfGen = new MBeanInterfaceGen();
            intfGen.updateOperations(mbeanIntf, operations);
        } finally {
            JavaModel.getJavaRepository().endTrans();
        }
    }
    
    private void addOpMethod(JavaClass tgtClass, MBeanOperation operation, 
            boolean isDeclaration) {
        JavaModelPackage pkg = (JavaModelPackage)tgtClass.refImmediatePackage();
        
        String methodBody = "\n// add your own implementation\n\n"; // NOI18N
        
        if (operation.isWrapped()) {
            StringBuffer body = new StringBuffer();
            body.append("theRef." + operation.getName() + "("); // NOI18N
            for (int i = 0; i < operation.getParametersSize(); i ++) {
                MBeanOperationParameter param = operation.getParameter(i);
                body.append(param.getParamName());
                if (i < operation.getParametersSize() - 1)
                    body.append(","); // NOI18N
            }
            body.append(");\n"); // NOI18N
        } else if  (!operation.getReturnTypeName().equals(WizardConstants.VOID_NAME)) {
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
