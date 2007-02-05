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
package org.netbeans.modules.vmd.game.model;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.PrintStream;
import java.lang.ref.SoftReference;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.event.EventListenerList;

public class ImageResource implements CodeGenerator {
	
	public static final boolean DEBUG = false;
	
	EventListenerList listenerList = new EventListenerList();
	
	private URL imageURL;
	private String relativeResourcePath;
	private int cellWidth;
	private int cellHeight;
	
	private StaticTile[][] staticTileGrid;
	
	
	private Map<Integer, AnimatedTile> animatedTiles = new HashMap<Integer, AnimatedTile>();
	private int animatedTileIndexKey = -1;
	
	private List<Sequence> sequences = new ArrayList();
	
	private StaticTile emptyTile;

	private static HashMap imgSoftReferences = new HashMap();	
	
	private static BufferedImage softenImage(URL imageURL) {
		BufferedImage img = null;
		//if (DEBUG) System.out.println("URL : " + imageURL);
		//if (DEBUG) System.out.println("imgSoftReferences: "  + imgSoftReferences);
		SoftReference softie = (SoftReference) imgSoftReferences.get(imageURL);
		if (softie != null)
			img = (BufferedImage) (softie).get();
		if (img == null) {
			if (DEBUG) System.out.println(">>>>> " + imageURL + " NOT available - reloading");
			img = ImageUtils.loadImage(imageURL);
			imgSoftReferences.put(imageURL, new SoftReference(img));
		}
		return img;
	}
	
	/**
	 * Only called from GlobalRepository.getImageResource()
	 */
	ImageResource(URL imageURL, String relativeResourcePath, int cellWidth, int cellHeight) {
		this.imageURL = imageURL;
		this.relativeResourcePath = relativeResourcePath;
		this.cellWidth = cellWidth;
		this.cellHeight = cellHeight;
		this.emptyTile = createEmptyTile(cellWidth, cellHeight);
		BufferedImage img = this.softenImage(imageURL);
		int rows = img.getHeight(null) / cellHeight;
		int cols = img.getWidth(null) / cellWidth;
		this.initStaticTileGrid(rows, cols);
	}
	
	public void addImageResourceListener(ImageResourceListener l) {
		this.listenerList.add(ImageResourceListener.class, l);
	}
	public void removeImageResourceListener(ImageResourceListener l) {
		this.listenerList.remove(ImageResourceListener.class, l);
	}
	
	public Sequence createSequence(String name) {
		return this.createSequence(name, 1);
	}
	public Sequence createSequence(String name, int numberFrames) {
		Sequence sequence = new Sequence(name, this, numberFrames);
		this.sequences.add(sequence);
		this.fireSequenceAdded(sequence);
		return sequence;
	}
	
	public Sequence createSequence(String name, Sequence sequence) {
		Sequence newSequence = new Sequence(name, sequence);
		this.sequences.add(newSequence);
		this.fireSequenceAdded(newSequence);
		return newSequence;
	}	

	public Sequence getSequenceByName(String name) {
		for (Sequence sequence : this.sequences) {
			if (sequence.getName().equals(name))
				return sequence;
		}
		return null;
	}
	
