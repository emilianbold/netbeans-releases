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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
@SuppressWarnings("unchecked")
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
        this( (CharSequence) doc.getProperty(CharSequence.class), encoding);
    }

    public EncodingInputStream(CharSequence chars, String encoding) {
        this( CharBuffer.wrap( chars), encoding);
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
