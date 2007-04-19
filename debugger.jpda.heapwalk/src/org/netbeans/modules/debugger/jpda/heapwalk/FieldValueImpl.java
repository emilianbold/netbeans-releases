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

package org.netbeans.modules.debugger.jpda.heapwalk;

import com.sun.tools.profiler.heap.Field;
import com.sun.tools.profiler.heap.FieldValue;
import com.sun.tools.profiler.heap.Instance;

import org.netbeans.api.debugger.jpda.Variable;

/**
 *
 * @author Martin Entlicher
 */
public class FieldValueImpl implements FieldValue {
    
    private org.netbeans.api.debugger.jpda.Field field;
    private Instance defInstance;
    private HeapImpl heap;
    
    /** Creates a new instance of FieldValueImpl */
    public FieldValueImpl(HeapImpl heap, Instance defInstance, org.netbeans.api.debugger.jpda.Field field) {
        this.field = field;
        this.defInstance = defInstance;
        this.heap = heap;
    }

    public Field getField() {
        return new FieldImpl(heap, field);
    }

    public String getValue() {
        return field.getValue();
    }

    public Instance getDefiningInstance() {
        return defInstance;
    }
    
}
