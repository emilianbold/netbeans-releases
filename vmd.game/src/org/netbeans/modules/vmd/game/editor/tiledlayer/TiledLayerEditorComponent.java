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

package org.netbeans.modules.vmd.game.editor.tiledlayer;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DebugGraphics;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import org.netbeans.modules.vmd.game.dialog.DuplicateTiledLayerDialog;
import org.netbeans.modules.vmd.game.dialog.NewSimpleTiledLayerDialog;
import org.netbeans.modules.vmd.game.model.AnimatedTile;
import org.netbeans.modules.vmd.game.model.ImageResource;
import org.netbeans.modules.vmd.game.model.Layer;
import org.netbeans.modules.vmd.game.model.Position;
import org.netbeans.modules.vmd.game.model.Tile;
import org.netbeans.modules.vmd.game.model.TileDataFlavor;
import org.netbeans.modules.vmd.game.model.TiledLayer;
import org.netbeans.modules.vmd.game.model.TiledLayerListener;
import org.netbeans.modules.vmd.game.nbdialog.TiledLayerDialog;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;


/**
 *
 * @author kherink
 */
public class TiledLayerEditorComponent extends JComponent implements MouseListener, Scrollable, TiledLayerListener {
	
	public static final boolean DEBUG = false;
	
	public static final byte GRID_MODE_DOTS = 0;
	public static final byte GRID_MODE_LINES = 1;
	public static final byte GRID_MODE_NOGRID = 2;
	
	private static final Color GRID_COLOR = Color.LIGHT_GRAY;
	private static final Color ANIMATED_TILE_GRID_COLOR = Color.CYAN;
	private static final Color HILITE_COLOR = new Color(0, 0, 255, 20);
		
	private static final int CELL_BORDER_WIDTH = 0;
	private static final int SELECTION_BORDER_WIDTH = 2;
	
	
	private byte gridMode = GRID_MODE_LINES;
	private int gridWidth = 1;
	
	private TiledLayer tiledLayer;
	
	private int paintTileIndex = 0;
	private Color currentSelectedColor = Color.getHSBColor(0.0f, 1.0f, 0.0f);
	
	private int cellWidth;
	private int cellHeight;
	
	private Position cellHiLited;
	private Set<Position> cellsSelected = Collections.synchronizedSet(new HashSet<Position>());
		
	private Timer timer;
	
	RulerHorizontal rulerHorizontal;
	RulerVertical rulerVertical;
	
	public static final int EDIT_MODE_PAINT = 0;
	public static final int EDIT_MODE_SELECT = 1;
	public static final String PAINT_CURSOR_NAME = "CUSTOM_PAINT_CURSOR"; // NOI18N
	
	private int editMode;
	
	private static Cursor paintCursor;
	private static Cursor selectionCursor;
	
	static {
		//create custom cursors
		URL cursorUrl = TiledLayerEditorComponent.class.getResource("res/drawing_mode_mouse_16.png"); // NOI18N
		ImageIcon cursorIcon = new ImageIcon(cursorUrl);
		paintCursor = Toolkit.getDefaultToolkit().createCustomCursor(cursorIcon.getImage(), new Point(7, 7), PAINT_CURSOR_NAME);
		
		cursorUrl = TiledLayerEditorComponent.class.getResource("res/select_mode_mouse_16.png"); // NOI18N
		cursorIcon = new ImageIcon(cursorUrl);
		selectionCursor = Cursor.getDefaultCursor();
	}
			
	/** Creates a new instance of EditorComponent */
	public TiledLayerEditorComponent(TiledLayer tiledLayer) {
		this.setTiledLayer(tiledLayer);
		this.tiledLayer.addTiledLayerListener(this);
		this.addMouseListener(this);
		this.addMouseMotionListener(new PaintMotionListener());
		this.setAutoscrolls(true);
		this.timer = new Timer();
		this.timer.schedule(new HiliteAnimator(), 0, 100);
		ToolTipManager.sharedInstance().registerComponent(this);
		//DnD
		DropTarget dropTarget = new DropTarget(this, new TiledLayerDropTargetListener());
		dropTarget.setActive(true);
		this.setDropTarget(dropTarget);
		
		this.rulerHorizontal = new RulerHorizontal();
		this.rulerVertical = new RulerVertical();
		
		
		this.setEditMode(EDIT_MODE_PAINT);
	}
	
	public Dimension getMaximumSize() {
		return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
	}
	
	public void setGridMode(byte gridMode) {
		this.gridMode = gridMode;
		if (this.gridMode == GRID_MODE_NOGRID) {
			this.gridWidth = 0;
		}
		else {
			this.gridWidth = 1;
		}
	}
	
	public String getToolTipText(MouseEvent event) {
		Position cell = this.getCellAtPoint(event.getPoint());
		int index = this.tiledLayer.getTileIndexAt(cell);
		if (index < Tile.EMPTY_TILE_INDEX) {
			AnimatedTile tile = (AnimatedTile) this.tiledLayer.getTileAt(cell);
			return NbBundle.getMessage(TiledLayerEditorComponent.class, 
					"TiledLayerEditorComponent.animTile.tooltip",
					new Object[] {tile.getName(), cell.getRow(), cell.getCol()});
			//return tile.getName() + " [" + cell.getRow() + "," + cell.getCol() + "]";
		}
		return NbBundle.getMessage(TiledLayerEditorComponent.class, 
				"TiledLayerEditorComponent.tile.tooltip", 
				new Object[] {index, cell.getRow(), cell.getCol()});
		//return "Index: " + index + " [" + cell.getRow() + "," + cell.getCol() + "]";
	}
	
	public Dimension getPreferredSize() {
		int width = this.gridWidth + (this.cellWidth + this.gridWidth) * this.tiledLayer.getColumnCount();
		int height = this.gridWidth + (this.cellHeight + this.gridWidth) * this.tiledLayer.getRowCount();
		return new Dimension(width, height);
	}
	
	public void setTiledLayer(TiledLayer tiledLayer) {
		this.tiledLayer = tiledLayer;
		this.cellWidth = this.tiledLayer.getTileWidth() + (CELL_BORDER_WIDTH*2);
		this.cellHeight = this.tiledLayer.getTileHeight() + (CELL_BORDER_WIDTH*2);
	}
	
	public void paintComponent(Graphics g) {
		if (DEBUG) System.out.println("EditorComponent clip : " + g.getClipBounds()); // NOI18N
		if (g instanceof DebugGraphics)
			return;
		Graphics2D g2d = (Graphics2D) g;
		if (gridMode == GRID_MODE_DOTS)
			this.paintGridDots(g2d);
		else
			this.paintGridLines(g2d);
		this.paintCells(g2d);
		
		if (this.cellHiLited != null) {
			this.paintGridHiLite((Graphics2D) g, cellHiLited, Color.ORANGE);
		}
	}
	
	//paints GRID as dots
	private void paintGridDots(Graphics2D g) {
		//Rectangle rect = g.getClipBounds();
		g.setColor(Color.BLACK);
		for (int horizontal = 0; horizontal < this.getHeight(); horizontal += (this.cellHeight + this.gridWidth)) {
			//g.fillRect(0, horizontal, this.getWidth(), this.gridWidth);
			for (int vertical = 0; vertical < this.getWidth(); vertical += (cellWidth + this.gridWidth)) {
				//g.fillRect(vertical, 0, this.gridWidth, this.getHeight());
				g.fillRect(vertical, horizontal, 1, 1);
			}
		}
	}
	
	//paints GRID as lines
	private void paintGridLines(Graphics2D g) {
		//Rectangle rect = g.getClipBounds();
		g.setColor(GRID_COLOR);
		for (int horizontal = 0; horizontal < this.getHeight(); horizontal += (this.cellHeight + this.gridWidth)) {
			//g.drawLine(0, horizontal, this.getWidth(), horizontal);
			g.fillRect(0, horizontal, this.getWidth(), this.gridWidth);
		}
		for (int vertical = 0; vertical < this.getWidth(); vertical += (cellWidth + this.gridWidth)) {
			//g.drawLine(vertical, 0, vertical, this.getHeight());
			g.fillRect(vertical, 0, this.gridWidth, this.getHeight());
		}
	}
	
