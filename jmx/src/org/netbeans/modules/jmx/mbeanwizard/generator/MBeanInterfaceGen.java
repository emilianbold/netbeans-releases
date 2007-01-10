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
import org.netbeans.modules.jmx.JavaModelHelper;
import org.netbeans.modules.jmx.MBeanDO;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle;

/**
 * MBean Interface code generator class
 * @author jfdenise
 */
public class MBeanInterfaceGen extends MBeanFileGenerator {
    String suffix;
    /** Creates a new instance of MBeanInterfaceGen */
    public MBeanInterfaceGen(String suffix) {
        this.suffix = suffix;
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
        String mbeanIntfName = mbeanName + suffix;
        DataFolder mbeanFolder = mbean.getDataFolder();
        DataObject intfTemplate = getTemplate(NbBundle.getMessage(JavaModelHelper.class,"PROP_MBeanInterfaceTemplate"));
        DataObject intfDObj = intfTemplate.createFromTemplate(
                mbeanFolder, mbeanIntfName );
        FileObject intfFile = intfDObj.getPrimaryFile();
        
        return intfFile;
    }
}
