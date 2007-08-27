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
 *
 */

package org.netbeans.modules.vmd.game.integration;

import org.netbeans.modules.vmd.api.codegen.CodeClassLevelPresenter;
import org.netbeans.modules.vmd.api.codegen.CodeWriter;
import org.netbeans.modules.vmd.api.codegen.MultiGuardedSection;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.Presenter;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.game.integration.components.GameTypes;
import org.netbeans.modules.vmd.game.model.*;
import org.netbeans.modules.vmd.midp.components.MidpTypes;

import javax.swing.text.StyledDocument;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * @author David Kaspar, Karel Herink
 */
public class GameCodeSupport {
	
	
	public static Presenter createSequenceCodePresenter() {
		return new CodeClassLevelPresenter.Adapter() {
			protected void generateFieldSectionCode(MultiGuardedSection section) {
				CodeWriter writer = section.getWriter();
				DesignComponent component = getComponent();
				
				String name = MidpTypes.getString(component.readProperty(SequenceCD.PROPERTY_NAME));
				int frameMillis = MidpTypes.getInteger(component.readProperty(SequenceCD.PROPERTY_FRAME_MS));
				
				writer.write("public int " + name + "Delay = " + frameMillis + ";\n"); // NOI18N
				writer.write("public int[] " + name + " = {"); // NOI18N
				
				int[] frames = GameTypes.getFrames(component.readProperty(SequenceCD.PROPERTY_FRAMES));
				for (int i = 0; i < frames.length; i ++) {
					if (i > 0) {
						writer.write(", "); // NOI18N
					}
					writer.write(Integer.toString(frames[i]));
				}
				
				writer.write("};\n"); // NOI18N
			}
		};
	}
	
	public static Presenter createImageResourceCodePresenter() {
		return new CodeClassLevelPresenter.Adapter() {
			protected void generateFieldSectionCode(MultiGuardedSection section) {
				CodeWriter writer = section.getWriter();
				DesignComponent component = getComponent();
				String name = MidpTypes.getString(component.readProperty(ImageResourceCD.PROPERTY_NAME));
				writer.write("private Image " + name + ";\n"); // NOI18N
			}
			protected void generateClassBodyCode(StyledDocument document) {
				DesignComponent component = getComponent();
				MultiGuardedSection section = MultiGuardedSection.create(document, component.getComponentID() + "-getter"); // NOI18N
				String name = MidpTypes.getString(component.readProperty(ImageResourceCD.PROPERTY_NAME));
				String path = MidpTypes.getString(component.readProperty(ImageResourceCD.PROPERTY_IMAGE_PATH));
				section.getWriter().write("public Image " + CodeUtils.createGetterMethodName(name) + "() throws java.io.IOException {\n"); // NOI18N
				section.getWriter().write("if (" + name + " == null) {\n"); // NOI18N
				
				section.getWriter().commit();
				section.switchToEditable(getComponent().getComponentID() + "-preInit"); // NOI18N
				section.getWriter().write(" // write pre-init user code here\n").commit(); // NOI18N
				section.switchToGuarded();
				
				section.getWriter().write(name + " = Image.createImage(\"" + path + "\");\n"); // NOI18N
				
				//XXX insert editable code here
				
				section.getWriter().write("}\n"); // NOI18N
				
				section.getWriter().commit();
				section.switchToEditable(getComponent().getComponentID() + "-postInit"); // NOI18N
				section.getWriter().write(" // write post-init user code here\n").commit(); // NOI18N
				section.switchToGuarded();
				
				section.getWriter().write("return this." + name + ";\n"); // NOI18N
				section.getWriter().write("}\n"); // NOI18N
				section.getWriter().write("\n"); // NOI18N
				
				section.getWriter().commit();
				section.close();
			}
		};
	}
		
