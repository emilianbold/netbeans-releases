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
package org.netbeans.modules.visualweb.propertyeditors.css;

import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.netbeans.modules.visualweb.propertyeditors.PropertyEditorBase;

/**
 *
 * @author Winston Prakash
 * @author gjmurphy
 */
public class CssStylePropertyEditor extends PropertyEditorBase implements
        PropertyChangeListener, com.sun.rave.propertyeditors.css.CssStylePropertyEditor {
    
    @Override
    public boolean supportsCustomEditor() {
        return true;
    }
    
    @Override
    public Component getCustomEditor() {
        StyleBuilderPanel styleBuilderPanel = 
                new StyleBuilderPanel(getAsText(), this.getDesignProperty());
        styleBuilderPanel.addCssPropertyChangeListener(this);
        return styleBuilderPanel;
    }
    
    public void propertyChange(PropertyChangeEvent evt) {
        setValue(evt.getNewValue());
    }
        
    @Override
    protected String getPropertyHelpId() {
        return "projrave_ui_elements_propeditors_style_prop_ed_main";
    }
    
}
