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
package org.netbeans.modules.uml.integration.finddialog.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.event.MouseInputAdapter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.netbeans.modules.uml.integration.finddialog.FindUtilities;
import org.netbeans.modules.uml.ui.swing.preferencedialog.ISwingPreferenceTableModel;


/**
 * @author sumitabhk
 *
 */
public class JReplaceTable extends JTable
{
	private ReplaceDialogUI m_UI = null;
	
	public JReplaceTable()
	{
		super();
	}
	
	public JReplaceTable(ISwingPreferenceTableModel model, ReplaceDialogUI ui)
	{
		super(model);
		
		m_UI = ui;
		
		ReplaceTableCellEditor cellEditor = new ReplaceTableCellEditor(ui);
		ReplaceTableCellRenderer renderer = new ReplaceTableCellRenderer();
		for (int x = 0; x < getColumnModel().getColumnCount(); x++)
		{
			getColumnModel().getColumn(x).setCellEditor(cellEditor);
			getColumnModel().getColumn(x).setCellRenderer(renderer);
		}
		
		MouseListener mListener = new ReplacePopupListener();
		this.addMouseListener(mListener);
		addMouseListener(new ReplaceMouseHandler());
	}
	
	public class ReplaceMouseHandler extends MouseInputAdapter
	{
	   public void mousePressed(MouseEvent e) 
	   {
			int selRow = rowAtPoint(e.getPoint());
			if(e.getClickCount() == 2)
			{
			  boolean isShift = (e.getModifiers() & MouseEvent.SHIFT_MASK) == MouseEvent.SHIFT_MASK;
			  m_UI.onDblClickFindResults(selRow, (FindTableModel)getModel(), isShift);
			}
	   }
	}
	
	private class ReplaceTableCellRenderer extends DefaultTableCellRenderer implements TableCellRenderer
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
	public class ReplacePopupListener extends MouseAdapter
	{
		/**
		 * 
		 */
		public ReplacePopupListener()
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
	
}



