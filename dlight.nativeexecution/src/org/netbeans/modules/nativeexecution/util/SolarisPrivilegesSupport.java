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
package org.netbeans.modules.nativeexecution.util;

import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ObservableAction;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import java.io.CharArrayWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.lang.ref.WeakReference;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import javax.swing.SwingUtilities;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.api.extexecution.ExecutionService;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.nativeexecution.api.impl.ConnectionManagerAccessor;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.support.ui.GrantPrivilegesDialog;
import org.netbeans.modules.nativeexecution.support.Encrypter;
import org.netbeans.modules.nativeexecution.support.InputRedirectorFactory;
import org.netbeans.modules.nativeexecution.support.Logger;
import org.netbeans.modules.nativeexecution.support.MacroExpanderFactory;
import org.netbeans.modules.nativeexecution.support.MacroExpanderFactory.MacroExpander;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.windows.InputOutput;

/**
 * Supporting class to provide functionality of requesting additional
 * process privileges (see privileges(5) to an execution session.
 * <br>
 * Execution session is either an ssh connection to a remote host or the
 * Runtime.getRuntime() for a localhost.
 * <br>
 * In case of localhost privileges will be granted to the current JVM process;
 * In case of remote - to the remote sshd process.
 * <br>
 * So, once execution session got needed privileges, any submitted task whithin
 * this session will inherit them.
 * <br>
 * To grant requested privileges a root password is needed. Password is prompted
 * but is never stored. So the password is asked for every new execution session.
 *
 */
public final class SolarisPrivilegesSupport {

    private static final java.util.logging.Logger log = Logger.getInstance();
    private Map<String, List<String>> privilegesHash =
            Collections.synchronizedMap(new HashMap<String, List<String>>());
    private static SolarisPrivilegesSupport instance = new SolarisPrivilegesSupport();
    private WeakReference<GrantPrivilegesDialog> dialogRef = null;

    private SolarisPrivilegesSupport() {
    }

    /**
     * Returnes <tt>SolarisPrivilegesSupport</tt> instance.
     * @return <tt>SolarisPrivilegesSupport</tt> instance.
     */
    public static SolarisPrivilegesSupport getInstance() {
        return instance;
    }

    /**
     * Tests whether the <tt>ExecutionEnvironment</tt> has all needed
     * execution privileges.
     * @param execEnv - <tt>ExecutionEnvironment</tt> to be tested
     * @param privs - list of priveleges to be tested
     * @return true if <tt>execEnv</tt> has all execution privileges listed in
     *         <tt>privs</tt>
     */
    public boolean hasPrivileges(
            final ExecutionEnvironment execEnv,
            final List<String> privs) {

        boolean status = true;
        List<String> real_privs = getExecutionPrivileges(execEnv);

        for (String priv : privs) {
            if (!status) {
                break;
            }
            status &= real_privs.contains(priv);
        }

        return status;
    }

    /**
     * Retrieves a list of currently effective execution privileges in the
     * <tt>ExecutionEnvironment</tt>
     *
     * @param execEnv <tt>ExecutionEnvironment</tt> to get privileges list from
     * @return a list of currently effective execution privileges
     */
    public List<String> getExecutionPrivileges(
            final ExecutionEnvironment execEnv) {
        return getExecutionPrivileges(execEnv, false);
    }

    private List<String> getExecutionPrivileges(
            final ExecutionEnvironment execEnv,
            final boolean forceQuery) {
        if (!forceQuery && privilegesHash.containsKey(execEnv.toString())) {
            return privilegesHash.get(execEnv.toString());
        }

        /*
         * To find out actual privileges that tasks will have use
         * > /bin/ppriv -v $$ | /bin/grep [IL]
         *
         * and return intersection of list of I (inherit) and L (limit)
         * privileges...
         */

        CharArrayWriter outWriter = new CharArrayWriter();

        NativeProcessBuilder npb =
                new NativeProcessBuilder(execEnv, "/bin/ppriv").setArguments("-v $$ | /bin/grep [IL]"); // NOI18N

        ExecutionDescriptor d = new ExecutionDescriptor();
        d = d.inputOutput(InputOutput.NULL);
        d = d.outLineBased(true);
        d = d.outProcessorFactory(new InputRedirectorFactory(outWriter));

        ExecutionService execService = ExecutionService.newService(
                npb, d, "getExecutionPrivileges"); // NOI18N

        Future<Integer> fresult = execService.run();
        int result = -1;

        try {
            result = fresult.get();
        } catch (ExecutionException ex) {
            Exceptions.printStackTrace(ex);
            return Collections.emptyList();
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
            return Collections.emptyList();
        }

        if (result != 0) {
            return Collections.emptyList();
        }

        List<String> iprivs = new ArrayList<String>();
        List<String> lprivs = new ArrayList<String>();

        String[] outArray = outWriter.toString().split("\n"); // NOI18N
        for (String str : outArray) {

            if (str.contains("I:")) { // NOI18N
                String[] privs = str.substring(
                        str.indexOf(": ") + 2).split(","); // NOI18N
                iprivs = Arrays.asList(privs);
            } else if (str.contains("L:")) { // NOI18N
                String[] privs = str.substring(
                        str.indexOf(": ") + 2).split(","); // NOI18N
                lprivs = Arrays.asList(privs);
            }
        }

        if (iprivs == null || lprivs == null) {
            return Collections.emptyList();
        }

        List<String> real_privs = new ArrayList<String>();

        for (String ipriv : iprivs) {
            if (lprivs.contains(ipriv)) {
                real_privs.add(ipriv);
            }
        }

        privilegesHash.put(execEnv.toString(), real_privs);

        return real_privs;
    }

