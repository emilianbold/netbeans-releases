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

import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jabberstudio.jso.InfoQuery;
import org.jabberstudio.jso.JID;
import org.jabberstudio.jso.JIDFormatException;
import org.jabberstudio.jso.Packet;
import org.jabberstudio.jso.PacketError;
import org.jabberstudio.jso.StreamDataFactory;
import org.jabberstudio.jso.StreamException;
import org.netbeans.lib.collab.CollaborationException;
import org.netbeans.lib.collab.P2PAudioSession;
import org.netbeans.lib.collab.P2PAudioSessionListener;
import org.netbeans.lib.collab.P2PTransport;
import org.netbeans.lib.collab.P2PUdpTransport;
import org.netbeans.lib.collab.xmpp.XMPPSession;
import org.netbeans.lib.collab.xmpp.jso.iface.x.jingle.Jingle;
import org.netbeans.lib.collab.xmpp.jso.iface.x.jingle.JingleAudio;
import org.netbeans.lib.collab.xmpp.jso.iface.x.jingle.JingleTransport;
import org.netbeans.lib.collab.xmpp.jso.iface.x.jingle.JingleUDP;
import org.netbeans.lib.collab.xmpp.jso.impl.x.jingle.JingleCandidate;

/**
 *
 * @author jerry
 */
public class JingleAudioSession implements P2PAudioSession{
    
    String _who;
    JingleAudioService _service;
    XMPPSession _session;
    Vector _listeners;
    Vector _echolisteners;
    String iqID;
    String sessionid;
    int _calltype;
    int _state;
    Vector _codecs; // the codecs offered by the other party
    Vector _transports; // the transports offered by the other party
    public static final int CONNECTION_TEST_SUCCESS = 0;
    public static final int CONNECTION_TEST_FAIL = 1;
    
    static final int STATE_INITIATED = 1;
    static final int STATE_SESSION_ACCEPTED = 2;
    static final int STATE_TERMINATED = 3;
    
    static Logger logger = LogManager.getLogger("org.netbeans.lib.collab.xmpp.jingle");
    /** Creates a new instance of JingleAudioSession */
    public JingleAudioSession(String who, JingleAudioService service, XMPPSession session, String iq, String sess, int calltype) {
        if(calltype != INCOMING && calltype != OUTGOING){
            throw new IllegalArgumentException("Call type must be either INCOMING or OUTGOING");
        }
        _who = who;
        _service = service;
        _session = session;
        _listeners = new Vector();
        iqID = iq;
        sessionid = sess;
        _state = 0;
        _calltype = calltype;
        _echolisteners = new Vector();
    }
    
    public void addListener(P2PAudioSessionListener listener) {
        _listeners.add(listener);
    }
    
    public void start() throws CollaborationException{
        if(_calltype == OUTGOING){
            startOutgoing();
        } else{
            startIncoming();
        }
    }
    
