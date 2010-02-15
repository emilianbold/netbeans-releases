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
import org.openide.util.Lookup;
import org.openide.windows.InputOutput;

/**
 *
 * @author ak119685
 */
public final class PtySupport {

    static {
        PtyImplAccessor.setDefault(new Accessor());
    }

    private PtySupport() {
    }

    public static Pty getPty(NativeProcess process) {
        if (!(process instanceof PtyNativeProcess)) {
            return null;
        }

        return ((PtyNativeProcess) process).getPty();
    }

    public static void connect(InputOutput io, NativeProcess process) {
        Collection<? extends IOConnector> connectors =
                Lookup.getDefault().lookupAll(IOConnector.class);

        for (IOConnector connector : connectors) {
            if (connector.connect(io, process)) {
                return;
            }
        }

        throw new UnsupportedOperationException(
                "No suitable IOConnector implementation found to connect "
                + io.toString() + " with " + process.toString()); // NOI18N
    }

    public static Pty allocate(ExecutionEnvironment env) throws IOException {
        if (!ConnectionManager.getInstance().isConnectedTo(env)) {
            throw new IllegalStateException();
        }

        Collection<? extends PtyAllocator> creators =
                Lookup.getDefault().lookupAll(PtyAllocator.class);

        for (PtyAllocator creator : creators) {
            if (creator.isApplicable(env)) {
                PtyImpl pty = creator.allocate(env);
                if (pty != null) {
                    return new Pty(pty);
                }
            }
        }

        return null;
    }

    public final static class Pty {

        private final PtyImpl impl;

        private Pty(PtyImpl impl) {
            this.impl = impl;
        }

        public String getSlaveName() {
            return impl.getSlaveName();
        }
    }

    private final static class Accessor extends PtyImplAccessor {

        @Override
        public PtyImpl getImpl(Pty pty) {
            return pty == null ? null : pty.impl;
        }
    }
}
