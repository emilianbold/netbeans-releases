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

package org.netbeans.lib.lexer.test;

public final class FixedTextDescriptor {
    
    public static FixedTextDescriptor create(String text, double ratio) {
        return new FixedTextDescriptor(text, ratio);
    }
    
    public static FixedTextDescriptor crlf(double ratio) {
        return new FixedTextDescriptor("\r\n", ratio);
    }
    

    private final String text;
    
    private final double ratio;
    
    private FixedTextDescriptor(String text, double ratio) {
        this.text = text;
        this.ratio = ratio;
    }

    public String text() {
        return text;
    }

    public double ratio() {
        return ratio;
    }

}