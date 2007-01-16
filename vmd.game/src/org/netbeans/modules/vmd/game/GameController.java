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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.api.model.TypeID;
import org.netbeans.modules.vmd.game.integration.components.GameTypes;
import org.netbeans.modules.vmd.game.model.AnimatedTile;
import org.netbeans.modules.vmd.game.model.AnimatedTileCD;
import org.netbeans.modules.vmd.game.model.GlobalRepository;
import org.netbeans.modules.vmd.game.model.GlobalRepositoryListener;
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
import org.netbeans.modules.vmd.midp.components.MidpTypes;

/**
 *
 * @author Karel Herink
 */
public class GameController implements DesignDocumentAwareness, GlobalRepositoryListener {
	
	/**
	 * Maps game builder model objects to the component ids of their designer2 design component
	 * counter-parts. E.g. a TiledLayer instance is the key and value is design component id of 
	 * the design component that represents the tiled layer in the design document.
	 */
	private final static Map<Object, DesignComponent> designIdMap = new HashMap<Object, DesignComponent>();
	
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
				if (true) {
					GlobalRepository.getInstance().removeGlobalRepositoryListener(GameController.this);
					
					//clean my internal model - remove all scenes, layers, and image resources
					GlobalRepository.getInstance().removeAllComponents();

					//add all components in the document
					DesignComponent root = designDocument.getRootComponent();
					GameController.this.modelComponent(root);
					
					GlobalRepository.getInstance().addGlobalRepositoryListener(GameController.this);
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
		designIdMap.put(animatedTile, animatedTiledDC);
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
		designIdMap.put(sprite, spriteDC);
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
		designIdMap.put(tiledLayer, tiledLayerDC);
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
		designIdMap.put(sequence, sequenceDC);
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
		
		ImageResource imgRes = GlobalRepository.getInstance().getImageResource(imgResUrl, tileWidth, tileHeight);
		designIdMap.put(imgRes, imageResourceDC);
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
		designIdMap.put(scene, sceneDC);		
		return scene;
	}
	
	public static DesignComponent createSceneDCFromScene(DesignDocument doc, Scene scene) {
		DesignComponent dcScene = doc.createComponent(SceneCD.TYPEID);
		dcScene.writeProperty(SceneCD.PROPERTY_NAME, MidpTypes.createStringValue(scene.getName()));		
		List<PropertyValue> sceneItems = new ArrayList<PropertyValue>();
		List<Layer> layers = scene.getLayers();
		for (Iterator<Layer> it = layers.iterator(); it.hasNext();) {
			DesignComponent sceneItemDC = doc.createComponent(SceneItemCD.TYPEID);			

			Layer layer = it.next();
			DesignComponent layerDC = designIdMap.get(layer);		
			assert (layerDC != null);

			sceneItemDC.writeProperty(SceneItemCD.PROPERTY_LAYER, PropertyValue.createComponentReference(layerDC));
			sceneItemDC.writeProperty(SceneItemCD.PROPERTY_LOCK, MidpTypes.createBooleanValue(scene.isLayerLocked(layer)));
			sceneItemDC.writeProperty(SceneItemCD.PROPERTY_VISIBLE, MidpTypes.createBooleanValue(scene.isLayerLocked(layer)));
			sceneItemDC.writeProperty(SceneItemCD.PROPERTY_POSITION, GameTypes.createPointProperty(scene.getLayerPosition(layer)));
			
			PropertyValue sceneItemPropVal = PropertyValue.createComponentReference(sceneItemDC);
			sceneItems.add(sceneItemPropVal);
		}
		dcScene.writeProperty(SceneCD.PROPERTY_SCENE_ITEMS, PropertyValue.createArray(SceneItemCD.TYPEID, sceneItems));
		
		designIdMap.put(scene, dcScene);
		return dcScene;
	}
	
	public static DesignComponent createTiledLayerDCFromTiledLayer(DesignDocument doc, TiledLayer layer) {
		DesignComponent dcLayer = doc.createComponent(TiledLayerCD.TYPEID);
		dcLayer.writeProperty(LayerCD.PROPERTY_NAME, MidpTypes.createStringValue(layer.getName()));
		
		DesignComponent dcImgRes = designIdMap.get(layer.getImageResource());
		assert(dcImgRes != null);
		
		dcLayer.writeProperty(LayerCD.PROPERTY_IMAGE_RESOURCE, PropertyValue.createComponentReference(dcImgRes));
		PropertyValue propTiles = GameTypes.createTilesProperty(layer.getTiles());
		dcLayer.writeProperty(TiledLayerCD.PROPERTY_TILES, propTiles);
				
		designIdMap.put(layer, dcLayer);
		return dcLayer;
	}
	
