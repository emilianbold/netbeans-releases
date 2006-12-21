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

import java.awt.Graphics2D;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.swing.AbstractListModel;
import javax.swing.JComponent;
import org.netbeans.modules.vmd.game.model.GlobalRepository;
import org.netbeans.modules.vmd.game.model.GlobalRepositoryListener;
import org.netbeans.modules.vmd.game.model.Layer;
import org.netbeans.modules.vmd.game.model.Scene;
import org.netbeans.modules.vmd.game.model.Sprite;
import org.netbeans.modules.vmd.game.model.TiledLayer;

/**
 *
 * @author kherink
 */
public class GlobalRepositoryListAdapter extends AbstractListModel implements GlobalRepositoryListener {
	
	private static final int ADDITIONAL_LIST_ITEM_COUNT = 3;
	private static final String ADDITIONAL_LIST_ITEM_SCENES_LABEL = "Scenes";
	private static final String ADDITIONAL_LIST_ITEM_TILEDLAYERS_LABEL = "Tiled Layers";
	private static final String ADDITIONAL_LIST_ITEM_SPRITES_LABEL = "Sprites";
	
	private GlobalRepository globalRepository = GlobalRepository.getInstance();	
	
	/** Creates a new instance of GlobalRepositoryListAdapter */
	public GlobalRepositoryListAdapter() {
		this.globalRepository.addGlobalRepositoryListener(this);
	}
	
	//AbstractListModel
	public int getSize() {
		int sceneCount = this.globalRepository.getScenes().size();
		int tiledLayerCount = this.globalRepository.getTiledLayers().size();
		int spriteCount = this.globalRepository.getSprites().size();
		
		return sceneCount + tiledLayerCount + spriteCount + ADDITIONAL_LIST_ITEM_COUNT;
	}

	public Object getElementAt(int index) {
		int sceneCount = this.globalRepository.getScenes().size();
		int tiledLayerCount = this.globalRepository.getTiledLayers().size();
		int spriteCount = this.globalRepository.getSprites().size();
		
		int offset = 0;
		
		if (index == 0)
			return ADDITIONAL_LIST_ITEM_SCENES_LABEL;
		offset += 1;
		
		if (index < sceneCount + offset)
			return this.globalRepository.getScenes().get(index -offset);
		offset += sceneCount;
		
		if (index == offset)
			return ADDITIONAL_LIST_ITEM_TILEDLAYERS_LABEL;
		offset += 1;
		
		if (index < tiledLayerCount + offset)
			return this.globalRepository.getTiledLayers().get(index - offset);
		offset += tiledLayerCount;
		
		if (index == offset)
			return ADDITIONAL_LIST_ITEM_SPRITES_LABEL;
		offset += 1;
		
		if (index < spriteCount + offset)
			return this.globalRepository.getSprites().get(index - offset);
		
		return null;
	}

	//GlobalRepositoryListener
	public void sceneAdded(Scene scene, int index) {
		int offset = 1;
		this.fireIntervalAdded(this, index + offset, index + offset);
	}

	public void sceneRemoved(Scene scene, int index) {
		int offset = 1;
		this.fireIntervalRemoved(this, index + offset, index + offset);
	}

	public void tiledLayerAdded(TiledLayer tiledLayer, int index) {
		int offset = 1 + this.globalRepository.getScenes().size() + 1;
		this.fireIntervalAdded(this, index + offset, index + offset);
	}

	public void tiledLayerRemoved(TiledLayer tiledLayer, int index) {
		int offset = 1 + this.globalRepository.getScenes().size() + 1;
		this.fireIntervalRemoved(this, index + offset, index + offset);
	}

	public void spriteAdded(Sprite sprite, int index) {
		int offset = 1 + this.globalRepository.getScenes().size() + 1 + this.globalRepository.getTiledLayers().size() + 1;
		this.fireIntervalAdded(this, index + offset, index + offset);
	}

	public void spriteRemoved(Sprite sprite, int index) {
		int offset = 1 + this.globalRepository.getScenes().size() + 1 + this.globalRepository.getTiledLayers().size() + 1;
		this.fireIntervalRemoved(this, index + offset, index + offset);
	}
	
}
