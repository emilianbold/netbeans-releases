/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.debugger.jpda.projects;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import org.openide.util.Exceptions;

/**
 * Structured representation of the class constant pool.
 *
 * @author Martin Entlicher
 */
public class ConstantPool {

    private static final byte TAG_UTF8 = 1;
    private static final byte TAG_INTEGER = 3;
    private static final byte TAG_FLOAT = 4;
    private static final byte TAG_LONG = 5;
    private static final byte TAG_DOUBLE = 6;
    private static final byte TAG_CLASS = 7;
    private static final byte TAG_STRING = 8;
    private static final byte TAG_FIELDREF = 9;
    private static final byte TAG_METHODREF = 10;
    private static final byte TAG_INTERFACEREF = 11;
    private static final byte TAG_NAMETYPE = 12;

    private final List<ConstantPool.Entry> entries;

    private ConstantPool(List<ConstantPool.Entry> entries) {
        this.entries = entries;
    }

    public ConstantPool.Entry getEntry(int index) {
        return entries.get(index);
    }

    public String getMethodName(int index) {
        EntryFieldMethodRef methodRef = (EntryFieldMethodRef) entries.get(index);
        return ((EntryUTF8) entries.get(((EntryNameType) entries.get(methodRef.nameAndTypeIndex)).getNameIndex())).getUTF8();
    }

    public static ConstantPool parse(byte[] bytes) {
        List<ConstantPool.Entry> entries = new ArrayList<ConstantPool.Entry>();
        DataInputStream in = new DataInputStream(new ByteArrayInputStream(bytes));
        entries.add(new EntryNULL());
        try {
            do {
                byte tagByte;
                try {
                    tagByte = in.readByte();
                } catch (EOFException eof) {
                    break;
                }
                ConstantPool.Entry entry;
                switch(tagByte) {
                    case TAG_UTF8:
                        entry = new EntryUTF8(in.readUTF());
                        break;
                    case TAG_INTEGER:
                        entry = new EntryInteger(in.readInt());
                        break;
                    case TAG_LONG:
                        entry = new EntryLong(in.readLong());
                        entries.add(entry);
                        entry = new EntryNULL(); // Long takes TWO constant pool entries!?!
                        break;
                    case TAG_FLOAT:
                        entry = new EntryFloat(in.readFloat());
                        break;
                    case TAG_DOUBLE:
                        entry = new EntryDouble(in.readDouble());
                        entries.add(entry);
                        entry = new EntryNULL(); // Double takes TWO constant pool entries!?!
                        break;
                    case TAG_CLASS:
                        entry = new EntryClass(in.readShort());
                        break;
                    case TAG_STRING:
                        entry = new EntryString(in.readShort());
                        break;
                    case TAG_NAMETYPE:
                        entry = new EntryNameType(in.readShort(), in.readShort());
                        break;
                    case TAG_FIELDREF:
                    case TAG_METHODREF:
                    case TAG_INTERFACEREF:
                        entry = new EntryFieldMethodRef(tagByte, in.readShort(), in.readShort());
                        break;
                    default:
                        Logger.getLogger(ConstantPool.class.getName()).warning("Unknown tag byte: "+tagByte);
                        entry = new EntryNULL();
                }
                entries.add(entry);
            } while(true);
        } catch (IOException ioex) {
            // Should not occur
            Exceptions.printStackTrace(ioex);
        }
        return new ConstantPool(entries);
    }


    // Entries inner classes

    public static abstract class Entry {

        private final byte tag;

        protected Entry(byte tag) {
            this.tag = tag;
        }

        public final byte getTag() {
            return tag;
        }
    }

    public static class EntryNULL extends Entry {
        public EntryNULL() {
            super((byte) 0);
        }
    }

    public static class EntryUTF8 extends Entry {

        private String utf8;

        public EntryUTF8(String utf8) {
            super(TAG_UTF8);
            this.utf8 = utf8;
        }

        public String getUTF8() {
            return utf8;
        }
    }

    public static class EntryInteger extends Entry {

        private int i;

        public EntryInteger(int i) {
            super(TAG_INTEGER);
            this.i = i;
        }

        public int getInteger() {
            return i;
        }
    }

    public static class EntryLong extends Entry {

        private long l;

        public EntryLong(long l) {
            super(TAG_LONG);
            this.l = l;
        }

        public long getLong() {
            return l;
        }
    }

    public static class EntryFloat extends Entry {

        private float f;

        public EntryFloat(float f) {
            super(TAG_FLOAT);
            this.f = f;
        }

        public float getFloat() {
            return f;
        }
    }

    public static class EntryDouble extends Entry {

        private double d;

        public EntryDouble(double d) {
            super(TAG_DOUBLE);
            this.d = d;
        }

        public double getDouble() {
            return d;
        }
    }

    public static class EntryClass extends Entry {

        /** Refrence to TAG_UTF8 entry */
        private short classRef;

        public EntryClass(short classRef) {
            super(TAG_CLASS);
            this.classRef = classRef;
        }

        public short getClassRef() {
            return classRef;
        }
    }

    public static class EntryString extends Entry {

        /** Refrence to TAG_UTF8 entry */
        private short stringRef;

        public EntryString(short stringRef) {
            super(TAG_STRING);
            this.stringRef = stringRef;
        }

        public short getStringRef() {
            return stringRef;
        }
    }

    public static class EntryNameType extends Entry {

        private short nameIndex;
        private short descriptorIndex;

        public EntryNameType(short nameIndex, short descriptorIndex) {
            super(TAG_NAMETYPE);
            this.nameIndex = nameIndex;
            this.descriptorIndex = descriptorIndex;
        }

        public short getNameIndex() {
            return nameIndex;
        }

        public short getDescriptorIndex() {
            return descriptorIndex;
        }
    }

    public static class EntryFieldMethodRef extends Entry {

        private short classIndex;
        private short nameAndTypeIndex;

        public EntryFieldMethodRef(byte type, short classIndex, short nameAndTypeIndex) {
            super(type);
            this.classIndex = classIndex;
            this.nameAndTypeIndex = nameAndTypeIndex;
        }
    }
}
