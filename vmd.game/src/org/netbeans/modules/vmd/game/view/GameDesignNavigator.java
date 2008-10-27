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
 */package org.netbeans.modules.vmd.game.view;

import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.Action;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.TreePath;
import org.netbeans.modules.vmd.game.model.Editable;
import org.netbeans.modules.vmd.game.model.GlobalRepository;
import org.netbeans.modules.vmd.game.model.adapter.GlobalRepositoryTreeAdapter;

/**
 * @author Karel Herink
 */
public class GameDesignNavigator extends JPanel {
	
	private GlobalRepository gameDesign;
	private JTree tree;
	private GlobalRepositoryTreeAdapter model;
	
    public GameDesignNavigator(GlobalRepository gameDesign) {
		this.gameDesign = gameDesign;
		this.init();
		this.setLayout(new BorderLayout());
		this.add(tree, BorderLayout.CENTER);
    }
	
	private void init() {
		this.model = new GlobalRepositoryTreeAdapter(gameDesign);
		this.tree = new JTree(model);
		this.tree.setRootVisible(true);
		this.tree.setShowsRootHandles(true);
		
		this.tree.setCellRenderer(new GameDesignTreeNodeRenderer());
		
		
		this.tree.addMouseListener(new TreeMousListener());
	}
	
	private class TreeMousListener extends MouseAdapter {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    this.handlePopup(e);
                }
            }
            
            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    this.handlePopup(e);
                }
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() >= 2 && SwingUtilities.isLeftMouseButton(e)) {
                    Object node = getSelectedNodeObj(e);
                    if (node instanceof Editable) {
                        Editable editable = (Editable) node;
                        gameDesign.getMainView().requestEditing(editable);
                    } else {
                        super.mouseClicked(e);
                    }
                } else {
                    super.mouseClicked(e);
                }
            }
            
            private Object getSelectedNodeObj(MouseEvent e){
                TreePath tp = tree.getClosestPathForLocation(e.getX(), e.getY());
                tree.setSelectionPath(tp);
                return tp.getLastPathComponent();
            }
            
            private void handlePopup(MouseEvent e) {
                Object node = getSelectedNodeObj(e);
                if (node instanceof Editable) {
                    Editable editable = (Editable) node;
                    JPopupMenu menu = new JPopupMenu();
                    for (Action action : editable.getActions()) {
                        menu.add(action);
                    }
                    menu.show(tree, e.getX(), e.getY());
                } else {
                    //pretty weird - should be editable :/
                }
            }
	}
	
}









