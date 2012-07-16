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
package org.netbeans.modules.netserver.websocket;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;


/**
 * @author ads
 *
 */
class WebSocketHandlerClient76 extends WebSocketHandlerClient75 {

    WebSocketHandlerClient76( WebSocketClient webSocketClient ) {
        super(webSocketClient);
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.netserver.websocket.WebSocketHandlerClient75#sendHandshake()
     */
    @Override
    public void sendHandshake() {
        StringBuilder builder = new StringBuilder(Utils.GET);
        builder.append(' ');
        builder.append(getClient().getUri().getPath());
        builder.append(' ');
        builder.append( Utils.HTTP_11);
        builder.append(Utils.CRLF);
        
        builder.append(Utils.WS_UPGRADE);
        builder.append(Utils.CRLF);
        
        builder.append(Utils.HOST);
        builder.append(": ");                               // NOI18N
        builder.append(getClient().getUri().getHost());
        builder.append(Utils.CRLF);
        
        builder.append("Origin: ");
        builder.append( Utils.getOrigin(getClient().getUri()));
        builder.append(Utils.CRLF);
        
        builder.append(Utils.KEY1);
        builder.append(": ");                               // NOI18N
        builder.append( generateKey());
        builder.append(Utils.CRLF);
        
        builder.append(Utils.KEY2);
        builder.append(": ");                               // NOI18N
        builder.append( generateKey());
        builder.append(Utils.CRLF);
        
        builder.append(Utils.WS_PROTOCOL);
        builder.append(": chat");                             // NOI18N
        
        builder.append( Utils.CRLF );
        builder.append( Utils.CRLF );
        
        byte[] bytes = builder.toString().getBytes( 
                Charset.forName(Utils.UTF_8));
        byte[] generated = generateContent();
        byte[] toSend = new byte[ bytes.length +generated.length];
        System.arraycopy(bytes, 0, toSend, 0, bytes.length);
        System.arraycopy(generated, 0, toSend, bytes.length, generated.length);
        getClient().send( toSend, getKey() );
    }
    
    private String generateKey() {
        // TODO : random challenge code generation should be used
        if ( tempKey ){
            return "4 @1  46546xW%0l 1 5";
        }
        tempKey = true;
        return "12998 5 Y3 1  .P00";
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.netserver.websocket.WebSocketHandlerClient75#readHandshakeResponse(java.nio.ByteBuffer)
     */
    @Override
    protected void readHandshakeResponse( ByteBuffer buffer )
            throws IOException
    {
        Utils.readHttpRequest(getClient().getChannel(), buffer);
        byte[] md5Challenge = readRequestContent(16);
        /*
         *  TODO : check md5Challenge against initial data in the handshake
         *  
         *  if ( md5Challenge is no correct ) {
         *      throws IOException("wrong handshake data");
         *  }
         */
    }
    
    private byte[] generateContent(){
        byte[] bytes = new byte[8];
        // TODO : random bytes challenge code generation should be used
        bytes[0]=0x47;
        bytes[1]=0x30;
        bytes[2]=0x22;
        bytes[3]=0x2D;
        bytes[4]=0x5A;
        bytes[5]=0x3F;
        bytes[6]=0x47;
        bytes[7]=0x58;
        return bytes;
    }
    
    // TODO: delete
    private boolean tempKey;

}
