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
package org.netbeans.modules.dlight.terminal.api;

import org.netbeans.lib.terminalemulator.StreamTerm;
import org.netbeans.modules.dlight.terminal.TerminalInputOutput;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;

/**
 * Support for running @{link Command}s under @{link IOProvider}s.
 * @author ivan
 */
public final class TerminalIOProviderSupport {

    public static IOProvider getIOProvider() {
        IOProvider iop = null;
        iop = IOProvider.get("Terminal"); // NOI18N
        if (iop == null) {
            System.out.printf("IOProviderActionSupport.getTermIOProvider() couldn't find our provider\n"); // NOI18N
            iop = IOProvider.getDefault();
        }
        return iop;
    }

    /**
     * Declare whether io to 'io' is internal to the IDE or external, via a pty.
     * For internal io Term requires a proper line discipline, for example,
     * to convert the "\n" emitted by println() to a "\n\r" and so on.
     * @param io The InputOutput to modify.
     * @param b Add line discipline if true.
     */
    public static void setInternal(InputOutput io, boolean b) {
        if (IOEmulation.isSupported(io) && b) {
            IOEmulation.setDisciplined(io);
        }
    }

    public static boolean isTerminalIO(InputOutput io) {
        return (io instanceof TerminalInputOutput);
    }

    public static StreamTerm getTerm(InputOutput io) {
        if (io instanceof TerminalInputOutput) {
            TerminalInputOutput tio = (TerminalInputOutput) io;
            return tio.term();
        }

        return null;
    }
}
