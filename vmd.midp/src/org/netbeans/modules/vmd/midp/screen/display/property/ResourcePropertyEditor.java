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
 *
 */

package org.netbeans.modules.vmd.midp.screen.display.property;

import org.netbeans.modules.vmd.api.screen.display.ScreenPropertyDescriptor;
import org.netbeans.modules.vmd.api.screen.display.ScreenPropertyEditor;

import javax.swing.*;
import java.awt.*;
import org.netbeans.modules.vmd.api.io.IOUtils;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.properties.common.PropertiesSupport;

/**
 * @author Karol Harezlak
 */
public class ResourcePropertyEditor implements ScreenPropertyEditor {
    
    private String propertyName;
    private DesignComponent propertyComponent;
    
    public ResourcePropertyEditor(String propertyName, DesignComponent propertyComponent) {
        assert propertyName != null;
        this.propertyName = propertyName;
        this.propertyComponent = propertyComponent;
    }
    
    public JComponent createEditorComponent(ScreenPropertyDescriptor property) {
        IOUtils.runInAWTNoBlocking(new Runnable() {
            public void run() {
                PropertiesSupport.showPropertyEditorForCurrentComponent (propertyComponent, propertyName);
            }
        });
        return null;
    }
    
    public Insets getEditorComponentInsets(JComponent editorComponent) {
        return null;
    }
    
}
