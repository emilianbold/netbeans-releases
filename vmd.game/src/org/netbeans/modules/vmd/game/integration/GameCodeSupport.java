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
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.netbeans.modules.vmd.game.model.AnimatedTileCD;
import org.netbeans.modules.vmd.game.model.ImageResourceCD;
import org.netbeans.modules.vmd.game.model.LayerCD;
import org.netbeans.modules.vmd.game.model.SceneCD;
import org.netbeans.modules.vmd.game.model.SceneItemCD;
import org.netbeans.modules.vmd.game.model.SequenceCD;
import org.netbeans.modules.vmd.game.model.SequenceContainerCDProperties;
import org.netbeans.modules.vmd.game.model.TiledLayerCD;

/**
 * @author David Kaspar
 */
public class GameCodeSupport {

    public static Presenter createSequenceCodePresenter () {
        return new CodeClassLevelPresenter.Adapter () {
            protected void generateFieldSectionCode (MultiGuardedSection section) {
                CodeWriter writer = section.getWriter ();
                DesignComponent component = getComponent ();

                String name = MidpTypes.getString (component.readProperty (SequenceCD.PROPERTY_NAME));
                int frameMillis = MidpTypes.getInteger (component.readProperty (SequenceCD.PROPERTY_FRAME_MS));
                DesignComponent imageResource = component.readProperty (SequenceCD.PROPERTY_IMAGE_RESOURCE).getComponent ();
                String imageResourceNameNoExt = getImageName (imageResource);

                writer.write ("public int sequence_" + name + "_" + imageResourceNameNoExt + "_delay = " + frameMillis + ";\n"); // NOI18N
                writer.write ("public int[] sequence_" + name + "_" + imageResourceNameNoExt + " = {"); // NOI18N

                int[] frames = GameTypes.getFrames (component.readProperty (SequenceCD.PROPERTY_FRAMES));
                for (int i = 0; i < frames.length; i ++) {
                    if (i > 0)
                        writer.write (", "); // NOI18N
                    writer.write (Integer.toString (frames[i]));
                }

                writer.write ("};\n"); // NOI18N
            }
        };
    }

    public static Presenter createImageResourceCodePresenter () {
        return new CodeClassLevelPresenter.Adapter() {
            protected void generateFieldSectionCode(MultiGuardedSection section) {
				CodeWriter writer = section.getWriter();
				DesignComponent component = getComponent();
				String name = getImageName(component);
				writer.write ("private Image image_" + name + ";\n"); // NOI18N
			}
            protected void generateMethodSectionCode(MultiGuardedSection section) {
                CodeWriter writer = section.getWriter();
                DesignComponent component = getComponent();
				String name = getImageName(component);
				String path = MidpTypes.getString(component.readProperty(ImageResourceCD.PROPERTY_IMAGE_PATH));
				writer.write("public Image getImage_" + name + "() throws java.io.IOException {\n"); // NOI18N
				writer.write("if (this.image_" + name + " == null) {\n"); // NOI18N
				writer.write("this.image_" + name + " = Image.createImage(\"" + path + "\");\n"); // NOI18N
				writer.write("}\n"); // NOI18N
				writer.write("return this.image_" + name + ";\n"); // NOI18N
				writer.write("}\n"); // NOI18N
				writer.write("\n"); // NOI18N
			}
		};
	}

    private static String getImageName (DesignComponent imageResource) {
		String path = MidpTypes.getString(imageResource.readProperty(ImageResourceCD.PROPERTY_IMAGE_PATH));
		int index = path.lastIndexOf("/");
		String name = path.substring(index + 1);
		name = name.substring(0, name.lastIndexOf("."));
		return name;
    }

