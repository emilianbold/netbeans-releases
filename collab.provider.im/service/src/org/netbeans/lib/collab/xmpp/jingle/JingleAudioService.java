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

import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;
import org.jabberstudio.jso.InfoQuery;
import org.jabberstudio.jso.JID;
import org.jabberstudio.jso.Packet;
import org.jabberstudio.jso.PacketError;
import org.jabberstudio.jso.StreamDataFactory;
import org.jabberstudio.jso.StreamException;
import org.netbeans.lib.collab.CollaborationException;
import org.netbeans.lib.collab.CollaborationPrincipal;
import org.netbeans.lib.collab.P2PAudioService;
import org.netbeans.lib.collab.P2PAudioSession;
import org.netbeans.lib.collab.P2PIncomingAudioListener;
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
public class JingleAudioService implements P2PAudioService {
    JingleService _parent;
    XMPPSession _session;
    Vector _outgoingcodecs;
    Vector _incomingcodecs;
    Vector _transports;
    Vector _listeners;
    boolean _enabled = false;
    public static final String FEATURE_JINGLE = "http://www.xmpp.org/extensions/xep-0166.html#ns";
    public static final String FEATURE_JINGLE_AUDIO = "http://www.xmpp.org/extensions/xep-0167.html#ns";
    private static final String FEATURE_JINGLE_RAWUDP = "http://www.xmpp.org/extensions/xep-0176.html#ns";
    private Set features;
    Hashtable _p2plist; // JingleAudioSessions keyed by iq id
    Hashtable _sesslist; // JingleAudioSessions keyed by Jingle session id
    static final int ERROR_BAD_REQUEST = 0;
    static final int ERROR_UNSUPPORTED_TRANSPORT = 1;
    static final int ERROR_UNSUPPORTED_CONTENT = 2;
    /** Creates a new instance of JingleAudioService */
    public JingleAudioService(JingleService parent, XMPPSession session) {
        _parent = parent;
        _session = session;
        _outgoingcodecs = new Vector();
        _incomingcodecs = new Vector();
        _transports = new Vector();
        _enabled = false;
        _listeners = new Vector();
        features = new LinkedHashSet();
        features.add(FEATURE_JINGLE);
        features.add(FEATURE_JINGLE_AUDIO);
        features.add(FEATURE_JINGLE_RAWUDP);
        _p2plist = new Hashtable();
        _sesslist = new Hashtable();
    }
    
    public void addOutgoingCodec(int id, String name, int channels, double clock, Properties props) {
        Codec c = new Codec(id, name, channels, clock);
        if(_outgoingcodecs.contains(c))
            return;
        _outgoingcodecs.add(c);
    }
    
    public void removeOutgoingCodec(int id, String name, int channels, double clock, Properties props) {
        Codec c = new Codec(id, name, channels, clock);
        if(_outgoingcodecs.contains(c)){
            _outgoingcodecs.remove(c);
        }
    }
    public void addIncomingCodec(int id, String name, int channels, double clock, Properties props) {
        Codec c = new Codec(id, name, channels, clock);
        if(_incomingcodecs.contains(c))
            return;
        _incomingcodecs.add(c);
    }
    
    public void removeIncomingCodec(int id, String name, int channels, double clock, Properties props) {
        Codec c = new Codec(id, name, channels, clock);
        if(_incomingcodecs.contains(c)){
            _incomingcodecs.remove(c);
        }
    }
    
    public void addTransport(P2PTransport transport) {
        if(_transports.contains(transport)){
            return;
        }
        _transports.add(transport);
    }
    
    public void enable() {
        _enabled = true;
    }
    
    public void disable() {
        _enabled = false;
    }
    
    public void addIncomingListener(P2PIncomingAudioListener listener) {
        _listeners.add(listener);
    }
    
    public P2PAudioSession createSession(String who) {
        String iq=_session.nextID("jingle");
        String sessid = _session.nextID("session");
        JingleAudioSession sess = new JingleAudioSession(who, this, _session, iq, sessid, JingleAudioSession.OUTGOING);
        _p2plist.put(iq, sess);
        _sesslist.put(sessid, sess);
        return sess;
    }
    
