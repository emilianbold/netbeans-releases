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
 * Created on Jun 9, 2003
 *
 */
package org.netbeans.modules.uml.ui.swing.finddialog;

import java.awt.Component;
import java.beans.PropertyChangeEvent;

import javax.swing.AbstractCellEditor;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

/**
 * @author sumitabhk
 *
 */
public class FindTableCellEditor extends AbstractCellEditor implements
		 TableCellEditor
{
	boolean focusChange = false;
	FindDialogUI m_UI = null;

	/**
	 *
	 */
	public FindTableCellEditor()
	{
		super();
	}
	public FindTableCellEditor(FindDialogUI ui)
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



