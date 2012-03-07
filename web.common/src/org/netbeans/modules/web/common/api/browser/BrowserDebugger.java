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
package org.netbeans.modules.web.common.api.browser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.api.project.Project;
import org.netbeans.modules.web.common.spi.browser.BrowserDebuggerImplementation;

/**
 * API wrapper of browser's debugging support.
 */
public final class BrowserDebugger {
    
    private static Map<BrowserDebuggerImplementation, List<WebBrowserPane>>
            activeDebuggingSessions = new HashMap<BrowserDebuggerImplementation, List<WebBrowserPane>>();

    /**
     * Does browser pane supports debugging? Returns TRUE if it does; FALSE if 
     * browser is not running in debug mode; or null in case debugging is not supported
     * by the browser at all.
     */
    public static Boolean isDebuggingEnabled(WebBrowserPane pane) {
        BrowserDebuggerImplementation impl = pane.getLookup().lookup(BrowserDebuggerImplementation.class);
        if (impl == null) {
            return null;
        }
        return Boolean.valueOf(impl.isDebuggingEnabled());
    }
    
    /**
     * Given browser pane would like to initiate debugging session. Browser
     * is having one debugging session which is shared by multiple browser panes. 
     */
    public static void startDebuggingSession(Project p, WebBrowserPane pane) {
        BrowserDebuggerImplementation impl = pane.getLookup().lookup(BrowserDebuggerImplementation.class);
        if (impl == null) {
            return;
        }
        List<WebBrowserPane> listOfPanes = activeDebuggingSessions.get(impl);
        if (listOfPanes == null) {
            impl.startDebuggingSession();
            listOfPanes = new ArrayList<WebBrowserPane>();
            activeDebuggingSessions.put(impl, listOfPanes);
        }
        assert !listOfPanes.contains(pane) : "this pane already started debugging session";
        listOfPanes.add(pane);
        impl.activateBreakpoints(p);
    }
    
    /**
     * When browser pane was closed it tells browser that debugging session for this
     * page is not required anymore. After all panes indicated that no debugging
     * session is required the browser debugging debugging session is stopped.
     */
    public static void stopDebuggingSession(WebBrowserPane pane) {
        BrowserDebuggerImplementation impl = pane.getLookup().lookup(BrowserDebuggerImplementation.class);
        if (impl == null) {
            return;
        }
        List<WebBrowserPane> listOfPanes = activeDebuggingSessions.get(impl);
        assert listOfPanes != null : "there is no record of this page starting debugging session";
        assert listOfPanes.contains(pane) : "this pane has not started debugging session";
        listOfPanes.remove(pane);
        if (listOfPanes.isEmpty()) {
            impl.stopDebuggingSession();
            activeDebuggingSessions.remove(impl);
        }
    }

    /**
     * 
     */
//    public static void activateBreakpoints(WebBrowserPane pane, Project p) {
//        BrowserDebuggerImplementation impl = pane.getLookup().lookup(BrowserDebuggerImplementation.class);
//        if (impl == null) {
//            return;
//        }
//        impl.activateBreakpoints(p);
//    }
    
    
}
