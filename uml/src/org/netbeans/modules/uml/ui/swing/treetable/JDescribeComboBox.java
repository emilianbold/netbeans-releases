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
 * Created on Jun 12, 2003
 *
 */
package org.netbeans.modules.uml.ui.swing.treetable;

import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JComboBox;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.table.TableCellEditor;

import org.netbeans.modules.uml.common.ETSystem;
import org.netbeans.modules.uml.ui.swing.propertyeditor.PropertyEditor;

/**
 * @author sumitabhk
 *
 */
public class JDescribeComboBox extends JComboBox
{
	PropertyEditor m_Editor = null;
	/**
	 * 
	 */
	public JDescribeComboBox()
	{
		super();
		setEditable(true);
		setUI(new NewBasicComboBoxUI());
	}
	
	public JDescribeComboBox(PropertyEditor editor)
	{
		super();
		setEditable(true);
		setRequestFocusEnabled(true);
		setFocusable(true);
		setLightWeightPopupEnabled(false);
		setUI(new NewBasicComboBoxUI());
		m_Editor = editor;

		//Need to add the key listener to the editor of this combo box, else I am not getting key
		//event after I type in something.
		getEditor().getEditorComponent().addKeyListener(new KeyListener() {

			public void keyTyped(KeyEvent e) 
			{
			}

			public void keyPressed(KeyEvent e) 
			{
				if (m_Editor != null)
				{
					Object obj = m_Editor.getGrid();
					if (obj != null && obj instanceof JTreeTable)
					{
						TableCellEditor editor = ((JTreeTable)obj).getCellEditor();
						if (editor != null && editor instanceof TreeTableCellEditor)
						{
							((TreeTableCellEditor)editor).handleKeyPress(e);
						}
					}
				}
			}

			public void keyReleased(KeyEvent e) 
			{
			}
		});
		
		getEditor().getEditorComponent().addFocusListener(new FocusListener() {

			public void focusGained(FocusEvent e)
			{
			}

			public void focusLost(FocusEvent e)
			{
				if (e.getOppositeComponent() == null)
				{
					ETSystem.out.println("Focus lost on null");
					// if the component that is taking the focus away from the property editor is null
					// then we are ASSUMING that it is another application
					if (m_Editor != null)
					{
						m_Editor.handleSave(true, null);
						m_Editor.setEditingComponent(null);
					}
				}
			}
		});
		
	}
	
	public void actionPerformed(ActionEvent e)
	{
		super.actionPerformed(e);
		
		//when Enter key is pressed, we get this event - in Property Editor we want to commit the change
		//made at this point, so extra code.
		if (m_Editor != null)
		{
			Object obj = m_Editor.getGrid();
			if (obj != null && obj instanceof JTreeTable)
			{
				TableCellEditor editor = ((JTreeTable)obj).getCellEditor();
				if (editor != null && editor instanceof TreeTableCellEditor)
				{
					//((TreeTableCellEditor)editor).handleKeyPress(new KeyEvent(this, KeyEvent.KEY_PRESSED, 0, 0, KeyEvent.VK_ENTER, KeyEvent.CHAR_UNDEFINED));
				}
			}
		}
	}

	//	our extended BasicComboBoxUI class
 	class NewBasicComboBoxUI extends BasicComboBoxUI
	 {
		 /* to catch hold of the focuslistener set in the  BasicComboBoxUI */

		 //FocusListener prevFocusListener;
		 KeyListener prevKeyListener;

		 public NewBasicComboBoxUI()
		 {
			 super();
			 // catch it here after being installed
			 //prevFocusListener = focusListener;
			 prevKeyListener = keyListener;
		 }

//		 protected FocusListener createFocusListener()
//		 {
//			 prevFocusListener = super.createFocusListener();
//			 return new MyFocusHandler();
//		 }
		 
		 protected KeyListener createKeyListener()
		 {
			prevKeyListener = super.createKeyListener();
			return new MyKeyHandler();
		 }

		public class MyKeyHandler implements KeyListener
		{

			/* (non-Javadoc)
			 * @see java.awt.event.KeyListener#keyTyped(java.awt.event.KeyEvent)
			 */
			public void keyTyped(KeyEvent e) 
			{
			}

			/* (non-Javadoc)
			 * @see java.awt.event.KeyListener#keyPressed(java.awt.event.KeyEvent)
			 */
			public void keyPressed(KeyEvent e) 
			{
//				if (m_Editor != null)
//				{
//					Object obj = m_Editor.getGrid();
//					if (obj != null && obj instanceof JTreeTable)
//					{
//						TableCellEditor editor = ((JTreeTable)obj).getCellEditor();
//						if (editor != null && editor instanceof TreeTableCellEditor)
//						{
//							((TreeTableCellEditor)editor).handleKeyPress(e);
//						}
//					}
//				}
			}

			/* (non-Javadoc)
			 * @see java.awt.event.KeyListener#keyReleased(java.awt.event.KeyEvent)
			 */
			public void keyReleased(KeyEvent e) 
			{
			}
			
		}

//		public class MyFocusHandler implements FocusListener
//		{
//			public void focusLost(FocusEvent e)
//			{
//				//when tab key is pressed - we directly get a focusLost and no KeyEvents for the editable
//				//combo box - so I have to commit the edit mode of this combo field and I am forcing the
//				//user to start editing the next field. So if the user actually pressed Shift+Tab, things
//				//will appear wrong. If the user is actually clicking on any other field in the TreeTable
//				//using mouse he will be fine, as I am doing invokeLater to start editing that field.
//				 if (m_Editor != null)
//				 {
//				 	Object source = e.getSource();
//				 	Component opp = e.getOppositeComponent();
//				 	ETSystem.out.println("Got Focus Lost for comp - " + source);
//					ETSystem.out.println("Got Focus Lost for comp opp - " + opp);
//				 	if (opp != null && !e.isTemporary() && source instanceof JTextField)
//				 	{
//						Object obj = m_Editor.getGrid();
//						if (obj != null && obj instanceof JTreeTable)
//						{
//							TableCellEditor editor = ((JTreeTable)obj).getCellEditor();
//							if (editor != null && editor instanceof TreeTableCellEditor)
//							{
//								editor.stopCellEditing();
//								((JTreeTable)obj).editingCanceled(null);
//								m_Editor.editNextRow();
//							}
//						}
//				 	}
//				 }
//			}
//
//		   public void focusGained(FocusEvent e)
//		   {
//				if (m_Editor != null)
//				{
//				   //m_Editor.handleFocusGainedOnCellEvent(e);
//				}
//		   }
//		}

	 }

}



