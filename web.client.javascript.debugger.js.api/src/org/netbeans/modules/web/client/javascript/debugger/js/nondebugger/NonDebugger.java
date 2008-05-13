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

package org.netbeans.modules.web.client.javascript.debugger.js.nondebugger;

import java.beans.PropertyChangeListener;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.concurrent.atomic.AtomicLong;

import org.netbeans.api.debugger.Breakpoint.HIT_COUNT_FILTERING_STYLE;
import org.netbeans.modules.web.client.javascript.debugger.js.dbgp.Feature;
import org.netbeans.modules.web.client.javascript.debugger.js.api.JSBreakpoint;
import org.netbeans.modules.web.client.javascript.debugger.js.api.JSCallStackFrame;
import org.netbeans.modules.web.client.javascript.debugger.js.api.JSDebugger;
import org.netbeans.modules.web.client.javascript.debugger.js.api.JSDebuggerConsoleEventListener;
import org.netbeans.modules.web.client.javascript.debugger.js.api.JSDebuggerEventListener;
import org.netbeans.modules.web.client.javascript.debugger.js.api.JSDebuggerState;
import org.netbeans.modules.web.client.javascript.debugger.js.api.JSProperty;
import org.netbeans.modules.web.client.javascript.debugger.js.api.JSSource;
import org.netbeans.modules.web.client.javascript.debugger.js.api.JSWindow;
import org.openide.awt.HtmlBrowser.Factory;
import org.openide.awt.HtmlBrowser.URLDisplayer;

/**
 * This is a no-op debugger.
 *
 * @author Sandip V. Chitale <sandipchitale@netbeans.org>
 */
public class NonDebugger implements JSDebugger {
    private String ID;
    private long sequenceId;
    
    private static AtomicLong sequenceIdGenerator = new AtomicLong(-1);
    
    private final URI uri;
    
    public NonDebugger(URI uri, Factory browser) {
        this.uri = uri;
        sequenceId = sequenceIdGenerator.incrementAndGet();
    }

    public void startDebugging() {
        startDebuggingImpl();
    }
    
    protected void startDebuggingImpl() {
        try {
            URLDisplayer.getDefault().showURL(getURI().toURL());
        } catch (MalformedURLException ex) {
            //Log message here
        }
    }

    public String getID() {
        if (ID == null) {
            ID = "NONDEBUGGER-" + getSequenceId(); // NOI18N
        }

        return ID;
    }
    
    protected final long getSequenceId() {
        return sequenceId;
    }
    
    public void setBooleanFeature(Feature.Name featureName, boolean featureValue) {}

    public void resume() {

    }
    
    public void pause() {

    }    

    public void stepInto() {

    }

    public void stepOver() {

    }

    public void stepOut() {

    }

    public void runToCursor() {

    }

    public boolean isRunningTo(URI uri, int line) {
        return false;
    }

    protected void finishImpl(boolean terminate) {

    }

    public String setBreakpoint(JSBreakpoint breakpoint) {
        return null;
    }
    
    public boolean removeBreakpoint(String id) {
        return false;
    }
    
    public boolean updateBreakpoint(String id, Boolean enabled, int line, int hitValue, HIT_COUNT_FILTERING_STYLE hitCondition, String condition) {
        return false;
    }    

    public void addJSDebuggerEventListener(
        JSDebuggerEventListener debuggerEventListener) {
    }
    
    public void addJSDebuggerConsoleEventListener(
        JSDebuggerConsoleEventListener debuggerConsoleEventListener) {
        
    }
    
    public void addPropertyChangeListener(PropertyChangeListener l) {
    }

    public void finish(boolean terminate) {
    }

    public Factory getBrowser() {
        return null;
    }

    public JSCallStackFrame[] getCallStackFrames() {
        return JSCallStackFrame.EMPTY_ARRAY;
    }

    public JSDebuggerState getDebuggerState() {
        return JSDebuggerState.RUNNING;
    }

    public InputStream getInputStreamForURL(URL url) {
        return null;
    }

    public JSProperty[] getProperties(JSCallStackFrame callStackFrame, String fullName) {
        return JSProperty.EMPTY_ARRAY;
    }
    
    public JSProperty getProperty(JSCallStackFrame callStackFrame, String fullName) {
        return null;
    }
    
    public JSProperty eval(JSCallStackFrame callStackFrame, String expression) {
        return null;
    }    

    public JSProperty getScope(JSCallStackFrame callStackFrame) {
        return null;
    }

    public JSSource[] getSources() {
        return JSSource.EMPTY_ARRAY;
    }

    public JSProperty getThis(JSCallStackFrame callStackFrame) {
        return null;
    }

    public URI getURI() {
        return uri;
    }

    public JSWindow[] getWindows() {
        return JSWindow.EMPTY_ARRAY;
    }

    public boolean isSuspended() {
        return false;
    }

    public void removeJSDebuggerEventListener(
            JSDebuggerEventListener debuggerEventListener) {        
    }
    
    public void removeJSDebuggerConsoleEventListener(
            JSDebuggerConsoleEventListener debuggerConsoleEventListener) {
    }
    
    public void removePropertyChangeListener(PropertyChangeListener l) {        
    }

}
