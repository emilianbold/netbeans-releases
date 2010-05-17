/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 2004-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.jmx.mbeanwizard.generator;

import org.netbeans.api.java.source.JavaSource;
import org.netbeans.modules.jmx.JavaModelHelper;
import org.netbeans.modules.jmx.MBeanDO;
import org.netbeans.modules.jmx.common.WizardConstants;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle;

/**
 * Standard MBean code generator class
 * @author jfdenise
 */
public class StdMBeanClassGen extends MBeanFileGenerator {
    
    private String suffix;
    
    StdMBeanClassGen(String suffix) {
        this.suffix = suffix;
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
        
        //generation of Empty MBean interface
        MBeanInterfaceGen intfGen = new MBeanInterfaceGen(suffix);
        FileObject itfFile = intfGen.generateMBean(mbean);
        JavaSource mbeanItf = JavaModelHelper.getSource(itfFile);
        
        boolean isExtended = mbean.isExtendedStandardMBean();
        DataObject dTemplate = null;
        if(isExtended)
           dTemplate =  getTemplate(NbBundle.getMessage(JavaModelHelper.class,"PROP_ExtendedStandardMBeanTemplate"));// NOI18N
        else
            dTemplate = mbean.getTemplate();
        
        //generation of MBean class
        // The template file is linked to MBean Type         
        mbeanDObj = dTemplate.createFromTemplate( 
                    mbeanFolder, mbeanName );
        
        FileObject mbeanFile = mbeanDObj.getPrimaryFile();            
        JavaSource mbeanClass = JavaModelHelper.getSource(mbeanFile);

        // Update description if it is 
        // XXX REVISIT, TO REMOVE WITH NEW UI.
        if (isExtended)
           JavaModelHelper.updateExtendedStandardMBeanTemplate(mbeanClass, mbeanItf, mbean); 
        else
           JavaModelHelper.updateStandardMBeanTemplate(mbeanClass, mbeanItf, mbean);
        
        return mbeanFile;
    }
}
