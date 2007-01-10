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
package org.netbeans.modules.jmx.agentwizard;

import java.util.HashSet;
import java.util.Set;

import org.netbeans.modules.jmx.WizardConstants;

import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataFolder;
import org.netbeans.api.java.source.JavaSource;

import org.netbeans.spi.project.ui.templates.support.Templates;

import org.netbeans.modules.jmx.JavaModelHelper;
import org.netbeans.modules.jmx.WizardHelpers;
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
