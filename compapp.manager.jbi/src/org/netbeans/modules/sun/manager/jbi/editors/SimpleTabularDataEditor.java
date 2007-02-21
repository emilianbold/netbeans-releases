/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.sun.manager.jbi.editors;

import java.beans.PropertyEditorSupport;
import java.util.Collection;
import java.util.StringTokenizer;
import java.util.Vector;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.TabularData;
import javax.management.openmbean.TabularDataSupport;
import javax.management.openmbean.TabularType;
import javax.swing.table.DefaultTableModel;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;


/**
 * A property editor for some basic TabularData support.
 *
 * @author jqian
 */
public class SimpleTabularDataEditor extends PropertyEditorSupport {
    
    private TabularType tabularType;
    
    @Override
    public String getAsText() {
        Object value = getValue();
        StringBuilder sb = new StringBuilder();
        
        if (value instanceof TabularData) {
            TabularData tabularData = (TabularData)value;
            tabularType = tabularData.getTabularType();
            
            sb.append("{"); // NOI18N
            for (Object rowDataObj : tabularData.values()) {
                CompositeData rowData = (CompositeData) rowDataObj;
                Collection rowValues = rowData.values();
                sb.append(rowValues);
            }
            sb.append("}"); // NOI18N
        }
        
        return sb.toString();
    }
    
    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        
        // naive support for parsing TabularData from string like the following:
        // {[foo, bar][frodo, sam]}
        
        try {
            TabularData tabularData = new TabularDataSupport(tabularType);
            CompositeType rowType = tabularType.getRowType();
            String[] columnNames = (String[]) rowType.keySet().toArray(new String[] {});
            
            if (text != null) {
                String dataString = text.trim().replaceFirst("\\{\\s*\\[", "").replaceAll("\\]\\s*\\[", "\n").replaceFirst("\\]\\s*\\}", ""); // NOI18N
                StringTokenizer stringTokenizer = new StringTokenizer(dataString, "\n"); // NOI18N
                while (stringTokenizer.hasMoreTokens()) {
                    String rowString = stringTokenizer.nextToken();
                    
                    String[] itemValues = new String[columnNames.length];
                    int index = 0;
                    StringTokenizer rowStringTokenizer = new StringTokenizer(rowString, ","); // NOI18N
                    while (rowStringTokenizer.hasMoreTokens()) {
                        itemValues[index++] = rowStringTokenizer.nextToken().trim();
                    }
                    CompositeData rowData =
                            new CompositeDataSupport(rowType, columnNames, itemValues);
                    tabularData.put(rowData);
                }
            }
            
            setValue(tabularData);
            
        } catch (Exception e) {
            throw new java.lang.IllegalArgumentException(e.getMessage());
        }
    }
    
    public void setValue(Object v) {
        super.setValue(v);
    }
    
    @Override
    public String getJavaInitializationString() {
        return null; // does not generate any code
    }
    
    @Override
    public boolean supportsCustomEditor() {
        return true;
    }
    
    @Override
    public java.awt.Component getCustomEditor() {
        return new SimpleTabularDataCustomEditor(this);
    }
    
}
