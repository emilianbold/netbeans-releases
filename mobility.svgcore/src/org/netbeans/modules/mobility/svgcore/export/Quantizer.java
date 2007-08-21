/*
 * Helma License Notice
 *
 * The contents of this file are subject to the Helma License
 * Version 2.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://adele.helma.org/download/helma/license.txt
 *
 * Copyright 1998-2003 Helma Software. All Rights Reserved.
 */

/*
 * This Class is a port from the C++ class CQuantizer, 
 * freely distributed on http://www.xdp.it/cquantizer.htm
 *
 * The following addaptions and extensions were added by
 * JŸrg Lehni (juerg@vectorama.org):
 * - Conversion of the input image to a BufferedImage with a IndexColorModel
 * - Dithering of images through the java2d's internal dithering algorithms,
 *   by setting the dither parameter to true.
 * - Support for a transparent color, which is correctly rendered by
 *   GIFImageWriter. All pixels with alpha < 0x80 are converted to this
 *   color when the parameter alphaToBitmask is set to true.
 */

package org.netbeans.modules.mobility.svgcore.export;

import java.awt.image.*;
import java.awt.*;

public class Quantizer {
	private class Node {
		boolean isLeaf;		// true if node has no children
		int pixelCount;		// Number of pixels represented by this leaf
		int redSum;			// Sum of red components
		int greenSum;		// Sum of green components
		int blueSum;		// Sum of blue components
		int alphaSum;		// Sum of alpha components
		int level;
		Node childNodes[];  // child nodes
		Node nextNode;		// the next reducible node
		
		public Node(int level, int significantBits) {
			redSum = greenSum = blueSum = alphaSum = pixelCount = 0;
			childNodes = new Node[significantBits];
			for	(int i = 0; i < significantBits; i++) childNodes[i] = null;
			nextNode = null;
			this.level = level;
			isLeaf = (level == significantBits);
		}

		protected void addColor(int r, int g, int b, int a) {
			// Update color	information if it's a leaf node.
			if (isLeaf) {
				pixelCount++;
				redSum += r;
				greenSum += g;
				blueSum += b;
				alphaSum += a;
			} else { // Recurse a level deeper if the node is not a leaf.
				int	shift =	7 -	level;
				int	index =(((r & mask[level]) >>> shift) << 2) |
					(((g & mask[level]) >>> shift) << 1) |
					(( b & mask[level]) >>> shift);
				Node child = childNodes[index];
				if (child == null) child = childNodes[index] = createNode(level + 1);
				child.addColor(r, g, b, a);
			}
		}			
	}
	
	static int mask[] = { 0x80, 0x40, 0x20, 0x10, 0x08, 0x04, 0x02, 0x01 };
	
    Node tree;
    int leafCount;
    Node[] reducibleNodes;
    int maxColors;
    int outputMaxColors;
    int significantBits;
	boolean dither;
	boolean bitmask;
	boolean addTransparency;
	
	public Quantizer(int numColors, boolean dithering, boolean alphaToBitmask) {
		maxColors = outputMaxColors = numColors;
		if (maxColors < 16) maxColors = 16; // this is for the workaround for too small color spaces (see getColorTable)

		dither = dithering;
		bitmask = alphaToBitmask;
		// the values for significantBits are chosen automatically. the less colors, the higher it should be (<= 8).
		// maybe playing around with these settings here is necessary... (lehni)
		/*if (maxColors > 128) significantBits = 6;
		else if (maxColors > 64) significantBits = 7;
		else*/ significantBits = 8;

		reducibleNodes = new Node[significantBits + 1];
		for	(int i=0; i <= significantBits; i++)
			reducibleNodes[i] = null;
			
		this.tree = null;
		leafCount = 0;
	}
	
	public BufferedImage quantizeImage(BufferedImage bi) {
		processImage(bi);
		byte[][] palette = getColorTable();
		int numColors = palette[0].length;
		
		// int depth = (int)Math.ceil(Math.log((float)numColors) / Math.log(2.0));
		int depth;
		for (depth=1; depth <=8; depth++)
			if ((1<<depth) >= numColors) break;

		IndexColorModel icm;
		if (addTransparency) icm = new IndexColorModel(depth, numColors, palette[0], palette[1], palette[2], 0);
		else icm = new IndexColorModel(depth, numColors, palette[0], palette[1], palette[2], palette[3]);
		int w = bi.getWidth(), h = bi.getHeight();
		BufferedImage indexed = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_INDEXED, icm);
		Graphics2D g2d = indexed.createGraphics();
               
