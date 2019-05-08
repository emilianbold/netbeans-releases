/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.cnd.dwarfdump.reader;

import java.io.ByteArrayInputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteOrder;
import java.util.logging.Level;
import org.netbeans.modules.cnd.dwarfdump.Dwarf;
import org.netbeans.modules.cnd.dwarfdump.dwarfconsts.ElfConstants;

/**
 * I decided not to extend RandomAccessFile because in this case I cannot
 * overwrite readXXX() methods (they are final in RandomAccessFile).
 * But we have to deal with bytes order...
 *
 */
public class ByteStreamReader implements DataInput {
    private MyRandomAccessFile file = null;
    private final String fileName;
    private int dataEncoding = 0;
    private int fileClass = 0;
    private byte address_size = -1;
    
    public static final int LSB = 1;
    public static final int MSB = 2;
    private final byte[] buffer = new byte[8];
    
    public ByteStreamReader(String fname, MyRandomAccessFile reader) {
        file = reader;
        this.fileName = fname;
    }
    
    public void dispose(){
        if (file != null) {
            try {
                file.close();
            } catch (IOException ex) {
                Dwarf.LOG.log(Level.INFO, "Cannot close "+fileName, ex); //NOI18N
            }
            file = null;
        }
    }
    
    public String getFileName() {
        return fileName;
    }
    
    public void setDataEncoding(int encoding) throws IOException {
        if (encoding == LSB || encoding == MSB) {
            dataEncoding = encoding;
            if (encoding == LSB) {
                file.getBuffer().order(ByteOrder.LITTLE_ENDIAN);
            } else {
                file.getBuffer().order(ByteOrder.BIG_ENDIAN);
            }
        } else {
            throw new IllegalArgumentException("Wrong Data Encoding specified (" + encoding + ")."); // NOI18N
        }
    }
    
    public int getDataEncoding() {
        return dataEncoding;
    }
    
    public void seek(long pos) throws IOException {
        file.seek(pos);
    }
    
    public long getFilePointer() throws IOException {
        return file.getFilePointer();
    }
    
    public long length() throws IOException {
        return file.length();
    }
    
    public void setAddressSize(byte size) {
        address_size = size;
    }
    
    public byte getAddressSize() {
        return address_size;
    }
    
    public long readNumber(int size) throws IOException {
        assert size <= 8;
        long n = 0;
        
        file.readFully(buffer, 0, size);
        
        for (int i = 0; i < size; i++) {
            long u;
            
            if (dataEncoding == LSB) {
                u = (0xff & buffer[i]);
            } else {
                u = (0xff & buffer[size - i - 1]);
            }
            
            n |= (u << (i * 8));
        }
        
        return n;
    }
    
    @Override
    public short readShort() throws IOException {
        if (file.remaining() >= 2) {
            return file.getBuffer().getShort();
        } else {
            return (short)readNumber(2);
        }
    }
    
    @Override
    public int readInt() throws IOException {
        if (file.remaining() >= 4) {
            return file.getBuffer().getInt();
        } else {
            return (int)readNumber(4);
        }
    }
    
    public long readDWlen() throws IOException {
        long res = readInt();
        if (res == -1) {
            res = readLong();
        }
        return res;
    }
    
    @Override
    public long readLong() throws IOException {
        if (file.remaining() >= 8) {
            return file.getBuffer().getLong();
        } else {
            return readNumber(8);
        }
    }
    
    public byte[] read(byte b[]) throws IOException {
        readFully(b);
        return b;
    }
    
    @Override
    public void readFully(byte[] b) throws IOException {
        if (file.remaining() >= b.length) {
            file.getBuffer().get(b);
        } else {
            file.readFully(b);
        }
    }
    
    @Override
    public void readFully(byte[] b, int off, int len) throws IOException {
        if (file.remaining() >= len) {
            file.getBuffer().get(b, off, len);
        } else {
            file.readFully(b, off, len);
        }
    }
    
    @Override
    public int skipBytes(int n) throws IOException {
        return file.skipBytes(n);
    }
    
    @Override
    public boolean readBoolean() throws IOException {
        return file.readBoolean();
    }
    
