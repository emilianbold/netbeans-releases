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

package org.netbeans.modules.web.client.javascript.debugger.js.spi;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import org.netbeans.modules.web.client.javascript.debugger.js.api.JSCallStackFrame;
import org.netbeans.modules.web.client.javascript.debugger.js.api.JSDebugger;
import org.netbeans.modules.web.client.javascript.debugger.js.api.JSDebuggerConsoleEvent;
import org.netbeans.modules.web.client.javascript.debugger.js.api.JSDebuggerConsoleEventListener;
import org.netbeans.modules.web.client.javascript.debugger.js.api.JSDebuggerEvent;
import org.netbeans.modules.web.client.javascript.debugger.js.api.JSDebuggerEventListener;
import org.netbeans.modules.web.client.javascript.debugger.js.api.JSDebuggerState;
import org.netbeans.modules.web.client.javascript.debugger.js.api.JSProperty;
import org.netbeans.modules.web.client.javascript.debugger.js.api.JSSource;
import org.netbeans.modules.web.client.javascript.debugger.js.api.JSWindow;
import org.openide.awt.HtmlBrowser;
import org.openide.execution.NbProcessDescriptor;

/**
 *
 * @author Sandip V. Chitale <sandipchitale@netbeans.org>
 */
public abstract class JSAbstractDebugger implements JSDebugger {
    private long sequenceId;
    private URI uri;
    private HtmlBrowser.Factory browser;
    private JSDebuggerState debuggerState = JSDebuggerState.NOT_CONNECTED;

    private JSWindow[] windows = JSWindow.EMPTY_ARRAY;
    private JSSource[] sources = JSSource.EMPTY_ARRAY;

    private JSCallStackFrame[] callStackFrames = JSCallStackFrame.EMPTY_ARRAY;

    private final List<JSDebuggerEventListener> listeners;
    private final List<JSDebuggerConsoleEventListener> consoleListeners;    
    protected final PropertyChangeSupport propertyChangeSupport;

    private static AtomicLong sequenceIdGenerator = new AtomicLong(-1);

    public JSAbstractDebugger(URI uri, HtmlBrowser.Factory browser) {
        this.uri = uri;
        this.browser = browser;

        sequenceId = sequenceIdGenerator.incrementAndGet();
        listeners = new CopyOnWriteArrayList<JSDebuggerEventListener>();
        consoleListeners = new CopyOnWriteArrayList<JSDebuggerConsoleEventListener>();
        propertyChangeSupport = new PropertyChangeSupport(this);
    }

    public final void startDebugging() {
        if (debuggerState.getState() != JSDebuggerState.State.NOT_CONNECTED) {
            throw new IllegalStateException(/* TODO */);
        }

        startDebuggingImpl();
    }

    protected final long getSequenceId() {
        return sequenceId;
    }

    protected abstract void startDebuggingImpl();

    public URI getURI() {
        return uri;
    }

    public HtmlBrowser.Factory getBrowser() {
        return browser;
    }

    public synchronized JSDebuggerState getDebuggerState() {
        return debuggerState;
    }

    protected void setDebuggerState(JSDebuggerState debuggerState) {
        synchronized(this) {
            this.debuggerState = debuggerState;
            switch (debuggerState.getState()) {
                case SUSPENDED:
                    callStackFrames = getCallStackFramesImpl();
                    break;
                default:
                    callStackFrames = JSCallStackFrame.EMPTY_ARRAY;
                    break;
            }
        }
        fireJSDebuggerEvent(new JSDebuggerEvent(this, this.debuggerState));
    }

    // Windows
    public JSWindow[] getWindows() {
        return windows;
    }

    protected void setWindows(JSWindow[] jsWindows) {
        JSWindow[] oldjsWindows = this.windows;
        this.windows = jsWindows;
        propertyChangeSupport.firePropertyChange(PROPERTY_WINDOWS,
                oldjsWindows,
                this.windows);
    }

    // Sources
    public JSSource[] getSources() {
        return sources;
    }

    protected void setSources(JSSource[] jsSources) {
        JSSource[] oldjsSources = this.sources;
        this.sources = jsSources;
        propertyChangeSupport.firePropertyChange(PROPERTY_SOURCES,
                oldjsSources,
                this.sources);
    }
    
