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

package org.netbeans.modules.debugger.jpda.models;

import com.sun.jdi.ClassType;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.Value;
import org.netbeans.api.debugger.jpda.Field;
import org.netbeans.api.debugger.jpda.Super;


/**
 * @author   Jan Jancura
 */
public class SuperVariable extends AbstractVariable implements Super {

    private ClassType superClass;
    private LocalsTreeModel model;

    
    // init ....................................................................
    
    SuperVariable (
        LocalsTreeModel model,
        Value value, 
        ClassType superClass,
        String parentID
    ) {
        super (
            model, 
            value, 
            parentID + ".super^"
        );
        this.superClass = superClass;
    }

    
    // Super impl ..............................................................
    
    public Field[] getFields (int from, int to) {
        AbstractVariable[] vs = getModel ().getSuperFields (
            this,
            false, from, to
        );
        Field[] fs = new Field [vs.length];
        System.arraycopy (vs, 0, fs, 0, vs.length);
        return fs;
    }
    
    public int getFieldsCount () {
        return superClass.fields ().size ();
    }
    
    public Super getSuper () {
        ClassType s = superClass.superclass ();
        if (s == null) return null;
        return getModel ().getSuper (
            s, 
            (ObjectReference) getInnerValue (),
            getID ()
        );
    }
    
    public String getType () {
        return superClass.name ();
    }

    
    // other methods ...........................................................
        
    public String toString () {
        return "SuperVariable " + superClass.name ();
    }
    
    ClassType getSuperClass () {
        return superClass;
    }
}
