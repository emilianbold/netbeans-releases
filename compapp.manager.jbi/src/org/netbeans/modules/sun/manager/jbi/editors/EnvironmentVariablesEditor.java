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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import javax.management.openmbean.CompositeData;

/**
 *
 * @author jqian
 */
public class EnvironmentVariablesEditor extends SimpleTabularDataEditor {

    public EnvironmentVariablesEditor() {
    }

    @Override
    public java.awt.Component getCustomEditor() {
        return new EnvironmentVariablesCustomEditor(this);
    }
    
    @Override
    protected String getStringForRowData(CompositeData rowData) {
        Collection rowValues = rowData.values();
        
        // Mask out password
        
        List<String> visibleRowValues = new ArrayList<String>();
        visibleRowValues.addAll(rowValues);
        
        String type = (String) visibleRowValues.get(
                EnvironmentVariablesCustomEditor.TYPE_COLUMN);
        
        if (type.equals(EnvironmentVariablesCustomEditor.PASSWORD_TYPE)) {
            String password = (String) visibleRowValues.get(
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
    protected void validateRowData(String[] rowData) throws Exception {
        if (rowData == null || rowData.length != 3) {
            throw new RuntimeException("Illegal environment variable: "  + Arrays.toString(rowData) +
                    ". The format should be [<name>, <type>, <value>].");
        }
        
        String type = rowData[1];
        if (! type.equals(EnvironmentVariablesCustomEditor.STRING_TYPE) &&
                type.equalsIgnoreCase(EnvironmentVariablesCustomEditor.STRING_TYPE)) {
            type = rowData[1] = EnvironmentVariablesCustomEditor.STRING_TYPE;
        } else if (! type.equals(EnvironmentVariablesCustomEditor.NUMBER_TYPE) &&
                type.equalsIgnoreCase(EnvironmentVariablesCustomEditor.NUMBER_TYPE)) {
            type = rowData[1] = EnvironmentVariablesCustomEditor.NUMBER_TYPE;
        } else if (! type.equals(EnvironmentVariablesCustomEditor.BOOLEAN_TYPE) &&
                type.equalsIgnoreCase(EnvironmentVariablesCustomEditor.BOOLEAN_TYPE)) {
            type = rowData[1] = EnvironmentVariablesCustomEditor.BOOLEAN_TYPE;
        } else if (! type.equals(EnvironmentVariablesCustomEditor.PASSWORD_TYPE) &&
                type.equalsIgnoreCase(EnvironmentVariablesCustomEditor.PASSWORD_TYPE)) {
            type = rowData[1] = EnvironmentVariablesCustomEditor.PASSWORD_TYPE;
        }
                
        if (! type.equals(EnvironmentVariablesCustomEditor.STRING_TYPE) &&
                ! type.equals(EnvironmentVariablesCustomEditor.NUMBER_TYPE) &&
                ! type.equals(EnvironmentVariablesCustomEditor.BOOLEAN_TYPE) &&
                ! type.equals(EnvironmentVariablesCustomEditor.PASSWORD_TYPE)) {
            throw new RuntimeException("Illegal environment variable type: " + type + 
                    ". The only supported types are: STRING, NUMBER, BOOLEAN and PASSWORD.");
        }        
        
        String value = rowData[2]; 
        if (type.equals(EnvironmentVariablesCustomEditor.BOOLEAN_TYPE)) {
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
        
        if (type.equals(EnvironmentVariablesCustomEditor.NUMBER_TYPE)) {
            try {                
                Double.parseDouble(value);
            } catch (Exception e) {
                throw new RuntimeException("Invalid number: " + value);
            }
        }
        
        if (type.equals(EnvironmentVariablesCustomEditor.PASSWORD_TYPE)) {
            if (!value.matches("^\\*+$")) {
                throw new RuntimeException("Password is in clear text. Please use the custom editor to set password.");
            }
        }
    }
}
