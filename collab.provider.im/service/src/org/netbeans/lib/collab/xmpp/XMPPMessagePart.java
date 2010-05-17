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

package org.netbeans.lib.collab.xmpp;

import java.util.*;
import org.netbeans.lib.collab.*;
import org.jabberstudio.jso.util.ByteCodec;

/**
 *
 *
 */
public class XMPPMessagePart implements MessagePart {

    Hashtable _contents = new Hashtable();
    Hashtable _headers = new Hashtable();
    String _body = null;
    static class Base64Cod {
        ByteCodec.Base64Codec b64 = new ByteCodec.Base64Codec();
        public String encode(byte[] input, int off, int len) {
            if (off != 0 || input.length != len) {
                byte[] temp = new byte[len];
                System.arraycopy(input, off, temp, 0, len);
                input = temp;
            }
            return b64.encode(input);
        }
        
        public String encode(byte[] input) {
            return b64.encode(input);
        }
        
        public byte[] decode(String input) {
            return b64.decode(input);
        }
    };
     
    static Base64Cod base64 = new Base64Cod();
    
    /** Creates a new instance of XMPPMessagePart */
    public XMPPMessagePart() {
        
    }
    
    public void clearContent() throws CollaborationException {
        _body = null;
        _contents = new Hashtable();
    }
    
    public String getContent() throws org.netbeans.lib.collab.CollaborationException {
	return _body;
    }
    
    public String getContent(String contentType) throws org.netbeans.lib.collab.CollaborationException {
	return (String)_contents.get(contentType);
    }
    
    public String getContentEncoding() {
	return (String)_headers.get(XMPPMessage.CONTENT_ENCODING);
    }
    
    public String getContentID() {
	return null;
    }
    
    public String getContentName() {
	return (String)_headers.get(XMPPMessage.CONTENT_NAME);
    }
    
    public String getContentType() {
	return (String)_headers.get(XMPPMessage.CONTENT_TYPE);
    }
    
    public java.io.InputStream getInputStream() throws CollaborationException {
        if (_body != null) 
             return new java.io.ByteArrayInputStream(_body.getBytes());
        return null;
    }
    
    public byte[] getBytes(String encoding) throws CollaborationException {
        if (_body != null) {
	    return base64.decode(_body);
	}
        return null;
    }
    
    public int getSize() throws CollaborationException {
	return _body.length();
    }
    
    public void setContent(String str) throws CollaborationException {
        _body = str;
    }
    
    public void setContent(java.io.InputStream is, String enc) 
                throws CollaborationException 
    {
        try {
	    if (enc != null) _headers.put(XMPPMessage.CONTENT_ENCODING, enc);
	    StringBuffer buf = new StringBuffer();
	    // Why 1023?
	    // base64 encoding takes scoops of 3 bytes at a time so the 
	    // size of each chunk to encode needs to be a multiple of 3
	    // otherwise the chunk-by-chunk encoding does not work
	    byte[] b = new byte[1023];
	    while (is.available() > 0) {
		int len = is.read(b, 0, b.length);
		buf.append(base64.encode(b, 0, len));
	    }
	    setContent(buf.toString());
        } catch(Exception e) {
            throw new CollaborationException(e.toString());
        }
    }
    
    public void setContent(byte[] b, String enc)
	throws CollaborationException
    {
	setContent(base64.encode(b));
        if (enc != null) _headers.put(XMPPMessage.CONTENT_ENCODING, enc);
    }
    
    public void setContentName(String str) throws CollaborationException {
        _headers.put(XMPPMessage.CONTENT_NAME, str);
    }
    
    public void setContent(String content, String contentType) throws CollaborationException {
        _contents.put(contentType, content);
        setContentType(contentType);
    }
    
    public void setContentType(String str) throws CollaborationException {
        _headers.put(XMPPMessage.CONTENT_TYPE, str);
    }
    
    protected Hashtable getContents() {
        return _contents;
    }
    
    protected void setHeaders(Hashtable ht) {
        _headers = ht;
    }
    
    protected Hashtable getHeaders() {
        return _headers;
    }
}
