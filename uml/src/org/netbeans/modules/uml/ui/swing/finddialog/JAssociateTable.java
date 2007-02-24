/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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


/*
 * Created on Jun 18, 2003
 *
 */
package org.netbeans.modules.uml.ui.swing.finddialog;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;

import javax.swing.AbstractCellEditor;
import javax.swing.Icon;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.event.MouseInputAdapter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import org.netbeans.modules.uml.ui.addins.associateDialog.AssociateDlgGUI;
import org.netbeans.modules.uml.ui.support.finddialog.FindUtilities;
import org.netbeans.modules.uml.ui.swing.preferencedialog.ISwingPreferenceTableModel;


/**
 * @author sumitabhk
 *
 */
public class JAssociateTable extends JTable
{
	private AssociateDlgGUI m_UI = null;
	
	public JAssociateTable()
	{
		super();
	}
	
	public JAssociateTable(ISwingPreferenceTableModel model, AssociateDlgGUI ui)
	{
		super(model);
		
		m_UI = ui;
		
		AssocTableCellEditor cellEditor = new AssocTableCellEditor(ui);
		AssocTableCellRenderer renderer = new AssocTableCellRenderer();
		for (int x = 0; x < getColumnModel().getColumnCount(); x++)
		{
			getColumnModel().getColumn(x).setCellEditor(cellEditor);
			getColumnModel().getColumn(x).setCellRenderer(renderer);
		}
		
		MouseListener mListener = new AssocPopupListener();
		this.addMouseListener(mListener);
		addMouseListener(new AssocMouseHandler());

		if (model.getColumnCount() > 0)
		{
			if (model.getColumnName(0).equals(""))
			{
				getColumnModel().getColumn(0).setMinWidth(20);
				getColumnModel().getColumn(0).setMaxWidth(20);
			}
		}
	}
	
	public class AssocMouseHandler extends MouseInputAdapter
	{
	   public void mousePressed(MouseEvent e) 
	   {
			int selRow = rowAtPoint(e.getPoint());
			if(e.getClickCount() == 2)
			{
			  //m_UI.onDblClickFindResults(selRow, (FindTableModel)getModel());
			}
	   }
	}
	
	private class AssocTableCellRenderer extends DefaultTableCellRenderer implements TableCellRenderer
	{
		/* (non-Javadoc)
		 * @see javax.swing.table.TableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
		 */
		public Component getTableCellRendererComponent(JTable table, 
													   Object value, 
													   boolean isSelected, 
													   boolean hasFocus, 
													   int row, 
													   int col)
		{
			Font pFont = FindUtilities.getGridFontFromPreferences();
			setFont(pFont);
			
			if (value instanceof String){
				setIcon(null);
				setText((String)value);
			}
			else if (value instanceof Icon){
				setIcon((Icon)value);
				setText(null);
			}
			Color background;
			Color foreground;

			if(isSelected) 
			{
				background = table.getSelectionBackground();
				foreground = table.getSelectionForeground();
			}
			else 
			{
				background = table.getBackground();
				foreground = table.getForeground();
			}
			Border highlightBorder = null;
			if (hasFocus) 
			{
				highlightBorder = UIManager.getBorder
							  ("Table.focusCellHighlightBorder");
			}
			TableCellRenderer tcr = getCellRenderer(row, col);
			if (tcr instanceof DefaultTableCellRenderer)
			{
				DefaultTableCellRenderer dtcr = ((DefaultTableCellRenderer)tcr);
				dtcr.setBackground(background);
				dtcr.setForeground(foreground);
			}
			return this;
		}
	}
	public class AssocPopupListener extends MouseAdapter
	{
		/**
		 * 
		 */
		public AssocPopupListener()
		{
			super();
		}

		public void mousePressed(MouseEvent e) {
		}

		public void mouseReleased(MouseEvent e) {
		}

		private void maybeShowPopup(MouseEvent e) {
		}

	}
	public class AssocTableCellEditor extends AbstractCellEditor implements
			 TableCellEditor 
	{
		boolean focusChange = false;
		AssociateDlgGUI m_UI = null;

		/**
		 * 
		 */
		public AssocTableCellEditor()
		{
			super();
		}
		public AssocTableCellEditor(AssociateDlgGUI ui)
		{
			super();
			m_UI = ui;
		}

		/**
		 * TreeTableCellEditor implementation. Component returned is the
		 * JTree.
		 */
		public Component getTableCellEditorComponent(JTable table,
								 Object value,
								 boolean isSelected,
								 int r, int c)
		{
			Component retObj = null;
			return retObj;
		}
		/* (non-Javadoc)
		 * @see javax.swing.CellEditor#getCellEditorValue()
		 */
		public Object getCellEditorValue()
		{
			return null;
		}

		private void columnValueChanged(PropertyChangeEvent e)
		{
		}

	}
	
}



