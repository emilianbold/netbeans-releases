/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.

Oracle and Java are registered trademarks of Oracle and/or its affiliates.
Other names may be trademarks of their respective owners.
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 2007 Sun
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
package org.netbeans.lib.collab.xmpp.jingle;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;

/**
 * Listens on given port for UDP packets. Sends any packet data back to
 * its source. Used for testing media connectivity
 * @author jerry
 */
public class EchoListener extends Thread {

    int _port;
    boolean _stop = false;

    /** Creates a new instance of EchoListener */
    public EchoListener(int port) {
        _port = port;
    }

    public void run() {
        
        DatagramSocket sock;
        DatagramPacket recvpack;
        try{
            sock = new DatagramSocket(_port);
            recvpack = new DatagramPacket(new byte[256], 256);
            sock.setSoTimeout(25);
            while(!_stop){
                try{
                    sock.receive(recvpack);
                } catch(SocketTimeoutException ex){
                    // Nothing came in, try again
                    continue;
                }
                //System.out.println("Got a packet, sending it back..");
                DatagramPacket sendpkt = new DatagramPacket(recvpack.getData(), recvpack.getData().length);
                sendpkt.setAddress(recvpack.getAddress());
                sendpkt.setPort(recvpack.getPort());
                try{
                    sock.send(sendpkt);
                }
                catch(Exception ex){
                    //TODO: log an error
                }
            }
            sock.close();
        } catch(Exception e){
            
        }
        
    }
    
    public void stopListener(){
        _stop = true;
    }
    
}
