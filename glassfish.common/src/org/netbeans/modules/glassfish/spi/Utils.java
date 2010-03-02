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

package org.netbeans.modules.glassfish.spi;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author vbk
 */
public class Utils {

    /**
     * A canWrite test that may tell the truth on Windows.
     *
     * This is a work around for http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4420020
     * @param f the file or directory to test
     * @return true when the file is writable...
     */
    public static boolean canWrite(File f) {
        if (org.openide.util.Utilities.isWindows()) {
            // File.canWrite() has lots of bogus return cases associated with
            // read-only directories and files...
            boolean retVal = true;
            java.io.File tmp = null;
            if (!f.exists()) {
                retVal = false;
            } else if (f.isDirectory()) {
                try             {
                    tmp = java.io.File.createTempFile("foo", ".tmp", f);
                }
                catch (IOException ex) {
                    // I hate using exceptions for flow of control
                    retVal = false;
                } finally {
                    if (null != tmp) {
                        tmp.delete();
                    }
                }
            } else {
                java.io.FileOutputStream fos = null;
                try {
                    fos = new java.io.FileOutputStream(f, true);
                }
                catch (FileNotFoundException ex) {
                    // I hate using exceptions for flow of control
                    retVal = false;
                } finally {
                    if (null != fos) {
                        try {
                            fos.close();
                        } catch (java.io.IOException ioe) {
                            Logger.getLogger(Utils.class.getName()).log(Level.FINEST,
                                    null, ioe);
                        }
                    }
                }
            }
            return retVal;
        } else {
            // we can actually trust the canWrite() implementation...
            return f.canWrite();
        }
    }

    public static final String VERSIONED_JAR_SUFFIX_MATCHER = "(?:-[0-9]+(?:\\.[0-9]+(?:_[0-9]+|)|).*|).jar"; // NOI18N

    /**
     *
     * @param jarNamePattern the name pattern to search for
     * @param modulesDir the place to look for the pattern
     * @return the jar file that matches the pattern, null otherwise.
     *
     * @since 1.5
     */
    public static File getFileFromPattern(String jarNamePattern, File modulesDir) {
        // if asserts are on... blame the caller
        assert jarNamePattern != null : "jarNamePattern should not be null";
        // if this is in production, asserts are off and we should handle this a bit more gracefully
        if (null == jarNamePattern) {
            // and log an error message
            Logger.getLogger("glassfish").log(Level.INFO, "caller passed invalid jarNamePattern",
                    new NullPointerException("jarNamePattern"));
            return null;
        }

        // if asserts are on... blame the caller
        assert modulesDir != null : "modulesDir  should not be null";
        // if this is in production, asserts are off and we should handle this a bit more gracefully
        if (null == modulesDir) {
            // and log an error message
            Logger.getLogger("glassfish").log(Level.INFO, "caller passed invalid param",
                    new NullPointerException("modulesDir"));
            return null;
        }

        int subindex = jarNamePattern.lastIndexOf("/");
        if (subindex != -1) {
            String subdir = jarNamePattern.substring(0, subindex);
            jarNamePattern = jarNamePattern.substring(subindex + 1);
            modulesDir = new File(modulesDir, subdir);
        }
        if (modulesDir.canRead() && modulesDir.isDirectory()) {
            File[] candidates = modulesDir.listFiles(new VersionFilter(jarNamePattern));
            if (candidates != null && candidates.length > 0) {
                return candidates[0]; // the first one
            }
        }
        return null;
    }

        private static class VersionFilter implements FileFilter {

        private final Pattern pattern;

        public VersionFilter(String namePattern) {
            pattern = Pattern.compile(namePattern);
        }

        public boolean accept(File file) {
            return pattern.matcher(file.getName()).matches();
        }

    }

    public static String sanitizeName(String name) {
        if (null == name || name.matches("[\\p{L}\\p{N}_][\\p{L}\\p{N}\\-_./;#]*")) {
            return name;
        }
        // the string is bad...
        return "_" + name.replaceAll("[^\\p{L}\\p{N}\\-_./;#]", "_");
    }

    /**
     * Add escape characters for backslash and dollar sign characters in
     * path field.
     *
     * @param path file path in string form.
     * @return adjusted path with backslashes and dollar signs escaped with
     *   backslash character.
     */
    public static final String escapePath(String path) {
        return path.replace("\\", "\\\\").replace("$", "\\$"); // NOI18N
    }

