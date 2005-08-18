/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.api.editor.settings;

/**
 * Style Constants for Fonts and Colors AttributeSets.
 *
 * @author Martin Roskanin
 */
public final class EditorStyleConstants {

    private String representation;
    
    EditorStyleConstants(String representation) {
        this.representation = representation;
    }

    /**
     * Name of the wave underline color attribute.
     */
    public static final Object WaveUnderlineColor = new EditorStyleConstants ("wave underline color"); //NOI18N
    
    /**
     * Name of the display name attribute.
     */
    public static final Object DisplayName = new EditorStyleConstants ("display name"); //NOI18N
    
    /**
     * Name of the default fonts and colots attribute.
     */
    public static final Object Default = new EditorStyleConstants ("default"); //NOI18N
    
    public String toString() {
        return representation;
    }

}
