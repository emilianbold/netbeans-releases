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
 * ReqProviderDialog.java
 *
 * Created on June 28, 2004, 1:57 PM
 */

package org.netbeans.modules.uml.core.requirementsframework;

//import org.netbeans.modules.uml.core.addinframework.AddInDialog;
import java.awt.Dialog;
import java.awt.Frame;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;

/**
 *
 * @author  Trey Spiva
 */
public class ReqProviderDialog implements IReqProviderDialog
{

   /** Creates a new instance of ReqProviderDialog */
   public ReqProviderDialog()
   {
   }

   public boolean display(Frame parentHwnd, IRequirementsManager manager)
   {   
	   RequirementProviderPanel panel = new RequirementProviderPanel(manager);
	   DialogDescriptor dialogDescriptor=new DialogDescriptor(panel,
			NbBundle.getMessage(ReqProviderDialog.class,
			"IDS_PROVIDER_DLG_TITLE")); // NOI18N
	   
	   Dialog dialog=DialogDisplayer.getDefault().createDialog(
			dialogDescriptor);
		try
		{
			dialog.setVisible(true);

			if (dialogDescriptor.getValue()==DialogDescriptor.OK_OPTION) 
			{
				panel.process();
			}
		}finally 
		{
			dialog.dispose();
		}
		return true;
   }
   
}