	public static Presenter createSceneCodePresenter() {
		return new CodeClassLevelPresenter.Adapter() {
			protected void generateClassBodyCode(StyledDocument document) {
				DesignComponent component = getComponent();
				MultiGuardedSection section = MultiGuardedSection.create(document, component.getComponentID() + "-updateLayerManager");// NOI18N
				String sceneName = MidpTypes.getString(component.readProperty(SceneCD.PROPERTY_NAME));
				
				section.getWriter().write("public void updateLayerManagerFor" + CodeUtils.capitalize(sceneName) + "(LayerManager lm) throws java.io.IOException {\n"); // NOI18N
				
				section.getWriter().commit();
				section.switchToEditable(getComponent().getComponentID() + "-preUpdate"); // NOI18N
				section.getWriter().write(" // write pre-update user code here\n").commit(); // NOI18N
				section.switchToGuarded();
				
				List<PropertyValue> tmp = component.readProperty(SceneCD.PROPERTY_SCENE_ITEMS).getArray();
				List<PropertyValue> sceneItems = new ArrayList<PropertyValue>();
				sceneItems.addAll(tmp);
				
				Collections.sort(sceneItems, new Comparator<PropertyValue> () {
					public int compare(PropertyValue a, PropertyValue b) {
						DesignComponent aItem = a.getComponent();
						int aZ = MidpTypes.getInteger(aItem.readProperty(SceneItemCD.PROPERTY_Z_ORDER));
						
						DesignComponent bItem = b.getComponent();
						int bZ = MidpTypes.getInteger(bItem.readProperty(SceneItemCD.PROPERTY_Z_ORDER));
						
						return new Integer(aZ).compareTo(bZ);
					}
				});
				for (PropertyValue propertyValue : sceneItems) {
					DesignComponent sceneItem = propertyValue.getComponent();
					
					DesignComponent layer = sceneItem.readProperty(SceneItemCD.PROPERTY_LAYER).getComponent();
					String layerName = MidpTypes.getString(layer.readProperty(LayerCD.PROPERTY_NAME));
					Point position = GameTypes.getPoint(sceneItem.readProperty(SceneItemCD.PROPERTY_POSITION));
					boolean visible = MidpTypes.getBoolean(sceneItem.readProperty(SceneItemCD.PROPERTY_VISIBLE));
					
					section.getWriter().write(CodeUtils.createGetterMethodName(layerName) + "().setPosition(" + position.x + ", " + position.y + ");\n"); // NOI18N
					section.getWriter().write(CodeUtils.createGetterMethodName(layerName) + "().setVisible(" + visible + ");\n"); // NOI18N
					section.getWriter().write("lm.append(" + CodeUtils.createGetterMethodName(layerName) + "());\n"); // NOI18N
				}
				
				section.getWriter().commit();
				section.switchToEditable(getComponent().getComponentID() + "-postUpdate"); // NOI18N
				section.getWriter().write(" // write post-update user code here\n").commit(); // NOI18N
				section.switchToGuarded();
				
				section.getWriter().write("}\n"); // NOI18N
				section.getWriter().write("\n"); // NOI18N
				
				section.getWriter().commit();
				section.close();
			}
		};
	}
	
	public static Presenter createSpriteCodePresenter() {
		return new CodeClassLevelPresenter.Adapter() {
			
			protected void generateFieldSectionCode(MultiGuardedSection section) {
				DesignComponent component = getComponent();
				String layerName = MidpTypes.getString(component.readProperty(LayerCD.PROPERTY_NAME));
				section.getWriter().write("private Sprite " + layerName + ";\n"); // NOI18N
			}
			
			protected void generateClassBodyCode(StyledDocument document) {
				DesignComponent component = getComponent();
				MultiGuardedSection section = MultiGuardedSection.create(document, component.getComponentID() + "-getter");// NOI18N
				String layerName = MidpTypes.getString(component.readProperty(LayerCD.PROPERTY_NAME));
				
				DesignComponent imageResource = component.readProperty(LayerCD.PROPERTY_IMAGE_RESOURCE).getComponent();
				String imageName = MidpTypes.getString(imageResource.readProperty(ImageResourceCD.PROPERTY_NAME));
				
				int tileWidth = MidpTypes.getInteger(component.readProperty(LayerCD.PROPERTY_TILE_WIDTH));
				int tileHeight = MidpTypes.getInteger(component.readProperty(LayerCD.PROPERTY_TILE_HEIGHT));
				
				DesignComponent defSeq = component.readProperty(SequenceContainerCDProperties.PROP_DEFAULT_SEQUENCE).getComponent();
				String defSeqName = MidpTypes.getString(defSeq.readProperty(SequenceCD.PROPERTY_NAME));
				
				section.getWriter().write("public Sprite " + CodeUtils.createGetterMethodName(layerName) + "() throws java.io.IOException {\n"); // NOI18N
				section.getWriter().write("if (" + layerName + " == null) {\n"); // NOI18N
				
				section.getWriter().commit();
				section.switchToEditable(getComponent().getComponentID() + "-preInit"); // NOI18N
				section.getWriter().write(" // write pre-init user code here\n").commit(); // NOI18N
				section.switchToGuarded();
				
				section.getWriter().write(layerName + " = new Sprite(" + CodeUtils.createGetterMethodName(imageName) + "(), " + tileWidth + ", " + tileHeight + ");\n"); // NOI18N
				section.getWriter().write(layerName + ".setFrameSequence(" + defSeqName + ");\n"); // NOI18N
				
				section.getWriter().commit();
				section.switchToEditable(getComponent().getComponentID() + "-postInit"); // NOI18N
				section.getWriter().write(" // write post-init user code here\n").commit(); // NOI18N
				section.switchToGuarded();
				
				section.getWriter().write("}\n"); // NOI18N
				
				//XXX insert editable code here
				
				section.getWriter().write("	return " + layerName + ";\n"); // NOI18N
				section.getWriter().write("}\n"); // NOI18N
				section.getWriter().write("\n"); // NOI18N
				
				section.getWriter().commit();
				section.close();
			}
		};
	}
	
