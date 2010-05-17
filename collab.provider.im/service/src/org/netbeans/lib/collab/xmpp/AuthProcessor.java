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

import org.jabberstudio.jso.NSI;
import org.jabberstudio.jso.Packet;
import org.jabberstudio.jso.Stream;
import org.jabberstudio.jso.StreamDataFactory;
import org.jabberstudio.jso.StreamException;
import org.jabberstudio.jso.sasl.SASLAuthPacket;
import org.jabberstudio.jso.sasl.SASLFailurePacket;
import org.jabberstudio.jso.sasl.SASLPacket;
import org.netbeans.lib.collab.AuthenticationException;
import org.netbeans.lib.collab.CollaborationException;
import org.netbeans.lib.collab.SASLClientProvider;
import org.netbeans.lib.collab.SASLData;
import org.netbeans.lib.collab.SASLProviderException;
import org.saxpath.SAXPathException;


/**
 *
 * @author Mridul Muralidharan
 */
public class AuthProcessor {
    
    private Stream stream;
    private StreamDataFactory sdf;
    
    public static final int SASL_AUTH_TIMEOUT_MS = 30000;

    public void setStream(Stream stream){
        this.stream = stream;
    }
    
    public void setStreamDataFactory(StreamDataFactory sdf){
        this.sdf = sdf;
    }
    
    //public void saslAuthenticate(final SASLMechanism mechanism)
    public void saslAuthenticate(SASLClientProvider provider , String mechanism)
    throws CollaborationException {

        PacketWatcher _watcher = null;
        
        try{
            try{
                _watcher = new PacketWatcher(sdf , "sasl:*" ,
                    "sasl", SASLPacket.NAMESPACE);
            }catch(SAXPathException saxpEx){
                throw new CollaborationException("sasl error" , saxpEx);
            }
            
            PacketWatcher watcher = _watcher;

            PacketHandler handler = new SASLPacketHandler(watcher , provider , 
                    stream , sdf , mechanism);
            
            watcher.setStream(stream);
            watcher.setHandler(handler);
            watcher.init();

            try{
                int retval = handler.process(null);
                
                if (retval == PacketHandler.FAILURE){
                    throw new CollaborationException("sasl error");
                }
            }catch(CollaborationException ex){
                throw ex;
            }catch(Exception ex){
                throw new CollaborationException("sasl error" , ex);
            }

            while (!watcher.isCompleted()){
                try{
                    stream.process();
                }catch(StreamException stEx){
                    //throw new CollaborationException("sasl error" , stEx);
                    throw new AuthenticationException("sasl error" , stEx);
                }
                
                synchronized(watcher){
                    try{
                        watcher.wait(10);
                    }catch(InterruptedException iEx){}
                }
                
                if (watcher.exceedsTimeout(SASL_AUTH_TIMEOUT_MS)){
                    //throw new CollaborationException("sasl error");
                    throw new AuthenticationException("sasl error - timeout");
                }
            }

            assert (PacketHandler.IN_PROGRESS != watcher.getStatus());

            Throwable th = watcher.getThrowable();
            if (null != th){
                if (th instanceof AuthenticationException){
                    throw (AuthenticationException)th;
                }

                throw new AuthenticationException("sasl error" , th);
            }

            if (PacketHandler.SUCCESS != watcher.getStatus()){
                throw new AuthenticationException("sasl error : status - " +
                        watcher.getStatus());
            }
        }finally{
            if (null != _watcher){
                _watcher.release();
            }
        }

        return ;
    }
}

class SASLPacketHandler implements PacketHandler{
    
    private PacketWatcher watcher;
    private SASLClientProvider provider;
    private Stream stream;
    private StreamDataFactory sdf;
    private String mechanism;
    
    public SASLPacketHandler(PacketWatcher watcher , SASLClientProvider provider ,
            Stream stream , StreamDataFactory sdf , String mechanism){
        this.watcher = watcher;
        this.provider = provider;
        this.stream = stream;
        this.sdf = sdf;
        this.mechanism = mechanism;
    }
            
    
    public void preProcess(){
        //synchronized(watcher)
            watcher.notify();
    }
    
