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
