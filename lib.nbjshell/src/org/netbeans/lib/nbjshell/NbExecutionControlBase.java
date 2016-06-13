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
package org.netbeans.lib.nbjshell;

import com.sun.jdi.ReferenceType;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import jdk.jshell.JShellException;
import jdk.jshell.spi.ExecutionEnv;
import org.netbeans.lib.nbjshell.RemoteExecutionSupport.RemoteEnv;

/**
 *
 * @author sdedic
 */
public abstract class NbExecutionControlBase<T> implements 
        NbExecutionControl,
        RemoteExecutionSupport.ClassControl<T> {
    private static final Logger LOG = Logger.getLogger(NbExecutionControlBase.class.getName());
    
    private RemoteExecutionSupport<T>   delegate;
    private ExecutionEnv execEnv;
    private ObjectInputStream   remoteIn;
    private ObjectOutputStream  remoteOut;
    protected final Object LOCK = new Object();
    
    protected NbExecutionControlBase() {
        delegate = createClosedSupport();
    }
    
    protected final void init(InputStream in, OutputStream out, ExecutionEnv execEnv) throws IOException {
        remoteOut = new ObjectOutputStream(out);
        remoteIn = new ObjectInputStream(
                RemoteExecutionSupport.demultiplexResponseStream(
                        in, null, null, Executors.newCachedThreadPool()
                )
        );
        delegate = new RemoteExecutionSupport<T>(new RemoteEnvImpl(), execEnv, this, LOCK);
    }
    
    protected final ExecutionEnv execEnv() {
        return execEnv;
    }

    public final boolean isUserCodeRunning() {
        return delegate.isUserCodeRunning();
    }
    
    protected Object getLock() {
        return LOCK;
    }

    public void close() {
        try {
            delegate.close();
        } catch (IOException ex) {
            
        }
    }

    public boolean load(Collection<String> classes) {
        return delegate.load(classes);
    }

    public String invoke(String classname, String methodname) throws JShellException {
        return delegate.invoke(classname, methodname);
    }

    public String varValue(String classname, String varname) {
        return delegate.varValue(classname, varname);
    }

    public boolean addToClasspath(String cp) {
        return delegate.addToClasspath(cp);
    }

    public boolean redefine(Collection<String> classes) {
        return delegate.redefine(classes);
    }

    public ClassStatus getClassStatus(String classname) {
        return delegate.getClassStatus(classname);
    }
    
    protected abstract boolean isClosed();
    
    protected void shutdown() {
        try {
            remoteIn.close();
            remoteOut.close();
        } catch (IOException ex) {
            LOG.log(Level.INFO, "Error closing streams", ex);
        }
    }
    
    private class RemoteEnvImpl implements RemoteExecutionSupport.RemoteEnv {
        @Override
        public ObjectInput getRemoteIn() {
            return remoteIn;
        }

        @Override
        public ObjectOutput getRemoteOut() {
            return remoteOut;
        }

        @Override
        public boolean isClosed() {
            return NbExecutionControlBase.this.isClosed();
        }

        @Override
        public void shutdown() {
            NbExecutionControlBase.this.shutdown();
        }
    }
    
    public ObjectOutputStream getRemoteOut() {
        return remoteOut;
    }
    
    protected ObjectInputStream  getRemoteIn() {
        return remoteIn;
    }
    
    @Override
    public Map<String, String> commandVersionInfo() {
        ObjectOutput out = getRemoteOut();
        ObjectInput in = getRemoteIn();
        Map<String, String> result = new HashMap<>();
        try {
            out.writeInt(CMD_VERSION_INFO);
            out.flush();
            int num = in.readInt();
            for (int i = 0; i < num; i++) {
                String key = in.readUTF();
                String val = in.readUTF();
                result.put(key, val);
            }
        } catch (IOException ex) {
            LOG.log(Level.INFO, "Error invoking JShell agent", ex.toString());
        }
        return result;
    }

    protected InputStream demultiplexAgentOutput(InputStream in, PrintStream output, PrintStream error) {
        return RemoteExecutionSupport.demultiplexResponseStream(in, output, error, 
                Executors.newCachedThreadPool());
    }

    static class ErrInput extends ByteArrayInputStream {
        private boolean err;
        
        public ErrInput(byte[] buf) {
            super(buf);
        }
        
        @Override
        public synchronized int read(byte[] b, int off, int len) {
            if (err) {
                return -1;
            }
            return super.read(b, off, len); 
        }

        @Override
        public synchronized int read() {
            if (err) {
                return -1;
            }
            return super.read(); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public int read(byte[] b) throws IOException {
            if (err) {
                throw new IOException("Not connected");
            }
            return super.read(b);
        }
    }

    public static <T> RemoteExecutionSupport<T> createClosedSupport() {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            final ObjectOutputStream out = new ObjectOutputStream(baos); // will produce OOS header
            baos.flush();
            ErrInput in = new ErrInput(baos.toByteArray());
            final ObjectInputStream remoteIn = new ObjectInputStream(in); // will read in the header :)
            in.err = true;
            return new RemoteExecutionSupport<T>(new RemoteEnv() {
                public ObjectInput          getRemoteIn() { return remoteIn; }
                public ObjectOutput         getRemoteOut() { return out; }
                public boolean              isClosed() { return true; }
                public void                 shutdown() {}
            }, null, null, new Object());
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
