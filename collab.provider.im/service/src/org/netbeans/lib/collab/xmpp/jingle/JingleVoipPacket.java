/*
 * JingleVoipPacket.java
 *
 * Created on January 6, 2006, 11:30 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.lib.collab.xmpp.jingle;

import org.jabberstudio.jso.StreamDataFactory;
import org.jabberstudio.jso.Packet;
import org.jabberstudio.jso.PacketError;
import org.jabberstudio.jso.JID;
import org.jabberstudio.jso.InfoQuery;
import org.jabberstudio.jso.StreamElement;
import org.jabberstudio.jso.NSI;
import org.jabberstudio.jso.Stream;
import org.netbeans.lib.collab.xmpp.XMPPSession;
import java.util.List;

/**
 *
 * @author jerry
 */
public class JingleVoipPacket {
    
    /** Creates a new instance of JingleVoipPacket */
    Packet _rawpacket;
    XMPPSession _session;
    StreamDataFactory _sdf;
    boolean isNewPkt = true;
    String _target;
    String _initiator;
    String _description;
    String _uniqueSID; // This is the Jingle session ID
    String _id; // this is the iq packet id
    String _action;
    String _redirLocation;
    private static NSI IQ_NAME = new NSI("iq", null);
    private static NSI jingle = new NSI("jingle", "http://jabber.org/protocol/jingle");
    private static NSI redirect = new NSI("redirect", "urn:ietf:params:xml:ns:xmpp-stanzas");
    private static NSI error  = new NSI("error", null);
    public static final String ACTION_INITIATE = "initiate";
    public static final String ACTION_REDIRECT = "redirect";
    public static final String ACTION_TERMINATE = "terminate";
    
    public JingleVoipPacket(StreamDataFactory sdf, XMPPSession session, org.jabberstudio.jso.Packet packet) {
        _sdf = sdf;
        _session = session;
        _rawpacket = packet;
        JID from = packet.getFrom();
        JID to = packet.getTo();
        if (from != null) {
            setInitiator(from.toString());
        }
        if (to != null) {
            setTarget(to.toString());
        }
        List elements = packet.listElements(jingle);
        if(elements.size() > 0){
            StreamElement ele = (StreamElement)elements.get(0);
            _action = ele.getAttributeValue("action");
        }
        else{
            // Could be a redirect packet
            PacketError err = packet.getError();
            if(err.getDefinedCondition().equals(PacketError.REDIRECT_CONDITION)){
		StreamElement red = err.getFirstElement("redirect");
                String redurl = red.normalizeText();
                _action = ACTION_REDIRECT;
                _redirLocation = redurl;
            }
        }
        isNewPkt = false;        
    }
    
    public JingleVoipPacket(StreamDataFactory sdf, XMPPSession session){
        isNewPkt = true;
        _sdf = sdf;
        _session = session;
    }

    public void setTarget(String target) {
        _target = target;
    }

    public void setUniqueSID(String sid) {
        _uniqueSID = sid;
    }

    public void setDescription(String description) {
        _description = description;
    }

    public void setInitiator(String caller) {
        _initiator = caller;
    }

    public String getTarget() {
        return _target;
    }

    public String getInitiator() {
        return _initiator;
    }

    public String getUniqueSID() {
        return _uniqueSID;
    }

    public String getDescription() {
        return _description;
    }
    
    public String getAction(){
        return _action;
    }
    
    public void setAction(String action){
        _action = action;
    }
    
    public void setRedirLocation(String l){
        _redirLocation = l;
    }
    
    public String getRedirLocation(){
        return _redirLocation;
    }
    
    public String getID(){
	return _id;
    }
    public void setID(String id){
	_id = id;
    }
    
    public void sendInitiate(){
       InfoQuery iqQuery = (InfoQuery)_sdf.createPacketNode(IQ_NAME, InfoQuery.class);
        iqQuery.setTo(new JID(_target));
        iqQuery.setType(InfoQuery.SET);
	if(getID() == null){
	    setID(nextID("jingle"));
	}
        iqQuery.setID(getID());
        
        StreamElement jelement = iqQuery.addElement(jingle, null);
        jelement.setAttributeValue("action", ACTION_INITIATE);
                
        sendPacket(iqQuery);
    }
    
    public void sendRedirect(){
        InfoQuery iqQuery = (InfoQuery)_sdf.createPacketNode(IQ_NAME, InfoQuery.class);
        iqQuery.setTo(new JID(_target));
        iqQuery.setType(InfoQuery.ERROR);
	iqQuery.setID(getID());
        PacketError err = (PacketError) _sdf.createPacketError(PacketError.MODIFY, null);
        err.setDefinedCondition(PacketError.REDIRECT_CONDITION);
        err.setAttributeValue("code", "302");
        StreamElement redir = err.getFirstElement("redirect");
        redir.addText(_redirLocation);
        iqQuery.add(err);
        sendPacket(iqQuery);
    }
    
    public void sendTerminate(){
        InfoQuery iqQuery = (InfoQuery) _sdf.createPacketNode(IQ_NAME, InfoQuery.class);
        iqQuery.setTo(new JID(_target));
        iqQuery.setType(InfoQuery.SET);
        iqQuery.setID(getID()==null?nextID("jingle"):getID());
        setAction(ACTION_TERMINATE);
        StreamElement jelement = iqQuery.addElement(jingle, null);
        jelement.setAttributeValue("action", getAction());
        sendPacket(iqQuery);
    }
    
    private void sendPacket(InfoQuery iqQuery){
        Stream stream = _session.getConnection();
        try{
            stream.send(iqQuery);
        }catch(Exception e){
            //TODO: Handle error gracefully
        }
        
    }
      private String nextID(String key) {
        return _session.nextID(key);
    }

}
