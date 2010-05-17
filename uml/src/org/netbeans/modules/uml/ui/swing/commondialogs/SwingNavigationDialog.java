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
