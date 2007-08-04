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

import java.awt.Component;
import javax.swing.ImageIcon;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;

import org.netbeans.modules.vmd.game.model.GlobalRepository;
import org.netbeans.modules.vmd.game.model.Scene;
import org.netbeans.modules.vmd.game.model.Sprite;
import org.netbeans.modules.vmd.game.model.TiledLayer;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

public class GameDesignTreeNodeRenderer implements TreeCellRenderer {
	
	private static ImageIcon imgGame;
	private static ImageIcon imgSprite;
	private static ImageIcon imgTiled;
	private static ImageIcon imgScene;
	
	static {
		imgGame = new ImageIcon(Utilities.loadImage("org/netbeans/modules/vmd/game/integration/res/gamer_16.png"));
		imgSprite = new ImageIcon(Utilities.loadImage("org/netbeans/modules/vmd/game/model/adapter/res/sprite.png"));
		imgTiled = new ImageIcon(Utilities.loadImage("org/netbeans/modules/vmd/game/model/adapter/res/tiled.png"));
		imgScene = new ImageIcon(Utilities.loadImage("org/netbeans/modules/vmd/game/model/adapter/res/scene.png"));
	}

	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
		if (value instanceof Scene) {
			return new SceneNodeRenderer().getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
		}
		else if (value instanceof TiledLayer) {
			return new TiledLayerNodeRenderer().getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
		}
		else if (value instanceof Sprite) {
			return new SpriteNodeRenderer().getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
		}
		else if (value instanceof GlobalRepository) {
			return new GlobalRepositoryNodeRenderer().getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
		}
		else {
			return new DefaultTreeCellRenderer().getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
		}
	}
	
	private class GlobalRepositoryNodeRenderer extends DefaultTreeCellRenderer {
		public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
			GlobalRepository gr = (GlobalRepository) value;
			DefaultTreeCellRenderer r = (DefaultTreeCellRenderer) super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
			r.setText(NbBundle.getMessage(GameDesignTreeNodeRenderer.class, "GameDesignTreeNodeRenderer.GameDesignNode.name"));
			r.setIcon(imgGame); // NOI18N
			this.setToolTipText(NbBundle.getMessage(GameDesignTreeNodeRenderer.class, "GameDesignTreeNodeRenderer.GameDesignNode.tooltip"));
			return r;
		}	
	}
	
	private class SceneNodeRenderer extends DefaultTreeCellRenderer {
		public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
			Scene scene = (Scene) value;
			DefaultTreeCellRenderer r = (DefaultTreeCellRenderer) super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
			r.setText(scene.getName()); // NOI18N
			r.setIcon(imgScene); // NOI18N
			r.setToolTipText(NbBundle.getMessage(GameDesignTreeNodeRenderer.class, "GameDesignTreeNodeRenderer.SceneNode.tooltip", scene.getName()));
			return r;
		}
	}

	private class TiledLayerNodeRenderer extends DefaultTreeCellRenderer {
		public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
			TiledLayer tiledLayer = (TiledLayer) value;
			DefaultTreeCellRenderer r = (DefaultTreeCellRenderer) super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
			r.setText(tiledLayer.getName());
			r.setIcon(imgTiled); // NOI18N
			r.setToolTipText(NbBundle.getMessage(GameDesignTreeNodeRenderer.class, "GameDesignTreeNodeRenderer.TiledLayerNode.tooltip", tiledLayer.getName()));
			return r;
		}
	}

	private class SpriteNodeRenderer extends DefaultTreeCellRenderer {
		public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
			Sprite sprite = (Sprite) value;
			DefaultTreeCellRenderer r = (DefaultTreeCellRenderer) super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
			r.setText(sprite.getName());
			r.setIcon(imgSprite); // NOI18N
			r.setToolTipText(NbBundle.getMessage(GameDesignTreeNodeRenderer.class, "GameDesignTreeNodeRenderer.SpriteNode.tooltip", sprite.getName()));
			return r;
		}	
	}
	
}
