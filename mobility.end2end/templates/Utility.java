/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of
 * the License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing
 * the software, include this License Header Notice in each file and include
 * the License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by
 * Oracle in the GPL Version 2 section of the License file that accompanied
 * this code. If applicable, add the following below the License Header, with
 * the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license." If you do not indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to its
 * licensees as provided above. However, if you add GPL Version 2 code and
 * therefore, elected the GPL Version 2 license, then the option applies only
 * if the new code is made subject to such option by the copyright holder.
 */
# import java.io.File;
# import org.netbeans.mobility.end2end.core.model.*;
# import org.netbeans.mobility.end2end.core.model.classdata.ClassData;
# import org.netbeans.mobility.end2end.core.model.protocol.Serializer;
# import org.netbeans.mobility.end2end.core.model.protocol.binary.ComplexTypeSerializer;
# import org.netbeans.mobility.end2end.core.model.protocol.binary.PrimitiveTypeSerializer;
# import java.util.*;
# ProtocolSupport support = new ProtocolSupport(data, this, false);
# setOut(support.getServletSupportPath("Utility"));
# getOutput().addCreatedFile( support.getServletSupportPath("Utility"));

package ${support.serverSupportPackage()};

import java.io.*;
import java.lang.reflect.*;

/**
 *  This class is used as an external protocol utility. It is so we don't
 *  generate as much code.
 */

public class Utility {
    
# ClassData[] supportedParamTypes = data.getParameterTypes();
# ClassData[] supportedReturnTypes = data.getReturnTypes();
# Set allTypes = new HashSet();
# allTypes.addAll(Arrays.asList(supportedParamTypes));
# allTypes.addAll(Arrays.asList(supportedReturnTypes));
# Set includedTypes = new HashSet();
# boolean arrayTypeSupported = false;
# for (Iterator i = allTypes.iterator(); i.hasNext(); ) {
#   ClassData type = (ClassData)i.next();
#   if (type.isArray()) {
#     arrayTypeSupported = true;
#     while (type.isArray()) {
#       type = type.getComponentType();
#     }
#   }
#   if (!includedTypes.contains(type)) {
#     int typeIndex = data.getValueForType(type);
#     Serializer s = type.getSerializer();
#     if( s instanceof PrimitiveTypeSerializer && !type.isPrimitive()) {
#       continue;
#     }
#     // reset the type strings to be only the constant
#     s.setType(null);
    private final static short ${s.getTypeConstant()} = ${typeIndex};
#     includedTypes.add(type);
#   }
# } // end iteration over allTypes

    /** Type value for arrays. */
    private final static short ARRAY_TYPE = -2;
    
    /** Marker for null. Null is a type and a value together. */
    private final static short NULL_TYPE = -1;

    /** Marker for void return types. */
    public final static Object VOID_VALUE = new Object();

    /** Compute the type value for a given class
     * @param clazz An object to be send to the client
     * @return a type value to identify the object
     */
    private static short getType(Class clazz) {
        String className = clazz.getName();
# Set baseReturnTypes = new HashSet();
# for (int i = 0; i < supportedReturnTypes.length; i++) {
#   ClassData type = supportedReturnTypes[i];
#   while (type.isArray()) {
#     type = type.getComponentType();
#   }
#   baseReturnTypes.add(type);
# }
# for (Iterator i = baseReturnTypes.iterator(); i.hasNext(); ) {
#   ClassData type = (ClassData) i.next();
#   Serializer s = type.getSerializer();
        if (className.equals("${type.getClassName()}")) {
            return ${s.getTypeConstant()};
        }
# }
# if (data.isArrayReturnTypeSupported()) {
        if (className.startsWith("[")) {
            return ARRAY_TYPE;
        }
# } // end is array return type supported

        throw new IllegalArgumentException("Unsupported type (" + className + ")");
    }

# if (data.isArrayParameterSupported()) {
    /**
     * Reads an array from the given data source and returns it.
     *
     *@param  source           The source from which the data is extracted.
     *@return                  The array from the data source
     *@exception  IOException  If an error occured while reading the data.
     */
    public static Object readArray(DataInput in) throws IOException {
        short type = in.readShort();
        int length = in.readInt();
        if (length == -1) {
            return null;
        } else {
            switch (type) {
#   for (int i = 0; i < supportedParamTypes.length; i++) {
#     ClassData type = supportedParamTypes[i];
#     if (type.isArray()) {
#       while (type.isArray()) {
#         type = type.getComponentType();
#       }
#       Serializer s = type.getSerializer();
                case ${s.getTypeConstant()}: {
                    ${type.getShortClassName()}[] data = new ${type.getShortClassName()}[length];
#       if (type.isPrimitive()) {
#         // special optimization for byte[]
#         if (type.equalsClass(Byte.TYPE)) {
                        in.readFully(data);
#         } else {
                    for (int i = 0; i < length; i++) {
                        ${s.read("in", false, "data[i]")};
                    }
#         }
#       } else {
                    for (int i = 0; i < length; i++) {
                        data[i] = (${type.getShortClassName()}) readObject(in);
                    }
#       }
                        return data;
                    }
#     }
#   }
                default: {
                    throw new IllegalArgumentException("Unsupported return type (" + type + ")");
                }
            }
        }
    }
# } // isArrayParameterSupported()
    
