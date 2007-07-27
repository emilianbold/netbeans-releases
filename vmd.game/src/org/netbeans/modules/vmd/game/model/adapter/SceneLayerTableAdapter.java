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
package org.netbeans.modules.vmd.game.model.adapter;

import java.awt.Point;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import org.netbeans.modules.vmd.game.model.Layer;
import org.netbeans.modules.vmd.game.model.Scene;
import org.netbeans.modules.vmd.game.model.SceneListener;
import org.netbeans.modules.vmd.game.model.Scene.LayerInfo;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;



public class SceneLayerTableAdapter implements TableModel, SceneListener, PropertyChangeListener  {
	
	public static final boolean DEBUG = false;

	private final static int COLS = 7;
	
	public final static int COL_INDEX_LAYER_TYPE = 0;
	public final static int COL_INDEX_LAYER_INDEX = 1;
	public final static int COL_INDEX_LAYER_VISIBILITY_INDICATOR = 2;
	public final static int COL_INDEX_LAYER_LOCK_INDICATOR = 3;
	public final static int COL_INDEX_LAYER_NAME = 4;
	public final static int COL_INDEX_LAYER_POS_X = 5;
	public final static int COL_INDEX_LAYER_POS_Y = 6;
	
	private Scene scene;
	
	private ArrayList listeners = new ArrayList();

	public SceneLayerTableAdapter(Scene scene) {
		this.scene = scene;
		this.scene.addSceneListener(this);
	}
	
	public int getColumnCount() {
		return COLS;
	}

	public int getRowCount() {
		return this.scene.getLayerCount();
	}

	public boolean isCellEditable(int rowIndex, int columnIndex) {
		//cannot manually edit layer position (Z) - this is done only by using DnD
		if (columnIndex == COL_INDEX_LAYER_INDEX)
			return false;
		//cannot edit layer type
		if (columnIndex == COL_INDEX_LAYER_TYPE)
			return false;
		//if the layer is locked then disable editing of layer location (X, Y)
		if (columnIndex == COL_INDEX_LAYER_POS_X || columnIndex == COL_INDEX_LAYER_POS_Y) {
			if (this.scene.isLayerLocked(this.scene.getLayerAt(rowIndex))) {
				return false;
			}
		}
		return true;
	}

