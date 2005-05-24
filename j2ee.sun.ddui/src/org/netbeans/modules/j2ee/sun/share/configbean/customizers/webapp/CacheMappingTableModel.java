/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
/*
 * CacheMappingTableModel.java
 *
 * Created on January 12, 2004, 6:51 PM
 */

package org.netbeans.modules.j2ee.sun.share.configbean.customizers.webapp;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.table.AbstractTableModel;
import javax.swing.event.EventListenerList;

import org.netbeans.modules.j2ee.sun.share.Constants;

import org.netbeans.modules.j2ee.sun.dd.api.web.CacheMapping;

import org.netbeans.modules.j2ee.sun.share.configbean.StorageBeanFactory;


/**
 *
 * @author Peter Williams
 */
public class CacheMappingTableModel extends AbstractTableModel {
	
	private static final ResourceBundle webappBundle = ResourceBundle.getBundle(
		"org.netbeans.modules.j2ee.sun.share.configbean.customizers.webapp.Bundle");	// NOI18N
		
	private static final String [] columnNames = { 
		webappBundle.getString("LBL_CacheTarget"),		// NOI18N
		webappBundle.getString("LBL_TargetValue"),		// NOI18N
		webappBundle.getString("LBL_CacheReference"), 	// NOI18N
		webappBundle.getString("LBL_ReferenceValue"), 	// NOI18N
	};
	
	private static final String [] cacheTargetType = { 
		webappBundle.getString("LBL_ServletName"),		// NOI18N
		webappBundle.getString("LBL_URLPattern"),		// NOI18N
	};
	
	private static final String [] cacheReferenceType = { 
		webappBundle.getString("LBL_CacheHelperReference"),		// NOI18N
		webappBundle.getString("LBL_CachePolicyDefinition"),	// NOI18N
	};

	/** List of listeners */
	protected EventListenerList listenerList = new EventListenerList();

	/** List of CacheMapping objects */
	private List rows;

	public CacheMappingTableModel() {
		rows = new ArrayList();
	}

	/* --------------------------------------------------------------------
	 *  Data manipulation
	 */
	public int addRow() {
		rows.add(StorageBeanFactory.getDefault().createCacheMapping_NoDefaults());
		int newIndex = rows.size()-1;
		fireTableRowsInserted(newIndex, newIndex);
		return newIndex;
	}

	public CacheMapping removeRow(int rowIndex) {
		CacheMapping removedMapping = null;
		
		if(rowIndex >= 0 && rowIndex < rows.size()) {
			removedMapping = (CacheMapping) rows.remove(rowIndex);
			fireTableRowsDeleted(rowIndex, rowIndex);
		}
		
		return removedMapping;
	}
	
	public void setData(List data) {
		if(data != null && data.size() > 0) {
			rows = new ArrayList(data);
		} else {
			rows = new ArrayList();
		}
	}
	
	public List getData() {
		return rows;
	}

	/* --------------------------------------------------------------------
	 *  Implementation of TableModel interface
	 */
//	public Class getColumnClass(int columnIndex) {
//		return String.class;
//	}

	public String getColumnName(int columnIndex) {
		assert(columnIndex >= 0 && columnIndex < 4);
		return columnNames[columnIndex];
	}

	public int getColumnCount() {
		return 4;
	}

	public int getRowCount() {
//		return (rows != null) ? rows.size() : 0;
		return rows.size();
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		Object result = null;

//		if(rows != null && rowIndex >= 0 && rowIndex < rows.size() && columnIndex >= 0 && columnIndex < 4) {
		if(rowIndex >= 0 && rowIndex < rows.size() && columnIndex >= 0 && columnIndex < 4) {
			CacheMapping mapping = (CacheMapping) rows.get(rowIndex);
			if(columnIndex < 2) {
				// cache target
				String servletName = mapping.getServletName();
				String urlPattern = mapping.getUrlPattern();
				if(columnIndex == 0) {
					result = (servletName != null) ? cacheTargetType[0] : cacheTargetType[1];
				} else {
					result = (servletName != null) ? servletName : urlPattern;
				}
			} else {
				// cache reference
				String helperRef = mapping.getCacheHelperRef();
				if(columnIndex == 2) {
					result = (helperRef != null) ? cacheReferenceType[0] : cacheReferenceType[1];
				} else {
					result = (helperRef != null) ? helperRef : webappBundle.getString("LBL_PolicyLabel");	// NOI18N
				}
			}
		}

		return result;
	}
}
