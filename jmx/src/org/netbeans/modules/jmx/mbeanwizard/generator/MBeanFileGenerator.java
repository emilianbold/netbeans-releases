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
 * Software is Sun Microsystems, Inc. Portions Copyright 2004-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.jmx.mbeanwizard.generator;
import org.netbeans.modules.jmx.MBeanDO;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataObject;

/**
 * Generic MBean File generator.
 * @author thomas
 */
public abstract class MBeanFileGenerator {
    /**
     * Generates all the files for the new MBean.
     * @param mbean <CODE>MBeanDO</CODE> the MBean to generate
     * @throws java.io.IOException <CODE>IOException</CODE>
     * @throws java.lang.Exception <CODE>Exception</CODE>
     * @return <CODE>FileObject</CODE> the generated file which is MBean class.
     */
    public abstract FileObject generateMBean(MBeanDO mbean)
            throws java.io.IOException, Exception;
    
    // returns the Dynamic MBean file template
    protected static DataObject getTemplate(String templatePath) throws Exception {
        /* get the template DataObject... */
        FileObject template = Repository.getDefault().getDefaultFileSystem()
                              .findResource(templatePath);
        return DataObject.find(template);
    }
    
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
    
  
    /*
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
    */
}
