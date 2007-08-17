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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.xml.core.lib;

import java.io.*;
import javax.swing.text.*;

/**
 * XML uses inband encoding detection - this class obtains it.
 *
 * @author  Petr Kuzel
 * @version 1.0
 */
public class EncodingHelper extends Object {

    // heuristic constant guessing max prolog length
    public static final int EXPECTED_PROLOG_LENGTH = 1000;

    /** Detect input stream encoding.
    * The stream stays intact.
    * @return java encoding names ("UTF8", "ASCII", etc.) or null
    * if the stream is not markable or enoding cannot be detected.
    */
    public static String detectEncoding(InputStream in) throws IOException {

        if (! in.markSupported()) {
            if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug("EncodingHelper got unmarkable stream: " + in.getClass()); // NOI18N
            return null;
        }

        try {
            in.mark(EXPECTED_PROLOG_LENGTH);

            byte[] bytes = new byte[EXPECTED_PROLOG_LENGTH];
            for (int i = 0; i<bytes.length; i++) {
                try {
                    int datum = in.read();
                    if (datum == -1) break;
                    bytes[i] = (byte) datum;
                } catch (EOFException ex) {
                }
            }

            String enc = autoDetectEncoding(bytes);
            if (enc == null) return null;
            
            enc = detectDeclaredEncoding(bytes, enc);
            if (enc == null) return null;
            
            return Convertors.iana2java (enc);
        } finally {
            in.reset();
        }
    }

        
    /**
     * @return Java encoding family identifier or <tt>null</tt> for unrecognized
     */
    static String autoDetectEncoding(byte[] buf) throws IOException {
        

        if (buf.length >= 4) {
            switch (buf[0]) {
                case 0:  
                    // byte order mark of (1234-big endian) or (2143) USC-4
                    // or '<' encoded as UCS-4 (1234, 2143, 3412) or UTF-16BE 
                    if (buf[1] == (byte)0x3c && buf[2] == (byte)0x00 && buf[3] == (byte)0x3f) {
                        return "UnicodeBigUnmarked";
                    }
                    // else it's probably UCS-4
                    break;

                case 0x3c:
                    switch (buf[1]) {
                        // First character is '<'; could be XML without
                        // an XML directive such as "<hello>", "<!-- ...", // NOI18N
                        // and so on.
                        
                        // 3c 00 3f 00 UTF-16 little endian
                        case 0x00:
                            if (buf [2] == (byte)0x3f && buf [3] == (byte)0x00) {
                                return  "UnicodeLittleUnmarked";
                            }                            
                            break;

                        // 3c 3f 78 6d == ASCII and supersets '<?xm'
                        case '?':
                            if (buf [2] == 'x' && buf [3] == 'm') {
                                return  "UTF8"; // NOI18N
                            }
                            break;
                    }
                    break;

                // 4c 6f a7 94 ... some EBCDIC code page
                case 0x4c:
                    if (buf[1] == (byte)0x6f && buf[2] == (byte)0xa7 && buf[3] == (byte)0x94) {
                        return "Cp037"; // NOI18N
                    }                     
                    break;

                // UTF-16 big-endian marked
                case (byte)0xfe:
                    if (buf[1] == (byte)0xff && (buf[2] != 0 || buf[3] != 0)) {
                        return  "UnicodeBig"; // NOI18N
                    }
                    break;

                // UTF-16 little-endian marked
                case (byte)0xff:
                    if (buf[1] == (byte)0xfe && (buf[2] != 0 || buf[3] != 0)) {                        
                        return "UnicodeLittle"; // NOI18N
                    }
                    break;
                    
                // UTF-8 byte order mark
                case (byte)0xef:
                    if (buf[1] == (byte)0xbb && buf[2] == (byte)0xbf) {
                        return "UTF8";  //NOI18N
                    }
                    break;
                    
            }
        }

        return null;
    }

    /**
     * Look for encoding='' anyway stop at <tt>?></tt>
     * @return found encoding or null if none declared
     */
    static String detectDeclaredEncoding(byte[] data, String baseEncoding) throws IOException {

        StringBuffer buf = new StringBuffer();
        Reader r;
        char delimiter = '"';

        r = new InputStreamReader(new ByteArrayInputStream(data), baseEncoding);
        try {
            for (int c = r.read(); c != -1; c = r.read()) {
                buf.append((char)c);
            }
        } catch (IOException ex) {
            // EOF of data out of boundary
            // dont care try to guess from given data
        }
        
        String s = buf.toString();
        
        int iend = s.indexOf("?>");
        iend = iend == -1 ? s.length() : iend;
        
        int iestart = s.indexOf("encoding");
        if (iestart == -1 || iestart > iend) return null;
        
        char[] chars = s.toCharArray();
        
        int i = iestart;
        
        for (; i<iend; i++) {
            if (chars[i] == '=') break;
        }
        
        for (; i<iend; i++) {
            if (chars[i] == '\'' || chars[i] == '"') {
                delimiter = chars[i];
                break;
            }
                
        }

        i++;
        
        int ivalstart = i;
        for (; i<iend; i++) {
            if (chars[i] == delimiter) {
                return new String(chars, ivalstart, i - ivalstart);
            }
        }
        
        return null;
    }
    
    /**
     * Parse MIME content type for attributes. 
     */
    static String parseMIMECharSet(String mime) {
        
        final String CHARSET = "charset";
        
        if (mime != null) {
            int i;

            mime = mime.toLowerCase ();
            i = mime.indexOf (';');
            if (i != -1) {
                String	attributes;

                attributes = mime.substring (i + 1);
                mime = mime.substring (0, i);

                // use "charset=..." if it's available // NOI18N
                i = attributes.indexOf (CHARSET); // NOI18N
                if (i != -1) {
                    attributes = attributes.substring (i + CHARSET.length());
                    // strip out subsequent attributes
                    if ((i = attributes.indexOf (';')) != -1)
                        attributes = attributes.substring (0, i);
                    // find start of value
                    if ((i = attributes.indexOf ('=')) != -1) {
                        attributes = attributes.substring (i + 1);
                        // strip out rfc822 comments
                        if ((i = attributes.indexOf ('(')) != -1)
                            attributes = attributes.substring (0, i);
                        // double quotes are optional
                        if ((i = attributes.indexOf ('"')) != -1) {
                            attributes = attributes.substring (i + 1);
                            attributes = attributes.substring (0,
                                                               attributes.indexOf ('"'));
                        }
                        return attributes.trim();
                        // XXX "\;", "\)" etc were mishandled above // NOI18N
                    }
                }
            }
        } 
        
        return null;        
    }

    
    
    /** Document itself is encoded as Unicode, but in
    * the document prolog is an encoding attribute.
    * @return java encoding names ("UTF8", "ASCII", etc.) or null if no guess
    */
    public static String detectEncoding(Document doc) throws IOException {

        if (doc == null) return null;

        try {

            String text = doc.getText(0,
                                      doc.getLength() > EXPECTED_PROLOG_LENGTH ?
                                      EXPECTED_PROLOG_LENGTH : doc.getLength()
                                     );
            InputStream in = new ByteArrayInputStream(text.getBytes());
            return detectEncoding(in);

        } catch (BadLocationException ex) {
            throw new RuntimeException(ex.toString());
        }

    }

}