		g2d.setRenderingHint(RenderingHints.KEY_DITHERING, dither ? RenderingHints.VALUE_DITHER_ENABLE : RenderingHints.VALUE_DITHER_DISABLE);
		g2d.drawImage(bi, 0, 0, null);
		g2d.dispose();
		return indexed;
	}
	
    public void processImage(BufferedImage bi) {
		addTransparency = false;
		int width = bi.getWidth();
		int height = bi.getHeight();
		tree = createNode(0);
		int[] row = new int[width];
		for	(int i=0; i < height; i++) {
			// fetch one row into memory:
			row = bi.getRGB(0, i, width, 1, row, 0, width); 
			for	(int j=0; j < width; j++) {
				int pixel = row[j];
				int a = (pixel >> 24) & 0xff;
				int r = (pixel >> 16) & 0xff;
				int g = (pixel >>  8) & 0xff;
				int b = (pixel      ) & 0xff;
				if (bitmask)  a = a < 0x80 ? 0 : 0xff;
				if (a > 0) {
					tree.addColor(r, g, b, a);
					while (leafCount > maxColors) reduceTree();
				} else if(!addTransparency) {
					maxColors--; // make place for the transparent color
					addTransparency = true;
				}
			}
		}
	}

	protected Node createNode(int level) {
		Node node = new Node(level, significantBits);
		 if (node.isLeaf) leafCount++;
		 else {
			 node.nextNode = reducibleNodes[level];
			 reducibleNodes[level] = node;
		 }
		 return node;
	 }
		
    void reduceTree() {
		// Find	the	deepest	level containing at	least one reducible	node.
		int i;
		for	(i=significantBits - 1; (i>0) && (reducibleNodes[i] == null); i--);
		
		// Reduce the node most	recently added to the list at level	i.
		Node node = reducibleNodes[i];
		reducibleNodes[i] = node.nextNode;
		
		int redSum = 0;
		int greenSum = 0;
		int blueSum = 0;
		int alphaSum = 0;
		int nChildren = 0;
		
		for	(i = 0; i < significantBits; i++)	{
			if (node.childNodes[i] != null) {
				Node child = node.childNodes[i];
				redSum	+= child.redSum;
				greenSum += child.greenSum;
				blueSum +=	child.blueSum;
				alphaSum += child.alphaSum;
				node.pixelCount += child.pixelCount;
				node.childNodes[i] = null;
				nChildren++;
			}
		}
		
		node.isLeaf = true;
		node.redSum = redSum;
		node.greenSum = greenSum;
		node.blueSum = blueSum;
		node.alphaSum = alphaSum;
		leafCount -= (nChildren - 1);
	}
	
    int getPaletteColors(Node node, byte[][] palette, int index, int[] sums) {
		if (node != null) {
			if (node.isLeaf) {
				palette[0][index] = (byte)(node.redSum / node.pixelCount);
				palette[1][index] = (byte)(node.greenSum / node.pixelCount);
				palette[2][index] = (byte)(node.blueSum / node.pixelCount);
				palette[3][index] = (byte)(node.alphaSum / node.pixelCount);
				if (sums != null) sums[index] = node.pixelCount;
				index++;
			} else {
				for	(int i = 0; i < significantBits; i++) {
					Node child = node.childNodes[i];
					if (child != null) index = getPaletteColors(child, palette, index, null);
				}
			}
		}
		return index;
	}

    byte[][] getColorTable() {
		byte[][] palette = null;
		int transOffs = addTransparency ? 1 : 0; // make room for the transparency color at index 0 ?
		if (outputMaxColors < 16) {
			byte tmpPal[][] = new byte[4][outputMaxColors];
			int sums[] = new int[outputMaxColors];
			getPaletteColors(tree, tmpPal, 0, sums);
			if (leafCount > outputMaxColors) {
				palette = new byte[4][outputMaxColors + transOffs];
				for (int j=0; j < outputMaxColors; j++) {
					int a = (j * leafCount) / outputMaxColors;
					int b = ((j + 1) * leafCount) / outputMaxColors;
					int nr, ng, nb, na, ns;
					nr = ng = nb = na = ns = 0;
					for (int l = a; l < b; l++){
						int s = sums[l];
						int m = l + transOffs;
						nr += tmpPal[0][m] * s;
						ng += tmpPal[1][m] * s;
						nb += tmpPal[2][m] * s;
						na += tmpPal[3][m] * s;
						ns += s;
					}
					int k = j + transOffs;
					palette[0][k] = (byte)(nr / ns);
					palette[1][k] = (byte)(ng / ns);
					palette[2][k] = (byte)(nb / ns);
					palette[3][k] = (byte)(na / ns);
				}
			} else {
				palette = new byte[4][leafCount + transOffs];
				for (int i = 0; i < leafCount; i++) {
					int j = i + transOffs;
					for (int k = 0; k < 4; k++)
						palette[k][j] = tmpPal[k][i];
				}
			}
		} else {
			palette = new byte[4][leafCount + transOffs];
			getPaletteColors(tree, palette, transOffs, null);
		}
		if (addTransparency)
			palette[0][0] = palette[1][0] = palette[2][0] = palette[3][0] = 0;

		return palette;
	}
}