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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.openide.util.RequestProcessor;

/**
 * Script executor for an external browser.
 *
 * @author Jan Stola
 */
public class RemoteScriptExecutor implements ScriptExecutor {
    /** Logger for this class. */
    private static final Logger LOG = Logger.getLogger(RemoteScriptExecutor.class.getName());
    // Message attributes
    private static final String MESSAGE_TYPE = "message"; // NOI18N
    private static final String MESSAGE_EVAL = "eval"; // NOI18N
    private static final String MESSAGE_SELECTION = "selection"; // NOI18N
    private static final String MESSAGE_ID = "id"; // NOI18N
    private static final String MESSAGE_SCRIPT = "script"; // NOI18N
    private static final String MESSAGE_STATUS = "status"; // NOI18N
    private static final String MESSAGE_STATUS_OK = "ok"; // NOI18N
    private static final String MESSAGE_RESULT = "result"; // NOI18N
    /** "post box" of the browser plugin. */
    private PostBox postBox;
    /** ID of the last message sent to the browser plugin. */
    private int lastIDSent = 0;
    /** ID of the last message received from the browser plugin. */
    private int lastIDReceived = 0;
    /** Lock guarding access to the modifiable state of the executor. */
    private final Object LOCK = new Object();
    /** Results of the executed scripts. It maps message ID to the result. */
    private Map<Integer,Object> results = new HashMap<Integer,Object>();

    /**
     * Creates a new {@code RemoteScriptExecutor}.
     * 
     * @param postBox "post box" of the browser plugin.
     */
    RemoteScriptExecutor(PostBox postBox) {
        this.postBox = postBox;
    }

    @Override
    public Object execute(String script) {
        synchronized (LOCK) {
            if (isDisposed()) {
                return ERROR_RESULT;
            }
            int id = ++lastIDSent;
            JSONObject message = new JSONObject();
            try {
                message.put(MESSAGE_TYPE, MESSAGE_EVAL);
                message.put(MESSAGE_ID, id);
                message.put(MESSAGE_SCRIPT, script);
                postBox.postMessage(message.toString());
            } catch (JSONException ex) {
                // Cannot happen
            }
            try {
                do {
                    LOCK.wait();
                } while (!results.containsKey(id));
            } catch (InterruptedException iex) {
                LOG.log(Level.INFO, null, iex);
            }
            return results.remove(id);
        }
    }

    /**
     * Disposes this executor. All pending results are set to {@code ERROR_RESULT}.
     */
    void dispose() {
        synchronized (LOCK) {
            if (lastIDReceived < lastIDSent) {
                int fromID = lastIDReceived+1;
                LOG.log(Level.INFO, "Executor disposed before responses with IDs {0} to {1} were received!", // NOI18N
                        new Object[]{fromID, lastIDSent});
                for (int i=fromID; i<=lastIDSent; i++) {
                    results.put(i, ERROR_RESULT);
                }
                lastIDReceived = lastIDSent;
            }
            postBox = null;
            LOCK.notifyAll();
        }
    }

    /**
     * Determines whether this executor was disposed.
     * 
     * @return {@code true} if the executor was disposed,
     * returns {@code false} otherwise.
     */
    private boolean isDisposed() {
        return (postBox == null);
    }

    /**
     * Called when a message for this executor is received.
     * 
     * @param messageTxt message for this executor.
     */
    void messageReceived(String messageTxt) {
        try {
            JSONObject message = new JSONObject(messageTxt);
            Object type = message.opt(MESSAGE_TYPE);
            if (MESSAGE_EVAL.equals(type)) {
                try {
                    int id = message.getInt(MESSAGE_ID);
                    synchronized (LOCK) {
                        for (int i=lastIDReceived+1; i<id; i++) {
                            LOG.log(Level.INFO, "Haven''t received result of execution of script with ID {0}.", i);
                            results.put(i, ERROR_RESULT);
                        }
                        Object status = message.opt(MESSAGE_STATUS);
                        Object result = message.opt(MESSAGE_RESULT);
                        if (MESSAGE_STATUS_OK.equals(status)) {
                            results.put(id, result);
                        } else {
                            LOG.log(Level.INFO, "Message with id {0} wasn''t executed successfuly: {1}",
                                    new Object[]{id, result});
                            results.put(id, ERROR_RESULT);
                        }
                        lastIDReceived = id;
                        LOCK.notifyAll();
                    }
                } catch (JSONException ex) {
                    LOG.log(Level.INFO, "Ignoring message with malformed id: {0}", messageTxt);
                }
            } else if (MESSAGE_SELECTION.equals(type)) {
                final ElementHandle handle = ElementHandle.forJSONObject(message.getJSONObject(MESSAGE_SELECTION));
                RequestProcessor.getDefault().post(new Runnable() {
                    @Override
                    public void run() {
                        PageModel.getDefault().setSelectedElements(Collections.singletonList(handle));
                    }
                });
            } else {
                LOG.log(Level.INFO, "Ignoring unexpected message: {0}", messageTxt);
            }
        } catch (JSONException ex) {
            LOG.log(Level.INFO, "Ignoring message that is not in JSON format: {0}", messageTxt);
        }        
    }

    /**
     * Interface through which the executor send messages to the browser plugin.
     */
    static interface PostBox {
        /**
         * Sends a message to the browser plugin.
         * 
         * @param message message to send to the browser plugin.
         */
        void postMessage(String message);
    } 
    
}
