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

/*
 * ClassInfo.java
 *
 * Created on September 29, 2006, 11:43 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.compapp.javaee.annotation;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Based on Glassfish ClassFile and ConstantPoolInfo.
 **/
public class ClassInfo {    
    public ClassInfo() {
    }

    public static boolean containsAnnotation(ReadableByteChannel in, long size, Set annotations) throws IOException {
        short majorVersion;
        short minorVersion;
        short accessFlags;
        boolean isValidClass = false;

        ByteBuffer header = ByteBuffer.allocate(12000);        
        /**
         * this is the .class file layout
         *
        ClassFile {
            u4 magic;
            u2 minor_version;
            u2 major_version;
            u2 constant_pool_count;
            cp_info constant_pool[constant_pool_count-1];
            u2 access_flags;
            u2 this_class;
            u2 super_class;
            u2 interfaces_count;
            u2 interfaces[interfaces_count];
            u2 fields_count;
            field_info fields[fields_count];
            u2 methods_count;
            method_info methods[methods_count];
            u2 attributes_count;
            attribute_info attributes[attributes_count];
        }        
         **/
        header.clear();   
        if (size!=-1 && size>header.capacity()) {
            // time to expand...
            header = ByteBuffer.allocate((int) size);
        }
        long read = (long) in.read(header);
        if (size!=-1 && read!=size) {
            return false;            
        }        
        header.rewind();
                
        if (header.getInt()!=magic) {
            return false;
        }
        
        majorVersion = header.getShort();
        minorVersion = header.getShort();
        int constantPoolSize = header.getShort();

        return constantPoolContainsAnnotation(constantPoolSize, header, annotations);
        
    }

    private static boolean constantPoolContainsAnnotation(int constantPoolSize, final ByteBuffer buffer, Set annotations) throws IOException  {
        byte[] bytes = new byte[Short.MAX_VALUE];        
        boolean ret = false;
        for (int i=1;i<constantPoolSize;i++) {
            final byte type = buffer.get();
            switch(type) {
                case ASCIZ:
                case UNICODE:
                    final short length = buffer.getShort();
                    if (length<0 || length>Short.MAX_VALUE) {
                        return true;
                    }
                    buffer.get(bytes, 0, length);
                    /* to speed up the process, I am comparing the first few
                     * bytes to Ljava since all annotations are in the java
                     * package, the reduces dramatically the number or String
                     * construction
                     */
                    if (bytes[0]=='L' && bytes[1]=='j' && bytes[2]=='a') {
                        String stringValue;
                        if (type==ASCIZ) {
                            stringValue = new String(bytes, 0, length,"US-ASCII");
                        } else {
                            stringValue = new String(bytes, 0, length);
                        }

                        if (annotations.contains(stringValue)) {
                                return true;
                        }
                    }
                    break;
                case CLASS:
                case STRING:
                    buffer.getShort();
                    break;
                case FIELDREF:
                case METHODREF:
                case INTERFACEMETHODREF:
                case INTEGER:
                case FLOAT:
                    buffer.position(buffer.position()+4);
                    break;
                case LONG:
                case DOUBLE:
                    buffer.position(buffer.position()+8);
                    // for long, and double, they use 2 constantPool 
                    i++;
                    break;
                case NAMEANDTYPE:
                    buffer.getShort();
                    buffer.getShort();
                    break;
                default:
                    Logger.getLogger(ClassInfo.class.getName()).severe("Unknow type constant pool " + type + " at position" + i);
                    break;
            }
        }
        return false;        
    }
        
    private static final int magic = 0xCAFEBABE;
    public static final int ACC_PUBLIC 		= 0x1;
    public static final int ACC_PRIVATE 	= 0x2;
    public static final int ACC_PROTECTED 	= 0x4;
    public static final int ACC_STATIC 		= 0x8;
    public static final int ACC_FINAL 		= 0x10;
    public static final int ACC_SYNCHRONIZED 	= 0x20;
    public static final int ACC_THREADSAFE 	= 0x40;
    public static final int ACC_TRANSIENT 	= 0x80;
    public static final int ACC_NATIVE 		= 0x100;
    public static final int ACC_INTERFACE 	= 0x200;
    public static final int ACC_ABSTRACT 	= 0x400;
    
    public static final byte CLASS = 7;
    public static final int FIELDREF = 9;
    public static final int METHODREF = 10;
    public static final int STRING = 8;
    public static final int INTEGER = 3;
    public static final int FLOAT = 4;
    public static final int LONG = 5;
    public static final int DOUBLE = 6;
    public static final int INTERFACEMETHODREF = 11;
    public static final int NAMEANDTYPE = 12;
    public static final int ASCIZ = 1;
    public static final int UNICODE = 2;        
}
