/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.javascript.debugger.browser;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.netbeans.modules.extbrowser.spi.ExternalBrowserDescriptor;
import org.netbeans.modules.web.browser.spi.BrowserDebuggerImplementation;
import org.netbeans.modules.web.javascript.debugger.wrd.Debugger;
import org.netbeans.modules.web.javascript.debugger.wrd.DebuggerListener;
import org.netbeans.modules.web.javascript.debugger.wrd.DebuggerState;

public class ChromeBrowserDebuggerImpl implements BrowserDebuggerImplementation {

    private static String DEBUGGER_OPTION = "--remote-debugging-port="; // NOI18N
    private static String DEFAULT_HOST = "localhost";
    private static int DEFAULT_PORT = 9222;
    private static final Logger LOGGER = Logger.getLogger(ChromeBrowserLookupProvider.class.getName());
    
    private ExternalBrowserDescriptor desc;
    private Debugger debugger = null;
    private DebuggerListener l;

    public ChromeBrowserDebuggerImpl(ExternalBrowserDescriptor desc) {
        this.desc = desc;
        l = new DebuggerListener() {
            @Override
            public void stateChanged(Debugger deb) {
                if (deb != null && deb.getState() == DebuggerState.DISCONNECTED) {
                    deb.removeListener(l);
                    deb = null;
                }
            }
        };
    }
            
    @Override
    public boolean startDebuggingSession(String urlToDebug) {
        if (debugger != null) {
            return true;
        }
        debugger = Debugger.createConnection(DEFAULT_HOST, DEFAULT_PORT, urlToDebug);
        if (debugger == null) {
            return false;
        }
        debugger.addListener(l);
        return true;
    }
    
    @Override
    public void stopDebuggingSession() {
        final Debugger d = debugger;
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                d.stopDebugger();
            }
        });
        debugger = null;
    }

    private int parsePortNumber() {
        int i = desc.getArguments().indexOf(DEBUGGER_OPTION);
        if (i == -1) {
            return -1;
        }
        String port = desc.getArguments().substring(i+DEBUGGER_OPTION.length());
        i = port.indexOf(" ");
        if (i != -1) {
            port = port.substring(0, i);
        }
        try {
            int portNumber = Integer.parseInt(port);
            return portNumber;
        } catch (NumberFormatException ex) {
            LOGGER.log(Level.FINE, "cannot parse port number out of '"+desc.getArguments()+"'");
            return -1;
        }
    }

}
