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
 * A stack map frame, as defined by a StackMapTable attribute.  A stack map
 * frame is a union of frame structures, which are represented here by public
 * subclasses.
 *
 * @author tball
 */
public abstract class StackMapFrame {
    private int tag;
    
    static StackMapFrame[] loadStackMapTable(DataInputStream in, ConstantPool pool) 
      throws IOException {
        int n = in.readUnsignedShort();
        StackMapFrame[] entries = new StackMapFrame[n];
        for (int i = 0; i < n; i++) {
            int tag = in.readUnsignedByte();
            StackMapFrame frame = null;
            if (tag >= 0 && tag <= 64)
                frame = new SameFrame(tag);
            else if (tag >= 64 && tag <= 127) {
                VerificationTypeInfo typeInfo = 
                    VerificationTypeInfo.loadVerificationTypeInfo(in, pool);
                frame = new SameLocals1StackItemFrame(tag, typeInfo);
            }
            else if (tag >= 128 && tag <= 246)
                throw new InvalidClassFormatException("reserved stack map frame tag used: " + tag);
            else if (tag == 247) {
                int offset = in.readUnsignedShort();
                VerificationTypeInfo typeInfo = 
                    VerificationTypeInfo.loadVerificationTypeInfo(in, pool);
                frame = new SameLocals1StackItemFrameExtended(tag, offset, typeInfo);
            }
            else if (tag >= 248 && tag <= 250) {
                int offset = in.readUnsignedShort();
                frame = new ChopFrame(tag, offset);
            }
            else if (tag == 251) {
                int offset = in.readUnsignedShort();
                frame = new SameFrameExtended(tag, offset);
            }
            else if (tag >= 252 && tag <= 254)
                frame = makeAppendFrame(tag, in, pool);
            else /* tag == 255 */
                frame = makeFullFrame(in, pool);
            entries[i] = frame;
        }
        return entries;
    }
    
    private static AppendFrame makeAppendFrame(int tag, DataInputStream in, ConstantPool pool) 
      throws IOException {
        int offset = in.readUnsignedShort();
        VerificationTypeInfo[] locals = new VerificationTypeInfo[tag - 251];
        for (int i = 0; i < locals.length; i++)
            locals[i] = VerificationTypeInfo.loadVerificationTypeInfo(in, pool);
        return new AppendFrame(tag, offset, locals);
    }
    
    private static FullFrame makeFullFrame(DataInputStream in, ConstantPool pool) 
      throws IOException {
        int offset = in.readUnsignedShort();
        int n = in.readUnsignedShort();
        VerificationTypeInfo[] locals = new VerificationTypeInfo[n];
        for (int i = 0; i < locals.length; i++)
            locals[i] = VerificationTypeInfo.loadVerificationTypeInfo(in, pool);
        n = in.readUnsignedShort();
        VerificationTypeInfo[] stackItems = new VerificationTypeInfo[n];
        for (int i = 0; i < stackItems.length; i++)
            stackItems[i] = VerificationTypeInfo.loadVerificationTypeInfo(in, pool);
        return new FullFrame(255, offset, locals, stackItems);
    }
    
    /** Creates new StackMapFrame */
    StackMapFrame(int tag) {
        this.tag = tag;
    }

    private void loadLocalVariableEntry(DataInputStream in, ConstantPool pool) 
      throws IOException {
    }

    public final int getTag() {
        return tag;
    }
    
    public static final class SameFrame extends StackMapFrame {
        SameFrame(int tag) {
            super(tag);
        }
    }
    
    public static final class SameLocals1StackItemFrame extends StackMapFrame {
        VerificationTypeInfo typeInfo;
        SameLocals1StackItemFrame(int tag, VerificationTypeInfo typeInfo) {
            super(tag);
            this.typeInfo = typeInfo;
        }
        
        public VerificationTypeInfo getVerificationTypeInfo() {
            return typeInfo;
        }
    }
    
    public static final class SameLocals1StackItemFrameExtended extends StackMapFrame {
        int offset;
        VerificationTypeInfo typeInfo;
        SameLocals1StackItemFrameExtended(int tag, int offset, VerificationTypeInfo typeInfo) {
            super(tag);
            this.offset = offset;
            this.typeInfo = typeInfo;
        }
        
        public int getOffsetDelta() {
            return offset;
        }
        
        public VerificationTypeInfo getVerificationTypeInfo() {
            return typeInfo;
        }
    }
    
    public static final class ChopFrame extends StackMapFrame {
        int offset;
        ChopFrame(int tag, int offset) {
            super(tag);
            this.offset = offset;
        }
        
        public int getOffsetDelta() {
            return offset;
        }
    }
    
    public static final class SameFrameExtended extends StackMapFrame {
        int offset;
        SameFrameExtended(int tag, int offset) {
            super(tag);
            this.offset = offset;
        }
        
        public int getOffsetDelta() {
            return offset;
        }
    }
    
    public static final class AppendFrame extends StackMapFrame {
        int offset;
        VerificationTypeInfo[] locals;
        AppendFrame(int tag, int offset, VerificationTypeInfo[] locals) {
            super(tag);
            this.offset = offset;
            this.locals = locals;
        }
        
        public int getOffsetDelta() {
            return offset;
        }
        
        public VerificationTypeInfo[] getLocals() {
            return (VerificationTypeInfo[])locals.clone();
        }
    }
    
    public static final class FullFrame extends StackMapFrame {
        int offset;
        VerificationTypeInfo[] locals;
        VerificationTypeInfo[] stackItems;
        FullFrame(int tag, int offset, VerificationTypeInfo[] locals,
                  VerificationTypeInfo[] stackItems) {
            super(tag);
            this.offset = offset;
            this.locals = locals;
            this.stackItems = stackItems;
        }
        
        public int getOffsetDelta() {
            return offset;
        }
        
        public VerificationTypeInfo[] getLocals() {
            return (VerificationTypeInfo[])locals.clone();
        }
        
        public VerificationTypeInfo[] getStackItems() {
            return (VerificationTypeInfo[])stackItems.clone();
        }
    }
}
