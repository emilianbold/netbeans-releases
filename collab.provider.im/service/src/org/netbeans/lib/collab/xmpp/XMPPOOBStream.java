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
import org.netbeans.lib.collab.util.*;
import org.jabberstudio.jso.*;
import org.jabberstudio.jso.x.core.*;
import org.jabberstudio.jso.x.info.OutOfBandExtension;
import java.net.*;
import java.io.*;
import java.util.List;

/**
 *
 *
 * @author Rahul Shah
 *
 */
public class XMPPOOBStream implements StreamingMethod {

    private XMPPSession _session;
    private XMPPContentStream _cs;
    private boolean abort = false;
    private HostPort hp;

    /** Creates a new instance of XMPPOOBStream */
    public XMPPOOBStream(XMPPSession session, XMPPContentStream cs) {
        _session = session;
        _cs = cs;
    }
    
    public void abort() {
        abort = true;
    }
    
    public void process(org.jabberstudio.jso.Packet packet) {
        try {
        if (packet instanceof InfoQuery) {
            if (packet.getType() == InfoQuery.RESULT) {
                _cs.notifyClosed(ContentStreamListener.STATUS_STREAM_OK, "");
                if (hp != null) {
                    Http.stopServ(hp);
                }
            } else if (packet.getType() == Packet.ERROR) {
                _cs.notifyClosed(ContentStreamListener.STATUS_STREAM_DISCONNECTED, "error");
                if (hp != null) {
                    Http.stopServ(hp);
                }
            } else if (packet.getType() == InfoQuery.SET) {
                List list = packet.listExtensions(OutOfBandExtension.IQ_NAMESPACE);
                if (list.size() != 0) {
                    OutOfBandExtension oob = (OutOfBandExtension)list.get(0);
                    URL url = oob.getURL();
                    InfoQuery iq = (InfoQuery)_session.getDataFactory().createPacketNode(XMPPSession.IQ_NAME, InfoQuery.class);
                    iq.setFrom(_session.getCurrentUserJID());
                    iq.setTo(packet.getFrom());
                    iq.setID(packet.getID());

                    try {
                        if (abort) return;
                        InputStream is = url.openStream();
                        _cs.notifyStarted();
                        byte b[] = new byte[1024];
                        int len = 0;
                        while((len = is.read(b)) != -1) {
                            if (abort) return;
                            _cs.write(b, len);
                        }
                        iq.setType(InfoQuery.RESULT);
                        if (abort) return;
                        _session.getConnection().send(iq);
                        _cs.notifyClosed(ContentStreamListener.STATUS_STREAM_OK, "");
                    } catch(IOException io) {
                        io.printStackTrace();
                        iq.setType(InfoQuery.ERROR);
                        _session.getConnection().send(iq);
                        _cs.notifyClosed(ContentStreamListener.STATUS_STREAM_DISCONNECTED, io.toString());
                    }
                }
            } 
        }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    public void start(org.jabberstudio.jso.JID to, String sid, java.io.InputStream is) {
        try {
            StreamDataFactory sdf = _session.getDataFactory();
            InfoQuery iq = (InfoQuery)sdf.createPacketNode(XMPPSession.IQ_NAME, InfoQuery.class);
            OutOfBandExtension oob = (OutOfBandExtension)sdf.createExtensionNode(OutOfBandExtension.IQ_NAME);
            File f = _cs.getFile();
            hp = new HostPort(InetAddress.getLocalHost().getHostAddress(), 0);
            Http.startServ(f.getAbsolutePath(), hp);
            URL url = new URL("http",hp.getHostName(), hp.getPort(), "/" + f.getName());
            oob.setURL(url);
            iq.add(oob);
            iq.setType(InfoQuery.SET);
            iq.setFrom(_session.getCurrentUserJID());
            iq.setTo(to);
            iq.setID(sid);
            if (abort) return;
            _session.getConnection().send(iq);
            _cs.notifyStarted();
        } catch(Exception e) {
            e.printStackTrace();
            _cs.notifyClosed(ContentStreamListener.STATUS_STREAM_ABORTED, e.toString());
        }
    }
}
