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

import javax.swing.JTable;
import org.netbeans.modules.vmd.game.model.Editable;
import org.netbeans.modules.vmd.game.model.GlobalRepository;
import org.netbeans.modules.vmd.game.model.adapter.GlobalRepositoryTableAdapter;

/**
 *
 * @author kaja
 */
public class GameDesignNavigator extends JTable {

	
	
	public static final int PAD_X = 4;
	public static final int PAD_Y = 4;
	
	private static final int IMG_PREVIEW_WIDTH = 40;
	private static final int IMG_PREVIEW_HEIGHT = 30;
	
	private GlobalRepository gameDesign;
	
    public GameDesignNavigator(GlobalRepository gameDesign) {
		System.out.println(">>>>>>>>>> GAME DESIGN: " + gameDesign); // NOI18N
		this.gameDesign = gameDesign;
		this.getColumnModel().setColumnMargin(0);
		this.setRowHeight(IMG_PREVIEW_HEIGHT);
		this.setModel(new GlobalRepositoryTableAdapter(gameDesign));
		this.setDefaultRenderer(Editable.class, new GameDesignTableCellRenderer());
    }

}
