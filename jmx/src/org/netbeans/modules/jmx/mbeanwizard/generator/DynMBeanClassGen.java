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
import org.netbeans.jmi.javamodel.Field;
import org.netbeans.jmi.javamodel.JavaClass;
import org.netbeans.jmi.javamodel.JavaModelPackage;
import org.netbeans.jmi.javamodel.Method;
import org.netbeans.jmi.javamodel.Parameter;
import org.netbeans.jmi.javamodel.Resource;
import org.netbeans.jmi.javamodel.Constructor;
import org.netbeans.modules.javacore.api.JavaModel;
import org.netbeans.modules.jmx.Introspector;
import org.netbeans.modules.jmx.Introspector.AttributeMethods;
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
 * Dynamic MBean code generator class
 * @author tl156378
 */
public class DynMBeanClassGen extends MBeanFileGenerator {
    
    /** Creates a new instance of DynMBeanClassGen */
    public DynMBeanClassGen() {
    }
    
    // {0} = attribute name to check
    // {1} = attribute name 
    // {2} = code to execute for attribute
    // {3} = return statement
    private static final String CHECK_ATTR_NAME_PATTERN = 
        "if ({0}.equals(\"{1}\")) '{'\n\n" + // NOI18N 
        "   {2}\n\n{3}" + // NOI18N 
        "'}'"; // NOI18N 

    // {0} = attribute name
    // {1} = return or set
    private static final String COMMENT_ATTR_VALUE_PATTERN = 
        "//TODO {1} value of {0} attribute"; // NOI18N 

    // {0} = attribute name
    private static final String THROW_ATTR_VALUE_PATTERN = 
        "throw new MBeanException(\n" + // NOI18N 
        "    new IllegalArgumentException(\"{0} is read-only.\"));"; // NOI18N 
    
    private static final String COMMENT_INVOKE = 
        "\n" + // NOI18N 
        "     //TODO add your code here\n\n" + // NOI18N 
        "     return "; // NOI18N 
    
    // {0} = attribute name
    // {1} = attribute type
    // {2} = attribute description
    // {3} = isReadable
    // {4} = isWritable
    // {5} = isIs
    private static final String MBEAN_ATT_INFO_PATTERN = new String(
      "    new MBeanAttributeInfo(\"{0}\",\n" +
      "                           {1},\n" +
      "                           \"{2}\",\n" +
      "                           {3},\n" +
      "                           {4},\n" +
      "                           {5})");
    
    // {0} = operation name
    // {1} = operation desc
    // {2} = operation type full name
    // {3} = operation index 
    private static final String MBEAN_OPERATION_INFO_PATTERN = new String(
      "    new MBeanOperationInfo(\"{0}\",\n" +
      "                           \"{1}\",\n" +
      "                           op{3}Params,\n" +
      "                           {2},\n" +
      "                           MBeanOperationInfo.ACTION)");
    
    // {0} = operation index
    private static final String MBEAN_PARAMS_INFO_PATTERN = new String(
      "       MBeanParameterInfo[] op{0}Params = new MBeanParameterInfo[] '{'\n");
    
    // {0} = param name
    // {1} = param type full name
    // {2} = param desc
    private static final String MBEAN_PARAM_INFO_PATTERN = new String(
      "           new MBeanParameterInfo(\"{0}\",{1},\"{2}\")");
    
    private JavaClass mbeanClass;
    