	private static Set<Integer> getAnimatedTileIndexesFromTiledLayer(DesignComponent tiledLayer) {
		assert (tiledLayer.getType().equals(TiledLayerCD.TYPEID));
		Set<Integer> animSet = new HashSet<Integer>();
		int[][] tiles = GameTypes.getTiles(tiledLayer.readProperty(TiledLayerCD.PROPERTY_TILES));
		for (int i = 0; i < tiles.length; i++) {
			for (int j = 0; j < tiles[i].length; j ++) {
				int index = tiles[i][j];
				if (index < 0) {
					animSet.add(index);
				}
			}
		}
		return animSet;
	}
	
	
	private static Set<DesignComponent> getAnimatedTilesFromTiledLayer(DesignComponent tiledLayer) {
		Set<DesignComponent> finalSet = new HashSet<DesignComponent>();
		Set<Integer> animSet = getAnimatedTileIndexesFromTiledLayer(tiledLayer);
		DesignComponent dcImgRes = tiledLayer.readProperty(LayerCD.PROPERTY_IMAGE_RESOURCE).getComponent();
		Collection <DesignComponent> children = dcImgRes.getComponents();
		for (DesignComponent child : children) {
			if (child.getType().equals(AnimatedTileCD.TYPEID)) {
				if (animSet.contains(MidpTypes.getInteger(child.readProperty(AnimatedTileCD.PROPERTY_INDEX)))) {
					finalSet.add(child);
				}
			}
		}
		return finalSet;
	}
	