    protected void startIncoming() throws CollaborationException{
        logger.debug("Starting incoming call with " + _who);
        // Start connectivity checks
        for(Iterator i = _transports.iterator(); i.hasNext(); ){
            P2PTransport t = (P2PTransport) i.next();
            if(t instanceof P2PUdpTransport){
                (new EchoTester(this, t.getAddress(), t.getPort() )).start();
            }
        }
        
        
        
        // 2. send "transport-info" with our transport
        StreamDataFactory sdf = _session.getDataFactory();
        InfoQuery iq = (InfoQuery)sdf.createPacketNode(XMPPSession.IQ_NAME, InfoQuery.class);
        String id = _session.nextID("jingle");
        iq.setTo(new JID(_who));
        iq.setType(InfoQuery.SET);
        iq.setID(id);
        _service._p2plist.put(id, this);
        Jingle jingle = (Jingle) iq.addElement(Jingle.NAME);
        jingle.setSessionID(sessionid);
        jingle.setAction(Jingle.ACTION_TRANSPORT_INFO);
        jingle.setInitiator(_who);
        jingle.setResponder(_session.getCurrentUserJID().toString());
        jingle.addContent("audio-content", _session.getCurrentUserJID().toString());
        /*
        Vector codecs;
        codecs = _service.getCodecs();
        if(codecs == null || codecs.size() == 0){
            throw new IllegalArgumentException("No codecs found");
        }
         */
        Vector transports;
        transports = _service.getTransports();
        if(transports == null || transports.size() == 0){
            throw new IllegalArgumentException("No transports found");
        }
        /*
        JingleAudio desc = (JingleAudio) sdf.createElementNode(JingleAudio.NAME);
        for(Iterator i=codecs.iterator(); i.hasNext();){
            JingleAudioService.Codec c = (JingleAudioService.Codec)i.next();
            desc.addPayload(c.getID(), c.getName(), c.getChannels(), c.getClock(), c.getProperties());
        }
        jingle.addDescription("audio-content", desc);
         */
        for(Iterator i = transports.iterator(); i.hasNext();){
            P2PTransport t = (P2PTransport) i.next();
            if(t instanceof P2PUdpTransport){
                JingleUDP udp = (JingleUDP) sdf.createElementNode(JingleUDP.NAME);
                udp.addCandidate(new JingleCandidate(t.getName(), t.getAddress(), t.getPort(), 0));
                jingle.addTransport("audio-content", udp);
                EchoListener echo = new EchoListener(t.getPort());
                echo.start();
                _echolisteners.add(echo);
            } else{
                throw new IllegalArgumentException("Only P2PUdpTransport is supported for now");
            }
        }
        try{
            _session.getConnection().send(iq);
        } catch(StreamException ex){
            throw new CollaborationException("Error while sending transport-info to " + _who , ex);
        }
        _state = STATE_INITIATED;
        
    }
    
    protected void startOutgoing() throws CollaborationException {
        // Check that all parameters we need are present
        if(_who == null){
            throw new IllegalArgumentException("target cannot be null");
        }
        logger.debug("Starting outgoing call with " + _who);
        Vector codecs;
        codecs = _service.getCodecs();
        if(codecs == null || codecs.size() == 0){
            throw new IllegalArgumentException("No codecs found");
        }
        Vector transports;
        transports = _service.getTransports();
        if(transports == null || transports.size() == 0){
            throw new IllegalArgumentException("No transports found");
        }
        // We're good to go
        StreamDataFactory sdf = _session.getDataFactory();
        InfoQuery iq = (InfoQuery) sdf.createPacketNode(XMPPSession.IQ_NAME, InfoQuery.class);
        iq.setTo(new JID(_who));
        iq.setType(InfoQuery.SET);
        iq.setID(iqID);
        Jingle jingle = (Jingle) iq.addElement(Jingle.NAME);
        jingle.setSessionID(sessionid);
        jingle.setAction(Jingle.ACTION_SESSION_INITIATE);
        jingle.setInitiator(_session.getCurrentUserJID().toString());
        jingle.setResponder(_who);
        jingle.addContent("audio-content", _session.getCurrentUserJID().toString());
        JingleAudio desc = (JingleAudio) sdf.createElementNode(JingleAudio.NAME);
        for(Iterator i=codecs.iterator(); i.hasNext();){
            JingleAudioService.Codec c = (JingleAudioService.Codec)i.next();
            desc.addPayload(c.getID(), c.getName(), c.getChannels(), c.getClock(), c.getProperties());
        }
        jingle.addDescription("audio-content", desc);
        for(Iterator i = transports.iterator(); i.hasNext();){
            P2PTransport t = (P2PTransport) i.next();
            if(t instanceof P2PUdpTransport){
                JingleUDP udp = (JingleUDP) sdf.createElementNode(JingleUDP.NAME);
                udp.addCandidate(new JingleCandidate(t.getName(), t.getAddress(), t.getPort(), 0));
                jingle.addTransport("audio-content", udp);
                EchoListener echo = new EchoListener(t.getPort());
                _session.addWorkerRunnable(echo);
                _echolisteners.add(echo);
                
            } else{
                throw new IllegalArgumentException("Only P2PUdpTransport is supported for now");
            }
        }
        try{
            _session.getConnection().send(iq);
        } catch(StreamException ex){
            throw new CollaborationException("Error while sending invite to " + _who , ex);
        }
        _state = STATE_INITIATED;
    }
    
