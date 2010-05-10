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
import org.netbeans.modules.nativeexecution.PtyNativeProcess;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.NativeProcess;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.pty.IOConnector;
import org.netbeans.modules.nativeexecution.pty.PtyAllocator;
import org.netbeans.modules.nativeexecution.support.Logger;
import org.openide.windows.InputOutput;

/**
 * An utility class for pty-related stuff...
 *
 * @author ak119685
 */
public final class PtySupport {

    private static final java.util.logging.Logger log = Logger.getInstance();

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
    public static String getTTY(NativeProcess process) {
        if (!(process instanceof PtyNativeProcess)) {
            return null;
        }

        return ((PtyNativeProcess) process).getTTY();
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
        return IOConnector.getInstance().connect(io, process);
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
        return IOConnector.getInstance().connect(io, pty);
    }

    /**
     * Allocates a new 'unconnected' pty
     * @param env - environmant in which a pty should be allocated
     * @return newly allocated pty or <tt>null</tt> if allocation failed
     */
    public static Pty allocate(ExecutionEnvironment env) throws IOException {
        if (!ConnectionManager.getInstance().isConnectedTo(env)) {
            throw new IllegalStateException();
        }

        return PtyAllocator.getInstance().allocate(env);
    }

    public static void deallocate(Pty pty) throws IOException {
        pty.close();
    }
}
