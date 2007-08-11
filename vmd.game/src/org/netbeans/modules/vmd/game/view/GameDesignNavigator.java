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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.vmd.game.view;

import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.Action;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
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
		public void mousePressed(MouseEvent e) {
			if (e.isPopupTrigger()) {
				this.handlePopup(e);
			}
		}
		public void mouseReleased(MouseEvent e) {
			if (e.isPopupTrigger()) {
				this.handlePopup(e);
			}
		}
		private void handlePopup(MouseEvent e) {
			TreePath tp = tree.getClosestPathForLocation(e.getX(), e.getY());
			Object node = tp.getLastPathComponent();
			tree.setSelectionPath(tp);
			if (node instanceof Editable) {
				Editable editable = (Editable) node;
				JPopupMenu menu = new JPopupMenu();
				for (Action action : editable.getActions()) {
                    menu.add(action);
                }
				menu.show(tree, e.getX(), e.getY());
			}
			else {
				//pretty weird - should be editable :/
			}
		}
	}
	
}









