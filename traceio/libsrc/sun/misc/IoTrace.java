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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */

package sun.misc;

/**
 *
 * @author Tomas Hurka
 */
import java.net.InetAddress;
import org.netbeans.lib.traceio.agent.TraceIO;

/**
 * Utility class used to identify trace points for I/O calls.
 * <p>
 * To use this class, a diagnostic tool must redefine this class with a version
 * that contains calls to the the diagnostic tool. This implementation will then
 * receive callbacks when file and socket operations are performed. The reason
 * for requiring a redefine of the class is to avoid any overhead caused by the
 * instrumentation.
 * <p>
 * The xxBegin() methods return a "context". This can be any Object. This
 * context will be passed to the corresponding xxEnd() method. This way, an
 * implementation can correlate the beginning of an operation with the end.
 * <p>
 * It is possible for a xxEnd() method to be called with a null handle. This
 * happens if tracing was started between the call to xxBegin() and xxEnd(), in
 * which case xxBegin() would not have been called. It is the implementation's
 * responsibility to not throw an exception in this case.
 * <p>
 * Only blocking I/O operations are identified with this facility.
 * <p>
 * <b>Warning</b>
 * <p>
 * These methods are called from sensitive points in the I/O subsystem. Great
 * care must be taken to not interfere with ongoing operations or cause
 * deadlocks. In particular:
 * <ul>
 * <li>Implementations must not throw exceptions since this will cause
 * disruptions to the I/O operations.
 * <li>Implementations must not do I/O operations since this will lead to an
 * endless loop.
 * <li>Since the hooks may be called while holding low-level locks in the I/O
 * subsystem, implementations must be careful with synchronization or
 * interaction with other threads to avoid deadlocks in the VM.
 * </ul>
 */
public final class IoTrace {

    private IoTrace() {
    }

    /**
     * Called before data is read from a socket.
     *
     * @return a context object
     */
    public static Object socketReadBegin() {
        return TraceIO.socketReadBegin();
    }

    /**
     * Called after data is read from the socket.
     *
     * @param context
     *            the context returned by the previous call to socketReadBegin()
     * @param address
     *            the remote address the socket is bound to
     * @param port
     *            the remote port the socket is bound to
     * @param timeout
     *            the SO_TIMEOUT value of the socket (in milliseconds) or 0 if
     *            there is no timeout set
     * @param bytesRead
     *            the number of bytes read from the socket, 0 if there was an
     *            error reading from the socket
     */
    public static void socketReadEnd(Object context, InetAddress address, int port, int timeout, long bytesRead) {
        TraceIO.socketReadEnd(context, address, port, timeout, bytesRead);
    }

    /**
     * Called before data is written to a socket.
     *
     * @return a context object
     */
    public static Object socketWriteBegin() {
        return TraceIO.socketWriteBegin();
    }

    /**
     * Called after data is written to a socket.
     *
     * @param context
     *            the context returned by the previous call to
     *            socketWriteBegin()
     * @param address
     *            the remote address the socket is bound to
     * @param port
     *            the remote port the socket is bound to
     * @param bytesWritten
     *            the number of bytes written to the socket, 0 if there was an
     *            error writing to the socket
     */
    public static void socketWriteEnd(Object context, InetAddress address, int port, long bytesWritten) {
        TraceIO.socketWriteEnd(context, address, port, bytesWritten);
    }

    /**
     * Called before data is read from a file.
     *
     * @param path
     *            the path of the file
     * @return a context object
     */
    public static Object fileReadBegin(String path) {
        return TraceIO.fileReadBegin(path);
    }

    /**
     * Called after data is read from a file.
     *
     * @param context
     *            the context returned by the previous call to fileReadBegin()
     * @param bytesRead
     *            the number of bytes written to the file, 0 if there was an
     *            error writing to the file
     */
    public static void fileReadEnd(Object context, long bytesRead) {
        TraceIO.fileReadEnd(context, bytesRead);
    }

    /**
     * Called before data is written to a file.
     *
     * @param path
     *            the path of the file
     * @return a context object
     */
    public static Object fileWriteBegin(String path) {
        return TraceIO.fileReadBegin(path);
    }

    /**
     * Called after data is written to a file.
     *
     * @param context
     *            the context returned by the previous call to fileReadBegin()
     * @param bytesWritten
     *            the number of bytes written to the file, 0 if there was an
     *            error writing to the file
     */
    public static void fileWriteEnd(Object context, long bytesWritten) {
        TraceIO.fileWriteEnd(context, bytesWritten);
    }
}
