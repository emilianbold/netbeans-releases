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
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package org.netbeans.modules.uml.propertysupport.options.panels;

import java.awt.BorderLayout;
import java.util.Hashtable;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.netbeans.modules.uml.propertysupport.options.api.UMLOptionsPanel;
import org.openide.util.NbBundle;

/**
 *
 * @author krichard
 */
public class JavaPlatformOptions implements UMLOptionsPanel {
    
    private JComponent form = null ;

    
    public JavaPlatformOptions() {
    }
    
    public void applyChanges() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public JComponent create() {
        if (form == null) {
        
            form = new JavaPlatformOptionsPanel();
        
        }
        return form;
    }
    
    public Hashtable getCurrentValues() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public Hashtable getUpdatedValues() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public String getDisplayName() {
        return NbBundle.getMessage (JavaPlatformOptions.class, "JAVA_PLATFORM");
    }
    
}
