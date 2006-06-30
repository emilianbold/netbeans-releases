/*
 * ExceptionTableEntry.java
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

import java.io.DataInputStream;
import java.io.IOException;

/**
 * An entry in the exception table of a method's code attribute.
 *
 * @author  Thomas Ball
 */
public final class ExceptionTableEntry {

    int startPC;
    int endPC;
    int handlerPC;
    CPClassInfo catchType;  // may be null for "finally" exception handler

    static ExceptionTableEntry[] loadExceptionTable(DataInputStream in, ConstantPool pool) 
      throws IOException {
        int n = in.readUnsignedShort();
        ExceptionTableEntry[] exceptions = new ExceptionTableEntry[n];
        for (int i = 0; i < n; i++)
            exceptions[i] = new ExceptionTableEntry(in, pool);
        return exceptions;
    }

    /** Creates new ExceptionTableEntry */
    ExceptionTableEntry(DataInputStream in, ConstantPool pool) 
      throws IOException {
        loadExceptionEntry(in, pool);
    }

    private void loadExceptionEntry(DataInputStream in, ConstantPool pool) 
      throws IOException {
        startPC = in.readUnsignedShort();
        endPC = in.readUnsignedShort();
        handlerPC = in.readUnsignedShort();
        int typeIndex = in.readUnsignedShort();
        if (typeIndex != 0) // may be 0 for "finally" exception handler
	    try {
	        catchType = pool.getClass(typeIndex);
	    } catch (IndexOutOfBoundsException e) {
	        System.err.println("invalid catchType (" + typeIndex +
				   ") in exception table entry");
	    }
    }
    
    /**
     * Returns the beginning offset into the method's bytecodes of this
     * exception handler.
     */
    public final int getStartPC() {
        return startPC;
    }
    
    /**
     * Returns the ending offset into the method's bytecodes of this
     * exception handler, or the length of the bytecode array if the
     * handler supports the method's last bytecodes (JVM 4.7.3).
     */
    public final int getEndPC() {
        return endPC;
    }
    
    /**
     * Returns the starting offset into the method's bytecodes of the 
     * exception handling code.
     */
    public final int getHandlerPC() {
        return handlerPC;
    }
    
    /**
     * Returns the type of exception handler, or <code>null</code>
     * if this handler catches all exceptions, such as an exception
     * handler for a "<code>finally</code>" clause (JVM 4.7.3).
     */
    public final CPClassInfo getCatchType() {
        return catchType;
    }
}