    public void mute() {
        throw new UnsupportedOperationException("Method not implemented yet");
    }
    
    public void terminate() throws CollaborationException {
        String id = _session.nextID("jingle");
        StreamDataFactory sdf = _session.getDataFactory();
        InfoQuery iq = (InfoQuery)sdf.createPacketNode(XMPPSession.IQ_NAME, InfoQuery.class);
        iq.setTo(new JID(_who));
        iq.setFrom(_session.getCurrentUserJID());
        iq.setType(InfoQuery.SET);
        iq.setID(id);
        Jingle jingle = (Jingle) iq.addElement(Jingle.NAME);
        jingle.setSessionID(sessionid);
        jingle.setAction(Jingle.ACTION_SESSION_TERMINATE);
        jingle.setInitiator(_who);
        try{
            _session.getConnection().send(iq);
            
        } catch(StreamException ex){
            throw new CollaborationException("Error while terminating call to " + _who, ex);
        }
    }
    
    public void processPacket(Packet packet) throws CollaborationException{
        if(_state == STATE_INITIATED || _state == STATE_SESSION_ACCEPTED){
            // We're expecting an ACK
            InfoQuery iq = (InfoQuery)packet;
            if(iq.getType().equals(iq.RESULT)){
                List l = iq.listElements();
                if(l == null || l.size() == 0){
                    // This is an ack
                    logger.debug("Recd ACK");
                    // remove this IQ id from the list
                    _service._p2plist.remove(iq.getID());
                    return;
                }
            }
            if(iq.getType() == InfoQuery.ERROR){
                // Recd an error
                // Notify listener about it
                PacketError err = iq.getError();
                if(err.getFirstElement("unsupported-transports") != null){
                    notifyError(P2PAudioSessionListener.ERROR_NO_TRANSPORT);
                } else if(err.getFirstElement("unsupported-content") != null){
                    notifyError(P2PAudioSessionListener.ERROR_NO_CODEC);
                } else{
                    notifyError(P2PAudioSessionListener.ERROR_UNKNOWN);
                }
            }
            if(iq.getType() == InfoQuery.SET){
                Jingle jingle = (Jingle) iq.getFirstElement(Jingle.class);
                if(jingle != null){
                    if(jingle.getAction().equals(jingle.ACTION_SESSION_ACCEPT)){
                        _state = STATE_SESSION_ACCEPTED;
                        // send an ack
                        InfoQuery ack = (InfoQuery)(_session.getDataFactory().createPacketNode(XMPPSession.IQ_NAME));
                        ack.setID(iq.getID());
                        ack.setType(InfoQuery.RESULT);
                        ack.setTo(iq.getFrom());
                        try{
                            _session.getConnection().send(ack);
                            
                        } catch(Exception e){
                            logger.error("Could not send ack");
                        }
                        // Stop the echo listeners now
                        for(Iterator i = _echolisteners.iterator(); i.hasNext(); ){
                            EchoListener echo = (EchoListener)i.next();
                            echo.stopListener();
                        }
                        // Notify the session listeners that the call is now established
                        for(Iterator i = _listeners.iterator(); i.hasNext(); ){
                            P2PAudioSessionListener l = (P2PAudioSessionListener) i.next();
                            l.onEstablished();
                        }
                    } else if(jingle.getAction().equals(jingle.ACTION_TRANSPORT_INFO) && _calltype == OUTGOING){
                        // Other party has sent some new transports
                        // First ACK them
                        // Then check them with Echo
                        InfoQuery ack = (InfoQuery)(_session.getDataFactory().createPacketNode(XMPPSession.IQ_NAME));
                        ack.setID(iq.getID());
                        ack.setType(InfoQuery.RESULT);
                        ack.setTo(iq.getFrom());
                        try{
                            _session.getConnection().send(ack);
                            
                        } catch(Exception e){
                            logger.error("Could not send ack");
                        }
                        // Clear the existing list of transports
                        _transports = new Vector();
                        List contents = jingle.getContentList();
                        for(Iterator i = contents.iterator(); i.hasNext(); ){
                            String content = (String)i.next();
                            JingleTransport t = jingle.getContentTransport(content);
                            if(t instanceof JingleUDP){
                                List translist = t.listCandidates();
                                for(Iterator k = translist.iterator(); k.hasNext(); ){
                                    JingleCandidate cand = (JingleCandidate)k.next();
                                    P2PUdpTransport t1 = new P2PUdpTransport(cand.getName(), cand.getIP(), cand.getPort());
                                    _transports.add(t1);
                                    _session.addWorkerRunnable(new EchoTester(this, t1.getAddress(), t1.getPort()));
                                }
                            } else{
                                _service.sendError(_service.ERROR_UNSUPPORTED_TRANSPORT, packet.getFrom(), packet.getID());
                            }
                        }
                        
                    } else if(jingle.getAction().equals(Jingle.ACTION_SESSION_INFO)){
                        // Notify the session listeners of the informational message
                        for(Iterator i = _listeners.iterator(); i.hasNext(); ){
                            P2PAudioSessionListener l = (P2PAudioSessionListener) i.next();
                            switch(jingle.getInfo()){
                                case Jingle.INFO_RINGING:
                                    l.onRinging();
                                    break;
                                case Jingle.INFO_BUSY:
                                    l.onBusy();
                                    break;
                                case Jingle.INFO_HOLD:
                                    l.onHold();
                                    break;
                                case Jingle.INFO_MUTE:
                                    l.onMuted();
                                    break;
                            }
                            
                        }
                    } 
                }
            }
            
        }
        // Terminate requests can come any time
        InfoQuery iq = (InfoQuery)packet;
        if(iq.getType() == InfoQuery.SET){
            Jingle jingle = (Jingle) iq.getFirstElement(Jingle.class);
            if(jingle != null && jingle.ACTION_SESSION_TERMINATE.equals(jingle.getAction())){
                // Notify session listers of termination
                for(Iterator i = _listeners.iterator(); i.hasNext(); ){
                    P2PAudioSessionListener l = (P2PAudioSessionListener) i.next();
                    l.onTerminate();
                }
                for(Iterator i = _echolisteners.iterator(); i.hasNext();){
                    EchoListener l = (EchoListener)i.next();
                    l.stopListener();
                }
                _echolisteners.clear();
                _state = STATE_TERMINATED;
            }
            
        }
        
        
    }
    
