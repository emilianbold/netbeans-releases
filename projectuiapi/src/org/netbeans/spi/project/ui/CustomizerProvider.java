/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.spi.project.ui;

/**
 * Ability for a project to provide a GUI customizer.
 * @see org.netbeans.api.project.Project#getLookup
 * @author Jesse Glick
 */
public interface CustomizerProvider {
    
    /**
     * Display some kind of customization UI for the project.
     * Typically would mean opening a nonmodal dialog, etc.
     */
    void showCustomizer();
    
}
