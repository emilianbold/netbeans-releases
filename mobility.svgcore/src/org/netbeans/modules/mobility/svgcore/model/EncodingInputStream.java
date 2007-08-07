/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt. 
  * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 */

package org.netbeans.modules.mobility.svgcore.model;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.editor.BaseDocument;

/**
 *
 * @author Pavel Benes
 */
public class EncodingInputStream extends InputStream {
    private final CharBuffer     m_chars;
    private       CharsetEncoder m_encoder = null;
    private final ByteBuffer     m_buffer;

    //TODO Revisit - is there a better way ?
    private static final Map ENCODING_MAPPING;
    
    static {
        ENCODING_MAPPING = new HashMap();
        ENCODING_MAPPING.put( "unicodelittleunmarked", "UTF-16"); //NOI18N
        ENCODING_MAPPING.put( "unicodebigunmarked", "UTF-16"); //NOI18N
        ENCODING_MAPPING.put( "unicode", "UTF-16"); //NOI18N
        ENCODING_MAPPING.put( "utf8", "UTF-8"); //NOI18N
        ENCODING_MAPPING.put( "unicodebig", "UTF-16BE"); //NOI18N
        ENCODING_MAPPING.put( "unicodelittle", "UTF-16LE"); //NOI18N
    }
    
    public EncodingInputStream(StringBuilder sb, String encoding) {
        this( CharBuffer.wrap(sb), encoding);
    }

    public EncodingInputStream(BaseDocument doc, String encoding) {
        this( CharBuffer.wrap( (CharSequence) doc.getProperty(CharSequence.class)), encoding);
    }
    
    public EncodingInputStream(CharBuffer chars, String encoding) {
        m_chars = chars;
        
        Charset charset;
        
        if (encoding == null) {
            charset = Charset.defaultCharset();
        } else {
            if (!Charset.isSupported(encoding)) {
                encoding = (String) ENCODING_MAPPING.get(encoding.toLowerCase());                
            } 
            charset = Charset.forName(encoding);
        }
        
        m_encoder =  charset.newEncoder();            
        m_buffer  = ByteBuffer.allocate(16384);
        m_buffer.limit(0);
    }
    
    public int read() throws IOException {
        if ( m_buffer.remaining() <= 0) {
            if (m_chars.remaining() > 0) {
                m_buffer.clear();
                CoderResult result = m_encoder.encode(m_chars, m_buffer, true);
                if (result.isError()) {
                    throw new IOException( result.toString());
                } else {
                    m_buffer.flip();
                }                    
            } 
            if ( m_buffer.remaining() <= 0) {
                return -1;
            }

        }
        int b = m_buffer.get();
        return b >= 0 ? b : (b + 0x100);
    }        
}
