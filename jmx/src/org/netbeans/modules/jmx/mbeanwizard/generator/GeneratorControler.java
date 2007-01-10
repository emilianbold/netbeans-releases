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

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.netbeans.modules.jmx.MBeanDO;
import org.netbeans.modules.jmx.MBeanNotification;
import org.netbeans.modules.jmx.WizardConstants;
import org.netbeans.modules.jmx.WizardHelpers;
import org.openide.filesystems.FileObject;
import org.openide.loaders.TemplateWizard;

/**
 * Wizard MBean code generator controller class.
 * @author jfdenise
 */
public class GeneratorControler {
    
    /**
     * Generate MBean code and creates the corresponding files.
     * @param wiz <CODE>TemplateWizard</CODE> MBean informations map
     * @throws java.io.IOException <CODE>IOException</CODE>
     * @throws java.lang.Exception <CODE>Exception</CODE>
     * @return <CODE>CreationResults</CODE> the generated files set
     */
    public static Set generate(TemplateWizard wiz) 
    throws java.io.IOException, Exception {
        MBeanDO mbeanDO = Translator.createMBeanDO(wiz);
        
        MBeanFileGenerator generator = null;
        
        if (mbeanDO.getType().equals(
                WizardConstants.MBEAN_DYNAMICMBEAN)) {
            generator = new DynMBeanClassGen();
        } else 
            if(mbeanDO.getType().equals(
                WizardConstants.MXBEAN))
                    generator = new StdMBeanClassGen(WizardConstants.MXBEAN_SUFFIX);
            else
                    generator = new StdMBeanClassGen(WizardConstants.MBEAN_ITF_SUFFIX); 
        FileObject createdFile = generator.generateMBean(mbeanDO);
        
        //add notifications
        if (mbeanDO.isNotificationEmitter()) {
            AddNotifGenerator notifGenerator = new AddNotifGenerator();
            List notifList = mbeanDO.getNotifs();
            MBeanNotification[] notifs = (MBeanNotification[])
                notifList.toArray(new MBeanNotification[notifList.size()]);
            notifGenerator.update(createdFile, notifs,
                    mbeanDO.isGenBroadcastDeleg(), mbeanDO.isGenSeqNumber());
        }
        
        //add MBeanRegistration interface implementation
        if (mbeanDO.implMBeanRegist()) {
            AddRegistIntfGenerator mbeanRegistGen = new AddRegistIntfGenerator();
            //TODO link to mbean wizard settings
            mbeanRegistGen.update(createdFile, mbeanDO.isKeepPreRegistRef());
        }
        
        Set<FileObject> s = new HashSet<FileObject>();
        s.add(createdFile);
        
        WizardHelpers.refreshProjectTree(wiz);
        return s;                         
    }
}
