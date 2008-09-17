/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.web.client.tools.javascript.debugger.api;

import java.beans.PropertyChangeListener;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;

import java.util.List;
import org.netbeans.api.debugger.Breakpoint.HIT_COUNT_FILTERING_STYLE;
import org.netbeans.modules.web.client.tools.common.dbgp.Feature;
import org.openide.awt.HtmlBrowser;

/**
 *
 * @author Sandip V. Chitale <sandipchitale@netbeans.org>
 */
public interface JSDebugger {
    String PROPERTY_SOURCES = "sources";
    String PROPERTY_WINDOWS = "windows";
    String PROPERTY_RELOADSOURCES = "reloadsources";

    void addPropertyChangeListener(PropertyChangeListener l);
    void removePropertyChangeListener(PropertyChangeListener l);

    boolean startDebugging();
    void cancelStartDebugging();

    String getID();

    HtmlBrowser.Factory getBrowser();
    URI getURI();

    JSDebuggerState getDebuggerState();

    void setBooleanFeature(Feature.Name featureName, boolean featureValue);
    List<String> setBreakpoint(JSBreakpoint breakpoint);    
    boolean removeBreakpoint(String id);
    boolean updateBreakpoint(String id, Boolean enabled, int line, int hitValue, HIT_COUNT_FILTERING_STYLE hitCondition, String condition);

    boolean isSuspended();

    void addJSDebuggerEventListener(JSDebuggerEventListener debuggerEventListener);
    void removeJSDebuggerEventListener(JSDebuggerEventListener debuggerEventListener);
    
    void addJSDebuggerConsoleEventListener(JSDebuggerConsoleEventListener debuggerConsoleEventListener);
    void removeJSDebuggerConsoleEventListener(JSDebuggerConsoleEventListener debuggerConsoleEventListener);

    void addJSHttpMessageEventListener(JSHttpMessageEventListener httpMessageEventListener);
    void removeJSHttpMessageEventListener(JSHttpMessageEventListener httpMessageEventListener);

    // Windows
    JSWindow[] getWindows();

    // Sources
    JSSource[] getSources();
    
    InputStream getInputStreamForURL(URL url);

    JSCallStackFrame[] getCallStackFrames();

    JSProperty getScope(JSCallStackFrame callStackFrame);
    JSProperty getThis(JSCallStackFrame callStackFrame);
    JSProperty getProperty(JSCallStackFrame callStackFrame, String fullName);
    JSProperty eval(JSCallStackFrame callStackFrame, String expression);
    JSProperty[] getProperties(JSCallStackFrame callStackFrame, String fullName);
    public boolean setProperty(JSCallStackFrame callStackFrame, String fullName, String value);    

    void resume();
    void pause();
    void stepInto();
    void stepOver();
    void stepOut();

    void runToCursor(JSURILocation location);
    boolean isRunningTo(URI uri, int line);

    public void finish(boolean terminate);

}
