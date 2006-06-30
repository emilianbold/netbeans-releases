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
