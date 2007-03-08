/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 
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

package org.netbeans.modules.cnd.repository.testbench.sfs;

import java.io.*;
import java.nio.*;
import org.netbeans.modules.cnd.repository.sfs.BufferDataInput;
import org.netbeans.modules.cnd.repository.sfs.BufferDataOutput;

/**
 * Testing BufferDataInput and BufferDataOutput.
 * @author Vladimir Kvashin
 */
public class TestDataInputOutput {
    	    
    private static final boolean VERBOSE = false;
	    
    private ByteBuffer readBuffer;
    private ByteBuffer writeBuffer;
    private DataOutput out;
    private DataInput in;
	
    public void test() throws IOException {
	ByteBuffer buffer = ByteBuffer.allocate(1024*1024);
	readBuffer = buffer.slice();
	writeBuffer = buffer.slice();
	out = new BufferDataOutput(writeBuffer);
	in = new BufferDataInput(readBuffer);
	test(in, out);
    }
    
    private void rewind() {
	readBuffer.rewind();
	writeBuffer.rewind();
    }
    
    private void rewindIfNeed() {
	if( readBuffer.remaining() < 1024 || writeBuffer.remaining() < 1024 ) {
	    rewind();
	}
    }

    private void test(DataInput in, DataOutput out) throws IOException {
	testByte(in, out);		rewind();
	testBoolean(in, out);		rewind();
	testChar(in, out);		rewind();
	testLine(in, out);		rewind();
	testUTF(in, out);		rewind();
	testUnsignedByte(in, out);	rewind();
	testUnsignedShort(in, out);	rewind();
	testSkip(in, out);		rewind();
	testShort(in, out);		rewind();
	testInt(in, out);		rewind();
	testLong(in, out);		rewind();
	testFloat(in, out);		rewind();
	testDouble(in, out);		rewind();
    }

    private void testByte(DataInput in, DataOutput out) throws IOException {
	System.err.printf("Testing bytes (one by one)\n");
	for( int i = Byte.MIN_VALUE; i <= Byte.MAX_VALUE; i++ ) {
	    byte orig = (byte) i;
	    byte res;
	    if( VERBOSE )  System.err.printf("Testng %d\n", orig);
	    out.write(orig);
	    out.writeByte(orig);
	    res = in.readByte();
	    if( res != orig ) {
		System.err.printf("Error in byte(1): wrote %d , read %d\n", orig, res);
	    }
	    res = in.readByte();
	    if( res != orig ) {
		System.err.printf("Error in byte(2): wrote %d , read %d\n", orig, res);
	    }
	}
    }
    
    private void testBoolean(DataInput in, DataOutput out) throws IOException {
	System.err.printf("Testing booleans\n");
	boolean[] orig = new boolean[] { true, false };
	for (int i = 0; i < orig.length; i++) {
	    out.writeBoolean(orig[i]);
	    boolean res = in.readBoolean();
	    if( orig[i] != res ) {
		System.err.printf("Error in boolean: wrote %b , read %b\n", orig[i], res);
	    }
	}
    }
    
    private void testChar(DataInput in, DataOutput out) throws IOException {
	System.err.printf("Testing chars\n");
	for( int i = Character.MIN_VALUE; i <= Character.MAX_VALUE; i++ ) {
	    char orig = (char) i;
	    char res;
	    if( VERBOSE )  System.err.printf("Testng %c\n", orig);
	    out.writeChar(orig);
	    res = in.readChar();
	    if( res != orig ) {
		System.err.printf("Error in char: wrote %c [0x%H] , read %c [0x%H]\n", orig, orig, res, res);
	    }
	    rewindIfNeed();
	}
    }    

    private void testDouble(DataInput in, DataOutput out) throws IOException {
	System.err.printf("Testing doubles\n");
	for( double i = Double.MIN_VALUE; i <= Double.MAX_VALUE; i++ ) {
	    double orig = i;
	    out.writeDouble(orig);
	    double res = in.readDouble();
	    if( res != orig ) {
		System.err.printf("Error in double: wrote %e [0x%H] , read %e [0x%H]\n", orig, orig, res, res);
	    }
	    rewindIfNeed();
	}
    }    
    
    private void testFloat(DataInput in, DataOutput out) throws IOException {
	System.err.printf("Testing floats\n");
	for( float i = Float.MIN_VALUE; i <= Float.MAX_VALUE; i++ ) {
	    float orig = i;
	    out.writeFloat(orig);
	    float res = in.readFloat();
	    if( res != orig ) {
		System.err.printf("Error in float: wrote %e [0x%H] , read %e [0x%H]\n", orig, orig, res, res);
	    }
	    rewindIfNeed();
	}
    }        
    
