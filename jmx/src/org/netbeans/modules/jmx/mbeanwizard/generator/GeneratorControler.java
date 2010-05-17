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

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.netbeans.modules.jmx.MBeanDO;
import org.netbeans.modules.jmx.MBeanNotification;
import org.netbeans.modules.jmx.common.WizardConstants;
import org.netbeans.modules.jmx.common.WizardHelpers;
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
