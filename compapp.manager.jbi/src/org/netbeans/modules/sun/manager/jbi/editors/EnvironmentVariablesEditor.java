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

import java.awt.Component;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.TabularType;
import org.netbeans.modules.sun.manager.jbi.management.model.JBIComponentConfigurationDescriptor;
import org.openide.util.NbBundle;

/**
 * A property editor for JBI Component's Environment Variables TabularData or 
 * Application Variables TabularData.
 * 
 * @author jqian
 */
public class EnvironmentVariablesEditor extends SimpleTabularDataEditor {
            
    /**
     * Constructs a Environment Variables / Application Variables property editor.
     * 
     * @param isAppVar  <code>true</code> for Application Variables; 
     *                  <code>false</code> for Environment Variables.
     * @param tabularType   the type of the tabular data
     */
    public EnvironmentVariablesEditor(boolean isAppVar, TabularType tabularType,
            JBIComponentConfigurationDescriptor descriptor, boolean isWritable) {
        super(NbBundle.getMessage(EnvironmentVariablesEditor.class, 
                isAppVar ? "LBL_APPLICATION_VARIABLES_TABLE" :  // NOI18N 
                    "LBL_ENVIRONMENT_VARIABLES_TABLE"),  // NOI18N 
              NbBundle.getMessage(EnvironmentVariablesEditor.class, 
                isAppVar ? "ACS_APPLICATION_VARIABLES_TABLE" :  // NOI18N 
                    "ACS_ENVIRONMENT_VARIABLES_TABLE"),  // NOI18N
              tabularType, descriptor, isWritable); 
    }

    @Override
    public Component getCustomEditor() {
        customEditor = new EnvironmentVariablesCustomEditor(this,
                tableLabelText, tableLabelDescription, 
                descriptor, isWritable);
        return customEditor;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    protected String getStringForRowData(CompositeData rowData) {
        Collection rowValues = rowData.values();
        
        // Mask out password
        
        List<String> visibleRowValues = new ArrayList<String>();
        visibleRowValues.addAll(rowValues);
        
        String type = visibleRowValues.get(
                EnvironmentVariablesCustomEditor.TYPE_COLUMN);
        
        if (type.equals(ApplicationVariableType.PASSWORD.toString())) {
            String password = visibleRowValues.get(
                    EnvironmentVariablesCustomEditor.VALUE_COLUMN);
            if (password != null) {
                password = password.replaceAll(".", "*"); // NOI18N
            }
            visibleRowValues.set(
                    EnvironmentVariablesCustomEditor.VALUE_COLUMN, password);
        }
        
        return visibleRowValues.toString();
    }
    
    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        assert false; // see attachEnv
    } 
        
    /*
    @Override
    protected void validateRowData(String[] rowData) throws Exception {
        if (rowData == null || rowData.length != 3) {
            throw new RuntimeException("Illegal environment variable: "  + Arrays.toString(rowData) +
                    ". The format should be [<name>, <type>, <value>].");
        }
        
        String type = rowData[1];
        if (! type.equals(ApplicationVariableType.STRING.toString()) &&
                type.equalsIgnoreCase(ApplicationVariableType.STRING.toString())) {
            type = rowData[1] = ApplicationVariableType.STRING.toString();
        } else if (! type.equals(ApplicationVariableType.NUMBER.toString()) &&
                type.equalsIgnoreCase(ApplicationVariableType.NUMBER.toString())) {
            type = rowData[1] = ApplicationVariableType.NUMBER.toString();
        } else if (! type.equals(ApplicationVariableType.BOOLEAN.toString()) &&
                type.equalsIgnoreCase(ApplicationVariableType.BOOLEAN.toString())) {
            type = rowData[1] = ApplicationVariableType.BOOLEAN.toString();
        } else if (! type.equals(ApplicationVariableType.PASSWORD.toString()) &&
                type.equalsIgnoreCase(ApplicationVariableType.PASSWORD.toString())) {
            type = rowData[1] = ApplicationVariableType.PASSWORD.toString();
        }
                
        if (! type.equals(ApplicationVariableType.STRING.toString()) &&
                ! type.equals(ApplicationVariableType.NUMBER.toString()) &&
                ! type.equals(ApplicationVariableType.BOOLEAN.toString()) &&
                ! type.equals(ApplicationVariableType.PASSWORD.toString())) {
            throw new RuntimeException("Illegal environment variable type: " + type + 
                    ". The only supported types are: STRING, NUMBER, BOOLEAN and PASSWORD.");
        }        
        
        String value = rowData[2]; 
        if (type.equals(ApplicationVariableType.BOOLEAN.toString())) {
            if (!value.equalsIgnoreCase(Boolean.TRUE.toString()) && 
                    !value.equalsIgnoreCase(Boolean.FALSE.toString()) ) {
                if (value.equals("0")) { // NOI18N
                    rowData[2] = Boolean.FALSE.toString();
                } else if (value.equals("1")) { // NOI18N
                    rowData[2] = Boolean.TRUE.toString();
                } else {
                    throw new RuntimeException("Illegal boolean value: " + value);
                }
            }
        }
        
        if (type.equals(ApplicationVariableType.NUMBER.toString())) {
            try {                
                Double.parseDouble(value);
            } catch (Exception e) {
                throw new RuntimeException("Invalid number: " + value);
            }
        }
        
        if (type.equals(ApplicationVariableType.PASSWORD.toString())) {
            if (!value.matches("^\\*+$")) {
                throw new RuntimeException("Password is in clear text. Please use the custom editor to set password.");
            }
        }
    }
    */
}
