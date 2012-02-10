/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.inspect;

import java.awt.EventQueue;
import java.nio.channels.SelectionKey;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.web.common.websocket.WebSocketReadHandler;
import org.netbeans.modules.web.common.websocket.WebSocketServer;
import org.openide.util.RequestProcessor;
import org.openide.windows.WindowManager;

/**
 * Page inspection-related handler for the WebSocket server.
 *
 * @author Jan Stola
 */
public class WSHandler implements WebSocketReadHandler {
    /** Logger used by this class. */
    private static final Logger LOG = Logger.getLogger(WSHandler.class.getName());
    /** WebSocket server this handler corresponds to. */
    private WebSocketServer server;
    /** Key corresponding to the current executor. */
    private SelectionKey currentKey;
    /** Current script executor. */
    private RemoteScriptExecutor currentExecutor;

    /**
     * Creates a new {@code WSHandler} for the specified server.
     * 
     * @param server WebSocket server this handler corresponds to.
     */
    WSHandler(WebSocketServer server) {
        this.server = server;
    }

    @Override
    public void read(SelectionKey key, byte[] data, Integer dataType) {
        String message = new String(data, Charset.forName(WebSocketServer.UTF_8));
        LOG.log(Level.FINEST, "Received: {0}", message); // NOI18N
        if (key.equals(currentKey)) {
            currentExecutor.messageReceived(message);
        } else {
            Logger.getLogger(WSHandler.class.getName()).log(Level.INFO,
                "Ignoring message: {0} from {1}.", new Object[]{message, key});
        }
    }

    /**
     * Updates {@code PageModel} by making the current executor its executor.
     */
    void updateModel() {
        final PageModel pageModel = PageModel.getDefault();
        if (pageModel instanceof PageModelImpl) {
            final ScriptExecutor executor = currentExecutor;
            RequestProcessor.getDefault().post(new Runnable() {
                @Override
                public void run() {
                    ((PageModelImpl)pageModel).setExecutor(executor);
                }
            });
        }        
    }

    @Override
    public void accepted(final SelectionKey key) {
        LOG.log(Level.FINE, "Accepted: {0}", key); // NOI18N
        currentKey = key;
        if (currentExecutor != null) {
            currentExecutor.dispose();
        }
        currentExecutor = new RemoteScriptExecutor(new RemoteScriptExecutor.PostBox() {
            @Override
            public void postMessage(String message) {
                if (key.isValid()) {
                    LOG.log(Level.FINEST, "Sending: {0}", message); // NOI18N
                    server.sendMessage(key, message);
                } else {
                    keyBecomeInvalid(key);
                }
            }
        });
        // Switch to NetBeans when user requests inspection in NetBeans
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                WindowManager.getDefault().getMainWindow().toFront();
            }
        });
        updateModel();
    }

    @Override
    public void closed(SelectionKey key) {
        LOG.log(Level.FINE, "Closed: {0}", key); // NOI18N
        keyBecomeInvalid(key);
    }

    /**
     * Invoked when some selection key of the WebSocket server becomes invalid.
     * 
     * @param key selection key that becomes invalid.
     */
    void keyBecomeInvalid(SelectionKey key) {
        if (key.equals(currentKey)) {
            currentExecutor.dispose();
            currentExecutor = null;
            currentKey = null;
            updateModel();
        }
    }

}
