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

package org.netbeans.modules.cnd.remote.ui.wizard;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.concurrent.CancellationException;
import org.netbeans.modules.cnd.api.toolchain.CompilerSetManager;
import org.netbeans.modules.cnd.api.remote.ServerList;
import org.netbeans.modules.cnd.remote.server.RemoteServerRecord;
import org.netbeans.modules.cnd.remote.support.RemoteCommandSupport;
import org.netbeans.modules.cnd.spi.remote.setup.HostValidator;
import org.netbeans.modules.cnd.api.toolchain.ui.ToolsCacheManager;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.openide.util.NbBundle;

/**
 *
 * @author Vladimir Kvashin
 */
public class HostValidatorImpl implements HostValidator {

    private final ToolsCacheManager cacheManager;
    private Runnable runOnFinish;

    public HostValidatorImpl(ToolsCacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @Override
    public Runnable getRunOnFinish() {
        return runOnFinish;
    }

    // TODO: ToolsCacheManager FIXUP
    public ToolsCacheManager getCacheManager() {
        return cacheManager;
    }
    
    @Override
    public boolean validate(ExecutionEnvironment env, char[] password, boolean rememberPassword, final PrintWriter writer) {
        boolean result = false;
        final RemoteServerRecord record = (RemoteServerRecord) ServerList.get(env);
        final boolean alreadyOnline = record.isOnline();
        if (alreadyOnline) {
            String message = NbBundle.getMessage(getClass(), "CreateHostVisualPanel2.MsgAlreadyConnected1");
            message = String.format(message, env.toString());
            writer.printf("%s", message); // NOI18N
        } else {
            record.resetOfflineState(); // this is a do-over
        }
        // move expensive operation out of EDT

        if (!alreadyOnline) {
            writer.print(NbBundle.getMessage(getClass(), "CreateHostVisualPanel2.MsgConnectingTo",
                    env.getHost()));
        }
        try {
            if (password == null || password.length == 0) {
                ConnectionManager.getInstance().connectTo(env);
            } else {
                ConnectionManager.getInstance().connectTo(env, password, rememberPassword);
            }
        } catch (IOException ex) {
            writer.print("\n" + RemoteCommandSupport.getMessage(ex)); //NOI18N
            return false;
        } catch (CancellationException ex) {
            return false;
        }
        if (!alreadyOnline) {
            writer.print(NbBundle.getMessage(getClass(), "CreateHostVisualPanel2.MsgDone") + '\n');
            writer.print(NbBundle.getMessage(getClass(), "CSM_ConfHost") + '\n');
            record.init(null);
        }
        if (record.isOnline()) {
            Writer reporter = new Writer() {

                @Override
                public void write(char[] cbuf, int off, int len) throws IOException {
                    final String value = new String(cbuf, off, len);
                    writer.print(value);
                }

                @Override
                public void flush() throws IOException {
                }

                @Override
                public void close() throws IOException {
                }
            };
            final CompilerSetManager csm = cacheManager.getCompilerSetManagerCopy(env, false);
            csm.initialize(false, false, reporter);
            runOnFinish = new Runnable() {
                @Override
                public void run() {
                    csm.finishInitialization();
                }
            };
            result = true;
        } else {
            writer.write(NbBundle.getMessage(getClass(), "CreateHostVisualPanel2.ErrConn")
                    + '\n' + record.getReason()); //NOI18N
        }
        if (alreadyOnline) {
            writer.write('\n' + NbBundle.getMessage(getClass(), "CreateHostVisualPanel2.MsgAlreadyConnected2"));
        }
        return result;
    }
}
