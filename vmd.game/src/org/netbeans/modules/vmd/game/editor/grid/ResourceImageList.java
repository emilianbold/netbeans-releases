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
import org.netbeans.modules.vmd.game.view.main.MainView;

/**
 *
 * @author kherink
 */
class ResourceImageList extends JList {
	
    public static final boolean DEBUG = true;
    
	private int padX = 2;
	private int padY = 2;
	
	
	
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
					MainView.getInstance().requestPreview(tile);
					MainView.getInstance().paintTileChanged(tile);
				}
			}
		});
		DragSource dragSource = new DragSource();
		DragGestureRecognizer dgr = dragSource.createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_COPY, new DGL());
	}
	
	public void setImageResource(ImageResource imageResource) {
		((ResourceImageListModel) this.getModel()).update(imageResource);
	}
	
	private class DGL extends DragSourceAdapter implements DragGestureListener {

		public void dragGestureRecognized(DragGestureEvent dge) {
			Point dragOrigin = dge.getDragOrigin();
			TileTransferable payload = new TileTransferable();
			//Dragging a single tile
			if (ResourceImageList.this.getSelectedValues().length == 0) {
				if (DEBUG) System.out.println("selection Empty");
				int index = ResourceImageList.this.locationToIndex(dragOrigin);
				Tile tile = (Tile) ResourceImageList.this.getModel().getElementAt(index);
				payload.getTiles().add(tile);
				ResourceImageList.this.hiliteTileAtPoint(dragOrigin);
			}
			//Dragging multiple tiles
			else {
				if (DEBUG) System.out.println("selection Not empty");
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
				if (DEBUG) System.out.println("Drop successful");
			}
			else {
				if (DEBUG) System.out.println("Drop unsuccessful");
			}
		}
	
	}
	
	private class ResourceImageListSelectionListener implements ListSelectionListener {		
		public void valueChanged(ListSelectionEvent e) {
			if (DEBUG) System.out.println("GridTableSelectionListener.valueChanged()");
	        if (e.getValueIsAdjusting())
	            return;
			int index = ResourceImageList.this.getSelectedIndex();
	        this.handleTileSelection(index);
		}

		private void handleTileSelection(int index) {
			if (DEBUG) System.out.println("Tile selected: " + index);
			StaticTile tile = (StaticTile) ResourceImageList.this.getModel().getElementAt(index);
			MainView.getInstance().requestPreview(tile);
			MainView.getInstance().paintTileChanged(tile);
		}
	}

	private void hiliteTileAtPoint(Point point) {
		if (DEBUG) System.out.println("HILITE tile");
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
