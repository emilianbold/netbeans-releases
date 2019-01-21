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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.mobility.svgcore.navigator;

import java.awt.Component;
import java.awt.Image;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import org.netbeans.modules.editor.structure.api.DocumentElement;
import org.netbeans.modules.mobility.svgcore.model.SVGFileModel;
import org.openide.awt.HtmlRenderer;
import org.openide.util.ImageUtilities;
import org.openide.util.Utilities;


/** TreeCellRenderer implementatin for the XML Navigator.
 *
 * @version 1.0
 */
class SVGNavigatorTreeCellRenderer extends DefaultTreeCellRenderer {    
    private static final String TAG_16      = "org/netbeans/modules/xml/text/navigator/resources/tag.png";  //NOI18N
//    private static final String PI_16       = "org/netbeans/modules/xml/text/navigator/resources/xml_declaration.png"; //NOI18N
//    private static final String DOCTYPE_16  = "org/netbeans/modules/xml/text/navigator/resources/doc_type.png"; //NOI18N
//    private static final String CDATA_16    = "org/netbeans/modules/xml/text/navigator/resources/cdata.png";    
    private static final String ERROR_16    = "org/netbeans/modules/xml/text/navigator/resources/badge_error.png"; //NOI18N
    private static final String ANIMATE_16  = "org/netbeans/modules/mobility/svgcore/resources/badge_animate.png"; //NOI18N
    private static final String TAG_GRAY_16 = "org/netbeans/modules/mobility/svgcore/resources/tag_gray.png"; //NOI18N
    
    private final Image  ERROR_IMAGE   = ImageUtilities.loadImage(ERROR_16, true);
    private final Image  ANIMATE_IMAGE = ImageUtilities.loadImage(ANIMATE_16, true);
    private final Icon[] TAG_GRAY_ICON = new Icon[]{getImageIcon(TAG_GRAY_16, false), getImageIcon(TAG_GRAY_16, true)};
    private final Icon[] TAG_ICON      = new Icon[]{getImageIcon(TAG_16, false), getImageIcon(TAG_16, true)};
    //private final Icon[] PI_ICON       = new Icon[]{getImageIcon(PI_16, false), getImageIcon(PI_16, true)};
    //private final Icon[] DOCTYPE_ICON  = new Icon[]{getImageIcon(DOCTYPE_16, false), getImageIcon(DOCTYPE_16, true)};
    //private final Icon[] CDATA_ICON    = new Icon[]{getImageIcon(CDATA_16, false), getImageIcon(CDATA_16, true)};

    private final Icon[] ANIMATE_TAG_ICON = new Icon[]{
        new ImageIcon(ImageUtilities.mergeImages( ImageUtilities.loadImage(TAG_16), ANIMATE_IMAGE, 5, 3)),
        TAG_ICON[1]
    };
    
    private final HtmlRenderer.Renderer renderer;
    
    public SVGNavigatorTreeCellRenderer() {
        super();
        renderer = HtmlRenderer.createRenderer();
        renderer.setHtml(true);
    }
    
    public Component getTreeCellRendererComponent(JTree tree, Object value,
            boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        SVGNavigatorNode tna      = (SVGNavigatorNode)value;
        DocumentElement  de       = (DocumentElement)tna.getDocumentElement();        
        String           htmlText = tna.getText(true);
        Component        comp     = renderer.getTreeCellRendererComponent(tree, htmlText, sel, expanded, leaf, row, hasFocus);
        
        comp.setEnabled(tree.isEnabled());
        ((JLabel)comp).setToolTipText(tna.getToolTipText().trim().length() > 0 ? tna.getToolTipText() : null);
        
        boolean containsError = tna.getChildrenErrorCount() > 0;
        
        if ( tna.getNodeVisibility() == SVGNavigatorTree.VISIBILITY_UNDIRECT) {
            setIcon( TAG_GRAY_ICON, containsError);
        } else {
            //normal icons
            if( SVGNavigatorTree.isTreeElement(de)) {
                if (SVGFileModel.isAnimation(de)) {
                    setIcon(ANIMATE_TAG_ICON, containsError);
                } else {
                    setIcon(TAG_ICON, containsError);
                }
            }          
        }
        
        return comp;
    }
    
    private void setIcon(Icon[] icons, boolean containsError) {
        renderer.setIcon(icons[containsError ? 1 : 0]);
    }
    
    private ImageIcon getImageIcon(String name, boolean error){
        ImageIcon icon = ImageUtilities.loadImageIcon(name, false);
        if(error)
            return new ImageIcon(ImageUtilities.mergeImages( icon.getImage(), ERROR_IMAGE, 15, 7 ));
        else
            return icon;
    }    
}
