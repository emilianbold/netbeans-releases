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

import java.awt.Dialog;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.swing.Action;
import org.netbeans.modules.vmd.game.editor.tiledlayer.TiledLayerEditor;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.event.EventListenerList;
import org.netbeans.modules.vmd.game.dialog.RenameTiledLayerDialog;
import org.netbeans.modules.vmd.game.editor.tiledlayer.TiledLayerNavigator;
import org.netbeans.modules.vmd.game.editor.tiledlayer.TiledLayerPreviewPanel;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;

public class TiledLayer extends Layer {

    private TiledLayerPreviewPanel preview;
    private TiledLayerNavigator navigator;

	public static final boolean DEBUG = false;

	EventListenerList listenerList = new EventListenerList();
	
	private JComponent editor;
	private int[][] grid;
	
	TiledLayer(GlobalRepository gameDesign, String name, ImageResource imageResource, int rows, int columns, int tileWidth, int tileHeight) {
		super(gameDesign, name, imageResource, tileWidth, tileHeight);
		this.grid = new int[rows][columns];
	}
	
	TiledLayer(GlobalRepository gameDesign, String name, ImageResource imageResource, int[][] grid, int tileWidth, int tileHeight) {
		super(gameDesign, name, imageResource, tileWidth, tileHeight);
		this.grid = grid;
	}
	
	TiledLayer(GlobalRepository gameDesign, String name, TiledLayer tiledLayer) {
		super(gameDesign, name, tiledLayer.getImageResource(), tiledLayer.getTileWidth(), tiledLayer.getTileHeight());
		this.grid = new int[tiledLayer.grid.length][];
		for (int i = 0; i < this.grid.length; i++) {
			int[] copyRow = new int[tiledLayer.grid[i].length];
			System.arraycopy(tiledLayer.grid[i], 0, copyRow, 0, tiledLayer.grid[i].length);
			this.grid[i] = copyRow;
		}
	}
	
	public ImageResourceInfo getImageResourceInfo() {
		return new ImageResourceInfo(this.getImageResource(), this.getTileWidth(), this.getTileHeight(), false);
	}
	
	
	public void addTiledLayerListener(TiledLayerListener l) {
		this.listenerList.add(TiledLayerListener.class, l);
	}
	
	public void removeTiledLayerListener(TiledLayerListener l) {
		this.listenerList.remove(TiledLayerListener.class, l);
	}
	
	
	public JComponent getEditor() {
		return this.editor == null ? this.editor = new TiledLayerEditor(this) : this.editor;
	}
	
	public int getRowCount() {
		return this.grid.length;
	}

	public int getColumnCount() {
		return this.grid[0].length;
	}

	public int getTileIndexAt(Position position) {
		return this.getTileIndexAt(position.getRow(), position.getCol());
	}
	public int getTileIndexAt(int rowIndex, int columnIndex) {
		if (rowIndex >= this.grid.length || columnIndex >= this.grid[0].length)
			return 0;
		return grid[rowIndex][columnIndex];
	}

	public Tile getTileAt(Position position) {
		return getTileAt(position.getRow(), position.getCol());
	}
	
	public Tile getTileAt(int rowIndex, int columnIndex) {
		if (rowIndex < 0 || rowIndex >= this.getRowCount() || columnIndex < 0 || columnIndex >= this.getColumnCount())
			return this.getImageResource().getTile(Tile.EMPTY_TILE_INDEX, this.getTileWidth(), this.getTileHeight(), false);
		Tile tile = null;
		int tileIndex = this.grid[rowIndex][columnIndex];
		tile = this.getImageResource().getTile(tileIndex, this.getTileWidth(), this.getTileHeight(), false);
		return tile;
	}
	
	/**
	 * Assigns a tile index to multiple positions.
	 */
	public void setTileAtPositions(int tileIndex, Set positions) {
		for (Iterator it = positions.iterator(); it.hasNext();) {
			Position pos = (Position) it.next();
			this.updateIndexGrid(tileIndex, pos.getRow(), pos.getCol());
		}
		this.fireTilesChanged(positions);
	}
	