	/**
	 * Returns four integers [topLeftRow, topLeftColumn, bottomRightRow, bottomRightColumn] these
	 * signify the currently visible grid area.
	 */
	private int[] getVisibleCellBounds() {
		int[] ret = new int[4];
		Rectangle rect = this.getVisibleRect();
		Position topLeft = this.getCellAtPoint(rect.getLocation());
		Position bottomRight = this.getCellAtCoordinates(rect.getLocation().x + rect.width, rect.getLocation().y + rect.height);
		ret[0] = topLeft.getRow();
		ret[1] = topLeft.getCol();
		ret[2] = bottomRight.getRow();
		ret[3] = bottomRight.getCol();
		return ret;
	}
	
	void paintCells(Graphics2D g) {
		Rectangle rect = g.getClipBounds();
		if (DEBUG) System.out.println("Paint cell: " + rect); // NOI18N
		Position topLeft = this.getCellAtPoint(rect.getLocation());
		Position bottomRight = this.getCellAtCoordinates(rect.getLocation().x + rect.width, rect.getLocation().y + rect.height);
		//if (DEBUG) System.out.println("topLeft: " + topLeft + ", bottomRight: " + bottomRight);
		//rows
		for (int row = topLeft.getRow(); row <= bottomRight.getRow(); row++) {
			//cols
			for (int col = topLeft.getCol(); col <= bottomRight.getCol(); col++) {
				Position cell = new Position(row, col);
				//if (DEBUG) System.out.println("Looking at: " + cell + " compared to " + this.cellHiLited);
				this.paintCellContents(g, cell);
				
				//paint selected cells
				if (this.currentSelectedColor != null && (this.cellsSelected.contains(cell))) {
					this.paintCellSelection(g, cell, this.currentSelectedColor);
				}
				
				//hi-lite animated tiles
				if (this.tiledLayer.getTileAt(cell).getIndex() < 0) {
					//if (DEBUG) System.out.println("animated grid");
					this.paintGridHiLite(g, cell, ANIMATED_TILE_GRID_COLOR);
				}
			}
		}
	}
	
	private void paintCellContents(Graphics2D g, Position cell) {
		Tile tile = this.tiledLayer.getTileAt(cell.getRow(), cell.getCol());
		Rectangle rect = this.getCellArea(cell);
		int x = rect.x + CELL_BORDER_WIDTH;
		int y = rect.y + CELL_BORDER_WIDTH;
		tile.paint(g, x, y);
	}
	private void paintCellSelection(Graphics2D g, Position cell, Color color) {
		//if (DEBUG) System.out.println("paintCellSelection: " + cell.toString());
		Rectangle rect = this.getCellArea(cell);
		g.setColor(color);
		g.fillRect(rect.x, rect.y, rect.width, SELECTION_BORDER_WIDTH);
		g.fillRect(rect.x, rect.y, SELECTION_BORDER_WIDTH, rect.height - (this.gridWidth * 2));
		g.fillRect(rect.x + (rect.width - SELECTION_BORDER_WIDTH), rect.y, SELECTION_BORDER_WIDTH, rect.height);
		g.fillRect(rect.x, rect.y + (rect.height - SELECTION_BORDER_WIDTH), rect.width, SELECTION_BORDER_WIDTH);
	}
	private void paintGridHiLite(Graphics2D g, Position cell, Color color) {
		//if (DEBUG) System.out.println("paintGridHiLite: " + cell.toString());
		Rectangle rect = this.getCellArea(cell);
		g.setColor(color);
		g.fillRect(rect.x, rect.y, rect.width, this.gridWidth);
		g.fillRect(rect.x, rect.y, this.gridWidth, rect.height);
		g.fillRect(rect.x + (rect.width - this.gridWidth), rect.y, this.gridWidth, rect.height);
		g.fillRect(rect.x, rect.y + (rect.height - this.gridWidth), rect.width, this.gridWidth);
	}
	
	TiledLayer getTiledLayer() {
		return this.tiledLayer;
	}
	
	private Position getCellAtPoint(Point p) {
		return this.getCellAtCoordinates(p.x, p.y);
	}
	private Position getCellAtCoordinates(int x, int y) {
		int row = (y - this.gridWidth) / (this.cellHeight + this.gridWidth);
		int col = (x - this.gridWidth) / (this.cellWidth + this.gridWidth);
		if (x < 0) {
			col--;
		}
		if (y < 0) {
			row--;
		}
		//if (DEBUG) System.out.println("row = " + row + " col = " + col);
		return new Position(row, col);
	}
	
	private Rectangle getCellArea(Position cell) {
		return this.getCellArea(cell.getRow(), cell.getCol());
	}
	private Rectangle getCellArea(int row, int col) {
		Rectangle cellArea = new Rectangle( ( (this.cellWidth + this.gridWidth) * col) + this.gridWidth, ((this.cellHeight + this.gridWidth) * row) + this.gridWidth, this.cellWidth, this.cellHeight);
		return cellArea;
	}
	
	
	void setPaintTileIndex(int index) {
		this.paintTileIndex = index;
	}
	
	private void selectByIndex(int index) {
		synchronized (this.cellsSelected) {
			for (int r = 0; r < this.tiledLayer.getRowCount(); r++) {
				for (int c = 0; c < this.tiledLayer.getColumnCount(); c++) {
					if (this.tiledLayer.getTileAt(r, c).getIndex() == index) {
						this.cellsSelected.add(new Position(r, c));
					}
				}
			}
		}
	}
	
	private void selectAll() {
		synchronized (this.cellsSelected) {
			for (int r = 0; r < this.tiledLayer.getRowCount(); r++) {
				for (int c = 0; c < this.tiledLayer.getColumnCount(); c++) {
					this.cellsSelected.add(new Position(r, c));
				}
			}
		}
	}
	
	private void invertSelection() {
		synchronized (this.cellsSelected) {
			for (int r = 0; r < this.tiledLayer.getRowCount(); r++) {
				for (int c = 0; c < this.tiledLayer.getColumnCount(); c++) {
					Position p = new Position(r, c);
					if (this.cellsSelected.contains(p)) {
						this.cellsSelected.remove(p);
					} else {
						this.cellsSelected.add(p);
					}
				}
			}
		}
	}
	

	
//----- mouse handling ------
	
	public void setEditMode(int editMode) {
		this.editMode = editMode;
		this.setCursor(editMode == EDIT_MODE_PAINT ? paintCursor : selectionCursor);
	}
	private boolean isPaintMode() {
		return this.editMode == EDIT_MODE_PAINT;
	}
	private boolean isSelectMode() {
		return this.editMode == EDIT_MODE_SELECT;
	}
	
	//MouseListener
	public void mouseClicked(MouseEvent e) {
		this.handleMouseClicked(e);
	}
	
	//MouseListener
	public void mousePressed(MouseEvent e) {
		if (e.isPopupTrigger()) {
			this.doPopUp(e);
		} else {
			this.handleMousePressed(e);
		}
	}
	
	//MouseListener
	public void mouseReleased(MouseEvent e) {
		this.firstDraggedCell = null;
		this.lastDraggedCell = null;
		if (e.isPopupTrigger()) {
			this.doPopUp(e);
		}
	}
	
	//MouseListener
	public void mouseEntered(MouseEvent e) {
	}
	
	//MouseListener
	public void mouseExited(MouseEvent e) {
		Position oldHilited = this.cellHiLited;
		this.cellHiLited = null;
		if (oldHilited != null)
			this.repaint(this.getCellArea(oldHilited));
	}
	
	private Position firstDraggedCell;
	private Position lastDraggedCell;

