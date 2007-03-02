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

package org.netbeans.modules.vmd.game;

import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import org.netbeans.modules.vmd.game.model.Editable;
import org.netbeans.modules.vmd.game.model.EditorManagerListener;
import org.netbeans.modules.vmd.game.model.GlobalRepository;
import org.netbeans.spi.navigator.NavigatorPanel;
import org.openide.util.Lookup;

/**
 *
 * @author kaja
 */
public class GameBuilderNavigator extends JPanel implements NavigatorPanel, EditorManagerListener {
    
	private GlobalRepository gameDesign;
	

	public GameBuilderNavigator() {
		this.setNavigator(null);
	}
	
	/** Creates a new instance of GameBuilderNavigator */
    public GameBuilderNavigator(GlobalRepository gameDesign) {
		System.out.println("GameBuilderNavigator instance created!");
		this.gameDesign = gameDesign;
		this.gameDesign.getMainView().addEditorManagerListener(this);
		Editable editable = this.gameDesign.getMainView().getCurrentEditable();
		if (editable == null)
			return;
		JComponent navigator = editable.getNavigator();
		this.setNavigator(navigator);
    }
    
    public String getDisplayName() {
        return "Game Builder Navigator";
    }

    public String getDisplayHint() {
        return "Show logical structure of game design components.";
    }

    public JComponent getComponent() {
        return this;
    }

    public void panelActivated(Lookup context) {
    }

    public void panelDeactivated() {
    }

    public Lookup getLookup() {
        return null;
    }
    
	private void setNavigator(JComponent navigator) {
		System.out.println("setNavigator: " + navigator);
		this.removeAll();
		if (navigator != null) {
			this.setLayout(new BorderLayout());
			this.add(new JScrollPane(navigator), BorderLayout.CENTER);
		}
		else {
			this.setLayout(new GridBagLayout());
			this.add(new JLabel("<No structure available>"));
		}
		this.validate();
		this.repaint();
	}
    
    public void editing(Editable e) {
		this.setNavigator(e.getNavigator());
    }


}
