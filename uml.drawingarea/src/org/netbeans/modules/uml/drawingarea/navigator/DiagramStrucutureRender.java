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
 * DiagramStrucutureRender.java
 *
 * Created on December 12, 2005, 8:41 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.uml.drawingarea.navigator;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.ui.support.commonresources.CommonResourceManager;
import java.awt.Component;
import java.awt.Image;
import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import org.openide.util.Utilities;

/**
 *
 * @author TreySpiva
 */
public class DiagramStrucutureRender extends DefaultTreeCellRenderer
{
   
   /** Creates a new instance of DiagramStrucutureRender */
   public DiagramStrucutureRender()
   {
   }

   public Component getTreeCellRendererComponent(JTree tree, 
                                                 Object value, 
                                                 boolean sel, 
                                                 boolean expanded, 
                                                 boolean leaf, 
                                                 int row, 
                                                 boolean hasFocus)
   {
      if(value instanceof IElement)
      {
         IElement element = (IElement)value;
         String type = element.getElementType();
         
         CommonResourceManager resource = CommonResourceManager.instance();
         Icon icon = resource.getIconForElementType(type);
         setOpenIcon(icon);
         setClosedIcon(icon);
         setLeafIcon(icon);
      }
      
      if(value instanceof INamedElement)
      {
         INamedElement element = (INamedElement)value;
         setText(element.getNameWithAlias());
      }
      
      return super.getTreeCellRendererComponent(tree, 
                                                value, 
                                                sel, 
                                                expanded, 
                                                leaf, 
                                                row, 
                                                hasFocus);
   }
   
   /**
    * @param string
    * @return
    */
   protected Image createImage(String iconLocation)
   {
      return Utilities.loadImage( iconLocation, true );
   }
}
