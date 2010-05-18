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

import org.jabberstudio.jso.*;
import org.jabberstudio.jso.x.core.*;
import org.jabberstudio.jso.x.si.*;
import org.jabberstudio.jso.x.sift.*;

import org.netbeans.lib.collab.*;
import org.netbeans.lib.collab.util.StringUtility;

import java.io.*;
import java.util.*;
import org.jabberstudio.jso.x.info.OutOfBandExtension;

/**
 *
 *
 * @author Rahul Shah
 *
 */
public class XMPPContentStream implements ContentStream {

    private String[] _methods = {};
    private File _file;
    private ContentStreamListener _listener;
    private XMPPSession _session;
    private XMPPReceiverFileStreamingProfile _profile;
    private long _transferredBytes = 0;
    private InfoQuery _iq;
    private String _sid;
    private JID _to;
    private StreamingMethod _stream;
    private boolean _closed = false;
    private long _lastUpdateTime = -1;
    private boolean _isOOBOnlyStream = false;
    public final static NSI INVALID_STREAM = new NSI("no-valid-streams", SIQuery.NAMESPACE);
    public final static NSI BAD_PROFILE = new NSI("bad-profile", SIQuery.NAMESPACE);
    public static long TIMEOUT = 1000*60*2;
    
    
    /** Creates a new instance of XMPPContentStream */
    public XMPPContentStream(XMPPSession session, ContentStreamListener listener) {
        _session = session;
        _listener = listener;
    }
    
    public XMPPContentStream(XMPPSession session, InfoQuery iq) 
           throws CollaborationException
    {
        _session = session;
        _iq = iq;
        List elements = iq.listElements(SIQuery.NAME);
        if (elements.size() > 0) {
            SIQuery si = (SIQuery)elements.get(0);
            SIProfile profile = si.getProfile();
            if (!(profile instanceof FileTransferProfile)) {
                XMPPSessionProvider.error("Unknown profile for stream initiation");
                reject(METHOD_NOT_SUPPORTED);
                throw new CollaborationException("Unknown profile for stream initiation");
            }
            _sid = si.getID();
            List list = si.listSupportedStreamMethods();
            _methods = new String[list.size()];
            int i = 0;
            for(Iterator itr = list.iterator(); itr.hasNext();) {
                _methods[i++] = (String)itr.next();
            }
        } else if (iq.listElements(OutOfBandExtension.IQ_NAME).size() > 0) {
            _sid = iq.getID();
            _methods = new String[]{StreamingService.OUTBAND_STREAM_METHOD};
            _isOOBOnlyStream = true;
        }
    }

    public void reject(String reason) throws CollaborationException {
        if (_iq == null) {
            throw new IllegalStateException("Cannot reject streams opened by you");
        }
        InfoQuery iq = (InfoQuery)_session.getDataFactory().createPacketNode(XMPPSession.IQ_NAME);
        PacketError error;
        StreamElement element;
        if (!_isOOBOnlyStream) {
            if (METHOD_NOT_SUPPORTED.equals(reason)) {
                error = (PacketError)_session.getDataFactory().
                                                    createPacketError(PacketError.CANCEL, PacketError.BAD_REQUEST_CONDITION);
                element = (StreamElement)_session.getDataFactory().createElementNode(INVALID_STREAM);
                error.add(element);
            } else if (BAD_REQUEST.equals(reason)) {
                error = (PacketError)_session.getDataFactory().
                                                    createPacketError(PacketError.CANCEL, PacketError.BAD_REQUEST_CONDITION);
                element = (StreamElement)_session.getDataFactory().createElementNode(BAD_PROFILE);
                error.add(element);
            } else {
                error = (PacketError)_session.getDataFactory().
                                                    createPacketError(PacketError.CANCEL, PacketError.FORBIDDEN_CONDITION);
                error.setText(reason);
            }
            iq.add(error);
        }
        iq.setID(_iq.getID());
        iq.setFrom(_session.getCurrentUserJID());
        iq.setTo(_iq.getFrom());
        iq.setType(InfoQuery.ERROR);
        try {
            _session.getConnection().send(iq);
        } catch(StreamException se) {
            notifyClosed(ContentStreamListener.STATUS_STREAM_DISCONNECTED, se.toString());
        }
        ((XMPPStreamingService)_session.getStreamingService()).removeContentStream(_sid);
    }
    
