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
