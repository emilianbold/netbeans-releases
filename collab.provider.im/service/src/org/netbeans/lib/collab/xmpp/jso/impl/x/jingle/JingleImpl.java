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

package org.netbeans.lib.collab.xmpp.jso.impl.x.jingle;

import java.util.Arrays;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import net.outer_planes.jso.ElementNode;
import net.outer_planes.jso.ExtensionNode;
import org.jabberstudio.jso.JID;
import org.jabberstudio.jso.NSI;
import org.jabberstudio.jso.StreamDataFactory;
import org.jabberstudio.jso.StreamElement;
import org.jabberstudio.jso.StreamObject;
import org.jabberstudio.jso.util.Utilities;
import org.netbeans.lib.collab.xmpp.jso.iface.x.jingle.Jingle;
import org.netbeans.lib.collab.xmpp.jso.iface.x.jingle.JingleContent;
import org.netbeans.lib.collab.xmpp.jso.iface.x.jingle.JingleAudio;
import org.netbeans.lib.collab.xmpp.jso.iface.x.jingle.JingleTransport;

/**
 *
 * @author jerry
 */
public class JingleImpl extends ExtensionNode implements Jingle {
    
    /**
     * Creates a new instance of JingleImpl
     */

    public JingleImpl(StreamDataFactory sdf) {
        super(sdf, NAME);
    }
    public JingleImpl(StreamDataFactory sdf, NSI n){
        super(sdf, n);
    }
    public JingleImpl(StreamElement parent, Jingle child){
        super(parent, (ExtensionNode)child);
    }
    
    public static final String legal_actions[] = {
        "content-accept",
        "content-add",
        "content-modify",
        "content-remove",
        "session-accept",
        "session-info",
        "session-initiate",
        "session-terminate",
        "transport-info",
    };
    static {
        Arrays.sort(legal_actions);
    }
    
    public static final String ACTION = "action";
    public static final String INITIATOR = "initiator";
    public static final String RESPONDER = "responder";
    public static final String SID = "sid";
    public static final String REDIRECT = "redirect";
    
    public void setAction(String action){
        if(Arrays.binarySearch(legal_actions, action) < 0){
            throw new IllegalArgumentException(action + " is not a valid jingle action, see XEP-166 for list");
        }
        setAttributeValue(ACTION, action);
    }

    public String getAction() {
        return getAttributeValue(ACTION);
    }
    
    public StreamObject copy(StreamElement parent){
        return new JingleImpl(parent, this);
    }

    public void setInitiator(String initiator){
        setAttributeValue(INITIATOR, initiator);
    }

    public String getInitiator() {
        return getAttributeValue(INITIATOR);
    }

    public void setResponder(String responder){
        setAttributeValue(RESPONDER, responder);
    }

    public String getResponder() {
        return getAttributeValue(RESPONDER);
    }

    public void setSessionID(String sid) {
        setAttributeValue(SID, sid);
    }

    public String getSessionID() {
        return getAttributeValue(SID);
    }

//    public void setRedirect(String redir_url) {
//        if (Utilities.isValidString(redir_url)){
//            clearElements(REDIRECT);
//            setAction(ACTION_SESSION_REDIRECT);
//            addElement(REDIRECT).addText(redir_url);
//        }
//        else{
//            throw new IllegalArgumentException(redir_url + "is not a valid string");
//        }
//    }
//
//    public String getRedirect() {
//        StreamElement   elem = getFirstElement(REDIRECT);
//        return (elem != null? elem.normalizeTrimText(): null);
//    }
    
    public void addContent(String name, String creator){
        if(creator == null || name == null){
            throw new IllegalArgumentException("name and creator are both mandatory for <content>");
        }
        StreamElement elem = addElement("content");
        elem.setAttributeValue("name", name);
        elem.setAttributeValue("creator", creator);
    }

    public void addDescription(String contentName, JingleAudio desc) {
        
        StreamElement content = findContent(contentName);
        if(content == null){
            throw new IllegalArgumentException("Could not find content with name = " + contentName);
            
        }
        content.add(desc);
 
    }

    protected StreamElement findContent(String contentName){
        List l = listElements("content");
        if(l == null || l.size() == 0){
            throw new IllegalArgumentException("No 'content' element found");
        }
        for (ListIterator i = l.listIterator(); i.hasNext(); ){
            StreamElement elem = (StreamElement)i.next();
            String name = elem.getAttributeValue("name");
            if(contentName.equals(name)){
                return elem;
            }
        }
        
        return null;
        
    }
    
    public List getContentList() {
        List l = listElements("content");
        if(l != null && l.size() > 0){
            LinkedList newlist = new LinkedList();
            for(ListIterator i = l.listIterator(); i.hasNext();){
                StreamElement elem = (StreamElement)i.next();
                newlist.add(elem.getAttributeValue("name"));
            }
            return newlist;
        }
        return null;
    }

    public JingleAudio getContentDescription(String contentName) {
        StreamElement elem = findContent(contentName);
        if(elem != null){
            return (JingleAudio) elem.getFirstElement(JingleAudio.class);
        }
        return null;
    }
    
    public static final String [] infotypes;
    static {
        infotypes = new String[4];
        infotypes[0] = "busy";
        infotypes[1] = "hold";
        infotypes[2] = "ringing";
        infotypes[3] = "mute";
    }
    
    public void setInfo(int infotype){
        setAction(ACTION_SESSION_INFO);
        addElement(infotypes[infotype],"http://www.xmpp.org/extensions/xep-0167.html#ns-info");
    }
    
    public int getInfo(){
        if(getAction().equals(ACTION_SESSION_INFO)){
            StreamElement elem = getFirstElement();
            String name = elem.getLocalName();
            if(name.equals("busy")){
                return INFO_BUSY;
            }
            else if(name.equals("hold")){
                return INFO_HOLD;
            }
            else if(name.equals("ringing")){
                return INFO_RINGING;
                
            } else if(name.equals("mute")){
                return INFO_MUTE;
            }
        }
            return -1;
        
    }

    public void addTransport(String contentName, JingleTransport transport) {
        StreamElement elem = findContent(contentName);
        if(elem != null){
            elem.add(transport);
        }
        else
            throw new IllegalArgumentException("No such content: " + contentName);
    }

    public JingleTransport getContentTransport(String contentName) {
        StreamElement elem = findContent(contentName);
        if(elem != null){
            return (JingleTransport) elem.getFirstElement(JingleTransport.class);
        }
        return null;        
    }
}