    public void accept(String preferredMethod, 
                       ReceiverStreamingProfile profile, 
                       ContentStreamListener listener) 
                       throws CollaborationException
    {
        if (!(profile instanceof XMPPReceiverFileStreamingProfile)) {
            reject(BAD_REQUEST);
            throw new CollaborationException("Profile Not Implemented");
        }
        _profile = (XMPPReceiverFileStreamingProfile)profile;
        _listener = listener;
        if (_iq == null) {
            throw new IllegalStateException("Cannot accept streams opened by you");
        }
        if (!StringUtility.contains(_methods, preferredMethod)) {
            throw new IllegalArgumentException("preferredMethod should be one of the supported methods");
        }
        if (_isOOBOnlyStream) {
            _stream = new XMPPOOBStream(_session, this);
            _stream.process(_iq);
        } else {
            InfoQuery iq = (InfoQuery)_session.getDataFactory().createPacketNode(XMPPSession.IQ_NAME);
            SIQuery si = (SIQuery)_session.getDataFactory().createElementNode(SIQuery.NAME);
            si.setPreferredStreamMethod(preferredMethod);
            //TODO: set other profile details
            iq.add(si);
            iq.setID(_iq.getID());
            iq.setTo(_iq.getFrom());
            iq.setType(InfoQuery.RESULT);
            try {
                _session.getConnection().send(iq);
            } catch(StreamException se) {
                notifyClosed(ContentStreamListener.STATUS_STREAM_DISCONNECTED, se.toString());
            }
            if (StreamingService.INBAND_STREAM_METHOD.equals(preferredMethod)) {
                _stream = new XMPPIBBStream(_session, this);
            } else if (StreamingService.OUTBAND_STREAM_METHOD.equals(preferredMethod)) {
                _stream = new XMPPOOBStream(_session, this);
            }
        }
        _lastUpdateTime = System.currentTimeMillis();
    }
    
    public void abort() throws CollaborationException {
        if (_closed) {
            throw new IllegalStateException("Cannot abort already closed streams");
        }
        if (_stream != null) {
            _stream.abort();
        }
        notifyClosed(ContentStreamListener.STATUS_STREAM_ABORTED, "Stream aborted");
    }
    
    public void timeout() {
        if (_stream != null) {
            _stream.abort();
        }
        notifyClosed(ContentStreamListener.STATUS_STREAM_ABORTED, "Stream Timeout");
    }
    
    public String[] getSupportedMethods() {
        return _methods;
    }
    
    public long getTransferredBytes() {
        return _transferredBytes;
    }
    
    public void notifyStarted() {
        if (_listener != null) {
            _listener.started();
        }
    }
    
    public void notifyClosed(int status, String reason) {
        if (_profile != null) _profile.close();
        if (_listener != null) {
            _listener.closed(status, reason);
        }
        try {
            ((XMPPStreamingService)_session.getStreamingService()).removeContentStream(_sid);
        } catch(CollaborationException e) {
            XMPPSessionProvider.error(e.toString(),e);
        }
        _closed = true;
    }
    
    public void setSessionID(String id) {
        _sid = id;
    }
    
    public void setTo(JID to) {
        _to = to;
    }
    
    public File getFile() {
        return _file;
    }
    
    public void setFile(File file) {
        _file = file;
    }

    public void start(String method) {
        if (StreamingService.INBAND_STREAM_METHOD.equals(method)) {
            _stream = new XMPPIBBStream(_session, this);
            try {
                _stream.start(_to, _sid, new FileInputStream(_file));
            } catch(FileNotFoundException fnfe) {
                //this should not happen as we do check for existence.
                fnfe.printStackTrace();
            }
        } else if (StreamingService.OUTBAND_STREAM_METHOD.equals(method)) {
            _stream = new XMPPOOBStream(_session, this);
            _stream.start(_to,  _sid, null);
        }
    }
    
    public void process(Packet packet) {
        if (_stream != null) {
            _stream.process(packet);
        }
    }
    
    public void write(byte[] b, int len) {
        _lastUpdateTime = System.currentTimeMillis();
        _profile.write(b, len);
        incrementTransferredBytes(len);
    }
    
    public void incrementTransferredBytes(int i) {
        _transferredBytes += i;
    }
    
    public boolean isTimedOut() {
        if (_lastUpdateTime == -1) return false;
        if ((System.currentTimeMillis() - _lastUpdateTime) > TIMEOUT) {
            return true;
        }
        return false;
    }
}
