/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.remote.experimental;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.Callable;
import org.netbeans.modules.cnd.remote.support.ExtProcessRemoteCommandSupport;
import org.openide.util.Exceptions;

/**
 *
 * @author Sergey Grinev
 */
public class RemoteProcessBuilder implements Callable<Process> {

    private final String command;

    public RemoteProcessBuilder(String command) {
        this.command = command;
    }

    public Process call() throws Exception {
        return new RemoteProcess();
    }

    private class RemoteProcess extends Process {

        private ExtProcessRemoteCommandSupport support;

        public RemoteProcess() {
            support = new ExtProcessRemoteCommandSupport("tester@eaglet-sr", "ls", null); // NOI18N
            try {
                is = support.getInputStream();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
                is = null;
            }
        }

        @Override
        public OutputStream getOutputStream() {
            try {
                return support.getOutputStream();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
                return null;
            }
        }
        private InputStream is;

        @Override
        public InputStream getInputStream() {
            return is;
        }

        @Override
        public InputStream getErrorStream() {
            try {
                return support.getErrorStream();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
                return null;
            }
        }

        @Override
        public int waitFor() throws InterruptedException {
            return support.waitFor();
        }

        @Override
        public int exitValue() {
            //TODO: check if finished
            return support.getExitStatus();
        }

        @Override
        public void destroy() {
            support.disconnect();
        }
    }
}