    @Override
    public byte readByte() throws IOException {
        if (file.remaining() >= 1) {
            return file.getBuffer().get();
        } else {
            return file.readByte();
        }
    }
    
    @Override
    public int readUnsignedByte() throws IOException {
        return file.readUnsignedByte();
    }
    
    @Override
    public int readUnsignedShort() throws IOException {
        return file.readUnsignedShort();
    }
    
    @Override
    public char readChar() throws IOException {
        return file.readChar();
    }
    
    @Override
    public float readFloat() throws IOException {
        return file.readFloat();
    }
    
    @Override
    public double readDouble() throws IOException {
        return file.readDouble();
    }
    
    @Override
    public String readLine() throws IOException {
        return file.readLine();
    }
    
    @Override
    public String readUTF() throws IOException {
        return file.readUTF();
    }
    
    /**
     * Reads null-padded limited-lenght UTF-8 string from file. If the string is
     * exactly len characters long, terminating null is not required.
     * Note: this function will read len bytes from the stream in ANY case.
     * @param len bytes to be read
     * @return Read string
     * @throws java.io.IOException
     */
    public String readUTF(int len) throws IOException {
        /* 
         * ReadUTF from DataInputStream will be used for reading. 
         * This assumes that string is represented in modified UTF-8 encoding 
         * of a Unicode string in a file. This, in case, assumes that first 2 
         * bytes indicate length of the string.
         * So, add 2 bytes and add length
         */ 
        
        byte[] bytes = new byte[len + 2];
        bytes[1] = (byte)(0xFF & len);
        bytes[0] = (byte)(0xFF & (len >> 8));
        readFully(bytes, 2, len);
        
        return new DataInputStream(new ByteArrayInputStream(bytes)).readUTF();
    }
    
    // Little Endian Base 128 (LEB128)
    private int readLEB128(boolean signed) throws IOException {
        int result = 0;
        int shift = 0;
        int b = 0x80;
        
        while ((0x80 & b) != 0) {
            b = file.readByte();
            result |= ((0x7f & b) << shift);
            shift += 7;
        }
        
        if (signed && shift < 32 && (0x40 & b) != 0) {
            result |= - (1 << shift);
        }
        
        return result;
    }
    
    public int readUnsignedLEB128() throws IOException {
        return readLEB128(false);
    }
    
    public int readSignedLEB128() throws IOException {
        return readLEB128(true);
    }
    
    public int getFileClass() {
        return fileClass;
    }
    
    public void setFileClass(int fileClass) {
        if (fileClass == ElfConstants.ELFCLASS32 || fileClass == ElfConstants.ELFCLASS64) {
            this.fileClass = fileClass;
        } else {
            throw new IllegalArgumentException("Wrong File Class specified (" + fileClass + ")."); // NOI18N
        }
    }
    
    public String readString() throws IOException {
        StringBuilder str = new StringBuilder();
        long beg = getFilePointer();
        int b = -1;
        boolean isUTF = false;
        while (b != 0) {
            b = readByte() & 0xFF;
            
            if (b != 0) {
                str.append((char)b);
            }
            if (b > 127) {
                isUTF = true;
            }
        }

        if (isUTF) {
            long end = getFilePointer();
            seek(beg);
            try {
                String s = readUTF(str.length());
                return s;
            } catch (IOException ex) {
                return str.toString();
            } finally {
                seek(end);
            }
        }
        
        return str.toString();
    }
    
    public boolean is32Bit() {
        return fileClass == ElfConstants.ELFCLASS32;
    }
    
    public boolean is64Bit() {
        return fileClass == ElfConstants.ELFCLASS64;
    }
    
    public long read3264() throws IOException {
        return (fileClass == ElfConstants.ELFCLASS32) ? ByteStreamReader.uintToLong(readInt()) : readLong();
    }
    
    public static int ubyteToInt(byte value) {
        return 0xFF & value;
    }

    public static int ushortToInt(short value) {
        return 0xFFFF & value;
    }

    public static long uintToLong(int value) {
        return 0xFFFFFFFFL & value;
    }

}
