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

import com.sun.tools.profiler.heap.ArrayItemValue;
import com.sun.tools.profiler.heap.Instance;

/**
 *
 * @author Martin Entlicher
 */
public class ArrayItemValueImpl implements ArrayItemValue {
    
    private Instance defInstance;
    private Instance instance;
    private int index;
    
    /** Creates a new instance of ArrayItemValueImpl */
    public ArrayItemValueImpl(Instance defInstance, Instance instance, int index) {
        this.defInstance = defInstance;
        this.instance = instance;
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

    public Instance getInstance() {
        return instance;
    }

    public Instance getDefiningInstance() {
        return defInstance;
    }
    
}
