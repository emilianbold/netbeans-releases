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
package org.netbeans.modules.vmd.game.model.adapter;

import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import org.netbeans.modules.vmd.game.model.GlobalRepository;
import org.netbeans.modules.vmd.game.model.GlobalRepositoryListener;
import org.netbeans.modules.vmd.game.model.ImageResource;
import org.netbeans.modules.vmd.game.model.Scene;
import org.netbeans.modules.vmd.game.model.Sprite;
import org.netbeans.modules.vmd.game.model.TiledLayer;
import org.openide.util.NbBundle;

public class GlobalRepositoryTreeAdapter implements TreeModel, GlobalRepositoryListener {

	private ArrayList listeners = new ArrayList();
	
	private static final int INDEX_TILED_LAYER_NODE = 0;
	private static final int INDEX_SPRITE_NODE = 1;
	
	private GlobalRepository globalRepository;
	private SimpleSpritesNode spritesNode = new SimpleSpritesNode();
	private SimpleTiledLayersNode tiledLayersNode = new SimpleTiledLayersNode();
	
	public GlobalRepositoryTreeAdapter(GlobalRepository globalRepository) {
		this.globalRepository = globalRepository;
		this.globalRepository.addGlobalRepositoryListener(this);
	}
	
	public Object getRoot() {
		return this.globalRepository;
	}

	public Object getChild(Object parent, int index) {
		if (parent == this.globalRepository) {
			if (index == INDEX_SPRITE_NODE) {
				return this.spritesNode;
			}
			else if (index == INDEX_TILED_LAYER_NODE) {
				return this.tiledLayersNode;
			}
		}
		else if (parent == this.spritesNode) {
			return globalRepository.getSprites().get(index);
		}
		else if (parent == this.tiledLayersNode) {
			return globalRepository.getTiledLayers().get(index);
		}
		return null;
	}

	public int getChildCount(Object parent) {
		if (parent == this.globalRepository)
			return 2;
		if (parent == this.spritesNode)
			return globalRepository.getSprites().size();
		if (parent == this.tiledLayersNode)
			return globalRepository.getTiledLayers().size();
		return 0;
	}

	public boolean isLeaf(Object node) {
		if (node == this.globalRepository || node == this.tiledLayersNode || node == this.spritesNode)
			return false;
		return true;
	}

	public void valueForPathChanged(TreePath path, Object newValue) {
		//System.out.println("GlobalRepositoryTreeAdapter.valueForPathChanged");
	}

	public int getIndexOfChild(Object parent, Object child) {
		if (parent == this.globalRepository) {
			if (child == this.tiledLayersNode)
				return INDEX_TILED_LAYER_NODE;
			if (child == this.spritesNode)
				return INDEX_SPRITE_NODE;
		}
		if (parent == this.tiledLayersNode)
			return this.globalRepository.getTiledLayers().indexOf(child);
		if (parent == this.spritesNode)
			return this.globalRepository.getSprites().indexOf(child);
		
		return -1;		
	}

	public void addTreeModelListener(TreeModelListener l) {
		this.listeners.add(l);
	}

	public void removeTreeModelListener(TreeModelListener l) {
		this.listeners.remove(l);
	}

	
	private class SimpleSpritesNode {
		public String toString() {
			return NbBundle.getMessage(GlobalRepositoryListAdapter.class, "GlobalRepositoryTreeAdapter.labelSprites");
		}
	}
	private class SimpleTiledLayersNode {
		public String toString() {
			return NbBundle.getMessage(GlobalRepositoryListAdapter.class, "GlobalRepositoryTreeAdapter.labelTiledLayers");
		}
	}
	public void sceneAdded(Scene scene, int index) {
	}

	public void sceneRemoved(Scene scene, int index) {
	}

	public void tiledLayerAdded(TiledLayer tiledLayer, int index) {
		TreePath parent = new TreePath(new Object[] {this.getRoot(), this.tiledLayersNode});
		int[] indicies = {index};
		Object[] children = new Object[] {tiledLayer};
		TreeModelEvent e = new TreeModelEvent(this, parent, indicies, children);
		for (Iterator iter = this.listeners.iterator(); iter.hasNext();) {
			TreeModelListener l = (TreeModelListener) iter.next();
			l.treeNodesInserted(e);
		}
	}

	public void tiledLayerRemoved(TiledLayer tiledLayer, int index) {
		TreePath parent = new TreePath(new Object[] {this.getRoot(), this.tiledLayersNode});
		int[] indicies = {index};
		Object[] children = new Object[] {tiledLayer};
		TreeModelEvent e = new TreeModelEvent(this, parent, indicies, children);
		for (Iterator iter = this.listeners.iterator(); iter.hasNext();) {
			TreeModelListener l = (TreeModelListener) iter.next();
			l.treeNodesRemoved(e);
		}
	}

	public void spriteAdded(Sprite sprite, int index) {
		TreePath parent = new TreePath(new Object[] {this.getRoot(), this.spritesNode});
		int[] indicies = {index};
		Object[] children = new Object[] {sprite};
		TreeModelEvent e = new TreeModelEvent(this, parent, indicies, children);
		for (Iterator iter = this.listeners.iterator(); iter.hasNext();) {
			TreeModelListener l = (TreeModelListener) iter.next();
			l.treeNodesInserted(e);
		}
	}

	public void spriteRemoved(Sprite sprite, int index) {
		TreePath parent = new TreePath(new Object[] {this.getRoot(), this.spritesNode});
		int[] indicies = {index};
		Object[] children = new Object[] {sprite};
		TreeModelEvent e = new TreeModelEvent(this, parent, indicies, children);
		for (Iterator iter = this.listeners.iterator(); iter.hasNext();) {
			TreeModelListener l = (TreeModelListener) iter.next();
			l.treeNodesRemoved(e);
		}
	}
	
    public void imageResourceAdded(ImageResource imageResource) {
    }

}
