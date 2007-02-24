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
 * Created on May 22, 2003
 *
 */
package org.netbeans.modules.uml.ui.swing.preferencedialog;

import java.awt.Component;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IConfigManager;
import org.netbeans.modules.uml.core.support.umlsupport.ProductRetriever;
import org.netbeans.modules.uml.core.support.umlutils.IPropertyDefinition;
import org.netbeans.modules.uml.core.support.umlutils.IPropertyElement;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem;
import org.netbeans.modules.uml.ui.controls.projecttree.ProjectTreeNode;
import org.netbeans.modules.uml.ui.support.commonresources.CommonResourceManager;
import org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeFolder;
import org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeItem;


/**
 * @author treys
 *
 */
public class PreferenceDialogTreeRenderer extends DefaultTreeCellRenderer implements TreeCellRenderer
{	
	private ImageIcon   m_WarningIcon      = null;
   
	public PreferenceDialogTreeRenderer()
	{	
	}
	
   /* (non-Javadoc)
    * @see javax.swing.tree.TreeCellRenderer#getTreeCellRendererComponent(javax.swing.JTree, java.lang.Object, boolean, boolean, boolean, int, boolean)
    */
   public Component getTreeCellRendererComponent(JTree  tree,
                                                 Object  value,
                                                 boolean isSelected,
                                                 boolean expanded,
                                                 boolean leaf,
                                                 int     row,
                                                 boolean hasFocus)
   {
		if(value instanceof DefaultMutableTreeNode)
		{
			DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
			formatElementForObject(node);
			setIcon(getImage(node, expanded));
		}
		
      selected = isSelected;
      if(isSelected == true)
      {
         setBackground(getBackgroundSelectionColor());
         setForeground(getTextSelectionColor());
      }
      else
      {
         setBackground(getBackgroundNonSelectionColor());
         setForeground(getTextNonSelectionColor());
      }
      
      return this;
   }
   
   protected Icon getImage(DefaultMutableTreeNode value, boolean expanded)
	{
		Icon retVal = null;		
		
		Object obj = value.getUserObject();
		if (obj != null && obj instanceof IPropertyElement)
		{
			IPropertyElement pEle = (IPropertyElement)obj;
			IPropertyDefinition pDef = pEle.getPropertyDefinition();
			if (pDef != null)
			{
				//we will have to specify/get the full path for the icon. Cannot use the config location
//				IConfigManager conMan = ProductRetriever.retrieveProduct().getConfigManager();
//				if (conMan != null)
//				{
//					String imageFileName = conMan.getDefaultConfigLocation();
//					String imageName = pDef.getImage();
//					if (imageName != null)
//					{
//						if (imageName.startsWith("\\"))
//						{
//							imageFileName += imageName;
//						}
//						else
//						{
//							imageFileName += "\\" + imageName;
//						}
//						CommonResourceManager resource = CommonResourceManager.instance();
//						retVal = resource.getIconForFile(imageFileName);
//					}
//				}
				String imageName = pDef.getImage();
				CommonResourceManager resource = CommonResourceManager.instance();
				retVal = resource.getIconForFile(imageName);
			}
		}
		if(retVal == null)
		{
			retVal = createImage(m_WarningIcon, "icons/warning.gif");
		}
		return retVal; 
	}
   
//   public void paint(Graphics g)
//   {
//   }

   /**
    * @param m_ProjectIcon
    * @param string
    * @return
    */
   private ImageIcon createImage(ImageIcon image, String filename)
   {
      if(image == null)
      {
      	image = new ImageIcon(filename);
      }
      return image;
   }
	
	/**
	 * @param value
	 */
	private void formatElementForObject(DefaultMutableTreeNode value)
	{
		String text   = "";		
		
		if (value != null)
		{
			Object obj = value.getUserObject();
			if (obj != null && obj instanceof IPropertyElement)
			{
				IPropertyElement pEle = (IPropertyElement)obj;
				IPropertyDefinition pDef = pEle.getPropertyDefinition();
				if (pDef != null)
				{
					text = pDef.getDisplayName();
				}
			}
		}
	   
	   if(text != null && text.length() > 0)
	   {
	   		setText(text);
	   }
	}	

}






