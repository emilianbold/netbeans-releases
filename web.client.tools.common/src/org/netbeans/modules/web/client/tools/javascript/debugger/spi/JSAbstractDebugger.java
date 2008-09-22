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

package org.netbeans.modules.web.client.tools.javascript.debugger.spi;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import org.netbeans.modules.web.client.tools.javascript.debugger.api.JSCallStackFrame;
import org.netbeans.modules.web.client.tools.javascript.debugger.api.JSDebugger;
import org.netbeans.modules.web.client.tools.javascript.debugger.api.JSDebuggerConsoleEvent;
import org.netbeans.modules.web.client.tools.javascript.debugger.api.JSDebuggerConsoleEventListener;
import org.netbeans.modules.web.client.tools.javascript.debugger.api.JSDebuggerEvent;
import org.netbeans.modules.web.client.tools.javascript.debugger.api.JSDebuggerEventListener;
import org.netbeans.modules.web.client.tools.javascript.debugger.api.JSDebuggerState;
import org.netbeans.modules.web.client.tools.javascript.debugger.api.JSHttpMessage;
import org.netbeans.modules.web.client.tools.javascript.debugger.api.JSHttpMessageEvent;
import org.netbeans.modules.web.client.tools.javascript.debugger.api.JSHttpMessageEventListener;
import org.netbeans.modules.web.client.tools.javascript.debugger.api.JSProperty;
import org.netbeans.modules.web.client.tools.javascript.debugger.api.JSSource;
import org.netbeans.modules.web.client.tools.javascript.debugger.api.JSWindow;
import org.openide.awt.HtmlBrowser;
import org.openide.execution.NbProcessDescriptor;
import org.openide.util.Utilities;

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
    private HashMap<String, JSSource> sources = new LinkedHashMap<String, JSSource>();

    private JSCallStackFrame[] callStackFrames = JSCallStackFrame.EMPTY_ARRAY;

    private final List<JSDebuggerEventListener> listeners;
    private final List<JSDebuggerConsoleEventListener> consoleListeners;
    private final List<JSHttpMessageEventListener> httpListeners;
    protected final PropertyChangeSupport propertyChangeSupport;

    private static AtomicLong sequenceIdGenerator = new AtomicLong(-1);

    public JSAbstractDebugger(URI uri, HtmlBrowser.Factory browser) {
        this.uri = uri;
        this.browser = browser;

        sequenceId = sequenceIdGenerator.incrementAndGet();
        listeners = new CopyOnWriteArrayList<JSDebuggerEventListener>();
        consoleListeners = new CopyOnWriteArrayList<JSDebuggerConsoleEventListener>();
        httpListeners = new CopyOnWriteArrayList<JSHttpMessageEventListener>();
        propertyChangeSupport = new PropertyChangeSupport(this);
    }

    public final boolean startDebugging() {
        if (debuggerState.getState() != JSDebuggerState.State.NOT_CONNECTED) {
            throw new IllegalStateException(/* TODO */);
        }

        return startDebuggingImpl();
    }

    public final void cancelStartDebugging() {    
        cancelStartDebuggingImpl();
    }
    
    protected final long getSequenceId() {
        return sequenceId;
    }

    protected abstract boolean startDebuggingImpl();
    protected abstract void cancelStartDebuggingImpl();

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

    protected void setHttpMessage(JSHttpMessage message){
        fireJSHttpMessageEvent(new JSHttpMessageEvent(this, message));
    }


    // Sources
    public JSSource[] getSources() {
        return sources.values().toArray(JSSource.EMPTY_ARRAY);
    }

    protected void setSources(JSSource[] jsSources) {
        JSSource[] oldjsSources = getSources();
        for(JSSource source : jsSources) {
            sources.put(source.getLocation().getURI().toString(), source);
        }
        propertyChangeSupport.firePropertyChange(PROPERTY_SOURCES,
                oldjsSources,
                getSources());
    }

    public InputStream getInputStreamForURL(URL url) {
        if (url != null) {
            try {
                return getInputStreamForURLImpl(url.toURI().toString());
            } catch (URISyntaxException use) {
                    Log.getLogger().log(Level.INFO, use.getMessage(), use);
            }
        }
        return null;
    }

    protected abstract InputStream getInputStreamForURLImpl(String uri);

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
    
    public boolean setProperty(JSCallStackFrame callStackFrame, String fullName, String value) {
        return setPropertyImpl(callStackFrame, fullName, value);
    }    
    
    protected abstract boolean setPropertyImpl(JSCallStackFrame callStackFrame, String fullName, String value);    

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
            try {
                listener.onDebuggerEvent(debuggerEvent);
            } catch (Exception ex) {
                Log.getLogger().log(Level.INFO, "Exception in debugger event listener", ex);
            }
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

    public void addJSHttpMessageEventListener(JSHttpMessageEventListener httpListener) {
        httpListeners.add(httpListener);
    }

    public void removeJSHttpMessageEventListener(JSHttpMessageEventListener httpListener) {
        httpListeners.remove(httpListener);
    }

    protected void fireJSHttpMessageEvent(JSHttpMessageEvent httpMessageEvent) {
        for (JSHttpMessageEventListener listener : httpListeners) {
            listener.onHttpMessageEvent(httpMessageEvent);
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

    protected String getBrowserArguments() {
        if (browser != null) {
            try {
                Method method = browser.getClass().getMethod("getBrowserExecutable");
                NbProcessDescriptor processDescriptor = createPatchedExecutable((NbProcessDescriptor) method.invoke(browser));
                String arguments = processDescriptor.getArguments();
                if (arguments != null) {
                    arguments = arguments.replaceAll("(\\{URL\\})|(\\{params\\})", ""); // NOI18N
                    return arguments;
                }
            } catch (SecurityException e) {
            } catch (NoSuchMethodException e) {
            } catch (IllegalArgumentException e) {
            } catch (IllegalAccessException e) {
            } catch (InvocationTargetException e) {
            }
        }
        
        return "";
    }
    
    
    /**  XXX Taken from extbrowser.UnixBrowserImpl
     * 
     * Creates modified NbProcessDescriptor that can be used to start
     * browser process when <CODE>-remote openURL()</CODE> options
     * cannot be used.
     * @return command or <CODE>null</CODE>
     * @param p Original command.
     */
    protected static NbProcessDescriptor createPatchedExecutable (NbProcessDescriptor p) {
        NbProcessDescriptor newP = null;
        
        String [] args = Utilities.parseParameters(p.getArguments());
        if (args.length > 1) {
            StringBuffer newArgs = new StringBuffer ();
            boolean found = false;
            for (int i=0; i<args.length; i++) {
                if (newArgs.length() > 0) {
                    newArgs.append(" ");  // NOI18N
                }
                if (args[i].indexOf("-remote") >= 0  // NOI18N
                &&  args[i+1].indexOf("openURL(") >=0) {  // NOI18N
                    found = true;
                    newArgs.append("{URL}");  // NOI18N
                    i += 1;
                }
                else {
                    newArgs.append(args[i]);  // NOI18N
                }
            }
            if (found) {
                newP = new NbProcessDescriptor (p.getProcessName(), newArgs.toString(), p.getInfo());
            }
        }
        return newP != null ? newP : p;
    }

    
    public final void finish(boolean terminate) {
        sources.clear();
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
