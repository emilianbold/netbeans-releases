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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.debugger.jpda;

import com.sun.jdi.ReferenceType;

import java.util.AbstractList;
import java.util.List;

import org.netbeans.api.debugger.jpda.JPDAClassType;

/**
 * Lazy list of class types.
 * 
 * @author Martin Entlicher
 */
class ClassTypeList extends AbstractList<JPDAClassType> {
    
    private JPDADebuggerImpl debugger;
    private List<ReferenceType> classes;
    
    /** Creates a new instance of ClassTypeList */
    ClassTypeList(JPDADebuggerImpl debugger, List<ReferenceType> classes) {
        this.debugger = debugger;
        this.classes = classes;
    }
    
    List<ReferenceType> getTypes() {
        return classes;
    }
    
    public JPDAClassType get(int i) {
        return debugger.getClassType(classes.get(i));
    }
    
    public int size() {
        return classes.size();
    }
    
}
