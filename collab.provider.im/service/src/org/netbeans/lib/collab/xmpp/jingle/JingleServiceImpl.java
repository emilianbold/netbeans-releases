/*
 * JingleServiceImpl.java
 *
 * Created on January 5, 2006, 2:41 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.lib.collab.xmpp.jingle;
import org.jabberstudio.jso.StreamException;
import org.netbeans.lib.collab.MediaListener;
import org.netbeans.lib.collab.MediaService;
import org.jabberstudio.jso.JSOImplementation;
import org.jabberstudio.jso.StreamDataFactory;
import org.jabberstudio.jso.Packet;
import org.netbeans.lib.collab.xmpp.XMPPSession;
import java.util.ArrayList;
import java.util.HashMap;
import org.jabberstudio.jso.InfoQuery;
import org.jabberstudio.jso.JID;
import org.jabberstudio.jso.NSI;
import org.jabberstudio.jso.StreamElement;
import org.jabberstudio.jso.util.PacketMonitor;
import org.apache.log4j.*;

/**
 *
 * @author jerry
 */
public class JingleServiceImpl implements MediaService{
    
    private static JSOImplementation _jso = JSOImplementation.getInstance();
    private static StreamDataFactory _sdf = _jso.getDataFactory();
    
    private XMPPSession _session  = null;
    private ArrayList _voipListeners;
    private HashMap  _voipsessions;
    private static Logger logger = LogManager.getLogger("org.netbeans.lib.collab.xmpp.jingle");
    
    /** Creates a new instance of JingleServiceImpl */
    public JingleServiceImpl(XMPPSession session) {
        
        _session = session;
        _voipListeners = new ArrayList();
        _voipsessions = new HashMap();
    }
    
    public void addListener(MediaListener vlistener){
        _voipListeners.add(vlistener);
    }
    public void removeListener(MediaListener vl){
        _voipListeners.remove(vl);
    }
    
    public void initiate(String target) {
        
        JingleVoipPacket pkt = new JingleVoipPacket(_sdf, _session);
        pkt.setAction(JingleVoipPacket.ACTION_INITIATE);
        pkt.setTarget(target);
        pkt.sendInitiate();
        _voipsessions.put(pkt.getID(), "INITIATE");
    }
    
    public void redirect(String id, String caller, String redirlocation){
        JingleVoipPacket pkt = new JingleVoipPacket(_sdf, _session);
        pkt.setTarget(caller);
        pkt.setRedirLocation(redirlocation);
        pkt.setID(id);
        pkt.sendRedirect();
        _voipsessions.put(pkt.getID(), "REDIRECT");
    }
    
    public void terminate(String id, String target){
        JingleVoipPacket pkt = new JingleVoipPacket(_sdf, _session);
        pkt.setTarget(target);
        pkt.setID(id);
        pkt.sendTerminate();
        _voipsessions.remove(pkt.getID());
    }
    
    public void processVoipPacket(Packet packet){
        
        for(int i = 0; i<_voipListeners.size(); i++){
            MediaListener ln = (MediaListener)_voipListeners.get(i);
            _session.addWorkerRunnable(new JingleListenerInvoker(packet, ln));
        }
        
    }
    
    
    public void setSessionState(String sessionid, String state){
        _voipsessions.put(sessionid, state);
    }
    public String getSessionState(String sessionid){
        return (String)_voipsessions.get(sessionid);
    }
    
    
    public String findMediaGateway() {
        if(_session.getVoipComponent() != null){
            return _session.getVoipComponent().toString();
        }
        return null;
    }
    
    
    class JingleListenerInvoker implements Runnable{
        Packet packet;
        MediaListener ln;
        
        public JingleListenerInvoker(Packet packet, MediaListener ln){
            this.packet = packet;
            this.ln = ln;
        }
        
        public void run(){
            JingleVoipPacket jpkt;
            jpkt = new JingleVoipPacket(_sdf, _session, packet);
            if(jpkt.getAction().equalsIgnoreCase(jpkt.ACTION_INITIATE)){
                ln.onInitiate(packet.getAttributeValue("id"),jpkt.getInitiator(), packet.getAttributeValue("to"), jpkt.getUniqueSID());
            } else if(jpkt.getAction().equalsIgnoreCase(jpkt.ACTION_REDIRECT)){
                ln.onRedirect(packet.getAttributeValue("id"), jpkt.getRedirLocation());
            } else if(jpkt.getAction().equalsIgnoreCase(jpkt.ACTION_TERMINATE)){
                ln.onTerminate(packet.getAttributeValue("id"));
            }
        }
    }
    
    /**
     * Called from XMPPSession.isJinglePacket to determine
     * if this packet ID is used by jingle
     */
    public boolean isJingleID(String id){
        String status = getSessionState(id);
        if(status == null){
            return false;
        } else{
            return true;
        }
    }
    private static NSI IQ_NAME = new NSI("iq", null);
    private static NSI reach = new NSI("reach", "http://jabber.org/protocol/reach");
    public String getAddress(String userid) {
        logger.debug("Looking up address for " + userid);
        InfoQuery iq = (InfoQuery)_sdf.createPacketNode(IQ_NAME, InfoQuery.class);
        iq.setTo(new JID(userid));
        iq.setType(InfoQuery.GET);
        iq.setID(_session.nextID("reach"));
        StreamElement r = iq.addElement(reach);
        Packet result;
        try {
            result = PacketMonitor.sendAndWatch(_session.getConnection(), iq);
            if(result.getType().equals(InfoQuery.RESULT)){
                StreamElement reach = result.getFirstElement("reach");
                if(reach != null){
                    StreamElement addr = reach.getFirstElement("addr");
                    if(addr != null){
                        String url;
                        url = addr.getAttributeValue("uri");
                        logger.debug("Returning address=" + url);
                        return url;
                    }
                    logger.debug("Got a null address");
                }
                logger.debug("no reach element");
            }
            logger.debug("Got a non-result packet");
        } catch (Exception ex) {
            logger.error("Got an Exception when looking up address " + ex.getMessage());
        }
        return null;
        
    }
}