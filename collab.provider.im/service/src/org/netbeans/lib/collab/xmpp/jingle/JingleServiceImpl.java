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
