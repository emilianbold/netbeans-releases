/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
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

package org.netbeans.modules.vmd.inspector;

import java.awt.Image;
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
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 *
 * @author Karol Harezlak
 */
final class InspectorBeanTreeView extends BeanTreeView {

    public InspectorBeanTreeView(final ExplorerManager explorerManager) {
        final JPopupMenu popupMenu = new JPopupMenu();
        Image collapseImage = Utilities.loadImage ("org/netbeans/modules/vmd/inspector/resources/collapse-all.png");
        Image expandImage = Utilities.loadImage ("org/netbeans/modules/vmd/inspector/resources/expand-all.png");
        
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