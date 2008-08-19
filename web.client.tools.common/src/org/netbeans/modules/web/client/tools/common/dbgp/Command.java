/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
package org.netbeans.modules.web.client.tools.common.dbgp;

import java.io.IOException;
import java.io.OutputStream;

import sun.misc.BASE64Encoder;


/**
 * @author ads, jdeva
 *
 */
public class Command {
    private static final String DATA_SEPARATOR = " -- ";            // NOI18N
    private static final String TRANSACTION_OPT = " -i ";           // NOI18N
    public static final String SPACE = " ";                         // NOI18N
    public static final String FILE_ARG = "-f ";                    // NOI18N
    public static final String DEPTH_ARG = "-d ";                // NOI18N
    
    Command( String command, int transactionId  ){
        this.command = command;
        this.transactionId = transactionId;
    }
    
    void send(OutputStream out) throws IOException {
        String encodedData = null; 
        if ( getData() != null ){
            BASE64Encoder encoder = new BASE64Encoder();
            encodedData = encoder.encode( getData().getBytes( 
                    Message.ISO_CHARSET) );
        }
        StringBuilder dataToSend = new StringBuilder(command);
        dataToSend.append( getArgumentString() );
        if ( encodedData != null ){
            dataToSend.append( DATA_SEPARATOR );
            dataToSend.append( encodedData );
        }
        byte[] bytes = dataToSend.toString().getBytes(Message.UTF_8);
        byte[] sendBytes = new byte[ bytes.length + 1 ];
        System.arraycopy(bytes , 0, sendBytes, 0, bytes.length );
        sendBytes[ bytes.length ] = 0;
        Log.getLogger().fine("\nCommand: " + new String(sendBytes));             // NOI18N
        out.write( sendBytes );
        out.flush();
    }
    
    int getTransactionId() {
        return transactionId;
    }
    
    boolean wantAcknowledgment() {
        return true;
    }
    
    protected String getData(){
        return null;
    }
    
    
    protected String getArguments() {
        return "";
    }
    
    private String getArgumentString(){
        if ( getArguments() != null && getArguments().length() > 0 ) {
            return TRANSACTION_OPT + transactionId + " " + getArguments();
        }
        else {
            return TRANSACTION_OPT + transactionId;
        }
    }

    @Override
    public String toString() {
        return getArgumentString();
    }
    
    public String getCommandName() {
        return command;
    }
    
    private String command;
    private int transactionId;
}
