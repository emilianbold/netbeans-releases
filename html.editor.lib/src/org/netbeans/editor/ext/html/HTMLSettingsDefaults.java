/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.editor.ext.html;

import org.netbeans.editor.ext.ExtSettingsDefaults;

/**
 * Initializer for the HTML editor settings.
 * 
 * @author Martin Roskanin
 * @since 08.2002
 *
 */
public class HTMLSettingsDefaults extends ExtSettingsDefaults {
    
    // lower case of HTML code completion
    public static final Boolean defaultCompletionLowerCase = Boolean.TRUE;    
    
    public static final Integer defaultCodeFoldingUpdateInterval = new Integer(2000); //ms
}
