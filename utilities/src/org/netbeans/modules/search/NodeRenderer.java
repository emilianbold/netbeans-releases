/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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

package org.netbeans.modules.search;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.beans.BeanInfo;
import java.io.CharConversionException;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;
import org.openide.awt.HtmlRenderer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.xml.XMLUtil;

/**
 * 
 * @author  Marian Petras
 */
final class NodeRenderer extends JComponent implements TreeCellRenderer {

    /** */
    private static Rectangle checkBounds;
    
    /** */
    private final HtmlRenderer.Renderer nodeRenderer;
    /** */
    private final Image rootIconImage;
    /** */
    private final JCheckBox checkBox;

    /** */
    private Image deletedObjectIconImage;
    /** */
    private String deletedObjectHtmlSuffix;
    /**
     * the component returned by
     * {@link HtmlRenderer.Renderer#getTreeCellRendererComponent() getTreeCellRendererComponent()}
     */
    private Component stringDisplayer = new JLabel(" ");                //NOI18N

    /**
     *
     */
    NodeRenderer(final boolean withCheckBox) {
        nodeRenderer = HtmlRenderer.createRenderer();
        rootIconImage = ImageUtilities.loadImage(
                            "org/netbeans/modules/search/res/find.gif", //NOI18N
                            true);                       //localized
        
        setLayout(null);
        if (!withCheckBox) {
            checkBox = null;
        } else {
            checkBox = new JCheckBox();
            checkBox.setBorderPaintedFlat(true);
            
            Color c = UIManager.getColor("Tree.textBackground");        //NOI18N
            if (c == null) {
                //May be null on GTK L&F
                c = Color.WHITE;
            }
            checkBox.setBackground(c);
            
            Dimension dim = checkBox.getPreferredSize();
            checkBox.setPreferredSize(new Dimension(dim.width, dim.height - 5));
        }
    }
    
    /**
     */
    public Component getTreeCellRendererComponent(JTree tree,
                                                  Object value,
                                                  boolean selected,
                                                  boolean expanded,
                                                  boolean leaf,
                                                  int row,
                                                  boolean hasFocus) {
        final boolean isRoot = (row == 0);
        
        String text;
        boolean isHtml;
        Image iconImage;
        boolean checked;
        
        if (isRoot &&
            value instanceof ResultTreeModel ) { // #179796
            
            final ResultTreeModel resultTreeModel = (ResultTreeModel) value;
            text = resultTreeModel.getRootDisplayName();
            isHtml = false;
            iconImage = rootIconImage;
            checked = (checkBox != null) ? resultTreeModel.isSelected()
                                         : false;
            
        } else if (value.getClass() == MatchingObject.class) {
            final MatchingObject matchingObj = (MatchingObject) value;
            final DataObject dataObj = (DataObject) matchingObj.object;
            final boolean valid = matchingObj.isObjectValid();
            if (valid) {
                final Node node = dataObj.getNodeDelegate();
                FileObject fileFolder = dataObj.getPrimaryFile().getParent();
                String folderPath = FileUtil.getFileDisplayName(fileFolder);
                try {
                    text = node.getHtmlDisplayName();
                    isHtml = true;
                    if (text == null) {
                        text = XMLUtil.toElementContent(node.getDisplayName());
                    }
                    text = text
                       + " <font color='!" + getFilePathColor() + "'>"  //NOI18N
                       + XMLUtil.toElementContent(folderPath);
                } catch (CharConversionException ex) {
                    text = node.getDisplayName() + ' ' + '(' + folderPath + ')';
                    isHtml = false;
                }
                iconImage = node.getIcon(BeanInfo.ICON_COLOR_16x16);
            } else {
                text = dataObj.getName() + getDeletedObjectHtmlSuffix();
                isHtml = true;
                iconImage = getDeletedObjectIconImage();
            }
            checked = (checkBox != null) ? matchingObj.isSelected()
                                         : false;
            
        } else {
            assert (value instanceof Node);
            
            final Node node = (Node) value;
            text = node.getHtmlDisplayName();
            isHtml = (text != null);
            if (!isHtml) {
                text = node.getDisplayName();
            }
            iconImage = node.getIcon(BeanInfo.ICON_COLOR_16x16);
            if (checkBox == null) {
                checked = false;
            } else {
                TreePath path = tree.getPathForRow(row);
                if (path == null) {         //surprisingly, this happens
                    checked = true;
                } else {
                    MatchingObject matchingObj
                            = (MatchingObject) path.getPathComponent(1);
                    if (matchingObj.isUniformSelection()) {
                        checked = matchingObj.isSelected();
                    } else {
                        int parentPathRow
                                = tree.getRowForPath(path.getParentPath());
                        int index = row - parentPathRow - 1;
                        checked = matchingObj.isSubnodeSelected(index);
                    }
                }
            }
        }
        
        stringDisplayer = nodeRenderer.getTreeCellRendererComponent(
                tree, 
                text,
                selected,
                expanded,
                leaf,
                row,
                hasFocus);

        nodeRenderer.setHtml(isHtml);
        nodeRenderer.setIcon(new ImageIcon(iconImage));
        
        //HtmlRenderer does not tolerate null colors - real ones are needed to
        //ensure fg/bg always diverge enough to be readable
        if (stringDisplayer.getBackground() == null) {
            stringDisplayer.setBackground(tree.getBackground());
        }
        if (stringDisplayer.getForeground() == null) {
            stringDisplayer.setForeground(tree.getForeground());
        }

        if (checkBox != null) {
            checkBox.setSelected(checked);
            return this;
        } else {
            return stringDisplayer;
        }
    }

