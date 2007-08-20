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

package org.netbeans.modules.cnd.dwarfdump.reader;

import java.io.ByteArrayInputStream;
import org.netbeans.modules.cnd.dwarfdump.dwarfconsts.ElfConstants;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * I decided not to extend RandomAccessFile because in this case I cannot
 * overwrite readXXX() methods (they are final in RandomAccessFile).
 * But we have to deal with bytes order...
 *
 * @author ak119685
 */
public class ByteStreamReader implements DataInput {
    private RandomAccessFile file = null;
    private String fileName;
    private int dataEncoding = 0;
    private int fileClass = 0;
    private byte address_size = -1;
    
    public static final int LSB = 1;
    public static final int MSB = 2;
    
    public ByteStreamReader(String fname, RandomAccessFile reader) {
        file = reader;
        this.fileName = fname;
    }
    
    public void dispose(){
        if (file != null) {
            try {
                file.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            file = null;
        }
    }
    
    public String getFileName() {
        return fileName;
    }
    
    public void setDataEncoding(int encoding) {
        if (encoding == LSB || encoding == MSB) {
            dataEncoding = encoding;
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
        byte[] bytes = new byte[size];
        long n = 0;
        
        file.readFully(bytes);
        
        for (int i = 0; i < size; i++) {
            long u = 0;
            
            if (dataEncoding == LSB) {
                u = (0xff & bytes[i]);
            } else {
                u = (0xff & bytes[size - i - 1]);
            }
            
            n |= (u << (i * 8));
        }
        
        return n;
    }
    
    public short readShort() throws IOException {
        return (short)readNumber(2);
    }
    
    public int readInt() throws IOException {
        return (int)readNumber(4);
    }
    
    public int readInt(boolean useEncoding) throws IOException {
        if (useEncoding) {
            return readInt();
        } else {
            return file.readInt();
        }
    }
    
    public long readDWlen() throws IOException {
        long res = readInt();
        if (res == -1) {
            res = readLong();
        }
        return res;
    }
    
    public long readLong() throws IOException {
        return readNumber(8);
    }
    
    public byte[] read(byte b[]) throws IOException {
        readFully(b);
        return b;
    }
    
    public void readFully(byte[] b) throws IOException {
        file.readFully(b);
    }
    
    public void readFully(byte[] b, int off, int len) throws IOException {
        file.readFully(b, off, len);
    }
    
    public int skipBytes(int n) throws IOException {
        return file.skipBytes(n);
    }
    
    public boolean readBoolean() throws IOException {
        return file.readBoolean();
    }
    
    public byte readByte() throws IOException {
        return file.readByte();
    }
    
    public int readUnsignedByte() throws IOException {
        return file.readUnsignedByte();
    }
    
    public int readUnsignedShort() throws IOException {
        return file.readUnsignedShort();
    }
    
    public char readChar() throws IOException {
        return file.readChar();
    }
    
    public float readFloat() throws IOException {
        return file.readFloat();
    }
    
    public double readDouble() throws IOException {
        return file.readDouble();
    }
    
    public String readLine() throws IOException {
        return file.readLine();
    }
    
    public String readUTF() throws IOException {
        return file.readUTF();
    }
    
    /**
     * Reads null-padded limited-lenght UTF-8 string from file. If the string is
     * exactly len characters long, terminating null is not required.
     * Note: this function will read len bytes from the stream in ANY case.
     * @param len bytes to be read
     * @return Read string
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
        byte b = -1;
        
        while (b != 0) {
            b = readByte();
            
            if (b != 0) {
                str.append((char)b);
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
        return (fileClass == ElfConstants.ELFCLASS32) ? readInt() : readLong();
    }
    
}
