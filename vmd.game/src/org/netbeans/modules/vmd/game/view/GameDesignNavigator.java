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
import javax.swing.JPanel;
import javax.swing.JTree;
import org.netbeans.modules.vmd.game.model.GlobalRepository;
import org.netbeans.modules.vmd.game.model.adapter.GlobalRepositoryTreeAdapter;

/**
 *
 * @author kaja
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
	}
}
