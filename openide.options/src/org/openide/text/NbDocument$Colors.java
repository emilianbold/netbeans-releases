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
package org.openide.text;

import java.awt.Color;


/** Dummy class holding utility methods for working with NetBeans document conventions.
*
* @author Jaroslav Tulach
*/


/** @deprecated Not useful for anything. */
public final class NbDocument$Colors extends org.openide.options.SystemOption {
    public static final String PROP_BREAKPOINT = "NbBreakpointStyle"; // NOI18N
    public static final String PROP_ERROR = "NbErrorStyle"; // NOI18N
    public static final String PROP_CURRENT = "NbCurrentStyle"; // NOI18N
    static final long serialVersionUID = -9152250591365746193L;

    public void setBreakpoint(Color c) {
    }

    public Color getBreakpoint() {
        return new Color(127, 127, 255);
    }

    public void setError(Color c) {
    }

    public Color getError() {
        return Color.red;
    }

    public void setCurrent(Color c) {
    }

    public Color getCurrent() {
        return Color.magenta;
    }

    public String displayName() {
        return "COLORS"; // NOI18N
    }
}
