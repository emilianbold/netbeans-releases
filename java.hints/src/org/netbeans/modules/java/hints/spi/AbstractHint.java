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
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package org.netbeans.modules.java.hints.spi;

import java.util.prefs.Preferences;
import javax.swing.JComponent;

/** Class to be extended by all the Java hints.
 *
 * @author Petr Hrebejk
 */
public abstract class AbstractHint implements TreeRule {
    
    public static final String ENABLED_KEY = "enabled";         // NOI18N
    public static final String SEVERITY_KEY = "severity";       // NOI18N
    public static final String IN_TASK_LIST_KEY = "inTaskList"; // NOI18N
    
    public static final boolean ENABLED_DEFAULT = true;
    public static final HintSeverity SEVERITY_DEFAULT = HintSeverity.WARNING;
    public static final boolean IN_TASK_LIST_DEFAULT = true;
            
    /** Gets preferences node which. Can return null (default impl. does) to get use the default
     * values and default behavior. 
     */
    public Preferences getPreferences() { // XXX Probably needs the profile as parameter
        return null;
    }
        
    /** Gets the UI description for this rule. It is fine to return null
     * to get the default behavior. Notice that the Preferences node is a copy
     * of the node returned frok {link:getPreferences()}. This is in oder to permit 
     * canceling changes done in the options dialog.
     * It is fine to return null (as default implementation does)
     */    
    public JComponent getCustomizer( Preferences node ) {
        return null;
    }
    
    public static enum HintSeverity {
        ERROR,
        WARNING,
        CURRENT_LINE_WARNING;        
    }
    
}