    /** name of the color to be used for displaying path to the found file */
    private String filePathColorName;

    /** @see  #filePathColor */
    private String getFilePathColor() {
        if (filePathColorName == null) {
            UIDefaults uiDefaults = UIManager.getDefaults();
            if (uiDefaults.getColor("Tree.selectionBackground").equals( //NOI18N
                    uiDefaults.getColor("controlShadow"))) {            //NOI18N
                filePathColorName = "Tree.selectionBorderColor";        //NOI18N
            } else {
                filePathColorName = "controlShadow";                    //NOI18N
            }
        }
        return filePathColorName;
    }
    
    @Override
    public void paint(Graphics g) {
        Dimension checkDim = checkBox.getSize();
        Dimension labelDim = stringDisplayer.getPreferredSize();

        int labelY = (checkDim.height >= labelDim.height)
                     ? (checkDim.height - labelDim.height) / 2
                     : 0;
        checkBox.paint(g);

        /*
         * The stringDisplayer's bounds are set to (0, 0, 0, 0), although
         * they have been set to reasonable values by doLayout().
         * To work-around this, we translate the Graphics' origin
         * to the desired location, paint the stringDisplayer and then
         * return the Graphics' origin back.
         */
        assert stringDisplayer.getBounds().x == 0
               && stringDisplayer.getBounds().y == 0;
        g.translate(checkDim.width, labelY);
        stringDisplayer.paint(g);
        g.translate(-checkDim.width, -labelY);
    }
    
    @Override
    public Dimension getPreferredSize() {
        stringDisplayer.setFont(getFont());
        
        Dimension prefSize = new Dimension(stringDisplayer.getPreferredSize());
        Dimension checkDim = checkBox.getPreferredSize();
        prefSize.width += checkDim.width;
        prefSize.height = Math.max(prefSize.height, checkDim.height);
        return prefSize;
    }
    
    @Override
    public void doLayout() {
        Dimension checkDim = checkBox.getPreferredSize();
        Dimension labelDim = stringDisplayer.getPreferredSize();
        int checkWidth = checkDim.width;
        int checkHeight = checkDim.height;
        int labelWidth = labelDim.width;
        int labelHeight = labelDim.height;
        
        int heightDif = labelHeight - checkHeight;
        int checkY = (heightDif > 2) ? heightDif / 2 - 1
                                     : 0;
        int labelY = (heightDif < 0) ? -heightDif / 2
                                     : 0;

        checkBox.setBounds(0, checkY, checkWidth, checkHeight);
        stringDisplayer.setBounds(checkWidth, labelY, labelWidth, labelHeight);
        if (checkBounds == null) {
            checkBounds = checkBox.getBounds();
        }
    }
    
    private Image getDeletedObjectIconImage() {
        if (deletedObjectIconImage == null) {
            deletedObjectIconImage = ImageUtilities.loadImage(
                    "org/netbeans/modules/search/res/invalid.png",      //NOI18N
                    true);                       //localized
        }
        return deletedObjectIconImage;
    }
    
    private String getDeletedObjectHtmlSuffix() {
        if (deletedObjectHtmlSuffix == null) {
            deletedObjectHtmlSuffix
                    = "&nbsp;&nbsp;<font color=\"#ff0000\">"            //NOI18N
                      + NbBundle.getMessage(getClass(),
                                            "LBL_InvalidFile")          //NOI18N
                      + "</font>";                                      //NOI18N
        }
        return deletedObjectHtmlSuffix;
    }

    static Rectangle getCheckBoxRectangle() {
        return (Rectangle) checkBounds.clone();
    }
}
