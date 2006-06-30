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

package org.netbeans.modules.web.debug.breakpoints;

import org.netbeans.api.debugger.*;
import org.netbeans.api.debugger.jpda.*;

/**
 *
 * @author Martin Grebac
 */
public class JspBreakpointsReader implements Properties.Reader {

    public String [] getSupportedClassNames () {
        return new String[] { JspLineBreakpoint.class.getName () };
    }

    public Object read (String typeID, Properties properties) {

        JspLineBreakpoint b = null;
        if (typeID.equals (JspLineBreakpoint.class.getName ())) {
            b = JspLineBreakpoint.create (
                properties.getString(JspLineBreakpoint.PROP_URL, null),
                properties.getInt(JspLineBreakpoint.PROP_LINE_NUMBER, 1)
            );
            b.setCondition(properties.getString (JspLineBreakpoint.PROP_CONDITION, ""));
            b.setPrintText(properties.getString (JspLineBreakpoint.PROP_PRINT_TEXT, ""));
            b.setGroupName(properties.getString (Breakpoint.PROP_GROUP_NAME, ""));
            b.setSuspend(properties.getInt (JspLineBreakpoint.PROP_SUSPEND, JspLineBreakpoint.SUSPEND_ALL));
            if (properties.getBoolean (JspLineBreakpoint.PROP_ENABLED, true)) {
                b.enable ();
            } else {
                b.disable ();
            }
        }

        return b;
    }
    
    public void write (Object object, Properties properties) {

        if (object instanceof JspLineBreakpoint) {
            JspLineBreakpoint b = (JspLineBreakpoint) object;
            properties.setString (JspLineBreakpoint.PROP_PRINT_TEXT, b.getPrintText ());
            properties.setString (JspLineBreakpoint.PROP_GROUP_NAME, b.getGroupName ());
            properties.setInt (JspLineBreakpoint.PROP_SUSPEND, b.getSuspend ());
            properties.setBoolean (JspLineBreakpoint.PROP_ENABLED, b.isEnabled ());        
            properties.setString (JspLineBreakpoint.PROP_URL, b.getURL ());
            properties.setInt (JspLineBreakpoint.PROP_LINE_NUMBER, b.getLineNumber ());
            properties.setString (JspLineBreakpoint.PROP_CONDITION, b.getCondition ());
        }
        return;
    }
}
