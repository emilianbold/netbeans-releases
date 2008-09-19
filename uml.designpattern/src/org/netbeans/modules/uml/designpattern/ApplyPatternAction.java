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
 * ApplyPatternAction.java
 *
 * Created on April 28, 2005, 12:10 PM
 */

package org.netbeans.modules.uml.designpattern;

import javax.swing.SwingUtilities;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.infrastructure.ICollaboration;
import org.netbeans.modules.uml.project.UMLProject;
import org.netbeans.modules.uml.ui.support.ProductHelper;
import org.netbeans.modules.uml.ui.support.wizard.IWizardSheet;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CookieAction;

/**
 *
 * @author Administrator
 */
public class ApplyPatternAction extends CookieAction
{
   
   /** Creates a new instance of ApplyPatternAction */
   public ApplyPatternAction()
   {
   }

    protected Class[] cookieClasses()
    {
       return new Class[] {IElement.class, UMLProject.class};
    }

    public String getName()
    {
       return NbBundle.getMessage(ApplyPatternAction.class,
				                      "IDS_POPUP_APPLY2"); // NOI18N
    }

    protected int mode()
    {
       return MODE_ALL;
    }

    public HelpCtx getHelpCtx()
    {
       return null;
    }
	
    protected void performAction(org.openide.nodes.Node[] nodes)
    {
       // get the pattern that is selected
       //ICollaboration pCollab = DesignPatternUtilities.getSelectedCollaboration(m_Context);
       ICollaboration pCollab = null;
       if(nodes.length == 1)
       {
          pCollab = (ICollaboration)nodes[0].getCookie(ICollaboration.class);
       }
       
       if (pCollab != null)
       {
          apply(pCollab);
       }
       else
       {
          // the user has not clicked on a pattern so we will need to present them with the wizard
          // so that they can pick one
          apply(pCollab);
       }
    }
    
    /**
     * Begin the process to apply ("instantiate") the pattern
     *
     * @param pDispatch[in]			The pattern to apply
     *
     * @return HRESULT
     *
     */
    public void apply(Object pDispatch)
    {
       //
       // Now begin applying the pattern, first must build the information about it
       //
       IDesignPatternManager manager = DesignPatternCatalog.getPatternManager();
       if (manager != null)
       {
          // build the details for this pattern because that is what is used
          // to apply the pattern - the pattern details
          IDesignPatternDetails pDetails = new DesignPatternDetails();
          ICollaboration pCollab = null;
          if (pDispatch instanceof ICollaboration)
          {
             pCollab = (ICollaboration)pDispatch;
          }
          if (pCollab != null)
          {
             manager.buildPatternDetails(pDispatch, pDetails);
          }
          // figure out if the all of the roles of the pattern have been
          // fulfilled (have instances for each of the roles)
          // because if they are, we will not show the dialog
          boolean bFulfill = manager.isPatternFulfilled(pDetails);
          if (!bFulfill)
          {
             // Display the GUI to the user
             String title = DefaultDesignPatternResource.getString("IDS_WIZARD_TITLE");
             Wizard wiz = new Wizard(ProductHelper.getProxyUserInterface().getWindowHandle(), title, true);
             wiz.setManager(manager);
             wiz.setDetails(pDetails);
             wiz.init(null, null, null);
             if (wiz.doModal() == IWizardSheet.PSWIZB_FINISH)
             {
                // user has hit okay on the gui and the gui information
                // has been validated, so begin the process of applying it
                manager.setDialog(wiz);
                applyPattern(manager, pDetails);
             }
          }
          else
          {
              // gui does not need to be displayed, so begin the process of applying
              applyPattern(manager, pDetails);
          }
       }
    }

    private void applyPattern(final IDesignPatternManager manager,
                              final IDesignPatternDetails pDetails)
    {
        Runnable runnable = new Runnable() 
            {
                public void run() 
                {
                    manager.applyPattern(pDetails);
                }
            };
        try {
            SwingUtilities.invokeAndWait(runnable);
        } catch (Exception iex) {
            iex.printStackTrace();
        }          
    }

    @Override
    protected boolean asynchronous()
    {
        return false;
    }



}
