package org.netbeans.modules.nativeexecution.api.util;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import org.netbeans.modules.nativeexecution.ConnectionManagerAccessor;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.support.WindowsSupport;
import org.openide.util.Exceptions;
import org.openide.util.Utilities;

/**
 * Utility class that provides information about particual host.
 */
public final class HostInfoUtils {

    /**
     * String constant that can be used to identify a localhost.
     */
    public static final String LOCALHOST = "127.0.0.1"; // NOI18N
    private static final List<String> myIPAdresses = new ArrayList<String>();
    private static final Map<String, Boolean> filesExistenceHash =
            Collections.synchronizedMap(new WeakHashMap<String, Boolean>());
    private static final Map<ExecutionEnvironment, ExecEnvInfo> execEnvInfo =
            Collections.synchronizedMap(new WeakHashMap<ExecutionEnvironment, ExecEnvInfo>());
    private static final String cmd_test = "/bin/test"; // NOI18N


    static {
        NetworkInterface iface = null;
        try {
            for (Enumeration ifaces = NetworkInterface.getNetworkInterfaces();
                    ifaces.hasMoreElements();) {
                iface = (NetworkInterface) ifaces.nextElement();
                for (Enumeration ips = iface.getInetAddresses();
                        ips.hasMoreElements();) {
                    myIPAdresses.add(
                            ((InetAddress) ips.nextElement()).getHostAddress());
                }
            }
        } catch (SocketException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    /**
     * Tests whether a file <tt>fname</tt> exists in <tt>execEnv</tt>.
     * Calling this method equals to calling
     * <pre>
     * fileExists(execEnv, fname, true)
     * </pre>
     * If execEnv referes to remote host that is not connected yet, a
     * <tt>ConnectException</tt> is thrown.
     *
     * @param execEnv <tt>ExecutionEnvironment</tt> to check for file existence
     *        in.
     * @param fname name of file to check for
     * @return <tt>true</tt> if file exists, <tt>false</tt> otherwise.
     *
     * @throws ConnectException if host, identified by this execution
     * environment is not connected.
     */
    public static boolean fileExists(final ExecutionEnvironment execEnv,
            final String fname) throws IOException {
        return fileExists(execEnv, fname, true);
    }

    /**
     * Tests whether a file <tt>fname</tt> exists in <tt>execEnv</tt>.
     * If execEnv referes to remote host that is not connected yet, a
     * <tt>ConnectException</tt> is thrown.
     *
     * @param execEnv <tt>ExecutionEnvironment</tt> to check for file existence
     *        in.
     * @param fname name of file to check for
     * @param useCache if <tt>true</tt> then subsequent tests for same files
     * in the same environment will not be actually performed, but result from
     * hash will be returned.
     * @return <tt>true</tt> if file exists, <tt>false</tt> otherwise.
     * @throws ConnectException if host, identified by this execution
     * environment is not connected.
     */
    public static boolean fileExists(final ExecutionEnvironment execEnv,
            final String fname, final boolean useCache)
            throws IOException {
        String key = execEnv.toString() + fname;

        if (useCache && filesExistenceHash.containsKey(key)) {
            return filesExistenceHash.get(key);
        }

        boolean fileExists = false;

        if (execEnv.isLocal()) {
            fileExists = new File(fname).exists();
        } else {
            if (!ConnectionManager.getInstance().isConnectedTo(execEnv)) {
                throw new ConnectException();
            }

            NativeProcessBuilder npb = new NativeProcessBuilder(
                    execEnv, cmd_test).setArguments("-f", fname); // NOI18N

            try {
                fileExists = npb.call().waitFor() == 0;
            } catch (InterruptedException ex) {
                throw new IOException(ex.getMessage());
            }
        }

        filesExistenceHash.put(key, fileExists);

        return fileExists;
    }

    /**
     * Returns string that identifies OS installed on the host specified by the
     * <tt>execEnv</tt>.
     * For localhost it just returns <tt>System.getProperty("os.name")</tt>,
     * for remote one - the result of <tt>/bin/uname -s</tt> command execution.
     *
     * @param execEnv <tt>ExecutionEnvironment</tt>
     * @return string that identifies OS installed on the host specified by the
     * <tt>execEnv</tt>
     * @throws ConnectException if host, identified by this execution
     * environment is not connected.
     */
    public static String getOS(final ExecutionEnvironment execEnv)
            throws ConnectException {
        ExecEnvInfo info = getHostInfo(execEnv);
        return info.os;
    }

    /**
     * Returns string that identifies platform that <tT>execEnv</tt> is refers
     * to.
     * @param execEnv <tt>ExecutionEnvironment</tt>
     * @return string that identifies platform that <tT>execEnv</tt> is refers
     * to.
     * @throws java.net.ConnectException if host, identified by this execution
     * environment is not connected.
     */
    public static String getPlatform(final ExecutionEnvironment execEnv)
            throws ConnectException {
        ExecEnvInfo info = getHostInfo(execEnv);
        return info.platform;
    }

    /**
     * Returns string that identifies the number of bits in the address space of
     * the native instruction set (32- or 64-bit) on the host that
     * <tt>execEnv</tt> is refers to.
     *
     * @param execEnv <tt>ExecutionEnvironment</tt>
     * @return "32" for 32-bit OS and "64" for 64-bit one
     *
     * @throws java.net.ConnectException if host, identified by this execution
     * environment is not connected.
     */
    public static String getIsaBits(ExecutionEnvironment execEnv)
            throws ConnectException {
        ExecEnvInfo info = getHostInfo(execEnv);
        return info.instructionSet;
    }

    /**
     * Returns true if and only if <tt>host</tt> identifies a localhost.
     *
     * @param host host identification string. Either hostname or IP address.
     * @return true if and only if <tt>host</tt> identifies a localhost.
     */
    public static boolean isLocalhost(String host) {
        boolean result = false;

        try {
            result = myIPAdresses.contains(
                    InetAddress.getByName(host).getHostAddress());
        } catch (UnknownHostException ex) {
        }

        return result;
    }

    public static String getShell(ExecutionEnvironment execEnv)
            throws ConnectException {
        ExecEnvInfo info = getHostInfo(execEnv);
        return info.shell;
    }

    static void updateHostInfo(ExecutionEnvironment execEnv) {
        synchronized (execEnvInfo) {
            if (execEnv.isLocal()) {
                execEnvInfo.put(execEnv, getLocalHostInfo());
            } else {
                execEnvInfo.put(execEnv, getRemoteHostInfo(execEnv));
            }
        }
    }

    private static ExecEnvInfo getHostInfo(ExecutionEnvironment execEnv) throws ConnectException {
        synchronized (execEnvInfo) {
            ExecEnvInfo info = execEnvInfo.get(execEnv);
            if (info == null) {
                updateHostInfo(execEnv);
                info = execEnvInfo.get(execEnv);
            }

            if (info == null) {
                throw new ConnectException("Unable to get host info"); // NOI18N
            }

            return info;
        }
    }

    private static ExecEnvInfo getLocalHostInfo() {
        ExecEnvInfo info = new ExecEnvInfo();
        info.os = System.getProperty("os.name").replaceAll(" ", "_"); // NOI18N
        info.platform = System.getProperty("os.arch"); // NOI18N

        if (Utilities.isWindows()) {
            info.shell = WindowsSupport.getInstance().getShell();
        } else {
            info.shell = "/bin/sh"; // NOI18N
        }

        // IZ#160260 - cannot always relay on sun.cpu.isalist
        String isalist = System.getProperty("sun.cpu.isalist"); // NOI18N

        if ("".equals(isalist)) { // NOI18N
            String testcmd;
            if ("SunOS".equals(info.os)) { // NOI18N
                testcmd = "/usr/bin/isalist | /bin/egrep \"sparcv9|amd64\""; // NOI18N
            } else {
                testcmd = "/bin/uname -a | /bin/egrep x86_64"; // NOI18N
            }

            ProcessBuilder pb = new ProcessBuilder(info.shell, "-c", testcmd); // NOI18N
            try {
                Process testProcess = pb.start();
                int status = testProcess.waitFor();

                info.instructionSet = status == 0 ? "64" : "32"; // NOI18N
            } catch (IOException ex) {
            } catch (InterruptedException ex) {
            }
        } else {
            info.instructionSet = isalist.contains("amd64") ? "64" : "32"; // NOI18N
        }

        String tmpDirBase = System.getProperty("java.io.tmpdir"); // NOI18N

        if (tmpDirBase == null) {
            tmpDirBase = "/var/tmp"; // NOI18N
        }

        File tmpDir = new File(tmpDirBase, "dlight_" + System.getProperty("user.name")); // NOI18N

        if (!tmpDir.exists()) {
            tmpDir.mkdirs();
        }

        boolean fail = false;
        int idx = 0;

        do {
            if (fail) {
                tmpDir = new File(tmpDirBase, "dlight_" + System.getProperty("user.name") + "_" + (++idx)); // NOI18N

                if (!tmpDir.exists()) {
                    if (!tmpDir.mkdirs()) {
                        fail = true;
                        continue;
                    }
                }

            }

            fail = false;
            File testFile = new File(tmpDir, "test"); // NOI18N

            if (testFile.exists()) {
                if (!testFile.delete()) {
                    fail = true;
                    continue;
                }
            }

            try {
                if (testFile.createNewFile()) {
                    testFile.delete();
                } else {
                    fail = true;
                }
            } catch (IOException ex) {
                fail = true;
            }

        } while (fail);

        info.tmpDirBase = tmpDir.getAbsolutePath();

        return info;
    }

    private static ExecEnvInfo getRemoteHostInfo(final ExecutionEnvironment execEnv) {
        final ConnectionManager cm = ConnectionManager.getInstance();
        final Session session = ConnectionManagerAccessor.getDefault().
                getConnectionSession(cm, execEnv, true);

        if (session == null) {
            return null;
        }

        ChannelExec echannel = null;
        ExecEnvInfo info = new ExecEnvInfo();

        try {
            StringBuilder command = new StringBuilder();

            command.append("U=`ls /bin/uname 2>/dev/null || ls /usr/bin/uname 2>/dev/null` &&"); // NOI18N
            command.append("O=`$U -s` && /bin/echo $O &&"); // NOI18N
            command.append("P=`$U -p` && test 'unknown' = $P && $U -m || echo $P &&"); // NOI18N
            command.append("test 'SunOS' = $O && /bin/isainfo -b || $U -a | grep x86_64 || echo 32 &&"); // NOI18N
            command.append("/bin/ls /bin/sh 2>/dev/null || /bin/ls /usr/bin/sh 2>/dev/null"); // NOI18N

            synchronized (session) {
                echannel = (ChannelExec) session.openChannel("exec"); // NOI18N
                echannel.setCommand(command.toString());
                echannel.connect();
            }

            InputStream out = echannel.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(out));
            String str;
            int lineno = 0;
            while ((str = reader.readLine()) != null) {
                switch (lineno) {
                    case 0:
                        String uname_s = str.trim();
                        if (uname_s.contains("_NT-")) { // NOI18N catches Cygwin and MinGW
                            info.os = "Windows"; // NOI18N
                        } else if (uname_s.equals("Darwin")) { // NOI18N
                            info.os = "Mac_OS_X"; // NOI18N
                        } else {
                            info.os = uname_s;
                        }
                        break;
                    case 1:
                        info.platform = str.trim().toLowerCase();
                        break;
                    case 2:
                        info.instructionSet = str.trim().toLowerCase();
                        break;
                    case 3:
                        info.shell = str.trim().toLowerCase();
                }
                lineno++;
            }

            echannel.getExitStatus();
        } catch (JSchException ex) {
            Exceptions.printStackTrace(ex);
            return null;
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            return null;
        } finally {
            if (echannel != null) {
                echannel.disconnect();
            }
        }

        // find-out tmpdir
        String tmpDirBase = "/var/tmp/dlight_" + execEnv.getUser() + "/"; // NOI18N
        int idx = 0;

        try {
            while (true) {
                synchronized (session) {
                    echannel = (ChannelExec) session.openChannel("exec"); // NOI18N
                    echannel.setCommand("/bin/mkdir -p " + tmpDirBase + " && test -w " + tmpDirBase); // NOI18N
                    echannel.connect();
                }

                // Wait result
                while (echannel.isConnected()) {
                    Thread.sleep(50);
                }

                if (echannel.getExitStatus() == 0) {
                    break;
                }

                tmpDirBase = "/var/tmp/dlight_" + execEnv.getUser() + "_" + (++idx) + "/"; // NOI18N
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return null;
        } catch (JSchException ex) {
            return null;
        } finally {
            if (echannel != null) {
                echannel.disconnect();
            }
        }

        info.tmpDirBase = tmpDirBase;

        return info;
    }

    public static String getTempDir(ExecutionEnvironment execEnv) throws ConnectException {
        ExecEnvInfo info = getHostInfo(execEnv);
        return info.tmpDirBase;
    }

    private static class ExecEnvInfo {

        String os;
        String platform;
        String instructionSet;
        String shell;
        String tmpDirBase;

        @Override
        public String toString() {
            return "os = " + os + "; platform = " + platform + "; instructionSet = " + instructionSet; // NOI18N
        }
    }
}
