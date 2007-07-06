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

/* this is stub code written based on Apple EAWT package javadoc published at
 * http://developer.apple.com. It makes compiling code which uses Apple EAWT
 * on non-Mac platforms possible.
 */

package com.apple.eawt;

import java.awt.Point;

public class Application {
    public void addAboutMenuItem() {
        // does nothing
    }
    
    public void addApplicationListener(final ApplicationListener listener) {
        // does nothing
    }
    
    public void addPreferencesMenuItem() {
        // does nothing
    }
    
    public static Application getApplication() {
        return null;
    }
    
    public boolean getEnabledAboutMenu() {
        return false;
    }
    
    public boolean getEnabledPreferencesMenu() {
        return false;
    }
    
    public static Point getMouseLocationOnScreen() {
        return null;
    }
    
    public boolean isAboutMenuItemPresent() {
        return false;
    }
    
    public boolean isPreferencesMenuItemPresent() {
        return false;
    }
    
    public void removeAboutMenuItem() {
        // does nothing
    }
    
    public void removeApplicationListener(final ApplicationListener listener) {
        // does nothing
    }
    
    public void removePreferencesMenuItem() {
        // does nothing
    }
    
    public void setEnabledAboutMenu(final boolean enable) {
        // does nothing
    }
    
    public void setEnabledPreferencesMenu(final boolean enable) {
        // does nothing
    }
}