    private void testLine(DataInput in, DataOutput out) throws IOException {
	System.err.printf("Testing readLine\n");
	String toWrite = "1\n22\r333\r\n4444\n\naaa\r\r";
	String[] reference = new String[] { "1", "22", "333", "4444", "", "aaa", "" };	
	for (int i = 0; i < toWrite.length(); i++) {
	    char c = toWrite.charAt(i);
	    byte b = (byte) (c & 0x00FF);
	    out.write(b);
	}
	
	for (int i = 0; i < reference.length; i++) {
	    String res = in.readLine();
	    if( ! reference[i].equals(res) ) {
		System.err.printf("Error in readLine: wrote \"%s\", read \"%s\"\n", reference[i], res);
	    }
	}
    }        
    
    private void testInt(DataInput in, DataOutput out) throws IOException {
	System.err.printf("Testing ints\n");
	for( int i = Integer.MIN_VALUE; i <= Integer.MAX_VALUE; i++ ){
	    int orig = (int) i;
	    int res;
	    if( VERBOSE )  System.err.printf("Testng %d\n", orig);
	    out.writeInt(orig);
	    res = in.readInt();
	    if( res != orig ) {
		System.err.printf("Error in int: wrote %d [0x%H] , read %d [0x%H]\n", orig, orig, res, res);
	    }
	    rewindIfNeed();
	}
    }    
    
    private void testLong(DataInput in, DataOutput out) throws IOException {
	System.err.printf("Testing longs\n");
	for( long orig = Long.MIN_VALUE; orig <= Long.MAX_VALUE; orig++ ) {
	    if( VERBOSE )  System.err.printf("Testng %d\n", orig);
	    out.writeLong(orig);
	    long res = in.readLong();
	    if( res != orig ) {
		System.err.printf("Error in long: wrote %d [0x%H] , read %d [0x%H]\n", orig, orig, res, res);
	    }
	    rewindIfNeed();
	}
    }
    
    private void testShort(DataInput in, DataOutput out) throws IOException {
	System.err.printf("Testing shorts\n");
	for( short orig = Short.MIN_VALUE; orig <= Short.MAX_VALUE; orig++ ) {
	    if( VERBOSE )  System.err.printf("Testng %d\n", orig);
	    out.writeShort(orig);
	    short res = in.readShort();
	    if( res != orig ) {
		System.err.printf("Error in short: wrote %d [0x%H] , read %d [0x%H]\n", orig, orig, res, res);
	    }
	    rewindIfNeed();
	}	
    }
    
    private void testUTF(DataInput in, DataOutput out) throws IOException {
	System.err.printf("Testing UTF\n");
	String[] reference = new String[] { "First", "Second", "Третья", "Четвертая", "С переводом \r\n каретки" };
	for (int i = 0; i < reference.length; i++) {
	    if( VERBOSE )  System.err.printf("Testng %s\n", reference[i]);
	    out.writeUTF(reference[i]);
	    String res = in.readUTF();
	    if( ! reference[i].equals(res) ) {
		System.err.printf("Error in QWE: wrote \"%s\", read \"%s\"\n", reference[i], res);
	    }
	    rewindIfNeed();
	}	
    }
    
    private void testUnsignedByte(DataInput in, DataOutput out) throws IOException {
	System.err.printf("Testing unsigned bytes\n");
	for (int orig = 0; orig <= 0xFF; orig++) {
	    if( VERBOSE )  System.err.printf("Testng %d\n", orig);	    
	    out.write((byte) orig);	    
	    int res = in.readUnsignedByte();
	    if( orig != res ) {
		System.err.printf("Error in unsigned byte: wrote %d [0x%H] , read %d [0x%H]\n", orig, orig, res, res);
	    }
	    rewindIfNeed();
	}	    }
    
    private void testUnsignedShort(DataInput in, DataOutput out) throws IOException {
	System.err.printf("Testing unsigned shorts\n");
	for (int orig = 0; orig <= 0xFFFF; orig++) {
	    if( VERBOSE )  System.err.printf("Testng %d\n", orig);	    
	    out.writeShort((short) orig);	    
	    int res = in.readUnsignedShort();
	    if( orig != res ) {
		System.err.printf("Error in unsigned short: wrote %d [0x%H] , read %d [0x%H]\n", orig, orig, res, res);
	    }
	    rewindIfNeed();
	}	
    }
    
    private void testSkip(DataInput in, DataOutput out) throws IOException {
	System.err.printf("Testing skipping bytes\n");
	out.writeInt(1);
	out.writeInt(2);
	String reference = "qwe";
	out.writeUTF(reference);
	in.skipBytes(8); // skip 2 integers
	String res = in.readUTF();
	if( ! reference.equals(res) ) {
	    System.err.printf("Error in skipping bytes: wrote \"%s\", read \"%s\"\n");
	}
    }

}
