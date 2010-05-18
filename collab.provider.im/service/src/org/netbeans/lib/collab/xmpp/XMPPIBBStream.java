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

import org.netbeans.lib.collab.*;
import org.jabberstudio.jso.*;
import java.io.*;
import java.util.List;
import java.util.Hashtable;

/**
 *
 *
 * @author Rahul Shah
 *
 */
public class XMPPIBBStream implements StreamingMethod {

    XMPPSession _session;
    XMPPContentStream _cs;
    int expectedSeqId = 0;
    Hashtable _outOfOrderData = new Hashtable();
    boolean abort = false;

    /** Creates a new instance of XMPPIBBStream */
    public XMPPIBBStream(XMPPSession session, XMPPContentStream cs) {
        _session = session;
        _cs = cs;
    }
    
    public void start(JID to, String sid, InputStream is) {
        XMPPSessionProvider.debug("starting IBB data transfer" + sid);
        InfoQuery iq = _session.createIBBInfoQuery(to, sid, true);
        try {
            if (abort) return;
            iq = (InfoQuery)_session.sendAndWatch(iq, 
                    _session.getRequestTimeout());
            if (iq == null) {
                _cs.notifyClosed(ContentStreamListener.STATUS_STREAM_ABORTED, "Timeout while waiting for reply to IBB open request");
                return;
            }
            if (iq.getType() == Packet.ERROR) {
                _cs.notifyClosed(ContentStreamListener.STATUS_STREAM_ABORTED, "Failed to open IBB Stream");
                return;
            }
            _cs.notifyStarted();
            Extension data = _session.getDataFactory().createExtensionNode(XMPPSession.IBB_DATA);
            data.setAttributeValue("sid",sid);
            org.jabberstudio.jso.Message newMsg;
            int seqId = 0;
            newMsg = (org.jabberstudio.jso.Message)_session.getDataFactory().createPacketNode(
                                            XMPPSession.MESSAGE_NAME, org.jabberstudio.jso.Message.class);                
            newMsg.setFrom(_session.getCurrentUserJID());
            newMsg.setTo(to);
            newMsg.setID(_session.nextID("message"));
            newMsg.add(data);
            StringBuffer buf = new StringBuffer();
            // Why (XMPPSession.IBB_MESSAGE_SIZE / 4) * 3?
            // base64 encoding takes scoops of 3 bytes at a time so the 
            // size of each chunk to encode needs to be a multiple of 3
            // otherwise the chunk-by-chunk encoding does not work
            int scoopSize = (XMPPSession.IBB_MESSAGE_SIZE / 4) * 3;
            byte[] b = new byte[scoopSize];
            int i = 0;
            int len = 0;
            String tempData = null;
            int maxsize = XMPPSession.IBB_MESSAGE_SIZE;
            while(len != -1) {
                len = is.read(b);
                if (len == -1) {
                    if ((maxsize = buf.length()) == 0) break;
                } else {
                    if(len ==  scoopSize && (buf.length() == 0)) {
                        tempData = XMPPSession.BASE64.encode(b);
                    } else {
                        buf.append(XMPPSession.BASE64.encode(b, 0, len));
                    }
                }
                 
                if (buf.length() >= maxsize) {
                    tempData = buf.substring(0, maxsize);
                    buf.replace(0, maxsize, "");
                }
                
                if (tempData != null) {
                    data.setAttributeValue("seq",Integer.toString(i++));
                    data.clearText();
                    data.addText(tempData);
                    tempData  = null;
                    if (abort) return;
                    _session.getConnection().send(newMsg);
                }
                _cs.incrementTransferredBytes(len);
            }
            //send the close packet
            iq = _session.createIBBInfoQuery(to, sid, false);
            if (abort) return;
            _session.getConnection().send(iq);
            _cs.notifyClosed(ContentStreamListener.STATUS_STREAM_OK, null);
        } catch(Exception e) {
            e.printStackTrace();
            _cs.notifyClosed(ContentStreamListener.STATUS_STREAM_DISCONNECTED, e.toString());
        }
    }
    
    public void process(Packet packet) {
        if (packet instanceof InfoQuery) {
            List elements = packet.listElements("open", XMPPSession.IBB_NAMESPACE);
            if (elements.size() > 0) {
                processIBBInfoQuery(packet, true);
            } else {
                elements = packet.listElements("close", XMPPSession.IBB_NAMESPACE);
                if (elements.size() > 0) {
                    processIBBInfoQuery(packet, false);
                }
            }
        } else if (packet instanceof org.jabberstudio.jso.Message) {
            processMessage(packet);
        }
                
    }
    
    public void processIBBInfoQuery(Packet packet, boolean open) {
        try {
            Packet p = _session.getDataFactory().createPacketNode(packet.getNSI());
            p.setID(packet.getID());
            p.setType(InfoQuery.RESULT);
            p.setFrom(_session.getCurrentUserJID());
            p.setTo(packet.getFrom());
            if (abort) return;
            _session.getConnection().send(p);
            if (open) {
                _cs.notifyStarted();
            } else {
                _cs.notifyClosed(ContentStreamListener.STATUS_STREAM_OK, null);
            }
        } catch(Exception e) {
            e.printStackTrace();
            _cs.notifyClosed(ContentStreamListener.STATUS_STREAM_DISCONNECTED, e.toString());
        }
    }
    
    public void processMessage(Packet packet) {
        try {
        List elements = packet.listElements("data", XMPPSession.IBB_NAMESPACE);
        if (elements.size() > 0) {
            StreamElement ibb = (StreamElement)elements.get(0);
            String content = ibb.normalizeText();
            //XMPPSessionProvider.debug(content);
            byte b[] = XMPPSession.BASE64.decode(content);
            String seq = ibb.getAttributeValue("seq");
            try {
                int seqId = Integer.parseInt(seq);
                if (seqId != expectedSeqId) {
                    //TODO: Add a limit on the size of out of order packets
                    //Strictly speaking according to JEP the stream should be aborted
                    //when a out of order packet is received but it will be difficult to impose
                    // the order on server so we are deviating from the JEP.
                    _outOfOrderData.put(seq, b);
                    return;
                }
            } catch(Exception e) {
                e.printStackTrace();
            }
            if (abort) return;
            _cs.write(b, b.length);
            expectedSeqId++;
            while(_outOfOrderData.containsKey(Integer.toString(expectedSeqId))) {
                if (abort) return;
                b = (byte[])_outOfOrderData.remove(Integer.toString(expectedSeqId));
                _cs.write(b , b.length);
                expectedSeqId++;
            }
        }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    public void abort() {
        abort = true;
    }
    
}