    /**
     * Tries to get requested privileges for the <tt>ExecutionEnvironment</tt>.
     * SHOULD NOT be called in AWT thread
     *
     * @return  true - if privileges has changed
     *          false - if no changes to privileges happened
     */
    private synchronized boolean requestExecutionPrivileges(
            final ExecutionEnvironment execEnv,
            final List<String> requiredPrivileges) {

        if (SwingUtilities.isEventDispatchThread()) {
            throw new RuntimeException("requestExecutionPrivileges " + // NOI18N
                    "should never be called in AWT thread"); // NOI18N
        }

        if (hasPrivileges(execEnv, requiredPrivileges)) {
            return false;
        }

        final ProgressHandle ph = ProgressHandleFactory.createHandle(
                loc("TaskPrivilegesSupport_Progress_RequestPrivileges")); // NOI18N
        ph.start();

        final List<String> currentPrivileges =
                getExecutionPrivileges(execEnv, false);

        List<String> newPrivileges = null;

        try {
            if (dialogRef == null || dialogRef.get() == null) {
                dialogRef = new WeakReference<GrantPrivilegesDialog>(
                        new GrantPrivilegesDialog());
            }

            GrantPrivilegesDialog dialog = dialogRef.get();
            boolean result = dialog.askPassword();

            if (result) {
                PrivilegesRequestor.doRequest(execEnv, requiredPrivileges,
                        dialog.getUser(), dialog.getPassword());
                newPrivileges = getExecutionPrivileges(execEnv, true);
            }
        } finally {
            ph.finish();
        }

        return newPrivileges != null &&
                !newPrivileges.equals(currentPrivileges);
    }

    /**
     * Returns <tt>ObservableAction<Boolean></tt> that can be invoked in order
     * to request needed execution privileges
     *
     * @param execEnv <tt>ExecutionEnvironment</tt> where to request privileges
     * @param requestedPrivileges a list of execution privileges to request
     * @return <tt>ObservableAction<Boolean></tt> that can be invoked in order
     * to request needed execution privileges
     */
    public ObservableAction<Boolean> requestPrivilegesAction(
            final ExecutionEnvironment execEnv,
            final List<String> requestedPrivileges) {

        RequestPrivilegesAction action =
                new RequestPrivilegesAction(execEnv, requestedPrivileges);

        return action;
    }

    private static class RequestPrivilegesAction
            extends ObservableAction<Boolean> {

        private final ExecutionEnvironment execEnv;
        private final List<String> requestedPrivileges;

        public RequestPrivilegesAction(
                final ExecutionEnvironment execEnv,
                final List<String> requestedPrivileges) {

            super(loc("TaskPrivilegesSupport_GrantPrivileges_Action")); // NOI18N
            this.execEnv = execEnv;
            this.requestedPrivileges = requestedPrivileges;
        }

        @Override
        protected Boolean performAction() {
            SolarisPrivilegesSupport sup = SolarisPrivilegesSupport.getInstance();
            return sup.requestExecutionPrivileges(execEnv, requestedPrivileges);
        }
    }

    private static String loc(String key, Object... params) {
        return NbBundle.getMessage(SolarisPrivilegesSupport.class, key, params);
    }

    private static class PrivilegesRequestor {

        private static final Map<String, Long> csums =
                new HashMap<String, Long>();


        static {
            csums.put("SunOS-x86", 2381709310L); // NOI18N
        }

        private static void doRequest(
                final ExecutionEnvironment execEnv,
                final List<String> requestedPrivileges,
                String user, String passwd) {

            // Construct privileges list
            StringBuffer sb = new StringBuffer();

            for (String priv : requestedPrivileges) {
                sb.append(priv).append(","); // NOI18N
            }

            if (execEnv.isLocal()) {
                doRequestLocal(execEnv, sb.toString(), user, passwd);
            } else {
                doRequestRemote(execEnv, sb.toString(), user, passwd);
            }
        }

