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
import org.netbeans.lib.collab.util.StringUtility;

import org.jabberstudio.jso.*;
import org.jabberstudio.jso.x.core.*;
import org.jabberstudio.jso.x.si.*;
import org.jabberstudio.jso.x.sift.*;
import org.jabberstudio.jso.x.info.OutOfBandExtension;
import org.jabberstudio.jso.x.disco.DiscoInfoQuery;

import java.util.*;
import java.io.FileInputStream;
import java.io.File;

/**
 *
 *
 * @author Rahul Shah
 *
 */
public class XMPPStreamingService implements StreamingService {

    List _streamingServiceListeners = Collections.synchronizedList(new ArrayList());
    XMPPSession __session;
    StreamDataFactory _sdf;
    Hashtable _activeStreams = new Hashtable();
    String[] _availableMethods;
    public static long CLEANUP_PERIOD = 1000*60*2;

    /** Creates a new instance of XMPPStreamingService */
    public XMPPStreamingService(XMPPSession session) {
        __session = session;
        _sdf = __session.getDataFactory();
        Timer timer = new Timer(true);
        timer.schedule(new TimerTask() {
            public void run() {
                cleanup();
            }
        }, 0, CLEANUP_PERIOD);
    }

    public void initialize(StreamingServiceListener listener)
                throws CollaborationException
    {
        addStreamingServiceListener(listener);

        __session.getApplicationInfo().addFeature(SIQuery.NAMESPACE);
        __session.getApplicationInfo().addFeature(FileTransferProfile.NAMESPACE);
    }

    public ContentStream open(String rcpt, String[] methods,
                              SenderStreamingProfile profile,
                              ContentStreamListener listener)
                              throws CollaborationException
    {
        if (profile instanceof SenderFileStreamingProfile) {
            SenderFileStreamingProfile fileProfile = (SenderFileStreamingProfile)profile;
            File file = fileProfile.getFile();
            JID recipient = new JID(rcpt);
            if (!recipient.hasResource()){
                // Sending to bare jid - NOT SUPPORTED.
                throw new CollaborationException("Sending to bare jid is not supported");
            }
            Set features;
            try {
                DiscoInfoQuery diq = __session.sendInfoQuery(recipient, null);
                features = diq.getFeatures();
            } catch(CollaborationException e) {
                features = null;//the recipient might not be supporting disco#info
		//e.printStackTrace();
            }
            String sid;
	    //true when rcpt does not support si and one of the supported methods has oob
            boolean useOOBOnly = false;
            if (features == null ||
                !features.contains(SIQuery.NAMESPACE))
            {
                if (StringUtility.contains(methods, OUTBAND_STREAM_METHOD)) {
                    useOOBOnly = true;
                    sid = __session.nextID("oob");
                } else {
                    throw new ServiceUnavailableException("Recipient does not support streaming");
                }
            } else {
                InfoQuery iq = (InfoQuery)_sdf.createPacketNode(XMPPSession.IQ_NAME);
                SIQuery si = (SIQuery)_sdf.createElementNode(SIQuery.NAME);
                si.setMimeType(fileProfile.getMimeType());
                sid = __session.nextID("sid");
                si.setID(sid);
                FileTransferProfile ftp = (FileTransferProfile)_sdf.createElementNode(FileTransferProfile.NAME);
                ftp.setName(file.getName());
                ftp.setSize(file.length());
                ftp.setDescription(fileProfile.getDescription());
                ftp.setHash(fileProfile.getHash());
                si.setProfile(ftp);
                for(int i = 0; i < methods.length; i++) {
                    si.addSupportedStreamMethod(methods[i]);
                }

                iq.add(si);
                iq.setID(sid);
                iq.setTo(recipient);
                iq.setType(InfoQuery.SET);

                try {
                    __session.getConnection().send(iq);
                } catch (StreamException se) {
                    XMPPSessionProvider.error(se.toString(),se);
                    throw new CollaborationException(se);
                }
                _availableMethods = methods;
            }

            XMPPContentStream cs;
            try {
                cs = new XMPPContentStream(__session, listener);
                cs.setSessionID(sid);
                cs.setTo(recipient);
                cs.setFile(file);
                _activeStreams.put(sid, cs);
                if (useOOBOnly) {
                    cs.start(OUTBAND_STREAM_METHOD);
                }
            } catch(Exception e) {
                XMPPSessionProvider.error(e.toString(),e);
                throw new CollaborationException(e);
            }
            return cs;
        } else {
            throw new CollaborationException("Not Implemented");
        }
    }

