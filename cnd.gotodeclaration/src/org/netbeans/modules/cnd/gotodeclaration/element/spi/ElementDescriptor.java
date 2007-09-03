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
package org.netbeans.modules.cnd.gotodeclaration.element.spi;

import javax.swing.Icon;

/**
 * Describes one element in the search list of "Go to Funcion or Variable" dialog
 * @author Vladimir Kvashi
 */
public interface ElementDescriptor {
    
    /**      
     * Gets the name of the element as such
     * @return element name 
     */
    String getDisplayName();
        
    /**
     * Gets an additional context for the element name. 
     * This would typically be the fully qualified name, minus the name part. 
     * Return null if there is no applicable context. 
     * 
     * @return the name of the context of the type, 
     * such as the fully qualified name minus the name part
     */
    String getContextName();

    /** 
     * Gets an icon that should be shown for this element. 
     * The icon should give a visual indication of the type of element
     * (e.g. function or variable or macro)
     * @return An Icon to be shown on the left hand side 
     */
    Icon getIcon();
    
    /**
     * Gets the name of the project that contains the given element.
     * @return the name of the project that contains the given element.
     */
    String getProjectName();
    
    /**
     * Gets the icon that represents the project that contains the given element.
     * @return project icon or null if there are no project associated with element
     */
    Icon getProjectIcon();
    
    /**
     * Gets the absolute path to the file that contains the element.
     * @return the absolute path to the file that contains the element.
     */
    String getAbsoluteFileName();
    
    /**
     * Opens the element in editor
     */
    void open();
}
