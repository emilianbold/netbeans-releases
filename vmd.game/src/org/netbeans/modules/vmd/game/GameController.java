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
import java.awt.Point;
import java.net.MalformedURLException;
import java.net.URL;
import org.netbeans.modules.vmd.api.io.DataObjectContext;
import org.netbeans.modules.vmd.api.io.DesignDocumentAwareness;
import org.netbeans.modules.vmd.api.io.IOUtils;
import org.netbeans.modules.vmd.api.model.DesignDocument;
import java.util.Collection;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.api.model.TypeID;
import org.netbeans.modules.vmd.game.model.AnimatedTile;
import org.netbeans.modules.vmd.game.model.AnimatedTileCD;
import org.netbeans.modules.vmd.game.model.GlobalRepository;
import org.netbeans.modules.vmd.game.model.ImageResource;
import org.netbeans.modules.vmd.game.model.ImageResourceCD;
import org.netbeans.modules.vmd.game.model.Layer;
import org.netbeans.modules.vmd.game.model.LayerCD;
import org.netbeans.modules.vmd.game.model.Scene;
import org.netbeans.modules.vmd.game.model.SceneCD;
import org.netbeans.modules.vmd.game.model.SceneItemCD;
import org.netbeans.modules.vmd.game.model.Sequence;
import org.netbeans.modules.vmd.game.model.SequenceCD;
import org.netbeans.modules.vmd.game.model.Sprite;
import org.netbeans.modules.vmd.game.model.SpriteCD;
import org.netbeans.modules.vmd.game.model.StaticTile;
import org.netbeans.modules.vmd.game.model.Tile;
import org.netbeans.modules.vmd.game.model.TiledLayer;
import org.netbeans.modules.vmd.game.model.TiledLayerCD;
import org.netbeans.modules.vmd.game.view.main.MainView;

/**
 *
 * @author Karel Herink
 */
public class GameController implements DesignDocumentAwareness {
	
	public static final String PROJECT_TYPE_GAME = "vmd-midp-game"; // NOI18N
	
	private DataObjectContext context;
	private JComponent loadingPanel;
	private JPanel panel = new JPanel(new BorderLayout());
	private JComponent view; //either loadingPanel or game editor
	
	private DesignDocument document;
	
	
	/** Creates a new instance of GameController */
	public GameController(DataObjectContext context) {
		this.context = context;
		this.loadingPanel = IOUtils.createLoadingPanel();
		this.panel.add(this.loadingPanel);
		this.context.addDesignDocumentAwareness(this);
	}
	
	public JComponent getVisualRepresentation() {
		return this.panel;
	}
	
	public void setDesignDocument(final DesignDocument designDocument) {
		this.panel.removeAll();
				
		this.document = designDocument;
		
		if (this.document == null) {
			this.view = this.loadingPanel;
			return;
		}
		this.view = MainView.getInstance().getRootComponent();
		this.document.getTransactionManager().readAccess(new Runnable() {
			public void run() {
				if (false) {
					//clean my internal model - remove all scenes, layers, and image resources
					GlobalRepository.getInstance().removeAllComponents();

					//add all components in the document
					DesignComponent root = designDocument.getRootComponent();
					GameController.this.modelComponent(root);
				}
			}
		});
		this.panel.add(view);
	}
	
	private void modelComponent(DesignComponent designComponent) {
		Collection<DesignComponent> children = designComponent.getComponents();
		if (children.size() > 0) {
			for (DesignComponent child : children) {
				this.modelComponent(child);
			}
		}
		TypeID typeId = designComponent.getType();
		
		if (typeId == SceneCD.TYPEID) {
			this.constructScene(designComponent);
		}
		else if (typeId == TiledLayerCD.TYPEID) {
			this.constructTiledLayer(designComponent);
		}
		else if (typeId == SpriteCD.TYPEID) {
			this.constructSprite(designComponent);
		}
		else if (typeId == SequenceCD.TYPEID) {
			this.constructSequence(designComponent);
		}
		else if (typeId == ImageResourceCD.TYPEID) {
			this.constructImageResource(designComponent);
		}
		else if (typeId == AnimatedTileCD.TYPEID) {
			this.constructAnimatedTile(designComponent);
		}
	}
	