    public boolean isAudioEnabled(String uid) {
        
        // Send a disco, check if it contains Jingle, Jingle Audio and Jingle Raw UDP
        // Clients should cache this info and not call this method repeatedly
        Set features = _session.getFeatures(uid);
        if(features != null && features.contains(FEATURE_JINGLE) && features.contains(FEATURE_JINGLE_AUDIO) &&
                features.contains(FEATURE_JINGLE_RAWUDP)){
            return true;
        } else{
            return false;
        }
    }
    
    public Set getAudioFeatures() {
        return features;
    }
    
    public Vector getCodecs(){
        return _outgoingcodecs;
    }
    public Vector getTransports(){
        return _transports;
    }
    
    boolean isJingleAudioPacket(Packet packet) {
        JingleAudioSession sess = (JingleAudioSession)_p2plist.get(packet.getID());
        if(sess != null){
            return true;
        }
        Jingle jingle = (Jingle) packet.getFirstElement(Jingle.NAME);
        if(jingle != null && _sesslist.get(jingle.getSessionID()) != null ){
            return true;
        }
        return false;
    }
    
    public void processPacket(Packet packet) throws CollaborationException{
        // First check if it is associated with an existing session
        String id = packet.getID();
        JingleAudioSession sess = (JingleAudioSession)_p2plist.get(id);
        if(sess != null){
            sess.processPacket(packet);
        } else{
            Jingle jingle = (Jingle) packet.getFirstElement(Jingle.class);
            if(jingle != null){
                try{
                sess = (JingleAudioSession)_sesslist.get(jingle.getSessionID());
                if(sess != null){
                    sess.processPacket(packet);
                    return;
                }
                }
                catch(Exception ee){
                    ee.printStackTrace();
                }
                // We got a jingle packet, which is not associated with an active session
                // This is only valid for session-initiate
                if(Jingle.ACTION_SESSION_INITIATE.equals(jingle.getAction())){
                    // Create a session and proceed
                    JingleAudioSession newsess;
                    newsess = new JingleAudioSession(jingle.getInitiator(), this, _session, packet.getID(), jingle.getSessionID(), JingleAudioSession.INCOMING);
                    Vector c = new Vector();
                    Vector t = new Vector();
                    JingleAudio aud;
                    List contents = jingle.getContentList();
                    for(Iterator i = contents.listIterator(); i.hasNext(); ){
                        String c1 = (String)i.next();
                        aud = jingle.getContentDescription(c1);
                        List payloads = aud.listPayloads();
                        for(Iterator j = payloads.listIterator(); j.hasNext(); ){
                            int payload_id = Integer.parseInt((String)j.next());
                            Codec cod = null;
                            cod = new Codec(payload_id, aud.getPayloadName(payload_id), aud.getPayloadChannels(payload_id),
                                    aud.getPayloadClock(payload_id));
                            if( _incomingcodecs.contains(cod)){
                                c.add(cod);
                            }
                            
                        }
                        
                        JingleTransport trans = jingle.getContentTransport(c1);
                        if(trans instanceof JingleUDP){
                            List translist = trans.listCandidates();
                            for(Iterator k = translist.iterator(); k.hasNext(); ){
                                JingleCandidate cand = (JingleCandidate)k.next();
                                P2PUdpTransport t1 = new P2PUdpTransport(cand.getName(), cand.getIP(), cand.getPort());
                                t.add(t1);
                            }
                        } else{
                            // Only UDP tranport is supported for now
                            sendError(ERROR_UNSUPPORTED_TRANSPORT, packet.getFrom(), packet.getID());
                            return;
                        }
                    }
                    if(c.size() == 0){
                        // No codecs were found that could be used
                        sendError(ERROR_UNSUPPORTED_CONTENT, packet.getFrom(), packet.getID());
                        return;
                        
                    }
                    _p2plist.put(packet.getID(), newsess);
                    _sesslist.put(jingle.getSessionID(), newsess);
                    newsess.setCodecs(c);
                    newsess.setTransports(t);
                    // ACK the session-initiate
                    InfoQuery iq = (InfoQuery)_session.getDataFactory().createPacketNode(XMPPSession.IQ_NAME);
                    iq.setFrom(_session.getCurrentUserJID());
                    iq.setTo(packet.getFrom());
                    iq.setID(packet.getID());
                    iq.setType(InfoQuery.RESULT);
                    try{
                        _session.getConnection().send(iq);
                    }catch(StreamException ex){
                        throw new CollaborationException("Could not ack session-initiate", ex);
                    }
                    for(Iterator i = _listeners.iterator(); i.hasNext(); ){
                        P2PIncomingAudioListener incoming = (P2PIncomingAudioListener)i.next();
                        incoming.onIncoming(newsess);
                    }
                } else{
                    // Return an error
                    sendError(ERROR_BAD_REQUEST, packet.getTo(), packet.getID());
                }
            }
        }
    }
    
    
    public void sendError(int condition, JID to, String id) throws CollaborationException{
        StreamDataFactory sdf = _session.getDataFactory();
        PacketError err = null;
        switch(condition){
            case ERROR_BAD_REQUEST:
                err = sdf.createPacketError(PacketError.CANCEL, "bad-request");
                break;
            case ERROR_UNSUPPORTED_TRANSPORT:
                err = sdf.createPacketError(PacketError.CANCEL, "feature-not-implemented");
                err.addElement("unsupported-transports", "http://www.xmpp.org/extensions/xep-0166.html#ns-errors");
                break;
            case ERROR_UNSUPPORTED_CONTENT:
                err = sdf.createPacketError(PacketError.CANCEL, "feature-not-implemented");
                err.addElement("unsupported-content", "http://www.xmpp.org/extensions/xep-0166.html#ns-errors");
                break;
        }
        InfoQuery iq = (InfoQuery)sdf.createPacketNode(XMPPSession.IQ_NAME);
        iq.setType(InfoQuery.ERROR);
        iq.setTo(to);
        //iq.setFrom(packet.getTo());
        iq.setID(id);
        iq.add(err);
        try{
            _session.getConnection().send(iq);
        }catch(StreamException ex){
            throw new CollaborationException("Could not send error response", ex);
        }
    }
    
