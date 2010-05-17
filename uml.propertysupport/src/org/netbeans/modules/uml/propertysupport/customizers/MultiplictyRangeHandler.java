/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
