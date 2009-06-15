/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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
package org.netbeans.modules.javacard.shell;

import com.sun.javacard.apduio.Apdu;
import com.sun.javacard.apduio.CadClientInterface;
import com.sun.javacard.apduio.CadT0Client;
import com.sun.javacard.apduio.CadT1Client;
import com.sun.javacard.apduio.CadTransportException;
import java.io.IOException;
import org.netbeans.modules.javacard.card.ReferenceImplementation;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.net.Socket;
import java.util.Collections;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.WeakHashMap;
import org.openide.util.NbBundle;

/**
 *
 * @author Anki R. Nelaturu
 */
public class APDUSender {

    static Map<ShellPanel, APDUSender> senders = Collections.synchronizedMap(new WeakHashMap<ShellPanel, APDUSender>());

    private CadClientInterface contactedInterface;
    private Socket contactedSocket;

    private CadClientInterface contactlessInterface;
    private Socket contactlessSocket;

    private boolean extended; // off by default
    private boolean contactless; // contacted by default

    private ReferenceImplementation server;
    
    static APDUSender getSender(ShellPanel shellPanel) {
        APDUSender sender = senders.get(shellPanel);
        if (sender == null) {
            sender = new APDUSender(shellPanel);
            senders.put(shellPanel, sender);
        }
        return sender;
    }

    public static String getString(String key) {
        return NbBundle.getMessage(APDUSender.class, key);
    }

    private APDUSender(ShellPanel shellPanel) {
        server = (ReferenceImplementation) shellPanel.getServer();
    }

    public String setExtended(boolean yesNo) {
        extended = yesNo;
        return yesNo ? getString("EXTENDED_MODE_ON") //NOI18N
                :getString("EXTENDED_MODE_OFF"); //NOI18N
    }

    public String setContactless(boolean yesNo) {
        contactless = yesNo;
        return yesNo ? getString("CONTACTLESS_ON") //NOI18N
                :getString("CONTACTLESS_OFF"); //NOI18N
    }
    
    public String powerup() throws IOException, CadTransportException {
        if(contactless) {
            return powerupContactless();
        }
        
        return powerupContacted();
    }

    public String powerdown() throws IOException, CadTransportException {
        if(contactless) {
            return powerdownContactless();
        }

        return powerdownContacted();
    }

    public String powerupContacted() throws IOException, CadTransportException {
        contactedSocket = new Socket("localhost", Integer.parseInt(server.getContactedPort()));
        contactedSocket.setTcpNoDelay(true);
        if("T=0".equals(server.getContactedProtocol())) {
            contactedInterface = new CadT0Client(new BufferedInputStream(contactedSocket.getInputStream()), new BufferedOutputStream(contactedSocket.getOutputStream()));
        } else {
            contactedInterface = new CadT1Client(new BufferedInputStream(contactedSocket.getInputStream()), new BufferedOutputStream(contactedSocket.getOutputStream()));
        }
        contactedInterface.powerUp();
        return "";
    }

    public String powerdownContacted() throws IOException, CadTransportException {
        try {
            contactedInterface.powerDown();
            contactedSocket.close();
            contactedInterface = null;
            contactedSocket = null;
        } catch(Exception ex) {
            //
        }
        return "";
    }

    public String powerupContactless() throws IOException, CadTransportException {
        contactlessSocket = new Socket("localhost", Integer.parseInt(server.getContactlessPort()));
        contactlessSocket.setTcpNoDelay(true);
        contactlessInterface = new CadT1Client(new BufferedInputStream(contactlessSocket.getInputStream()), new BufferedOutputStream(contactlessSocket.getOutputStream()));
        contactlessInterface.powerUp();
        return "";
    }

    public String powerdownContactless() throws IOException, CadTransportException {
        try {
            contactlessInterface.powerDown();
            contactlessSocket.close();
            contactlessInterface = null;
            contactlessSocket = null;
        } catch(Exception ex) {
            //
        }
        return "";
    }

    public String send(String apduString) throws IOException, ShellException, CadTransportException {
        if(contactless) {
            return sendContactless(apduString);
        }
        return sendContacted(apduString);
    }

    public String sendContacted(String apduString) throws IOException, ShellException, CadTransportException {
        if(contactedInterface == null) {
            powerup();
        }
        Apdu apdu = new Apdu(toBytes(apduString));
        apdu.isExtended = extended;
        contactedInterface.exchangeApdu(apdu);
        String response = apdu.toString();
        return response;
    }

    public String sendContactless(String apduString) throws IOException, ShellException, CadTransportException {
        if(contactlessInterface == null) {
            powerup();
        }
        Apdu apdu = new Apdu(toBytes(apduString));
        apdu.isExtended = extended;
        contactlessInterface.exchangeApdu(apdu);
        String response = apdu.toString();
        return response;
    }

    private static byte[] toBytes(String str) throws ShellException {
        StringTokenizer st = new StringTokenizer(str, ", ");
        byte[] bytes = new byte[st.countTokens()];
        int i = 0;
        String token = null;
            while (st.hasMoreTokens()) {
                token = st.nextToken();
                switch (token.charAt(0)) {
                    case '\'':
                        bytes[i] = (byte) token.charAt(1);
                        break;
                    case '0':
                        if (token.charAt(1) == 'x' || token.charAt(1) == 'X') {
                            bytes[i] = (byte) Integer.parseInt(token.substring(2), 16);
                        } else {
                            bytes[i] = (byte) Integer.parseInt(token.substring(1), 8);
                        }
                        break;
                    case '1': //NOI18N
                    case '2': //NOI18N
                    case '3': //NOI18N
                    case '4': //NOI18N
                    case '5': //NOI18N
                    case '6': //NOI18N
                    case '7': //NOI18N
                    case '8': //NOI18N
                    case '9': //NOI18N
                        bytes[i] = (byte) Integer.parseInt(token, 10);
                        break;
                    default:
                        String msg = NbBundle.getMessage (APDUSender.class,
                                "ERR_INVALID_TOKEN", token);
                        throw new ShellException(msg);

                }
                i++;
            }
        return bytes;
    }
}
