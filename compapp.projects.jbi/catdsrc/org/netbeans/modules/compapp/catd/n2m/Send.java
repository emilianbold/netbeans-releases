/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.compapp.catd.n2m;

import java.io.StringReader;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.soap.SOAPConnection;
import javax.xml.transform.stream.StreamSource;
import org.netbeans.modules.compapp.catd.util.Util;

/**
 *
 * @author Bing Lu
 */
public class Send implements Runnable {
    private String mName;
        String mExpectedHttpWarning;
        String mDestination;
        SOAPConnection mConnection;
        Input mInput;
        int mBatches;

        public Send(String destination, String httpWarning, SOAPConnection connection, Input input, String batches) throws Exception {
            mDestination = destination;
            mExpectedHttpWarning = httpWarning;
            mConnection = connection;
            mInput = input;
            mBatches = Integer.parseInt(batches);
        }

        public void run() {
            String action = mInput.getAction();
            for (int i = 0; i < mBatches; i++) {
                try {
                    String data = mInput.nextData();
//                    System.out.println("data: " + data);
                    SOAPMessage message = MessageFactory.newInstance().createMessage();
                    message.getMimeHeaders().addHeader("soapaction", action);
                    SOAPPart soapPart = message.getSOAPPart();
                    soapPart.setContent(new StreamSource(new StringReader(data)));
                    message.saveChanges();
                    Util.sendMessage(mInput.getName(), false, mConnection, mDestination, message, null, mExpectedHttpWarning, null);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