	private AnimatedTile constructAnimatedTile(DesignComponent animatedTiledDC) {
		String name = (String) animatedTiledDC.readProperty(AnimatedTileCD.PROPERTY_NAME).getPrimitiveValue();
		DesignComponent imgResDC = animatedTiledDC.readProperty(AnimatedTileCD.PROP_IMAGE_RESOURCE).getComponent();
		ImageResource imgRes = this.constructImageResource(imgResDC);
		//if ImageResource already has an animated tile of that name it must have been already constructed
		AnimatedTile animatedTile = imgRes.getAnimatedTileByName(name);
		if (animatedTile != null) {
			return animatedTile;
		}
		
		DesignComponent defaultSequenceDC = animatedTiledDC.readProperty(AnimatedTileCD.PROP_DEFAULT_SEQUENCE).getComponent();
		List<PropertyValue> sequenceDCs = animatedTiledDC.readProperty(AnimatedTileCD.PROP_SEQUENCES).getArray();

		Sequence defaultSequence = this.constructSequence(defaultSequenceDC);
		
		animatedTile = imgRes.createAnimatedTile(name, Tile.EMPTY_TILE_INDEX);
		animatedTile.setDefaultSequence(defaultSequence);
		for (PropertyValue propertyValue : sequenceDCs) {
			DesignComponent sequenceDC = propertyValue.getComponent();
			Sequence sequence = this.constructSequence(sequenceDC);
			animatedTile.append(sequence);
		}		
		
		return animatedTile;
	}
	
	private Sprite constructSprite(DesignComponent spriteDC) {
		String name = (String) spriteDC.readProperty(LayerCD.PROPERTY_NAME).getPrimitiveValue();
		//if GlobalRepository already has a layer of that name it must have been already constructed
		Sprite sprite = (Sprite) GlobalRepository.getInstance().getLayerByName(name);
		if (sprite != null) {
			return sprite;
		}
		DesignComponent imgResDC = spriteDC.readProperty(LayerCD.PROPERTY_IMAGE_RESOURCE).getComponent();
		ImageResource imgRes = this.constructImageResource(imgResDC);

		DesignComponent defaultSequenceDC = spriteDC.readProperty(SpriteCD.PROP_DEFAULT_SEQUENCE).getComponent();
		List<PropertyValue> sequenceDCs = spriteDC.readProperty(SpriteCD.PROP_SEQUENCES).getArray();

		Sequence defaultSequence = this.constructSequence(defaultSequenceDC);
		
		sprite = GlobalRepository.getInstance().createSprite(name, imgRes, 1);
		sprite.setDefaultSequence(defaultSequence);
		for (PropertyValue propertyValue : sequenceDCs) {
			DesignComponent sequenceDC = propertyValue.getComponent();
			Sequence sequence = this.constructSequence(sequenceDC);
			sprite.append(sequence);
		}		
		
		return sprite;
	}
	
	private TiledLayer constructTiledLayer(DesignComponent tiledLayerDC) {
		String name = (String) tiledLayerDC.readProperty(LayerCD.PROPERTY_NAME).getPrimitiveValue();
		//if GlobalRepository already has a layer of that name it must have been already constructed
		TiledLayer tiledLayer = (TiledLayer) GlobalRepository.getInstance().getLayerByName(name);
		if (tiledLayer != null) {
			return tiledLayer;
		}
		DesignComponent imgResDC = tiledLayerDC.readProperty(LayerCD.PROPERTY_IMAGE_RESOURCE).getComponent();
		ImageResource imgRes = this.constructImageResource(imgResDC);
		int[][]  grid = (int[][]) tiledLayerDC.readProperty(TiledLayerCD.PROPERTY_TILES).getPrimitiveValue();
		
		GlobalRepository.getInstance().createTiledLayer(name, imgRes, grid);
		return tiledLayer;
	}
	
