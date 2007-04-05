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

import com.sun.tools.profiler.heap.PrimitiveArrayInstance;

import java.util.ArrayList;
import java.util.List;

import org.netbeans.api.debugger.jpda.ObjectVariable;
import org.netbeans.api.debugger.jpda.Variable;

/**
 *
 * @author Martin Entlicher
 */
public class PrimitiveArrayInstanceImpl extends InstanceImpl implements PrimitiveArrayInstance {
    
    private ObjectVariable array;
    
    /** Creates a new instance of PrimitiveArrayInstanceImpl */
    public PrimitiveArrayInstanceImpl(ObjectVariable array, int instanceNo) {
        super(array, instanceNo);
        this.array = array;
    }

    public int getLength() {
        return array.getFieldsCount();
    }

    public List<String> getValues() {
        Variable[] values = array.getFields(0, getLength());
        List<String> strValues = new ArrayList<String>();
        for (Variable value: values) {
            strValues.add(value.getValue());
        }
        return strValues;
    }

}