    public void setCodecs(Vector c){
        _codecs = c;
    }
    
    public void setTransports(Vector t){
        _transports = t;
    }
    
    protected void notifyError(int condition){
        for(Iterator i = _listeners.iterator(); i.hasNext(); ){
            P2PAudioSessionListener l = (P2PAudioSessionListener)i.next();
            l.onError(condition);
        }
    }
    
    public void onConnectionTest(int status){
        try {
            if(status == CONNECTION_TEST_SUCCESS){
                //Definitively accept content & transport
                String id = _session.nextID("jingle");
                StreamDataFactory sdf = _session.getDataFactory();
                InfoQuery iq = (InfoQuery)sdf.createPacketNode(XMPPSession.IQ_NAME, InfoQuery.class);
                iq.setTo(new JID(_who));
                iq.setType(InfoQuery.SET);
                iq.setID(id);
                Jingle jingle = (Jingle) iq.addElement(Jingle.NAME);
                jingle.setSessionID(sessionid);
                jingle.setAction(Jingle.ACTION_SESSION_ACCEPT);
                jingle.setInitiator(_who);
                jingle.setResponder(_session.getCurrentUserJID().toString());
                jingle.addContent("audio-content", _who);
                JingleAudio desc = (JingleAudio) sdf.createElementNode(JingleAudio.NAME);
                if(_calltype == INCOMING){
                    for(Iterator i=_codecs.iterator(); i.hasNext(); ){
                        JingleAudioService.Codec c = (JingleAudioService.Codec) i.next();
                        desc.addPayload(c.getID(), c.getName(), c.getChannels(), c.getClock(), c.getProperties());
                    }
                }
                jingle.addDescription("audio-content", desc);
                for(Iterator i = _transports.iterator(); i.hasNext() ; ){
                    P2PTransport t = (P2PTransport)i.next();
                    if(t instanceof P2PUdpTransport){
                        JingleUDP udp = (JingleUDP) sdf.createElementNode(JingleUDP.NAME);
                        udp.addCandidate(new JingleCandidate(t.getName(), t.getAddress(), t.getPort(), 0));
                        jingle.addTransport("audio-content", udp);
                    }
                }
                _service._p2plist.put(id, this);
                try{
                    _session.getConnection().send(iq);
                } catch(StreamException ex){
                    logger.error("Could not acknowledge call");
                    notifyError(P2PAudioSessionListener.ERROR_CALL_FAILURE);
                }
            }
            else{
                // Connection test failed
                terminate();
                
            }
        } catch (JIDFormatException ex) {
            ex.printStackTrace();
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
        } catch (UnsupportedOperationException ex) {
            ex.printStackTrace();
        } catch(Exception ex){
            ex.printStackTrace();
        }
    }
    
