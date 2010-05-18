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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Properties;
import net.outer_planes.jso.ElementNode;
import org.jabberstudio.jso.StreamDataFactory;
import org.jabberstudio.jso.StreamElement;
import org.jabberstudio.jso.StreamObject;
import org.netbeans.lib.collab.xmpp.jso.iface.x.jingle.JingleAudio;

/**
 *
 * @author jerry
 */
public class JingleAudioImpl extends ElementNode implements JingleAudio {
    
    /**
     * Creates a new instance of JingleAudioImpl
     */
    
    public static final String PAYLOAD_TYPE = "payload-type";
    public static final String ID = "id";
    public static final String PAYLOAD_NAME = "name";
    public static final String CLOCKRATE = "clockrate";
    public static final String CHANNELS = "channels";
    public static final String MAXPTIME = "maxptime"; // Not implemented
    public static final String PTIME = "ptime"; // Not implemented
    
    public JingleAudioImpl(StreamDataFactory sdf) {
        super(sdf, NAME);
    }
    public JingleAudioImpl(StreamElement parent, JingleAudio child){
        super(parent, (ElementNode)child);
    }
    public StreamObject copy(StreamElement parent){
        return new JingleAudioImpl(parent, this);
    }
    
    public void addPayload(int payload_id, String name, int channels, double clock, 
            Properties properties) {
        StreamElement p;
        p = findPayloadByID(payload_id);
        if(p == null){
            // Not found, create it
            p = addElement(PAYLOAD_TYPE);
        }
        p.setAttributeValue(ID, String.valueOf(payload_id));
        p.setAttributeValue(PAYLOAD_NAME, name);
        p.setAttributeValue(CHANNELS, String.valueOf(channels));
        p.setAttributeValue(CLOCKRATE, String.valueOf(clock));
    }
    
    protected StreamElement findPayloadByID(int id){
        String sp_id = String.valueOf(id);
        List payloads = listElements(PAYLOAD_TYPE);
        StreamElement p;
        for(Iterator i = payloads.iterator(); i.hasNext(); ){
            p = (StreamElement)i.next();
            if(p.getAttributeValue(ID).equals(sp_id)){
                return p;
            }
        }
        return null;
    }
    
    public void removePayload(int payload_id) {
        StreamElement p = findPayloadByID(payload_id);
        if(p != null){
            remove(p);
        }
    }

    
    public String getPayloadName(int payload_id) {
        StreamElement elem = findPayloadByID(payload_id);
        if(elem != null){
            String name = elem.getAttributeValue(PAYLOAD_NAME);
            return name;
        }
        return null;

    }

    public int getPayloadChannels(int payload_id) {
        StreamElement elem = findPayloadByID(payload_id);
        if(elem != null){
            String str_chan = elem.getAttributeValue(CHANNELS);
            return Integer.parseInt(str_chan);
        }
        
        return 0;
    }

    public double getPayloadClock(int payload_id) {
        StreamElement elem = findPayloadByID(payload_id);
        if(elem != null){
            String str_clock = elem.getAttributeValue(CLOCKRATE);
            return Float.parseFloat(str_clock);
        }

        return 0;
    }

    public Properties getPayloadProperties(int payload_id) {
        throw new UnsupportedOperationException();
       // return null;
    }

    public List listPayloads() {
        List payloads = listElements(PAYLOAD_TYPE);
        ArrayList newlist = new ArrayList(payloads.size());
        for(ListIterator i=payloads.listIterator(); i.hasNext(); ){
            StreamElement elem = (StreamElement)i.next();
            newlist.add(elem.getAttributeValue(ID));
        }
        return newlist;
    }
}