	public static Presenter createTiledLayerCodePresenter() {
		return new CodeClassLevelPresenter.Adapter() {
			
			protected void generateFieldSectionCode(MultiGuardedSection section) {
				DesignComponent component = getComponent();
				
				//define the layer
				String layerName = MidpTypes.getString(component.readProperty(LayerCD.PROPERTY_NAME));
				section.getWriter().write("private TiledLayer " + layerName + ";\n"); // NOI18N
				
				Set<DesignComponent> ats = getAnimatedTilesFromTiledLayer(component);
				for (DesignComponent animtile : ats) {
					String animTileName = MidpTypes.getString(animtile.readProperty(AnimatedTileCD.PROPERTY_NAME));
					section.getWriter().write("public int " +animTileName + CodeUtils.capitalize(layerName) + ";\n"); // NOI18N
				}
			}
			
			protected void generateClassBodyCode(StyledDocument document) {
				DesignComponent component = getComponent();
				MultiGuardedSection section = MultiGuardedSection.create(document, component.getComponentID() + "-getter");// NOI18N
				
				String layerName = MidpTypes.getString(component.readProperty(LayerCD.PROPERTY_NAME));
				int[][] tiles = GameTypes.getTiles(component.readProperty(TiledLayerCD.PROPERTY_TILES));
				
				DesignComponent imageResource = component.readProperty(LayerCD.PROPERTY_IMAGE_RESOURCE).getComponent();
				String imageName = MidpTypes.getString(imageResource.readProperty(ImageResourceCD.PROPERTY_NAME));
				int tileWidth = MidpTypes.getInteger(component.readProperty(LayerCD.PROPERTY_TILE_WIDTH));
				int tileHeight = MidpTypes.getInteger(component.readProperty(LayerCD.PROPERTY_TILE_HEIGHT));
				
				section.getWriter().write("public TiledLayer " + CodeUtils.createGetterMethodName(layerName) + "() throws java.io.IOException {\n"); // NOI18N
				section.getWriter().write("if (" + layerName + " == null) {\n"); // NOI18N
				
				section.getWriter().commit();
				section.switchToEditable(getComponent().getComponentID() + "-preInit"); // NOI18N
				section.getWriter().write(" // write pre-init user code here\n").commit(); // NOI18N
				section.switchToGuarded();
				
				section.getWriter().write(layerName + " = new TiledLayer(" // NOI18N
						+ tiles[0].length + ", " + tiles.length + ", " + CodeUtils.createGetterMethodName(imageName) // NOI18N
						+ "(), " + tileWidth + ", " + tileHeight + ");\n"); // NOI18N
				
				//init animated tiles
				Set<DesignComponent> ats = getAnimatedTilesFromTiledLayer(component);
				for (DesignComponent animtile : ats) {
					String animTileName = MidpTypes.getString(animtile.readProperty(AnimatedTileCD.PROPERTY_NAME));
					DesignComponent defSeq = animtile.readProperty(SequenceContainerCDProperties.PROP_DEFAULT_SEQUENCE).getComponent();
					
					String seqName = MidpTypes.getString(defSeq.readProperty(SequenceCD.PROPERTY_NAME));
					section.getWriter().write(animTileName + CodeUtils.capitalize(layerName) + " = " + layerName + ".createAnimatedTile(" + seqName + "[0]);\n"); // NOI18N
				}
				Map<Integer, String> indexNames = new HashMap<Integer, String>();
				for (DesignComponent at : ats) {
					indexNames.put(MidpTypes.getInteger(at.readProperty(AnimatedTileCD.PROPERTY_INDEX)), MidpTypes.getString(at.readProperty(AnimatedTileCD.PROPERTY_NAME)));
				}
				
				//write tile matrix
				section.getWriter().write("int[][] tiles = {\n"); // NOI18N
				for (int rowIndex = 0; rowIndex < tiles.length; rowIndex ++) {
					section.getWriter().write("{ "); // NOI18N
					int[] row = tiles[rowIndex];
					for (int columnIndex = 0; columnIndex < row.length; columnIndex ++) {
						if (columnIndex > 0)
							section.getWriter().write(", "); // NOI18N
						//check for animTile
						int tileIndex = row[columnIndex];
						if (tileIndex >= 0) {
							section.getWriter().write(Integer.toString(row[columnIndex]));
						} else {
							String atName = indexNames.get(tileIndex);
							section.getWriter().write(atName + CodeUtils.capitalize(layerName)); // NOI18N
						}
					}
					if (rowIndex < tiles.length - 1)
						section.getWriter().write(" },\n"); // NOI18N
					else
						section.getWriter().write(" }\n"); // NOI18N
				}
				section.getWriter().write("};\n"); // NOI18N
				
				section.getWriter().commit();
				section.switchToEditable(getComponent().getComponentID() + "-midInit"); // NOI18N
				section.getWriter().write(" // write mid-init user code here\n").commit(); // NOI18N
				section.switchToGuarded();
				
				section.getWriter().write("for (int row = 0; row < " + tiles.length + "; row++) {\n"); // NOI18N
				section.getWriter().write("for (int col = 0; col < " + tiles[0].length + "; col++) {\n"); // NOI18N
				section.getWriter().write(layerName + ".setCell(col, row, tiles[row][col]);\n"); // NOI18N
				section.getWriter().write("}\n"); // NOI18N
				section.getWriter().write("}\n"); // NOI18N
				section.getWriter().write("}\n"); // NOI18N
				
				section.getWriter().commit();
				section.switchToEditable(getComponent().getComponentID() + "-postInit"); // NOI18N
				section.getWriter().write(" // write post-init user code here\n").commit(); // NOI18N
				section.switchToGuarded();
				
				section.getWriter().write("	return " + layerName + ";\n"); // NOI18N
				section.getWriter().write("}\n"); // NOI18N
				section.getWriter().write("\n"); // NOI18N
				
				section.getWriter().commit();
				section.close();
			}
		};
	}
	
}
