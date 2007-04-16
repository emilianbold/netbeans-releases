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
package org.netbeans.modules.java.editor.options;

import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;

/**
 *
 * @author Jan Lahoda
 */
public class MarkOccurencesSettings {
    
    private static final String MARK_OCCURENCES = "MarkOccurences"; // NOI18N

    public static String ON_OFF = "OnOff"; // NOI18N
    public static String TYPES = "Types"; // NOI18N
    public static String METHODS = "Methods"; // NOI18N
    public static String CONSTANTS = "Constants"; // NOI18N
    public static String FIELDS = "Fields"; // NOI18N
    public static String LOCAL_VARIABLES = "LocalVariables"; // NOI18N
    public static String EXCEPTIONS = "Exceptions"; // NOI18N
    public static String EXIT = "Exit"; // NOI18N
    public static String IMPLEMENTS = "Implements"; // NOI18N
    public static String OVERRIDES = "Overrides"; // NOI18N
    public static String BREAK_CONTINUE = "BreakContinue"; // NOI18N
    public static String KEEP_MARKS = "KeepMarks"; // NOI18N
    
    private MarkOccurencesSettings() {
    }

    public static Preferences getCurrentNode() {
        Preferences preferences = NbPreferences.forModule(MarkOccurencesOptionsPanelController.class);
        return preferences.node(MARK_OCCURENCES).node(getCurrentProfileId());
    }
    
    private static String getCurrentProfileId() {
        return "default"; // NOI18N
    }
    
}
