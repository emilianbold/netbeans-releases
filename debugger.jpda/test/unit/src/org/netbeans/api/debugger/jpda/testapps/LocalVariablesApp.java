/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.api.debugger.jpda.testapps;

/**
 * Sample local variables test. DO NOT MODIFY - line numbers must not change in this source file.
 *
 * @author Maros Sandor
 */
public class LocalVariablesApp {

    public static void main(String[] args) {
        LocalVariablesApp sa = new LocalVariablesApp();
        sa.watches();
    }

    private void watches() {
        int     x = 40;
        float   y = 50.5f;
        Object  s = "asdfghjkl";
        {
            int g = 20;
            System.currentTimeMillis();
        }
        CharSequence g = "ad";
        System.currentTimeMillis();
    }
}