    public class Codec{
        public Codec(int id, String name, int channels, double clock){
            this(id, name, channels, clock, null);
        }
        public Codec(int id, String name, int channels, double clock, Properties props){
            _id = id;
            _name = name;
            _channels = channels;
            _clock = clock;
            _props = props;
        }
        
        /**
         * Holds value of property _name.
         */
        private String _name;
        
        /**
         * Getter for property _name.
         *
         * @return Value of property _name.
         */
        public String getName() {
            return this._name;
        }
        
        /**
         * Setter for property _name.
         *
         * @param _name New value of property _name.
         */
        public void setName(String name) {
            this._name = name;
        }
        
        /**
         * Holds value of property _channels.
         */
        private int _channels;
        
        /**
         * Getter for property _channels.
         *
         * @return Value of property _channels.
         */
        public int getChannels() {
            return this._channels;
        }
        
        /**
         * Setter for property _channels.
         *
         * @param _channels New value of property _channels.
         */
        public void setChannels(int channels) {
            this._channels = channels;
        }
        
        /**
         * Holds value of property _clock.
         */
        private double _clock;
        
        /**
         * Getter for property _clock.
         *
         * @return Value of property _clock.
         */
        public double getClock() {
            return this._clock;
        }
        
        /**
         * Setter for property _clock.
         *
         * @param _clock New value of property _clock.
         */
        public void setClock(double clock) {
            this._clock = clock;
        }
        
        private Properties _props;
        
        public Properties getProperties(){
            return _props;
        }
        
        public void setProperties(Properties props){
            _props = props;
        }
        
        protected int _id;
        public void setID(int id){
            _id = id;
        }
        public int getID(){
            return _id;
        }
        
        
        public int hashCode(){
            // Calculating hashcode for long as documented in JavaDocs of Long
            int hash;
            long v = Double.doubleToLongBits(_clock);
            hash = _id*1000 + _channels + ((int)(v^(v>>>32)));
            hash += _props!=null?_props.hashCode():0;
            
            return hash;
            
        }
        
        public boolean equals(Object c){
            if(c==null) return false;
            if(this.hashCode() == c.hashCode()){
                return true;
            }
            return false;
        }
        
        public String toString(){
            return ("ID: " + _id + ", Codec: " + _name + ", channels: " + _channels + ", clock: " + _clock + ", hashcode = " + hashCode());
        }
        
    }
}
