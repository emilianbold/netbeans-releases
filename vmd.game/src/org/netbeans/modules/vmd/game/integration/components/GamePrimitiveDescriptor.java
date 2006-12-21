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
package org.netbeans.modules.vmd.game.integration.components;

import org.netbeans.modules.vmd.api.model.PrimitiveDescriptor;
import org.netbeans.modules.vmd.api.model.PrimitiveDescriptorFactory;
import org.netbeans.modules.vmd.midp.components.MidpPrimitiveDescriptor;
import org.netbeans.modules.vmd.game.GameController;

import java.util.StringTokenizer;

/**
 * @author David Kaspar
 */
// HINT - after making change, update GameCodeSupport too
public final class GamePrimitiveDescriptor implements PrimitiveDescriptorFactory {

    static final String TYPEID_STRING_TILES = "#TiledLayerTiles";

    static final TilesPrimitiveDescriptor PRIMITIVE_DESCRIPTOR_TILES = new TilesPrimitiveDescriptor ();

    private MidpPrimitiveDescriptor midp = new MidpPrimitiveDescriptor ();

    public String getProjectType () {
        return GameController.PROJECT_TYPE_GAME;
    }

    public PrimitiveDescriptor getDescriptorForTypeIDString (String string) {
        if (TYPEID_STRING_TILES.equals (string))
            return PRIMITIVE_DESCRIPTOR_TILES;
        //TODO
        return midp.getDescriptorForTypeIDString (string);
    }

    // HINT - describes serialization of the TiledLayer.tiles property
    private static class TilesPrimitiveDescriptor implements PrimitiveDescriptor {

        public String serialize (Object value) {
            int[][] array = (int[][]) value;
            StringBuffer serialized = new StringBuffer ();
            int rows = array.length;
            int cols = array.length > 0 ? array[0].length : 0;
            serialized.append (rows).append (',').append (cols);
            for (int[] row : array)
                for (int cell : row)
                    serialized.append (',').append (cell);
            return serialized.toString ();
        }

        public Object deserialize (String serialized) {
            StringTokenizer tokenizer = new StringTokenizer (serialized, ",");
            int rows = Integer.parseInt (tokenizer.nextToken ());
            int cols = Integer.parseInt (tokenizer.nextToken ());
            int[][] array = new int[rows][cols];
            for (int y = 0; y < rows; y ++)
                for (int x = 0; x < cols; x ++)
                    array[y][x] = Integer.parseInt (tokenizer.nextToken ());
            return array;
        }

        public boolean isValidInstance (Object object) {
            return object instanceof int[][];
        }

    }
}
