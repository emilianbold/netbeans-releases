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

package org.netbeans.modules.cnd.repository.sfs;

import java.io.*;

/**
 * Utility class for reading and writing UTF
 * @author Vladimir Kvashin
 */
public class UTF {
    
    /**
     * Copied from package-local static method DataInputStreaam.writeUTF
     *
     * Writes a string to the specified DataOutput using
     * <a href="DataInput.html#modified-utf-8">modified UTF-8</a>
     * encoding in a machine-independent manner. 
     * <p>
     * First, two bytes are written to out as if by the <code>writeShort</code>
     * method giving the number of bytes to follow. This value is the number of
     * bytes actually written out, not the length of the string. Following the
     * length, each character of the string is output, in sequence, using the
     * modified UTF-8 encoding for the character. If no exception is thrown, the
     * counter <code>written</code> is incremented by the total number of 
     * bytes written to the output stream. This will be at least two 
     * plus the length of <code>str</code>, and at most two plus 
     * thrice the length of <code>str</code>.
     *
     * @param      str   a string to be written.
     * @param      out   destination to write to
     * @return     The number of bytes written out.
     * @exception  IOException  if an I/O error occurs.
     */
    public static int writeUTF(String str, DataOutput out) throws IOException {
        int strlen = str.length();
	int utflen = 0;
	int c, count = 0;
 
        /* use charAt instead of copying String to char array */
	for (int i = 0; i < strlen; i++) {
            c = str.charAt(i);
	    if ((c >= 0x0001) && (c <= 0x007F)) {
		utflen++;
	    } else if (c > 0x07FF) {
		utflen += 3;
	    } else {
		utflen += 2;
	    }
	}

	if (utflen > 65535)
	    throw new UTFDataFormatException(
                "encoded string too long: " + utflen + " bytes");

        byte[] bytearr = null;
//        if (out instanceof DataOutputStream) {
//            DataOutputStream dos = (DataOutputStream)out;
//            if(dos.bytearr == null || (dos.bytearr.length < (utflen+2)))
//                dos.bytearr = new byte[(utflen*2) + 2];
//            bytearr = dos.bytearr;
//        } else {
            bytearr = new byte[utflen+2];
//        }
     
	bytearr[count++] = (byte) ((utflen >>> 8) & 0xFF);
	bytearr[count++] = (byte) ((utflen >>> 0) & 0xFF);  
        
        int i=0;
        for (i=0; i<strlen; i++) {
           c = str.charAt(i);
           if (!((c >= 0x0001) && (c <= 0x007F))) break;
           bytearr[count++] = (byte) c;
        }
	
	for (;i < strlen; i++){
            c = str.charAt(i);
	    if ((c >= 0x0001) && (c <= 0x007F)) {
		bytearr[count++] = (byte) c;
               
	    } else if (c > 0x07FF) {
		bytearr[count++] = (byte) (0xE0 | ((c >> 12) & 0x0F));
		bytearr[count++] = (byte) (0x80 | ((c >>  6) & 0x3F));
		bytearr[count++] = (byte) (0x80 | ((c >>  0) & 0x3F));
	    } else {
		bytearr[count++] = (byte) (0xC0 | ((c >>  6) & 0x1F));
		bytearr[count++] = (byte) (0x80 | ((c >>  0) & 0x3F));
	    }
	}
        out.write(bytearr, 0, utflen+2);
        return utflen + 2;
    }
    
    public final static String readUTF(DataInput in) throws IOException {
	return DataInputStream.readUTF(in);
    }
    
}