    /**
     *  Sends return values to the client output stream.
     *
     *@param  output           The output stream into which all the data should be
     *      written
     *@param  returnValues     The values which we should write into the stream
     *@exception  IOException  If an error occured while writing the results
     */
    public static void writeResults( DataOutput output, Object[] returnValues) throws IOException {
        for ( int i = 0; i < returnValues.length; i++ ) {
            writeObject(output, returnValues[i]);
        }
    }
    
    public static void writeObject(DataOutput output, Object o) throws IOException {
        if (o == null) { // return null
            output.writeShort(NULL_TYPE);
# if (data.isReturnTypeSupported(Void.TYPE)) {
        } else if (o == VOID_VALUE) { // return void
            output.writeShort(VOID_TYPE);
# } // end is void return type supported
# Set writableTypes = new HashSet();
# for( int i = 0; i < supportedReturnTypes.length; i++ ) {
#   ClassData type = supportedReturnTypes[i];    
#   if( !type.isPrimitive() && type.getSerializer() instanceof PrimitiveTypeSerializer ) {
#     continue;
#   }
#   writableTypes.add( type );
# }
# if (data.isArrayReturnTypeSupported()) {
        } else if (o.getClass().isArray()) {
            output.writeShort(ARRAY_TYPE);
            short elementType = getType(o.getClass().getComponentType());
            output.writeShort(elementType);
            switch (elementType) {
#   for( Iterator it = writableTypes.iterator(); it.hasNext(); ) {
#     ClassData type = (ClassData)it.next();
#     if (type.isArray()) {
#       type = type.getComponentType();
#       if (type.isPrimitive()) {
#         Serializer s = type.getSerializer();
#         if( s instanceof ComplexTypeSerializer ) {
#             ((ComplexTypeSerializer)s).setClient( false );
#         } 
                case ${s.getTypeConstant()}: {
                    ${type.getShortClassName()}[] array = (${type.getShortClassName()}[]) o;
                    output.writeInt(array.length);
                    for (int i = 0; i < array.length; i++) {
                        ${s.writeObject("output", "array[i]")}
                    }
                    break;
                }
#       }
#     }
#   }
                default: {
                    int length = Array.getLength(o);
                    output.writeInt(length);
                    for (int i = 0; i < length; i++) {
                        writeObject(output, Array.get(o, i));
                    }
                }
 
            }
# } // end isArrayReturnTypeSupported
# for( Iterator it = writableTypes.iterator(); it.hasNext(); ) {
#       ClassData type = (ClassData)it.next();
#       if (type.isArray()) {
#           continue;
#       }
#       String typeName;
#       if (type.isPrimitive()) {
#           typeName = type.getWrapperType().getShortClassName();
#       } else {
#           typeName = type.getShortClassName();
#       }
#       Serializer s = type.getSerializer();
#       if( s instanceof ComplexTypeSerializer ) {
#           ((ComplexTypeSerializer)s).setClient( false );
#       } 
        } else if (o instanceof ${typeName}) {
            ${s.writeType("output")}
            ${s.writeObject("output", support.castFromReference("o", type))}
# }
        }
    }

    static Object toObject(byte b) {
        return new Byte(b);
    }

    static Object toObject(short s) {
        return new Short(s);
    }

    static Object toObject(char c) {
        return new Character(c);
    }

    static Object toObject(int i) {
        return new Integer(i);
    }

    static Object toObject(long l) {
        return new Long(l);
    }

    static Object toObject(float f) {
        return new Float(f);
    }

    static Object toObject(double d) {
        return new Double(d);
    }

    static Object toObject(boolean b) {
        return new Boolean(b);
    }
    
    static Object toObject(Object o) {
        return o;
    }    

