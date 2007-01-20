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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;
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
import org.netbeans.modules.vmd.game.model.ImageResourceListener;
import org.netbeans.modules.vmd.game.model.Layer;
import org.netbeans.modules.vmd.game.model.LayerCD;
import org.netbeans.modules.vmd.game.model.Scene;
import org.netbeans.modules.vmd.game.model.Scene.LayerInfo;
import org.netbeans.modules.vmd.game.model.SceneCD;
import org.netbeans.modules.vmd.game.model.SceneItemCD;
import org.netbeans.modules.vmd.game.model.SceneListener;
import org.netbeans.modules.vmd.game.model.Sequence;
import org.netbeans.modules.vmd.game.model.SequenceCD;
import org.netbeans.modules.vmd.game.model.SequenceContainer;
import org.netbeans.modules.vmd.game.model.SequenceContainerCDProperties;
import org.netbeans.modules.vmd.game.model.SequenceContainerListener;
import org.netbeans.modules.vmd.game.model.SequenceListener;
import org.netbeans.modules.vmd.game.model.Sprite;
import org.netbeans.modules.vmd.game.model.SpriteCD;
import org.netbeans.modules.vmd.game.model.StaticTile;
import org.netbeans.modules.vmd.game.model.Tile;
import org.netbeans.modules.vmd.game.model.TiledLayer;
import org.netbeans.modules.vmd.game.model.TiledLayerCD;
import org.netbeans.modules.vmd.game.model.TiledLayerListener;
import org.netbeans.modules.vmd.game.view.main.MainView;
import org.netbeans.modules.vmd.midp.components.MidpTypes;

/**
 *
 * @author Karel Herink
 */