	public void fireSequenceAdded(Sequence sequence) {
		Object[] listeners = listenerList.getListenerList();
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == ImageResourceListener.class) {
				((ImageResourceListener) listeners[i+1]).sequenceAdded(this, sequence);
			}
		}
	}
	
	
	
	public void removeSequence(Sequence sequence) {
		this.sequences.remove(sequence);
		this.fireSequenceRemoved(sequence);
	}
	
	public void fireSequenceRemoved(Sequence sequence) {
		Object[] listeners = listenerList.getListenerList();
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == ImageResourceListener.class) {
				((ImageResourceListener) listeners[i+1]).sequenceRemoved(this, sequence);
			}
		}
	}
	
	public List getSequences() {
		return Collections.unmodifiableList(sequences);
	}
	
	public AnimatedTile createAnimatedTile(String name, int firstStaticTileIndex) {
		int index = this.animatedTileIndexKey--;
		AnimatedTile animatedTile = new AnimatedTile(name, this, index);
		Sequence seq = animatedTile.getDefaultSequence();
		seq.setFrame(new StaticTile(this, firstStaticTileIndex), 0);
		this.animatedTiles.put(new Integer(index), animatedTile);
		this.fireAnimatedTileAdded(animatedTile);
		return animatedTile;
	}
	
	public AnimatedTile createAnimatedTile(String name, Sequence sequence) {
		int index = this.animatedTileIndexKey--;
		AnimatedTile animatedTile = new AnimatedTile(name, this, index, sequence);
		this.animatedTiles.put(index, animatedTile);
		this.fireAnimatedTileAdded(animatedTile);		
		return animatedTile;
	}
	
	public AnimatedTile createAnimatedTile(int[] staticTileIndexes) {
		assert(staticTileIndexes != null);
		assert(staticTileIndexes.length >= 1);
		AnimatedTile at = createAnimatedTile(this.getNextAnimatedTileName(), staticTileIndexes[0]);
		if (staticTileIndexes.length > 1) {
			Sequence seq = at.getDefaultSequence();
			for (int i = 1; i < staticTileIndexes.length; i++) {
				seq.setFrame(new StaticTile(this, staticTileIndexes[i]), i);
			}
		}
		return at;
	}
	
	private static final String ANIMATED_TILE_NAME_PREFIX = "AnimTile";
	private String getNextAnimatedTileName() {
		int biggestNum = 0;
		for (AnimatedTile at : this.getAnimatedTiles()) {
			String name = at.getName();
			if (name.startsWith(ANIMATED_TILE_NAME_PREFIX)) {
				try {
					int num = Integer.parseInt(name.substring(ANIMATED_TILE_NAME_PREFIX.length()));
					if (num > biggestNum)
						biggestNum = num;
				} catch (NumberFormatException nfe) {
				}
			}
		}
		DecimalFormat df = new DecimalFormat("000");
		String nextNum = df.format(biggestNum + 1);
		return ANIMATED_TILE_NAME_PREFIX + nextNum;
	}
	
	
	public void fireAnimatedTileAdded(AnimatedTile animatedTile) {
		Object[] listeners = listenerList.getListenerList();
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == ImageResourceListener.class) {
				((ImageResourceListener) listeners[i+1]).animatedTileAdded(this, animatedTile);
			}
		}
	}
	
	public void removeAnimatedTile(int index) {
		AnimatedTile removedTile = (AnimatedTile) this.animatedTiles.remove(new Integer(index));
		this.fireAnimatedTileRemoved(removedTile);
	}
	
	public void fireAnimatedTileRemoved(AnimatedTile animatedTile) {
		Object[] listeners = listenerList.getListenerList();
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == ImageResourceListener.class) {
				((ImageResourceListener) listeners[i+1]).animatedTileRemoved(this, animatedTile);
			}
		}
	}
	
	
	public AnimatedTile getAnimatedTileByName(String name) {
		Collection c = this.animatedTiles.values();
		for (Iterator iter = c.iterator(); iter.hasNext(); ) {
			AnimatedTile tile = (AnimatedTile) iter.next();
			if (tile.getName().equals(name))
				return tile;
		}
		return null;
	}
	
	private StaticTile createEmptyTile(int cellWidth, int cellHeight) {
		StaticTile emptyTile = new StaticTile(this, Tile.EMPTY_TILE_INDEX);
		return emptyTile;
	}
	
	
	private StaticTile[][] getStaticTileGrid() {
		return staticTileGrid;
	}
	
	public int getStaticTileCount() {
		return this.getRowCount() * this.getColumnCount();
	}
	
	public int getStaticTileIndex(int cellRow, int cellColumn) {
		return this.getStaticTileAt(cellRow, cellColumn).getIndex();
	}
	
	public Tile getTile(int index) {
		//zero is empty tile
		if (index == Tile.EMPTY_TILE_INDEX)
			return this.emptyTile;
		//bigger than zero is a static tile
		if (index > Tile.EMPTY_TILE_INDEX) {
			int[] coordinates = this.translateIndex(index);
			return this.getStaticTileGrid()[coordinates[0]][coordinates[1]];
		}
		//smaller than zero is an animated tile
		else {
			return (Tile) this.animatedTiles.get(new Integer(index));
		}
	}
	
	public List<AnimatedTile> getAnimatedTiles() {
		List<AnimatedTile> list = new ArrayList(this.animatedTiles.values());
		Collections.sort(list);
		return Collections.unmodifiableList(list);
	}
	
	public String getRelativeResourcePath() {
		return this.relativeResourcePath;
	}
	
	public int getCellHeight() {
		return this.cellHeight;
	}
	
	public int getCellWidth() {
		return this.cellWidth;
	}
	
	public StaticTile getStaticTileAt(int row, int col) {
		return this.getStaticTileGrid()[row][col];
	}
	
	/**
	 * Returns int[row, col].
	 */
	private int[] translateIndex(int index) {
		int[] coordinates = new int[2];
		coordinates[0] = (index -1) / this.getStaticTileGrid()[0].length;
		coordinates[1] = (index -1) % this.getStaticTileGrid()[0].length;
		return coordinates;
	}
	
	public int getRowCount() {
		return this.getStaticTileGrid().length;
	}
	
	public int getColumnCount() {
		return this.getStaticTileGrid()[0].length;
	}
	
	
	private void initStaticTileGrid(int rows, int cols) {
		this.staticTileGrid = new StaticTile[rows][cols];
		int index = 1;
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				StaticTile staticTile = new StaticTile(this, index++);
				staticTileGrid[i][j] = staticTile;
			}
		}
	}
	
	void paint(int index, Graphics2D g, int x, int y) {
		if (index == Tile.EMPTY_TILE_INDEX)
			return;
		//static tile
		if (index > Tile.EMPTY_TILE_INDEX) {
			BufferedImage img = this.softenImage(this.imageURL);
			int[] coordinates = this.translateIndex(index);
			int resX = coordinates[1] * this.cellWidth;
			int resY = coordinates[0] * this.cellHeight;
			
			g.drawImage(img, x, y, x + this.cellWidth, y + this.cellHeight, resX, resY, resX + this.cellWidth, resY + this.cellHeight, null);
		}
		//animated tile
		else {
			AnimatedTile a = (AnimatedTile) this.getTile(index);
			a.paint(g, x, y);
		}
	}
		
	public String toString() {
		return "ImageResource: " + this.imageURL + " " + this.cellWidth + "x" + this.cellHeight;
	}
	
	public URL getURL() {
		return this.imageURL;
	}
	
	public String getName() {
		String path = this.getURL().getPath();
		int index = path.lastIndexOf("/");
		String name = path.substring(index + 1);
		return name;
	}
	
	public String getNameNoExt() {
		String name = this.getName();
		name = name.substring(0, name.lastIndexOf("."));
		return name;
	}
	
	public Collection getCodeGenerators() {
		HashSet gens = new HashSet();
		//AnimatedTiles are added for each TiledLayer - not from here
		//WRONG TO DO HERE: gens.add(this.getAnimatedTiles());
		gens.addAll(this.sequences);
		return gens;
	}
	
	public void generateCode(PrintStream ps) {
		ps.println("private Image image_" + this.getNameNoExt() + ";");
		ps.println("public Image getImage_" + this.getNameNoExt() + "() throws IOException {");
		ps.println("    if (this.image_" + this.getNameNoExt() + " == null)");
		ps.println("		this.image_" + this.getNameNoExt() +" = Image.createImage(\"/" + this.getName() + "\");");
		ps.println("	return this.image_" + this.getNameNoExt() + ";");
		ps.println("}");
	}
	
}