    protected static Object readObject(DataInput in) throws IOException {
        short type = in.readShort();
#//        short length;
                
        switch (type) {
#//     for (int i = 0; i < supportedReturnTypes.length; i++) {
#//       ClassData type = supportedReturnTypes[i];
#     Set readableTypes = new HashSet();
#     for( int i = 0; i < supportedParamTypes.length; i++ ) {
#       ClassData type = supportedParamTypes[i];    
#       if( !type.isPrimitive() && type.getSerializer() instanceof PrimitiveTypeSerializer ) {
#         continue;
#       }
#       readableTypes.add( type );
#     }
#     for( Iterator it = readableTypes.iterator(); it.hasNext(); ) {
#       ClassData type = (ClassData)it.next();
#       String typeName;
#       if (type.isPrimitive()) {
#           typeName = type.getWrapperType().getShortClassName();
#       } else {
#           typeName = type.getShortClassName();
#       }
#       if (!type.isArray()) {
#         Serializer s = type.getSerializer();
#         if( s instanceof ComplexTypeSerializer ) {
#            ((ComplexTypeSerializer)s).setClient( false );
#         } 
#         if( s instanceof ComplexTypeSerializer || type.isPrimitive()) {
            case ${s.getTypeConstant()}: /* ${typeName} */
                ${s.readAndReturnObject("in")};
#         }
#       }
#   }
# if (data.isArrayParameterSupported()) {
            case ARRAY_TYPE:
                return readArray(in);
# }
                case NULL_TYPE: /* null */
                return null;
            default:
                throw new IllegalArgumentException(
                        "Unsupported return type (" + type + ")");
        }
    }
    
    public static class CompositeDataOutput implements DataOutput {
        private DataOutput[] outputs;

        public CompositeDataOutput(DataOutput[] outputs) {
            this.outputs = (DataOutput[]) outputs.clone();
        }

        public void write(int b) throws IOException {
            this.writeByte(b);
        }
        public void write(byte[] b) throws IOException {
            this.write(b, 0, b.length);
        }
        public void write(byte[] b, int offset, int len) throws IOException {
            for (int i = 0; i < outputs.length; i++) {
                outputs[i].write(b, offset, len);
            }
        }
        public void writeBoolean(boolean b) throws IOException {
            for (int i = 0; i < outputs.length; i++) {
                outputs[i].writeBoolean(b);
            }
        }
        public void writeByte(int b) throws IOException {
            for (int i = 0; i < outputs.length; i++) {
                outputs[i].writeByte(b);
            }
        }
        public void writeBytes(String s) throws IOException {
            for (int i = 0; i < outputs.length; i++) {
                outputs[i].writeBytes(s);
            }
        }
        public void writeChar(int c) throws IOException {
            for (int i = 0; i < outputs.length; i++) {
                outputs[i].writeChar(c);
            }
        }
        public void writeChars(String s) throws IOException {
            for (int i = 0; i < outputs.length; i++) {
                outputs[i].writeChars(s);
            }
        }
        public void writeDouble(double d) throws IOException {
            for (int i = 0; i < outputs.length; i++) {
                outputs[i].writeDouble(d);
            }
        }
        public void writeFloat(float f) throws IOException {
            for (int i = 0; i < outputs.length; i++) {
                outputs[i].writeFloat(f);
            }
        }
        public void writeInt(int value) throws IOException {
            for (int i = 0; i < outputs.length; i++) {
                outputs[i].writeInt(value);
            }
        }
        public void writeLong(long l) throws IOException {
            for (int i = 0; i < outputs.length; i++) {
                outputs[i].writeLong(l);
            }
        }
        public void writeShort(int s) throws IOException {
            for (int i = 0; i < outputs.length; i++) {
                outputs[i].writeShort(s);
            }
        }
        public void writeUTF(String s) throws IOException {
            for (int i = 0; i < outputs.length; i++) {
                outputs[i].writeUTF(s);
            }
        }
    }

    public static class TracedDataOutput implements DataOutput {
        private PrintStream log;
        public TracedDataOutput(PrintStream log) {
            this.log = log;
        }
        public void write(int b) throws IOException {
            this.writeByte(b);
        }
        public void write(byte[] b) throws IOException {
            this.write(b, 0, b.length);
        }
        public void write(byte[] b, int offset, int len) throws IOException {
            log.println("Writing " + len + " bytes");
        }
        public void writeBoolean(boolean b) throws IOException {
            log.println("Writing boolean: " + b);
        }
        public void writeByte(int b) throws IOException {
            log.println("Writing byte: " + b);
        }
        public void writeBytes(String s) throws IOException {
            log.println("Writing bytes: '" + s + "'");
        }
        public void writeChar(int c) throws IOException {
            log.println("Writing char: " + c);
        }
        public void writeChars(String s) throws IOException {
            log.println("Writing chars: '" + s + "'");
        }
        public void writeDouble(double d) throws IOException {
            log.println("Writing double: " + d);
        }
        public void writeFloat(float f) throws IOException {
            log.println("Writing float: " + f);
        }
        public void writeInt(int i) throws IOException {
            log.println("Writing integer: " + i);
        }
        public void writeLong(long l) throws IOException {
            log.println("Writing long: " + l);
        }
        public void writeShort(int s) throws IOException {
            log.println("Writing short: " + s);
        }
        public void writeUTF(String s) throws IOException {
            log.println("Writing string: '" + s + "'");
        }
    }

}
