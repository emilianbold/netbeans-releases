package org.netbeans.modules.nativeexecution.util;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.openide.util.Exceptions;

/**
 * Utility class that provides information about particual host.
 */
public final class HostInfoUtils {

    /**
     * String constant that can be used to identify a localhost.
     */
    public static final String LOCALHOST = "127.0.0.1"; // NOI18N
    private static List<String> myIPAdresses = new ArrayList<String>();
    private static Map<String, Boolean> filesExistenceHash =
            Collections.synchronizedMap(new WeakHashMap<String, Boolean>());
    private static Map<ExecutionEnvironment, HostInfo> hostInfo =
            Collections.synchronizedMap(new WeakHashMap<ExecutionEnvironment, HostInfo>());
    private static final String cmd_uname = "/bin/uname"; // NOI18N
    private static final String cmd_sh = "/bin/sh"; // NOI18N
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
     * <tt>HostNotConnectedException</tt> is thrown.
     *
     * @param execEnv <tt>ExecutionEnvironment</tt> to check for file existence
     *        in.
     * @param fname name of file to check for
     * @return <tt>true</tt> if file exists, <tt>false</tt> otherwise.
     *
     * @throws HostNotConnectedException if host, identified by this execution
     * environment is not connected.
     */
    public static boolean fileExists(final ExecutionEnvironment execEnv,
            final String fname) throws HostNotConnectedException {
        return fileExists(execEnv, fname, true);
    }

    /**
     * Tests whether a file <tt>fname</tt> exists in <tt>execEnv</tt>.
     * If execEnv referes to remote host that is not connected yet, a
     * <tt>HostNotConnectedException</tt> is thrown.
     *
     * @param execEnv <tt>ExecutionEnvironment</tt> to check for file existence
     *        in.
     * @param fname name of file to check for
     * @param useCache if <tt>true</tt> then subsequent tests for same files
     * in the same environment will not be actually performed, but result from
     * hash will be returned.
     * @return <tt>true</tt> if file exists, <tt>false</tt> otherwise.
     * @throws HostNotConnectedException if host, identified by this execution
     * environment is not connected.
     */
    public static boolean fileExists(final ExecutionEnvironment execEnv,
            final String fname, final boolean useCache)
            throws HostNotConnectedException {
        String key = execEnv.toString() + fname;

        if (useCache && filesExistenceHash.containsKey(key)) {
            return filesExistenceHash.get(key);
        }

        boolean fileExists = false;

        if (execEnv.isLocal()) {
            fileExists = new File(fname).exists();
        } else {
            if (ConnectionManager.getInstance().isConnectedTo(execEnv)) {
                throw new HostNotConnectedException();
            }

            NativeProcessBuilder npb = new NativeProcessBuilder(
                    execEnv, cmd_test).setArguments("-f", fname);

            try {
                fileExists = npb.call().waitFor() == 0;
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
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
     * @throws HostNotConnectedException if host, identified by this execution
     * environment is not connected.
     */
    public static String getOS(final ExecutionEnvironment execEnv)
            throws HostNotConnectedException {
        HostInfo info = getHostInfo(execEnv);
        return info.os;
    }

    public static String getPlatform(final ExecutionEnvironment execEnv)
            throws HostNotConnectedException {
        HostInfo info = getHostInfo(execEnv);
        return info.platform;
    }

    public static String getIsaInfo(ExecutionEnvironment execEnv)
            throws HostNotConnectedException {
        HostInfo info = getHostInfo(execEnv);
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

    static synchronized void updateHostInfo(ExecutionEnvironment execEnv) {
        if (execEnv.isLocal()) {
            hostInfo.put(execEnv, getLocalHostInfo());
        } else {
            Session session =
                    ConnectionManager.getInstance().getConnectionSession(execEnv);
            hostInfo.put(execEnv, getRemoteHostInfo(session));
        }
    }

    private static HostInfo getHostInfo(ExecutionEnvironment execEnv) throws HostNotConnectedException {
        HostInfo info = hostInfo.get(execEnv);
        if (info == null) {
            if (execEnv.isRemote()) {
                throw new HostNotConnectedException();
            }

            updateHostInfo(execEnv);
            info = hostInfo.get(execEnv);
        }

        return info;
    }

    private static HostInfo getLocalHostInfo() {
        HostInfo info = new HostInfo();
        info.os = System.getProperty("os.name");
        info.platform = System.getProperty("os.arch");
        info.instructionSet = System.getProperty("sun.cpu.isalist").contains("amd64") ? "64" : "32";
        return info;
    }

    private static HostInfo getRemoteHostInfo(Session session) {
        ChannelExec echannel = null;
        StringBuilder command = new StringBuilder();

        command.append("U=/bin/uname &&"); // NOI18N
        command.append("O=`$U -s` && /bin/echo $O &&"); // NOI18N
        command.append("P=`$U -p` && test 'unknown' = $P && $U -m || echo $P &&"); // NOI18N
        command.append("test 'SunOS' = $O && /bin/isainfo -b || $U -a | grep x86_64 || echo 32"); // NOI18N

        try {
            echannel = (ChannelExec) session.openChannel("exec"); // NOI18N
            echannel.setCommand(command.toString());
            echannel.connect();
        } catch (JSchException ex) {
            Exceptions.printStackTrace(ex);
        }

        HostInfo info = new HostInfo();

        try {
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
                }
                lineno++;
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

        return info;
    }

    /**
     * Tests whether the OS, that is ran in this execution environment, is Unix
     * or not.
     * @param execEnv <tt>ExecutionEnvironment</tt> to test
     * @return true if execEnv refers to a host that runs Solaris or Linux.
     * @throws HostNotConnectedException if host is not connected yet.
     */
//    public static boolean isUnix(ExecutionEnvironment execEnv)
//            throws HostNotConnectedException {
//        String os = getOS(execEnv);
//        return "SunOS".equals(os) || "Linux".equals(os); // NOI18N
//    }
    private static class HostInfo {

        String os;
        String platform;
        String instructionSet;

        @Override
        public String toString() {
            return "os = " + os + "; platform = " + platform + "; instructionSet = " + instructionSet; // NOI18N
        }
    }
}
