/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2007, 2016 Oracle and/or its affiliates. All rights reserved.
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
