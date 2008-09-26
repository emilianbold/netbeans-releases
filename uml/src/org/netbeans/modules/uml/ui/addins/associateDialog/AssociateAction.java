/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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

/*
 * DiagramCreatorAction.java
 *
 * Created on January 7, 2005, 10:38 AM
 */

package org.netbeans.modules.uml.ui.addins.associateDialog;

//import org.netbeans.modules.uml.associatewith.*;
import org.netbeans.modules.uml.ui.addins.associateDialog.*;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeDiagram;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CookieAction;

/**
 *
 * @author  Craig Conover
 */
public class AssociateAction extends CookieAction
{
    
    /**
     * Creates a new instance of AssociateAction
     */
    public AssociateAction()
    {
    }
    
    protected Class[] cookieClasses()
    {
        return new Class[] {IElement.class};
    }
    
    protected int mode()
    {
        return MODE_ALL;
    }
    
    
    protected boolean enable(Node[] nodes)
    {
        for (Node curNode : nodes)
        {
            IElement curElement = (IElement)curNode.getCookie(IElement.class);
            
            if (curElement == null)
            {
                ITreeDiagram cookie = (ITreeDiagram)curNode.getCookie(ITreeDiagram.class);
                if (cookie == null)
                    return false;
            }
        }
        
        return true;
    }
    
    public HelpCtx getHelpCtx()
    {
        return null;
    }
    
    public String getName()
    {
        return NbBundle.getMessage(
                AssociateAction.class, "IDS_POPUP_ASSOCIATE"); // NOI18N
    }
    
    protected void performAction(Node[] nodes)
    {
        final ETList<IElement> elements = new ETArrayList<IElement>();
        final ETList<IProxyDiagram> diagrams = new ETArrayList<IProxyDiagram>();
        
        for (Node curNode : nodes)
        {
            IElement curElement = (IElement)curNode.getCookie(IElement.class);
            
            if (curElement != null)
            {
                elements.add(curElement);
            }
            else 
            {

                ITreeDiagram cookie = (ITreeDiagram)curNode.getCookie(ITreeDiagram.class);
                if (cookie != null)
                {
                    IProxyDiagram dia = cookie.getDiagram();
                    diagrams.add(dia);     
                }
            }
                
        }
        
//        AssociateDlgAddIn assocDlg = new AssociateDlgAddIn();
//                    assocDlg.handleAssociate(elements, diagrams);
        
        if ((elements != null && elements.size() > 0) ||
           ((diagrams != null && diagrams.size() > 0)))
        {
            Thread thread = new Thread(new Runnable()
            {
                public void run()
                {
                    AssociateDlgAddIn assocDlg = new AssociateDlgAddIn();
                    assocDlg.handleAssociate(elements, diagrams);
                }
            });
            
            thread.run();
        }
    }

    @Override
    protected boolean asynchronous()
    {
        return false;
    }


    ///////////////////////////////////////////////////////////////////////////
    // Helper Methods
}
