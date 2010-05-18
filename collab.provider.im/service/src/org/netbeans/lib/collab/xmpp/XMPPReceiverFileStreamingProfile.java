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
import java.io.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.net.URL;

import org.jabberstudio.jso.*;
import org.jabberstudio.jso.x.core.*;
import org.jabberstudio.jso.x.si.*;
import org.jabberstudio.jso.x.sift.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.jabberstudio.jso.x.info.OutOfBandExtension;

/**
 *
 *
 * @author Rahul Shah
 *
 */
public class XMPPReceiverFileStreamingProfile implements ReceiverFileStreamingProfile {
    //list of output streams where data needs to be written.
    List _outputstreams = new ArrayList();
    String _name;
    byte[] _senderHash;
    String _desc;
    String _mimeType;
    long _lastModified;
    long _size = -1;
    long _offset = 0;
    long _length = -1;
    MessageDigest md5;
    byte[] _receiverHash;
    
    /** Creates a new instance of XMPPReceiverFileStreamingProfile */
    public XMPPReceiverFileStreamingProfile(ContentStream cs, InfoQuery iq) 
                                            throws CollaborationException 
    {
        List elements = iq.listElements(SIQuery.NAME);
        if (elements.size() > 0) {
            SIQuery si = (SIQuery)elements.get(0);
            _mimeType = si.getMimeType();
            SIProfile profile = si.getProfile();
            if (!(profile instanceof FileTransferProfile)) {
                XMPPSessionProvider.info("Unknown profile for stream initiation");
                cs.reject(ContentStream.METHOD_NOT_SUPPORTED);
                throw new CollaborationException("Unknown profile for stream initiation");
            }
            _name = ((FileTransferProfile)profile).getName();
            _desc = ((FileTransferProfile)profile).getDescription();
            _size = ((FileTransferProfile)profile).getSize();
            _senderHash = ((FileTransferProfile)profile).getHash();
             if (_senderHash != null && _senderHash.length > 0) {
                 try {
                     md5 = MessageDigest.getInstance("MD5");
                     md5.reset();
                  } catch(NoSuchAlgorithmException nsae) {
                     nsae.printStackTrace();
                  }
             }
        } else if ((elements = iq.listElements(OutOfBandExtension.IQ_NAME)).size() > 0) {
            OutOfBandExtension oob = (OutOfBandExtension)elements.get(0);
            URL url = oob.getURL();
            _name = url.getPath();
            int i = _name.lastIndexOf('/');
            if (i != -1) _name = _name.substring(i);
            _desc = oob.getDescription();
        }
        //_sid = si.getID();
    }
    
    public void addOutput(java.io.OutputStream os) {
        if (os != null) _outputstreams.add(os);
    }
    
    public void addOutput(java.io.File f) {
        if (f.isDirectory()) {
            f = new File(f, _name);
        }
        try {
            f.createNewFile();
            addOutput(new FileOutputStream(f, true));
        } catch(Exception e) {
        }
    }
    
    public int checkIntegrity() {
        if (_senderHash == null || _senderHash.length == 0) {
            return INTEGRITY_UNKNOWN;
        }
        if (_receiverHash == null || _receiverHash.length == 0) {
            throw new IllegalStateException();
        }
        if (md5.isEqual(_senderHash, _receiverHash)) {
            return INTEGRITY_OK;
        } else {
            return INTEGRITY_COMPROMISED;
        }
    }
    
    public String getDescription() {
        return _desc;
    }
    
    public byte[] getHash() {
        return _senderHash;
    }
    
    public long getLastModified() {
        return _lastModified;
    }
    
    public long getLength() {
        if (_length != -1) {
            return _length;
        }
        return size();
    }
    
    public String getMimeType() {
        return _mimeType;
    }
    
    public String getName() {
        return _name;
    }
    
    public long getOffset() {
        return _offset;
    }
    
    public void setLength(long length) {
        _length = length;
    }
    
    public void setOffset(long offset) {
        _offset = offset;
    }
    
    public long size() {
        return _size;
    }
    
    public void write(byte[] b, int len) 
    {
        try {
            for(Iterator itr = _outputstreams.iterator(); itr.hasNext();) {
                ((OutputStream)itr.next()).write(b, 0, len);
            }
            if (_senderHash != null && _senderHash.length > 0) {
                md5.update(b, 0, len);
            }
        } catch(IOException ioe) {
            ioe.printStackTrace();
        }
    }
    
    public void close() {
        for(Iterator itr = _outputstreams.iterator(); itr.hasNext();) {
            try {
                ((OutputStream)itr.next()).close();
            } catch(IOException ioe) {
                ioe.printStackTrace();
            }
        }
        if (_senderHash != null && _senderHash.length > 0) {
            _receiverHash = md5.digest();
        }
    }
}