    /**
     * Generates all the files for the new Dynamic MBean.
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
            DataObject dTemplate = getDynTemplate();                
            mbeanDObj = dTemplate.createFromTemplate( 
                    mbeanFolder, mbeanName );
            FileObject mbeanFile = mbeanDObj.getPrimaryFile();
            Resource mbeanRc = JavaModel.getResource(mbeanFile);
            mbeanClass = WizardHelpers.getJavaClass(mbeanRc,mbeanName);
            
            addNeededImport(mbean, mbeanRc);
            updateDescription(mbean,mbeanClass);
            updateGetAttr(mbean,mbeanClass);
            updateSetAttr(mbean,mbeanClass);
            updateInvoke(mbean,mbeanClass);
            updateMBeanInfo(mbean,mbeanClass);
            
            // generation of DynamicSupport file
            DataObject dynSupportTemplate = getDynSupportTemplate();                
            DataObject dynSupportDObj = dynSupportTemplate.createFromTemplate( 
                    mbeanFolder, mbeanName + WizardConstants.MBEAN_SUPPORT_SUFFIX );
            
            createdFile = mbeanFile;
        } finally {
            JavaModel.getJavaRepository().endTrans();
        }
        return createdFile;
    }
    
    public JavaClass getMBeanClass() {
        return mbeanClass;
    }

    // returns the Dynamic MBean file template
    private DataObject getDynTemplate() throws Exception {
        /* get the template DataObject... */
        String templatePath = NbBundle.getMessage(Introspector.class,"PROP_DynamicMBeanTemplate"); // NOI18N
        FileObject template = Repository.getDefault().getDefaultFileSystem()
                              .findResource(templatePath);
        return DataObject.find(template);
    }

    // returns the Dynamic Support MBean file template
    private DataObject getDynSupportTemplate() throws Exception {
        /* get the template DataObject... */
        String templatePath = NbBundle.getMessage(Introspector.class,"PROP_DynamicSupportMBeanTemplate"); // NOI18N
        FileObject template = Repository.getDefault().getDefaultFileSystem()
                              .findResource(templatePath);
        return DataObject.find(template);
    }
    
    private void updateGetAttr(MBeanDO mbean, JavaClass mbeanClass) {
        JavaModelPackage pkg = (JavaModelPackage) mbeanClass.refImmediatePackage();
        
        List params = new ArrayList();
        params.add(getType(pkg, "java.lang.String")); // NOI18N
        Method getAttr = mbeanClass.getMethod("getAttribute", params,false); // NOI18N 
        
        List attrList = mbean.getAttributes();
        MBeanAttribute[] attributes = (MBeanAttribute[])
            attrList.toArray(new MBeanAttribute[attrList.size()]);
        StringBuffer content = new StringBuffer();
        content.append("\n");
        MessageFormat formCheckAttr = new MessageFormat(CHECK_ATTR_NAME_PATTERN);
        MessageFormat formAttrComment = new MessageFormat(COMMENT_ATTR_VALUE_PATTERN);
        Object[] args;
        for (int i = 0; i < attributes.length ; i++) {
            args = new Object[] { "attributeName" , attributes[i].getName(), // NOI18N 
                 formAttrComment.format(new Object[] {attributes[i].getName(),"return"}), // NOI18N 
                 "return " + WizardHelpers.getDefaultValue( // NOI18N
                         attributes[i].getTypeName()) + ";\n" // NOI18N 
            };
            content.append(formCheckAttr.format(args) + "\n\n"); // NOI18N 
        }
        getAttr.setBodyText(content.toString() + getAttr.getBodyText().substring(1));
    }
    
    private void updateSetAttr(MBeanDO mbean, JavaClass mbeanClass) {
        JavaModelPackage pkg = (JavaModelPackage) mbeanClass.refImmediatePackage();
        
        List params = new ArrayList();
        params.add(getType(pkg, "javax.management.Attribute")); // NOI18N
        Method setAttr = mbeanClass.getMethod("setAttribute", params,false); // NOI18N 
        
        List attrList = mbean.getAttributes();
        MBeanAttribute[] attributes = (MBeanAttribute[])
            attrList.toArray(new MBeanAttribute[attrList.size()]);
        StringBuffer content = new StringBuffer();
        content.append("\n");
        MessageFormat formCheckAttr = new MessageFormat(CHECK_ATTR_NAME_PATTERN);
        MessageFormat formAttrComment = new MessageFormat(COMMENT_ATTR_VALUE_PATTERN);
        MessageFormat formAttrThrow = new MessageFormat(THROW_ATTR_VALUE_PATTERN);
        Object[] args;
        for (int i = 0; i < attributes.length ; i++) {
            String checkedAttrBody;
            if (attributes[i].isWritable())
                checkedAttrBody = formAttrComment.format(
                        new Object[] {attributes[i].getName(),"set"}); // NOI18N
            else
                checkedAttrBody = formAttrThrow.format(
                        new Object[] {attributes[i].getName() });
            args = new Object[] { "attribute.getName()" , attributes[i].getName(), // NOI18N 
                  checkedAttrBody, "" // NOI18N
            };
            content.append(formCheckAttr.format(args));
            content.append(" else ");
            if (i == attributes.length - 1)
                content.append("\n   ");
        }
        setAttr.setBodyText(content.toString() + setAttr.getBodyText().substring(1));
    }
    
    private void updateInvoke(MBeanDO mbean, JavaClass mbeanClass) {
        JavaModelPackage pkg = (JavaModelPackage) mbeanClass.refImmediatePackage();
        
        List params = new ArrayList();
        params.add(getType(pkg, "java.lang.String")); // NOI18N
        params.add(getType(pkg, "java.lang.Object[]")); // NOI18N
        params.add(getType(pkg, "java.lang.String[]")); // NOI18N
        Method invoke = mbeanClass.getMethod("invoke", params,false); // NOI18N 
        
        List opList = mbean.getOperations();
        MBeanOperation[] operations = (MBeanOperation[])
            opList.toArray(new MBeanOperation[opList.size()]);
        StringBuffer content = new StringBuffer();
        content.append("\n");
        if (operations.length > 0) {
            content.append(METHOD_SIGNATURE_DEF);
            addImport(mbeanClass.getResource(),"java.util.Arrays"); // NOI18N
        }
        MessageFormat formOperation = 
                new MessageFormat(OPERATION_CHECK_PATTERN);
        Object[] args;
        for (int i = 0; i < operations.length ; i++) {
            content.append(METHOD_SIGNATURE);
            for (int j = 0; j < operations[i].getParametersSize(); j ++) {
                MBeanOperationParameter param = operations[i].getParameter(j);
                content.append(WizardHelpers.getFullTypeNameCode( // NOI18N
                        param.getParamType()));
                if (j < operations[i].getParametersSize() - 1) {
                    content.append(","); // NOI18N
                }
                content.append("\n"); // NOI18N
            }
            content.append("};\n"); // NOI18N
            String code = COMMENT_INVOKE + WizardHelpers.getDefaultValue(
                    operations[i].getReturnTypeName()) + ";"; // NOI18N
            Object[] arg = { operations[i].getName(), code , "operationName" }; // NOI18N
            content.append(formOperation.format(arg));
            content.append("\n"); // NOI18N
        }
        invoke.setBodyText(content.toString() + invoke.getBodyText().substring(1));
    }
    
    private void updateMBeanInfo(MBeanDO mbean, JavaClass mbeanClass) {
        JavaModelPackage pkg = (JavaModelPackage) mbeanClass.refImmediatePackage();
        
        List params = new ArrayList();
        Method buildInfo = 
                mbeanClass.getMethod("buildDynamicMBeanInfo", params,false); // NOI18N 
        
        // add MBeanAttributeInfo
        List attrList = mbean.getAttributes();
        MBeanAttribute[] attributes = (MBeanAttribute[])
            attrList.toArray(new MBeanAttribute[attrList.size()]);
        StringBuffer attrInfo = new StringBuffer();
        attrInfo.append("{\n"); // NOI18N
        MessageFormat attrInfoForm = new MessageFormat(MBEAN_ATT_INFO_PATTERN);
        for (int i = 0; i < attributes.length ; i++) {
            Object[] args = new Object[] {
                attributes[i].getName(),
                WizardHelpers.getFullTypeNameCode(attributes[i].getTypeName()),
                attributes[i].getDescription(),
                attributes[i].isReadable(),
                attributes[i].isWritable(),
                "false" // NOI18N 
            };
            attrInfo.append(attrInfoForm.format(args));
            if (i != attributes.length - 1)
                attrInfo.append(","); // NOI18N 
            attrInfo.append("\n"); // NOI18N
        }
        attrInfo.append("}"); // NOI18N
        
        // add MBeanOperationInfo
        List opList = mbean.getOperations();
        MBeanOperation[] operations = (MBeanOperation[])
            opList.toArray(new MBeanOperation[opList.size()]);
        StringBuffer opInfo = new StringBuffer();
        StringBuffer paramsInfo = new StringBuffer();
        MessageFormat opInfoForm = new MessageFormat(MBEAN_OPERATION_INFO_PATTERN);
        MessageFormat opParamsForm = new MessageFormat(MBEAN_PARAMS_INFO_PATTERN);
        MessageFormat opParamForm = new MessageFormat(MBEAN_PARAM_INFO_PATTERN);
        opInfo.append("{\n"); // NOI18N
        for (int i = 0; i < operations.length ; i++) {
            //add MBeanParameterInfo
            Object[] args = new Object[] { i };
            paramsInfo.append(opParamsForm.format(args));
            int nbParam = operations[i].getParametersSize();
            for (int j = 0; j < nbParam; j++) {
                MBeanOperationParameter param = operations[i].getParameter(j);
                Object[] pargs = new Object[] { 
                    param.getParamName(),
                    WizardHelpers.getFullTypeNameCode(param.getParamType()),
                    param.getParamDescription()
                };
                paramsInfo.append(opParamForm.format(pargs));
                if (j != nbParam - 1)
                    paramsInfo.append(","); // NOI18N
                paramsInfo.append("\n"); // NOI18N
            }
            paramsInfo.append("};\n"); // NOI18N
            
            //add MBeanOperationInfo
            args = new Object[] {
                operations[i].getName(),
                operations[i].getDescription(),
                WizardHelpers.getFullTypeNameCode(operations[i].getReturnTypeName()),
                i
            };
            opInfo.append(opInfoForm.format(args));
            if (i != operations.length - 1)
                opInfo.append(","); // NOI18N 
            opInfo.append("\n"); // NOI18N
        }
        opInfo.append("}"); // NOI18N
        
        MessageFormat formBody = new MessageFormat(buildInfo.getBodyText());
        Object[] args = new Object[] { attrInfo.toString(), paramsInfo.toString(),
            opInfo.toString(), mbean.getDescription()}; 
        
        buildInfo.setBodyText(formBody.format(args).substring(1));
    }
    
    private void updateDescription(MBeanDO mbean,JavaClass mbeanClass) {
        MessageFormat formDoc = new MessageFormat(mbeanClass.getJavadocText());
        Object[] args = new Object[] { mbean.getDescription() }; // NOI18N     
        mbeanClass.setJavadocText(formDoc.format(args));
    }
 
}
