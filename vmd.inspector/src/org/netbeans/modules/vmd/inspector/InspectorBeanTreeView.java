/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.vmd.inspector;

import java.awt.Image;
import java.awt.dnd.DnDConstants;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JPopupMenu;

import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.BeanTreeView;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 *
 * @author Karol Harezlak
 */
final class InspectorBeanTreeView extends BeanTreeView {

    public InspectorBeanTreeView(final ExplorerManager explorerManager) {
        final JPopupMenu popupMenu = new JPopupMenu();
        Image collapseImage = ImageUtilities.loadImage ("org/netbeans/modules/vmd/inspector/resources/collapse-all.png"); //NOI18N
        Image expandImage = ImageUtilities.loadImage ("org/netbeans/modules/vmd/inspector/resources/expand-all.png"); //NOI18N
        this.setAllowedDragActions(DnDConstants.ACTION_COPY_OR_MOVE);
        
        popupMenu.add( new MenuAction(NbBundle.getMessage(InspectorBeanTreeView.class, "CTL_InspectorExpandAction" ), expandImage) { //NOI18N
            public void actionPerformed(ActionEvent e) {
                InspectorBeanTreeView.this.expandAll();
            }
        });
        
        popupMenu.add( new MenuAction(NbBundle.getMessage(InspectorBeanTreeView.class, "CTL_InspectorCollapseAction" ), collapseImage ) { //NOI18N
            public void actionPerformed(ActionEvent e) { 
                if  (explorerManager.getRootContext().getChildren().getNodes() == null 
                     || explorerManager.getRootContext().getChildren().getNodes().length == 0 )
                        return;
                
                Node[] rootNodes = explorerManager.getRootContext().getChildren().getNodes();
                for (Node node : rootNodes) {
                    deepDive(node);
                }
            }
            
            private void deepDive(Node parentNode) {
                for (Node node  : parentNode.getChildren().getNodes()) {
                    deepDive(node);
                }
                collapseNode(parentNode);
            }
        });
        
        tree.addMouseListener(new MouseListener() {
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == 3
                    && explorerManager.getSelectedNodes().length == 1
                    && explorerManager.getSelectedNodes()[0] == explorerManager.getRootContext()) {
                        popupMenu.show(e.getComponent(),e.getX(), e.getY());
                }
            }
            public void mouseEntered(MouseEvent e) {
            }
            public void mouseExited(MouseEvent e) {
            }
            public void mousePressed(MouseEvent e) {
            }
            public void mouseReleased(MouseEvent e) {
            }
        });
        
    }
    
    private abstract class MenuAction extends AbstractAction {
        public MenuAction(String name, Image icon) {
            putValue(Action.NAME, name);
            putValue(Action.SMALL_ICON, new ImageIcon(icon));
        }
    }
    
    
}