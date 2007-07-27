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

package org.netbeans.modules.vmd.game.editor.grid;
import java.awt.Point;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragGestureRecognizer;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceAdapter;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import javax.swing.JList;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.netbeans.modules.vmd.game.editor.common.TileCellRenderer;
import org.netbeans.modules.vmd.game.model.ImageResource;
import org.netbeans.modules.vmd.game.model.StaticTile;
import org.netbeans.modules.vmd.game.model.Tile;
import org.netbeans.modules.vmd.game.model.TileTransferable;

/**
 *
 * @author kherink
 */
class ResourceImageList extends JList {
	
    public static final boolean DEBUG = true;
    
	private int padX = 4;
	private int padY = 4;
	
	
	
	/** Creates a new instance of ResourceImageList */
	public ResourceImageList() {
		this.setModel(new ResourceImageListModel());
		this.setCellRenderer(new TileCellRenderer(padX, padY));
		this.setLayoutOrientation(JList.HORIZONTAL_WRAP);
		this.setVisibleRowCount(-1);
		this.addListSelectionListener(new ResourceImageListSelectionListener());
		this.addFocusListener(new FocusAdapter() {
			public void focusGained(FocusEvent e) {
				int index = ResourceImageList.this.getSelectedIndex();
				if (index != -1) {
					StaticTile tile = (StaticTile) ResourceImageList.this.getModel().getElementAt(index);
					tile.getImageResource().getGameDesign().getMainView().requestPreview(tile);
					tile.getImageResource().getGameDesign().getMainView().paintTileChanged(tile);
				}
			}
		});
		DragSource dragSource = new DragSource();
		DragGestureRecognizer dgr = dragSource.createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_COPY, new DGL());
	}
	
	public void setImageResource(ImageResource imageResource, int tileWidth, int tileHeight, boolean zeroBasedIndex) {
		((ResourceImageListModel) this.getModel()).update(imageResource, tileWidth, tileHeight, zeroBasedIndex);
	}
	
	private class DGL extends DragSourceAdapter implements DragGestureListener {

		public void dragGestureRecognized(DragGestureEvent dge) {
			Point dragOrigin = dge.getDragOrigin();
			TileTransferable payload = new TileTransferable();
			//Dragging a single tile
			if (ResourceImageList.this.getSelectedValues().length == 0) {
				if (DEBUG) System.out.println("selection Empty"); // NOI18N
				int index = ResourceImageList.this.locationToIndex(dragOrigin);
				Tile tile = (Tile) ResourceImageList.this.getModel().getElementAt(index);
				payload.getTiles().add(tile);
				ResourceImageList.this.hiliteTileAtPoint(dragOrigin);
			}
			//Dragging multiple tiles
			else {
				if (DEBUG) System.out.println("selection Not empty"); // NOI18N
				Object[] values = ResourceImageList.this.getSelectedValues();
				for (int i = 0; i < values.length; i++) {
					payload.getTiles().add((Tile) values[i]);
				}
			}
			dge.startDrag(null, payload, this);
		}
		
		public void dragDropEnd(DragSourceDropEvent dsde) {
			super.dragDropEnd(dsde);
			if (dsde.getDropSuccess()) {
				if (DEBUG) System.out.println("Drop successful"); // NOI18N
			}
			else {
				if (DEBUG) System.out.println("Drop unsuccessful"); // NOI18N
			}
		}
	
	}
	
	private class ResourceImageListSelectionListener implements ListSelectionListener {
		public void valueChanged(ListSelectionEvent e) {
			if (DEBUG) System.out.println("GridTableSelectionListener.valueChanged()"); // NOI18N
			int index = ResourceImageList.this.getSelectedIndex();
	        this.handleTileSelection(index);
		}

		private void handleTileSelection(int index) {
			if (DEBUG) System.out.println("Tile selected: " + index); // NOI18N
			StaticTile tile = (StaticTile) ResourceImageList.this.getModel().getElementAt(index);
			tile.getImageResource().getGameDesign().getMainView().requestPreview(tile);
			tile.getImageResource().getGameDesign().getMainView().paintTileChanged(tile);
		}
	}

	private void hiliteTileAtPoint(Point point) {
		if (DEBUG) System.out.println("HILITE tile"); // NOI18N
		int hilite = this.locationToIndex(point);
		this.setSelectedIndex(hilite);
	}

	public Tile getSelectedTile() {
		int selection = this.getSelectedIndex();
		return (Tile) this.getModel().getElementAt(selection);
	}

    public java.awt.Dimension getPreferredSize() {
		return this.getMaximumSize();
    }
	

}
