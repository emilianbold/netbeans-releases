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

package org.netbeans.modules.jmx.mbeanwizard.popup;
import org.netbeans.modules.jmx.mbeanwizard.tablemodel.MBeanOperationTableModel;
import org.netbeans.modules.jmx.mbeanwizard.tablemodel.OperationParameterTableModel;
import org.netbeans.modules.jmx.mbeanwizard.listener.AddTableRowListener;
import org.netbeans.modules.jmx.mbeanwizard.listener.RemTableRowListener;
import java.awt.BorderLayout;

import javax.swing.JButton;
import javax.swing.JTextField;

import org.openide.util.NbBundle;
import org.netbeans.modules.jmx.mbeanwizard.table.WrapperOperationParameterPopupTable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import org.netbeans.modules.jmx.FireEvent;
import org.netbeans.modules.jmx.MBeanOperationParameter;



/**
 * Class implementing the parameter popup window
 *
 */
public class WrapperOperationParameterPopup extends OperationParameterPopup {
    
    /**
     * Constructor
     * @param ancestorPanel the parent panel of the popup; here the wizard
     * @param textField the text field to fill with the popup information
     * @param result the intermediate structure which storespopup information
     * @param wiz the parent-window's wizard panel
     */
    public WrapperOperationParameterPopup(JPanel ancestorPanel, 
            MBeanOperationTableModel model,
            JTextField textField, int editedRow, FireEvent wiz) {
        
        super(ancestorPanel, model, textField, editedRow, wiz);
    }
    
    protected void setDimensions(String str) {
        setTitle(str);
        setModal(true);
        setResizable(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setBounds(350,250,POPUP_WIDTH,POPUP_HEIGHT); 
        setVisible(true);
    }
    
    protected void initJTable() {
        
        popupTableModel = new OperationParameterTableModel();
        popupTable = new WrapperOperationParameterPopupTable(popupTableModel);
        popupTable.setName("ParamPopupTable");// NOI18N
    }
    
     protected JButton[] getUsedButtons() {
        return new JButton[] {
                closeJButton
        };
    }
}