    public void processSIPackets(InfoQuery iq) {
        try {
            XMPPContentStream cs = (XMPPContentStream)_activeStreams.get(iq.getID());
            if (cs != null) {
                if (Packet.ERROR.equals(iq.getType())) {
                    PacketError error = iq.getError();
                    if (error == null) {
                        cs.notifyClosed(ContentStreamListener.STATUS_STREAM_DISCONNECTED, "Unknown reason");
                    } else if (error.getType() == PacketError.CANCEL) {
                        if (null != _availableMethods &&
					StringUtility.contains(_availableMethods, OUTBAND_STREAM_METHOD)) {
                            //if oob is one of the option then try it once - temporary fix for exodus.
                            cs.start(OUTBAND_STREAM_METHOD);
                        } else if (error.listElements(XMPPContentStream.BAD_PROFILE).size() > 0) {
                            cs.notifyClosed(ContentStreamListener.STATUS_STREAM_REJECTED, ContentStream.BAD_REQUEST);
                        } else if (error.listElements(XMPPContentStream.INVALID_STREAM).size() > 0) {
                            cs.notifyClosed(ContentStreamListener.STATUS_STREAM_REJECTED, ContentStream.METHOD_NOT_SUPPORTED);
                        } else if (PacketError.FORBIDDEN_CONDITION.equals(error.getDefinedCondition())) {
                            cs.notifyClosed(ContentStreamListener.STATUS_STREAM_REJECTED, error.getText());
                        } else {
                            cs.notifyClosed(ContentStreamListener.STATUS_STREAM_REJECTED, "Stream Rejected. Unknown reason");
                        }
                    }
                } else if (InfoQuery.RESULT.equals(iq.getType())) {
                    //success
                    List elements = iq.listElements(SIQuery.NAME);
                    if (elements.size() == 0) {
                       cs.notifyClosed(ContentStreamListener.STATUS_STREAM_DISCONNECTED, "Unknown reason");
                       return;
                    }
                    SIQuery si = (SIQuery)elements.get(0);
                    //start the transfers based on the preferred mode
                    cs.start(si.getPreferredStreamMethod());
                }
            } else {
                //create the ContentStream object and call the listener
                cs = new XMPPContentStream(__session, iq);
                JID sender = iq.getFrom();
                List elements = iq.listElements(SIQuery.NAME);
                String sid;
                if (elements.size() == 0) {
                   if (iq.listElements(OutOfBandExtension.IQ_NAME).size() == 0) {
                       cs.notifyClosed(ContentStreamListener.STATUS_STREAM_DISCONNECTED, "Unknown reason");
                       return;
                   } else {
                       sid = iq.getID();
                   }
                } else {
                    SIQuery si = (SIQuery)elements.get(0);
                    sid = si.getID();
                }
                _activeStreams.put(sid, cs);
                ReceiverStreamingProfile profile = new XMPPReceiverFileStreamingProfile(cs, iq);
                _fireStreamingServiceListeners(sender == null? null: 
                    //sender.toBareJID().toString()
                    sender.toString()
                    , profile, cs);
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public XMPPContentStream getContentStream(String sid) {
        if (sid == null) return null;
        return (XMPPContentStream)_activeStreams.get(sid);
    }

    public XMPPContentStream removeContentStream(String sid) {
        if (sid == null) return null;
        return (XMPPContentStream)_activeStreams.remove(sid);
    }

    public boolean isStreamingPacket(Packet packet) {
        if (packet.listElements(SIQuery.NAME).size() > 0) {
            return true;
        }
        if (packet.getType() == Packet.ERROR &&
            _activeStreams.containsKey(packet.getID()))
        {
            return true;
        }
        return false;
    }

    public void cleanup() {
        for(Iterator itr = _activeStreams.entrySet().iterator(); itr.hasNext();) {
            Map.Entry entry = (Map.Entry)itr.next();
            XMPPContentStream cs = (XMPPContentStream)entry.getValue();
            if (cs.isTimedOut()) {
                String sid = (String)entry.getKey();
                XMPPSessionProvider.debug("Removing timedout content stream sid - " + sid);
                _activeStreams.remove(sid);
                cs.timeout();
            }
        }
    }

    private void _fireStreamingServiceListeners(String sender, ReceiverStreamingProfile profile, ContentStream stream) {
        __session.addWorkerRunnable(new StreamingServiceNotifier(sender, profile, stream));
    }

    public void addStreamingServiceListener(StreamingServiceListener listener) {
        if (!_streamingServiceListeners.contains(listener))
            _streamingServiceListeners.add(listener);
    }
    
    public void removeStreamingServiceListener(StreamingServiceListener listener) {
        _streamingServiceListeners.remove(listener);
    }
    
    private class StreamingServiceNotifier implements Runnable {
        String sender;
        ReceiverStreamingProfile profile;
        ContentStream stream;
        
        StreamingServiceNotifier(String sender, ReceiverStreamingProfile profile, ContentStream stream) {
            this.sender = sender;
            this.profile = profile;
            this.stream = stream;
        }
        
        public void run() {
            synchronized(_streamingServiceListeners) {
                for(Iterator itr = _streamingServiceListeners.iterator(); itr.hasNext();) {
                    try {
                        StreamingServiceListener l = (StreamingServiceListener)itr.next();
                        if (l == null) continue;
                        l.onContentStream(sender, profile, stream);
                    } catch(Exception e) {
                        XMPPSessionProvider.error(e.toString(),e);
                    }
                }
            }
        }
    }
}

