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



package org.netbeans.modules.uml.ui.swing.preferencedialog;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.beans.PropertyChangeEvent;
import java.util.StringTokenizer;

import javax.swing.AbstractCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.TableCellEditor;

import org.netbeans.modules.uml.core.configstringframework.ConfigStringHelper;
import org.netbeans.modules.uml.core.configstringframework.IConfigStringTranslator;
import org.netbeans.modules.uml.core.coreapplication.ICoreProduct;
import org.netbeans.modules.uml.core.coreapplication.IPreferenceManager2;
import org.netbeans.modules.uml.core.support.umlsupport.ProductRetriever;
import org.netbeans.modules.uml.core.support.umlutils.IPropertyDefinition;
import org.netbeans.modules.uml.core.support.umlutils.IPropertyElement;
import org.netbeans.modules.uml.ui.swing.preferencedialog.JPreferenceDialogTable.PreferenceTableCellRenderer;
import org.netbeans.modules.uml.ui.swing.treetable.JDescribeDotButton;

/**
 * @author sumitabhk
 *
 */
public class PreferenceDialogTableCellEditor extends AbstractCellEditor 
											implements TableCellEditor
{
	boolean focusChange = false;
	PreferenceDialogUI m_UI = null;
	private int m_CurRow = 0;
	private JTable m_Table = null;
	private Component m_EditingComponent = null;
	/**
	 * 
	 */
	public PreferenceDialogTableCellEditor(PreferenceDialogUI ui)
	{
		super();
		m_UI = ui;
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.TableCellEditor#getTableCellEditorComponent(javax.swing.JTable, java.lang.Object, boolean, int, int)
	 */
	public Component getTableCellEditorComponent(JTable table, 
												 Object value, 
												 boolean isSelected, 
												 int row, 
												 int col)
	{
		Component retObj = null;
		m_Table = table;
		m_CurRow = row;

		//I want to get the PropertyElement at this row which is stored in col=0
		Object obj = table.getModel().getValueAt(row, 0);
		if (obj instanceof IPropertyElement)
		{
			IPropertyElement pEle = (IPropertyElement)obj;
			
			//I want to load the new help doc.
			m_UI.loadHelp(pEle);
			
			//if this is the values column, I want to edit it.
			if (col == 1)
			{
				IPropertyDefinition pDef = pEle.getPropertyDefinition();
				if (pDef != null)
				{
					String pDefName = pDef.getName();
					int pos = pDefName.indexOf("Resources");
					int pos2 = pDefName.indexOf("Font");
					if (pos == 0 && pDefName.length() == 9)
					{
						PreferenceDialogUI ui = ((JPreferenceDialogTable)table).getPreferenceDialogUI();
						String buttonText = (String)table.getValueAt(row, col);
						JDescribeDotButton btn = new JDescribeDotButton(row, ui, buttonText);
						btn.setBorder(null);
						retObj = btn;
					}
					else if (pos2 > -1)
					{
						PreferenceDialogUI ui = ((JPreferenceDialogTable)table).getPreferenceDialogUI();
						String buttonText = (String)table.getValueAt(row, col);
						Font f = ui.buildCurrentFont(pEle);
						JDescribeDotButton btn = new JDescribeDotButton(row, ui, buttonText, f, null);
						btn.setBorder(null);
						retObj = btn;
					}
					else
					{
						String values = pDef.getValidValues();
						IConfigStringTranslator translator = ConfigStringHelper.instance().getTranslator();
						if (values != null)
						{
							JComboBox temp = new JComboBox();
							//temp.setEditable(true);
							temp.setPopupVisible(false);
							temp.setBorder(null);
                                                        temp.setFont(new java.awt.Font("Dialog", 0, 11));
							StringTokenizer tokenizer = new StringTokenizer(values, "|");
							while (tokenizer.hasMoreTokens())
							{
								String transVal = translator.translate(pDef, tokenizer.nextToken());
								temp.addItem(transVal);
							}
							
							String eleVal = pEle.getValue();
							if (eleVal != null)
							{
								eleVal = translator.translate(pDef, eleVal);
								temp.setSelectedItem(eleVal);
							}
							else
							{
								temp.setSelectedItem(value);
							}
							retObj = temp;
						}
						else
						{
							JTextField temp = new JTextField();
							temp.setBorder(null);
							temp.setFont(new java.awt.Font("Dialog", 0, 11));
							String eleVal = pEle.getValue();
							if (eleVal != null)
							{
								eleVal = translator.translate(pDef, eleVal);
								temp.setText(eleVal);
								temp.setEditable(true);
							}
							else if (value != null)
							{
								temp.setText(value.toString());
								temp.setEditable(true);
							}
							retObj = temp;
						}
					}
					if (retObj != null)
					{
						IPreferenceManager2 prefMan = ProductRetriever.retrieveProduct().getPreferenceManager();
						if (prefMan != null)
						{
							if (prefMan.isEditable(pEle))
							{
								retObj.setEnabled(true);
							}
							else
							{
								retObj.setEnabled(false);
							}
						}
					}
				}
			}
		}
		
		//save the editing component so that it can be used while saving.
		m_EditingComponent = retObj;
//		if (retObj != null && retObj.isEnabled())
//		{
//			retObj.addPropertyChangeListener(new PropertyChangeListener()
//			{
//
//				public void propertyChange(PropertyChangeEvent arg0)
//				{
//					columnValueChanged(arg0);
//				}
//			});
//		}
		return retObj;
	}

	/* (non-Javadoc)
	 * @see javax.swing.CellEditor#getCellEditorValue()
	 */
	public Object getCellEditorValue()
	{
		// TODO Auto-generated method stub
		return null;
	}

	private void columnValueChanged(PropertyChangeEvent e)
	{
		try {
			Object obj = e.getSource();
			String str = e.getPropertyName();
			if (str.equals("ancestor"))
			{
				if (!focusChange)
				{
					Object newVal = e.getOldValue();
					if (newVal != null && newVal instanceof JPreferenceDialogTable)
					{
						JPreferenceDialogTable table = (JPreferenceDialogTable)newVal;
						int row = table.getSelectedRow();
						
						//get property element at this row and the new value
						Object propEle = table.getValueAt(row, 0);
						
						if (propEle instanceof IPropertyElement)
						{
							IPropertyElement pEle = (IPropertyElement)propEle;
							String oldVal = pEle.getValue();
							String value = "";
						
							if (obj instanceof JTextField)
							{
								JTextField field = (JTextField)obj;
								value = field.getText();
							}
							else if (obj instanceof JComboBox)
							{
								JComboBox field = (JComboBox)obj;
								value = (String)field.getSelectedItem();
								IConfigStringTranslator trans = ConfigStringHelper.instance().getTranslator();
								IPropertyDefinition pDef = pEle.getPropertyDefinition();
								value = trans.translateIntoPSK(pDef, value);
							}

							if (oldVal != null && value != null && !oldVal.equals(value))
							{
								//we need to set the new value
								pEle.setModified(true);
								pEle.setValue(value);
							}
						}
					}
				}
				else
				{
					focusChange = false;
				}
			}
			else if (str.equals("nextFocus"))
			{
				focusChange = true;
			}
			
		}catch (Exception ep)
		{
			ep.printStackTrace();
		}
	}

	public boolean stopCellEditing()
	{
		if (m_Table != null)
		{
			Object obj = m_Table.getModel().getValueAt(m_CurRow, 0);
			if (obj instanceof IPropertyElement)
			{
				IPropertyElement pEle = (IPropertyElement)obj;
				String oldVal = pEle.getValue();
				if (oldVal == null){
					oldVal = "";
				}
				String value = "";
						
				if (m_EditingComponent instanceof JTextField)
				{
					JTextField field = (JTextField)m_EditingComponent;
					value = field.getText();
				}
				else if (m_EditingComponent instanceof JComboBox)
				{
					JComboBox field = (JComboBox)m_EditingComponent;
					value = (String)field.getSelectedItem();
					IConfigStringTranslator trans = ConfigStringHelper.instance().getTranslator();
					IPropertyDefinition pDef = pEle.getPropertyDefinition();
					String newvalue = trans.translateIntoPSK(pDef, value);
					if (newvalue != null && newvalue.length() > 0){
						value = newvalue;
					}
				}
				else if (m_EditingComponent instanceof JDescribeDotButton)
				{
					value = oldVal;
				}

				boolean change = true;
				IPropertyDefinition pDef = pEle.getPropertyDefinition();
				if (pDef != null)
				{
					// if the user has blanked out the value in the grid, we need to do additional checking that
					// the preference is not required (ie. cannot have a value of blank)
					if (value == null || value.length() == 0)
					{
						boolean isReqd = pDef.isRequired();
						if (isReqd)
						{
							// cannot have a value of blank and cannot be deleted
							// so get the original value of the preference and reset the grid back
							String origValue = pEle.getOrigValue();
							change = false;
						}
					}
					// we are going to allow the change to go through
					if (change)
					{
						// another check to see if the change should go through - we want to see if there
						// is a validate method on the definition
						// if there is, then we want to do some additional calls to make sure what we are setting
						// it to is valid per the object in the validate method
						String validM = pDef.getFromAttrMap("validate");
						boolean isValid = true;
						if (validM != null && validM.length() > 0)
						{
							// turn into a class id
							ICoreProduct product = ProductRetriever.retrieveProduct();
							if(product != null)
							{
								try
								{
									Class clazz = Class.forName(validM);
									Object actual = clazz.newInstance();
									if (actual != null)
									{
										// invoke the validate method on this class
										Class[] params = new Class[3];
										params[0] = Object.class;
										params[1] = String.class;
										params[2] = String.class;
										java.lang.reflect.Method method = clazz.getMethod("validate", params);
										// right now only going to figure out one, but someday we should parse this
										// string for delimiters
										Object[] args = new Object[3];
										args[0] = pEle;
										args[1] = pDef.getName();
										args[2] = value;
										Object result = method.invoke(actual, args);
										if (result.getClass() == Boolean.class)
										{
											if (result.toString().equals("true"))
											{
												Class[] params2 = new Class[1];
												params2[0] = Object.class;
												java.lang.reflect.Method method2 = clazz.getMethod("whenValid", params2);
												Object[] args2 = new Object[1];
												args2[0] = pEle;
												Object result2 = method2.invoke(actual, args2);
												isValid = true;
											}
											else
											{
												Class[] params2 = new Class[1];
												params2[0] = Object.class;
												java.lang.reflect.Method method2 = clazz.getMethod("whenInvalid", params2);
												Object[] args2 = new Object[1];
												args2[0] = pEle;
												Object result2 = method2.invoke(actual, args2);
												isValid = false;
											}
										}
									}
								}
								catch (Exception e)
								{
									isValid = false;
								}
							}
						}
						//
						//
						//
						if (isValid)
						{
							if (oldVal != null && value != null && !oldVal.equals(value))
							{
								// set the value on the property element
								//we need to set the new value
								pEle.setModified(true);
								pEle.setValue(value);
							}
						}
						else
						{
							//CComBSTR origValue;
							//pEle->get_OrigValue(&origValue);
							//m_Grid->put_TextMatrix(Row, Col, origValue);
						}
					}
				}
			}
		}
		m_EditingComponent = null;
		m_Table = null;
		return super.stopCellEditing();
	}
	
	public void cancelCellEditing()
	{
		m_EditingComponent = null;
		m_Table = null;
		super.cancelCellEditing();
	}
}



