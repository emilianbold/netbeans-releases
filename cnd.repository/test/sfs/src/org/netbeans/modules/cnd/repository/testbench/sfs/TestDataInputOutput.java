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

package org.netbeans.modules.cnd.repository.testbench.sfs;

import java.io.*;
import java.nio.*;
import java.util.List;
import org.netbeans.modules.cnd.repository.sfs.BufferDataInput;

/**
 * Testing BufferDataInput and BufferDataOutput.
 * @author Vladimir Kvashin
 */
public class TestDataInputOutput extends BaseTest {
    	    
    private static final boolean VERBOSE = false;
	    
    private ByteBuffer readBuffer;
    private ByteBuffer writeBuffer;
    private DataOutput out;
    private DataInput in;
	
    public boolean test(List<String> params ) throws IOException {
	ByteBuffer buffer = ByteBuffer.allocate(1024*1024);
	readBuffer = buffer.slice();
	writeBuffer = buffer.slice();
	out = new DataOutputStream(new ByteArrayOutputStream());
	in = new BufferDataInput(readBuffer);
	return test(in, out);
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

    private boolean test(DataInput in, DataOutput out) throws IOException {
        boolean passed = true;
	passed &= testByte(in, out);		rewind();
	passed &= testBoolean(in, out);         rewind();
	passed &= testChar(in, out);		rewind();
	passed &= testLine(in, out);		rewind();
	passed &= testUTF(in, out);		rewind();
	passed &= testUnsignedByte(in, out);	rewind();
	passed &= testUnsignedShort(in, out);	rewind();
	passed &= testSkip(in, out);		rewind();
	passed &= testShort(in, out);		rewind();
	passed &= testInt(in, out);		rewind();
	passed &= testLong(in, out);		rewind();
	passed &= testFloat(in, out);		rewind();
	passed &= testDouble(in, out);		rewind();
        return passed;
    }

    private boolean testByte(DataInput in, DataOutput out) throws IOException {
        boolean passed = true;
	System.err.printf("Testing bytes (one by one)\n");
	for( int i = Byte.MIN_VALUE; i <= Byte.MAX_VALUE; i++ ) {
	    byte orig = (byte) i;
	    byte res;
	    if( VERBOSE )  System.err.printf("Testng %d\n", orig);
	    out.write(orig);
	    out.writeByte(orig);
	    res = in.readByte();
	    if( res != orig ) {
                passed = false;
		System.err.printf("Error in byte(1): wrote %d , read %d\n", orig, res);
	    }
	    res = in.readByte();
	    if( res != orig ) {
                passed = false;
		System.err.printf("Error in byte(2): wrote %d , read %d\n", orig, res);
	    }
	}
        return passed;
    }
    
    private boolean testBoolean(DataInput in, DataOutput out) throws IOException {
        boolean passed = true;
	System.err.printf("Testing booleans\n");
	boolean[] orig = new boolean[] { true, false };
	for (int i = 0; i < orig.length; i++) {
	    out.writeBoolean(orig[i]);
	    boolean res = in.readBoolean();
	    if( orig[i] != res ) {
                passed = false;
		System.err.printf("Error in boolean: wrote %b , read %b\n", orig[i], res);
	    }
	}
        return passed;
    }
    
    private boolean testChar(DataInput in, DataOutput out) throws IOException {
        boolean passed = true;
	System.err.printf("Testing chars\n");
	for( int i = Character.MIN_VALUE; i <= Character.MAX_VALUE; i++ ) {
	    char orig = (char) i;
	    char res;
	    if( VERBOSE )  System.err.printf("Testng %c\n", orig);
	    out.writeChar(orig);
	    res = in.readChar();
	    if( res != orig ) {
                passed = false;
		System.err.printf("Error in char: wrote %c [0x%H] , read %c [0x%H]\n", orig, orig, res, res);
	    }
	    rewindIfNeed();
	}
        return passed;
    }    

    private boolean testDouble(DataInput in, DataOutput out) throws IOException {
        boolean passed = true;
	System.err.printf("Testing doubles\n");
	for( double i = Double.MIN_VALUE; i <= Double.MAX_VALUE; i++ ) {
	    double orig = i;
	    out.writeDouble(orig);
	    double res = in.readDouble();
	    if( res != orig ) {
                passed = false;
		System.err.printf("Error in double: wrote %e [0x%H] , read %e [0x%H]\n", orig, orig, res, res);
	    }
	    rewindIfNeed();
	}
        return passed;
    }    
    
    private boolean testFloat(DataInput in, DataOutput out) throws IOException {
        boolean passed = true;
	System.err.printf("Testing floats\n");
	for( float i = Float.MIN_VALUE; i <= Float.MAX_VALUE; i++ ) {
	    float orig = i;
	    out.writeFloat(orig);
	    float res = in.readFloat();
	    if( res != orig ) {
                passed = false;
		System.err.printf("Error in float: wrote %e [0x%H] , read %e [0x%H]\n", orig, orig, res, res);
	    }
	    rewindIfNeed();
	}
        return passed;
    }        
    
    private boolean testLine(DataInput in, DataOutput out) throws IOException {
        boolean passed = true;
	System.err.printf("Testing readLine\n");
	String toWrite = "1\n22\r333\r\n4444\n\naaa\r\r"; // NOI18N
	String[] reference = new String[] { "1", "22", "333", "4444", "", "aaa", "" }; // NOI18N
	for (int i = 0; i < toWrite.length(); i++) {
	    char c = toWrite.charAt(i);
	    byte b = (byte) (c & 0x00FF);
	    out.write(b);
	}
	
	for (int i = 0; i < reference.length; i++) {
	    String res = in.readLine();
	    if( ! reference[i].equals(res) ) {
                passed = false;
		System.err.printf("Error in readLine: wrote \"%s\", read \"%s\"\n", reference[i], res);
	    }
	}
        return passed;
    }        
    
    private boolean testInt(DataInput in, DataOutput out) throws IOException {
        boolean passed = true;
	System.err.printf("Testing ints\n");
	for( int i = Integer.MIN_VALUE; i <= Integer.MAX_VALUE; i++ ){
	    int orig = (int) i;
	    int res;
	    if( VERBOSE )  System.err.printf("Testng %d\n", orig);
	    out.writeInt(orig);
	    res = in.readInt();
	    if( res != orig ) {
                passed = false;
		System.err.printf("Error in int: wrote %d [0x%H] , read %d [0x%H]\n", orig, orig, res, res);
	    }
	    rewindIfNeed();
	}
        return passed;
    }    
    
    private boolean testLong(DataInput in, DataOutput out) throws IOException {
        boolean passed = true;
	System.err.printf("Testing longs\n");
	for( long orig = Long.MIN_VALUE; orig <= Long.MAX_VALUE; orig++ ) {
	    if( VERBOSE )  System.err.printf("Testng %d\n", orig);
	    out.writeLong(orig);
	    long res = in.readLong();
	    if( res != orig ) {
                passed = false;
		System.err.printf("Error in long: wrote %d [0x%H] , read %d [0x%H]\n", orig, orig, res, res);
	    }
	    rewindIfNeed();
	}
        return passed;
    }
    
    private boolean testShort(DataInput in, DataOutput out) throws IOException {
        boolean passed = true;
	System.err.printf("Testing shorts\n");
	for( short orig = Short.MIN_VALUE; orig <= Short.MAX_VALUE; orig++ ) {
	    if( VERBOSE )  System.err.printf("Testng %d\n", orig);
	    out.writeShort(orig);
	    short res = in.readShort();
	    if( res != orig ) {
                passed = false;
		System.err.printf("Error in short: wrote %d [0x%H] , read %d [0x%H]\n", orig, orig, res, res);
	    }
	    rewindIfNeed();
	}	
        return passed;
    }
    
    private boolean testUTF(DataInput in, DataOutput out) throws IOException {
        boolean passed = true;
	System.err.printf("Testing UTF\n");
	String[] reference = new String[] { "First", "Second", "????????????", "??????????????????", "?? ?????????????????? \r\n ??????????????" }; // NOI18N
	for (int i = 0; i < reference.length; i++) {
	    if( VERBOSE )  System.err.printf("Testng %s\n", reference[i]);
	    out.writeUTF(reference[i]);
	    String res = in.readUTF();
	    if( ! reference[i].equals(res) ) {
                passed = false;
		System.err.printf("Error in QWE: wrote \"%s\", read \"%s\"\n", reference[i], res);
	    }
	    rewindIfNeed();
	}	
        return passed;
    }
    
    private boolean testUnsignedByte(DataInput in, DataOutput out) throws IOException {
        boolean passed = true;
	System.err.printf("Testing unsigned bytes\n");
	for (int orig = 0; orig <= 0xFF; orig++) {
	    if( VERBOSE )  System.err.printf("Testng %d\n", orig);	    
	    out.write((byte) orig);	    
	    int res = in.readUnsignedByte();
	    if( orig != res ) {
                passed = false;
		System.err.printf("Error in unsigned byte: wrote %d [0x%H] , read %d [0x%H]\n", orig, orig, res, res);
	    }
	    rewindIfNeed();
	}	    
        return passed;
    }
    
    private boolean testUnsignedShort(DataInput in, DataOutput out) throws IOException {
        boolean passed = true;
	System.err.printf("Testing unsigned shorts\n");
	for (int orig = 0; orig <= 0xFFFF; orig++) {
	    if( VERBOSE )  System.err.printf("Testng %d\n", orig);	    
	    out.writeShort((short) orig);	    
	    int res = in.readUnsignedShort();
	    if( orig != res ) {
                passed = false;
		System.err.printf("Error in unsigned short: wrote %d [0x%H] , read %d [0x%H]\n", orig, orig, res, res);
	    }
	    rewindIfNeed();
	}	
        return passed;
    }
    
    private boolean testSkip(DataInput in, DataOutput out) throws IOException {
        boolean passed = true;
	System.err.printf("Testing skipping bytes\n");
	out.writeInt(1);
	out.writeInt(2);
	String reference = "qwe"; // NOI18N
	out.writeUTF(reference);
	in.skipBytes(8); // skip 2 integers
	String res = in.readUTF();
	if( ! reference.equals(res) ) {
            passed = false;
	    System.err.printf("Error in skipping bytes: wrote \"%s\", read \"%s\"\n");
	}
        return passed;
    }

}
