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
package org.netbeans.modules.uml.ui.swing.projecttree;

import java.awt.Component;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram;
import org.netbeans.modules.uml.core.support.umlutils.IDataFormatter;
import org.netbeans.modules.uml.core.workspacemanagement.IWSElement;
import org.netbeans.modules.uml.ui.controls.drawingarea.IUIDiagram;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem;
import org.netbeans.modules.uml.ui.controls.projecttree.ProjectTreeNode;
import org.netbeans.modules.uml.ui.support.ProductHelper;
import org.netbeans.modules.uml.ui.support.commonresources.CommonResourceManager;
import org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeDiagram;
import org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeFolder;
import org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeItem;
import org.netbeans.modules.uml.core.scm.ISCMIntegrator;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaControl;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Rectangle;
import javax.swing.SwingUtilities;
import org.netbeans.modules.uml.core.support.Debug;

/**
 * @author treys
 *
 */
public class ProjectTreeRender extends DefaultTreeCellRenderer implements TreeCellRenderer
{
    private ImageIcon   m_WarningIcon      = null;

    public ProjectTreeRender()
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
        if(value instanceof ITreeItem)
        {
            formatElementForObject(value);
            setIcon(getImage((ITreeItem)value, expanded));
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

    protected Icon getImage(ITreeItem value, boolean expanded)
    {
        Icon retVal = null;

        IProjectTreeItem data = value.getData();
        if(value instanceof ITreeFolder)
        {
            CommonResourceManager resource = CommonResourceManager.instance();
            retVal = resource.getIconForElementType(value.getName());
        }
        else if(value instanceof ITreeDiagram)
        {
            ITreeDiagram diagram = (ITreeDiagram)value;

            CommonResourceManager resource = CommonResourceManager.instance();
            retVal = resource.getIconForElementType(diagram.getDiagramType());
        }
        else if(data.getModelElement() != null)
        {
            CommonResourceManager resource = CommonResourceManager.instance();
            retVal = resource.getIconForDisp(data.getModelElement());
        }
        else if(data.isProject() == true)
        {
            CommonResourceManager resource = CommonResourceManager.instance();
            retVal = resource.getIconForElementType("WSProject"); //$NON-NLS-1$
        }
        else if(data.isWorkspace() == true)
        {
            CommonResourceManager resource = CommonResourceManager.instance();
            // special case for design pattern catalog
            if (data.getItemText().equals("DesignPatternCatalog"))
            {
                retVal = resource.getIconForElementType("DesignPatternCatalog");
            }
            else
            {
                retVal = resource.getIconForElementType("Workspace");
            }
        }
        else
        {
            CommonResourceManager resource = CommonResourceManager.instance();
            retVal = resource.getIconForElementType(value.getName());

        }
        if(retVal == null)
        {
            retVal = createImage(m_WarningIcon, ProjectTreeResources.getString("ProjectTreeRender.Warning_Image_Path")); //$NON-NLS-1$
        }

        return getIconWithOverlay(retVal, value);
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
    private void formatElementForObject(Object value)
    {
        String text   = "";		 //$NON-NLS-1$

        if(value instanceof ProjectTreeNode)
        {
            ProjectTreeNode node = (ProjectTreeNode)value;
            text = node.getDisplayedName();
        }

        if((text != null) && (text.length() > 0))
        {
            setText(text);
        }
    }

    protected Icon getOverlayIcon(IProjectTreeItem item)
    {
        Icon retVal = null;


        ISCMIntegrator gator = ProductHelper.getSCMIntegrator();
        if(gator != null)
        {
           if(item.getItemText().equals("C") == true)
           {
              Debug.out.println("Here");
           }
           //String output = "getOverlayIcon(" + item.getItemText() + ")";
           //Debug.out.println(output);
           int kind = gator.getSCMMaskKind(item);
           retVal = gator.getSCMMask(kind);
        }

        return retVal;
    }

    protected Icon getIconWithOverlay(Icon image, ITreeItem item)
    {
        Icon retVal = image;

        if(!(item instanceof ITreeFolder))
        {
            Icon overlay = getOverlayIcon(item.getData());

            if((image != null) && (overlay != null))
            {
                GraphicsDevice[] gs = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
                GraphicsConfiguration[] gc = gs[0].getConfigurations();
                Image retImage = gc[0].createCompatibleVolatileImage(image.getIconWidth(), image.getIconHeight());
                Graphics g = retImage.getGraphics();

                image.paintIcon(this, g, 0, 0);

                int overlayY = image.getIconHeight() - overlay.getIconHeight();
                overlay.paintIcon(this, g, 0, overlayY);

                retVal = new ImageIcon(retImage);
            }
        }
        return retVal;
    }
}






