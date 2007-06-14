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

import java.util.Hashtable;
import javax.swing.JComponent;
import org.netbeans.modules.uml.propertysupport.options.api.UMLOptionsPanel;
import org.openide.util.NbBundle;

/**
 *
 * @author krichard
 */
public class GeneralOptionsPanel implements UMLOptionsPanel {
    
    private GeneralOptionsPanelForm form = null ;

    
    public GeneralOptionsPanel() {
    }
    
    public void update() {
        form.load();
    }
    
    public void applyChanges() {
        form.store() ;
    }
    
    public void cancel() {
        form.cancel() ;
    }
    
    public JComponent create() {
        if (form == null) {
        
            form = new GeneralOptionsPanelForm();
        
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
