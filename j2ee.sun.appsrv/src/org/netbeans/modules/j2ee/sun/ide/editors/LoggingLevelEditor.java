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
/*
 * LoggingLevelEditor.java
 *
 * Created on March 18, 2004, 1:22 PM
 */

package org.netbeans.modules.j2ee.sun.ide.editors;

import java.util.logging.Level;

/** Property editor for java.util.logging.Level values.
 * @author vkraemer
 */
public class LoggingLevelEditor extends LogLevelEditor {
    
    /** Creates a new instance of LoggingLevelEditor */
    public LoggingLevelEditor() {
    }
    static String[] choices = {
        Level.ALL.toString(),
        Level.FINEST.toString(), 
        Level.FINER.toString(),
        Level.FINE.toString(),
        Level.CONFIG.toString(),
        Level.INFO.toString(), 
        Level.WARNING.toString(), 
        Level.SEVERE.toString(),
        Level.OFF.toString(), 
    };
    
    /** Returns the text values that represent valid Level values.
     * @return text values ordered least to most restrictive
     */    
    public String[] getTags() {
        return choices;
    }
}
