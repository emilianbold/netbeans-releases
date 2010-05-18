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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.uml.core.reverseengineering.reintegration;

import org.netbeans.modules.uml.util.ITaskFinishListener;
import org.openide.util.NbBundle;

import org.netbeans.api.progress.aggregate.ProgressContributor;
import org.netbeans.api.progress.aggregate.AggregateProgressFactory;

import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.core.support.umlsupport.IStrings;
import org.netbeans.modules.uml.util.AbstractNBTask;

/**
 *
 * @author Craig Conover, craig.conover@sun.com
 */
public class ReverseEngineerTask extends AbstractNBTask
{
    private IUMLParsingIntegrator parsingIntegrator;
    private INamespace nameSpace;
    private IStrings files;
    private boolean useFileChooser = false;
    private boolean useDiagramCreateWizard = false;
    private boolean displayOutputWindow = true;
    private boolean extractClasses = true;
    
    
    public ReverseEngineerTask(
        INamespace pNameSpace, 
        IStrings pFiles,
        boolean pUseFileChooser, 
        boolean pUseDiagramCreateWizard, 
        boolean pDisplayOutputWindow, 
        boolean pExtractClasses,
        ITaskFinishListener listener)
    {
        parsingIntegrator = new UMLParsingIntegrator();
        nameSpace = pNameSpace;
        files = pFiles;
        useFileChooser = pUseFileChooser;
        useDiagramCreateWizard = pUseDiagramCreateWizard;
        displayOutputWindow = pDisplayOutputWindow; 
        extractClasses = pExtractClasses;
        
        if (listener != null)
            addListener(listener);
    }

    
    protected void begin()
    {
        parsingIntegrator.setFiles(files);
        parsingIntegrator.setTaskSupervisor(this);
        
//        log("Reverse Engineering selected options:");
//        log("  Source project : " + getSourceProject());
//        log("  Target UML project : " + getTargetProject());
//        log("  Create new project? : " + isCreateNewProject());
//        log("  RE Project or subset sources: " + getREActionType());
//        log("");

        
        parsingIntegrator.reverseEngineer(
            nameSpace, 
            useFileChooser, 
            useDiagramCreateWizard, 
            displayOutputWindow, 
            extractClasses);
    }

    
    private final int SUBTASK_TOT = 4;
    
    protected void initTask()
    {
        setLogLevel(SUMMARY);
        setTaskName(getBundleMessage("IDS_RE_TITLE"));

        //kris richards - "REShowOutput" pref expunged
        setDisplayOutput(true);
        
        progressContribs = new ProgressContributor[SUBTASK_TOT];
        int i = 0;
        
        progressContribs[i] = AggregateProgressFactory
            .createProgressContributor(
                "Parsing Elements (step " 
                + (i+1) + " of " + SUBTASK_TOT + ")");
            
        progressContribs[++i] = AggregateProgressFactory
            .createProgressContributor(
                "Analyzing Atribute/Operation Types (step " 
                + (i+1) + " of " + SUBTASK_TOT + ")");
            
        progressContribs[++i] = AggregateProgressFactory
            .createProgressContributor(
                "Resolving Attribute Types (step " 
                + (i+1) + " of " + SUBTASK_TOT + ")");
            
        progressContribs[++i] = AggregateProgressFactory
            .createProgressContributor(
                "Integrating Elements (step " 
                + (i+1) + " of " + SUBTASK_TOT + ")");
    }

    
    protected void finish()
    {
    }
    
    private String getBundleMessage(String key)
    {
        return NbBundle.getMessage(ReverseEngineerTask.class, key);
    }

    public IUMLParsingIntegrator getParsingIntegrator()
    {
        return parsingIntegrator;
    }

    public void setParsingIntegrator(IUMLParsingIntegrator parsingIntegrator)
    {
        this.parsingIntegrator = parsingIntegrator;
    }
}
