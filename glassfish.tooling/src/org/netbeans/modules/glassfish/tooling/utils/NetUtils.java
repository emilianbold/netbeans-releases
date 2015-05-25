/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * https://java.net/projects/gf-tooling/pages/License or LICENSE.TXT.
 * See the License for the specific language governing permissions
 * and limitations under the License.  When distributing the software,
 * include this License Header Notice in each file and include the License
 * file at LICENSE.TXT. Oracle designates this particular file as subject
 * to the "Classpath" exception as provided by Oracle in the GPL Version 2
 * section of the License file that accompanied this code. If applicable,
 * add the following below the License Header, with the fields enclosed
 * by brackets [] replaced by your own identifying information:
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
package org.netbeans.modules.glassfish.tooling.utils;

import java.io.IOException;
import java.net.ConnectException;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import org.netbeans.modules.glassfish.tooling.GlassFishIdeException;
import org.netbeans.modules.glassfish.tooling.logging.Logger;

/**
 * Networking utilities
 * <p/>
 * @author Tomas Kraus
 */
public class NetUtils {

    ////////////////////////////////////////////////////////////////////////////
    // Inner classes                                                          //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Comparator for {@link InetAddress} instances to be sorted.
     */
    public static class InetAddressComparator
            implements Comparator<InetAddress> {

        /**
         * Compares values of <code>InetAddr</code> instances.
         * <p/>
         * @param ip1 First <code>InetAddr</code> instance to be compared.
         * @param ip2 Second <code>InetAddr</code> instance to be compared.
         * @return A negative integer, zero, or a positive integer as the first
         *         argument is less than, equal to, or greater than the second.
         */
        @Override
        public int compare(final InetAddress ip1, final InetAddress ip2) {
            byte[] addr1 = ip1.getAddress();
            byte[] addr2 = ip2.getAddress();
            int result = addr2.length - addr1.length;
            if (result == 0) {
                for (int i = 0; result == 0 && i < addr1.length; i++) {
                    result = addr1[i] - addr2[i];
                }
            }
            return result;
        }
        
    }

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /** Logger instance for this class. */
    private static final Logger LOGGER = new Logger(ServerUtils.class);

    /** Port check timeout [ms]. */
    public static final int PORT_CHECK_TIMEOUT = 2000;

    /** This is the test query used to ping the server in an attempt to
     *  determine if it is secure or not. */
    private static byte [] TEST_QUERY = new byte [] {
        // The following SSL query is from nmap (http://www.insecure.org)
        // This HTTPS request should work for most (all?) HTTPS servers
        (byte)0x16, (byte)0x03, (byte)0x00, (byte)0x00, (byte) 'S', (byte)0x01,
        (byte)0x00, (byte)0x00, (byte) 'O', (byte)0x03, (byte)0x00, (byte) '?',
        (byte) 'G', (byte)0xd7, (byte)0xf7, (byte)0xba, (byte) ',', (byte)0xee,
        (byte)0xea, (byte)0xb2, (byte) '`', (byte) '~', (byte)0xf3, (byte)0x00,
        (byte)0xfd, (byte)0x82, (byte) '{', (byte)0xb9, (byte)0xd5, (byte)0x96,
        (byte)0xc8, (byte) 'w', (byte)0x9b, (byte)0xe6, (byte)0xc4, (byte)0xdb,
        (byte) '<', (byte) '=', (byte)0xdb, (byte) 'o', (byte)0xef, (byte)0x10,
        (byte) 'n', (byte)0x00, (byte)0x00, (byte) '(', (byte)0x00, (byte)0x16,
        (byte)0x00, (byte)0x13, (byte)0x00, (byte)0x0a, (byte)0x00, (byte) 'f',
        (byte)0x00, (byte)0x05, (byte)0x00, (byte)0x04, (byte)0x00, (byte) 'e',
        (byte)0x00, (byte) 'd', (byte)0x00, (byte) 'c', (byte)0x00, (byte) 'b',
        (byte)0x00, (byte) 'a', (byte)0x00, (byte) '`', (byte)0x00, (byte)0x15,
        (byte)0x00, (byte)0x12, (byte)0x00, (byte)0x09, (byte)0x00, (byte)0x14,
        (byte)0x00, (byte)0x11, (byte)0x00, (byte)0x08, (byte)0x00, (byte)0x06,
        (byte)0x00, (byte)0x03, (byte)0x01, (byte)0x00,
        // The following is a HTTP request, some HTTP servers won't
        // respond unless the following is also sent
        (byte) 'G', (byte) 'E', (byte) 'T', (byte) ' ', (byte) '/',
        // change the detector to request something that the monitor knows to filter
        //  out.  This will work-around 109891. Use the longest filtered prefix to
        //  avoid false positives....
        (byte) 'c', (byte) 'o', (byte) 'm', (byte) '_', (byte) 's', (byte) 'u',
        (byte) 'n', (byte) '_', (byte) 'w', (byte) 'e', (byte) 'b', (byte) '_',
        (byte) 'u', (byte) 'i',
        (byte) ' ',
        (byte) 'H', (byte) 'T', (byte) 'T', (byte) 'P', (byte) '/', (byte) '1',
        (byte) '.', (byte) '0', (byte)'\n', (byte)'\n'
    };

