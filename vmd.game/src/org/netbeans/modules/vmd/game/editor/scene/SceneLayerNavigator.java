/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
package org.netbeans.modules.vmd.game.editor.scene;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragGestureRecognizer;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceAdapter;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.io.IOException;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import org.netbeans.modules.vmd.game.editor.common.NonEmptyStringTableCellEditor;
import org.netbeans.modules.vmd.game.model.Layer;
import org.netbeans.modules.vmd.game.model.LayerDataFlavor;
import org.netbeans.modules.vmd.game.model.Scene;
import org.netbeans.modules.vmd.game.model.adapter.SceneLayerTableAdapter;

public class SceneLayerNavigator extends JTable {
	
	public static final boolean DEBUG = false;
	
	public static final int PAD_X = 4;
	public static final int PAD_Y = 4;
	
	private static final int IMG_PREVIEW_WIDTH = 40;
	private static final int IMG_PREVIEW_HEIGHT = 30;
	
	private Scene scene;
	
	public SceneLayerNavigator(Scene layerModel) {
		this.scene = layerModel;
		this.setModel(new SceneLayerTableAdapter(layerModel));
		this.getColumnModel().setColumnMargin(0);
		
		//Dnd
		DragSource dragSource = new DragSource();
		DragGestureRecognizer dragGestureRecognizer = dragSource.createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_MOVE, new DGL());
		DropTarget dropTarget = new DropTarget(this, new TableDropTarget());
		dropTarget.setActive(true);
		this.setDropTarget(dropTarget);
		
		
		this.getSelectionModel().addListSelectionListener(new SceneTableSelectionListener());
		
		this.setRowHeight(IMG_PREVIEW_HEIGHT);
		int width = IMG_PREVIEW_WIDTH /* + 2*PAD_X */ + this.getColumnModel().getColumnMargin();
		
		TableColumn typeColumn = this.getColumnModel().getColumn(SceneLayerTableAdapter.COL_INDEX_LAYER_TYPE);
		typeColumn.setPreferredWidth(width);
		typeColumn.setMaxWidth(width);
		typeColumn.setMinWidth(width);
		
		
		TableColumn indexColumn = this.getColumnModel().getColumn(SceneLayerTableAdapter.COL_INDEX_LAYER_INDEX);
		indexColumn.setPreferredWidth(width);
		indexColumn.setMaxWidth(width);
		indexColumn.setMinWidth(width);
		
		TableColumn visibilityColumn = this.getColumnModel().getColumn(SceneLayerTableAdapter.COL_INDEX_LAYER_VISIBILITY_INDICATOR);
		visibilityColumn.setPreferredWidth(width);
		visibilityColumn.setMaxWidth(width);
		visibilityColumn.setMinWidth(width);
		
		TableColumn lockColumn = this.getColumnModel().getColumn(SceneLayerTableAdapter.COL_INDEX_LAYER_LOCK_INDICATOR);
		lockColumn.setPreferredWidth(width);
		lockColumn.setMaxWidth(width);
		lockColumn.setMinWidth(width);
		
		this.setDefaultEditor(Boolean.class, new BooleanTableCellRenderer(PAD_X, PAD_Y));
		this.setDefaultRenderer(Boolean.class, new BooleanTableCellRenderer(PAD_X, PAD_Y));
		
		this.setDefaultRenderer(Layer.class, new LayerTableCellRenderer());
                
                this.setDefaultEditor(String.class, new NonEmptyStringTableCellEditor());

		this.setDefaultRenderer(Integer.class, new DefaultTableCellRenderer() {
                        @Override
			public Component getTableCellRendererComponent(JTable table, Object value,
					boolean isSelected, boolean hasFocus, int row, int column) {
				this.setHorizontalAlignment(SwingConstants.CENTER);
				return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			}
                });

                TableColumn posXColumn = this.getColumnModel().getColumn(SceneLayerTableAdapter.COL_INDEX_LAYER_POS_X);
		posXColumn.setPreferredWidth(width + 10);
		posXColumn.setMaxWidth(width + 10);
		posXColumn.setMinWidth(width + 10);
		
		TableColumn posYColumn = this.getColumnModel().getColumn(SceneLayerTableAdapter.COL_INDEX_LAYER_POS_Y);
		posYColumn.setPreferredWidth(width + 10);
		posYColumn.setMaxWidth(width + 10);
		posYColumn.setMinWidth(width + 10);
		
		
		this.setShowVerticalLines(false);
		this.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
	}
	
        @Override
	public Dimension getPreferredScrollableViewportSize() {
		return super.getPreferredSize();
	}
	
        private class SceneTableSelectionListener implements ListSelectionListener {
		
		public void valueChanged(ListSelectionEvent e) {
			if (e.getValueIsAdjusting()){
				return;
			}
			ListSelectionModel lsm = (ListSelectionModel) e.getSource();
			if (lsm.isSelectionEmpty())
				return;
			int selectedLayerIndex = SceneLayerNavigator.this.getSelectionModel().getAnchorSelectionIndex();
			Layer selectedLayer = SceneLayerNavigator.this.scene.getLayerAt(selectedLayerIndex);
			if (DEBUG) System.out.println(selectedLayer + " has been selected."); // NOI18N
			selectedLayer.getGameDesign().getMainView().requestPreview(selectedLayer);
		}
	}
	
	
	//DnD implementation
	private class DGL extends DragSourceAdapter implements DragGestureListener {
		public void dragGestureRecognized(DragGestureEvent dge) {
			Point dragOrigin = dge.getDragOrigin();
			int srcRow = SceneLayerNavigator.this.rowAtPoint(dragOrigin);
			if (DEBUG) System.out.println("dragGestureRecognized @ " + dragOrigin + " row: " + srcRow); // NOI18N
			Layer payload = (Layer) SceneLayerNavigator.this.scene.getLayerAt(srcRow);
			//Cursor.getPredefinedCursor(Cursor.getSystemCustomCursor(null));
			if (DEBUG) System.out.println("payload = " + payload); // NOI18N
			SceneLayerNavigator.this.setRowSelectionInterval(srcRow, srcRow);
			dge.startDrag(null, payload, this);
		}
		
        @Override
		public void dragDropEnd(DragSourceDropEvent dsde) {
			super.dragDropEnd(dsde);
			if (dsde.getDropSuccess()) {
				if (DEBUG) System.out.println("Drop End - success"); // NOI18N
			} 
			else {
				if (DEBUG) System.out.println("Drop End - failure!!!"); // NOI18N
			}
		}
	}
	
	private class TableDropTarget extends DropTargetAdapter {
		public void drop(DropTargetDropEvent dtde) {
			Point dropPoint = dtde.getLocation();
			if (DEBUG) System.out.println("Start drop @: " + dropPoint); // NOI18N
			int dropRow = SceneLayerNavigator.this.rowAtPoint(dropPoint);
			Transferable transferable = dtde.getTransferable();
			try {
				LayerDataFlavor layerFlavor = new LayerDataFlavor();
				if (transferable.isDataFlavorSupported(layerFlavor)) {
					dtde.acceptDrop(DnDConstants.ACTION_MOVE);
					Layer layer = (Layer) transferable.getTransferData(layerFlavor);
					SceneLayerNavigator.this.scene.insert(layer, dropRow);
					dtde.dropComplete(true);
				} 
				else {
					if (DEBUG) System.out.println("NOT a Layer ... weird."); // NOI18N
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
        @Override
		public void dragExit(DropTargetEvent dte) {
			if (DEBUG) System.out.println("dragExit"); // NOI18N
		}
	}
}
