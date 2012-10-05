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
package org.netbeans.modules.nativeexecution.pty;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.NativeProcess;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.api.util.HelperUtility;
import org.netbeans.modules.nativeexecution.api.util.Signal;
import org.netbeans.modules.nativeexecution.support.Logger;

/**
 *
 * @author akrasny
 */
public final class NbKillUtility extends HelperUtility {

    private final static NbKillUtility instance = new NbKillUtility();

    private NbKillUtility() {
        super("bin/nativeexecution/${osname}-${platform}${_isa}/killall"); // NOI18N
    }

    public static NbKillUtility getInstance() {
        return instance;
    }

    /**
     * Sends the specified signal to all processes within the specified process
     * session.
     *
     * This feature is supported on Solaris only and this call has no effect on
     * other systems.
     *
     * This is thread-safe method.
     *
     * This method could block calling thread for a while. Avoid to call it in
     * EDT.
     *
     * @param env - context environment
     * @param signal - signal to send
     * @param sid - ID of the session
     * @return -1 in case of some error or if utility is not supported; 0 on
     * success.
     */
    public int signalSession(ExecutionEnvironment env, Signal signal, int sid) {
        return doSignal("-s", env, signal, sid, 0, null); // NOI18N
    }

    /**
     * Sends the specified signal to all processes within the specified process
     * group (see killpg(3C)).
     *
     * This is thread-safe method.
     *
     * This method could block calling thread for a while. Avoid to call it in
     * EDT.
     *
     * @param env - context environment
     * @param signal - signal to send
     * @param pgid - ID of the process group
     * @return -1 in case of some error or if utility is not supported; 0 on
     * success.
     */
    public int signalGroup(ExecutionEnvironment env, Signal signal, int pgid) {
        return doSignal("-g", env, signal, pgid, 0, null); // NOI18N
    }

    /**
     * Sends the specified signal to the process with the specified process ID.
     * (see kill(3C)).
     *
     * This is thread-safe method.
     *
     * This method could block calling thread for a while. Avoid to call it in
     * EDT.
     *
     * @param env - context environment
     * @param signal - signal to send
     * @param pid - ID of the process group
     * @return -1 in case of some error or if utility is not supported; 0 on
     * success.
     */
    public int signalProcess(ExecutionEnvironment env, Signal signal, int pid) {
        return doSignal("-p", env, signal, pid, 0, null); // NOI18N
    }

    /**
     * Sends the specified signal with attached sigdata to the specified process
     * (see sigqueue(3RT)).
     *
     * On MacOSX sigqueue is not supported. In case of MacOSX call of this
     * method is equivalent for just sending signal to a process.
     *
     * This is thread-safe method.
     *
     * This method could block calling thread for a while. Avoid to call it in
     * EDT.
     *
     * @param env - context environment
     * @param signal - signal to send
     * @param sigdata - integer data to attach
     * @param pid - process ID to send signal to
     * @return -1 in case of some error or if utility is not supported; 0 on
     * success.
     */
    public int signalProcess(ExecutionEnvironment env, Signal signal, int sigdata, int pid) {
        return doSignal("-q", env, signal, pid, sigdata, null); // NOI18N
    }

    /**
     * Sends signal
     * <code>signal</code> to all processes that contain passed 'magic'
     * environment variable.
     *
     * This is thread-safe method.
     *
     * This method could block calling thread for a while. Avoid to call it in
     * EDT.
     *
     * @param env - context environment
     * @param signal - signal to send
     * @param magicEnv environment variable in form VAR=VAL.
     * @return -1 in case of some error or if utility is not supported; 0 on
     * success.
     */
    public int signalAll(ExecutionEnvironment env, Signal signal, String magicEnv) {
        return doSignal("-p", env, signal, 0, 0, magicEnv); // NOI18N
    }

    /**
     * Returns false if not supported for the environment passed.
     *
     * @param env - environment to validate
     * @return -1 in case of some error or if utility is not supported; 0 on
     * success.
     */
    public boolean isSupported(ExecutionEnvironment env) {
        boolean result = false;

        try {
            result = getPath(env) != null;
        } catch (IOException ex) {
        }

        if (!result) {
            Logger.getInstance().log(Level.FINEST, "Not supported for {0}", env.getDisplayName()); // NOI18N
        }

        return result;
    }

    private int doSignal(String scope, ExecutionEnvironment env, Signal signal, int id, int sigdata, String magicEnv) {
        if (!isSupported(env)) {
            return -1;
        }

        try {
            String path = getPath(env);
            NativeProcessBuilder npb = NativeProcessBuilder.newProcessBuilder(env);
            npb.getEnvironment().clear();
            npb.setExecutable(path);

            List<String> args = new ArrayList<String>();
            args.add(scope);
            args.add(signal == Signal.NULL ? "NULL" : signal.name().substring(3)); // NOI18N
            if (magicEnv != null) {
                args.add("-m"); // NOI18N
                args.add(magicEnv); // NOI18N
            }
            args.add("" + id); // NOI18N
            if ("-q".equals(scope)) { // NOI18N
                args.add("" + sigdata); // NOI18N
            }

            npb.setArguments(args.toArray(new String[args.size()]));
            NativeProcess kill = npb.call();
            return kill.waitFor();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return -1;
        } catch (IOException ex) {
            Logger.getInstance().log(Level.FINE, "attempt to send signal " + signal.name() + " to " + scope + " with ID == " + id, ex); // NOI18N
            return -1;
        }
    }
}