    /** Comparator for {@link InetAddress} instances to be sorted. */
    private static final InetAddressComparator INET_ADDRESS_COMPARATOR
            = new InetAddressComparator();

    ////////////////////////////////////////////////////////////////////////////
    // Static methods                                                         //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Finds out if server is running on remote host by connecting to remote
     * host and port.
     * <p/>
     * @param host Server host.
     * @param port Server port.
     * @param timeout Network connection timeout [ms].
     * @return Returns <code>true</code> when server port is accepting
     *         connections or <code>false</code> otherwise.
     */
    public static boolean isPortListeningRemote(final String host,
            final int port, final int timeout) {
        final String METHOD = "isPortListeningRemote";
        if (null == host) {
            return false;
        }
        Socket socket = null;
        try {
            socket = new Socket();
            socket.connect(new InetSocketAddress(host, port), timeout);
            return true;
        } catch (IOException ex) {
            return false;
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException ioe) {
                    LOGGER.log(Level.INFO, METHOD,
                            "closeError", ioe.getLocalizedMessage());
                }
            }
        }
    }

    /**
     * Finds out if server is running on remote host by connecting to remote
     * host and port.
     * <p/>
     * @param host Server host.
     * @param port Server port.
     * @return Returns <code>true</code> when server port is accepting
     *         connections or <code>false</code> otherwise.
     */
    public static boolean isPortListeningRemote(final String host,
            final int port) {
        return isPortListeningRemote(host, port, 0);
    }

    /**
     * Finds out if server is running on local host by binding to local port.
     * <p/>
     * @param host Server host or <code>null</code> value for address of the
     *             loopback interface. 
     * @param port Server port.
     * @return Returns <code>true</code> when server port is accepting
     *         connections or <code>false</code> otherwise.
     */
    public static boolean isPortListeningLocal(final String host,
            final int port) {
        final String METHOD = "isPortListeningLocal";
        ServerSocket socket = null;
        try {
            InetAddress ia = InetAddress.getByName(host);
            socket = new ServerSocket(port, 1, ia);
            return false;
        } catch (IOException ioe) {
            return true;
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException ioe) {
                    LOGGER.log(Level.INFO, METHOD,
                            "closeError", ioe.getLocalizedMessage());
                }
            }
        }
    }

    /**
     * Determine whether an HTTP listener is secure or not..
     * <p/>
     * This method accepts a host name and port #.  It uses this information
     * to attempt to connect to the port, send a test query, analyze the
     * result to determine if the port is secure or is not secure (currently
     * only HTTP / HTTPS is supported).
     * it might emit a warning in the server log for GlassFish cases.
     * No Harm, just an annoying warning, so we need to use this call only
     * when really needed.
     * <p/>
     * @param hostname The host for the HTTP listener.
     * @param port     The port for the HTTP listener.
     * @throws IOException
     * @throws SocketTimeoutException
     * @throws ConnectException
     */
    public static boolean isSecurePort(String hostname, int port)
            throws IOException, ConnectException, SocketTimeoutException {
        return isSecurePort(hostname,port, 0);
    }

    /**
     * Determine whether an HTTP listener is secure or not..
     * <p/>
     * This method accepts a host name and port #.  It uses this information
     * to attempt to connect to the port, send a test query, analyze the
     * result to determine if the port is secure or is not secure (currently
     * only HTTP / HTTPS is supported).
     * it might emit a warning in the server log for GlassFish cases.
     * No Harm, just an annoying warning, so we need to use this call only
     * when really needed.
     * <p/>
     * @param hostname The host for the HTTP listener.
     * @param port     The port for the HTTP listener.
     * @param depth     Method calling depth.
     * @throws IOException
     * @throws SocketTimeoutException
     * @throws ConnectException
     */
    private static boolean isSecurePort(String hostname, int port, int depth) 
            throws IOException, ConnectException, SocketTimeoutException {
        final String METHOD = "isSecurePort";
        boolean isSecure;
        try (Socket socket = new Socket()) {
            try {
                LOGGER.log(Level.FINE, METHOD, "socket");
                socket.connect(new InetSocketAddress(hostname, port), PORT_CHECK_TIMEOUT);
                socket.setSoTimeout(PORT_CHECK_TIMEOUT);
            // This could be bug 70020 due to SOCKs proxy not having localhost
            } catch (SocketException ex) {
                String socksNonProxyHosts = System.getProperty("socksNonProxyHosts");
                if(socksNonProxyHosts != null && socksNonProxyHosts.indexOf("localhost") < 0) {
                    String localhost = socksNonProxyHosts.length() > 0 ? "|localhost" : "localhost";
                    System.setProperty("socksNonProxyHosts",  socksNonProxyHosts + localhost);
                    if (depth < 1) {
                        socket.close();
                        return isSecurePort(hostname,port,1);
                    } else {
                        socket.close();
                        ConnectException ce = new ConnectException();
                        ce.initCause(ex);
                        throw ce; //status unknow at this point
                        //next call, we'll be ok and it will really detect if we are secure or not
                    }
                }
            }
            java.io.OutputStream ostream = socket.getOutputStream();
            ostream.write(TEST_QUERY);
            java.io.InputStream istream = socket.getInputStream();
            byte[] input = new byte[8192];
            istream.read(input);
            String response = new String(input).toLowerCase(Locale.ENGLISH);
            isSecure = true;
            if (response.length() == 0) {
                //isSecure = false;
                // Close the socket
                socket.close();
                throw new ConnectException();
            } else if (response.startsWith("http/1.1 302 moved temporarily")) {
                // 3.1 has started to use redirects... but 3.0 is still using the older strategies...
                isSecure = true;
            } else if (response.startsWith("http/1.")) {
                isSecure = false;
            } else if (response.indexOf("<html") != -1) {
                isSecure = false;
            } else if (response.indexOf("</html") != -1) {
                // New test added to resolve 106245
                // when the user has the IDE use a proxy (like webcache.foo.bar.com),
                // the response comes back as "d><title>....</html>".  It looks like
                // something eats the "<html><hea" off the front of the data that
                // gets returned.
                //
                // This test makes an allowance for that behavior. I figure testing
                // the likely "last bit" is better than testing a bit that is close
                // to the data that seems to get eaten.
                //
                isSecure = false;
            } else if (response.indexOf("connection: ") != -1) {
                isSecure = false;
            }
        }
        return isSecure;
    }

    /**
     * Retrieve {@link Set} of IP addresses of this host.
     * <p/>
     * @return {@link Set} of IP addresses of this host.
     * @throws GlassFishIdeException if addresses of this host could not
     *         be retrieved.
     */
    public static Set<InetAddress> getHostIPs() {
        final String METHOD = "getHostIPs";
        Set<InetAddress> addrs = new TreeSet<>(INET_ADDRESS_COMPARATOR);
        try {
            Enumeration<NetworkInterface> ifaces = NetworkInterface.
                    getNetworkInterfaces();
            while (ifaces.hasMoreElements()) {
                NetworkInterface iface = ifaces.nextElement();
                for (Enumeration<InetAddress> e = iface.getInetAddresses(); e.hasMoreElements(); ) {
                    InetAddress a = e.nextElement();
                    addrs.add(a);
                }
            }
        } catch (SocketException se) {
            addrs = null;
            throw new GlassFishIdeException(LOGGER.excMsg(METHOD, "exception"));
        }
        return addrs;
    }

    /**
     * Retrieve {@link Set} of IPv4 addresses of this host.
     * <p/>
     * @return {@link Set} of IPv4 addresses of this host.
     */
    public static Set<Inet4Address> getHostIP4s() {
        Set<Inet4Address> addrs = new TreeSet<>(INET_ADDRESS_COMPARATOR);
        for (InetAddress a : getHostIPs()) {
            if (a instanceof Inet4Address) {
                addrs.add((Inet4Address) a);
            }
        }
        return addrs;
    }

    /**
     * Retrieve {@link Set} of IPv6 addresses of this host.
     * <p/>
     * @return {@link Set} of IPv6 addresses of this host.
     */
    public static Set<Inet6Address> getHostIP6s() {
        Set<Inet6Address> addrs = new TreeSet<>(INET_ADDRESS_COMPARATOR);
        for (InetAddress a : getHostIPs()) {
            if (a instanceof Inet6Address) {
                addrs.add((Inet6Address) a);
            }
        }
        return addrs;
    }

}
