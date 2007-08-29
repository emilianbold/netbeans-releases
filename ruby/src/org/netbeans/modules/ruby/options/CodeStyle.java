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

package org.netbeans.modules.ruby.options;

import java.util.prefs.Preferences;
import org.netbeans.api.project.Project;

import static org.netbeans.modules.ruby.options.FmtOptions.*;

/** 
 *  XXX make sure the getters get the defaults from somewhere
 *  XXX add support for profiles
 *  XXX get the preferences node from somewhere else in odrer to be able not to
 *      use the getters and to be able to write to it.
 * 
 * @author Dusan Balek
 */
public final class CodeStyle {
    
    private static CodeStyle INSTANCE;

    static {
        FmtOptions.codeStyleProducer = new Producer();
    }
    
    private Preferences preferences;
    
    private CodeStyle(Preferences preferences) {
        this.preferences = preferences;
    }

    /** For testing purposes only */
    public static CodeStyle getTestStyle(Preferences prefs) {
        return new CodeStyle(prefs);
    }
    
    public synchronized static CodeStyle getDefault(Project project) {
        
        if ( FmtOptions.codeStyleProducer == null ) {
            FmtOptions.codeStyleProducer = new Producer();
        }
        
        if (INSTANCE == null) {
            INSTANCE = create();
        }
        return INSTANCE;
    }
    
    static CodeStyle create() {
        return new CodeStyle(FmtOptions.getPreferences(FmtOptions.getCurrentProfileId()));
    }
    
    // General tabs and indents ------------------------------------------------
    
    public boolean expandTabToSpaces() {
        return preferences.getBoolean(expandTabToSpaces, getGlobalExpandTabToSpaces());
    }

    public int getTabSize() {
        return preferences.getInt(tabSize, getGlobalTabSize());
    }

    public int getIndentSize() {
        return preferences.getInt(indentSize, getDefaultAsInt(indentSize));
    }

    public int getContinuationIndentSize() {
        return preferences.getInt(continuationIndentSize, getDefaultAsInt(continuationIndentSize));
    }

    public boolean reformatComments() {
        return preferences.getBoolean(reformatComments, getDefaultAsBoolean(reformatComments));
    }

    public boolean indentHtml() {
        return preferences.getBoolean(indentHtml, getDefaultAsBoolean(indentHtml));
    }
    
    public int getRightMargin() {
        return preferences.getInt(rightMargin, getGlobalRightMargin());
    }

    // Communication with non public packages ----------------------------------
    
    private static class Producer implements FmtOptions.CodeStyleProducer {

        public CodeStyle create(Preferences preferences) {
            return new CodeStyle(preferences);
        }
        
    } 
}
