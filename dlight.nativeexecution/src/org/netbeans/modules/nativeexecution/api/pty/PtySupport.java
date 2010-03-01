/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.nativeexecution.api.pty;

import java.io.IOException;
import java.util.Collection;
import org.netbeans.modules.nativeexecution.PtyNativeProcess;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.NativeProcess;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.spi.pty.IOConnector;
import org.netbeans.modules.nativeexecution.spi.pty.PtyAllocator;
import org.netbeans.modules.nativeexecution.spi.pty.PtyImpl;
import org.netbeans.modules.nativeexecution.spi.support.pty.PtyImplAccessor;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.windows.InputOutput;

/**
 * An utility class for pty-related stuff...
 *
 * @author ak119685
 */
public final class PtySupport {

    static {
        PtyImplAccessor.setDefault(new Accessor());
    }

    private PtySupport() {
    }

    /**
     * This method returns a Pty that is currently associated with the process
     * (if any).
     *
     * @param process - process to get pty of
     * @return Pty that is currently associated with the process or <tt>null</tt>
     * if ptocess was started in non-pty mode.
     */
    public static Pty getPty(NativeProcess process) {
        if (!(process instanceof PtyNativeProcess)) {
            return null;
        }

        return ((PtyNativeProcess) process).getPty();
    }

    /**
     * Connects process' IO streams with the specified InputOutput.
     * 
     * @param io - <tt>InputOutput</tt> to connect process' IO with
     * @param process - the process which should be connected with the io
     *
     * @return <tt>true</tt> if operation was successfull. <tt>false</tt> otherwise.
     */
    public static boolean connect(InputOutput io, NativeProcess process) {
        Collection<? extends IOConnector> connectors =
                Lookup.getDefault().lookupAll(IOConnector.class);

        for (IOConnector connector : connectors) {
            if (connector.connect(io, process)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Connects pty's IO streams (master side) with the specified <tt>InputOutput</tt>.
     * So that IO of the process that will do input/output to the specified pty'
     * slave will go to the specified <tt>InputOutput</tt>.
     *
     * @param io - <tt>InputOutput</tt> to connect pty's IO with
     * @param pty - the pty to connect InputOutput with
     *
     * @return <tt>true</tt> if operation was successfull. <tt>false</tt> otherwise.
     */
    public static boolean connect(InputOutput io, Pty pty) {
        Collection<? extends IOConnector> connectors =
                Lookup.getDefault().lookupAll(IOConnector.class);

        for (IOConnector connector : connectors) {
            if (connector.connect(io, pty)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Allocates a new 'unconnected' pty
     * @param env - environmant in which a pty should be allocated
     * @return newly allocated pty or <tt>null</tt> if allocation failed
     */
    public static Pty allocate(ExecutionEnvironment env) {
        if (!ConnectionManager.getInstance().isConnectedTo(env)) {
            throw new IllegalStateException();
        }

        Collection<? extends PtyAllocator> creators =
                Lookup.getDefault().lookupAll(PtyAllocator.class);

        for (PtyAllocator creator : creators) {
            if (creator.isApplicable(env)) {
                PtyImpl pty = null;

                try {
                    pty = creator.allocate(env);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }

                if (pty != null) {
                    return new Pty(pty);
                }
            }
        }

        return null;
    }

    /**
     * A class that represents a pty
     * @see PtySupport.allocate(ExecutionEnvironment)
     * @see PtySupport.getPty(NativeProcess)
     */
    public final static class Pty {

        private final PtyImpl impl;

        private Pty(PtyImpl impl) {
            this.impl = impl;
        }

        /**
         * Returns the name that can be used to connect to the slave side of
         * the pseudo-terminal (as tty(1))
         *
         * @return user's terminal name
         */
        public String getSlaveName() {
            return impl.getSlaveName();
        }

        /**
         * Closes the pty. It is responsibility of user to close the pty if
         * it was allocated directly. If a pty was allocated indirectly (while
         * starting a process in a pty mode, closure of the pty is done
         * automatically).
         *
         * @throws IOException in case close failed.
         */
        public void close() throws IOException {
            impl.close();
        }
    }

    private final static class Accessor extends PtyImplAccessor {

        @Override
        public PtyImpl getImpl(Pty pty) {
            return pty == null ? null : pty.impl;
        }
    }
}
