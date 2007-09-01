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

import java.awt.Dimension;
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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Iterator;
import java.util.List;
import javax.swing.Action;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPopupMenu;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.netbeans.modules.vmd.game.model.AnimatedTile;
import org.netbeans.modules.vmd.game.model.Editable;
import org.netbeans.modules.vmd.game.model.ImageResource;
import org.netbeans.modules.vmd.game.model.ImageResourceListener;
import org.netbeans.modules.vmd.game.model.Sequence;
import org.netbeans.modules.vmd.game.model.SequenceContainer;
import org.netbeans.modules.vmd.game.model.SequenceContainerListener;
import org.netbeans.modules.vmd.game.model.Tile;
import org.netbeans.modules.vmd.game.model.TileTransferable;
/**
 *
 * @author kherink
 */
public class AnimatedTileList extends JList {
	
	public static final boolean DEBUG = false;
	
	private TiledLayerEditorComponent editorComponent;
	private ImageResource imageResource;
	private AnimatedTileListDataModel model;
	
	/** Creates a new instance of AnimatedTileList */
	public AnimatedTileList(TiledLayerEditorComponent editorComponent) {
		this.editorComponent = editorComponent;
		this.imageResource = this.editorComponent.getTiledLayer().getImageResource();
		this.model = new AnimatedTileListDataModel();
		this.init();
		this.imageResource.addImageResourceListener(model);
		this.setModel(model);
		this.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		this.setCellRenderer(new AnimatedTileListCellRenderer());
		this.setMinimumSize(new Dimension(this.editorComponent.getTiledLayer().getTileWidth(), 20));
		this.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (e.getValueIsAdjusting())
					return;
				AnimatedTileList.this.updatePaintTile();
			}
		});
		this.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
               AnimatedTileList.this.updatePaintTile();
            }
		});
		this.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				if (e.isPopupTrigger()) {
					handlePopup(e);
				}
			}
			public void mouseReleased(MouseEvent e) {
				if (e.isPopupTrigger()) {
					handlePopup(e);
				}
			}
            public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() >= 2 && SwingUtilities.isLeftMouseButton(e)) {
					AnimatedTile tile = (AnimatedTile) AnimatedTileList.this.getSelectedValue();
					tile.getImageResource().getGameDesign().getMainView().requestEditing(tile);
				}
            }
		});
		//DnD
		DragSource dragSource = new DragSource();
		DragGestureRecognizer dragGestureRecognizer = dragSource.createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_MOVE, new DGL());

	}
	
	private void handlePopup(MouseEvent e) {
		JPopupMenu menu = new JPopupMenu();
        int row = AnimatedTileList.this.locationToIndex(e.getPoint());
        AnimatedTile at = (AnimatedTile) AnimatedTileList.this.getModel().getElementAt(row);
		List<Action> actions = at.getActions();
		for (Action action : actions) {
            menu.add(action);
		}
		menu.show(this, e.getX(), e.getY());
	}
	
	private void updatePaintTile() {
		Tile tile = (Tile) this.getSelectedValue();
		if (tile == null)
			return;
		tile.getImageResource().getGameDesign().getMainView().requestPreview(tile);
		this.editorComponent.setPaintTileIndex(tile.getIndex());
	}
	
	private void init() {
		List animatedTiles = this.imageResource.getAnimatedTiles(this.editorComponent.getTiledLayer().getTileWidth(), this.editorComponent.getTiledLayer().getTileHeight());
		for (Iterator iter = animatedTiles.iterator(); iter.hasNext();) {
			AnimatedTile tile = (AnimatedTile) iter.next();
			tile.addSequenceContainerListener(this.model);
			this.model.addElement(tile);
		}
	}
	
	
	
	public Dimension getMaximumSize() {
		return this.getPreferredSize();
	}

	private class DGL extends DragSourceAdapter implements DragGestureListener {

		public void dragGestureRecognized(DragGestureEvent dge) {
			Point dragOrigin = dge.getDragOrigin();
			int row = AnimatedTileList.this.locationToIndex(dragOrigin);
			Tile tile = (Tile) AnimatedTileList.this.getModel().getElementAt(row);
			TileTransferable payload = new TileTransferable();
			payload.getTiles().add(tile);
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

	private class AnimatedTileListDataModel extends DefaultListModel implements ImageResourceListener, SequenceContainerListener, PropertyChangeListener {

		@Override
		public void addElement(Object obj) {
			AnimatedTile tile = (AnimatedTile) obj;
			super.addElement(tile);
			tile.addPropertyChangeListener(this);
		}
		
        public void propertyChange(PropertyChangeEvent evt) {
			int index = this.indexOf(evt.getSource());
            if (evt.getPropertyName().equals(Editable.PROPERTY_NAME)) {
                AnimatedTileListDataModel.this.fireContentsChanged(this, index, index);
            }
        }
		
		//ImageResourceListener
		public void animatedTileAdded(ImageResource source, AnimatedTile tile) {
			if (tile.getWidth() == AnimatedTileList.this.editorComponent.getTiledLayer().getTileWidth()
					&& tile.getHeight() == AnimatedTileList.this.editorComponent.getTiledLayer().getTileHeight()) {
				this.addElement(tile);
				tile.addSequenceContainerListener(this);
			}
		}
		public void animatedTileRemoved(ImageResource source, AnimatedTile tile) {
			tile.removeSequenceContainerListener(this);
			this.removeElement(tile);
		}

		public void sequenceAdded(ImageResource source, Sequence sequence) {
		}

		public void sequenceRemoved(ImageResource source, Sequence sequence) {
		}

		//SequenceContainerListener
        public void sequenceAdded(SequenceContainer source, Sequence sequence, int index) {
			int tileIndex = this.indexOf(source);
			this.fireContentsChanged(this, tileIndex, tileIndex);
        }

        public void sequenceRemoved(SequenceContainer source, Sequence sequence, int index) {
			int tileIndex = this.indexOf(source);
			this.fireContentsChanged(this, tileIndex, tileIndex);
        }

        public void sequenceMoved(SequenceContainer source, Sequence sequence, int indexOld, int indexNew) {
			int tileIndex = this.indexOf(source);
			this.fireContentsChanged(this, tileIndex, tileIndex);
        }
	}
}
