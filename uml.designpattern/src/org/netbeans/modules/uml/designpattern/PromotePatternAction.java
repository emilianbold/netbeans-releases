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
 * PromotePatternAction.java
 *
 * Created on April 28, 2005, 12:38 PM
 */

package org.netbeans.modules.uml.designpattern;

import java.awt.Dialog;
import org.netbeans.modules.uml.core.metamodel.infrastructure.ICollaboration;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CookieAction;

/**
 *
 * @author Administrator
 */
public class PromotePatternAction extends CookieAction
{

   /** Creates a new instance of ApplyPatternAction */
   public PromotePatternAction()
   {
   }

    protected Class[] cookieClasses()
    {
       return new Class[] {ICollaboration.class};
    }

    public String getName()
    {
       return NbBundle.getMessage(PromotePatternAction.class,
				                      "IDS_POPUP_PROMOTE"); // NOI18N
    }

    protected int mode()
    {
       return MODE_EXACTLY_ONE;
    }

    public HelpCtx getHelpCtx()
    {
       return null;
    }

    protected void performAction(org.openide.nodes.Node[] nodes)
    {
       if(nodes.length == 1)
       {
          // get the pattern that is selected in the tree
          ICollaboration pCollab = (ICollaboration)nodes[0].getCookie(ICollaboration.class);
          if (pCollab != null)
          {
             promote(pCollab);
          }
       }
    }
    
    /**
	 * Begin the process to promote the pattern to the design center
	 *
	 * @param pDispatch[in]			The pattern to apply
	 *
	 * @return HRESULT
	 */
	public void promote(Object pDispatch)
	{
      IDesignPatternManager manager = DesignPatternCatalog.getPatternManager();
		if ((pDispatch != null) && (manager != null))
		{
			ICollaboration pCollab = null;
			if (pDispatch instanceof ICollaboration)
			{
				pCollab = (ICollaboration)pDispatch;
			}
			if (pCollab != null)
			{
				// build the details for this pattern because that is what is used
				// to promote the pattern - the pattern details
				IDesignPatternDetails pDetails = new DesignPatternDetails();
				manager.buildPatternDetails(pCollab, pDetails);
				PromotePatternPanel panel = new PromotePatternPanel(manager, pDetails);
				if (pDetails != null)
				{
					DialogDescriptor dialogDescriptor=new DialogDescriptor(panel,
					NbBundle.getMessage(PromotePatternPanel.class,
								"IDS_PROMOTETITLE")); // NOI18N
	   
					Dialog dialog=DialogDisplayer.getDefault().createDialog(
						dialogDescriptor);
                    dialog.getAccessibleContext().setAccessibleDescription(
                            NbBundle.getMessage(PromotePatternPanel.class,
								"IDS_PROMOTETITLE")); // NOI18N
					try
					{
						dialog.setVisible(true);

						if (dialogDescriptor.getValue()==DialogDescriptor.OK_OPTION) 
						{
							panel.promote();
						}
					}finally 
					{
						dialog.dispose();
					}
				}
			}
	   }
	}
}
