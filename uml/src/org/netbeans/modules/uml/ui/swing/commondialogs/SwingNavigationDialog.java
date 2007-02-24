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

package org.netbeans.modules.uml.ui.swing.commondialogs;

import java.awt.Dialog;
import org.netbeans.modules.uml.ui.support.UserSettings;
import org.netbeans.modules.uml.ui.support.commondialogs.INavigationDialog;
import org.netbeans.modules.uml.ui.support.commondialogs.MessageResultKindEnum;
import org.netbeans.modules.uml.ui.support.diagramsupport.IPresentationTarget;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;

/**
 * 
 * @author Trey Spiva
 */
public class SwingNavigationDialog extends JCenterDialog implements INavigationDialog
{
   private IElement m_Element = null;
    private boolean m_IsDiagram = false;
    private String m_TargetXMIID = "";   
    private boolean m_IsProject = false;
    
    public SwingNavigationDialog()
    {
    }
    
    public int display(IElement element,
            ETList < IProxyDiagram > diagrams,
            ETList<IPresentationTarget>     targets,
            ETList <IProxyDiagram> assocDiagrams,
            ETList<IElement> assocElements)
    {
        int retVal = MessageResultKindEnum.SQDRK_RESULT_OK;
        
        m_Element = element;
        
        if (element instanceof IProject)
        {
            m_IsProject = true;
        }
        
        UserSettings settings = new UserSettings();
        
        SwingNavigationView navigatorView = new SwingNavigationView(this);
        navigatorView.buildList(diagrams, targets, assocDiagrams, assocElements);
        
        String dlgTitle = NbBundle.getMessage(SwingNavigationDialog.class, "IDS_ELEMENTNAVIGATION");
        DialogDescriptor dialogDescriptor = new DialogDescriptor(navigatorView, dlgTitle);
        Dialog dialog = DialogDisplayer.getDefault().createDialog(dialogDescriptor);
        try
        {
            dialog.setVisible(true);
            if (dialogDescriptor.getValue() == DialogDescriptor.OK_OPTION)
            {
                retVal = MessageResultKindEnum.SQDRK_RESULT_OK;
                navigatorView.performOKAction();
            }
            else
            {
                retVal = MessageResultKindEnum.SQDRK_RESULT_ABORT;
            }
        }
        finally
        {
            dialog.dispose();
        }
        
        return retVal;
    }
    
    public void setIsDiagram(boolean val)
    {
        m_IsDiagram = val;
    }
    
    public boolean getIsDiagram()
    {
        return m_IsDiagram;
    }
    
    public boolean getIsProject()
    {
        return m_IsProject;
    }
    
    public void setTargetXMIID(String xmiid)
    {
        m_TargetXMIID = xmiid;
    }
    
    public String getTargetXMIID()
    {
        return m_TargetXMIID;
    }
    
    public IElement getElement()
    {
        return this.m_Element;
    }
}
