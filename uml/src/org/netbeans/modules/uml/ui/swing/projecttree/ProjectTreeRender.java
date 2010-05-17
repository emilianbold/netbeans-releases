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


/*
 * Created on May 22, 2003
 *
 */
package org.netbeans.modules.uml.ui.swing.projecttree;

import java.awt.Component;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;

import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem;
import org.netbeans.modules.uml.ui.controls.projecttree.ProjectTreeNode;
import org.netbeans.modules.uml.ui.support.ProductHelper;
import org.netbeans.modules.uml.ui.support.commonresources.CommonResourceManager;
import org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeDiagram;
import org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeFolder;
import org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeItem;
import org.netbeans.modules.uml.core.scm.ISCMIntegrator;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
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






