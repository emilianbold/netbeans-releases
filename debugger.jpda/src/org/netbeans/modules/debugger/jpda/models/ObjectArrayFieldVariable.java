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

package org.netbeans.modules.debugger.jpda.models;

import com.sun.jdi.ObjectReference;
import com.sun.jdi.ArrayReference;
import org.netbeans.api.debugger.jpda.ObjectVariable;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;


/**
 * @author   Jan Jancura
 */
class ObjectArrayFieldVariable extends ArrayFieldVariable
implements ObjectVariable {

    ObjectArrayFieldVariable (
        JPDADebuggerImpl debugger,
        ObjectReference value,
        String declaredType,
        ArrayReference array,
        int index,
        int maxIndex,
        String parentID
    ) {
        super (
            debugger, 
            value, 
            declaredType,
            array,
            index,
            maxIndex,
            parentID
        );
    }

    public ObjectArrayFieldVariable clone() {
        ObjectArrayFieldVariable clon = new ObjectArrayFieldVariable(
                getDebugger(),
                (ObjectReference) getJDIValue(),
                getDeclaredType(),
                array,
                index,
                0,
                getID());
        clon.maxIndexLog = this.maxIndexLog;
        return clon;
    }

    // other methods ...........................................................

    public String toString () {
        return "ObjectArrayFieldVariable " + getName ();
    }
}
