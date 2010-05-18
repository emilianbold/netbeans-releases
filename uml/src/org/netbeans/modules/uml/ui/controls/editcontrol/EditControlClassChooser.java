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
 * Created on Feb 25, 2004
 *
 */
package org.netbeans.modules.uml.ui.controls.editcontrol;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.netbeans.modules.uml.core.support.umlsupport.IStrings;
import org.netbeans.modules.uml.ui.support.drawingproperties.DrawingPropertyResource;
import org.netbeans.modules.uml.ui.swing.commondialogs.JCenterDialog;

import java.awt.event.ActionEvent;

/**
 * @author jingmingm
 *
 */
public class EditControlClassChooser extends JCenterDialog
{
	protected static String m_ClassName = "";
	
	protected JLabel m_selectedClassName = null;
	protected static JLabel m_sampleField = null;
	protected JButton m_cancel = null;
	protected JButton m_ok = null;
	protected static JList m_NameList = null;
	
	protected void init(IStrings strs)
	{
		getContentPane().setLayout(new BorderLayout());
		
      JPanel namePanel = new JPanel();
      namePanel.setLayout(new GridBagLayout());
      
		getContentPane().add(namePanel, BorderLayout.CENTER);
		JPanel buttonPanel = new JPanel();
		getContentPane().add(buttonPanel, BorderLayout.SOUTH);
	
		// Add Class name
		m_selectedClassName = new JLabel();
      m_selectedClassName.setText(DrawingPropertyResource.getString("IDS_CHOOSECLASSLABEL"));
      GridBagConstraints constraints = new GridBagConstraints();
      constraints.gridx = 0;
      constraints.gridy = 0;
      constraints.insets = new Insets(10, 5, 10, 5);
      constraints.fill = GridBagConstraints.BOTH;
		namePanel.add(m_selectedClassName, constraints);
		
		Object[] names = strs.toArray();
		m_NameList = new JList(names);
      m_NameList.setVisibleRowCount(10);
		m_NameList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		m_NameList.addListSelectionListener(new ListSelectionListener()
		{
			public void valueChanged(ListSelectionEvent e)
			{
				JList names = (JList)e.getSource();
				m_ClassName = (String)names.getSelectedValue();
			}
		});	
			
		JScrollPane pane = new JScrollPane(m_NameList);
      constraints = new GridBagConstraints();
      constraints.gridx = 0;
      constraints.gridy = 1;
      constraints.fill = GridBagConstraints.BOTH;
      constraints.weightx = 1.0;
      constraints.weighty = 1.0;
      constraints.insets = new Insets(0, 5, 0, 5);
		namePanel.add(pane, constraints);
		
		// Add buttons
		m_ok = new JButton();
		m_ok.setText(DrawingPropertyResource.getString("IDS_OK"));
		buttonPanel.add(m_ok, BorderLayout.WEST);
		m_ok.addActionListener
		(
			new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					okPressed(e);
				}
			}
		);
		
		m_cancel = new JButton();
		m_cancel.setText(DrawingPropertyResource.getString("IDS_CANCEL"));
		buttonPanel.add(m_cancel, BorderLayout.EAST);
		m_cancel.addActionListener
		(
			new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					cancelPressed(e);
				}
			}
		);
		setTitle(DrawingPropertyResource.getString("IDS_CHOOSECLASS"));
		setSize(400, 300);
	}
		
	public EditControlClassChooser(IStrings strs)
	{
		init(strs);
	}

	protected void cancelPressed(ActionEvent e)
	{
		m_ClassName = null;
		dispose();
	}
	
	protected void okPressed(ActionEvent e)
	{
		dispose();
	}

   public String selectClass()
   {
      String name = null;
      setModal(true);
      setVisible(true);
      name = m_ClassName;
      return name;
   }
   
}



