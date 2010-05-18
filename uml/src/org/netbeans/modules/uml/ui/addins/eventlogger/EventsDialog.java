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


package org.netbeans.modules.uml.ui.addins.eventlogger;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.Calendar;

import javax.swing.JDialog;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.MouseInputAdapter;
import javax.swing.table.DefaultTableModel;

import org.netbeans.modules.uml.ui.swing.commondialogs.JCenterDialog;

/**
 * @author sumitabhk
 *
 */
public class EventsDialog extends JCenterDialog implements ActionListener
{
	private JTable m_Table = new JTable();
	private DefaultTableModel m_Model = new DefaultTableModel();
	private EventLoggingAddin m_Parent = null;
	
	private boolean m_ModifierFilter = false;
	private boolean m_WorkspaceFilter = false;
	private boolean m_RoundTripFilter = false;
	private boolean m_ClassifierFilter = false;
	private boolean m_ProjectTreeFilter = false;
	private boolean m_ProjectTreeFilterFilter = false;
	private boolean m_CoreProductFilter = false;
	private boolean m_LifetimeFilter = false;
	private boolean m_ReleationshipFilter = false;
	private boolean m_EditControlFilter = false;
	private boolean m_MessengerFilter = false;
	private boolean m_VBAFilter = false;
	private boolean m_AddinFilter = false;
	private boolean m_DrawAreaFilter = false;
	
	//the popup that we will show
	private JPopupMenu m_Menu = null;
	
	/**
	 * 
	 */
	public EventsDialog()
	{
		super();
	}

	public EventsDialog(EventLoggingAddin addin)
	{
		super();
		m_Parent = addin;
		m_Model.addColumn("Time");
		m_Model.addColumn("Message Type");
		m_Table.setModel(m_Model);
		m_Table.addMouseListener(new EventMouseHandler());
		this.getContentPane().add(new JScrollPane(m_Table));
		this.setSize(400, 300);
	}

	/**
	 * Adds an entry to the list
	 */
	public void addEntry(String newEntry)
	{
		String dateStr = Calendar.getInstance().getTime().toString();
		Object[] vec = new Object[2];
		vec[0] = dateStr;
		vec[1] = newEntry;
		m_Model.addRow(vec);
		m_Table.updateUI();
	}

	public void onContextMenu()
	{
		m_Table.removeAll();
		m_Table.updateUI();
	}
	
	public void onFilterEvents()
	{
		EventFilterDialog dialog = new EventFilterDialog(m_Parent);
		dialog.setDrawAreaFilter(m_DrawAreaFilter);
		dialog.setAddinFilter(m_AddinFilter);
		dialog.setVBAFilter(m_VBAFilter);
		dialog.setMessengerFilter(m_MessengerFilter);
		dialog.setEditControlFilter(m_EditControlFilter);
		dialog.setReleationshipFilter(m_ReleationshipFilter);
		dialog.setLifetimeFilter(m_LifetimeFilter);
		dialog.setCoreProductFilter(m_CoreProductFilter);
		dialog.setProjectTreeFilterFilter(m_ProjectTreeFilterFilter);
		dialog.setProjectTreeFilter(m_ProjectTreeFilter);
		dialog.setClassifierFilter(m_ClassifierFilter);
		dialog.setRoundTripFilter(m_RoundTripFilter);
		dialog.setWorkspaceFilter(m_WorkspaceFilter);
		
		dialog.setModal(true);
		dialog.setVisible(true);
	}
	
	public JPopupMenu getPopupMenu()
	{
		if (m_Menu == null)
		{
			m_Menu = new JPopupMenu();
			JMenuItem menuItem = new JMenuItem("Clear");
			m_Menu.add(menuItem);
			menuItem.addActionListener(this);
			m_Menu.setPreferredSize(new Dimension(60, 25));
		}
		return m_Menu;
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e)
	{
//		int count = m_Model.getRowCount();
//		for (int i=0; i<count; i++)
//		{
//			m_Model.removeRow(i);
//		}
		m_Model.setRowCount(0);
		m_Table.updateUI();
	}
	
	private class EventMouseHandler extends MouseInputAdapter
	{
		//should allow user to clear the display
		public void mousePressed(MouseEvent e)
		{
			if(e.getButton() == MouseEvent.BUTTON3)
			{
				JPopupMenu menu = getPopupMenu();
				menu.show(e.getComponent(), e.getX(), e.getY());
			}
		}
	}
}