	public Class getColumnClass(int columnIndex) {
		if (columnIndex == COL_INDEX_LAYER_TYPE)
			return Layer.class;
		if (columnIndex == COL_INDEX_LAYER_INDEX)
			return Integer.class;
		if (columnIndex == COL_INDEX_LAYER_VISIBILITY_INDICATOR)
			return Boolean.class;
		if (columnIndex == COL_INDEX_LAYER_LOCK_INDICATOR)
			return Boolean.class;
		if (columnIndex == COL_INDEX_LAYER_NAME)
			return String.class;
		if (columnIndex == COL_INDEX_LAYER_POS_X)
			return Integer.class;
		if (columnIndex == COL_INDEX_LAYER_POS_Y)
			return Integer.class;
		return Object.class;
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		if (rowIndex < 0 || rowIndex >= this.scene.getLayerCount() || columnIndex < 0 || columnIndex >= COLS)
			return null;
		Layer layer = this.scene.getLayerAt(rowIndex);
		switch (columnIndex) {
			case COL_INDEX_LAYER_TYPE:
				return layer;
			case COL_INDEX_LAYER_INDEX:
				return new Integer(this.scene.indexOf(layer));
			case COL_INDEX_LAYER_VISIBILITY_INDICATOR:
				return new Boolean(this.scene.isLayerVisible(layer));
			case COL_INDEX_LAYER_LOCK_INDICATOR:
				return new Boolean(this.scene.isLayerLocked(layer));
			case COL_INDEX_LAYER_NAME:
				return layer.getName();
			case COL_INDEX_LAYER_POS_X:
				return new Integer(this.scene.getLayerPosition(layer).x);
			case COL_INDEX_LAYER_POS_Y:
				return new Integer(this.scene.getLayerPosition(layer).y);
		}
		return null;
	}

	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		if (rowIndex < 0 || rowIndex >= this.scene.getLayerCount() || columnIndex < 0 || columnIndex >= COLS) {
			throw new IllegalArgumentException("Arguments rowIndex = " + rowIndex  // NOI18N
					+ ", columnIndex = " + columnIndex + " are illegal for table of " +  // NOI18N
					this.scene.getLayerCount() + " rows and " + COLS + " columns."); // NOI18N
		}
		Layer layer = this.scene.getLayerAt(rowIndex);
		switch (columnIndex) {
			//XXX this case has not been tested - this cell is not editable for now
			case COL_INDEX_LAYER_INDEX:
				Integer indexVal = (Integer) aValue;
				int newIndex = indexVal.intValue();
				this.scene.move(layer, newIndex);
				break;
			case COL_INDEX_LAYER_VISIBILITY_INDICATOR:
				boolean visible = ((Boolean) aValue).booleanValue();
				this.scene.setLayerVisible(layer, visible);
				break;
			case COL_INDEX_LAYER_LOCK_INDICATOR:
				boolean locked = ((Boolean) aValue).booleanValue();
				this.scene.setLayerLocked(layer, locked);
				break;
			case COL_INDEX_LAYER_NAME:
				String name = (String) aValue;
				if (name.equals(layer.getName())) {
					return;
				}
				if (!this.scene.getGameDesign().isComponentNameAvailable(name)) {
					DialogDisplayer.getDefault().notify(
							new DialogDescriptor.Message(
							NbBundle.getMessage(SceneLayerTableAdapter.class, "SceneLayerTableAdapter.noRenameDialog.txt", name),
							//"Layer cannot be renamed because component name '" + name + "' already exists.", 
							DialogDescriptor.ERROR_MESSAGE)
					);
				}
				else {
					layer.setName(name);
				}
				break;
			case COL_INDEX_LAYER_POS_X:
				Integer xPos = (Integer) aValue;
				this.scene.setLayerPositionX(layer, xPos.intValue(), false);
				break;
			case COL_INDEX_LAYER_POS_Y:
				Integer yPos = (Integer) aValue;
				this.scene.setLayerPositionY(layer, yPos.intValue(), false);
				break;
		}
	}

	public String getColumnName(int columnIndex) {
		switch (columnIndex) {
			case COL_INDEX_LAYER_TYPE:
				return "Type";
			case COL_INDEX_LAYER_INDEX:
				return "Z";
			case COL_INDEX_LAYER_VISIBILITY_INDICATOR:
				return "View";
			case COL_INDEX_LAYER_LOCK_INDICATOR:
				return "Lock";
			case COL_INDEX_LAYER_NAME:
				return "Name";
			case COL_INDEX_LAYER_POS_X:
				return "X";
			case COL_INDEX_LAYER_POS_Y:
				return "Y";
			default:
				return "???";
		}
	}

	public void addTableModelListener(TableModelListener l) {
		this.listeners.add(l);
	}

	public void removeTableModelListener(TableModelListener l) {
		this.listeners.remove(l);
	}
	
	private void fireTableChanged(TableModelEvent e) {
		for (Iterator iter = this.listeners.iterator(); iter.hasNext();) {
			TableModelListener l = (TableModelListener) iter.next();
			l.tableChanged(e);
		}
		
	}

	//listens for name changes on all layers
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getSource() instanceof Layer) {
			Layer layer = (Layer) evt.getSource();
			if (DEBUG) System.out.println("layer changed: " + layer); // NOI18N
			int index = this.scene.indexOf(layer);
			TableModelEvent e = new TableModelEvent(this, index, index, TableModelEvent.ALL_COLUMNS, TableModelEvent.UPDATE);
			this.fireTableChanged(e);
		}
		
	}

	public void layerAdded(Scene sourceScene, Layer layer, int index) {
		TableModelEvent e = new TableModelEvent(this, index, index, TableModelEvent.ALL_COLUMNS, TableModelEvent.INSERT);
		layer.addPropertyChangeListener(this);
		this.fireTableChanged(e);
	}

	public void layerRemoved(Scene sourceScene, Layer layer, LayerInfo info, int index) {
		TableModelEvent e = new TableModelEvent(this, index, index, TableModelEvent.ALL_COLUMNS, TableModelEvent.DELETE);
		layer.removePropertyChangeListener(this);
		this.fireTableChanged(e);
	}

	public void layerModified(Scene sourceScene, Layer layer) {
		int index = sourceScene.indexOf(layer);
		TableModelEvent e = new TableModelEvent(this, index, index, TableModelEvent.ALL_COLUMNS, TableModelEvent.UPDATE);
		this.fireTableChanged(e);
	}
	
	public void layerMoved(Scene sourceScene, Layer layer, int indexOld, int indexNew) {
		this.layerRemoved(sourceScene, layer, null, indexOld);
		this.layerAdded(sourceScene, layer, indexNew);
	}

	public void layerLockChanged(Scene sourceScene, Layer layer, boolean locked) {
		this.layerModified(sourceScene, layer);
	}

	public void layerPositionChanged(Scene sourceScene, Layer layer, Point oldPosition, Point newPosition, boolean inTransition) {
		this.layerModified(sourceScene, layer);
	}

	public void layerVisibilityChanged(Scene sourceScene, Layer layer, boolean visible) {
		this.layerModified(sourceScene, layer);
	}
	
}
