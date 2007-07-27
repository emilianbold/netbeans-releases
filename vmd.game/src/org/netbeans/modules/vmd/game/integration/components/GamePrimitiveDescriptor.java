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
import org.netbeans.modules.vmd.game.GameController;
import org.netbeans.modules.vmd.midp.components.MidpPrimitiveDescriptor;

import java.awt.*;
import java.util.StringTokenizer;

/**
 * @author David Kaspar, Karel Herink
 */
// HINT - after making change, update GameCodeSupport too
public final class GamePrimitiveDescriptor implements PrimitiveDescriptorFactory {
	
	static final String TYPEID_STRING_TILES = "#TiledLayerTiles"; // NOI18N
	static final String TYPEID_STRING_FRAMES = "#SequenceFrames"; // NOI18N
	static final String TYPEID_STRING_POINT = "#Point"; // NOI18N
	
	static final TilesPrimitiveDescriptor PRIMITIVE_DESCRIPTOR_TILES = new TilesPrimitiveDescriptor();
	static final FramesPrimitiveDescriptor PRIMITIVE_DESCRIPTOR_FRAMES = new FramesPrimitiveDescriptor();
	static final PointPrimitiveDescriptor PRIMITIVE_DESCRIPTOR_POINT = new PointPrimitiveDescriptor();
	
	private MidpPrimitiveDescriptor midp = new MidpPrimitiveDescriptor();
	
	public String getProjectType() {
		return GameController.PROJECT_TYPE_GAME;
	}
	
	public PrimitiveDescriptor getDescriptorForTypeIDString(String string) {
		if (TYPEID_STRING_TILES.equals(string))
			return PRIMITIVE_DESCRIPTOR_TILES;
		if (TYPEID_STRING_FRAMES.equals(string))
			return PRIMITIVE_DESCRIPTOR_FRAMES;
		if (TYPEID_STRING_POINT.equals(string))
			return PRIMITIVE_DESCRIPTOR_POINT;
		//TODO
		return midp.getDescriptorForTypeIDString(string);
	}
	
	private static class PointPrimitiveDescriptor implements PrimitiveDescriptor {
		
        public String serialize(Object value) {
			Point point = (Point) value;
			StringBuilder sb = new StringBuilder();
			sb.append(Integer.toString((int) point.getX()));
			sb.append(",");
			sb.append(Integer.toString((int) point.getY()));
			return sb.toString();
        }

        public Object deserialize(String serialized) {
			String[] xAndY = serialized.split(",");
			Point p = new Point(Integer.parseInt(xAndY[0]), Integer.parseInt(xAndY[1]));
			return p;
        }

        public boolean isValidInstance(Object object) {
			return (object instanceof Point);
        }
}
	
	private static class FramesPrimitiveDescriptor implements PrimitiveDescriptor {
		
		public String serialize(Object value) {
			int[] array = (int[]) value;
			StringBuilder serialized = new StringBuilder();
			serialized.append(array.length).append(","); // NOI18N
			for (int cell : array) {
				serialized.append(cell).append(','); // NOI18N
			}
			return serialized.toString();
		}
		
		public Object deserialize(String serialized) {
			String[] tokens = serialized.split(",");
			int length = Integer.parseInt(tokens[0]);
			int[] array = new int[length];
			for (int i = 0; i < length; i++) {
				array[i] = Integer.parseInt(tokens[i+1]);
			}
			return array;
		}
		
		public boolean isValidInstance(Object object) {
			return object instanceof int[];
		}
		
	}
	
	
	// HINT - describes serialization of the TiledLayer.tiles property
	private static class TilesPrimitiveDescriptor implements PrimitiveDescriptor {
		
		public String serialize(Object value) {
			int[][] array = (int[][]) value;
			StringBuffer serialized = new StringBuffer();
			int rows = array.length;
			int cols = array.length > 0 ? array[0].length : 0;
			serialized.append(rows).append(',').append(cols);
			for (int[] row : array)
				for (int cell : row)
					serialized.append(',').append(cell);
				return serialized.toString();
		}
		
		public Object deserialize(String serialized) {
			StringTokenizer tokenizer = new StringTokenizer(serialized, ",");
			int rows = Integer.parseInt(tokenizer.nextToken());
			int cols = Integer.parseInt(tokenizer.nextToken());
			int[][] array = new int[rows][cols];
			for (int y = 0; y < rows; y ++)
				for (int x = 0; x < cols; x ++)
					array[y][x] = Integer.parseInt(tokenizer.nextToken());
			return array;
		}
		
		public boolean isValidInstance(Object object) {
			return object instanceof int[][];
		}
		
	}
}
