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

package org.netbeans.modules.cnd.gotodeclaration.element.ui;

import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;

/** Holds all the UI options, names etc, Uses innerclasses plainly as namespaces.
 *
 * @author phrebejk
 */
final class GoToElementOptions {
            
    /** Creates a new instance of UiOptions */
    private GoToElementOptions() {}
    
    final static class GoToElementDialog {
    
        private static final String GO_TO_TYPE_DIALOG = "GoToTypeDialog"; // NOI18N    
        
        private static final String CASE_SENSITIVE = "caseSensitive"; // NOI18N
        private static final String WIDTH = "width"; // NOI18N
        private static final String HEIGHT = "height"; // NOI18N
    
        private static Preferences node;
        
        public static boolean getCaseSensitive() {
            return getNode().getBoolean(CASE_SENSITIVE, false);
        }
        
        public static void setCaseSensitive( boolean caseSensitive) {
            getNode().putBoolean(CASE_SENSITIVE, caseSensitive);
        }
        
        public static int getHeight() {
            return getNode().getInt(HEIGHT, 460);
        }
        
        public static void setHeight( int height ) {
            getNode().putInt(HEIGHT, height);
        }
        
        public static int getWidth() {
            return getNode().getInt(WIDTH, 680);
        }
         
        public static void setWidth( int width ) {
            getNode().putInt(WIDTH, width);
        }
        
        private static synchronized Preferences getNode() {
            if ( node == null ) {                
                Preferences p = NbPreferences.forModule(GoToElementOptions.class);
                node = p.node(GO_TO_TYPE_DIALOG);
            }
            return node;
        }
    }
    
}
