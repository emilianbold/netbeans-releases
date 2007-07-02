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

package org.netbeans.modules.uml.propertysupport.customizers;

import java.util.ResourceBundle;
import javax.swing.event.TableModelEvent;
import javax.swing.table.TableModel;
import org.netbeans.modules.uml.common.generics.ETPairT;
import org.openide.util.NbBundle;

/**
 *
 * @author Thuy
 */
public class MultiplictyRangeHandler 
{
    private ResourceBundle bundle = NbBundle.getBundle(MultiplictyRangeHandler.class);
    /** Creates a new instance of MultiplictyRangeHandler */
    public MultiplictyRangeHandler()
    {
    }
    
    public ETPairT<Boolean, String> tableValueChanged(TableModelEvent e) {
        StringBuffer message = new StringBuffer();
        ETPairT <Boolean, String> retPair = null;
        boolean valid = true;
        int row = e.getFirstRow();
        int column = e.getColumn();
        int lowerVal = 0;
        int upperVal = 0;
        String upper = null;
        TableModel model = (TableModel)e.getSource();
        if (row != -1 && column != 2) {
            if (column == 0 ) {         // lower column
                if (valid = validateLower(row, column, model, message)) {
                    if (valid = validateUpper(row, column+1, model, message)) {
                        upper = (String) model.getValueAt(row, column+1);
                        if (!"*".equals(upper)) {  // upper is infinite, no need to check
                            lowerVal = Integer.parseInt((String) model.getValueAt(row, column));
                            upperVal = Integer.parseInt((String) model.getValueAt(row, column+1));
                            // fixed issue 108024: Lower value must be smaller or equal to upper value
                            if (lowerVal > upperVal) {
                                valid = false;
                                message.append(bundle.getString("MSG_INVALID_LOWER"));
                                message.append(" " + bundle.getString("MSG_LOWER_MUST_BE_SMALLER"));
                            }
                        }
                    }
                }
            } else if (column == 1) {   // upper column
                if (valid = validateUpper(row, column, model, message)) {
                    if (valid = validateLower(row, column-1, model, message)) {
                        upper = (String) model.getValueAt(row, column);
                        if (!"*".equals(upper)) {  // upper is infinite, no need to check
                            upperVal = Integer.parseInt((String) model.getValueAt(row, column));
                            lowerVal = Integer.parseInt((String) model.getValueAt(row, column-1));
                            // fixed issue 108024: Upper value must be greater or equal to upper value
                            if (upperVal < lowerVal) {
                                valid = false;
                                message.append(bundle.getString("MSG_INVALID_UPPER"));
                                message.append(" " + bundle.getString("MSG_UPPER_MUST_BE_GREATER"));
                            }
                        }
                    }
                }
            }
            retPair = new ETPairT(Boolean.valueOf(valid), message.toString());
        }
        return retPair;
    }
    
    private boolean validateLower(int row, int column, TableModel model, StringBuffer message){
        boolean valid = true;
        String lower = (String) model.getValueAt(row, column);
        try {
            if (lower == null || lower.length() == 0) {
                valid = false;
                message.append(bundle.getString("MSG_EMTPY_LOWER"));
            } else {
                int lowerVal = Integer.parseInt(lower);
                if (lowerVal < 0) {
                    valid = false;
                    message.append(bundle.getString("MSG_INVALID_LOWER"));
                }
            }
        } catch (NumberFormatException nfe) {
            valid = false;
            message.append(bundle.getString("MSG_INVALID_LOWER"));
        }
        if (!valid) {
            message.append(" " + bundle.getString("MSG_LOWER_MUST_BE_SMALLER"));
        }
        return valid;
    }
    
    private boolean validateUpper(int row, int column, TableModel model, StringBuffer message){
        boolean valid = true;
        String upper = (String) model.getValueAt(row, column);
        
        if (upper == null || upper.length() == 0) {
            valid = false;
            message.append(bundle.getString("MSG_EMTPY_UPPER"));
        } else if (!upper.equals("*")){  // fix issue 108025: allowed * for infinite value
            try {
                int upperVal = Integer.parseInt(upper);
                if (upperVal <= 0) {     // fix issue 108024: upper must be > 0
                    valid = false;
                    message.append(bundle.getString("MSG_INVALID_UPPER"));
                }
            } catch (NumberFormatException nfe) {
                valid = false;
                message.append(bundle.getString("MSG_INVALID_UPPER"));
            }
        }
        if (!valid) {
            message.append(" " + bundle.getString("MSG_UPPER_MUST_BE_GREATER"));
        }
        
        return valid;
    }
}