	private void fireTilesChanged(Set positions) {
		Object[] listeners = listenerList.getListenerList();
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == TiledLayerListener.class) {
				((TiledLayerListener) listeners[i+1]).tilesChanged(this, positions);
			}
		}
	}	

	/**
	 * Returns AnimatedTiles currently assigned to this TiledLayer.
	 */
	private Set getAnimatedTiles() {
		HashSet set = new HashSet();
		for (int i = 0; i < this.grid.length; i++ ) {
			for (int j = 0; j < this.grid[i].length; j++) {
				Tile tile = this.getImageResource().getTile(this.grid[i][j], this.getTileWidth(), this.getTileHeight(), false);
				if (tile instanceof AnimatedTile) {
					set.add(tile);
				}
			}
		}
		return set;
	}
	
	public void setTileAt(int tileIndex, Position position) {
		this.setTileAt(tileIndex, position.getRow(), position.getCol());
	}
	
	public void setTileAt(int tileIndex, int rowIndex, int columnIndex) {
		if (updateIndexGrid(tileIndex, rowIndex, columnIndex)) {
			this.fireTileChanged(rowIndex, columnIndex);
		}
	}
	
	private void fireTileChanged(int rowIndex, int columnIndex) {
		Object[] listeners = listenerList.getListenerList();
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == TiledLayerListener.class) {
				((TiledLayerListener) listeners[i+1]).tileChanged(this, rowIndex, columnIndex);
			}
		}
	}	

	
	private boolean updateIndexGrid(int tileIndex, int rowIndex, int columnIndex) {
		while (rowIndex < 0) {
			this.insertRows(0, 1);
			rowIndex++;
		}
		while (columnIndex < 0) {
			this.insertColumns(0, 1);
			columnIndex++;
		}
		
		boolean changed = false;
		
		if (rowIndex >= this.getRowCount() || columnIndex >= this.getColumnCount()) {
			this.growLayerToSize(rowIndex, columnIndex);
			changed = true;
		}
		
		if (this.grid[rowIndex][columnIndex] == tileIndex && !changed)
			return false;
		
		this.grid[rowIndex][columnIndex] = tileIndex;
		return true;
	}
		
	public void growLayerToSize(int rows, int cols) {
		int origRows = this.getRowCount();
		int origCols = this.getColumnCount();
		if (rows >= this.getRowCount()) {
			int newRowCount = rows + 1;
			int[][] newRows = new int[newRowCount][];
			System.arraycopy(this.grid, 0, newRows, 0, this.grid.length);
			for (int row = this.grid.length; row < newRowCount; row++) {
				newRows[row] = new int[this.getColumnCount()];
			}
			this.grid = newRows;
		}
		if (cols >= this.getColumnCount()) {
			int newColCount = cols + 1;
			for (int row = 0; row < this.getRowCount(); row++) {
				int[] oldRow = this.grid[row];
				int[] newRow = new int[newColCount];
				System.arraycopy(oldRow, 0, newRow, 0, oldRow.length);
				this.grid[row] = newRow;
			}
		}
		
		int newRowCount = this.getRowCount();
		int newColCount = this.getColumnCount();
		if (origRows < newRowCount) {
			this.fireRowsInserted(origRows - 1, newRowCount - origRows);
		}
		if (origCols < newColCount) {
			this.fireColumnsInserted(origCols - 1, newColCount - origCols);
		}
	}
	
	/**
	 * Removes all outer rows and columns that contain only empty tiles. Thus shrinking
	 * the layer to minimal grid size without affecting the visible content.
	 */
	public void trimToSize() {
		int left = Integer.MAX_VALUE;
		int right = Integer.MIN_VALUE;
		int top = Integer.MAX_VALUE;
		int bottom = Integer.MIN_VALUE;
		synchronized (this.grid) {
			for (int r = 0; r < this.grid.length; r++ ) {
				for (int c = 0; c < this.grid[r].length; c++) {
					if (this.grid[r][c] != Tile.EMPTY_TILE_INDEX) {
						//find left boundry
						left = Math.min(left, c);
						//find right boundry
						right = Math.max(right, c);
						//find top boundry
						top = Math.min(top, r);
						//find bottom boundry
						bottom = Math.max(bottom, r);
					}
				}
			}
			//then remove all the rows and cols outside the boundries
			//remove right
			this.deleteColumns(right + 1, this.grid[0].length - right -1);
			//remove bottom
			this.deleteRows(bottom + 1, this.grid.length - bottom -1);
			//remove left
			this.deleteColumns(0, left);
			//remove top
			this.deleteRows(0, top);
		}
	}
	
	
	private void fireRowsInserted(int index, int count) {
		Object[] listeners = listenerList.getListenerList();
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == TiledLayerListener.class) {
				((TiledLayerListener) listeners[i+1]).rowsInserted(this, index, count);
			}
		}
	}	
	private void fireRowsRemoved(int index, int count) {
		Object[] listeners = listenerList.getListenerList();
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == TiledLayerListener.class) {
				((TiledLayerListener) listeners[i+1]).rowsRemoved(this, index, count);
			}
		}
	}	
	private void fireColumnsInserted(int index, int count) {
		Object[] listeners = listenerList.getListenerList();
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == TiledLayerListener.class) {
				((TiledLayerListener) listeners[i+1]).columnsInserted(this, index, count);
			}
		}
	}
	private void fireColumnsRemoved(int index, int count) {
		Object[] listeners = listenerList.getListenerList();
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == TiledLayerListener.class) {
				((TiledLayerListener) listeners[i+1]).columnsRemoved(this, index, count);
			}
		}
	}

	public void insertRows(int insRowIndex, int count) {
		int[][] newGrid = new int[this.grid.length + count][];
		
		for (int rowIndex = 0, newIndex = 0; newIndex < newGrid.length; newIndex++, rowIndex++) {
			
			//copy first existing rows
			if (newIndex < insRowIndex) {
				newGrid[newIndex] = this.grid[rowIndex];
			}
			//insert new empty rows
			else if (newIndex == insRowIndex) {
				for (int i = 0; i < count; i++) {
					newGrid[newIndex] = new int[this.grid[0].length];
					newIndex++;
				}
				if (rowIndex < this.grid.length) {
					newGrid[newIndex] = this.grid[rowIndex];
				}
			}
			//copy last existing rows
			else {
				newGrid[newIndex] = this.grid[rowIndex];
			}
			
		}
		this.grid = newGrid;
		
		this.fireRowsInserted(insRowIndex, count);
	}
	
	public void deleteRows(int rowIndex, int count) {
		int endIndex = rowIndex + count -1;
		int[][] newGrid = new int[this.grid.length - count][];
		for (int r = 0, index = 0; r < this.grid.length; r++ ) {
			if (r < rowIndex || r > endIndex) {
				newGrid[index] = this.grid[r];
				index++;
			}
		}
		this.grid = newGrid;
		this.fireRowsRemoved(rowIndex, count);
	}
	
	public void insertColumns(int colIndex, int count) {
		int[] insert = new int[count];
		for (int r = 0; r < this.grid.length; r++) {
			int[] row = this.grid[r];
			int[] newRow = new int[row.length + count];
			//copy the part before insert
			System.arraycopy(row, 0, newRow, 0, colIndex);
			//copy the insert
			System.arraycopy(insert, 0, newRow, colIndex, count);
			//copy the part after insert
			System.arraycopy(row, colIndex, newRow, colIndex + count, row.length - colIndex);
			
			this.grid[r] = newRow;
		}
		this.fireColumnsInserted(colIndex, count);
	}
	
	public void deleteColumns(int colIndex, int count) {
		int endIndex = colIndex + count;
		synchronized (this.grid) {
			for (int r = 0; r < this.grid.length; r++ ) {
				int[] newRow = new int[this.grid[r].length - count];
				System.arraycopy(this.grid[r], 0, newRow, 0, colIndex);
				System.arraycopy(this.grid[r], endIndex, newRow, colIndex, this.grid[r].length - endIndex);
				this.grid[r] = newRow;
			}
		}
		this.fireColumnsRemoved(colIndex, count);
	}

	public int getHeight() {
		return this.grid.length * this.getTileHeight();
	}

	public int getWidth() {
		return this.grid[0].length * this.getTileWidth();
	}

	public List<Action> getActions() {
		List<Action> superActions = super.getActions();
		List<Action> actions = new ArrayList<Action>();
		actions.addAll(superActions);
		actions.add(new RenameAction());
		return actions;
	}
	
	public class RenameAction extends AbstractAction {
		{
			this.putValue(NAME,  NbBundle.getMessage(TiledLayer.class, "TiledLayer.RenameAction.text"));
		}
		public void actionPerformed(ActionEvent e) {
			RenameTiledLayerDialog dialog = new RenameTiledLayerDialog(TiledLayer.this);
			DialogDescriptor dd = new DialogDescriptor(dialog, NbBundle.getMessage(TiledLayer.class, "TiledLayer.RenameAction.text"));
			dd.setButtonListener(dialog);
			dd.setValid(false);
			dialog.setDialogDescriptor(dd);
			Dialog d = DialogDisplayer.getDefault().createDialog(dd);
			d.setVisible(true);
		}
	}

	public String getDisplayableTypeName() {
		return NbBundle.getMessage(TiledLayer.class, "TiledLayer.text");
	}
	
	public int[][] getTiles() {
		return this.grid;
	}
	
	// Previewable
	public void paint(Graphics2D g, int x, int y) {
		// TODO
	}
		
	public JComponent getPreview() {
		if (this.preview == null) {
            return this.preview = new TiledLayerPreviewPanel(this, true);
        }
		return this.preview;
	}

	public JComponent getNavigator() {
		if (this.navigator == null) {
            return this.navigator = new TiledLayerNavigator(this);
        }
		return this.navigator;
	}
	
	public void paint(Graphics2D g) {

		//only paint the tiles inside the clip
		Rectangle rect = g.getClipBounds();
		int tileWidth = super.getTileWidth();
		int tileHeight = super.getTileHeight();
		
		int minRow = rect.y / tileHeight;
		int maxRow = ((rect.y + rect.height) / tileHeight) +1;
		int minCol = rect.x / tileWidth;
		int maxCol = ((rect.x + rect.width) / tileWidth) + 1;
		
		for (int r = minRow; r < this.getRowCount() && r < maxRow; r++) {
			for (int c = minCol; c < this.getColumnCount() && c < maxCol; c++) {
				this.getImageResource().paint(this.grid[r][c], g, c*tileWidth, r*tileHeight,tileWidth, tileHeight, false);
			}
		}
	}
}