	private void handleMouseClicked(MouseEvent e) {
		this.firstDraggedCell = null;
		this.lastDraggedCell = null;
		
		Position cell = this.getCellAtCoordinates(e.getX(), e.getY());
		if (SwingUtilities.isRightMouseButton(e)) {
			return;
		}
		
		if (isSelectMode()) {
			if (e.getClickCount() >= 2 && !SwingUtilities.isRightMouseButton(e)) {
				this.selectByIndex(this.tiledLayer.getTileAt(cell.getRow(), cell.getCol()).getIndex());
			}
			this.tiledLayer.getGameDesign().getMainView().requestPreview(this.tiledLayer.getTileAt(cell.getRow(), cell.getCol()));
		}
		else if (isPaintMode()) {
			//do nothing, painting happens on mouse pressed
		}
		this.repaint(getCellArea(cell));
	}
		
	private void handleMousePressed(MouseEvent e) {
		Position cell = this.getCellAtCoordinates(e.getX(), e.getY());
		this.firstDraggedCell = cell;
		this.lastDraggedCell = cell;
		
		if (this.isSelectMode()) {
			synchronized (this.cellsSelected) {
				if (!this.cellsSelected.remove(cell)) {
					this.cellsSelected.add(cell);
				}
			}
		}
		else if (isPaintMode()) {
			this.tiledLayer.setTileAt(this.paintTileIndex, cell.getRow(), cell.getCol());
			Set oldSelected = this.cellsSelected;
			this.cellsSelected = new HashSet<Position>();
			for (Iterator it = oldSelected.iterator(); it.hasNext();) {
				Position oldSelCel = (Position) it.next();
				this.repaint(getCellArea(oldSelCel));
			}
		}
		
		this.repaint(getCellArea(cell));
		this.tiledLayer.getGameDesign().getMainView().requestPreview(this.tiledLayer.getTileAt(cell.getRow(), cell.getCol()));
	}
	
	//MouseMotionListener
	private class PaintMotionListener extends MouseMotionAdapter {
		
		public void mouseDragged(MouseEvent e) {
			Point point = e.getPoint();
			Position cell = TiledLayerEditorComponent.this.getCellAtPoint(point);

			
			if (TiledLayerEditorComponent.this.isSelectMode()) {
				if (cell.equals(lastDraggedCell)) {
					return;
				}
				System.out.println("Drag from " + lastDraggedCell + " to " + cell); // NOI18N
				
				synchronized (TiledLayerEditorComponent.this.cellsSelected) {
					if (e.isControlDown() || e.isMetaDown() || e.isAltDown()) {
						TiledLayerEditorComponent.this.cellsSelected.remove(cell);
					}
					if (false) {
						
						int rowStep = firstDraggedCell.getRow() <= cell.getRow() ? 1 : -1;
						System.out.println("row step : " + rowStep); // NOI18N
						int colStep = firstDraggedCell.getCol() <= cell.getCol() ? 1 : -1;
						System.out.println("col step : " + colStep); // NOI18N
						
						//if drag change in colls
						if (cell.getCol() != lastDraggedCell.getCol()) {
							
							int dcOld = Math.abs(lastDraggedCell.getCol() - firstDraggedCell.getCol());
							int dcNew = Math.abs(cell.getCol() - firstDraggedCell.getCol());
							System.out.println("COL dcOld: " + dcOld + ", dcNew: " + dcNew); // NOI18N
							
							Set<Position> deltaNewSet = new HashSet<Position>();
							for (int c = lastDraggedCell.getCol(); c != cell.getCol(); c+=colStep) {
								System.out.print("c = " + c);
								for (int r = firstDraggedCell.getRow(); r != cell.getRow(); r+=rowStep) {								
									System.out.println(" r = " + r);
									Position pos = new Position(r, c);
									System.out.println("\tadd to delta " + pos); // NOI18N
									deltaNewSet.add(pos);
								}
							}
							
							//if shrinking selection - remove the deltaSet
							if (dcOld > dcNew) {
								System.out.println("remove delta"); // NOI18N
								cellsSelected.removeAll(deltaNewSet);
							}
							//else growing selection - add the deltaSet
							else {
								System.out.println("add delta"); // NOI18N
								cellsSelected.addAll(deltaNewSet);
							}
						}
						
						//change in rows
						if (cell.getRow() != lastDraggedCell.getRow()) {
							int dcOld = Math.abs(lastDraggedCell.getRow() - firstDraggedCell.getRow());
							int dcNew = Math.abs(cell.getRow() - firstDraggedCell.getRow());
							System.out.println("ROW dcOld: " + dcOld + ", dcNew: " + dcNew); // NOI18N
							
							Set<Position> deltaSet = new HashSet<Position>();
							for (int r = lastDraggedCell.getRow(); r != cell.getRow(); r-=rowStep) {
								for (int c = lastDraggedCell.getCol(); c != cell.getCol(); c-=colStep) {
									Position pos = new Position(r, c);
									System.out.println("\tadd to delta " + pos); // NOI18N
									deltaSet.add(pos);
								}
							}
							
							//if shrinking selection - remove the deltaSet
							if (dcOld > dcNew) {
								System.out.println("remove delta"); // NOI18N
								cellsSelected.removeAll(deltaSet);
							}
							//else growing selection - add the deltaSet
							else {
								System.out.println("add delta"); // NOI18N
								cellsSelected.addAll(deltaSet);
							}
						}
						
						TiledLayerEditorComponent.this.lastDraggedCell = cell;
					}
					else {
						TiledLayerEditorComponent.this.cellsSelected.add(cell);
					}
				}
			}
			else if (TiledLayerEditorComponent.this.isPaintMode()) {
				int tileIndex = TiledLayerEditorComponent.this.paintTileIndex;
				//if we are on the same tile and trying to paint the same index then we aren't really changing anything :)
				if (cell.equals(lastDraggedCell) && (tileIndex == TiledLayerEditorComponent.this.tiledLayer.getTileAt(cell.getRow(), cell.getCol()).getIndex()))
					return;
				if (lastDraggedCell != null)
					TiledLayerEditorComponent.this.repaint(TiledLayerEditorComponent.this.getCellArea(lastDraggedCell));
				TiledLayerEditorComponent.this.cellHiLited = cell;
				//if (DEBUG) System.out.println("tile index = " + tileIndex);
				TiledLayerEditorComponent.this.tiledLayer.setTileAt(tileIndex, cell.getRow(), cell.getCol());
			}
			
			
			TiledLayerEditorComponent.this.repaint(TiledLayerEditorComponent.this.getCellArea(cell));
			TiledLayerEditorComponent.this.lastDraggedCell = cell;
			//The user is dragging us, so scroll!
			Rectangle r = new Rectangle(e.getX(), e.getY(), 1, 1);
			scrollRectToVisible(r);
		}
		
		public void mouseMoved(MouseEvent e) {
			TiledLayerEditorComponent.this.updateHiLite(e.getPoint());
		}
	}
		
