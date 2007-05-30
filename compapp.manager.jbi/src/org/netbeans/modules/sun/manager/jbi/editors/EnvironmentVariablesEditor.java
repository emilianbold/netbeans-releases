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

import java.util.ArrayList;
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
            password = password.replaceAll(".", "*"); // NOI18N
            visibleRowValues.set(
                    EnvironmentVariablesCustomEditor.VALUE_COLUMN, password);
        }
        
        return visibleRowValues.toString();
    }
    
    @Override
    protected void validateRowData(String[] rowData) throws Exception {
        if (rowData == null || rowData.length != 3) {
            throw new RuntimeException("Illegal row data: "  + rowData);
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
