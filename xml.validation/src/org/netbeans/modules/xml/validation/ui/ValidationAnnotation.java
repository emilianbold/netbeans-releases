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

package org.netbeans.modules.xml.validation.ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.openide.text.Annotation;
import org.openide.text.Line;

/**
 * Class to annotate validation errors in the source multiview.
 */
public class ValidationAnnotation extends Annotation implements PropertyChangeListener {
    
    private String message;
    
    public String getAnnotationType() {
        return "org-netbeans-modules-xml-core-error"; // NOI18N
    }
    
    public String getShortDescription() {
        return message;
    }
    
    public void show( Line line, String message) {
        this.message = message;
        attach(line);
        line.addPropertyChangeListener(this);
    }
    
    public void propertyChange( PropertyChangeEvent propertyChangeEvent ) {
        Line line = (Line) propertyChangeEvent.getSource();
        if (line != null) {
            line.removePropertyChangeListener(this);
            detach();
        }
    }
}