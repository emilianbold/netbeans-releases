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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.visualweb.dataconnectivity.explorer;

import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.beans.PropertyChangeListener;
import java.beans.PropertyEditor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import org.openide.util.NbBundle;

/**
 *
 * @author JohnBaker
 */
public class DataSourcePropertyEditor implements PropertyEditor {       
    private String url;
    
    DataSourcePropertyEditor(String jdbcUrl) {   
        url = jdbcUrl;
    }
    
    public void setValue(Object value) {
        url = (String)value;
    }

    public Object getValue() {
        return url;
    }

    public boolean isPaintable() {
        return false;
    }

    public void paintValue(Graphics gfx, Rectangle box) {
       
    }

    public String getJavaInitializationString() {
        return "";
    }

    public String getAsText() {
        return url == null ? NbBundle.getMessage (DataSourcePropertyEditor.class,
                "JDBC_URL") : url; //NOI18N
    }

    public void setAsText(String text) throws IllegalArgumentException {
        throw new IllegalArgumentException();
    }

    public String[] getTags() {
        return null;
    }

    public Component getCustomEditor() {      
        return null;
    }

    public boolean supportsCustomEditor() {
        return false;
    }

    private final List <PropertyChangeListener> listeners = 
            Collections.<PropertyChangeListener>synchronizedList (
            new ArrayList <PropertyChangeListener> ());
    
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        listeners.add(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        listeners.remove (listener);
    }

}
