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
