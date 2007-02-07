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

import java.awt.*;
import java.util.List;

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
                writer.write ("public int[] sequence_" + name + "_" + imageResourceNameNoExt + " = {\n"); // NOI18N

                int[] frames = GameTypes.getFrames (component.readProperty (SequenceCD.PROPERTY_FRAMES));
                for (int i = 0; i < frames.length; i ++) {
                    if (i > 0)
                        writer.write (", "); // NOI18N
                    writer.write (Integer.toString (frames[i])); // TODO - previously there was getIndex called on a frame
                }

                writer.write ("};\n");
            }
        };
    }

    private static String getImageName (DesignComponent imageResource) {
        return "NOT_RESOLVED_YET"; // TODO - resolve the name-no-ext from imageResource
    }

    public static Presenter createSceneCodePresenter () {
        return new CodeClassLevelPresenter.Adapter() {

            protected void generateMethodSectionCode (MultiGuardedSection section) {
                CodeWriter writer = section.getWriter ();
                DesignComponent component = getComponent ();
                String sceneName = MidpTypes.getString (component.readProperty (SceneCD.PROPERTY_NAME));

                writer.write ("public void updateLayerManagerFor" + sceneName + " (LayerManager lm) throws IOException {\n"); // NOI18N

                List<PropertyValue> sceneItems = component.readProperty (SceneCD.PROPERTY_SCENE_ITEMS).getArray ();
                // TODO - z-order of SceneItem is not used
                for (PropertyValue propertyValue : sceneItems) {
                    DesignComponent sceneItem = propertyValue.getComponent ();

                    DesignComponent layer = sceneItem.readProperty (SceneItemCD.PROPERTY_LAYER).getComponent ();
                    String layerName = MidpTypes.getString (layer.readProperty (LayerCD.PROPERTY_NAME));
                    Point position = GameTypes.getPoint (sceneItem.readProperty (SceneItemCD.PROPERTY_POSITION));
                    boolean visible = MidpTypes.getBoolean (sceneItem.readProperty (SceneItemCD.PROPERTY_VISIBLE));

                    writer.write ("this.getLayer_" + layerName + "().setPosition(" + position.x + ", " + position.y + ");\n"); // NOI18N
                    writer.write ("this.getLayer_" + layerName + "().setVisible(" + visible + ");\n"); // NOI18N
                    writer.write ("lm.append(this.getLayer_" + layerName + "());\n");// NOI18N
                }

                writer.write ("}\n"); // NOI18N
                writer.write ("\n"); // NOI18N
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
//                DesignComponent imageResource = component.readProperty (SpriteCD.)


                writer.write ("public Sprite getLayer_" + layerName + "() throws IOException {\n"); // NOI18N
                writer.write ("if (this.sprite_" + layerName + " == null) {\n"); // NOI18N
                // TODO - constructor and setFrameSequence call
//                writer.write ("this.sprite_" + layerName + " = new Sprite(this.getImage_" + this.getImageResource ().getNameNoExt () + "(), " + this.getTileWidth () + ", " + this.getTileHeight () + ");"); // NOI18N
//                writer.write ("		this.sprite_" + this.getName () + ".setFrameSequence(this.sequence_" + this.getDefaultSequence ().getName () + "_" + this.getImageResource ().getNameNoExt () + ");"); // NOI18N
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
                // TODO - animTile fields?
            }

            protected void generateMethodSectionCode (MultiGuardedSection section) {
                CodeWriter writer = section.getWriter ();
                DesignComponent component = getComponent ();
                String layerName = MidpTypes.getString (component.readProperty (LayerCD.PROPERTY_NAME));
                int[][] tiles = GameTypes.getTiles (component.readProperty (TiledLayerCD.PROPERTY_TILES));

                writer.write ("public TiledLayer getLayer_" + layerName + "() throws IOException {\n"); // NOI18N
                writer.write ("if (this.tiledLayer_" + layerName + " == null) {\n"); // NOI18N
                // TODO - constructor call
                // TODO - animTile fields assignment?
//                writer.write ("this.tiledLayer_" + layerName + " = new Sprite(this.getImage_" + this.getImageResource ().getNameNoExt () + "(), " + this.getTileWidth () + ", " + this.getTileHeight () + ");"); // NOI18N

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

                writer.write ("for (int row = 0; row < tiles.length; row++) {");
                writer.write ("for (int col = 0; col < tiles[row].length; col++) {");
                writer.write ("tl.setCell(col, row, tiles[row][col]);");
                writer.write ("}");
                writer.write ("}");
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
