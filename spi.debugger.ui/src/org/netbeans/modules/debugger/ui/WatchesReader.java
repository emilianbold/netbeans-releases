/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.debugger.ui;

import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.Properties;
import org.netbeans.api.debugger.Watch;


/**
 *
 * @author Jan Jancura
 */
public class WatchesReader implements Properties.Reader {
    
    
    public String [] getSupportedClassNames () {
        return new String[] {
            Watch.class.getName (),
        };
    }
    public Object read (String typeID, Properties properties) {
        if (typeID.equals (Watch.class.getName ()))
            return DebuggerManager.getDebuggerManager ().createWatch (
                properties.getString (Watch.PROP_EXPRESSION, null)
            );
        return null;
    }
    
    public void write (Object object, Properties properties) {
        if (object instanceof Watch)
            properties.setString (
                Watch.PROP_EXPRESSION, 
                ((Watch) object).getExpression ()
            );
    }
}