	private void doPopUp(MouseEvent e) {
		Position position = this.getCellAtPoint(e.getPoint());
		
// 		CreateTiledLayerAction ctl = new CreateTiledLayerAction();
		DuplicateTiledLayerAction dtl = new DuplicateTiledLayerAction();
		CreateFromSelectionAction cfs = new CreateFromSelectionAction();
		EraseSelectionAction es = new EraseSelectionAction();
		if (TiledLayerEditorComponent.this.cellsSelected.size() < 2) {
			cfs.setEnabled(false);
		}
		
		SelectRowAction sr = new SelectRowAction();
		sr.putValue(SelectRowAction.PROP_POSITION, position);
		
		SelectColumnAction sc = new SelectColumnAction();
		sc.putValue(SelectColumnAction.PROP_POSITION, position);
		
		SelectByIndexAction sbi = new SelectByIndexAction();
		sbi.putValue(SelectByIndexAction.PROP_POSITION, position);
		
		SelectAllAction sa = new SelectAllAction();
		
		InvertSelectionAction is = new InvertSelectionAction();
		
		PrependRowAction pr = new PrependRowAction();
		pr.putValue(PrependRowAction.PROP_POSITION, position);
		
		AppendRowAction ar = new AppendRowAction();
		ar.putValue(AppendRowAction.PROP_POSITION, position);
		
		PrependColumnAction pc = new PrependColumnAction();
		pc.putValue(PrependColumnAction.PROP_POSITION, position);
		
		AppendColumnAction ac = new AppendColumnAction();
		ac.putValue(AppendColumnAction.PROP_POSITION, position);
		
		DeleteRowAction dr = new DeleteRowAction();
		dr.putValue(DeleteRowAction.PROP_POSITION, position);
		
		DeleteColumnAction dc = new DeleteColumnAction();
		dc.putValue(DeleteColumnAction.PROP_POSITION, position);
		
		TrimToSizeAction tts = new TrimToSizeAction();
		
		JCheckBoxMenuItem itemPaint = new JCheckBoxMenuItem(
				NbBundle.getMessage(TiledLayerEditorComponent.class, "TiledLayerEditorComponent.menuModePaint.txt"), 
				this.editMode == EDIT_MODE_PAINT);
		itemPaint.addItemListener(new EditModeListener(EDIT_MODE_PAINT));

		JCheckBoxMenuItem itemSelect = new JCheckBoxMenuItem(
				NbBundle.getMessage(TiledLayerEditorComponent.class, "TiledLayerEditorComponent.menuModeSelect.txt"), 
				this.editMode == EDIT_MODE_SELECT);
		itemSelect.addItemListener(new EditModeListener(EDIT_MODE_SELECT));
		
		JPopupMenu menu = new JPopupMenu();
		
		menu.add(itemPaint);
		menu.add(itemSelect);
		menu.addSeparator();
//		menu.add(ctl);
		menu.add(dtl);
		List<Action> actions = this.tiledLayer.getActions();
		for (Action action : actions) {
			//don't wanna add edit action since we are already editing :)
			if (action instanceof Layer.EditLayerAction)
				continue;
			menu.add(action);
		}
		menu.addSeparator();
		menu.add(cfs);
		menu.add(es);
		JMenu selecttionSubMenu = new JMenu(NbBundle.getMessage(TiledLayerEditorComponent.class, "TiledLayerEditorComponent.menuSelect.txt"));
		selecttionSubMenu.add(selecttionSubMenu);
		selecttionSubMenu.add(sr);
		selecttionSubMenu.add(sc);
		selecttionSubMenu.add(sbi);
		selecttionSubMenu.add(sa);
		selecttionSubMenu.add(is);
		menu.add(selecttionSubMenu);
		menu.addSeparator();
		menu.add(pr);
		menu.add(ar);
		menu.add(pc);
		menu.add(ac);
		menu.addSeparator();
		menu.add(dr);
		menu.add(dc);
		menu.addSeparator();
		menu.add(tts);
		
		menu.show(this, e.getX(), e.getY());
	}
	
	private class EditModeListener implements ItemListener {
		private int mode;
		public EditModeListener(int mode) {
			this.mode = mode;
		}
        public void itemStateChanged(ItemEvent e) {
			System.out.println("setting edit mode"); // NOI18N
			TiledLayerEditorComponent.this.setEditMode(this.mode);
        }
	}

    
//	public class CreateTiledLayerAction extends AbstractAction {
//		{
//			this.putValue(NAME, "New Tiled Layer");
//		}
//		
//		public void actionPerformed(ActionEvent e) {
//			TiledLayerDialog nld = new TiledLayerDialog(TiledLayerEditorComponent.this.tiledLayer.getGameDesign());
//			DialogDescriptor dd = new DialogDescriptor(nld, "Create new TiledLayer");
//			dd.setButtonListener(nld);
//			dd.setValid(false);
//			nld.setDialogDescriptor(dd);
//			Dialog d = DialogDisplayer.getDefault().createDialog(dd);
//			d.setVisible(true);
//		}
//	}
	
	public class DuplicateTiledLayerAction extends AbstractAction {
		{
			this.putValue(NAME, NbBundle.getMessage(TiledLayerEditorComponent.class, "TiledLayerEditorComponent.actionDuplicateTiledLayer.txt"));
		}
		
		public void actionPerformed(ActionEvent e) {
			DuplicateTiledLayerDialog dialog = new DuplicateTiledLayerDialog(TiledLayerEditorComponent.this.tiledLayer);
			DialogDescriptor dd = new DialogDescriptor(dialog, 
					NbBundle.getMessage(TiledLayerEditorComponent.class, "TiledLayerEditorComponent.actionDuplicateTiledLayer.txt"));
			dd.setButtonListener(dialog);
			dd.setValid(false);
			dialog.setDialogDescriptor(dd);
			Dialog d = DialogDisplayer.getDefault().createDialog(dd);
			d.setVisible(true);
		}
	}
	
	public class TrimToSizeAction extends AbstractAction {
		{
			this.putValue(NAME, NbBundle.getMessage(TiledLayerEditorComponent.class, "TiledLayerEditorComponent.actionTrim.txt"));
		}
		
		public void actionPerformed(ActionEvent e) {
			TiledLayerEditorComponent.this.tiledLayer.trimToSize();
		}
	}
	
	public class CreateFromSelectionAction extends AbstractAction {
		{
			this.putValue(NAME, NbBundle.getMessage(TiledLayerEditorComponent.class, "TiledLayerEditorComponent.actionNewFromSelection.txt"));
		}
		
		public void actionPerformed(ActionEvent e) {
			int left = Integer.MAX_VALUE;
			int right = Integer.MIN_VALUE;
			int top = Integer.MAX_VALUE;
			int bottom = Integer.MIN_VALUE;
			int[][] grid;
			synchronized (TiledLayerEditorComponent.this.cellsSelected) {
				if (TiledLayerEditorComponent.this.cellsSelected.size() < 2)
					return;
				Set <Position> cells = TiledLayerEditorComponent.this.cellsSelected;
				//first find the grid size that can hold all the selected cells
				for (Position position : cells) {
					//find left boundry
					left = Math.min(left, position.getCol());
					//find right boundry
					right = Math.max(right, position.getCol());
					//find top boundry
					top = Math.min(top, position.getRow());
					//find bottom boundry
					bottom = Math.max(bottom, position.getRow());
				}
				//then fill the grid with selected cells
				grid = new int[(bottom-top)+1] [(right-left)+1];
				for (Position position : cells) {
					grid[position.getRow()-top] [position.getCol()-left] = TiledLayerEditorComponent.this.tiledLayer.getTileIndexAt(position);
				}
			}
			
			NewSimpleTiledLayerDialog dialog = new NewSimpleTiledLayerDialog(TiledLayerEditorComponent.this.tiledLayer.getImageResource(), 
					grid,
					TiledLayerEditorComponent.this.tiledLayer.getTileWidth(),
					TiledLayerEditorComponent.this.tiledLayer.getTileHeight()
					);
			DialogDescriptor dd = new DialogDescriptor(dialog, NbBundle.getMessage(TiledLayerEditorComponent.class, "TiledLayerEditorComponent.actionNewFromSelection.txt"));
			dd.setButtonListener(dialog);
			dd.setValid(false);
			dialog.setDialogDescriptor(dd);
			Dialog d = DialogDisplayer.getDefault().createDialog(dd);
			d.setVisible(true);
		}
	}
	
	public class EraseSelectionAction extends AbstractAction {
		{
			this.putValue(NAME, NbBundle.getMessage(TiledLayerEditorComponent.class, "TiledLayerEditorComponent.actionEraseSelection.txt"));
		}
		
		public void actionPerformed(ActionEvent e) {
			TiledLayerEditorComponent.this.tiledLayer.setTileAtPositions(Tile.EMPTY_TILE_INDEX, TiledLayerEditorComponent.this.cellsSelected);
		}
	}
	