public class GameController implements DesignDocumentAwareness, GlobalRepositoryListener, 
		SceneListener, TiledLayerListener, SequenceContainerListener, ImageResourceListener,
		SequenceListener, PropertyChangeListener {
	
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
		
		JComponent view = null;

		if (designDocument == null) {
			view = this.loadingPanel;
		}
		else {
			view = MainView.getInstance().getRootComponent();
			designDocument.getTransactionManager().readAccess(new Runnable() {
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
		}
		this.panel.add(view);
		this.panel.validate();
	}
	
	private void modelComponent(DesignComponent designComponent) {
		//this is sometimes null when i close and open the same project :(
		if (designComponent == null) {
			return;
		}
		
		Collection<DesignComponent> children = designComponent.getComponents();
		if (children.size() > 0) {
			for (DesignComponent child : children) {
				this.modelComponent(child);
			}
		}
		TypeID typeId = designComponent.getType();
		
		if (typeId == SceneCD.TYPEID) {
			Scene scene = this.constructScene(designComponent);
			scene.addSceneListener(this);
			designIdMap.put(scene, designComponent);
		}
		else if (typeId == TiledLayerCD.TYPEID) {
			TiledLayer layer = this.constructTiledLayer(designComponent);
			layer.addTiledLayerListener(this);
			designIdMap.put(layer, designComponent);
		}
		else if (typeId == SpriteCD.TYPEID) {
			Sprite sprite = this.constructSprite(designComponent);
			sprite.addSequenceContainerListener(this);
			designIdMap.put(sprite, designComponent);
		}
		else if (typeId == SequenceCD.TYPEID) {
			Sequence sequence = this.constructSequence(designComponent);
			sequence.addSequenceListener(this);
			designIdMap.put(sequence, designComponent);
		}
		else if (typeId == ImageResourceCD.TYPEID) {
			ImageResource imageResource = this.constructImageResource(designComponent);
			imageResource.addImageResourceListener(this);
			designIdMap.put(imageResource, designComponent);
		}
		else if (typeId == AnimatedTileCD.TYPEID) {
			AnimatedTile animatedTile = this.constructAnimatedTile(designComponent);
			animatedTile.addSequenceContainerListener(this);
			designIdMap.put(animatedTile, designComponent);
		}
	}
	
	//These methods create game model from design components
	
	private AnimatedTile constructAnimatedTile(DesignComponent animatedTiledDC) {
		String name = (String) animatedTiledDC.readProperty(AnimatedTileCD.PROPERTY_NAME).getPrimitiveValue();
		DesignComponent imgResDC = animatedTiledDC.readProperty(AnimatedTileCD.PROP_IMAGE_RESOURCE).getComponent();
		ImageResource imgRes = this.constructImageResource(imgResDC);
		//if ImageResource already has an animated tile of that name it must have been already constructed
		AnimatedTile animatedTile = imgRes.getAnimatedTileByName(name);
		if (animatedTile != null) {
			return animatedTile;
		}
		
		DesignComponent defaultSequenceDC = animatedTiledDC.readProperty(SequenceContainerCDProperties.PROP_DEFAULT_SEQUENCE).getComponent();
		List<PropertyValue> sequenceDCs = animatedTiledDC.readProperty(SequenceContainerCDProperties.PROP_SEQUENCES).getArray();

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

		DesignComponent defaultSequenceDC = spriteDC.readProperty(SequenceContainerCDProperties.PROP_DEFAULT_SEQUENCE).getComponent();
		List<PropertyValue> sequenceDCs = spriteDC.readProperty(SequenceContainerCDProperties.PROP_SEQUENCES).getArray();

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
		
		tiledLayer = GlobalRepository.getInstance().createTiledLayer(name, imgRes, grid);
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
		
		ImageResource imgRes = GlobalRepository.getInstance().getImageResource(imgResUrl, tileWidth, tileHeight);
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
	
	
	//These methods create design components from game model
	
	public static DesignComponent createSceneDCFromScene(DesignDocument doc, Scene scene) {
		DesignComponent dcScene = doc.createComponent(SceneCD.TYPEID);
		dcScene.writeProperty(SceneCD.PROPERTY_NAME, MidpTypes.createStringValue(scene.getName()));		
		writeSceneItemsToSceneDC(doc, dcScene, scene);
		return dcScene;
	}
	
	private static void writeSceneItemsToSceneDC(DesignDocument doc, DesignComponent dcScene, Scene scene) {
		//fist remove all scene items
		List<PropertyValue> sceneItemProps = dcScene.readProperty(SceneCD.PROPERTY_SCENE_ITEMS).getArray();
		for (Iterator<PropertyValue> it = sceneItemProps.iterator(); it.hasNext();) {
			PropertyValue propertyValue = it.next();
			DesignComponent dcSceneItemComp = propertyValue.getComponent();
			dcSceneItemComp.removeFromParentComponent();
		}
		dcScene.writeProperty(SceneCD.PROPERTY_SCENE_ITEMS, PropertyValue.createEmptyArray(SceneItemCD.TYPEID));

		//them add scene items according to the current state of the scene
		List<Layer> layers = scene.getLayers();
		List<PropertyValue> sceneItems = new ArrayList<PropertyValue>();
		assert(sceneItems.isEmpty());
		
		for (Iterator<Layer> it = layers.iterator(); it.hasNext();) {
			DesignComponent sceneItemDC = doc.createComponent(SceneItemCD.TYPEID);

			Layer layer = it.next();
			DesignComponent layerDC = designIdMap.get(layer);
			assert (layerDC != null);

			sceneItemDC.writeProperty(SceneItemCD.PROPERTY_LAYER, PropertyValue.createComponentReference(layerDC));
			sceneItemDC.writeProperty(SceneItemCD.PROPERTY_LOCK, MidpTypes.createBooleanValue(scene.isLayerLocked(layer)));
			sceneItemDC.writeProperty(SceneItemCD.PROPERTY_VISIBLE, MidpTypes.createBooleanValue(scene.isLayerVisible(layer)));
			sceneItemDC.writeProperty(SceneItemCD.PROPERTY_POSITION, GameTypes.createPointProperty(scene.getLayerPosition(layer)));
			
			dcScene.addComponent(sceneItemDC);
			
			PropertyValue sceneItemPropVal = PropertyValue.createComponentReference(sceneItemDC);
			sceneItems.add(sceneItemPropVal);
		}
		dcScene.writeProperty(SceneCD.PROPERTY_SCENE_ITEMS, PropertyValue.createArray(SceneItemCD.TYPEID, sceneItems));
	}
	
	public static DesignComponent createTiledLayerDCFromTiledLayer(DesignDocument doc, TiledLayer layer) {
		DesignComponent dcLayer = doc.createComponent(TiledLayerCD.TYPEID);
		writeTiledLayerPropsToDC(dcLayer, layer);
		return dcLayer;
	}
	
	private static void writeTiledLayerPropsToDC(DesignComponent dcLayer, TiledLayer layer) {
		dcLayer.writeProperty(LayerCD.PROPERTY_NAME, MidpTypes.createStringValue(layer.getName()));
		
		DesignComponent dcImgRes = designIdMap.get(layer.getImageResource());
		assert(dcImgRes != null);
		
		dcLayer.writeProperty(LayerCD.PROPERTY_IMAGE_RESOURCE, PropertyValue.createComponentReference(dcImgRes));
		PropertyValue propTiles = GameTypes.createTilesProperty(layer.getTiles());
		dcLayer.writeProperty(TiledLayerCD.PROPERTY_TILES, propTiles);
	}
	
	public DesignComponent createSpriteDCFromSprite(Sprite layer) {
		DesignComponent dcLayer = this.document.createComponent(SpriteCD.TYPEID);
		dcLayer.writeProperty(LayerCD.PROPERTY_NAME, MidpTypes.createStringValue(layer.getName()));
		
		DesignComponent dcImgRes = designIdMap.get(layer.getImageResource());
		assert(dcImgRes != null);
		dcLayer.writeProperty(LayerCD.PROPERTY_IMAGE_RESOURCE, PropertyValue.createComponentReference(dcImgRes));
		
		this.updateSequenceContainerProps(layer);
		
		return  dcLayer;
	}
	
	public static DesignComponent createImageResourceDCFromImageResource(DesignDocument doc, ImageResource imageResource) {
		DesignComponent dcImgRes = doc.createComponent(ImageResourceCD.TYPEID);
		dcImgRes.writeProperty(ImageResourceCD.PROPERTY_IMAGE_URL, MidpTypes.createStringValue(imageResource.getURL().toExternalForm()));
		dcImgRes.writeProperty(ImageResourceCD.PROPERTY_TILE_WIDTH, MidpTypes.createIntegerValue(imageResource.getCellWidth()));
		dcImgRes.writeProperty(ImageResourceCD.PROPERTY_TILE_HEIGHT, MidpTypes.createIntegerValue(imageResource.getCellHeight()));
		
		return dcImgRes;
	}
	
	//--------------- GlobalRepositoryListener ------------------
	
    public void sceneAdded(final Scene scene, int index) {
		this.document.getTransactionManager().writeAccess(new Runnable() {
            public void run() {
				DesignDocument doc = GameController.this.document;
				DesignComponent sceneDC = createSceneDCFromScene(doc, scene);
				designIdMap.put(scene, sceneDC);
				scene.addSceneListener(GameController.this);
				doc.getRootComponent().addComponent(sceneDC);
            }
		});
    }

    public void sceneRemoved(Scene scene, int index) {
		scene.removeSceneListener(this);
		final DesignComponent dcScene = designIdMap.remove(scene);
		assert (dcScene != null);
		this.document.getTransactionManager().writeAccess(new Runnable() {
            public void run() {
				document.deleteComponent(dcScene);
            }			
		});
    }

    public void tiledLayerAdded(final TiledLayer tiledLayer, int index) {
		this.document.getTransactionManager().writeAccess(new Runnable() {
            public void run() {
				DesignDocument doc = GameController.this.document;
				DesignComponent tiledLayerDC = createTiledLayerDCFromTiledLayer(doc, tiledLayer);
				designIdMap.put(tiledLayer, tiledLayerDC);
				tiledLayer.addTiledLayerListener(GameController.this);
				doc.getRootComponent().addComponent(tiledLayerDC);
            }
		});		
    }

    public void tiledLayerRemoved(TiledLayer tiledLayer, int index) {
		tiledLayer.removeTiledLayerListener(this);
		final DesignComponent dcLayer = designIdMap.remove(tiledLayer);
		assert (dcLayer != null);
		this.document.getTransactionManager().writeAccess(new Runnable() {
            public void run() {
				document.deleteComponent(dcLayer);
            }			
		});
    }

    public void spriteAdded(final Sprite sprite, int index) {
		this.document.getTransactionManager().writeAccess(new Runnable() {
            public void run() {
				DesignComponent spriteDC = GameController.this.createSpriteDCFromSprite(sprite);
				designIdMap.put(sprite, spriteDC);
				GameController.this.document.getRootComponent().addComponent(spriteDC);
            }
		});
    }

    public void spriteRemoved(Sprite sprite, int index) {
		sprite.removeSequenceContainerListener(this);
		final DesignComponent dcLayer = designIdMap.remove(sprite);
		assert (dcLayer != null);
		this.document.getTransactionManager().writeAccess(new Runnable() {
            public void run() {
				document.deleteComponent(dcLayer);
            }			
		});
    }

    public void imageResourceAdded(final ImageResource imageResource) {
		this.document.getTransactionManager().writeAccess(new Runnable() {
            public void run() {
				DesignDocument doc = GameController.this.document;
				DesignComponent imgResDC = createImageResourceDCFromImageResource(doc, imageResource);
				designIdMap.put(imageResource, imgResDC);
				doc.getRootComponent().addComponent(imgResDC);
            }
		});
    }

	//----------------- SceneListener -------------------
	
    public void layerAdded(final Scene sourceScene, final Layer layer, int index) {
		this.document.getTransactionManager().writeAccess(new Runnable() {
            public void run() {
                DesignComponent dcScene = designIdMap.get(sourceScene);
				assert(dcScene != null);
				DesignComponent dcLayer = designIdMap.get(layer);
				assert(dcLayer != null);
				writeSceneItemsToSceneDC(document, dcScene, sourceScene);
            }
		});
    }

    public void layerRemoved(final Scene sourceScene, final Layer layer, final LayerInfo info, int index) {
		this.document.getTransactionManager().writeAccess(new Runnable() {
            public void run() {
                DesignComponent dcScene = designIdMap.get(sourceScene);
				assert(dcScene != null);
				DesignComponent dcLayer = designIdMap.get(layer);
				assert(dcLayer != null);
				writeSceneItemsToSceneDC(document, dcScene, sourceScene);
            }
		});
    }

    public void layerMoved(final Scene sourceScene, final Layer layer, int indexOld, int indexNew) {
		this.document.getTransactionManager().writeAccess(new Runnable() {
            public void run() {
                DesignComponent dcScene = designIdMap.get(sourceScene);
				assert(dcScene != null);
				DesignComponent dcLayer = designIdMap.get(layer);
				assert(dcLayer != null);
				writeSceneItemsToSceneDC(document, dcScene, sourceScene);
            }
		});
    }

    public void layerPositionChanged(final Scene sourceScene, final Layer layer, final Point oldPosition, final Point newPosition) {
		this.document.getTransactionManager().writeAccess(new Runnable() {
            public void run() {
                DesignComponent dcScene = designIdMap.get(sourceScene);
				assert(dcScene != null);
				DesignComponent dcLayer = designIdMap.get(layer);
				assert(dcLayer != null);
				writeSceneItemsToSceneDC(document, dcScene, sourceScene);
            }
		});
    }

    public void layerLockChanged(final Scene sourceScene, final Layer layer, boolean locked) {
		this.document.getTransactionManager().writeAccess(new Runnable() {
            public void run() {
                DesignComponent dcScene = designIdMap.get(sourceScene);
				assert(dcScene != null);
				DesignComponent dcLayer = designIdMap.get(layer);
				assert(dcLayer != null);
				writeSceneItemsToSceneDC(document, dcScene, sourceScene);
            }
		});
    }

    public void layerVisibilityChanged(final Scene sourceScene, final Layer layer, boolean visible) {
		this.document.getTransactionManager().writeAccess(new Runnable() {
            public void run() {
                DesignComponent dcScene = designIdMap.get(sourceScene);
				assert(dcScene != null);
				DesignComponent dcLayer = designIdMap.get(layer);
				assert(dcLayer != null);
				writeSceneItemsToSceneDC(document, dcScene, sourceScene);
            }
		});
    }

	
	//----------------- TiledLayerListener ---------------------
	
    public void tileChanged(TiledLayer source, int row, int col) {
		this.updateTiledLayerDCProps(source);
    }

    public void tilesChanged(TiledLayer source, Set positions) {
		this.updateTiledLayerDCProps(source);
    }

    public void columnsInserted(TiledLayer source, int index, int count) {
		this.updateTiledLayerDCProps(source);
    }

    public void columnsRemoved(TiledLayer source, int index, int count) {
		this.updateTiledLayerDCProps(source);
    }

    public void rowsInserted(TiledLayer source, int index, int count) {
		this.updateTiledLayerDCProps(source);
    }

    public void rowsRemoved(TiledLayer source, int index, int count) {
		this.updateTiledLayerDCProps(source);
    }

	private void updateTiledLayerDCProps(final TiledLayer layer) {
		this.document.getTransactionManager().writeAccess(new Runnable() {
            public void run() {
				DesignComponent dcLayer = designIdMap.get(layer);
				assert(dcLayer != null);

				writeTiledLayerPropsToDC(dcLayer, layer);				
			}
		});
	}
	
	//------------------- SequenceContainerListener ------------------
	
    public void sequenceAdded(SequenceContainer source, Sequence sequence, int index) {
		this.updateSequenceContainerProps(source);
    }

    public void sequenceRemoved(SequenceContainer source, Sequence sequence, int index) {
		DesignComponent dcSequenceContainer = designIdMap.get(source);
		assert(dcSequenceContainer != null);
		this.updateSequenceContainerProps(source);
    }

    public void sequenceMoved(SequenceContainer source, Sequence sequence, int indexOld, int indexNew) {
		DesignComponent dcSequenceContainer = designIdMap.get(source);
		assert(dcSequenceContainer != null);
		this.updateSequenceContainerProps(source);
    }

	private void updateSequenceContainerProps(final SequenceContainer sequenceContainer) {
		this.document.getTransactionManager().writeAccess(new Runnable() {
            public void run() {
				DesignComponent dcSequenceContainer = designIdMap.get(sequenceContainer);
				assert(dcSequenceContainer != null);
				
				Sequence defaultSequence = sequenceContainer.getDefaultSequence();
				DesignComponent dcDefSequence = designIdMap.get(defaultSequence);
				assert(dcDefSequence != null);
				dcSequenceContainer.writeProperty(SequenceContainerCDProperties.PROP_DEFAULT_SEQUENCE, PropertyValue.createComponentReference(dcDefSequence));

				List<PropertyValue> sequenceDCs = new ArrayList<PropertyValue>();
				List<Sequence> sequences = sequenceContainer.getSequences();
				for (Sequence sequence : sequences) {
					DesignComponent dcSequence = designIdMap.get(sequence);
					assert(dcDefSequence != null);
					PropertyValue seqPropVal = PropertyValue.createComponentReference(dcSequence);
					sequenceDCs.add(seqPropVal);
				}
				dcSequenceContainer.writeProperty(SequenceContainerCDProperties.PROP_SEQUENCES, PropertyValue.createArray(SequenceCD.TYPEID, sequenceDCs));
			}
		});
	}
	
	//---------------------- ImageResourceListener -------------------------
	
    public void animatedTileAdded(ImageResource source, AnimatedTile tile) {
    }

    public void animatedTileRemoved(ImageResource source, AnimatedTile tile) {
		tile.removeSequenceContainerListener(this);
		final DesignComponent dcTile = designIdMap.remove(tile);
		assert(dcTile != null);
		this.document.getTransactionManager().writeAccess(new Runnable() {
            public void run() {
				document.deleteComponent(dcTile);
            }			
		});
    }

    public void sequenceAdded(ImageResource source, Sequence sequence) {
    }

    public void sequenceRemoved(ImageResource source, Sequence sequence) {
		sequence.removeSequenceListener(this);
		final DesignComponent dcSequence = designIdMap.remove(sequence);
		assert(dcSequence != null);
		this.document.getTransactionManager().writeAccess(new Runnable() {
            public void run() {
				document.deleteComponent(dcSequence);
            }			
		});
    }

	//----------------------- SequenceListener ----------------------------
	
    public void frameAdded(Sequence sequence, int index) {
		this.updateSequenceProps(sequence);
    }

    public void frameRemoved(Sequence sequence, int index) {
		this.updateSequenceProps(sequence);
    }

    public void frameModified(Sequence sequence, int index) {
		this.updateSequenceProps(sequence);
    }
	
	private void updateSequenceProps(final Sequence seq) {
		this.document.getTransactionManager().writeAccess(new Runnable() {
            public void run() {
				DesignComponent dcSeq = designIdMap.get(seq);
				assert(dcSeq != null);
            }
		});
	}
	
	
	//----------------------- PropertyChangeListener --------------------------
	
    public void propertyChange(PropertyChangeEvent e) {
		System.out.println("PropertyChangeEvent source: " + e.getSource() + ", prop: " + e.getPropertyName() + ", new: " + e.getNewValue() + ", old: " + e.getOldValue());
    }

}