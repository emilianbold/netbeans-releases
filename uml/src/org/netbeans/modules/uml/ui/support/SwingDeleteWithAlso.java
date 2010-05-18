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
 * Created on Jun 3, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.netbeans.modules.uml.ui.support;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.netbeans.modules.uml.core.support.umlsupport.StringUtilities;
import org.netbeans.modules.uml.ui.swing.commondialogs.DefaultCommonDialogResource;
import org.netbeans.modules.uml.ui.swing.commondialogs.JCenterDialog;
import org.openide.util.NbBundle;

/**
 * @author jingmingm
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class SwingDeleteWithAlso extends JCenterDialog implements ItemListener
{
	private boolean m_cancled = true;
	private boolean m_affectModel = true;
	private boolean m_also = false;
	private boolean m_never = false;
	private JLabel m_LabelMessage = null;
	private JCheckBox m_CheckboxData = null;
	private JCheckBox m_checkBoxAlso = null;
	private JCheckBox m_CheckboxNever = null;
	protected JButton m_No = null;
	protected JButton m_Yes = null;
   
   private boolean m_bDeleteAlso = false;
	
	public SwingDeleteWithAlso()
	{
		init( true );
	}
	public SwingDeleteWithAlso( String szDeleteConnectorMessages, String szAlsoQuestion, JDialog pParent ) {
            init( szDeleteConnectorMessages.equals("PSK_ASK") );
            
            setCheckboxText(m_checkBoxAlso, szAlsoQuestion);
            
            if( szDeleteConnectorMessages.equals("PSK_ALWAYS") ) {
                m_checkBoxAlso.setSelected( true );
            } else if( szDeleteConnectorMessages.equals("PSK_NEVER") ) {
                m_checkBoxAlso.setSelected( false );
            }
        }
	
	protected void init( boolean bDisplayExtraCheckboxes )
	{
		getContentPane().setLayout(new BorderLayout());
		
		m_LabelMessage = new JLabel( NbBundle.getMessage(SwingDeleteWithAlso.class, "DELETE_GRAPH_OBJECTS_MESSAGE") );
		String text = NbBundle.getMessage(SwingDeleteWithAlso.class, "DELETE_ELEMENTS_QUESTION");
		m_CheckboxData = new JCheckBox();
		setCheckboxText(m_CheckboxData, text);
		m_CheckboxData.setSelected(true);
		m_CheckboxData.addItemListener(this);
		m_checkBoxAlso = new JCheckBox();
		m_checkBoxAlso.setSelected(false);
		m_checkBoxAlso.addItemListener(this);
      m_checkBoxAlso.setVisible( bDisplayExtraCheckboxes );
		m_CheckboxNever = new JCheckBox();
		m_CheckboxNever.setSelected(false);
		String dontShow = DefaultCommonDialogResource.getString("IDS_DONT_SHOW");
		setCheckboxText(m_CheckboxNever, dontShow);
		m_CheckboxNever.addItemListener(this);
      m_CheckboxNever.setVisible( bDisplayExtraCheckboxes );
		Box checkBoxes = Box.createVerticalBox();
		checkBoxes.add(Box.createVerticalStrut(6));
		checkBoxes.add(m_LabelMessage);
		checkBoxes.add(Box.createVerticalStrut(6));
		checkBoxes.add(m_CheckboxData);
		checkBoxes.add(Box.createVerticalStrut(3));
		checkBoxes.add(m_checkBoxAlso);
		checkBoxes.add(Box.createVerticalStrut(3));
		//Box neverBox = Box.createHorizontalBox();
		//neverBox.add(Box.createHorizontalStrut(3));
		//neverBox.add(m_CheckboxNever);
		//checkBoxes.add(neverBox);
		checkBoxes.add(m_CheckboxNever);
		getContentPane().add(checkBoxes, BorderLayout.CENTER);
		
		// Add buttons
		JPanel buttonPanel = new JPanel();
		getContentPane().add(buttonPanel, BorderLayout.SOUTH);
		String yesStr = DefaultCommonDialogResource.getString("IDS_YES");
		m_Yes = new JButton();
		setButtonText(m_Yes, yesStr);
		buttonPanel.add(m_Yes, BorderLayout.WEST);
		m_Yes.addActionListener
		(
			new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					m_cancled = false;
					dispose();
				}
			}
		);
		
		String noStr = DefaultCommonDialogResource.getString("IDS_NO");
		m_No = new JButton();
		setButtonText(m_No, noStr);
		buttonPanel.add(m_No, BorderLayout.EAST);
		m_No.addActionListener
		(
			new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					m_cancled = true;
					dispose();
				}
			}
		);
		setTitle(NbBundle.getMessage(SwingDeleteWithAlso.class, "DELETE_QUESTIONDIALOGTITLE"));
		setSize(520, 180);
		setModal(true);
	}

   public void itemStateChanged(ItemEvent e)
   {
		Object source = e.getItemSelectable();
		if (source == m_CheckboxData)
		{
			m_affectModel = m_CheckboxData.isSelected();
		}
		else if (source == m_checkBoxAlso)
		{
			m_also = m_checkBoxAlso.isSelected();
		}    
		else if (source == m_CheckboxNever)
		{
			m_never = m_CheckboxNever.isSelected();
		}    
   }

	public boolean getCanceled()
	{
		return m_cancled;
	}
	
	public boolean getDeleteModel()
	{
		return m_affectModel;	
	}
	
	public void setDeleteModel(boolean affectDataModel)
	{
		m_CheckboxData.setSelected(affectDataModel);	
	}
	
	public boolean getAlso()
	{
		return m_also;
	}
	
	public boolean getNever()
	{
		return m_never;
	}
	private void setCheckboxText(JCheckBox box, String text)
	{
		String checkboxText = text;
		String under = "";
		int pos = text.indexOf('&');
		if (pos > -1)
		{
			under = text.substring(pos + 1, pos + 2);
			checkboxText = StringUtilities.replaceAllSubstrings(text, "&", "");
		}
		box.setText( checkboxText );
		if (under.length() > 0)
		{
			box.setMnemonic(under.charAt(0));
		}
	}
	private void setButtonText(JButton button, String text)
	{
		String checkboxText = text;
		String under = "";
		int pos = text.indexOf('&');
		if (pos > -1)
		{
			under = text.substring(pos + 1, pos + 2);
			checkboxText = StringUtilities.replaceAllSubstrings(text, "&", "");
		}
		button.setText( checkboxText );
		if (under.length() > 0)
		{
			button.setMnemonic(under.charAt(0));
		}
	}
	
}