	private Sequence constructSequence(DesignComponent sequenceDC) {
		String name = (String) sequenceDC.readProperty(SequenceCD.PROPERTY_NAME).getPrimitiveValue();
		DesignComponent imgResDC = sequenceDC.readProperty(LayerCD.PROPERTY_IMAGE_RESOURCE).getComponent();
		ImageResource imgRes = this.constructImageResource(imgResDC);
		
		//if GlobalRepository already has a sequence of that name it must have been already constructed
		Sequence sequence = imgRes.getSequenceByName(name);
		if (sequence != null) {
			return sequence;
		}
		
		int[]  frames = (int[]) sequenceDC.readProperty(SequenceCD.PROPERTY_FRAMES).getPrimitiveValue();
		int frameMs = (Integer) sequenceDC.readProperty(SequenceCD.PROPERTY_FRAME_MS).getPrimitiveValue();
		sequence = imgRes.createSequence(name);
		sequence.setFrameMs(frameMs);
		for (int i : frames) {
			sequence.addFrame((StaticTile) imgRes.getTile(i));
		}
		
		return sequence;
	}	
	
	private ImageResource constructImageResource(DesignComponent imageResourceDC) {
		String imgResUrlString = (String) imageResourceDC.readProperty(ImageResourceCD.PROPERTY_IMAGE_URL).getPrimitiveValue();
		URL imgResUrl = null;
		try {
			imgResUrl = new URL(imgResUrlString);
		} catch (MalformedURLException e) {
			//TODO here probably display a dialog allowing a user to supply new image location instead of bailing
			throw new RuntimeException(e);
		}
		
		int tileHeight = (Integer) imageResourceDC.readProperty(ImageResourceCD.PROPERTY_TILE_HEIGHT).getPrimitiveValue();
		int tileWidth = (Integer) imageResourceDC.readProperty(ImageResourceCD.PROPERTY_TILE_WIDTH).getPrimitiveValue();		
		
		ImageResource imgRes = GlobalRepository.getImageResource(imgResUrl, tileWidth, tileHeight);
		return imgRes;
	}
	
	private Scene constructScene(DesignComponent sceneDC) {
		String name = (String) sceneDC.readProperty(SceneCD.PROPERTY_NAME).getPrimitiveValue();
		//if GlobalRepository already has a scene of that name it must have been already constructed
		Scene scene = GlobalRepository.getInstance().getSceneByName(name);
		if (scene != null) {
			return scene;
		}
		scene = GlobalRepository.getInstance().createScene(name);
		List<PropertyValue> sceneItemsProps = sceneDC.readProperty(SceneCD.PROPERTY_SCENE_ITEMS).getArray();
		for (PropertyValue sceneItemProp : sceneItemsProps) {
			DesignComponent sceneItemDC = sceneItemProp.getComponent();
			
			Point layerLocation = (Point) sceneItemDC.readProperty(SceneItemCD.PROPERTY_POSITION).getPrimitiveValue();
			Boolean locked = (Boolean) sceneItemDC.readProperty(SceneItemCD.PROPERTY_LOCK).getPrimitiveValue();
			Boolean visible = (Boolean) sceneItemDC.readProperty(SceneItemCD.PROPERTY_VISIBLE).getPrimitiveValue();
			DesignComponent layerDC = sceneItemDC.readProperty(SceneItemCD.PROPERTY_LAYER).getComponent();
			if (layerDC.getType() == TiledLayerCD.TYPEID) {
				this.constructTiledLayer(layerDC);
			}
			else {
				this.constructSprite(layerDC);
			}
			
			String layerName = (String) layerDC.readProperty(LayerCD.PROPERTY_NAME).getPrimitiveValue();
			
			Layer layer = GlobalRepository.getInstance().getLayerByName(layerName);
			scene.append(layer);
			scene.setLayerPosition(layer, layerLocation);
			scene.setLayerVisible(layer, visible);
			scene.setLayerLocked(layer, locked);
		}
		return scene;
	}
	
}
