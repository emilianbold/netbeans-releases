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
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

public class GameDesignTreeNodeRenderer implements TreeCellRenderer {
	
	private static ImageIcon imgGame;
	private static ImageIcon imgSprite;
	private static ImageIcon imgTiled;
	private static ImageIcon imgScene;
	
	static {
		imgGame = new ImageIcon(ImageUtilities.loadImage("org/netbeans/modules/vmd/game/integration/res/gamer_16.png"));
		imgSprite = new ImageIcon(ImageUtilities.loadImage("org/netbeans/modules/vmd/game/model/adapter/res/sprite.png"));
		imgTiled = new ImageIcon(ImageUtilities.loadImage("org/netbeans/modules/vmd/game/model/adapter/res/tiled.png"));
		imgScene = new ImageIcon(ImageUtilities.loadImage("org/netbeans/modules/vmd/game/model/adapter/res/scene.png"));
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
                @Override
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
                @Override
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
                @Override
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
                @Override
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
