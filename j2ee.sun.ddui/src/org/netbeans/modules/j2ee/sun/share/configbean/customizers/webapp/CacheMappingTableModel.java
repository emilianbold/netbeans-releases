/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
 * CacheMappingTableModel.java
 *
 * Created on January 12, 2004, 6:51 PM
 */

package org.netbeans.modules.j2ee.sun.share.configbean.customizers.webapp;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.table.AbstractTableModel;

import org.netbeans.modules.j2ee.sun.dd.api.ASDDVersion;
import org.netbeans.modules.j2ee.sun.dd.api.web.CacheMapping;

import org.netbeans.modules.j2ee.sun.share.configbean.StorageBeanFactory;
import org.openide.util.NbBundle;


/**
 *
 * @author Peter Williams
 */
public class CacheMappingTableModel extends AbstractTableModel {
	
	private final ResourceBundle webappBundle = NbBundle.getBundle(
		"org.netbeans.modules.j2ee.sun.share.configbean.customizers.webapp.Bundle");	// NOI18N
		
	private final String [] columnNames = { 
		webappBundle.getString("LBL_CacheTarget"),		// NOI18N
		webappBundle.getString("LBL_TargetValue"),		// NOI18N
		webappBundle.getString("LBL_CacheReference"), 	// NOI18N
		webappBundle.getString("LBL_ReferenceValue"), 	// NOI18N
	};
	
	private final String [] cacheTargetType = { 
		webappBundle.getString("LBL_ServletName"),		// NOI18N
		webappBundle.getString("LBL_URLPattern"),		// NOI18N
	};
	
	private final String [] cacheReferenceType = { 
		webappBundle.getString("LBL_CacheHelperReference"),		// NOI18N
		webappBundle.getString("LBL_CachePolicyDefinition"),	// NOI18N
	};

	/** List of CacheMapping objects */
	private List rows;
    
	/** Application Server version for selected data */
	private ASDDVersion appServerVersion;

	public CacheMappingTableModel() {
		rows = new ArrayList();
	}

	/* --------------------------------------------------------------------
	 *  Data manipulation
	 */
	public int addRow() {
		rows.add(StorageBeanFactory.getStorageBeanFactory(appServerVersion).createCacheMapping_NoDefaults());
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
	
	public void setData(List data, ASDDVersion asVersion) {
		appServerVersion = asVersion;
        
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
