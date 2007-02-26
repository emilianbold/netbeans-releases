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

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.lang.ref.SoftReference;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.event.EventListenerList;

public class ImageResource {
	
	public static final boolean DEBUG = false;
	
    private GlobalRepository gameDesign;
	
	EventListenerList listenerList = new EventListenerList();
	
	private URL imageURL;
	private String relativeResourcePath;
	
	private Map<Dimension, StaticTile> emptyTiles = new HashMap<Dimension, StaticTile>();
	private Map<Dimension, StaticTile[][]> staticTileGrids = new HashMap<Dimension, StaticTile[][]>();
	private Map<Integer, AnimatedTile> animatedTiles = new HashMap<Integer, AnimatedTile>();
	private int animatedTileIndexKey = -1;
	
	private List<Sequence> sequences = new ArrayList();

	private static HashMap imgSoftReferences = new HashMap();	
	
	private static BufferedImage softenImage(URL imageURL) {
		BufferedImage img = null;
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
	ImageResource(GlobalRepository gameDesign, URL imageURL, String relativeResourcePath) {
		assert (gameDesign != null);
		this.gameDesign = gameDesign;
		this.imageURL = imageURL;
		this.relativeResourcePath = relativeResourcePath;
	}
	
	public void addImageResourceListener(ImageResourceListener l) {
		this.listenerList.add(ImageResourceListener.class, l);
	}
	public void removeImageResourceListener(ImageResourceListener l) {
		this.listenerList.remove(ImageResourceListener.class, l);
	}
	
	public Sequence createSequence(String name, int numberFrames, int frameWidth, int frameHeight) {
		if (!this.gameDesign.isComponentNameAvailable(name)) {
			throw new IllegalArgumentException("Sequence cannot be created because component name '" + name + "' already exists.");
		}
		Sequence sequence = new Sequence(name, this, numberFrames, frameWidth, frameHeight);
		this.sequences.add(sequence);
		this.fireSequenceAdded(sequence);
		return sequence;
	}
	
	public Sequence createSequence(String name, Sequence sequence) {
		if (!this.gameDesign.isComponentNameAvailable(name)) {
			throw new IllegalArgumentException("Sequence cannot be created because component name '" + name + "' already exists.");
		}
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
	
	
	public AnimatedTile createAnimatedTile(int index, String name, int firstStaticTileIndex, int tileWidth, int tileHeight) {
		assert (index < 0);
		assert (this.animatedTiles.get(index) == null);
		
		if (!this.gameDesign.isComponentNameAvailable(name)) {
			throw new IllegalArgumentException("AnimatedTile cannot be created because component name '" + name + "' already exists.");
		}
		
		if (this.animatedTileIndexKey >= index) {
			this.animatedTileIndexKey = index -1;
		}
		AnimatedTile animatedTile = new AnimatedTile(name, this, index, tileWidth, tileHeight);
		Sequence seq = animatedTile.getDefaultSequence();
		seq.setFrame(new StaticTile(this, firstStaticTileIndex, tileWidth, tileHeight), 0);
		this.animatedTiles.put(index, animatedTile);
		this.fireAnimatedTileAdded(animatedTile);
		return animatedTile;		
	}
	
	public AnimatedTile createAnimatedTile(String name, int firstStaticTileIndex, int tileWidth, int tileHeight) {
		if (!this.gameDesign.isComponentNameAvailable(name)) {
			throw new IllegalArgumentException("AnimatedTile cannot be created because component name '" + name + "' already exists.");
		}
		
		int index = this.animatedTileIndexKey--;
		return this.createAnimatedTile(index, name, firstStaticTileIndex, tileWidth, tileHeight);
	}
	
	public AnimatedTile createAnimatedTile(int index, String name, Sequence sequence) {
		assert (index < 0);
		assert (this.animatedTiles.get(index) == null);
		
		if (!this.gameDesign.isComponentNameAvailable(name)) {
			throw new IllegalArgumentException("AnimatedTile cannot be created because component name '" + name + "' already exists.");
		}
		
		if (this.animatedTileIndexKey >= index) {
			this.animatedTileIndexKey = index -1;
		}
		AnimatedTile animatedTile = new AnimatedTile(name, this, index, sequence, sequence.getFrameWidth(), sequence.getFrameHeight());
		this.animatedTiles.put(index, animatedTile);
		this.fireAnimatedTileAdded(animatedTile);		
		return animatedTile;		
	}
	
	public AnimatedTile createAnimatedTile(String name, Sequence sequence) {
		if (!this.gameDesign.isComponentNameAvailable(name)) {
			throw new IllegalArgumentException("AnimatedTile cannot be created because component name '" + name + "' already exists.");
		}
		int index = this.animatedTileIndexKey--;
		return this.createAnimatedTile(index, name, sequence);
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
	
	private StaticTile getEmptyTile(int cellWidth, int cellHeight) {
		StaticTile emptyTile = this.emptyTiles.get(getGridKey(cellWidth, cellHeight));
		if (emptyTile == null) {
			emptyTile = new StaticTile(this, Tile.EMPTY_TILE_INDEX, cellWidth, cellHeight);
			this.emptyTiles.put(getGridKey(cellWidth, cellHeight), emptyTile);
		}
		return emptyTile;
	}
	
	
	private StaticTile[][] getStaticTileGrid(int tileWidth, int tileHeight) {		
		StaticTile[][] grid = this.staticTileGrids.get(getGridKey(tileWidth, tileHeight));
		if (grid == null) {
			grid = this.initStaticTileGrid(tileWidth, tileHeight);
		}
		return grid;
	}
	
	public Set<Dimension> getTileResolutions() {
		return Collections.unmodifiableSet(this.staticTileGrids.keySet());
	}
	
	private static Dimension getGridKey(int tileWidth, int tileHeight) {
		return new Dimension(tileWidth, tileHeight);
	}
	
	public int getStaticTileCount(int tileWidth, int tileHeight) {
		return this.getRowCount(tileWidth, tileHeight) * this.getColumnCount(tileWidth, tileHeight);
	}
	
	public int getStaticTileIndex(int cellRow, int cellColumn, int tileWidth, int tileHeight) {
		return this.getStaticTileAt(cellRow, cellColumn, tileWidth, tileHeight).getIndex();
	}
	
	public Tile getTile(int index, int tileWidth, int tileHeight) {
		//zero is empty tile
		if (index == Tile.EMPTY_TILE_INDEX)
			return this.getEmptyTile(tileWidth, tileHeight);
		//bigger than zero is a static tile
		if (index > Tile.EMPTY_TILE_INDEX) {
			int[] coordinates = this.translateIndex(index, tileWidth, tileHeight);
			return this.getStaticTileGrid(tileWidth, tileHeight)[coordinates[0]][coordinates[1]];
		}
		//smaller than zero is an animated tile
		else {
			return (Tile) this.animatedTiles.get(new Integer(index));
		}
	}
	
	public List<AnimatedTile> getAnimatedTiles() {
		List<AnimatedTile> list = new ArrayList<AnimatedTile>(this.animatedTiles.values());
		Collections.sort(list);
		return list;
	}
	
	public List<AnimatedTile> getAnimatedTiles(int width, int height) {
		List<AnimatedTile> list = new ArrayList<AnimatedTile>();
		for (AnimatedTile at : this.animatedTiles.values()) {
			if (at.getWidth() == width && at.getHeight() == height) {
				list.add(at);
			}
		}
		return list;
	}
	
	public StaticTile getStaticTileAt(int row, int col, int tileWidth, int tileHeight) {
		StaticTile[][] grid = this.getStaticTileGrid(tileWidth, tileHeight);
		return grid[row][col];
	}
	
	/**
	 * Returns int[row, col].
	 */
	private int[] translateIndex(int index, int tileWidth, int tileHeight) {
		StaticTile[][] grid = this.getStaticTileGrid(tileWidth, tileHeight);
		int[] coordinates = new int[2];
		coordinates[0] = (index -1) / grid[0].length;
		coordinates[1] = (index -1) % grid[0].length;
		return coordinates;
	}
	
	public int getRowCount(int tileWidth, int tileHeight) {
		StaticTile[][] grid = this.getStaticTileGrid(tileWidth, tileHeight);
		return grid.length;
	}
	
	public int getColumnCount(int tileWidth, int tileHeight) {
		StaticTile[][] grid = this.getStaticTileGrid(tileWidth, tileHeight);
		return grid[0].length;
	}
	
	private StaticTile[][] initStaticTileGrid(int tileWidth, int tileHeight) {
		BufferedImage img = this.softenImage(this.imageURL);
		int rows = img.getHeight(null) / tileHeight;
		int cols = img.getWidth(null) / tileWidth;
		
		StaticTile[][] staticTileGrid = new StaticTile[rows][cols];
		int index = 1;
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				StaticTile staticTile = new StaticTile(this, index++, tileWidth, tileHeight);
				staticTileGrid[i][j] = staticTile;
			}
		}
		this.staticTileGrids.put(getGridKey(tileWidth, tileHeight), staticTileGrid);
		return staticTileGrid;
	}
	
	void paint(int index, Graphics2D g, int x, int y, int tileWidth, int tileHeight) {
		if (index == Tile.EMPTY_TILE_INDEX)
			return;
		//static tile
		if (index > Tile.EMPTY_TILE_INDEX) {
			BufferedImage img = this.softenImage(this.imageURL);
			int[] coordinates = this.translateIndex(index, tileWidth, tileHeight);
			int resX = coordinates[1] * tileWidth;
			int resY = coordinates[0] * tileHeight;
			
			g.drawImage(img, x, y, x + tileWidth, y + tileHeight, resX, resY, resX + tileWidth, resY + tileHeight, null);
		}
		//animated tile
		else {
			AnimatedTile a = (AnimatedTile) this.getTile(index, tileWidth, tileHeight);
			a.paint(g, x, y);
		}
	}
		
	public String getRelativeResourcePath() {
		return this.relativeResourcePath;
	}
	
	public String toString() {
		return "ImageResource: " + this.imageURL;
	}
	
	public GlobalRepository getGameDesign() {
		return this.gameDesign;
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

}
