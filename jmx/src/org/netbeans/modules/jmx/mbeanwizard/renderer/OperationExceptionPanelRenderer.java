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
 * Software is Sun Microsystems, Inc. Portions Copyright 2004-2005 Sun
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
package org.netbeans.modules.jmx.mbeanwizard.renderer;

import javax.swing.table.DefaultTableCellRenderer;
import java.awt.Component;
import java.util.ArrayList;
import javax.swing.JTextField;
import javax.swing.JTable;
import javax.swing.JPanel;
import org.netbeans.modules.jmx.MBeanOperationException;


/**
 * Class managing the rendering for the panel responsible for the
 * operation exception popup display and result textfield
 *
 */
public class OperationExceptionPanelRenderer extends  DefaultTableCellRenderer {

    /*******************************************************************/
    // here, the model is not typed because more than one table uses it
    // i.e we have to call explicitely the model's internal structure
    // via getValueAt and setValueAt
    /********************************************************************/
    
    private JPanel comp;
    private JTextField text;
    
    /**
     * Constructor
     * @param comp the panel containing the popup button and the result 
     * text field
     * @param text the result text field
     */
    public OperationExceptionPanelRenderer(JPanel comp, JTextField text) {
        this.comp = comp;
        this.text = text;
    }
    
    /**
     * Method returning the modified component (component + rendering)
     * @param table the table in which the component is contained
     * @param value the value of the component
     * @param isSelected true if the component is selected
     * @param hasFocus true if the component has the focus
     * @param row the row of the component in the table
     * @param column the column of the component in the table
     * @return Component the modified component
     */
    public Component getTableCellRendererComponent(JTable table,
            Object value,
            boolean isSelected,
            boolean hasFocus,
            int row,
            int column) {
        
        // gets the whole information also we only want to have the name
        // and for example not the description
        ArrayList<MBeanOperationException> oText = 
                (ArrayList<MBeanOperationException>)
                     table.getModel().getValueAt(row, column);
        String excepClassString = "";// NOI18N
        for (int i = 0; i < oText.size(); i++) {
            excepClassString += oText.get(i).getExceptionClass();
            
            if (i < oText.size()-1)
                excepClassString += ",";// NOI18N
        }
        text.setText(excepClassString);
        
        /*
        // makes visual line selection possible
        if (row == table.getSelectedRow()) {
            text.setBackground(table.getSelectionBackground());
        }
         */
        
        return comp;
    }
    
    /**
     * Returns simply the component
     * @return Component the component
     */
    public Component getComponent() {
        return comp;
    }
}
