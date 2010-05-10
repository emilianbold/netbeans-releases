/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.terminal.api;

import java.io.InputStream;
import java.io.OutputStream;
import org.openide.util.Lookup;
import org.openide.windows.InputOutput;

import org.netbeans.lib.terminalemulator.Term;

/**
 * Capability of an InputOutput which provides direct access to a Term.
 * @author ivan
 */
public abstract class IOTerm {

    private static IOTerm find(InputOutput io) {
        if (io instanceof Lookup.Provider) {
            Lookup.Provider p = (Lookup.Provider) io;
            return p.getLookup().lookup(IOTerm.class);
        }
        return null;
    }

    /**
     * Checks whether this feature is supported for provided IO
     * @param io IO to check on
     * @return true if supported
     */
    public static boolean isSupported(InputOutput io) {
        return find(io) != null;
    }

    /**
     * Return the underlying Term associatd with this IO.
     * @param io IO to operate on.
     * @return underlying Term associatd with io. null if no such Term.
     */
    public static Term term(InputOutput io) {
	IOTerm iot = find(io);
	if (iot != null)
	    return iot.term();
	else
	    return null;
    }

    /**
     * Connect an I/O stream pair or triple to this Term.
     *
     * @param pin Input (and paste operations) to the sub-process.
     *             this stream.
     * @param pout Main output from the sub-process. Stuff received via this
     *             stream will be rendered on the screen.
     * @param perr Error output from process. May be null if the error stream
     *		   is already absorbed into 'pout' as the case might be with
     *             ptys.
     */
    public static void connect(InputOutput io, OutputStream pin, InputStream pout, InputStream perr) {
	IOTerm iot = find(io);
	if (iot != null)
	    iot.connect(pin, pout, perr);
	else
	    return;
    }

    /**
     * Disconnect previously connected Streams and free resources.
     * Arrange to wait until all pending output from a terminated or exited
     * process has been rendered in the terminal and then call
     * continuation.run() on the EDT thread.
     * Only then can connect() be called again.
     * @param continuation The continuation to run after all output has been
     *        drained.
     */
    public static void disconnect(InputOutput io, Runnable continuation) {
	IOTerm iot = find(io);
	if (iot != null)
	    iot.disconnect(continuation);
	else
	    return;
    }


    /**
     * Return the underlying Term associatd with this IO.
     * @return underlying Term associatd with io.
     */
    abstract protected Term term();

    /**
     * Connect an I/O stream pair or triple to this Term.
     *
     * @param pin Input (and paste operations) to the sub-process.
     *             this stream.
     * @param pout Main output from the sub-process. Stuff received via this
     *             stream will be rendered on the screen.
     * @param perr Error output from process. May be null if the error stream
     *		   is already absorbed into 'pout' as the case might be with
     *             ptys.
     */
    abstract protected void connect(OutputStream pin, InputStream pout, InputStream perr);

    abstract protected void disconnect(Runnable continuation);
}
