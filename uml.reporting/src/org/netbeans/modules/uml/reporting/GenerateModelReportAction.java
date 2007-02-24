/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.uml.reporting;

import java.io.File;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.project.UMLProject;
import org.netbeans.modules.uml.project.UMLProjectHelper;
import org.netbeans.modules.uml.reporting.wizard.ReportWizardSettings;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.CookieAction;

public final class GenerateModelReportAction extends CookieAction
{
    
    protected void performAction(Node[] activatedNodes)
    {
        IElement element=null;
        
        element = (IElement)activatedNodes[0].getCookie(IElement.class);
        if (element==null)
        {
            element = getProject(activatedNodes[0]);
        }
        ReportWizardSettings settings = new ReportWizardSettings(element);
        
        FileObject pdir = settings.getProject().getProjectDirectory();
        File reportDir = new File(FileUtil.toFile(pdir), "report"); // NOI18N
        settings.setReportFolder(reportDir);
        RequestProcessor processor = new RequestProcessor("uml"); // NOI18N
        ReportTask task = new ReportTask(settings);
        processor.post(task);
    }
    
    
    private IProject getProject(Node node)
    {
        UMLProject project = (UMLProject)node.getLookup().lookup(UMLProject.class);
        if (project!=null)
        {
            UMLProjectHelper helper = (UMLProjectHelper)project.getLookup().lookup(UMLProjectHelper.class);
            return helper.getProject();
        }
        return null;
    }
    
    
    protected int mode()
    {
        return CookieAction.MODE_EXACTLY_ONE;
    }
    
    public String getName()
    {
        return NbBundle.getMessage(GenerateModelReportAction.class, "CTL_GenerateModelReportAction");
    }
    
    protected Class[] cookieClasses()
    {
        return new Class[] {
            IElement.class,
            UMLProject.class
        };
    }
    
    protected String iconResource()
    {
        return "org/netbeans/modules/uml/resources/toolbar_images/ModelReport.png";
    }
    
    public HelpCtx getHelpCtx()
    {
        return HelpCtx.DEFAULT_HELP;
    }
    
    protected boolean asynchronous()
    {
        return false;
    }
    
}

