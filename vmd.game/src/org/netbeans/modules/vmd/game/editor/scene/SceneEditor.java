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

package org.netbeans.modules.vmd.game.editor.scene;

import javax.swing.JComponent;
import javax.swing.JScrollPane;

import org.netbeans.modules.vmd.game.model.Scene;

/**
 *
 * @author Thomas Rae
 */

public class SceneEditor {
	public static final boolean DEBUG = false;
	
	private Scene scene;
	private JScrollPane scroll;
	
	/** Creates a new instance of SceneEditor */
	public SceneEditor(Scene scene) {
		this.scene = scene;
		ScenePanel editor = new ScenePanel(scene);
		this.scroll = new JScrollPane(editor);
		scroll.setColumnHeaderView(editor.getRulerHorizontal());
		scroll.setRowHeaderView(editor.getRulerVertical());
		scroll.setCorner(JScrollPane.UPPER_LEFT_CORNER, editor.getGridButton());
	}
	
	public JComponent getJComponent() {
		return this.scroll;
	}
}