    public InputStream getInputStreamForURL(URL url) {
        if (url == null) {
            return null;
        }
        return getInputStreamForURLImpl(url);
    }
    
    protected abstract InputStream getInputStreamForURLImpl(URL url); 
    
    public JSCallStackFrame[] getCallStackFrames() {
        return callStackFrames;
    }

    protected abstract JSCallStackFrame[] getCallStackFramesImpl();

    public JSProperty getScope(JSCallStackFrame callStackFrame) {
        return getScopeImpl(callStackFrame);
    }

    protected abstract JSProperty getScopeImpl(JSCallStackFrame callStackFrame);

    public JSProperty getThis(JSCallStackFrame callStackFrame) {
        return getThisImpl(callStackFrame);
    }
    
    protected abstract JSProperty getThisImpl(JSCallStackFrame callStackFrame);
    
    public JSProperty eval(JSCallStackFrame callStackFrame, String expression) {
        return evalImpl(callStackFrame, expression);
    }
    
    protected abstract JSProperty evalImpl(JSCallStackFrame callStackFrame, String expression);        
    
    public JSProperty getProperty(JSCallStackFrame callStackFrame, String fullName) {
        return getPropertyImpl(callStackFrame, fullName);
    }
    
    protected abstract JSProperty getPropertyImpl(JSCallStackFrame callStackFrame, String fullName);    

    public JSProperty[] getProperties(JSCallStackFrame callStackFrame, String fullName) {
        return getPropertiesImpl(callStackFrame, fullName);
    }
    
    protected abstract JSProperty[] getPropertiesImpl(JSCallStackFrame callStackFrame, String fullName);

    public void setCallStackFrames(JSCallStackFrame[] callSTackFrames) {
        this.callStackFrames = callSTackFrames;
    }

    public boolean isSuspended() {
        return debuggerState.getState() == JSDebuggerState.State.SUSPENDED;
    }

    public void addJSDebuggerEventListener(JSDebuggerEventListener debuggerEventListener) {
        listeners.add(debuggerEventListener);
    }

    public void removeJSDebuggerEventListener(JSDebuggerEventListener debuggerEventListener) {
        listeners.remove(debuggerEventListener);
    }    
    
    protected void fireJSDebuggerEvent(JSDebuggerEvent debuggerEvent) {
        for (JSDebuggerEventListener listener : listeners) {
            listener.onDebuggerEvent(debuggerEvent);
        }
    }   
    
    public void addJSDebuggerConsoleEventListener(JSDebuggerConsoleEventListener consoleEventListener) {
        consoleListeners.add(consoleEventListener);
    }

    public void removeJSDebuggerConsoleEventListener(JSDebuggerConsoleEventListener consoleEventListener) {
        consoleListeners.remove(consoleEventListener);
    }
    
    protected void fireJSDebuggerConsoleEvent(JSDebuggerConsoleEvent consoleEvent) {
        for (JSDebuggerConsoleEventListener listener : consoleListeners) {
            listener.onConsoleEvent(consoleEvent);
        }
    }    

    // Property Change Listeners
    public void addPropertyChangeListener(PropertyChangeListener l) {
        propertyChangeSupport.addPropertyChangeListener(l);
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
        propertyChangeSupport.removePropertyChangeListener(l);
    }

    protected String getBrowserExecutable() {
        if (browser != null) {
            try {
                Method method = browser.getClass().getMethod("getBrowserExecutable");
                NbProcessDescriptor processDescriptor = (NbProcessDescriptor) method.invoke(browser);
                return processDescriptor.getProcessName();
            } catch (SecurityException e) {
            } catch (NoSuchMethodException e) {
            } catch (IllegalArgumentException e) {
            } catch (IllegalAccessException e) {
            } catch (InvocationTargetException e) {
            }
        }
        return "firefox"; // NOI18N
    }

    public final void finish(boolean terminate) {
        finishImpl(terminate);

        // Terminated by the user
        if (terminate) {
            setDebuggerState(JSDebuggerState.DISCONNECTED_USER);
        } else {
            // Not terminated by the user
            setDebuggerState(JSDebuggerState.DISCONNECTED);
        }
    }

    protected abstract void finishImpl(boolean terminate);
}
