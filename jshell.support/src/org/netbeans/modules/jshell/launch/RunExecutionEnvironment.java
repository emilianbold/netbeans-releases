/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
 */
package org.netbeans.modules.jshell.launch;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import jdk.internal.jshell.remote.RemoteCodes;
import org.netbeans.lib.nbjshell.NbExecutionControl;
import jdk.jshell.spi.ExecutionEnv;
import org.netbeans.lib.nbjshell.NbExecutionControlBase;
import org.openide.awt.StatusDisplayer;

/**
 * Exec environment suitable for machines without active JDI connection.
 *
 * @author sdedic
 */
public class RunExecutionEnvironment extends NbExecutionControlBase<Long> implements RemoteJShellAccessor, ShellLaunchListener, NbExecutionControl {
    private final ShellAgent agent;
    
    private boolean added;
    private volatile boolean closed;
    private JShellConnection shellConnection;
    private ObjectInputStream dis;
    private ObjectOutputStream dos;
    private String targetSpec;

    public RunExecutionEnvironment(ShellAgent agent, String targetSpec) {
        this.agent = agent;
        this.targetSpec = targetSpec;
    }
    
    public JShellConnection getOpenedConnection() {
        synchronized (this) {
            return shellConnection;
        }
    }

    @Override
    public boolean redefineClasses(Map<Long, byte[]> redefines) throws IOException, IllegalStateException {
        if (dis == null || dos == null) {
            throw new UnsupportedOperationException("streams not opened");
        }
        dos.writeInt(NbExecutionControl.CMD_REDEFINE);
        dos.writeInt(redefines.size());
        for (Map.Entry<Long, byte[]> en : redefines.entrySet()) {
            Long l = (Long)en.getKey();
            
            dos.writeLong(l);
            dos.writeObject(en.getValue());
        }
        dos.flush();
        
        int code = dis.readInt();
        if (code == RemoteCodes.RESULT_SUCCESS) {
            return true;
        }
        if (code == RemoteCodes.RESULT_FAIL) {
            String msg = dis.readUTF();
            throw new IllegalStateException(msg);
        } else {
            throw new IOException("Invalid response code");
        }
    }

    @Override
    protected void shutdown() {
        requestShutdown();
        super.shutdown();
    }

    
    @Override
    protected boolean isClosed() {
        return closed;
    }
    
    @Override
    public Collection<Long> nameToRef(String className) {
        if (dis == null || dos == null) {
            // no class sent over -> no class ever could be defined.
            return null;
        }
        try {
            dos.writeInt(NbExecutionControl.CMD_CLASSID);
            dos.writeUTF(className);
            dos.flush();
            
            int code = dis.readInt();
            if (code != RemoteCodes.RESULT_SUCCESS) {
                return null;
            }
            long l = dis.readLong();
            return l != -1 ? Collections.singleton(l) : null;
        } catch (IOException ex) {
            return null;
        }
    }

    @Override
    public void start(ExecutionEnv ee) throws Exception {
        getConnection(false);
        OutputStream o = getConnection(true).getAgentInput();
        InputStream i = getConnection(true).getAgentOutput();
        init(i, o, ee);
        dis = getRemoteIn();
        dos = getRemoteOut();
    }

    @Override
    public void close() {
        requestShutdown();
    }

    /**
     * Sends stop user code instruction. Must create a separate connection to the agent
     * @return
     * @throws IllegalStateException 
     */
    @Override
    public void stop() {
        int id = this.shellConnection.getRemoteAgentId();
        try (JShellConnection stopConnection = agent.createConnection();
            ObjectInputStream in = new ObjectInputStream(stopConnection.getAgentOutput());
            ObjectOutputStream out = new ObjectOutputStream(stopConnection.getAgentInput())
        ) {
            
            out.writeInt(NbExecutionControl.CMD_STOP);
            out.writeInt(id);
            out.flush();
            int success = in.readInt();
        } catch (IOException ex) {
        }
    }

    @Override
    public boolean requestShutdown() {
        agent.closeConnection(shellConnection);
        return false;
    }

    private JShellConnection getConnection(boolean dontConnect) throws IOException {
        synchronized (this) {
            if (closed || (dontConnect && shellConnection == null)) {
                throw new IOException(Bundle.MSG_AgentConnectionBroken());
            }
            if (shellConnection != null) {
                return shellConnection;
            }
        }
        try {
            JShellConnection x = agent.createConnection();
            synchronized (this) {
                if (!added) {
                    ShellLaunchManager.getInstance().addLaunchListener(this);
                    added = true;
                }
                return this.shellConnection = x;
            }
        } catch (IOException ex) {
            StatusDisplayer.getDefault().setStatusText(Bundle.MSG_ErrorConnectingToAgent(ex.getLocalizedMessage()), 100);
            throw ex;
        }
    }

    @Override
    public synchronized void closeStreams() {
        if (shellConnection == null) {
            return;
        }
        try {
            OutputStream os = shellConnection.getAgentInput();
            os.close();
        } catch (IOException ex) {
        }
        try {
            InputStream is = shellConnection.getAgentOutput();
            is.close();
        } catch (IOException ex) {
        }

        requestShutdown();
    }

    @Override
    public void connectionInitiated(ShellLaunchEvent ev) { }

    @Override
    public void handshakeCompleted(ShellLaunchEvent ev) { }

    @Override
    public void connectionClosed(ShellLaunchEvent ev) {
        synchronized (this) {
            if (ev.getConnection() != this.shellConnection || closed) {
                return;
            }
            closed = true;
        }
        ShellLaunchManager.getInstance().removeLaunchListener(this);
    }

    @Override
    public void agentDestroyed(ShellLaunchEvent ev) {
        synchronized (this) {
            if (ev.getAgent() != agent || closed) {
                return;
            }
            this.shellConnection = null;
            closed = true;
        }
        ShellLaunchManager.getInstance().removeLaunchListener(this);
    }

    @Override
    public String getTargetSpec() {
        return targetSpec;
    }
}
