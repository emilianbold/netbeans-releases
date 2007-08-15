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



package org.netbeans.modules.uml.ui.support.applicationmanager;

import java.awt.event.ActionListener;
import java.util.Iterator;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JToolBar;

import org.dom4j.Element;
import org.dom4j.Node;

import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;
import org.netbeans.modules.uml.resources.images.ImageUtil;


/**
 *
 * @author Trey Spiva
 */
public class DiagramToolDetails
{
   private Node m_ToolBarDetails = null;
   private boolean m_IsOptional  = true;

   /**
    * @param toolBarNode
    */
   public DiagramToolDetails(Node toolBarNode, boolean optional)
   {
      setToolBarDetails(toolBarNode);
      setOptional(optional);
   }

   /**
    * @return
    */
   public Node getToolBarDetails()
   {
      return m_ToolBarDetails;
   }

   /**
    * @param node
    */
   public void setToolBarDetails(Node node)
   {
      m_ToolBarDetails = node;
   }

   public void addToolbarContents(JToolBar bar, ActionListener listener)
   {
      if(bar != null)
      {
         Node details = getToolBarDetails();
         if(details.hasContent() == true)
         {
            if (details instanceof Element)
            {
               Element detailElement = (Element)details;
               
               for (Iterator iter = detailElement.elementIterator(); iter.hasNext();)
               {
                  Element curElement = (Element)iter.next();
                  if(curElement.getName().equals("Button") == true)
                  {
                     JButton button = new JButton();
                     button.setName(XMLManip.getAttributeValue(curElement, "name"));
                     button.setActionCommand(XMLManip.getAttributeValue(curElement, "id"));
                     button.setToolTipText(XMLManip.getAttributeValue(curElement, "tooltip"));
                     
                     Class resourceClass = ImageUtil.class;
                     button.setIcon(new ImageIcon(resourceClass.getResource(XMLManip.getAttributeValue(curElement, "icon"))));
                     button.addActionListener(listener);
                     bar.add(button);
                  }
                  else if(curElement.getName().equals("Seperator") == true)
                  {
                     bar.addSeparator();
                  }
               }
            }
         }
      }
   }
   
   /**
    * @return
    */
   public boolean isRequired()
   {
      return !isOptional();
   }

   /**
    * @return
    */
   public boolean isOptional()
   {
      return m_IsOptional;
   }

   /**
    * @param b
    */
   public void setOptional(boolean b)
   {
      m_IsOptional = b;
   }

}
