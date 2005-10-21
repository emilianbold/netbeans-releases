/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.debugger.jpda.models;

import com.sun.jdi.ClassType;
import com.sun.jdi.ObjectReference;

import org.netbeans.api.debugger.jpda.Super;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;


/**
 * @author   Jan Jancura
 */
class SuperVariable extends AbstractVariable implements Super {

    // init ....................................................................
    private ClassType classType;
    
    
    SuperVariable (
        JPDADebuggerImpl debugger,
        ObjectReference value, 
        ClassType classType,
        String parentID
    ) {
        super (
            debugger, 
            value, 
            parentID + ".super^"
        );
        this.classType = classType;
    }

    
    // Super impl ..............................................................
    
    public Super getSuper () {
        if (getInnerValue () == null) 
            return null;
        ClassType superType = this.classType.superclass ();
        if (superType == null) 
            return null;
        return new SuperVariable(
                this.getDebugger(), 
                (ObjectReference) this.getInnerValue(),
                superType,
                this.getID()
                );
    }
    
        
    // other methods ...........................................................
        
    public String toString () {
        return "SuperVariable " + getType();
    }
    
    public String getType () {
        return this.classType.name ();
    }
}