    public static Presenter createSceneCodePresenter () {
        return new CodeClassLevelPresenter.Adapter() {

            protected void generateMethodSectionCode (MultiGuardedSection section) {
                CodeWriter writer = section.getWriter ();
                DesignComponent component = getComponent ();
                String sceneName = MidpTypes.getString (component.readProperty (SceneCD.PROPERTY_NAME));

                writer.write ("public void updateLayerManagerFor" + sceneName + "(LayerManager lm) throws java.io.IOException {\n"); // NOI18N

                List<PropertyValue> tmp = component.readProperty (SceneCD.PROPERTY_SCENE_ITEMS).getArray ();
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
                    DesignComponent sceneItem = propertyValue.getComponent ();

                    DesignComponent layer = sceneItem.readProperty (SceneItemCD.PROPERTY_LAYER).getComponent ();
                    String layerName = MidpTypes.getString (layer.readProperty (LayerCD.PROPERTY_NAME));
                    Point position = GameTypes.getPoint (sceneItem.readProperty (SceneItemCD.PROPERTY_POSITION));
                    boolean visible = MidpTypes.getBoolean (sceneItem.readProperty (SceneItemCD.PROPERTY_VISIBLE));

                    writer.write ("this.getLayer_" + layerName + "().setPosition(" + position.x + ", " + position.y + ");\n"); // NOI18N
                    writer.write ("this.getLayer_" + layerName + "().setVisible(" + visible + ");\n"); // NOI18N
                    writer.write ("lm.append(this.getLayer_" + layerName + "());\n"); // NOI18N
                }

                writer.write ("}\n"); // NOI18N
                writer.write ("\n"); // NOI18N
            }
        };
    }
	
	public static Presenter createAnimatedTileCodePresenter() {
        return new CodeClassLevelPresenter.Adapter() {

            protected void generateFieldSectionCode(MultiGuardedSection section) {
                DesignComponent component = getComponent();
                String name = MidpTypes.getString (component.readProperty (AnimatedTileCD.PROPERTY_NAME));
                section.getWriter().write("private int animatedTile_" + name + ";\n"); // NOI18N
			}
			
            protected void generateMethodSectionCode(MultiGuardedSection section) {
                CodeWriter writer = section.getWriter();
                DesignComponent component = getComponent();
			}
		};
	}
	
    public static Presenter createSpriteCodePresenter () {
        return new CodeClassLevelPresenter.Adapter () {

            protected void generateFieldSectionCode (MultiGuardedSection section) {
                DesignComponent component = getComponent ();
                String layerName = MidpTypes.getString (component.readProperty (LayerCD.PROPERTY_NAME));
                section.getWriter ().write ("private Sprite sprite_" + layerName + ";\n"); // NOI18N
            }

            protected void generateMethodSectionCode (MultiGuardedSection section) {
                CodeWriter writer = section.getWriter ();
                DesignComponent component = getComponent ();
                String layerName = MidpTypes.getString (component.readProperty (LayerCD.PROPERTY_NAME));
				
				DesignComponent imageResource = component.readProperty(LayerCD.PROPERTY_IMAGE_RESOURCE).getComponent();
				String imageName = getImageName(imageResource);
				int tileWidth = MidpTypes.getInteger(imageResource.readProperty(ImageResourceCD.PROPERTY_TILE_WIDTH));
				int tileHeight = MidpTypes.getInteger(imageResource.readProperty(ImageResourceCD.PROPERTY_TILE_HEIGHT));
				
				DesignComponent defSeq = component.readProperty(SequenceContainerCDProperties.PROP_DEFAULT_SEQUENCE).getComponent();
				String defSeqName = MidpTypes.getString (defSeq.readProperty (SequenceCD.PROPERTY_NAME));
				
                writer.write ("public Sprite getLayer_" + layerName + "() throws java.io.IOException {\n"); // NOI18N
                writer.write ("if (this.sprite_" + layerName + " == null) {\n"); // NOI18N
                
				writer.write ("this.sprite_" + layerName + " = new Sprite(this.getImage_" + imageName + "(), " + tileWidth + ", " + tileHeight + ");\n"); // NOI18N
                writer.write ("		this.sprite_" + layerName + ".setFrameSequence(this.sequence_" + defSeqName + "_" + imageName + ");\n"); // NOI18N
                
				writer.write ("}\n"); // NOI18N
                writer.write ("	return this.sprite_" + layerName + ";\n"); // NOI18N
                writer.write ("}\n"); // NOI18N
                writer.write ("\n"); // NOI18N
            }
        };
    }

    public static Presenter createTiledLayerCodePresenter () {
        return new CodeClassLevelPresenter.Adapter() {

            protected void generateFieldSectionCode (MultiGuardedSection section) {
                DesignComponent component = getComponent ();
                String layerName = MidpTypes.getString (component.readProperty (LayerCD.PROPERTY_NAME));
                section.getWriter ().write ("private TiledLayer tiledLayer_" + layerName + ";\n"); // NOI18N
            }

            protected void generateMethodSectionCode (MultiGuardedSection section) {
                CodeWriter writer = section.getWriter ();
                DesignComponent component = getComponent ();
                String layerName = MidpTypes.getString (component.readProperty (LayerCD.PROPERTY_NAME));
                int[][] tiles = GameTypes.getTiles (component.readProperty (TiledLayerCD.PROPERTY_TILES));

 				DesignComponent imageResource = component.readProperty(LayerCD.PROPERTY_IMAGE_RESOURCE).getComponent();
				String imageName = getImageName(imageResource);
				int tileWidth = MidpTypes.getInteger(imageResource.readProperty(ImageResourceCD.PROPERTY_TILE_WIDTH));
				int tileHeight = MidpTypes.getInteger(imageResource.readProperty(ImageResourceCD.PROPERTY_TILE_HEIGHT));

				writer.write ("public TiledLayer getLayer_" + layerName + "() throws java.io.IOException {\n"); // NOI18N
                writer.write ("if (this.tiledLayer_" + layerName + " == null) {\n"); // NOI18N
				writer.write ("this.tiledLayer_" + layerName + " = new TiledLayer(" // NOI18N
						+ tiles[0].length + ", " + tiles.length + ", this.getImage_" // NOI18N
						+ imageName + "(), " + tileWidth + ", " + tileHeight + ");\n"); // NOI18N
				
                writer.write ("int[][] tiles = {\n"); // NOI18N
                for (int rowIndex = 0; rowIndex < tiles.length; rowIndex ++) {
                    writer.write ("{ "); // NOI18N
                    int[] row = tiles[rowIndex];
                    for (int columnIndex = 0; columnIndex < row.length; columnIndex ++) {
                        if (columnIndex > 0)
                            writer.write (", "); // NOI18N
                        // TODO - check for animTile?
                        writer.write (Integer.toString (row[columnIndex]));
                    }
                    if (rowIndex < tiles.length - 1)
                        writer.write (" },\n"); // NOI18N
                    else
                        writer.write (" }\n"); // NOI18N
                }
				writer.write ("};\n");
				
                writer.write ("for (int row = 0; row < " + tiles.length + "; row++) {\n");
                writer.write ("for (int col = 0; col < " + tiles[0].length + "; col++) {\n");
                writer.write ("this.tiledLayer_" + layerName + ".setCell(col, row, tiles[row][col]);\n");
                writer.write ("}\n");
                writer.write ("}\n");
                writer.write ("}\n"); // NOI18N
                writer.write ("	return this.tiledLayer_" + layerName + ";\n"); // NOI18N
                writer.write ("}\n"); // NOI18N
                writer.write ("\n"); // NOI18N

                // TODO - animTiles method?
            }
        };
    }

    // TODO - createAnimatedTileCodePresenter

}
