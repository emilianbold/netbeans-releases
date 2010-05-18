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
 * Created on Jun 10, 2003
 *
 */
package org.netbeans.modules.uml.ui.swing.propertyeditor;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.List;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Collection;
import java.util.Iterator;
import java.util.TreeMap;

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;

import org.netbeans.modules.uml.ui.support.ProductHelper;
import org.netbeans.modules.uml.ui.swing.commondialogs.JCenterDialog;

/**
 * @author sumitabhk
 *
 */
public class PropertyEditorFilterDialog extends JCenterDialog
{
	private List 						 m_List = null;
	private JButton                m_OKBtn      = null;
	private JButton                m_CancelBtn  = null;
	private PropertyEditorFilter 	 m_Filter = null;
	GridBagConstraints gridbagConstraints=null;
	JPanel pnlBottom=null;
	JPanel pnlTop=null;

	/**
	 *
	 */
	public PropertyEditorFilterDialog()
	{
		super();
		setTitle(PropertyEditorResources.getString("PropertyEditorFilterDialog.Property_Filter_Dialog_Title")); //$NON-NLS-1$
		setModal(true);
		setSize(300, 450);
		center(ProductHelper.getProxyUserInterface().getWindowHandle());
	}

	/**
	 *
	 */
	public void loadFilterDialog(PropertyEditorFilter filter)
	{
		if (filter != null)
		{
			m_Filter = filter;
			TreeMap<String, String> map = new TreeMap<String, String>();
			Iterator commonMap = filter.getCommonMapIter();
			for (; commonMap.hasNext();)
			{
				Object obj = commonMap.next();
			   String value = obj.toString();
			   map.put(value, value);
			}

			Iterator otherMap = filter.getOtherMapIter();
			for (; otherMap.hasNext();)
			{
				Object obj = otherMap.next();
				String value = obj.toString();
				map.put(value, value);
			}

			Collection col = map.values();
			if (col != null)
			{
				int size = col.size();
				m_List = new List(size);
				Iterator i = col.iterator();
				for (; i.hasNext();)
				{
					Object obj = i.next();
					String value = obj.toString();
					m_List.add(value);
				}
			}

			getContentPane().setLayout(new GridBagLayout());
			gridbagConstraints=new GridBagConstraints();

			pnlTop=new JPanel();
			pnlTop.setLayout(new GridBagLayout());
			gridbagConstraints.gridx=0;
			gridbagConstraints.gridy=0;
			gridbagConstraints.fill=GridBagConstraints.BOTH;
			gridbagConstraints.weightx=1;
			gridbagConstraints.weighty=10;
			pnlTop.add(m_List,gridbagConstraints);
			gridbagConstraints.gridx=0;
			gridbagConstraints.gridy=0;
			gridbagConstraints.fill=GridBagConstraints.BOTH;
			gridbagConstraints.weightx=1;
			gridbagConstraints.weighty=10;
			gridbagConstraints.insets=new Insets(10,10,5,10);
			getContentPane().add(pnlTop,gridbagConstraints);


			//Dimension buttonSize = new Dimension(75, 25);
			m_OKBtn = new JButton(new OKAction(this));
			//m_OKBtn.setPreferredSize(buttonSize);
			//m_OKBtn.setMaximumSize(buttonSize);
			getRootPane().setDefaultButton(m_OKBtn);
			m_CancelBtn = new JButton(new CancelAction());
			//m_CancelBtn.setPreferredSize(buttonSize);
			//m_CancelBtn.setMaximumSize(buttonSize);

			pnlBottom=new JPanel();
			pnlBottom.setLayout(new GridBagLayout());
			Box buttonBox = Box.createHorizontalBox();
            buttonBox.add(Box.createHorizontalGlue());
            buttonBox.add(m_OKBtn);
            buttonBox.add(Box.createHorizontalStrut(5));
            buttonBox.add(m_CancelBtn);
            buttonBox.add(Box.createHorizontalStrut(0));// For Creating Spaces
            gridbagConstraints.gridx=0;
            gridbagConstraints.gridy=0;
            gridbagConstraints.weightx=1.0;
            gridbagConstraints.weighty=1.0;
            gridbagConstraints.fill=GridBagConstraints.BOTH;
            pnlBottom.add(buttonBox,gridbagConstraints);

			Dimension buttonSize = getMaxButtonWidth();
			m_OKBtn.setMaximumSize(buttonSize);
			m_OKBtn.setPreferredSize(buttonSize);
			m_CancelBtn.setMaximumSize(buttonSize);
			m_CancelBtn.setPreferredSize(buttonSize);

			gridbagConstraints.gridx=0;
			gridbagConstraints.gridy=1;
			gridbagConstraints.fill=GridBagConstraints.BOTH;
			gridbagConstraints.weightx=1;
			gridbagConstraints.weighty=0.1;
			gridbagConstraints.insets=new Insets(5,10,10,0);
			getContentPane().add(pnlBottom,gridbagConstraints);


			//get

			/*panel.setBorder(new EmptyBorder(10, 10, 5, 10));
			panel.setLayout(new BorderLayout());

			panel.add(m_List);

			Box buttonPane = Box.createHorizontalBox();
			buttonPane.add(Box.createHorizontalGlue());
			buttonPane.add(m_OKBtn);
			buttonPane.add(Box.createHorizontalStrut(5));
			buttonPane.add(m_CancelBtn);
			panel.add(buttonPane, BorderLayout.SOUTH);

			buttonPane.setBorder(new EmptyBorder(5, 0, 0, 0));
			getContentPane().add(panel);*/


		}
	}
	private Dimension getMaxButtonWidth()
	{
		Dimension ret = null;
		Dimension d = m_OKBtn.getPreferredSize();
		double max  = d.width;

		d = m_CancelBtn.getPreferredSize();
		if(d.width > max){
			 max = d.width;
			 ret = d;
		}

		return ret;

	}
	/**
	 * The action that performs the OK button action.  The users changes will
	 * be saved.
	 */
	protected class OKAction extends AbstractAction
	{
		private PropertyEditorFilterDialog m_Dialog = null;

		public OKAction(PropertyEditorFilterDialog dialog)
		{
			super(PropertyEditorResources.getString("IDS_OK"));
			m_Dialog = dialog;
		}

		/**
		 * Saves the users changes and closes the window.
		 *
		 * @param e The event data.
		 */
		public void actionPerformed(ActionEvent e)
		{
			String selText = m_List.getSelectedItem();
			if (m_Filter != null)
			{
				m_Filter.setCurrentSelection(selText);
			}
			hide();
			dispose();
		}
	}

	/**
	 * The action that performs the cancel button action.  The users changes will
	 * be discarded.
	 */
	public class CancelAction extends AbstractAction
	{
		public CancelAction()
		{
			super(PropertyEditorResources.getString("IDS_CANCEL"));
		}

		/**
		 * Saves the users changes and closes the window.
		 *
		 * @param e The event data.
		 */
		public void actionPerformed(ActionEvent e)
		{
			hide();
			dispose();
		}
	}

}



