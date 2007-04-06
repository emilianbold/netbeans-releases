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
 * ModelingPalette.java
 *
 * Created on March 2, 2005, 3:13 PM
 */

package org.netbeans.modules.uml.palette.ui;

import org.openide.nodes.Node;
import org.openide.nodes.FilterNode;
import org.openide.cookies.InstanceCookie;

/**
 *
 * @author Praveen Savur
 */
public class ModelingPalette extends FilterNode
{
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
   
   /** Creates a new instance of ModelingPalette */
   public ModelingPalette(Node root)
   {
      super(root, new PaletteCategoryFilterNode(root));
      enableDelegation(DELEGATE);
   }
   
   //    public SystemAction[] getActions() {
   //        if (staticActions == null)
   //            staticActions = new SystemAction [] {
   //                SystemAction.get(ReorderAction.class)
   //            };
   //            return staticActions;
   //    }
}

class PaletteCategoryFilterNode extends FilterNode.Children
{
   
   public PaletteCategoryFilterNode(Node node)
   {
      super(node);
   }
   
   
   protected Node copyNode(Node node)
   {
      return new PaletteCategoryNode(node);
   }
}

class PaletteCategoryNode extends FilterNode
{
   
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
   
   public PaletteCategoryNode(Node node)
   {
      super(node, new PaletteElementFilterNode(node));
      enableDelegation(DELEGATE);
   }
}


class PaletteElementFilterNode extends FilterNode.Children
{
   public PaletteElementFilterNode(Node node)
   {
      super(node);
   }
   
   public Node copyNode(Node node)
   {
      InstanceCookie ic = (InstanceCookie) node.getCookie(InstanceCookie.class);
      if (ic != null)
         return new PaletteElementNode(ic, node);
      
      return node;
   }
}
