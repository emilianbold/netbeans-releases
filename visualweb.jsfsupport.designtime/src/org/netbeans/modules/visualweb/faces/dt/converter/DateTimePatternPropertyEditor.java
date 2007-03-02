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
package org.netbeans.modules.visualweb.faces.dt.converter;

import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.netbeans.modules.visualweb.faces.dt.AbstractPropertyEditor;

/**
 * @author eric
 *
 * @deprecated
 */
public class DateTimePatternPropertyEditor extends AbstractPropertyEditor 
        implements PropertyChangeListener {

    public String getAsText() {
        String value = (String)getValue();
        return value == null ? "" : value; //NOI18N
    }

    public Component getCustomEditor() {
        DateTimePatternPanel panel = new DateTimePatternPanel(this, getDesignProperty());
        panel.addPropertyChangeListener(this);
        return panel;
    }

    public void setAsText(String string) {
        string = string.trim();
        String value = null;
        boolean unset = true;
        if (string.length() > 0) {
            value = string;
            unset = false;
        }
        setValue(value);
        if (unset) {
            unsetProperty();
        }
    }

    public boolean supportsCustomEditor() {
        return true;
    }

    /** 
     * Notify this editor that its property's value has changed.
     */
    public void propertyChange(PropertyChangeEvent event) {
        this.setValue(event.getNewValue());
        super.firePropertyChange();
    }

}