	public class DeleteColumnAction extends AbstractAction {
		public static final String PROP_POSITION = "PROP_POSITION"; // NOI18N
		{
			this.putValue(NAME, NbBundle.getMessage(TiledLayerEditorComponent.class, "TiledLayerEditorComponent.actionDeleteCol.txt"));
		}
		
		public void actionPerformed(ActionEvent e) {
			Position p = ((Position) this.getValue(PROP_POSITION));
			TiledLayerEditorComponent.this.tiledLayer.deleteColumns(p.getCol(), 1);
		}
	}
	public class DeleteRowAction extends AbstractAction {
		public static final String PROP_POSITION = "PROP_POSITION"; // NOI18N
		{
			this.putValue(NAME, NbBundle.getMessage(TiledLayerEditorComponent.class, "TiledLayerEditorComponent.actionDeleteRow.txt"));
		}
		
		public void actionPerformed(ActionEvent e) {
			Position p = ((Position) this.getValue(PROP_POSITION));
			TiledLayerEditorComponent.this.tiledLayer.deleteRows(p.getRow(), 1);
		}
	}
	public class SelectColumnAction extends AbstractAction {
		public static final String PROP_POSITION = "PROP_POSITION"; // NOI18N
		{
			this.putValue(NAME, NbBundle.getMessage(TiledLayerEditorComponent.class, "TiledLayerEditorComponent.actionSelectCol.txt"));
		}
		
		public void actionPerformed(ActionEvent e) {
			Position p = ((Position) this.getValue(PROP_POSITION));
			TiledLayerEditorComponent.this.setColumnSelection(p.getCol(), true);
		}
	}
	public class SelectRowAction extends AbstractAction {
		public static final String PROP_POSITION = "PROP_POSITION"; // NOI18N
		{
			this.putValue(NAME, NbBundle.getMessage(TiledLayerEditorComponent.class, "TiledLayerEditorComponent.actionSelectRow.txt"));
		}
		
		public void actionPerformed(ActionEvent e) {
			Position p = ((Position) this.getValue(PROP_POSITION));
			TiledLayerEditorComponent.this.setRowSelection(p.getRow(), true);
		}
	}
	public class SelectByIndexAction extends AbstractAction {
		public static final String PROP_POSITION = "PROP_POSITION"; // NOI18N
		{
			this.putValue(NAME, NbBundle.getMessage(TiledLayerEditorComponent.class, "TiledLayerEditorComponent.actionSelectIndex.txt"));
		}
		
		public void actionPerformed(ActionEvent e) {
			Position p = ((Position) this.getValue(PROP_POSITION));
			TiledLayerEditorComponent.this.selectByIndex(TiledLayerEditorComponent.this.tiledLayer.getTileIndexAt(p));
		}
	}
	public class InvertSelectionAction extends AbstractAction {
		{
			this.putValue(NAME, NbBundle.getMessage(TiledLayerEditorComponent.class, "TiledLayerEditorComponent.actionInvertSelection.txt"));
		}
		
		public void actionPerformed(ActionEvent e) {
			TiledLayerEditorComponent.this.invertSelection();
		}
	}
	public class SelectAllAction extends AbstractAction {
		{
			this.putValue(NAME, NbBundle.getMessage(TiledLayerEditorComponent.class, "TiledLayerEditorComponent.actionSelectAll.txt"));
		}
		
		public void actionPerformed(ActionEvent e) {
			TiledLayerEditorComponent.this.selectAll();
		}
	}
	
	public class PrependRowAction extends AbstractAction {
		public static final String PROP_POSITION = "PROP_POSITION"; // NOI18N
		{
			this.putValue(NAME, NbBundle.getMessage(TiledLayerEditorComponent.class, "TiledLayerEditorComponent.actionPrependRow.txt"));
		}
		
		public void actionPerformed(ActionEvent e) {
			Position p = ((Position) this.getValue(PROP_POSITION));
			TiledLayerEditorComponent.this.tiledLayer.insertRows(p.getRow(), 1);
		}
	}
	public class AppendRowAction extends AbstractAction {
		public static final String PROP_POSITION = "PROP_POSITION"; // NOI18N
		{
			this.putValue(NAME, NbBundle.getMessage(TiledLayerEditorComponent.class, "TiledLayerEditorComponent.actionAppendRow.txt"));
		}
		
		public void actionPerformed(ActionEvent e) {
			Position p = ((Position) this.getValue(PROP_POSITION));
			TiledLayerEditorComponent.this.tiledLayer.insertRows(p.getRow() + 1, 1);
		}
	}
	public class PrependColumnAction extends AbstractAction {
		public static final String PROP_POSITION = "PROP_POSITION"; // NOI18N
		{
			this.putValue(NAME, NbBundle.getMessage(TiledLayerEditorComponent.class, "TiledLayerEditorComponent.actionPrependCol.txt"));
		}
		
		public void actionPerformed(ActionEvent e) {
			Position p = ((Position) this.getValue(PROP_POSITION));
			TiledLayerEditorComponent.this.tiledLayer.insertColumns(p.getCol(), 1);
		}
	}
	public class AppendColumnAction extends AbstractAction {
		public static final String PROP_POSITION = "PROP_POSITION"; // NOI18N
		{
			this.putValue(NAME, NbBundle.getMessage(TiledLayerEditorComponent.class, "TiledLayerEditorComponent.actionAppendCol.txt"));
		}
		
		public void actionPerformed(ActionEvent e) {
			Position p = ((Position) this.getValue(PROP_POSITION));
			TiledLayerEditorComponent.this.tiledLayer.insertColumns(p.getCol() + 1, 1);
		}
	}
	
	private void setRowSelection(int row, boolean selected) {
		synchronized (this.cellsSelected) {
			if (selected) {
				int cols = this.tiledLayer.getColumnCount();
				for (int i = 0; i < cols; i++) {
					this.cellsSelected.add(new Position(row, i));
				}
			}
			else {
				for (Iterator<Position> it = cellsSelected.iterator(); it.hasNext();) {
					Position position = it.next();
					if (position.getRow() == row) {
						it.remove();
					}
				}
			}
		}
		//TODO : repaint the row only
		this.repaint();
	}
	
	private void setColumnSelection(int col, boolean selected) {
		synchronized(this.cellsSelected) {
			if (selected) {
				int rows = this.tiledLayer.getRowCount();
				for (int i = 0; i < rows; i++) {
					this.cellsSelected.add(new Position(i, col));
				}
			}
			else {

				for (Iterator<Position> it = cellsSelected.iterator(); it.hasNext();) {
					Position position = it.next();
					if (position.getCol() == col) {
						it.remove();
					}
				}

			}
		}
		//TODO : repaint the column only
		this.repaint();
	}
	
	//Scrollable
	public Dimension getPreferredScrollableViewportSize() {
		return this.getPreferredSize();
	}
	
