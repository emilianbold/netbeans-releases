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
package org.netbeans.modules.vmd.game.view;

import org.netbeans.modules.vmd.game.editor.scene.*;
import java.awt.Component;
import javax.swing.ImageIcon;

import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import org.netbeans.modules.vmd.game.model.Layer;
import org.netbeans.modules.vmd.game.model.Scene;
import org.netbeans.modules.vmd.game.model.TiledLayer;
import org.netbeans.modules.vmd.game.model.Sprite;
import org.openide.util.NbBundle;


public class GameDesignTableCellRenderer extends DefaultTableCellRenderer {
	
	private ImageIcon iconSprite = new ImageIcon(this.getClass().getResource("res/sprite.png")); // NOI18N
	private ImageIcon iconTiledLayer = new ImageIcon(this.getClass().getResource("res/tiled.png")); // NOI18N
	private ImageIcon iconScene = new ImageIcon(this.getClass().getResource("res/scene.png")); // NOI18N

	
	public Component getTableCellRendererComponent(JTable table, final Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		if (value instanceof Layer) {
			this.setText(null);
			this.setHorizontalAlignment(SwingConstants.CENTER);
			if (value instanceof Sprite) {
				this.setIcon(iconSprite);
				this.setToolTipText(NbBundle.getMessage(GameDesignTableCellRenderer.class, "GameDesignTableCellRenderer.iconSprite.tooltip"));
			}
			else if (value instanceof TiledLayer) {
				this.setIcon(iconTiledLayer);
				this.setToolTipText(NbBundle.getMessage(GameDesignTableCellRenderer.class, "GameDesignTableCellRenderer.iconTiledLayer.tooltip"));
			}
		}
		else if (value instanceof Scene) {
			this.setIcon(iconScene);
			this.setToolTipText(NbBundle.getMessage(GameDesignTableCellRenderer.class, "GameDesignTableCellRenderer.iconScene.tooltip"));
		}
		else {
			throw new IllegalArgumentException("Only Layer or Scene can be rendered."); // NOI18N
		}
		return this;
	}
		
}
