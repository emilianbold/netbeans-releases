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

package org.netbeans.modules.classfile;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * VerificationTypeInfo structure, which is defined as a union structure and
 * used to define stack map frame structures.
 *
 * @author tball
 */
public abstract class VerificationTypeInfo {
    private int tag;
    
    public static final int ITEM_Top = 0;
    public static final int ITEM_Integer = 1;
    public static final int ITEM_Float = 2;
    public static final int ITEM_Double = 3;
    public static final int ITEM_Long = 4;
    public static final int ITEM_Null = 5;
    public static final int ITEM_UninitializedThis = 6;
    public static final int ITEM_Object = 7;
    public static final int ITEM_Uninitialized = 8;
    
    static VerificationTypeInfo loadVerificationTypeInfo(DataInputStream in, ConstantPool pool) 
      throws IOException {
        int tag = in.readUnsignedByte();
        switch (tag) {
            case ITEM_Top: return new TopVariableInfo();
            case ITEM_Integer: return new IntegerVariableInfo();
            case ITEM_Float: return new FloatVariableInfo();
            case ITEM_Long: return new LongVariableInfo();
            case ITEM_Double: return new DoubleVariableInfo();
            case ITEM_Null: return new NullVariableInfo();
            case ITEM_UninitializedThis: return new UninitializedThisVariableInfo();
            case ITEM_Object: {
                int cpool_index = in.readUnsignedShort();
                return new ObjectVariableInfo(pool.get(cpool_index));
            }
            case ITEM_Uninitialized: {
                int offset = in.readUnsignedShort();
                return new UninitializedVariableInfo(offset);
            }
            default:
                throw new InvalidClassFormatException("invalid verification_type_info tag: " + tag);
        }
    }
    
    /** Creates a new instance of VerificationTypeInfo */
    public VerificationTypeInfo(int tag) {
        this.tag = tag;
    }

    public int getTag() {
        return tag;
    }
    
    public static final class TopVariableInfo extends VerificationTypeInfo {
        TopVariableInfo() {
            super(ITEM_Top);
        }
    }
    
    public static final class IntegerVariableInfo extends VerificationTypeInfo {
        IntegerVariableInfo() {
            super(ITEM_Integer);
        }
    }
    
    public static final class FloatVariableInfo extends VerificationTypeInfo {
        FloatVariableInfo() {
            super(ITEM_Float);
        }
    }
    
    public static final class LongVariableInfo extends VerificationTypeInfo {
        LongVariableInfo() {
            super(ITEM_Long);
        }
    }
    
    public static final class DoubleVariableInfo extends VerificationTypeInfo {
        DoubleVariableInfo() {
            super(ITEM_Double);
        }
    }
    
    public static final class NullVariableInfo extends VerificationTypeInfo {
        NullVariableInfo() {
            super(ITEM_Null);
        }
    }
    
    public static final class UninitializedThisVariableInfo extends VerificationTypeInfo {
        UninitializedThisVariableInfo() {
            super(ITEM_UninitializedThis);
        }
    }
    
    public static final class ObjectVariableInfo extends VerificationTypeInfo {
        CPEntry cpEntry;
        ObjectVariableInfo(CPEntry entry) {
            super(ITEM_Object);
            cpEntry = entry;
        }
        
        public CPEntry getConstantPoolEntry() {
            return cpEntry;
        }
    }
    
    public static final class UninitializedVariableInfo extends VerificationTypeInfo {
        int offset;
        UninitializedVariableInfo(int offset) {
            super(ITEM_Object);
            this.offset = offset;
        }
        
        public int getOffset() {
            return offset;
        }
    }
}