    public int process(Packet packet) throws Exception{
        
        boolean serverAuthFailed = false;
        boolean authFailed = false;
        
        if (null != packet &&
                !(packet instanceof SASLPacket)){
            return PacketHandler.FAILURE;
        }

        SASLPacket spacket = (SASLPacket)packet;

        if (null != spacket){
            serverAuthFailed = (spacket.getAction().equals(SASLPacket.ABORT) ||
                    spacket.getAction().equals(SASLPacket.FAILURE));
            authFailed = serverAuthFailed;
        }
                        
        
        if (provider instanceof NativeSASLClientProvider){
            NativeSASLClientProvider nprovider = (NativeSASLClientProvider)provider;
            try{
                Packet response = (Packet)nprovider.process(packet);
                if (null == response || serverAuthFailed){
                    return authFailed ? PacketHandler.FAILURE : PacketHandler.SUCCESS;
                }
                NSI rnsi = response.getNSI();
                if (rnsi.equals(SASLPacket.NAME_ABORT) || 
                        rnsi.equals(SASLPacket.NAME_FAILURE)){
                    authFailed = true;
                }
                if (!serverAuthFailed){
                    stream.send(response);
                }
            }catch(SASLProviderException spEx){
                SASLFailurePacket failure = (SASLFailurePacket)sdf.
                        createElementNode(SASLFailurePacket.NAME ,
                            SASLFailurePacket.class);
                
                failure.setCondition(SASLFailurePacket.ABORTED_CONDITION);
                if (!serverAuthFailed){
                    stream.send(failure);
                }
                return PacketHandler.FAILURE;
            }
            return authFailed ? PacketHandler.FAILURE : PacketHandler.IN_PROGRESS;
        }
        
        // custom SASL module.
        SASLDataImpl data = new SASLDataImpl();
        data.setRequestData(null == spacket ? null : spacket.getData());
        data.setRequestStatus(getRequestStatus(spacket));

        try{
            provider.process(data);
        }catch(SASLProviderException spEx){
            data.setResponseStatus(SASLData.ABORT);
            data.setResponseData(null);
        }

        if (!isValidStatus(data.getResponseStatus()) ||
                SASLData.FAILURE == data.getResponseStatus() ||
                SASLData.ABORT == data.getResponseStatus()){
            
            if (!serverAuthFailed){
                // send 'failure' to the server.
                SASLFailurePacket failure = (SASLFailurePacket)sdf.
                        createElementNode(SASLFailurePacket.NAME ,
                            SASLFailurePacket.class);
                
                failure.setCondition(SASLData.FAILURE == data.getResponseStatus() ?
                    SASLFailurePacket.NOT_AUTHORIZED_CONDITION: 
                    SASLFailurePacket.ABORTED_CONDITION );
                
                failure.setData(data.getResponseData());
                stream.send(failure);
            }
            return PacketHandler.FAILURE;
        }
        
        byte[] respData = data.getResponseData();
        int respStatus = data.getResponseStatus();
        
        if (null == packet){
            // initial request.
            // set the mechanism to be used for the server.
            SASLAuthPacket saslAuthPacket = (SASLAuthPacket)
                sdf.createElementNode(getResponseNSI(respStatus) ,
                    SASLPacket.class);
            saslAuthPacket.setMechanismName(mechanism);
            saslAuthPacket.setData(respData);
            assert !serverAuthFailed;
            stream.send(saslAuthPacket);
        }
        else{
            if (!serverAuthFailed){            
                if (null != respData){
                    SASLPacket saslpacket = (SASLPacket)sdf.createElementNode(getResponseNSI(respStatus) ,
                            SASLPacket.class);
                    saslpacket.setData(respData);
                    stream.send(saslpacket);
                }
                if (SASLData.SUCCESS == data.getResponseStatus()){
                    if (serverAuthFailed){
                        throw new IllegalStateException("Client provider response to server's failed auth is success.");
                    }
                    return PacketHandler.SUCCESS;
                }
            }
        }
        return authFailed ? PacketHandler.FAILURE : PacketHandler.IN_PROGRESS;
    }
    
    public void postProcess(){
        //synchronized(watcher)
            watcher.notify();
    }

    private static int getRequestStatus(SASLPacket packet){

        if (null == packet){
            return SASLData.START;
        }
        SASLPacket.Action action = packet.getAction();

        if (null == action){
            return SASLData.START;
        }
        if (action.equals(SASLPacket.CHALLENGE)){
            return SASLData.CHALLENGE;
        }
        if (action.equals(SASLPacket.FAILURE)){
            return SASLData.FAILURE;
        }
        if (action.equals(SASLPacket.RESPONSE)){
            return SASLData.RESPONSE;
        }
        if (action.equals(SASLPacket.SUCCESS)){
            return SASLData.SUCCESS;
        }
        if (action.equals(SASLPacket.ABORT)){
            return SASLData.ABORT;
        }

        // unknown action.
        return SASLData.FAILURE;
    }
    
    private static boolean isValidStatus(int status){
        return status == SASLData.START ||
                status == SASLData.CHALLENGE ||
                status == SASLData.FAILURE ||
                status == SASLData.RESPONSE ||
                status == SASLData.ABORT ||
                status == SASLData.SUCCESS;
    }
    
    private NSI getResponseNSI(int respStatus){
        if (respStatus == SASLData.CHALLENGE){
            return SASLPacket.NAME_CHALLENGE;
        }
        if (respStatus == SASLData.FAILURE){
            return SASLPacket.NAME_FAILURE;
        }
        if (respStatus == SASLData.RESPONSE){
            return SASLPacket.NAME_RESPONSE;
        }
        if (respStatus == SASLData.START){
            return SASLPacket.NAME_AUTH;
        }
        if (respStatus == SASLData.SUCCESS){
            return SASLPacket.NAME_SUCCESS;
        }
        if (respStatus == SASLData.ABORT){
            return SASLPacket.NAME_ABORT;
        }
        // ??!!
        return SASLPacket.NAME_FAILURE;
    }
}
