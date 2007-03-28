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

package org.netbeans.modules.websvc.design.view.widget;

import java.util.Collections;
import org.netbeans.modules.xml.wsdl.model.Input;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.Part;

/**
 *
 * @author Ajit
 */
public class ParametersTableModel implements TableModel{
    
    private transient Input input;
    
    
    /**
     *
     * @param input
     */
    public ParametersTableModel(Input input) {
        this.input = input;
        
    }
    
    public int getRowCount() {
        Message message = null;
        if(input!=null && input.getMessage()!=null && ((message=input.getMessage().get())!=null)) {
            return message.getParts().size();
        }
        return 0;
    }
    
    public int getColumnCount() {
        return 2;
    }
    
    public String getColumnName(int columnIndex) {
        switch(columnIndex) {
        case 0:
            return "Part Name";
        case 1:
            return "Part Element or Type";
        default:
            throw new IllegalArgumentException("");
        }
    }
    
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        switch(columnIndex) {
        case 0:
            return true;
        case 1:
            return false;
        default:
            return false;
        }
    }
    
    public String getValueAt(int rowIndex, int columnIndex) {
        if (rowIndex>=0 && rowIndex<getRowCount()) {
            Part part = Collections.list(Collections.enumeration(
                    input.getMessage().get().getParts())).get(rowIndex);
            switch(columnIndex) {
            case 0:
                return part.getName();
            case 1:
                String parameter = (part.getElement()!=null?""+part.getElement().getQName():
                    part.getType()!=null?""+part.getType().getQName():"");
                return parameter;
            default:
                throw new IllegalArgumentException("");
            }
        }
        return null;
    }
    
    public void setValueAt(String aValue, int rowIndex, int columnIndex) {
        if (rowIndex>=0 && rowIndex<getRowCount()) {
            Part part = Collections.list(Collections.enumeration(
                    input.getMessage().get().getParts())).get(rowIndex);
            switch(columnIndex) {
            case 0:
                if(!part.getName().equals(aValue)) {
                    part.getModel().startTransaction();
                    part.setName(aValue);
                    part.getModel().endTransaction();
                }
                break;
            case 1:
                throw new IllegalArgumentException("");
            default:
                throw new IllegalArgumentException("");
            }
        }
    }
    
}
