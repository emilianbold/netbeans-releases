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

package org.netbeans.lib.collab.xmpp.jingle;

import java.util.List;
import org.jabberstudio.jso.Packet;
import org.netbeans.lib.collab.CollaborationException;
import org.netbeans.lib.collab.P2PAudioService;
import org.netbeans.lib.collab.P2PService;
import org.netbeans.lib.collab.P2PServiceBase;
import org.netbeans.lib.collab.xmpp.XMPPSession;
import org.netbeans.lib.collab.xmpp.jso.iface.x.jingle.Jingle;

/**
 *
 * @author jerry
 */
public class JingleService implements P2PService{

    XMPPSession _session;
    /**
     * Creates a new instance of JingleService
     */
    public JingleService(XMPPSession sess) {
        _session = sess;
    }

    JingleAudioService _audio;
    public P2PAudioService getAudioService() {
        if(_audio == null){
            _audio = new JingleAudioService(this, _session);
        }
        return _audio;
    }

    public P2PServiceBase getAVService() {
        throw new UnsupportedOperationException("P2P Audio/Video not yet implemented");
    }

    public P2PServiceBase getFileTransferService() {
        throw new UnsupportedOperationException("P2P FileTransfer not yet implemented");
    }

    public boolean isJinglePacket(Packet packet) {
        System.out.println("Checking if jingle packet");
        List l = packet.listExtensions(Jingle.class);
        if(l != null && l.size() > 0 ){
            System.out.println("Found jingle packet");
            return true;
        }
        if(_audio.isJingleAudioPacket(packet)){
            return true;
        }
        System.out.println("Not a jingle packet");
        return false;
    }

    public void processPacket(Packet packet)  throws CollaborationException{
        // For now, we only have audio packets
        if(_audio != null){
            _audio.processPacket(packet);
        }
    }
    
}
