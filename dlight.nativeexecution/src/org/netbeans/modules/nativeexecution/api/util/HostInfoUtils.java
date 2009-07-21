package org.netbeans.modules.nativeexecution.api.util;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
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
import java.util.concurrent.CancellationException;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.support.hostinfo.FetchHostInfoTask;
import org.netbeans.modules.nativeexecution.support.filesearch.FileSearchParams;
import org.netbeans.modules.nativeexecution.support.filesearch.FileSearchTask;
import org.netbeans.modules.nativeexecution.support.Logger;
import org.netbeans.modules.nativeexecution.support.TasksCachedProcessor;
import org.openide.util.Exceptions;

/**
 * Utility class that provides information about particual host.
 */
public final class HostInfoUtils {

    /**
     * String constant that can be used to identify a localhost.
     */
    public static final String LOCALHOST = "127.0.0.1"; // NOI18N
    private static final java.util.logging.Logger log = Logger.getInstance();
    private static final List<String> myIPAdresses = new ArrayList<String>();
    private static final Map<String, Boolean> filesExistenceHash =
            Collections.synchronizedMap(new WeakHashMap<String, Boolean>());
    private static final TasksCachedProcessor<ExecutionEnvironment, HostInfo> hostInfoCachedProcessor =
            new TasksCachedProcessor<ExecutionEnvironment, HostInfo>(new FetchHostInfoTask(), false);


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
     * Utility method that dumps HostInfo to specified stream
     * @param hostinfo hostinfo that should be dumped
     * @param stream stream to dump to
     */
    public static void dumpInfo(HostInfo hostinfo, PrintStream stream) {
        stream.println("------------"); // NOI18N
        if (hostinfo == null) {
            stream.println("HostInfo is NULL"); // NOI18N
        } else {
            stream.println("Hostname      : " + hostinfo.getHostname()); // NOI18N
            stream.println("OS Family     : " + hostinfo.getOSFamily()); // NOI18N
            stream.println("OS            : " + hostinfo.getOS().getName()); // NOI18N
            stream.println("OS Version    : " + hostinfo.getOS().getVersion()); // NOI18N
            stream.println("OS Bitness    : " + hostinfo.getOS().getBitness()); // NOI18N
            stream.println("CPU Family    : " + hostinfo.getCpuFamily()); // NOI18N
            stream.println("CPU #         : " + hostinfo.getCpuNum()); // NOI18N
            stream.println("shell to use  : " + hostinfo.getShell()); // NOI18N
            stream.println("tmpdir to use : " + hostinfo.getTempDir()); // NOI18N
            stream.println("tmpdir (file) to use : " + hostinfo.getTempDirFile().toString()); // NOI18N
        }
        stream.println("------------"); // NOI18N
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

            NativeProcessBuilder npb = NativeProcessBuilder.newProcessBuilder(execEnv);
            npb.setExecutable("test").setArguments("-e", fname); // NOI18N

            try {
                fileExists = npb.call().waitFor() == 0;
            } catch (InterruptedException ex) {
                throw new IOException(ex.getMessage());
            }
        }

        filesExistenceHash.put(key, fileExists);

        return fileExists;
    }

    public static String searchFile(ExecutionEnvironment execEnv,
            List<String> searchPaths, String file, boolean searchInUserPaths) {

        FileSearchParams fileSearchParams = new FileSearchParams(execEnv,
                searchPaths, file, searchInUserPaths);

        String result = null;

        try {
            result = new FileSearchTask().compute(fileSearchParams);
        } catch (InterruptedException ex) {
        }

        return result;
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

    /**
     * Tests whether host info has been already fetched for the particular
     * execution environment.
     *
     * @param execEnv environment to perform test against
     * @return <tt>true</tt> if info is available and getHostInfo() could be
     * called without a risk to be blocked for a significant time.
     * <tt>false</tt> otherwise.
     */
    public static boolean isHostInfoAvailable(final ExecutionEnvironment execEnv) {
        return hostInfoCachedProcessor.isResultAvailable(execEnv);
    }

    /**
     * Returns <tt>HostInfo</tt> with information about the host identified
     * by <tt>execEnv</tt>. Invocation of this method may block current thread
     * for rather significant amount of time or can even initiate UI-user
     * interraction. This happens when execEnv represents remote host and no
     * active connection to that host is available.
     * An attempt to establish new connection will be performed. This may initiate
     * password prompt.
     *
     * One should avoid to call this method from within AWT thread without prior 
     * call to isHostInfoAvailable().
     *
     * @param execEnv execution environment to get information about
     * @return information about the host represented by execEnv. <tt>null</tt>
     * if interrupted of connection initiation is cancelled by user.
     * Also returns null if execEnv parameter is null.
     * @see #isHostInfoAvailable(org.netbeans.modules.nativeexecution.api.ExecutionEnvironment)
     */
    public static HostInfo getHostInfo(final ExecutionEnvironment execEnv) throws IOException, CancellationException {
        if (execEnv == null) {
            return null;
        }

        try {
            return hostInfoCachedProcessor.compute(execEnv);
        } catch (InterruptedException ex) {
            throw new CancellationException("getHostInfo interrupted"); // NOI18N
        }
    }

    /**
     * For testing purposes only!
     */
    protected static void resetHostsData() {
        hostInfoCachedProcessor.resetCache();
    }
}
