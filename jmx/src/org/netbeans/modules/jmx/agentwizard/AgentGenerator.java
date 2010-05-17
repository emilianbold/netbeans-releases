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
package org.netbeans.modules.jmx.agentwizard;

import java.util.HashSet;
import java.util.Set;

import org.netbeans.modules.jmx.common.WizardConstants;

import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataFolder;
import org.netbeans.api.java.source.JavaSource;

import org.netbeans.spi.project.ui.templates.support.Templates;

import org.netbeans.modules.jmx.JavaModelHelper;
import org.netbeans.modules.jmx.common.WizardHelpers;
/**
 *
 *  Wizard Agent code generator class
 */
public class AgentGenerator {
   
    /**
     * Entry point to generate agent code.
     * @param wiz <CODE>WizardDescriptor</CODE> a wizard
     * @throws java.io.IOException <CODE>IOException</CODE>
     * @throws java.lang.Exception <CODE>Exception</CODE>
     * @return <CODE>CreationResults</CODE> results of agent creation
     */
    public Set generateAgent(final WizardDescriptor wiz)
    throws java.io.IOException, Exception {
        FileObject createdFile = null;
        final String agentName = Templates.getTargetName(wiz);
        FileObject agentFolder = Templates.getTargetFolder(wiz);
        DataFolder agentFolderDataObj = DataFolder.findFolder(agentFolder);
        
        //==============================================
        // agent generation
        //==============================================
        DataObject agentDObj = null;
        
        FileObject template = Templates.getTemplate( wiz );
        DataObject dTemplate = DataObject.find( template );
        agentDObj = dTemplate.createFromTemplate(
                agentFolderDataObj, agentName );
        
        Boolean mainMethodSelected = (Boolean) wiz.getProperty(
                WizardConstants.PROP_AGENT_MAIN_METHOD_SELECTED);
        boolean removeMainMethod = ((mainMethodSelected == null) || (!mainMethodSelected));
        
        Boolean sampleCodeSelected = (Boolean) wiz.getProperty(
                WizardConstants.PROP_AGENT_SAMPLE_CODE_SELECTED);
        
        boolean removeSampleCode = ((sampleCodeSelected !=null) && (!sampleCodeSelected));
        
        //Obtain an JavaSource - represents a java file
        JavaSource js = JavaModelHelper.getSource(agentDObj.getPrimaryFile());
        
        JavaModelHelper.generateAgent(js, removeMainMethod, removeSampleCode);
        
        WizardHelpers.save(agentDObj);
        Set s = new HashSet();
        s.add(agentDObj.getPrimaryFile());
        return s;
    }
}
