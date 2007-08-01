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

package org.netbeans.modules.uml.designpattern;

import java.awt.event.ActionEvent;
import java.util.ResourceBundle;
import javax.swing.Action;
import javax.swing.TransferHandler;
import org.netbeans.modules.uml.resources.images.ImageUtil;

import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CallbackSystemAction;

/**
 * The DesignCenterAction will add a new design center control the the explorer
 * mode for the current workspace.
 * @author  Trey Spiva
 * @version 1.0
 */
public class DesignCenterAction extends CallbackSystemAction
{
   private ResourceBundle mBundle = null;
   
   /** Creates new DesignCenterAction */
   public DesignCenterAction()
   {
      
      mBundle = NbBundle.getBundle(DesignCenterAction.class);
      
      putValue(Action.NAME, mBundle.getString("Action.DesignCenter.Title")); // NOI18N
//      putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(mBundle.getString("IDS_CTRLSHIFTC")));
      //putValue(Action.SMALL_ICON, "org/netbeans/modules/uml/resources/designcenter.gif");
   }
   
   /**
    * Gets the name of the action.
    */
   public String getName()
   {
      
      return (String)getValue(Action.NAME);
   }
   
   /**
    * Get a help context for the action
    * @return help for this action
    */
   public HelpCtx getHelpCtx()
   {
      return null;
   }
   
   /* (non-Javadoc)
    * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
    */
   public void actionPerformed(ActionEvent e)
   {
      
       DesignCenterComponent component = DesignCenterComponent.getInstance();
       TransferHandler handler = component.getTransferHandler();
       component.setTransferHandler(null);
       component.setTransferHandler(handler);
       component.open();
       component.requestActive();
      
   }
   
   protected String iconResource()
   {
       return ImageUtil.instance().IMAGE_FOLDER + "design-center.gif"; // NOI18N
   }
   
   public boolean isEnabled()	{
       return true;
   }
}
