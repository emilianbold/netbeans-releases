/* 
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.terminal.api;

import org.netbeans.modules.terminal.TermTopComponent;
import java.util.logging.Logger;
import org.openide.windows.IOContainer;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

public final class TerminalProvider {

    private static TerminalProvider instance;

    public static TerminalProvider getDefault() {
        if (instance == null)
            instance = new TerminalProvider();
        return instance;
    }

    private TerminalProvider() {
    }

    private TerminalWindow getInstance(String preferredID) {
        // The following is a mutated form of boilerplate
        // TopComponent.getInstance().
        TopComponent win = WindowManager.getDefault().findTopComponent(preferredID);

        if (win == null) {
            Logger.getLogger(TerminalProvider.class.getName()).warning(
                    "Cannot find " + preferredID + " component. It will not be located properly in the window system.");
            // fall back
            win = TermTopComponent.findInstance();

        } else if (! (win instanceof TerminalWindow)) {
            Logger.getLogger(TerminalProvider.class.getName()).warning(
                    "There seem to be multiple components with the '" + preferredID +
                    "' ID. That is a potential source of errors and unexpected behavior.");

            // fall back
            win = TermTopComponent.findInstance();
        }

        TerminalWindow itc = (TerminalWindow) win;
        return itc;
    }

    /**
     * Get a Terminal in the given IOContainer.
     */
    public Terminal createTerminal(String name, IOContainer ioContainer) {
        return new Terminal(ioContainer, name);
    }

    /**
     * Get a Terminal in the default terminal TopComponent.
     */
    public Terminal createTerminal(String name) {
        TermTopComponent tc = TermTopComponent.findInstance();
        return new Terminal(tc.terminalContainer(), name);
    }

    /**
     * Get a Terminal in the terminal TopComponent identified
     * by 'preferredID'.
     */
    public synchronized Terminal createTerminal(String name, String preferredID) {
        TerminalWindow itc = getInstance(preferredID);
        return new Terminal(itc.terminalContainer(), name);
    }

    /**
     * Utility for creating custom {@link Terminal}-based TopComponents.
     * See the class comment for {@link TerminalContainer} for a description of
     * how to do this.
     * @param tc TopComponent the Terminals will go into.
     * @param name The name of the TopComponent.
     *        Usually @{link TopComponent.getName()}
     * @return a TerminalContainer.
     */

    public static TerminalContainer createTerminalContainer(TopComponent tc, String name) {
        return new TerminalContainer(tc, name);
    }
}
    
