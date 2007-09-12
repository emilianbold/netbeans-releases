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

package org.netbeans.modules.sun.manager.jbi.editors;


import java.beans.PropertyEditorSupport;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.StringTokenizer;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.TabularData;
import javax.management.openmbean.TabularDataSupport;
import javax.management.openmbean.TabularType;
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
        
        if (value != null ) {
            assert value instanceof TabularData;
            
            TabularData tabularData = (TabularData)value;
            tabularType = tabularData.getTabularType();
            
            sb.append("{"); // NOI18N
            for (Object rowDataObj : tabularData.values()) {
                CompositeData rowData = (CompositeData) rowDataObj;
                String rowValues = getStringForRowData(rowData);
                sb.append(rowValues);
            }
            sb.append("}"); //NOI18N
        }
        
        return sb.toString();
    }
    
    protected String getStringForRowData(CompositeData rowData) {
        Collection rowValues = rowData.values();
        return rowValues.toString();
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
                String dataString = text.trim().replaceFirst("\\{\\s*\\[", ""). // NOI18N
                        replaceAll("\\]\\s*\\[", "\n").replaceFirst("\\]\\s*\\}", ""); // NOI18N
                StringTokenizer stringTokenizer = new StringTokenizer(dataString, "\n"); // NOI18N
                while (stringTokenizer.hasMoreTokens()) {
                    String rowString = stringTokenizer.nextToken();
                    
                    List<String> itemList = new ArrayList<String>();
                    StringTokenizer rowStringTokenizer = new StringTokenizer(rowString, ","); // NOI18N
                    while (rowStringTokenizer.hasMoreTokens()) {
                        itemList.add(rowStringTokenizer.nextToken().trim());
                    }
                    String[] itemValues = itemList.toArray(new String[itemList.size()]);
                    
                    validateRowData(itemValues);
                    
                    CompositeData rowData =
                            new CompositeDataSupport(rowType, columnNames, itemValues);
                    tabularData.put(rowData);
                }
            }
            
            setValue(tabularData);
            
        } catch (Exception e) {
            NotifyDescriptor d = new NotifyDescriptor.Message(e.getMessage(),
                    NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(d);
        }
    }
    
    protected void validateRowData(String[] rowData) throws Exception {
        ; // no-op
    }

    @Override    
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
