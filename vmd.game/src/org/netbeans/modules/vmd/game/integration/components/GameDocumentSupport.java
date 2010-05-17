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
package org.netbeans.modules.vmd.game.integration.components;

import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.DesignDocument;
import org.netbeans.modules.vmd.api.model.PropertyValue;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.CannotRedoException;
import org.netbeans.modules.vmd.game.model.SpriteCD;
import org.netbeans.modules.vmd.game.model.TiledLayerCD;

/**
 * @author David Kaspar
 */
public class GameDocumentSupport {
	
	//this class must listen for changes in my model and add items to designer2 model
	//i.e GameDocumentSupport implements SceneListener
	
	public static DesignComponent addSprite(DesignDocument document) {
		DesignComponent rootComponent = document.getRootComponent();
		DesignComponent sprite = document.createComponent(SpriteCD.TYPEID);
		rootComponent.addComponent(sprite);
		
		// TODO - initialize sprite
		
		return sprite;
	}
	
//	public static DesignComponent addSpriteSequence(DesignComponent sprite) {
//		DesignDocument document = sprite.getDocument();
//		DesignComponent spriteSequenceComponent = document.createComponent(SpriteSequenceCD.TYPEID);
//		sprite.addComponent(spriteSequenceComponent);
//		spriteSequenceComponent.writeProperty(SpriteSequenceCD.PROP_SPRITE, PropertyValue.createComponentReference(sprite));
//		// TODO - initialize sprite sequence
//		return spriteSequenceComponent;
//	}
//	
//	public static void removeSpriteSequnce(DesignComponent spriteSequence) {
//		DesignComponent spriteComponent = spriteSequence.getParentComponent();
//		spriteComponent.writeProperty(SpriteSequenceCD.PROP_SPRITE, PropertyValue.createNull());
//		spriteComponent.addComponent(spriteSequence);
//	}
	
	public static DesignComponent addTiledLayer(DesignDocument document) {
		//HINT -- here show a dialog to select an image
		DesignComponent rootComponent = document.getRootComponent();
		DesignComponent tiledLayer = document.createComponent(TiledLayerCD.TYPEID);
		rootComponent.addComponent(tiledLayer);
		
		// TODO - initialize tiled layer
		
		return tiledLayer;
	}
	
	// TODO - replace by TiledLayer.ROWS property
	public static int getRowCount(PropertyValue tilesPropertyValue) {
		if (tilesPropertyValue == null)
			return 0;
		return GameTypes.getTiles(tilesPropertyValue).length;
	}
	
	// TODO - replace by TiledLayer.COLUMNS property
	public static int getColumnCount(PropertyValue tilesPropertyValue) {
		if (tilesPropertyValue == null)
			return 0;
		int[][] tiles = GameTypes.getTiles(tilesPropertyValue);
		if (tiles.length < 1)
			return 0;
		return tiles[0].length;
	}
	
	public static void setSingleTile(DesignComponent tiledLayer, int y, int x, int newValue) {
		PropertyValue tilesPropertyValue = tiledLayer.readProperty(TiledLayerCD.PROPERTY_TILES);
		int[][] tiles = GameTypes.getTiles(tilesPropertyValue);
		int oldValue = tiles[y][x]; // TODO - check for boundaries
		tiles[y][x] = newValue;
		TilesChangeUndoableEdit undoableEdit = new TilesChangeUndoableEdit(tiledLayer, y, x, oldValue, newValue);
		tiledLayer.getDocument().getTransactionManager().undoableEditHappened(undoableEdit);
		// HINT - notify designer that internal structure was changed
		tiledLayer.writeProperty(TiledLayerCD.PROPERTY_TILES, GameTypes.createTilesProperty(tiles));
	}
	
	private static class TilesChangeUndoableEdit extends AbstractUndoableEdit {
		
		private DesignComponent tiledLayer;
		private int y;
		private int x;
		private int oldValue;
		private int newValue;
		
		public TilesChangeUndoableEdit(DesignComponent tiledLayer, int y, int x, int oldValue, int newValue) {
			this.tiledLayer = tiledLayer;
			this.y = y;
			this.x = x;
			this.oldValue = oldValue;
			this.newValue = newValue;
		}
		
		public void undo() throws CannotUndoException {
			super.undo();
			PropertyValue tilesPropertyValue = tiledLayer.readProperty(TiledLayerCD.PROPERTY_TILES);
			int[][] tiles = GameTypes.getTiles(tilesPropertyValue);
			tiles[y][x] = oldValue;
			// TODO - invoke repaint of particular area at TiledLayerEditor
		}
		
		public void redo() throws CannotRedoException {
			super.redo();
			PropertyValue tilesPropertyValue = tiledLayer.readProperty(TiledLayerCD.PROPERTY_TILES);
			int[][] tiles = GameTypes.getTiles(tilesPropertyValue);
			tiles[y][x] = newValue;
			// TODO - invoke repaint of particular area at TiledLayerEditor
		}
		
		public boolean isSignificant() {
			return false;
		}
		
	}
	
}
