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

/*
 * ApplyPatternAction.java
 *
 * Created on April 28, 2005, 12:10 PM
 */

package org.netbeans.modules.uml.designpattern;

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
                manager.applyPattern(pDetails);
             }
          }
          else
          {
             // gui does not need to be displayed, so begin the process of applying
             manager.applyPattern(pDetails);
          }
       }
    }
}
