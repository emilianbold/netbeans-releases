/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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
package org.netbeans.modules.versioning.util;

import javax.swing.*;

/**
 * Groups outputs from versioning commands in one window.
 * 
 * @author Maros Sandor
 */
public final class VersioningOutputManager {

    private static final VersioningOutputManager instance = new VersioningOutputManager();
    
    public static VersioningOutputManager getInstance() {
        return instance;
    }
    
    VersioningOutputManager() {
    }

    /**
     * Adds a component to the Versioning Output window and brings it to front.
     * Only one component with a given key can be displayed in the output window at any one time so if a component
     * added with the same key already exists in the Versioning Output window, it is removed. 
     * The supplied component's name, obtained by getName(), is used as a title for the component. 
     * 
     * @param key category key of the component or null if the component should be independent
     * @param component component to display in the Versioning Output window
     */
    public void addComponent(String key, JComponent component) {
        VersioningOutputTopComponent tc = VersioningOutputTopComponent.getInstance();
        tc.addComponent(key, component);
        tc.open();
    }
}
