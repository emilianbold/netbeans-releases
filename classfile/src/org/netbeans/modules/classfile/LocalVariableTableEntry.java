/*
 * LocalVariableTable.java
 *
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
 *
 * Contributor(s): Thomas Ball
 *
 * Version: $Revision$
 */

package org.netbeans.modules.classfile;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * An entry in the local variable table of a method's code attribute.
 *
 * @author  Thomas Ball
 */
public final class LocalVariableTableEntry {
    
    int startPC;
    int length;
    String name;
    String description;
    int index;

    static LocalVariableTableEntry[] loadLocalVariableTable(DataInputStream in, ConstantPool pool) 
      throws IOException {
        int n = in.readUnsignedShort();
        LocalVariableTableEntry[] exceptions = new LocalVariableTableEntry[n];
        for (int i = 0; i < n; i++)
            exceptions[i] = new LocalVariableTableEntry(in, pool);
        return exceptions;
    }

    /** Creates new LocalVariableTableEntry */
    LocalVariableTableEntry(DataInputStream in, ConstantPool pool) 
      throws IOException {
        loadLocalVariableEntry(in, pool);
    }

    private void loadLocalVariableEntry(DataInputStream in, ConstantPool pool) 
      throws IOException {
          startPC = in.readUnsignedShort();
          length = in.readUnsignedShort();
          CPUTF8Info entry = (CPUTF8Info)pool.get(in.readUnsignedShort());
          name = entry.getName();
          entry = (CPUTF8Info)pool.get(in.readUnsignedShort());
          description = entry.getName();
          index = in.readUnsignedShort();
    }

    /**
     * @return the first byte code offset where this variable is valid.
     */ 
    public final int getStartPC() {
        return startPC;
    }

    /**
     * @return the length of the range of code bytes where this variable
     *         is valid.  
     */
    public final int getLength() {
        return length;
    }

    /**
     * @return the name of this variable.
     */
    public final String getName() {
        return name;
    }

    /**
     * @return the signature (type) of this variable.
     */
    public final String getDescription() {
        return description;
    }

    /**
     * @return the variable's index into the local variable array
     *         for the current stack frame.
     */
    public final int getIndex() {
        return index;
    }
}