    public void ringing() throws CollaborationException {
        // send <ringing/> to the other party
        String id = _session.nextID("jingle");
        StreamDataFactory sdf = _session.getDataFactory();
        InfoQuery iq = (InfoQuery)sdf.createPacketNode(XMPPSession.IQ_NAME, InfoQuery.class);
        iq.setTo(new JID(_who));
        iq.setType(InfoQuery.SET);
        iq.setID(id);
        Jingle jingle = (Jingle) iq.addElement(Jingle.NAME);
        jingle.setSessionID(sessionid);
        jingle.setAction(Jingle.ACTION_SESSION_INFO);
        jingle.setInitiator(_who);
        jingle.setResponder(_session.getCurrentUserJID().toString());
        jingle.setInfo(Jingle.INFO_RINGING);
    }
    
    public String getCaller(){
        if(_calltype == INCOMING){
            return _who;
            
        } else{
            return _session.getCurrentUserJID().toString();
        }
    }
    
    public void busy() throws CollaborationException {
        String id = _session.nextID("jingle");
        StreamDataFactory sdf = _session.getDataFactory();
        InfoQuery iq = (InfoQuery)sdf.createPacketNode(XMPPSession.IQ_NAME, InfoQuery.class);
        iq.setTo(new JID(_who));
        iq.setType(InfoQuery.SET);
        iq.setID(id);
        Jingle jingle = (Jingle) iq.addElement(Jingle.NAME);
        jingle.setSessionID(sessionid);
        jingle.setAction(Jingle.ACTION_SESSION_INFO);
        jingle.setInitiator(_who);
        jingle.setResponder(_session.getCurrentUserJID().toString());
        jingle.setInfo(Jingle.INFO_BUSY);
        
    }
    
    public int getCallType(){
        return _calltype;
    }
    
    public P2PTransport getOutgoingTransport(){
        if( _transports != null && _transports.size() != 0){
            return (P2PTransport) _transports.get(0);
        }
        else return null;
    }
}