    /**
     * identify the http/https protocol designator for a port
     *
     */
    public static String getHttpListenerProtocol(String hostname, String port) {
        String retVal = "http";
        try {
            retVal = getHttpListenerProtocol(hostname, Integer.parseInt(port));
        } catch (NumberFormatException nfe) {
            Logger.getLogger("glassfish").log(Level.INFO, "returning http due to exception", nfe);
        }
        return retVal;
    }

    /**
     * identify the http/https protocol designator for a port
     *
     */
    public static String getHttpListenerProtocol(String hostname, int port) {
        String retVal = "http";
        try {
            if (isSecurePort(hostname, port)) {
                retVal = "https";
            }
        } catch (ConnectException ex) {
            Logger.getLogger("glassfish").log(Level.INFO, null, ex);
        } catch (SocketException ex) {
            Logger.getLogger("glassfish").log(Level.FINE, null, ex);
        } catch (SocketTimeoutException ex) {
            Logger.getLogger("glassfish").log(Level.INFO, null, ex);
        } catch (IOException ex) {
            Logger.getLogger("glassfish").log(Level.INFO, null, ex);
        }
        return retVal;
    }

    private static final int PORT_CHECK_TIMEOUT = 4000; // Port check timeout in ms

    /**
     * Determine whether an http listener is secure or not..
     *
     *  This method accepts a hostname and port #.  It uses this information
     *  to attempt to connect to the port, send a test query, analyze the
     *  result to determine if the port is secure or unsecure (currently only
     *  http / https is supported).
     * it might emit a warning in the server log for GlassFish cases
     * No Harm, just an annoying warning, so we need to use this call only when really needed
     *
     * @param hostname the host for the http-listener
     * @param port the port for the http-listener
     * @throws IOException
     * @throws SocketTimeoutException
     * @throws ConnectException
     */
    public static boolean isSecurePort(String hostname, int port)
            throws IOException, ConnectException, SocketTimeoutException {
        return isSecurePort(hostname,port, 0);
    }

    private static boolean isSecurePort(String hostname, int port, int depth)
            throws IOException, ConnectException, SocketTimeoutException {
        // Open the socket with a short timeout for connects and reads.
        Socket socket = new Socket();
        try {
            socket.connect(new InetSocketAddress(hostname, port), PORT_CHECK_TIMEOUT);
            socket.setSoTimeout(PORT_CHECK_TIMEOUT);
        } catch(SocketException ex) { // this could be bug 70020 due to SOCKs proxy not having localhost
            String socksNonProxyHosts = System.getProperty("socksNonProxyHosts");
            if(socksNonProxyHosts != null && socksNonProxyHosts.indexOf("localhost") < 0) {
                String localhost = socksNonProxyHosts.length() > 0 ? "|localhost" : "localhost";
                System.setProperty("socksNonProxyHosts",  socksNonProxyHosts + localhost);
                if (depth < 1) {
                    return isSecurePort(hostname,port,1);
                } else {

                ConnectException ce = new ConnectException();
                ce.initCause(ex);
                throw ce; //status unknow at this point
                //next call, we'll be ok and it will really detect if we are secure or not
                }
            }
        }

        // Send an https query (w/ trailing http query)
        java.io.OutputStream ostream = socket.getOutputStream();
        ostream.write(TEST_QUERY);

        // Get the result
        java.io.InputStream istream = socket.getInputStream();
        byte[] input = new byte[8192];
        istream.read(input);

        // Close the socket
        socket.close();

        // Determine protocol from result
        // Can't read https response w/ OpenSSL (or equiv), so use as
        // default & try to detect an http response.
        String response = new String(input).toLowerCase(Locale.ENGLISH);
        boolean isSecure = true;
        if (response.length() == 0) {
            //isSecure = false;
            throw new ConnectException();
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
        return isSecure;
    }

    /**
     *  This is the test query used to ping the server in an attempt to
     *  determine if it is secure or not.
     */
    private static byte [] TEST_QUERY = new byte [] {
        // The following SSL query is from nmap (http://www.insecure.org)
        // This HTTPS request should work for most (all?) https servers
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

    public static void doCopy(FileObject from, FileObject toParent) throws IOException {
        if (null != from) {
            if (from.isFolder()) {
                //FileObject copy = toParent.getF
                FileObject copy = FileUtil.createFolder(toParent,from.getNameExt());
                FileObject[] kids = from.getChildren();
                for (int i = 0; i < kids.length; i++) {
                    doCopy(kids[i], copy);
                }
            } else {
                assert from.isData();
                FileObject target = toParent.getFileObject(from.getName(),from.getExt());
                if (null == target) {
                    FileUtil.copyFile(from, toParent, from.getName(), from.getExt());
                }
            }
        }
    }
}
