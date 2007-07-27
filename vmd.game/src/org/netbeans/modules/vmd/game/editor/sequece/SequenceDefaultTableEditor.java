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
package org.netbeans.modules.vmd.game.editor.sequece;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.EventObject;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.CellEditorListener;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author kherink
 */
public class SequenceDefaultTableEditor extends JRadioButton implements TableCellRenderer, TableCellEditor, ActionListener, ItemListener {
	
	//Without this panel the border doesn't show up correcly when jbutton is selected and has focus
	private JPanel panel = new JPanel();
	
	
	protected static Border noFocusBorder = new EmptyBorder(1, 1, 1, 1);
	private Color unselectedForeground;
	private Color unselectedBackground;
	
	//protected EventListenerList listenerList = new EventListenerList();
	
	
	/** Creates a new instance of SequenceDefaultTableEditor */
	public SequenceDefaultTableEditor() {
		this.addActionListener(this);
		this.addItemListener(this);
		this.setHorizontalAlignment(SwingConstants.CENTER);
		this.setBackground(Color.WHITE);
		this.panel.setBackground(Color.WHITE);
		this.panel.setLayout(new BorderLayout());
		this.panel.add(this, BorderLayout.CENTER);
	}
	
	//------------ TableCellRenderer -----------------
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		if (value instanceof  Boolean) {
			Boolean b = (Boolean) value;
			this.setSelected(b);
			if (isSelected) {
				super.setForeground(table.getSelectionForeground());
				super.setBackground(table.getSelectionBackground());
				this.panel.setForeground(table.getSelectionForeground());
				this.panel.setBackground(table.getSelectionBackground());
			} else {
				super.setForeground((unselectedForeground != null) ? unselectedForeground
						: table.getForeground());
				super.setBackground((unselectedBackground != null) ? unselectedBackground
						: table.getBackground());
				this.panel.setForeground((unselectedForeground != null) ? unselectedForeground
						: table.getForeground());
				this.panel.setBackground((unselectedBackground != null) ? unselectedBackground
						: table.getBackground());
			}
			
			setFont(table.getFont());
			
			if (hasFocus) {
				Border border = null;
				if (isSelected) {
					border = UIManager.getBorder("Table.focusSelectedCellHighlightBorder"); // NOI18N
				}
				if (border == null) {
					border = UIManager.getBorder("Table.focusCellHighlightBorder"); // NOI18N
				}
				this.panel.setBorder(border);
				
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
			} else {
				this.panel.setBorder(noFocusBorder);
			}
			return this.panel;
		}
		throw new IllegalArgumentException("Only Boolean can be rendered!"); // NOI18N
	}
	
	//------------ TableCellEditor -------------------
	
	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
		if (value instanceof  Boolean) {
			this.setSelected((Boolean) value);
			return this;
		}
		throw new IllegalArgumentException("Only Boolean can be edited."); // NOI18N
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