        private static void doRequestLocal(final ExecutionEnvironment execEnv,
                final String requestedPrivs, String user, String passwd) {

            MacroExpander macroExpander = MacroExpanderFactory.getExpander(execEnv);
            String privp = null;
            String path = "$osname-$platform";
            try {
                path = macroExpander.expandMacros(path); // NOI18N
            } catch (ParseException ex) {

            }

            privp = "bin/nativeexecution/" + path + "/privp"; // NOI18N
            InstalledFileLocator fl = InstalledFileLocator.getDefault();
            File file = fl.locate(privp, null, false);

            if (file == null || !file.exists()) {
                log.severe("Cannot request privileges! privp not found!"); // NOI18N
                return;
            }

            privp = file.getAbsolutePath();

            // Will not pass any password to unknown program...
            if (!Encrypter.checkCRC32(privp, csums.get(path))) {
                log.severe("Wrong privp executable! CRC check failed!"); // NOI18N
                return;
            }

            Runtime rt = Runtime.getRuntime();
            Process p = null;

            try {
                // Set execution privileges ...
                p = rt.exec(new String[]{"/bin/chmod", "755", privp}); // NOI18N
                p.waitFor();
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }

            String pid = null;

            try {
                File self = new File("/proc/self"); // NOI18N
                pid = self.getCanonicalFile().getName();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }

            if (pid == null) {
                return;
            }

            try {
                p = rt.exec(new String[]{privp, user, requestedPrivs, pid});
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }

            if (p == null) {
                return;
            }

            PrintWriter w = new PrintWriter(p.getOutputStream());
            w.write(passwd.trim() + "\n"); // NOI18N
            w.flush();

            try {
                int result = p.waitFor();
                if (result != 0) {
                    log.severe("doRequestLocal failed! privp returned " + result); // NOI18N
                }
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        /**
         * Expects some predefined string to appear in reader's stream
         * @param r - reader to use
         * @param expectedString
         * @return
         */
        private final static String expect(
                final InputStream in,
                final String expectedString) {

            int pos = 0;
            int len = expectedString.length();
            char[] cbuf = new char[2];
            StringBuffer sb = new StringBuffer();

            try {
                Reader r = new InputStreamReader(in);
                while (pos != len && r.read(cbuf, 0, 1) != -1) {
                    char currentChar = expectedString.charAt(pos);
                    if (currentChar == '%') {
                        pos++;
                        sb.append(cbuf[0]);
                    } else if (currentChar == cbuf[0]) {
                        pos++;
                    } else {
                        pos = 0;
                    }
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }

            return sb.toString();

        }

        private static synchronized void doRequestRemote(
                final ExecutionEnvironment execEnv,
                final String requestedPrivs,
                final String user, final String passwd) {

            ConnectionManager mgr = ConnectionManager.getInstance();

            final Session session = ConnectionManagerAccessor.getDefault().
                    getConnectionSession(mgr, execEnv);

            if (session == null) {
                return;
            }

            String pid = "`/usr/bin/ptree $$|" + // NOI18N
                    "/bin/awk '/sshd$/{p=$1}END{print p}'`"; // NOI18N

            OutputStream out = null;
            InputStream in = null;

            String script = "/usr/bin/ppriv -s I+" + // NOI18N
                    requestedPrivs + " " + pid; // NOI18N

            StringBuffer cmd = new StringBuffer("/sbin/su - "); // NOI18N
            cmd.append(user).append(" -c \""); // NOI18N
            cmd.append(script).append("\"; echo ExitStatus:$?\n"); // NOI18N

            ChannelShell channel = null;
            try {
                channel = (ChannelShell) session.openChannel("shell"); // NOI18N
                channel.setPty(true);
                channel.setPtyType("ldterm"); // NOI18N

                out = channel.getOutputStream();
                in = channel.getInputStream();

                channel.connect();

                PrintWriter w = new PrintWriter(out);
                w.write(cmd.toString());
                w.flush();

                expect(in, "Password:"); // NOI18N

                w.write(passwd.trim() + "\n"); // NOI18N
                w.flush();

                String exitStatus = expect(in, "ExitStatus:%"); // NOI18N

                int status = 1;
                try {
                    status = Integer.valueOf(exitStatus).intValue();
                } finally {
                    if (status != 0) {
                        NotifyDescriptor dd =
                                new NotifyDescriptor.Message("/sbin/su failed"); // NOI18N
                        DialogDisplayer.getDefault().notify(dd);
                    }
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            } catch (JSchException ex) {
                Exceptions.printStackTrace(ex);
            } finally {
                // DO NOT CLOSE CHANNEL HERE...
                // channel.disconnect();

                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }

                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
        }
    }
}
