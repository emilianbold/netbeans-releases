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
package org.netbeans.modules.vmd.game.editor.scene;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.net.MalformedURLException;
import java.util.EventObject;

import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.CellEditorListener;
import javax.swing.event.EventListenerList;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import org.netbeans.modules.vmd.game.model.adapter.SceneLayerTableAdapter;

public class BooleanTableCellRenderer extends JCheckBox implements TableCellRenderer, TableCellEditor, ActionListener, ItemListener {
	
	protected static Border noFocusBorder = new EmptyBorder(1, 1, 1, 1);
	private Color unselectedForeground;
	private Color unselectedBackground;
	
	private ImageIcon iconVisible;
	private ImageIcon iconInvisible;
	private ImageIcon iconLocked;
	private ImageIcon iconUnlocked;
	
	protected EventListenerList listenerList = new EventListenerList();
	
	public BooleanTableCellRenderer(int padX, int padY) {
		try {
			this.initIcons();
		} 
		catch (MalformedURLException e) {
			e.printStackTrace();
		}
		this.addActionListener(this);
		this.addItemListener(this);
		this.setBackground(Color.WHITE);
		Dimension d = new Dimension(this.iconVisible.getIconWidth() + padX, this.iconVisible.getIconHeight() + padY);
		this.setPreferredSize(d);
		this.setHorizontalAlignment(SwingConstants.CENTER);
		// this.setMaximumSize(d);
	}
	
	private void initIcons() throws MalformedURLException {
		this.iconVisible = new ImageIcon(this.getClass().getResource("res/visible.png")); // NOI18N
		this.iconInvisible = new ImageIcon(this.getClass().getResource("res/invisible.png")); // NOI18N
		this.iconLocked = new ImageIcon(this.getClass().getResource("res/lock.png")); // NOI18N
		this.iconUnlocked = new ImageIcon(this.getClass().getResource("res/unlock.png")); // NOI18N
	}
	
	public Component getTableCellRendererComponent(JTable table, final Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		if (value instanceof Boolean) {
			this.setImages(column);
			this.setSelected(((Boolean) value).booleanValue());
			if (isSelected) {
				super.setForeground(table.getSelectionForeground());
				super.setBackground(table.getSelectionBackground());
			} 
			else {
				super.setForeground((unselectedForeground != null) ? unselectedForeground : table.getForeground());
				super.setBackground((unselectedBackground != null) ? unselectedBackground : table.getBackground());
			}
			
			setFont(table.getFont());
			
			if (hasFocus) {
				setBorder(UIManager.getBorder("Table.focusCellHighlightBorder")); // NOI18N
				if (!isSelected && table.isCellEditable(row, column)) {
					Color col;
					col = UIManager.getColor("Table.focusCellForeground"); // NOI18N
					if (col != null) {
						super.setForeground(col);
					}
					col = UIManager.getColor("Table.focusCellBackground"); // NOI18N
					if (col != null) {
						super.setBackground(col);
					}
				}
			} 
			else {
				setBorder(noFocusBorder);
			}
			return this;
		} 
		else {
			throw new IllegalArgumentException("Only Boolean can be rendered."); // NOI18N
		}
	}
	
	public Component getTableCellEditorComponent(final JTable table, final Object value, boolean isSelected, final int row, final int column) {
		if (value instanceof Boolean) {
			this.setImages(column);
			this.setSelected(((Boolean) value).booleanValue());
			return this;
		} 
		throw new IllegalArgumentException("Only Boolean can be edited."); // NOI18N
	}
	
	private void setImages(int col) {
		if (col == SceneLayerTableAdapter.COL_INDEX_LAYER_LOCK_INDICATOR) {
			this.setSelectedIcon(iconLocked);
			this.setIcon(iconUnlocked);
		} 
		else {
			this.setSelectedIcon(iconVisible);
			this.setIcon(iconInvisible);
		}
	}
	
	public void addCellEditorListener(CellEditorListener listener) {
		listenerList.add(CellEditorListener.class, listener);
	}
	
	public void removeCellEditorListener(CellEditorListener listener) {
		listenerList.remove(CellEditorListener.class, listener);
	}
	
	protected void fireEditingStopped() {
		CellEditorListener listener;
		Object[] listeners = listenerList.getListenerList();
		for (int i = 0; i < listeners.length; i++) {
			if (listeners[i] == CellEditorListener.class) {
				listener = (CellEditorListener) listeners[i + 1];
				listener.editingStopped(changeEvent);
			}
		}
	}
	
	protected void fireEditingCanceled() {
		CellEditorListener listener;
		Object[] listeners = listenerList.getListenerList();
		for (int i = 0; i < listeners.length; i++) {
			if (listeners[i] == CellEditorListener.class) {
				listener = (CellEditorListener) listeners[i + 1];
				listener.editingCanceled(changeEvent);
			}
		}
	}
	
	public void cancelCellEditing() {
		fireEditingCanceled();
	}
	
	public boolean stopCellEditing() {
		fireEditingStopped();
		return true;
	}
	
	public Object getCellEditorValue() {
		return new Boolean(this.isSelected());
	}
	
	public boolean isCellEditable(EventObject anEvent) {
		return true;
	}
	
	public boolean shouldSelectCell(EventObject anEvent) {
		return true;
	}
	
	public void actionPerformed(ActionEvent e) {
		this.fireEditingStopped();
	}
	
	public void itemStateChanged(ItemEvent e) {
		this.fireEditingStopped();
	}
	
	public void setForeground(Color c) {
		super.setForeground(c);
		unselectedForeground = c;
	}
	
	public void setBackground(Color c) {
		super.setBackground(c);
		unselectedBackground = c;
	}
	
}
