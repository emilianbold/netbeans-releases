/*
 * ConstantPool.java
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

import java.io.*;
import java.util.Vector;

/**
 * Class representing a Java class file constant pool.
 *
 * @author Thomas Ball
 */
public class ConstantPool {

    private static final int CONSTANT_POOL_START = 1;

    // Constant Type enums (JVM spec table 4.3)
    static final int CONSTANT_Utf8 = 1;
    static final int CONSTANT_Integer = 3;
    static final int CONSTANT_Float = 4;
    static final int CONSTANT_Long = 5;
    static final int CONSTANT_Double = 6;
    static final int CONSTANT_Class = 7;
    static final int CONSTANT_String = 8;
    static final int CONSTANT_FieldRef = 9;
    static final int CONSTANT_MethodRef = 10;
    static final int CONSTANT_InterfaceMethodRef = 11;
    static final int CONSTANT_NameAndType = 12;

    CPEntry[] cpEntries;
    
    int constantPoolCount = 0;

    /**
     * Create a ConstantPool object from a stream of bytes.
     *
     * @param size number of entries in this constant pool.
     * @param bytes a stream of bytes defining the constant pool.
     */
    /* package-private */ ConstantPool(int size, InputStream bytes) {
        if (size < 0)
            throw new IllegalArgumentException("size cannot be negative");
        if (bytes == null)
            throw new IllegalArgumentException("byte stream not specified");
        constantPoolCount = size;
        cpEntries = new CPEntry[constantPoolCount];
        load(bytes);
    }
    
    /**
     * Get the CPEntry at a specific constant pool index.
     *
     * @param index the constant pool index for the entry
     */
    public final CPEntry get(int index) {
        if (index <= 0 || index >= cpEntries.length)
            throw new IndexOutOfBoundsException(Integer.toString(index));
        return cpEntries[index];
    }

    /**
     * Get the CPClassInfo at a specific index.
     *
     * @param index the constant pool index for the entry
     */
    public final CPClassInfo getClass(int index) {
        if (index <= 0)
            throw new IndexOutOfBoundsException(Integer.toString(index));
        return (CPClassInfo)get(index);
    }

    /**
     * Create a new ConstantPool object with no entries.
     * NOTE: not supported until classfile writing is.
     */
    /*public*/ ConstantPool() {
        constantPoolCount = CONSTANT_POOL_START;
        cpEntries = new CPEntry[constantPoolCount];
    }

    /* Return an array of all constants of a specified class type.
     *
     * @param type   the constant pool type to return.
     */
    public final Object[] getAllConstants(Class classType) {
        Vector v = new Vector();
        int n = cpEntries.length;
        for (int i = CONSTANT_POOL_START; i < n; i++) {
            if (cpEntries[i] != null && 
                cpEntries[i].getClass().equals(classType)) {
                v.addElement(cpEntries[i]);
            }
        }
        Object[] result = new Object[v.size()];
        v.copyInto(result);
        return result;
    }

    /* Return an array of all class references in pool.
     */
    public final String[] getAllClassNames() {
        /* Collect all class references.  The safest way to do this
         * is to combine the ClassInfo constants with any UTF8Info
         * constants which match the pattern "L*;".
         */
        Vector v = new Vector();
        Object[] oa = getAllConstants(CPClassInfo.class);
        for (int i = 0; i < oa.length; i++) {
            CPClassInfo ci = (CPClassInfo)oa[i];
            v.addElement(ci.getName());
        }

        oa = getAllConstants(CPUTF8Info.class);
        for (int i = 0; i < oa.length; i++) {
            CPUTF8Info utf = (CPUTF8Info)oa[i];
            String name = utf.getName();
            if (name.length() > 0 && name.charAt(0) == 'L' &&
                name.charAt(name.length() - 1) == ';') {
                String clsName = name.substring(1, name.length() - 1);
                if (!v.contains(clsName)) {
                    v.addElement(clsName);
                }
            }
        }
        String[] result = new String[v.size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = (String)v.elementAt(i);
        }
        return result;
    }

    final String getString(int index) {
    	CPUTF8Info utf = (CPUTF8Info)cpEntries[index];
    	return utf.getName();
    }
    
    private void load(InputStream cpBytes) {
        try {
	    ConstantPoolReader cpr = new ConstantPoolReader(cpBytes);

            // Read in pool entries.
            for (int i = CONSTANT_POOL_START; i < constantPoolCount; i++) {
                CPEntry newEntry = getConstantPoolEntry(cpr);
                cpEntries[i] = newEntry;
        
                if (newEntry.usesTwoSlots())
                    i++;
            }
    
            // Resolve pool entries.
            for (int i = CONSTANT_POOL_START; i < constantPoolCount; i++) {
                CPEntry entry = cpEntries[i];
                if (entry == null) {
                    continue;
                }
                entry.resolve(cpEntries);
            }
        } catch (IOException ioe) {
            throw new IllegalArgumentException("invalid class format");
        }
    }

    private CPEntry getConstantPoolEntry(ConstantPoolReader cpr)
            throws IOException {
        CPEntry newEntry = null;
        byte type = cpr.readByte();
        switch (type) {
          case CONSTANT_Utf8:
              newEntry = new CPUTF8Info(this, cpr.readRawUTF());
              break;

          case CONSTANT_Integer:
              newEntry = new CPIntegerInfo(this, cpr.readInt());
              break;

          case CONSTANT_Float:
              newEntry = new CPFloatInfo(this, cpr.readFloat());
              break;

          case CONSTANT_Long:
              newEntry = new CPLongInfo(this, cpr.readLong());
              break;

          case CONSTANT_Double:
              newEntry = new CPDoubleInfo(this, cpr.readDouble());
              break;

          case CONSTANT_Class: {
              int nameIndex = cpr.readShort();
              newEntry = new CPClassInfo(this, nameIndex);
              break;
          }

          case CONSTANT_String: {
              int nameIndex = cpr.readShort();
              newEntry = new CPStringInfo(this, nameIndex);
              break;
          }

          case CONSTANT_FieldRef: {
              int classIndex = cpr.readShort();
              int natIndex = cpr.readShort();
              newEntry = new CPFieldInfo(this, classIndex, natIndex);
              break;
          }

          case CONSTANT_MethodRef: {
              int classIndex = cpr.readShort();
              int natIndex = cpr.readShort();
              newEntry = new CPMethodInfo(this, classIndex, natIndex);
              break;
          }

          case CONSTANT_InterfaceMethodRef: {
              int classIndex = cpr.readShort();
              int natIndex = cpr.readShort();
              newEntry = new CPInterfaceMethodInfo(this, classIndex, natIndex);
              break;
          }

          case CONSTANT_NameAndType: {
              int nameIndex = cpr.readShort();
              int descIndex = cpr.readShort();
              newEntry = new CPNameAndTypeInfo(this, nameIndex, descIndex);
              break;
          }

          default:
              throw new IllegalArgumentException(
                          "invalid constant pool type: " + type);
        }

        if (newEntry == null) {
            throw new InternalError("assertion failure");
        }              
        return newEntry;
    }
}
