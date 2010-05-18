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
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Arrays;

/**
 * Send UDP packets containing a randomly-generated string
 * to a given address:port, and wait for them to return. This
 * is a basic connectivity check to ensure that media can flow.
 * @author jerry
 */
public class EchoTester  extends Thread{

    JingleAudioSession _session;
    //int _source_port;
    String _target_ip;
    int _target_port;
    /** Creates a new instance of EchoTester */
    public EchoTester (JingleAudioSession session,/* int source_port,*/ String target_ip, int target_port) {
        _session = session;
        _target_ip = target_ip;
        _target_port = target_port;
      //  _source_port = source_port;
        
    }
    
    public void run(){
        try{
            DatagramSocket sock;
            DatagramPacket sendpack;
            sock = new DatagramSocket();
            byte []buf = new byte[256];
            for(int i=0; i<256; i++){
                buf[i] = (byte)(Math.random() * 256);
            }
            sock.setSoTimeout(1000);
            int cnt = 0;
            DatagramPacket recvpack = new DatagramPacket(new byte[256], 256);
            for(int i=0; i<10; i++){
                sendpack = new DatagramPacket(buf, 0, 256, InetAddress.getByName(_target_ip), _target_port);
                sock.send(sendpack);
                try{
                    sock.receive(recvpack);
                }
                catch(SocketTimeoutException ex){
                    // Did not get response, try again
                    continue;
                }
                if(Arrays.equals(buf, recvpack.getData())){
                    cnt++;
                }
            }
            if(cnt > 0){
                _session.onConnectionTest(_session.CONNECTION_TEST_SUCCESS);
              
            }
            else{
                _session.onConnectionTest(_session.CONNECTION_TEST_FAIL);
            }
            sock.close();
        }
        catch(Exception e){
            
        }
    }
    
}
