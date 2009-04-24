/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.remote.server;

import java.beans.PropertyChangeSupport;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.cnd.api.remote.ServerRecord;
import org.netbeans.modules.cnd.remote.mapper.RemotePathMap;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.openide.awt.StatusDisplayer;
import org.openide.util.NbBundle;

/**
 * The definition of a remote server and login. 
 * 
 * @author gordonp
 */
public class RemoteServerRecord implements ServerRecord {

    public static enum State {
        UNINITIALIZED, INITIALIZING, ONLINE, OFFLINE, CANCELLED;
    }
    
    public static final String PROP_STATE_CHANGED = "stateChanged"; // NOI18N

    private final ExecutionEnvironment executionEnvironment;
    private final boolean editable;
    private boolean deleted;
    private State state;
    private final Object stateLock;
    private String reason;
    private String displayName;
    
    private static final Logger log = Logger.getLogger("cnd.remote.logger"); // NOI18N
    
    /**
     * Create a new ServerRecord. This is always called from RemoteServerList.get, but can be
     * in the AWT Event thread if called while adding a node from ToolsPanel, or in a different
     * thread if called during startup from cached information.
     */
    protected RemoteServerRecord(ExecutionEnvironment env) {
        this(env, false);
    }

    protected RemoteServerRecord(final ExecutionEnvironment env, boolean connect) {
        this(env, env.getDisplayName(), connect);
    }

    protected RemoteServerRecord(final ExecutionEnvironment env, String displayName, boolean connect) {
        this.executionEnvironment = env;
        stateLock = new String("RemoteServerRecord state lock for " + toString()); // NOI18N
        reason = null;
        deleted = false;
        this.displayName = (displayName == null) ? env.getDisplayName() : displayName;
        
        if (env.isLocal()) {
            editable = false;
            state = State.ONLINE;
        } else {
            editable = true;
            state = connect ? State.UNINITIALIZED : State.OFFLINE;
        }
    }

    @Override
    public ExecutionEnvironment getExecutionEnvironment() {
        return executionEnvironment;
    }

    @Override
    public String toString() {
        return executionEnvironment.toString();
    }

    public synchronized void validate(final boolean force) {
        if (isOnline()) {
            return;
        }
        log.fine("RSR.validate2: Validating " + toString());
        if (force) {
            ProgressHandle ph = ProgressHandleFactory.createHandle(NbBundle.getMessage(RemoteServerRecord.class, "PBAR_ConnectingTo", getDisplayName())); // NOI18N
            ph.start();
            init(null);
            ph.finish();
        }
        String msg;
        if (isOnline()) {
            msg = NbBundle.getMessage(RemoteServerRecord.class, "Validation_OK", getDisplayName());// NOI18N
        } else {
            msg = NbBundle.getMessage(RemoteServerRecord.class, "Validation_ERR", getDisplayName(), getStateAsText(), getReason());// NOI18N
        }
        StatusDisplayer.getDefault().setStatusText(msg);        
    }
    
    /**
     * Start the initialization process. This should <b>never</b> be done from the AWT Evet
     * thread. Parts of the initialization use this thread and will block.
     */
    public synchronized void init(PropertyChangeSupport pcs) {
        assert !SwingUtilities.isEventDispatchThread() : "RemoteServer initialization must be done out of EDT"; // NOI18N
        Object ostate = state;
        state = State.INITIALIZING;
        RemoteServerSetup rss = new RemoteServerSetup(getExecutionEnvironment());
        if (rss.needsSetupOrUpdate()) {
            rss.setup();
        }

        synchronized (stateLock) {
            if (rss.isCancelled()) {
                state = State.CANCELLED;
            } else if (rss.isFailed()) {
                state = State.OFFLINE;
                reason = rss.getReason();
            } else {
                RemotePathMap.getRemotePathMapInstance(getExecutionEnvironment()).init();
                state = State.ONLINE;
            }
        }
        if (pcs != null) {
            pcs.firePropertyChange(RemoteServerRecord.PROP_STATE_CHANGED, ostate, state);
        }
    }
    
    public boolean resetOfflineState() {
        synchronized (stateLock) {
            if (this.state != State.INITIALIZING && state != State.ONLINE) {
                state = State.UNINITIALIZED;
                return true;
            }
        }
        return false;
    }
    
    public String getStateAsText() {
        // TODO: not good to use object's toString as resource key
        return NbBundle.getMessage(RemoteServerRecord.class, state.toString());
    }
    
    public boolean isOnline() {
        return state == State.ONLINE;
    }
    
    public boolean isOffline() {
        return state == State.OFFLINE;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public boolean isDeleted() {
        return deleted;
    }
    
    public boolean isEditable() {
        return editable;
    }

    public boolean isRemote() {
        return executionEnvironment.isRemote();
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getServerName() {
        return executionEnvironment.getHost();
    }

    public String getUserName() {
        return executionEnvironment.getUser();
    }
    
    public String getReason() {
        return reason == null ? "" : reason;
    }

    /*package*/void setState(State state) {
        this.state = state;
    }
}
