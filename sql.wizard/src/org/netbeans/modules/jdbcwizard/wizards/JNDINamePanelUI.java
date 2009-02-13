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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 * 
 * Copyright 2009 Sun Microsystems, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *  http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */

package org.netbeans.modules.jdbcwizard.wizards;

import java.awt.FlowLayout;

import javax.swing.SwingConstants;

import org.openide.util.HelpCtx;

import org.openide.util.NbBundle;




/**
 * @author npedapudi
 */
public class JNDINamePanelUI extends javax.swing.JPanel {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /** Creates new form JNDINamePanel */
    public JNDINamePanelUI(final String title) {
        if (title != null && title.trim().length() != 0) {
            this.setName(title);
        }
        this.initComponents();
    }

    /**
     * intializes the components
     */
    private void initComponents() {
        this.jLabel1 = new javax.swing.JLabel();
        this.jTextField1 = new javax.swing.JTextField();
        this.jTextField1.setText(JNDINamePanel.JNDI_DEFAULT_NAME);
        this.jLabel1.setText(NbBundle.getMessage(JDBCWizardTablePanel.class,"LBL_JNDI_NAME"));
		this.jLabel1.setHorizontalAlignment(SwingConstants.RIGHT);

		this.setLayout(new FlowLayout());
		this.add(jLabel1);
		this.add(jTextField1);
		//this.jTextField1.setPreferredSize(new Dimension((3*jLabel1.getWidth()),jLabel1.getHeight()));
    }

    /**
     * @return
     */
    public HelpCtx getHelp() {
        // Show no Help button for this panel:
       return new HelpCtx(JNDINamePanelUI.class);
    }

    // Variables declaration - do not modify
    javax.swing.JLabel jLabel1;

    javax.swing.JTextField jTextField1;
    // End of variables declaration

}
