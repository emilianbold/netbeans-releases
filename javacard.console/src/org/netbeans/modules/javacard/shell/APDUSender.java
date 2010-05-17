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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.net.Socket;
import java.util.Collections;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.WeakHashMap;
import java.util.regex.Pattern;
import org.netbeans.modules.javacard.spi.capabilities.UrlCapability;
import org.netbeans.modules.javacard.spi.Card;
import org.netbeans.modules.javacard.spi.capabilities.PortKind;
import org.netbeans.modules.javacard.spi.capabilities.PortProvider;
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

    private Card card;
    
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
        card = shellPanel.getCard();
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
        PortProvider prov = card.getCapability(PortProvider.class);
        UrlCapability apdu = card.getCapability(UrlCapability.class);
        int contactedPort = prov.getPort(PortKind.CONTACTED);
        contactedSocket = new Socket(prov.getHost(), contactedPort);
        contactedSocket.setTcpNoDelay(true);
        switch (apdu.getContactedProtocol()) {
            //XXX Are these streams ever closed?  This is probably a leak
            case T0 :
                contactedInterface = new CadT0Client(new BufferedInputStream(
                        contactedSocket.getInputStream()), new
                        BufferedOutputStream(contactedSocket.getOutputStream()));
                break;
            case T1 :
                contactedInterface = new CadT1Client(
                        new BufferedInputStream(contactedSocket.getInputStream()),
                        new BufferedOutputStream(contactedSocket.getOutputStream()));
                break;
            default :
                throw new IOException ("No contacted protocol specified"); //NOI18N
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
        PortProvider prov = card.getLookup().lookup(PortProvider.class);
        int contactlessport = prov.getPort(PortKind.CONTACTLESS);
        contactlessSocket = new Socket(prov.getHost(), contactlessport); //NOI18N
        contactlessSocket.setTcpNoDelay(true);
        contactlessInterface = new CadT1Client(new BufferedInputStream(
                contactlessSocket.getInputStream()), new BufferedOutputStream(
                contactlessSocket.getOutputStream()));
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
        try {
            Apdu apdu = new Apdu(toBytes(apduString));
            apdu.isExtended = extended;
            contactedInterface.exchangeApdu(apdu);
            String response = apdu.toString();
            return response;
        } catch (ArrayIndexOutOfBoundsException aioobe) {
            ShellException e = new ShellException (NbBundle.getMessage(APDUSender.class,
                    "MSG_INSUFFICIENT_BYTES", aioobe.getMessage())); //NOI18N
            e.initCause(aioobe);
            throw e;
        }
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

    static byte[] toBytes (String str) throws ShellException {
        return new APDUParser(str).bytes();
    }
}
