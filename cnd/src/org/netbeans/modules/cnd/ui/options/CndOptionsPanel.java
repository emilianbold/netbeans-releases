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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.ui.options;

import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.border.EmptyBorder;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.NbBundle;


/** Implementation of C/C++ Options panel */
public final class CndOptionsPanel extends JPanel {

    private JTabbedPane tabbedPane = new JTabbedPane();
    
    public CndOptionsPanel() {
        // no logic in constructor to speedup Tools->Options display time
    }
    
    /*package*/ void updateControllers(OptionsPanelController[] comps) {
        String name;
        char mnem;
        tabbedPane.removeAll();
	for (int i = 0; i < comps.length; i++) {
	    comps[i].getComponent(null).setBorder(new EmptyBorder(8, 8, 8, 8));
            name = NbBundle.getMessage(comps[i].getClass(), comps[i].getComponent(null).getName());
            mnem = NbBundle.getMessage(comps[i].getClass(), comps[i].getComponent(null).getName() + "_Mnemonic").charAt(0); // NOI18N
	    tabbedPane.addTab(name, comps[i].getComponent(null));
	    tabbedPane.setMnemonicAt(i, mnem);
	}      
        
        setLayout(new BorderLayout());
        add(tabbedPane, BorderLayout.CENTER);        
    }
}


