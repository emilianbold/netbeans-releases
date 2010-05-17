/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
