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

package org.netbeans.modules.uml.palette.ui;

import java.awt.Image;
import org.openide.nodes.Node;
import org.openide.ErrorManager;
import org.openide.cookies.InstanceCookie;
import org.openide.nodes.FilterNode;
import org.netbeans.modules.uml.core.support.umlsupport.Log;
import org.netbeans.modules.uml.palette.model.ModelingPaletteNodeDescriptor;


/**
 *
 * @author Thuy
 */

class PaletteElementNode extends FilterNode
{
   Node node;
   InstanceCookie ic;
   ModelingPaletteNodeDescriptor nodeDescriptor;
   
   private static final int DELEGATE = DELEGATE_SET_NAME |
         DELEGATE_GET_NAME |
         DELEGATE_SET_DISPLAY_NAME |
         DELEGATE_GET_DISPLAY_NAME |
         DELEGATE_SET_SHORT_DESCRIPTION |
         DELEGATE_GET_SHORT_DESCRIPTION |
         DELEGATE_DESTROY |
         DELEGATE_GET_ACTIONS |
         DELEGATE_GET_CONTEXT_ACTIONS |
         DELEGATE_SET_VALUE |
         DELEGATE_GET_VALUE;
   
   public PaletteElementNode(InstanceCookie ic, Node node)
   {
      super(node);
      this.node = node;
      this.ic = ic;
      enableDelegation(DELEGATE);
      nodeDescriptor = getModelingPaletteNodeDescriptorInstance();
      
   }
   
   public String getDisplayName()
   {
      if(nodeDescriptor != null)
         return nodeDescriptor.getDisplayName();
      return "Default"; // Should not happen.
   }
   
   public String getShortDescription()
   {
      if(nodeDescriptor != null)
         return nodeDescriptor.getTooltip();
      return "Default"; // Should not happen.
   }
   
   public Image getIcon(int type)
   {
      if(nodeDescriptor != null)
         return nodeDescriptor.getIcon(type);
      return null;
   }
   
   public Image getOpenedIcon(int type)
   {
      return getIcon(type);
   }
   
   public ModelingPaletteNodeDescriptor getModelingPaletteNodeDescriptorInstance()
   {
      if (ic != null)
      {
         try
         {
            Object o = ic.instanceCreate();
            if (o instanceof ModelingPaletteNodeDescriptor)
               return (ModelingPaletteNodeDescriptor) o;
         }
         catch (Exception e)
         {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            Log.out("ModelingPaletteNodeDescriptor():getModelingPaletteNodeDescriptorInstance(): Error " + e); // NOI18N
         }
      }
      return null;
   }
}
