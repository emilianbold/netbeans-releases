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

import org.jabberstudio.jso.Packet;
import org.jabberstudio.jso.Stream;
import org.jabberstudio.jso.StreamDataFactory;
import org.jabberstudio.jso.StreamException;
import org.jabberstudio.jso.io.StreamSource;
import org.jabberstudio.jso.tls.StartTLSPacket;
import org.netbeans.lib.collab.CollaborationException;
import org.saxpath.SAXPathException;

/**
 * Implementation which abstracts out the actual starttls session setup.
 *
 * @author Mridul Muralidharan
 */
public class StartTLSHandler {

    private StreamDataFactory _sdf;
    private Stream _connection;
    private StreamSourceCreator _streamSrcCreator;
    private StreamSource _css;
    
    public static final int TLS_START_TIME_MS = 10000;
    
    /** Creates a new instance of StartTLSHandler */
    public StartTLSHandler(StreamDataFactory _sdf , Stream _connection , 
            StreamSourceCreator _streamSrcCreator , StreamSource _css) {
        this._sdf = _sdf;
        this._connection = _connection;
        this._streamSrcCreator = _streamSrcCreator;
        this._css = _css;
    }
    
    
    public void process() throws CollaborationException{
        PacketWatcher _watcher = null;
        
        try{
            try{
                _watcher = new PacketWatcher(_sdf , "tls:*" , "tls", StartTLSPacket.NAMESPACE);
            }catch(SAXPathException saxpEx){
                throw new CollaborationException("tls error" , saxpEx);
            }
            
            final PacketWatcher watcher = _watcher;

            PacketHandler handler = new PacketHandler(){
                public void preProcess(){
                    watcher.notify();
                }
                public int process(Packet packet) throws Exception{
                    _streamSrcCreator.upgradeToTLS(_css);
                    return SUCCESS;
                }
                public void postProcess(){
                    watcher.notify();
                }
            };
            watcher.setStream(_connection);
            watcher.setHandler(handler);
            watcher.init();

            StartTLSPacket packet= 
                    (StartTLSPacket)_sdf.createPacketNode(
                        StartTLSPacket.NAME_STARTTLS, StartTLSPacket.class);

            try{
                _connection.send(packet);
            }catch(StreamException stEx){
                throw new CollaborationException("tls error" , stEx);
            }

            while (!watcher.isStarted()){
                try{
                    _connection.process();
                }catch(StreamException stEx){
                    throw new CollaborationException("tls error" , stEx);
                }
                synchronized(watcher){
                    try{
                        watcher.wait(10);
                    }catch(InterruptedException iEx){}
                }

                if (_connection.getCurrentStatus() == _connection.CLOSED ||
                        (watcher.exceedsTimeout(TLS_START_TIME_MS) && !watcher.isStarted())){
                    throw new CollaborationException("tls error");
                }
            }

            assert (watcher.isStarted());
            XMPPSessionProvider.debug("TLS process started");

            // This is not necessary since as of now , the processing happens
            // within the same thread - but why make unnecessary impl related
            // assumptions and tightly couple our code with jso impl details.
            synchronized(watcher){
                while (!watcher.isCompleted() &&
                        !watcher.exceedsTimeout(TLS_START_TIME_MS)){
                    try{
                        watcher.wait(TLS_START_TIME_MS);
                    }catch(InterruptedException iEx){}
                }
            }
            
            XMPPSessionProvider.debug("TLS process complete");

            if (null != watcher.getThrowable()){
                throw new CollaborationException("tls error" , watcher.getThrowable());
            }
            if (!watcher.isCompleted() || PacketHandler.SUCCESS != watcher.getStatus()){
                throw new CollaborationException("tls error");
            }
        }
        finally{
            if (null != _watcher){
                _watcher.release();
            }
        }
        
    }
    
}
