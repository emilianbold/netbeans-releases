/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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

package org.netbeans.modules.vmd.game.model.adapter;

import javax.swing.AbstractListModel;
import org.netbeans.modules.vmd.game.model.GlobalRepository;
import org.netbeans.modules.vmd.game.model.GlobalRepositoryListener;
import org.netbeans.modules.vmd.game.model.ImageResource;
import org.netbeans.modules.vmd.game.model.Scene;
import org.netbeans.modules.vmd.game.model.Sprite;
import org.netbeans.modules.vmd.game.model.TiledLayer;
import org.openide.util.NbBundle;

/**
 *
 * 
 */
public class GlobalRepositoryListAdapter extends AbstractListModel implements GlobalRepositoryListener {
	
	private static final int ADDITIONAL_LIST_ITEM_COUNT = 4;
	private static final String ADDITIONAL_LIST_ITEM_SCENES_LABEL = 
			NbBundle.getMessage(GlobalRepositoryListAdapter.class, "GlobalRepositoryListAdapter.labelScenes");
	private static final String ADDITIONAL_LIST_ITEM_TILEDLAYERS_LABEL =
			NbBundle.getMessage(GlobalRepositoryListAdapter.class, "GlobalRepositoryListAdapter.labelTiledLayers");
	private static final String ADDITIONAL_LIST_ITEM_SPRITES_LABEL =
			NbBundle.getMessage(GlobalRepositoryListAdapter.class, "GlobalRepositoryListAdapter.labelSprites");
	
	private GlobalRepository gameDesign;	
	
	/** Creates a new instance of GlobalRepositoryListAdapter */
	public GlobalRepositoryListAdapter() {
	}
	
	public void setGameDesign(GlobalRepository gameDesign) {
		this.gameDesign = gameDesign;
		this.gameDesign.addGlobalRepositoryListener(this);
		this.fireContentsChanged(this, 0, this.getSize() -1);
	}
	
	//AbstractListModel
	public int getSize() {
		if (this.gameDesign == null) {
			return 0;
		}
		int sceneCount = this.gameDesign.getScenes().size();
		int tiledLayerCount = this.gameDesign.getTiledLayers().size();
		int spriteCount = this.gameDesign.getSprites().size();
		
		return sceneCount + tiledLayerCount + spriteCount + ADDITIONAL_LIST_ITEM_COUNT;
	}

	public Object getElementAt(int index) {
		int sceneCount = this.gameDesign.getScenes().size();
		int tiledLayerCount = this.gameDesign.getTiledLayers().size();
		int spriteCount = this.gameDesign.getSprites().size();
		
		int offset = 0;
		
		if (index == 0)
			return this.gameDesign;
		offset += 1;

		if (index == 1)
			return ADDITIONAL_LIST_ITEM_SCENES_LABEL;
		offset += 1;
		
		if (index < sceneCount + offset)
			return this.gameDesign.getScenes().get(index -offset);
		offset += sceneCount;
		
		if (index == offset)
			return ADDITIONAL_LIST_ITEM_TILEDLAYERS_LABEL;
		offset += 1;
		
		if (index < tiledLayerCount + offset)
			return this.gameDesign.getTiledLayers().get(index - offset);
		offset += tiledLayerCount;
		
		if (index == offset)
			return ADDITIONAL_LIST_ITEM_SPRITES_LABEL;
		offset += 1;
		
		if (index < spriteCount + offset)
			return this.gameDesign.getSprites().get(index - offset);
		
		return null;
	}

	//GlobalRepositoryListener
	public void sceneAdded(Scene scene, int index) {
		int offset = 2;
		this.fireIntervalAdded(this, index + offset, index + offset);
	}

	public void sceneRemoved(Scene scene, int index) {
		int offset = 2;
		this.fireIntervalRemoved(this, index + offset, index + offset);
	}

	public void tiledLayerAdded(TiledLayer tiledLayer, int index) {
		int offset = 2 + this.gameDesign.getScenes().size() + 1;
		this.fireIntervalAdded(this, index + offset, index + offset);
	}

	public void tiledLayerRemoved(TiledLayer tiledLayer, int index) {
		int offset = 2 + this.gameDesign.getScenes().size() + 1;
		this.fireIntervalRemoved(this, index + offset, index + offset);
	}

	public void spriteAdded(Sprite sprite, int index) {
		int offset = 2 + this.gameDesign.getScenes().size() + 1 + this.gameDesign.getTiledLayers().size() + 1;
		this.fireIntervalAdded(this, index + offset, index + offset);
	}

	public void spriteRemoved(Sprite sprite, int index) {
		int offset = 2 + this.gameDesign.getScenes().size() + 1 + this.gameDesign.getTiledLayers().size() + 1;
		this.fireIntervalRemoved(this, index + offset, index + offset);
	}
	
    public void imageResourceAdded(ImageResource imageResource) {
    }

}
