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


package org.netbeans.identity.samples;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.Set;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

/**
 *
 * 
 */
public class LogHandler implements SOAPHandler<SOAPMessageContext> {
    
    PrintStream out;
    
    public boolean handleMessage(SOAPMessageContext smc) {
        Boolean outboundProperty = (Boolean)
            smc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
        if (!outboundProperty.booleanValue()) {
            String fileName = System.getProperty("user.home") +
                "/AccessManager/request";
            SOAPMessage message = smc.getMessage();
            try {
                File logFile = new File(fileName);
                if (logFile != null && logFile.exists()) {
                    out = new PrintStream(new FileOutputStream(logFile), false);
                } else {
                    out = System.out;
                }
                out.println("HandleMessage for request called:");
                message.writeTo(out);
                out.close();
            } catch (SOAPException ex) {
                ex.printStackTrace();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return true;
    }
    
    public Set<QName> getHeaders() {
        Set<QName> qnames = new HashSet();
        qnames.add(new QName(
            "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-01.xsd",
            "Security", "wsse"));
        return (qnames);
    }
    
    public boolean handleFault(SOAPMessageContext mc) {
        return true;
    }
    
    public void close(MessageContext context) {
    }
    
}
