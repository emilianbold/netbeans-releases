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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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


/*
 * Created on Jun 18, 2003
 *
 */
package org.netbeans.modules.uml.ui.swing.finddialog;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.event.MouseInputAdapter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import org.netbeans.modules.uml.ui.support.finddialog.FindUtilities;
import org.netbeans.modules.uml.ui.swing.preferencedialog.ISwingPreferenceTableModel;


/**
 * @author sumitabhk
 *
 */
public class JFindTable extends JTable
{
	private FindDialogUI m_UI = null;
	
	public JFindTable()
	{
		super();
	}
	
	public JFindTable(ISwingPreferenceTableModel model, FindDialogUI ui)
	{
		super(model);
		
		m_UI = ui;
		
		FindTableCellEditor cellEditor = new FindTableCellEditor(ui);
		FindTableCellRenderer renderer = new FindTableCellRenderer();
		for (int x = 0; x < getColumnModel().getColumnCount(); x++)
		{
			getColumnModel().getColumn(x).setCellEditor(cellEditor);
			getColumnModel().getColumn(x).setCellRenderer(renderer);
		}
		
		MouseListener mListener = new FindPopupListener();
		this.addMouseListener(mListener);
		addMouseListener(new FindMouseHandler());

		if (model.getColumnCount() > 0)
		{
			//if (model.getColumnName(0).equals(""))
			//{                                
				getColumnModel().getColumn(0).setMinWidth(30);
				getColumnModel().getColumn(0).setMaxWidth(30);
			//}
		}
		
		// Add accelerator
		ActionListener action = new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				int selRow = getSelectedRow();
				if (selRow != -1)
				{ 
					m_UI.onDblClickFindResults(selRow, (FindTableModel)getModel());
				}
			}
		};
		registerKeyboardAction(action, KeyStroke.getKeyStroke("SPACE"), JComponent.WHEN_IN_FOCUSED_WINDOW);
        
	}
	
	public class FindMouseHandler extends MouseInputAdapter
	{
	   public void mousePressed(MouseEvent e) 
	   {
			int selRow = rowAtPoint(e.getPoint());
			if(e.getClickCount() == 2)
			{
			  m_UI.onDblClickFindResults(selRow, (FindTableModel)getModel());
			}
	   }
	}
	
	private class FindTableCellRenderer extends DefaultTableCellRenderer implements TableCellRenderer
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
	public class FindPopupListener extends MouseAdapter
	{
		/**
		 * 
		 */
		public FindPopupListener()
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



