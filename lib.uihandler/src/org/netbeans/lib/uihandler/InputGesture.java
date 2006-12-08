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
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 */

package org.netbeans.lib.uihandler;

import java.util.logging.LogRecord;

/** Represents a gesture that initiated the given LogRecord.
 *
 * @author Jaroslav Tulach
 */
public enum InputGesture {
    KEYBOARD, MENU, TOOLBAR;
    
    /** Finds the right InputGesture for given LogRecord.
     * @param rec the record
     * @return the gesture that initated the record or null if unknown
     */
    public static InputGesture 
        valueOf(LogRecord rec) {
        if ("UI_ACTION_BUTTON_PRESS".equals(rec.getResourceBundleName())) {
            if (rec.getMessage().indexOf("Actions$Menu") >= 0) {
                return MENU;
            }
            if (rec.getMessage().indexOf("Actions$Toolbar") >= 0) {
                return TOOLBAR;
            }
        }
        if ("UI_ACTION_KEY_PRESS".equals(rec.getResourceBundleName())) {
            return KEYBOARD;
        }
        return null;
    }
}