	public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
		if (orientation == SwingConstants.HORIZONTAL) {
			return this.tiledLayer.getTileWidth();
		}
		return this.tiledLayer.getTileHeight();
	}
	
	public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
		if (orientation == SwingConstants.HORIZONTAL) {
			return this.tiledLayer.getTileWidth();
		}
		return this.tiledLayer.getTileHeight();
	}
	
	public boolean getScrollableTracksViewportWidth() {
		return false;
	}
	public boolean getScrollableTracksViewportHeight() {
		return false;
	}
	
	private class HiliteAnimator extends TimerTask {
		private Color[] colors = {
			new Color(255, 255, 255),
			new Color(200, 200, 200),
			new Color(155, 155, 155),
			new Color(100, 100, 100),
			new Color(55, 55, 55),
			new Color(0, 0, 0),
			new Color(55, 55, 55),
			new Color(100, 100, 100),
			new Color(155, 155, 155),
			new Color(200, 200, 200),
			new Color(255, 255, 255),
        };
		private int i = 0;
		
		public void run() {
			synchronized (TiledLayerEditorComponent.this.cellsSelected) {
				if (++i >= colors.length) {
					i = 0;
				}
				TiledLayerEditorComponent.this.currentSelectedColor = colors[i];
				int[] bounds = TiledLayerEditorComponent.this.getVisibleCellBounds();
				
				for (int r = bounds[0]; r <= bounds[2]; r++) {
					for (int c = bounds[1]; c <= bounds[3]; c++) {
						Position cell = new Position(r, c);
						if (false) System.out.println("looking at: " + cell); // NOI18N
						if (TiledLayerEditorComponent.this.cellsSelected.contains(cell)) {
							if (false) System.out.println("selected cell repaint: " + cell); // NOI18N
							Rectangle rect = TiledLayerEditorComponent.this.getCellArea(cell);
                            
							TiledLayerEditorComponent.this.repaint(rect.x, rect.y, rect.width, SELECTION_BORDER_WIDTH);
							TiledLayerEditorComponent.this.repaint(rect.x, rect.y, SELECTION_BORDER_WIDTH, rect.height - (TiledLayerEditorComponent.this.gridWidth * 2));
							TiledLayerEditorComponent.this.repaint(rect.x + (rect.width - SELECTION_BORDER_WIDTH), rect.y, SELECTION_BORDER_WIDTH, rect.height);
							TiledLayerEditorComponent.this.repaint(rect.x, rect.y + (rect.height - SELECTION_BORDER_WIDTH), rect.width, SELECTION_BORDER_WIDTH);
						}
					}
				}
			}
		}
	}
	
	private boolean isCellVisible(Position cell) {
		return this.getVisibleRect().intersects(this.getCellArea(cell));
	}
	
	private void updateHiLite(Point point) {
		Position oldHilited = this.cellHiLited;
		Position cell = this.getCellAtPoint(point);
		if (cell == null)
			return;
		//if (DEBUG) System.out.println("dragOver " + cell);
		if (!cell.equals(oldHilited)) {
			this.cellHiLited = cell;
			if (oldHilited != null) {
				this.repaint(this.getCellArea(oldHilited));
			}
			this.repaint(this.getCellArea(this.cellHiLited));
		}
	}
	
	
	//DnD implementation
	private class TiledLayerDropTargetListener implements DropTargetListener {
		public void dragEnter(DropTargetDragEvent dtde) {
			if (DEBUG) System.out.println("dragEnter"); // NOI18N
		}
		public void dragOver(DropTargetDragEvent dtde) {
			TiledLayerEditorComponent.this.updateHiLite(dtde.getLocation());
		}
		public void dropActionChanged(DropTargetDragEvent dtde) {
			if (DEBUG) System.out.println("dropActionChanged"); // NOI18N
		}
		public void dragExit(DropTargetEvent dte) {
			if (DEBUG) System.out.println("dragExit"); // NOI18N
		}
		public void drop(DropTargetDropEvent dtde) {
			Point dropPoint = dtde.getLocation();
			if (DEBUG) System.out.println("Start drop @: " + dropPoint); // NOI18N
			Transferable transferable = dtde.getTransferable();
			try {
				TileDataFlavor tileFlavor = new TileDataFlavor();
				if (transferable.isDataFlavorSupported(tileFlavor)) {
					dtde.acceptDrop(DnDConstants.ACTION_COPY);
					List<Tile> tiles = (List<Tile>) transferable.getTransferData(tileFlavor);
					assert (tiles.size() > 0);
					ImageResource imgRes = TiledLayerEditorComponent.this.tiledLayer.getImageResource();
					Tile newTile = imgRes.getTile(tiles.get(0).getIndex(),
							TiledLayerEditorComponent.this.tiledLayer.getTileWidth(),
							TiledLayerEditorComponent.this.tiledLayer.getTileHeight(),
							false
							);
					Position cell = TiledLayerEditorComponent.this.getCellAtPoint(dropPoint);
					if (TiledLayerEditorComponent.this.cellsSelected.contains(cell)) {
						TiledLayerEditorComponent.this.tiledLayer.setTileAtPositions(newTile.getIndex(), TiledLayerEditorComponent.this.cellsSelected);
					} else {
						TiledLayerEditorComponent.this.tiledLayer.setTileAt(newTile.getIndex(), cell.getRow(), cell.getCol());
					}
					dtde.dropComplete(true);
				} else {
					if (DEBUG) System.out.println("NOT a Tile :("); // NOI18N
					dtde.dropComplete(false);
				}
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
				dtde.dropComplete(false);
			} catch (UnsupportedFlavorException e) {
				e.printStackTrace();
				dtde.dropComplete(false);
			} catch (IOException e) {
				e.printStackTrace();
				dtde.dropComplete(false);
			}
		}
	}
	
	//TiledLayerListener
	public void tileChanged(TiledLayer tiledLayer, int row, int col) {
		this.repaint(this.getCellArea(row, col));
	}
	
	public void tilesChanged(TiledLayer tiledLayer, Set positions) {
		this.revalidate();
		this.repaint();
	}
	
	public void columnsInserted(TiledLayer tiledLayer, int index, int count) {
		this.shiftSelectedCellColumns(index, count);
		this.revalidate();
		this.repaint();
		this.rulerHorizontal.repaint();
	}
	
	public void columnsRemoved(TiledLayer tiledLayer, int index, int count) {
		this.shiftSelectedCellColumns(index, -count);
		this.revalidate();
		this.repaint();
		this.rulerHorizontal.repaint();
	}
	
	public void rowsInserted(TiledLayer tiledLayer, int index, int count) {
		this.shiftSelectedCellRows(index, count);
		this.revalidate();
		this.repaint();
		this.rulerVertical.repaint();
	}
	
	public void rowsRemoved(TiledLayer tiledLayer, int index, int count) {
		this.shiftSelectedCellRows(index, -count);
		this.revalidate();
		this.repaint();
		this.rulerVertical.repaint();
	}
	
	private void shiftSelectedCellRows(int index, int count) {
				
		synchronized (this.cellsSelected) {
			List<Position> bucket = new ArrayList<Position>();
			for (Iterator<Position> it = cellsSelected.iterator(); it.hasNext();) {
				Position position = it.next();
				int curRow = position.getRow();
				if (curRow >= index) {
					it.remove();
					//System.out.println("Shifting row " + curRow + " to " + (curRow + count));
					bucket.add(new Position(curRow + count, position.getCol()));
				}
			}

			for (Position position : bucket) {
				this.cellsSelected.add(position);
			}
		}
	}
	
	private void shiftSelectedCellColumns(int index, int count) {
		
		synchronized (this.cellsSelected) {
			List<Position> bucket = new ArrayList<Position>();
			for (Iterator<Position> it = cellsSelected.iterator(); it.hasNext();) {
				Position position = it.next();
				int curCol = position.getCol();
				if (curCol >= index) {
					it.remove();
					//System.out.println("Shifting col " + curCol + " to " + (curCol + count));
					bucket.add(new Position(position.getRow(), curCol + count));
				}
			}

			for (Position position : bucket) {
				this.cellsSelected.add(position);
			}
		}
	}

	private GridButton gridButton = new GridButton();
	
	public JComponent getGridButton() {
		return this.gridButton;
	}
	
	private class GridButton extends JComponent implements MouseListener {
		private static final int BORDER = 2;
		public GridButton() {
			ToolTipManager.sharedInstance().registerComponent(this);
			this.addMouseListener(this);
		}
		
		@Override
		public String getToolTipText() {
			return NbBundle.getMessage(TiledLayerEditorComponent.class, "TiledLayerEditorComponent.GridButton.tooltip");
		}
		
		@Override
		protected void paintComponent(Graphics g) {
			g.setColor(new Color(230, 230, 255));
			g.fillRect(0, 0, this.getWidth(), this.getHeight());
			g.setColor(Color.BLACK);
			int startx = BORDER;
			int starty = BORDER;
			int x = startx;
			int y = starty;
			int w = this.getWidth() - (2*BORDER);
			int h = this.getHeight() - (2*BORDER);
			if (TiledLayerEditorComponent.this.gridWidth > 0) {
				if (TiledLayerEditorComponent.this.gridMode == GRID_MODE_DOTS) {
					g.setColor(Color.GRAY);
				}
				int offX = w / 2;
				int offY = h / 2;
				
				x += offX;
				g.drawLine(x, starty, x, starty + h);
				
				y += offY;
				g.drawLine(startx, y, startx + w, y);
				
			}
			g.setColor(Color.BLACK);
			g.drawRoundRect(startx, starty, w, h, BORDER, BORDER);
		}

		public void mouseClicked(MouseEvent e) {
			byte mode = TiledLayerEditorComponent.this.gridMode;
			if (mode == GRID_MODE_LINES) {
				if (TiledLayerEditorComponent.this.gridWidth == 0) {
					TiledLayerEditorComponent.this.setGridMode(GRID_MODE_DOTS);
				}
				else {
					TiledLayerEditorComponent.this.gridWidth = 0;
				}
			}
			else {
				TiledLayerEditorComponent.this.setGridMode(GRID_MODE_LINES);
				TiledLayerEditorComponent.this.gridWidth = 1;
			}
			this.repaint();
			TiledLayerEditorComponent.this.rulerHorizontal.repaint();
			TiledLayerEditorComponent.this.rulerVertical.repaint();
			TiledLayerEditorComponent.this.repaint();
		}

		public void mouseEntered(MouseEvent e) {
		}

		public void mouseExited(MouseEvent e) {
		}

		public void mousePressed(MouseEvent e) {
		}

		public void mouseReleased(MouseEvent e) {
		}
	}
	
	class RulerVertical extends JComponent implements MouseListener, MouseMotionListener {
		private static final int SIZE = 14;
		private static final boolean DEBUG = false;
		
		private Set<Integer> pressed = new HashSet<Integer>();
		
		private int hilitedRowHeader = -1;
		
		public RulerVertical() {
			ToolTipManager.sharedInstance().registerComponent(this);
			this.addMouseListener(this);
			this.addMouseMotionListener(this);
		}
		
		public String getToolTipText(MouseEvent event) {
			return NbBundle.getMessage(TiledLayerEditorComponent.class, 
					"TiledLayerEditorComponent.verticalRuler.tooltip", 
					this.getRowAtPoint(event.getPoint()));
			//return "Row: " + this.getRowAtPoint(event.getPoint());
		}
		
		public Dimension getPreferredSize() {
			Dimension size = TiledLayerEditorComponent.this.getPreferredSize();
			size.width = SIZE;
			return size;
		}
		
		protected void paintComponent(Graphics graphincs) {
			Graphics2D g = (Graphics2D) graphincs;
			
			Rectangle rect = g.getClipBounds();
			g.setColor(Color.WHITE);
			g.fill(rect);
			if (DEBUG) System.out.println("RulerVertical.repaint " + rect); // NOI18N
			
			int unit = TiledLayerEditorComponent.this.gridWidth + TiledLayerEditorComponent.this.cellHeight;
			
			for (int y = (rect.y / unit) * unit; y <= rect.y + rect.height; y+= unit) {
				int row = y/unit;
								
				//only paint header cells for existing rows
				if (row >= TiledLayerEditorComponent.this.tiledLayer.getRowCount())
					break;
				
				boolean raised = true;
				g.setColor(new Color(240, 238, 230));
				
				if (this.pressed.contains(row)) {
					g.setColor(new Color(200, 200, 200));
					raised = false;
				}
				
				
				g.fill3DRect(0, y + TiledLayerEditorComponent.this.gridWidth/2, SIZE, unit, raised);
				
				if (row == this.hilitedRowHeader) {
					g.setColor(HILITE_COLOR);
					g.fill3DRect(0, y + TiledLayerEditorComponent.this.gridWidth/2, SIZE, unit, raised);
				}
			}
		}
		
		private int getRowAtPoint(Point point) {
			return this.getRowAtCoordinates(point.x, point.y);
		}
		private int getRowAtCoordinates(int x, int y) {
			return (y - TiledLayerEditorComponent.this.gridWidth) / (TiledLayerEditorComponent.this.cellHeight + TiledLayerEditorComponent.this.gridWidth);
		}
		
		private Rectangle getRowHeaderArea(int row) {
			Rectangle area = new Rectangle( 
					0,
					((TiledLayerEditorComponent.this.cellHeight + TiledLayerEditorComponent.this.gridWidth) * row) + TiledLayerEditorComponent.this.gridWidth/2, 
					SIZE,
					TiledLayerEditorComponent.this.cellHeight + TiledLayerEditorComponent.this.gridWidth
					);
			return area;
		}
				
		private void hiliteRowHeader(int row) {
			if (this.hilitedRowHeader == row) {
				return;
			}
			int oldHilited = this.hilitedRowHeader;
			this.hilitedRowHeader = row;
			this.repaint(this.getRowHeaderArea(row));
			this.repaint(this.getRowHeaderArea(oldHilited));
		}
		
		/**
		 * Toggle the selection of the column header and the column cell selection.
		 */
		public void mouseClicked(MouseEvent e) {
			int row = this.getRowAtPoint(e.getPoint());
			TiledLayerEditorComponent.this.setRowSelection(row, !e.isControlDown());
			this.repaint(this.getRowHeaderArea(row));
		}
		public void mousePressed(MouseEvent e) {
			int row = this.getRowAtPoint(e.getPoint());
			if (e.isPopupTrigger()) {
				this.handlePopUp(e);
			}
			else {
				this.pressed.add(row);
				this.repaint(this.getRowHeaderArea(row));
			}
		}
		public void mouseReleased(MouseEvent e) {
			if (e.isPopupTrigger()) {
				this.handlePopUp(e);
			}
			else {
				this.pressed.clear();
				this.repaint();
			}
		}
		
		public void mouseEntered(MouseEvent e) {
		}
		public void mouseExited(MouseEvent e) {
			this.hiliteRowHeader(-1);
		}
		
		public void mouseDragged(MouseEvent e) {
		}
		public void mouseMoved(MouseEvent e) {
			int row = this.getRowAtPoint(e.getPoint());
			this.hiliteRowHeader(row);
		}

		private void handlePopUp(MouseEvent e) {
			int row = this.getRowAtPoint(e.getPoint());
			if (DEBUG) System.out.println("Popup row: " + row); // NOI18N
			JPopupMenu menu = this.createRulerPopupMenu(row);
			menu.show(this, e.getX(), e.getY());
		}
		private JPopupMenu createRulerPopupMenu(int row) {
			JPopupMenu menu = new JPopupMenu();
			for (Iterator iter = this.getActions().iterator(); iter.hasNext();) {
				Action action = (Action) iter.next();
				action.putValue("ROW", new Integer(row)); // NOI18N
				menu.add(action);
			}
			return menu;
		}
		public List<Action> getActions() {
			List<Action> actions = new ArrayList<Action>();
			actions.add(new DeleteRowAction());
			actions.add(new PrependRowAction());
			actions.add(new AppendRowAction());
			return Collections.unmodifiableList(actions);
		}
		
		private class DeleteRowAction extends AbstractAction {
			{
				this.putValue(NAME, NbBundle.getMessage(
						TiledLayerEditorComponent.class, 
						"TiledLayerEditorComponent.verticalRuler.actionDeleteRow.txt"));
			}
			
			public void actionPerformed(ActionEvent e) {
				int row = ((Integer) this.getValue("ROW")).intValue(); // NOI18N
				TiledLayerEditorComponent.this.tiledLayer.deleteRows(row, 1);
			}
		}
		private class PrependRowAction extends AbstractAction {
			{
				this.putValue(NAME, NbBundle.getMessage(
						TiledLayerEditorComponent.class, 
						"TiledLayerEditorComponent.verticalRuler.actionPrependRow.txt"));
			}
			
			public void actionPerformed(ActionEvent e) {
				int row = ((Integer) this.getValue("ROW")).intValue(); // NOI18N
				TiledLayerEditorComponent.this.tiledLayer.insertRows(row, 1);
			}
		}
		private class AppendRowAction extends AbstractAction {
			{
				this.putValue(NAME, NbBundle.getMessage(
						TiledLayerEditorComponent.class, 
						"TiledLayerEditorComponent.verticalRuler.actionAppendRow.txt"));
			}
			
			public void actionPerformed(ActionEvent e) {
				int row = ((Integer) this.getValue("ROW")).intValue(); // NOI18N
				TiledLayerEditorComponent.this.tiledLayer.insertRows(row +1, 1);
			}
		}
	}
	
	class RulerHorizontal  extends JComponent implements MouseListener, MouseMotionListener {
		private static final int SIZE = 14;
		private static final boolean DEBUG = false;
		
		private Set<Integer> pressed = new HashSet<Integer>();

		private int hilitedColumnHeader = -1;
		
		public RulerHorizontal() {
			ToolTipManager.sharedInstance().registerComponent(this);
			this.addMouseListener(this);
			this.addMouseMotionListener(this);
		}
		
		public String getToolTipText(MouseEvent event) {
			return NbBundle.getMessage(TiledLayerEditorComponent.class, 
					"TiledLayerEditorComponent.horizontalRuler.tooltip", 
					this.getColumnAtPoint(event.getPoint()));
			//return "Column: " + this.getColumnAtPoint(event.getPoint());
		}
		
		public Dimension getPreferredSize() {
			Dimension size = TiledLayerEditorComponent.this.getPreferredSize();
			size.height = SIZE;
			return size;
		}
		protected void paintComponent(Graphics graphincs) {
			Graphics2D g = (Graphics2D) graphincs;
			
			Rectangle rect = g.getClipBounds();
			g.setColor(Color.WHITE);
			g.fill(rect);
			
			if (DEBUG) System.out.println("RulerHorizontal.repaint " + rect); // NOI18N
			
			int unit = TiledLayerEditorComponent.this.gridWidth + TiledLayerEditorComponent.this.cellWidth;
			
			for (int x = (rect.x / unit) * unit; x <= rect.x + rect.width; x+= unit) {
				int col =  x / unit;
				
				if (col >= TiledLayerEditorComponent.this.tiledLayer.getColumnCount()) {
					break;
				}
				
				boolean raised = true;
				//if (DEBUG) System.out.println("paint col: " + col);
				g.setColor(new Color(240, 238, 230));
				
				if (this.pressed.contains(col)) {
					g.setColor(new Color(200, 200, 200));
					raised = false;
				}

				g.fill3DRect(x + TiledLayerEditorComponent.this.gridWidth/2, 0, unit, SIZE, raised);
				
				if (col == this.hilitedColumnHeader) {
					g.setColor(HILITE_COLOR);
					g.fill3DRect(x + TiledLayerEditorComponent.this.gridWidth/2, 0, unit, SIZE, raised);
				}
			}
		}
		
		private int getColumnAtPoint(Point point) {
			return this.getColumnAtCoordinates(point.x, point.y);
		}
		private int getColumnAtCoordinates(int x, int y) {
			return (x - TiledLayerEditorComponent.this.gridWidth) / (TiledLayerEditorComponent.this.cellWidth + TiledLayerEditorComponent.this.gridWidth);
		}
		
		private Rectangle getColumnHeaderArea(int col) {
			Rectangle area = new Rectangle( 
					((TiledLayerEditorComponent.this.cellWidth + TiledLayerEditorComponent.this.gridWidth) * col) + TiledLayerEditorComponent.this.gridWidth/2, 
					0,
					TiledLayerEditorComponent.this.cellWidth + TiledLayerEditorComponent.this.gridWidth,
					SIZE);
			return area;
		}
				
		private void hiliteColumnHeader(int col) {
			if (this.hilitedColumnHeader == col) {
				return;
			}
			int oldHilited = this.hilitedColumnHeader;
			this.hilitedColumnHeader = col;
			this.repaint(this.getColumnHeaderArea(col));
			this.repaint(this.getColumnHeaderArea(oldHilited));
		}
		
		/**
		 * Toggle the selection of the column header and the column cell selection.
		 */
		public void mouseClicked(MouseEvent e) {
			int col = this.getColumnAtPoint(e.getPoint());
			TiledLayerEditorComponent.this.setColumnSelection(col, !e.isControlDown());
		}
		public void mousePressed(MouseEvent e) {
			int col = this.getColumnAtPoint(e.getPoint());
			if (e.isPopupTrigger()) {
				this.handlePopUp(e);
			}
			else {
				this.pressed.add(col);
				this.repaint(this.getColumnHeaderArea(col));
			}
		}
		public void mouseReleased(MouseEvent e) {
			int col = this.getColumnAtPoint(e.getPoint());
			if (e.isPopupTrigger()) {
				this.handlePopUp(e);
			}
			else {
				this.pressed.clear();
				this.repaint();
			}
		}
		
		public void mouseEntered(MouseEvent e) {
		}
		public void mouseExited(MouseEvent e) {
			this.hiliteColumnHeader(-1);
		}
		
		public void mouseDragged(MouseEvent e) {
		}
		public void mouseMoved(MouseEvent e) {
			int col = this.getColumnAtPoint(e.getPoint());
			this.hiliteColumnHeader(col);
		}

		private void handlePopUp(MouseEvent e) {
			int col = this.getColumnAtPoint(e.getPoint());
			if (DEBUG) System.out.println("Popup col: " + col); // NOI18N
			JPopupMenu menu = this.createRulerPopupMenu(col);
			menu.show(this, e.getX(), e.getY());
			this.hiliteColumnHeader(col);
		}
		private JPopupMenu createRulerPopupMenu(int col) {
			JPopupMenu menu = new JPopupMenu();
			for (Iterator iter = this.getActions().iterator(); iter.hasNext();) {
				Action action = (Action) iter.next();
				action.putValue("COLUMN", new Integer(col)); // NOI18N
				menu.add(action);
			}
			return menu;
		}
		
		public List getActions() {
			ArrayList actions = new ArrayList();
			actions.add(new DeleteColAction());
			actions.add(new PrependColAction());
			actions.add(new AppendColAction());
			return Collections.unmodifiableList(actions);
		}
		
		private class DeleteColAction extends AbstractAction {
			{
				this.putValue(NAME, NbBundle.getMessage(
						TiledLayerEditorComponent.class, 
						"TiledLayerEditorComponent.horizontalRuler.actionDeleteCol.txt"));
			}
			
			public void actionPerformed(ActionEvent e) {
				int col = ((Integer) this.getValue("COLUMN")).intValue(); // NOI18N
				TiledLayerEditorComponent.this.tiledLayer.deleteColumns(col, 1);
			}
		}
		private class PrependColAction extends AbstractAction {
			{
				this.putValue(NAME, NbBundle.getMessage(
						TiledLayerEditorComponent.class, 
						"TiledLayerEditorComponent.horizontalRuler.actionPrependCol.txt"));
			}
			
			public void actionPerformed(ActionEvent e) {
				int col = ((Integer) this.getValue("COLUMN")).intValue(); // NOI18N
				TiledLayerEditorComponent.this.tiledLayer.insertColumns(col, 1);
			}
		}
		private class AppendColAction extends AbstractAction {
			{
				this.putValue(NAME, NbBundle.getMessage(
						TiledLayerEditorComponent.class, 
						"TiledLayerEditorComponent.horizontalRuler.actionAppendCol.txt"));
			}
			
			public void actionPerformed(ActionEvent e) {
				int col = ((Integer) this.getValue("COLUMN")).intValue(); // NOI18N
				TiledLayerEditorComponent.this.tiledLayer.insertColumns(col +1, 1);
			}
		}
	}
	
}

