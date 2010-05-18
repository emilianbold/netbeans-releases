/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
	
    public static final boolean DEBUG = false;
    
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
            if (tile != null) {
                tile.getImageResource().getGameDesign().getMainView().requestPreview(tile);
                tile.getImageResource().getGameDesign().getMainView().paintTileChanged(tile);
            } else {
                System.out.println("WARNING: selected tile " + index + " is NULL"); // NOI18N
            }
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
