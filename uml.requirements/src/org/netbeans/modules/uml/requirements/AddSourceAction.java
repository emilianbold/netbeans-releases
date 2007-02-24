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
 * AddSourceAction.java
 *
 * Created on July 1, 2004, 2:52 PM
 */

package org.netbeans.modules.uml.requirements;

import org.netbeans.modules.uml.core.requirementsframework.IReqProviderDialog;
import org.netbeans.modules.uml.core.requirementsframework.ReqProviderDialog;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem;
import org.netbeans.modules.uml.ui.products.ad.application.ApplicationView;
import org.netbeans.modules.uml.ui.products.ad.application.IMenuManager;
//import org.netbeans.modules.uml.ui.products.ad.application.action.IContributionItem;
//import org.netbeans.modules.uml.ui.products.ad.application.action.IViewActionDelegate;
//import org.netbeans.modules.uml.ui.products.ad.application.action.PluginAction;
//import org.netbeans.modules.uml.ui.products.ad.application.selection.ISelection;
import org.netbeans.modules.uml.ui.support.ProductHelper;
import org.netbeans.modules.uml.ui.support.applicationmanager.IProxyUserInterface;
import org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeItem;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.openide.util.NbBundle;

/**
 *
 * @author  Trey Spiva
 */
public class AddSourceAction extends AbstractAction
{
   /** Creates a new instance of AddSourceAction */
   public AddSourceAction()
   {
       this.putValue(AbstractAction.NAME, NbBundle.getMessage(AddSourceAction.class, "IDS_ADD_REQUIREMENTSOURCE"));
   }

    public void actionPerformed(ActionEvent actionEvent)
    {
        IReqProviderDialog dlg = new ReqProviderDialog();

         IProxyUserInterface ui = ProductHelper.getProxyUserInterface();
         dlg.display(ui.getWindowHandle(), ADRequirementsManager.instance());
    }
   
}