	public static DesignComponent createSpriteDCFromSprite(DesignDocument doc, Sprite layer) {
		DesignComponent dcLayer = doc.createComponent(SpriteCD.TYPEID);
		dcLayer.writeProperty(LayerCD.PROPERTY_NAME, MidpTypes.createStringValue(layer.getName()));
		
		DesignComponent dcImgRes = designIdMap.get(layer.getImageResource());
		assert(dcImgRes != null);
		dcLayer.writeProperty(LayerCD.PROPERTY_IMAGE_RESOURCE, PropertyValue.createComponentReference(dcImgRes));
		
		Sequence defaultSequence = layer.getDefaultSequence();
		DesignComponent dcDefSequence = designIdMap.get(defaultSequence);
		assert(dcDefSequence != null);
		dcLayer.writeProperty(SpriteCD.PROP_DEFAULT_SEQUENCE, PropertyValue.createComponentReference(dcDefSequence));
		
		List<PropertyValue> sequenceDCs = new ArrayList<PropertyValue>();
		List<Sequence> sequences = layer.getSequences();
		for (Sequence sequence : sequences) {
			DesignComponent dcSequence = designIdMap.get(sequence);
			assert(dcDefSequence != null);
			PropertyValue seqPropVal = PropertyValue.createComponentReference(dcImgRes);
			sequenceDCs.add(seqPropVal);
		}
		dcLayer.writeProperty(SpriteCD.PROP_SEQUENCES, PropertyValue.createArray(SequenceCD.TYPEID, sequenceDCs));
		
		designIdMap.put(layer, dcLayer);
		return  dcLayer;
	}
	
	public static DesignComponent createImageResourceDCFromImageResource(DesignDocument doc, ImageResource imageResource) {
		DesignComponent dcImgRes = doc.createComponent(ImageResourceCD.TYPEID);
		dcImgRes.writeProperty(ImageResourceCD.PROPERTY_IMAGE_URL, MidpTypes.createStringValue(imageResource.getURL().toExternalForm()));
		dcImgRes.writeProperty(ImageResourceCD.PROPERTY_TILE_WIDTH, MidpTypes.createIntegerValue(imageResource.getCellWidth()));
		dcImgRes.writeProperty(ImageResourceCD.PROPERTY_TILE_HEIGHT, MidpTypes.createIntegerValue(imageResource.getCellHeight()));
		
		designIdMap.put(imageResource, dcImgRes);
		return dcImgRes;
	}
	
	//--------------- GlobalRepositoryListener ------------------
	
    public void sceneAdded(final Scene scene, int index) {
		this.document.getTransactionManager().writeAccess(new Runnable() {
            public void run() {
				DesignDocument doc = GameController.this.document;
				DesignComponent sceneDC = createSceneDCFromScene(doc, scene);
				doc.getRootComponent().addComponent(sceneDC);
            }
		});
    }

    public void sceneRemoved(Scene scene, int index) {
		DesignComponent dcScene = designIdMap.remove(scene);
		assert (dcScene != null);
		this.document.deleteComponent(dcScene);
    }

    public void tiledLayerAdded(final TiledLayer tiledLayer, int index) {
		this.document.getTransactionManager().writeAccess(new Runnable() {
            public void run() {
				DesignDocument doc = GameController.this.document;
				DesignComponent tiledLayerDC = createTiledLayerDCFromTiledLayer(doc, tiledLayer);
				doc.getRootComponent().addComponent(tiledLayerDC);
            }
		});		
    }

    public void tiledLayerRemoved(TiledLayer tiledLayer, int index) {
		DesignComponent dcLayer = designIdMap.remove(tiledLayer);
		assert (dcLayer != null);
		this.document.deleteComponent(dcLayer);
    }

    public void spriteAdded(final Sprite sprite, int index) {
		this.document.getTransactionManager().writeAccess(new Runnable() {
            public void run() {
				DesignDocument doc = GameController.this.document;
				DesignComponent spriteDC = createSpriteDCFromSprite(doc, sprite);
				doc.getRootComponent().addComponent(spriteDC);
            }
		});
    }

    public void spriteRemoved(Sprite sprite, int index) {
		DesignComponent dcLayer = designIdMap.remove(sprite);
		assert (dcLayer != null);
		this.document.deleteComponent(dcLayer);
    }

    public void imageResourceAdded(final ImageResource imageResource) {
		this.document.getTransactionManager().writeAccess(new Runnable() {
            public void run() {
				DesignDocument doc = GameController.this.document;
				DesignComponent imgResDC = createImageResourceDCFromImageResource(doc, imageResource);
				doc.getRootComponent().addComponent(imgResDC);
            }
		});
    }

}
