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
