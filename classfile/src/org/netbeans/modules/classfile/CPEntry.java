/*
 * CPEntry.java
 *
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
 *
 * Contributor(s): Thomas Ball
 *
 * Version: $Revision$
 */

package org.netbeans.modules.classfile;


/**
 * A class representing an entry in a ConstantPool.
 *
 * @author Thomas Ball
 */
public abstract class CPEntry {

    ConstantPool pool;
    Object value;

    CPEntry(ConstantPool pool) {
	this.pool = pool;
    }

    void resolve(CPEntry[] pool) {
        // already resolved by default
    }

    /* The VM doesn't allow the next constant pool slot to be used
     * for longs and doubles.
     */
    boolean usesTwoSlots() {
	return false;
    }
    
    public Object getValue() {
        return value;
    }

    /**
     * Returns the constant type value, or tag, as defined by
     * table 4.3 of the Java Virtual Machine specification.
     */
    public abstract int getTag();
}

