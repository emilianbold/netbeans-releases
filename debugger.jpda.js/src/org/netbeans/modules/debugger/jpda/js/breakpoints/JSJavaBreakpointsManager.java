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

package org.netbeans.modules.debugger.jpda.js.breakpoints;

import com.sun.jdi.request.EventRequest;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.DebuggerManagerAdapter;
import org.netbeans.api.debugger.LazyDebuggerManagerListener;
import org.netbeans.api.debugger.jpda.ClassLoadUnloadBreakpoint;
import org.netbeans.api.debugger.jpda.ClassVariable;
import org.netbeans.api.debugger.jpda.JPDABreakpoint;
import org.netbeans.api.debugger.jpda.JPDAClassType;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.LineBreakpoint;
import org.netbeans.api.debugger.jpda.MethodBreakpoint;
import org.netbeans.api.debugger.jpda.Variable;
import org.netbeans.api.debugger.jpda.event.JPDABreakpointEvent;
import org.netbeans.api.debugger.jpda.event.JPDABreakpointListener;
import org.netbeans.modules.debugger.jpda.js.JSUtils;
import org.netbeans.modules.debugger.jpda.js.source.Source;
import org.netbeans.modules.debugger.jpda.js.source.SourceURLMapper;
import org.netbeans.modules.javascript2.debug.breakpoints.JSLineBreakpoint;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import org.openide.filesystems.FileObject;

/**
 * Manages creation/removal of Java breakpoints corresponding to JS breakpoints.
 * 
 * @author Martin
 */
@DebuggerServiceRegistration(types=LazyDebuggerManagerListener.class)
public class JSJavaBreakpointsManager extends DebuggerManagerAdapter {
    
    private final Map<JPDADebugger, ScriptsHandler> scriptHandlers = new HashMap<>();
    //private final Map<JSLineBreakpoint, LineBreakpoint> breakpoints = new HashMap<>();
    private final Map<URLEquality, Set<JSLineBreakpoint>> breakpointsByURL = new HashMap<>();
    private final Map<String, Set<JSLineBreakpoint>> breakpointsByScriptNames = new HashMap<>();
    private ClassLoadUnloadBreakpoint scriptBP;
    //private ScriptsHandler sh;
    
    public JSJavaBreakpointsManager() {
    }

    @Override
    public Breakpoint[] initBreakpoints() {
        scriptBP = ClassLoadUnloadBreakpoint.create(JSUtils.NASHORN_SCRIPT+"*",
                                                    false,
                                                    ClassLoadUnloadBreakpoint.TYPE_CLASS_LOADED);
        scriptBP.setHidden(true);
        scriptBP.setSuspend(EventRequest.SUSPEND_NONE);
        return new Breakpoint[] { scriptBP };
    }
    
    @Override
    public String[] getProperties() {
        return new String[] { DebuggerManager.PROP_BREAKPOINTS_INIT,
                              DebuggerManager.PROP_BREAKPOINTS,
                              DebuggerManager.PROP_DEBUGGER_ENGINES };
    }

    @Override
    public void breakpointAdded(Breakpoint breakpoint) {
        if (!(breakpoint instanceof JSLineBreakpoint)) {
            return ;
        }
        JSLineBreakpoint jslb = (JSLineBreakpoint) breakpoint;
        URL url = jslb.getURL();
        URLEquality urle = new URLEquality(url);
        synchronized (breakpointsByURL) {
            Set<JSLineBreakpoint> bpts = breakpointsByURL.get(urle);
            if (bpts == null) {
                bpts = new HashSet<>();
                breakpointsByURL.put(urle, bpts);
            }
            bpts.add(jslb);
        }
        String scriptName = getScriptName(jslb);
        synchronized (breakpointsByScriptNames) {
            Set<JSLineBreakpoint> bpts = breakpointsByScriptNames.get(scriptName);
            if (bpts == null) {
                bpts = new HashSet<>();
                breakpointsByScriptNames.put(scriptName, bpts);
            }
            bpts.add(jslb);
        }
        synchronized (scriptHandlers) {
            for (ScriptsHandler sh : scriptHandlers.values()) {
                sh.addScriptName(scriptName, jslb);
            }
        }
    }

    @Override
    public void breakpointRemoved(Breakpoint breakpoint) {
        if (!(breakpoint instanceof JSLineBreakpoint)) {
            return ;
        }
        JSLineBreakpoint jslb = (JSLineBreakpoint) breakpoint;
        URL url = jslb.getURL();
        URLEquality urle = new URLEquality(url);
        synchronized (breakpointsByURL) {
            Set<JSLineBreakpoint> bpts = breakpointsByURL.get(urle);
            if (bpts != null) {
                bpts.remove(jslb);
                if (bpts.isEmpty()) {
                    breakpointsByURL.remove(urle);
                }
            }
        }
        String scriptName = getScriptName(jslb);
        boolean removeScript = false;
        synchronized (breakpointsByScriptNames) {
            Set<JSLineBreakpoint> bpts = breakpointsByScriptNames.get(scriptName);
            if (bpts != null) {
                bpts.remove(jslb);
                if (bpts.isEmpty()) {
                    breakpointsByScriptNames.remove(scriptName);
                    removeScript = true;
                }
            }
        }
        synchronized (scriptHandlers) {
            for (ScriptsHandler sh : scriptHandlers.values()) {
                sh.removeBreakpoint(scriptName, jslb, removeScript);
            }
        }
    }

    @Override
    public void engineAdded(DebuggerEngine engine) {
        JPDADebugger debugger = engine.lookupFirst(null, JPDADebugger.class);
        if (debugger == null) {
            return ;
        }
        synchronized (scriptHandlers) {
            if (scriptHandlers.containsKey(debugger)) {
                return ;
            }
        }
        ScriptsHandler sh = new ScriptsHandler(debugger);
        scriptBP.addJPDABreakpointListener(sh);
        synchronized (scriptHandlers) {
            scriptHandlers.put(debugger, sh);
        }
        synchronized (breakpointsByScriptNames) {
            for (String scriptName : breakpointsByScriptNames.keySet()) {
                sh.addScriptName(scriptName, null);
            }
        }
    }

    @Override
    public void engineRemoved(DebuggerEngine engine) {
        JPDADebugger debugger = engine.lookupFirst(null, JPDADebugger.class);
        if (debugger == null) {
            return ;
        }
        ScriptsHandler sh;
        synchronized (scriptHandlers) {
            sh = scriptHandlers.remove(debugger);
        }
        if (sh != null) {
            scriptBP.removeJPDABreakpointListener(sh);
            sh.destroy();
        }
    }
    
    private static String getScriptName(JSLineBreakpoint jslb) {
        String name;
        FileObject fo = jslb.getFileObject();
        if (fo != null) {
            name = fo.getName();
        } else {
            String url = jslb.getURL().toExternalForm();
            int i1 = url.lastIndexOf('/');
            if (i1 < 0) {
                i1 = 0;
            } else {
                i1++;
            }
            int i2 = url.lastIndexOf('.');
            if (i2 < i1) {
                i2 = url.length();
            }
            name = url.substring(i1, i2);
            name = SourceURLMapper.percentDecode(name);
        }
        if ("<eval>".equals(name)) {        // NOI18N
            name = "\\^eval\\_";            // NOI18N
        }
        return name;
    }
    
    private final class ScriptsHandler implements JPDABreakpointListener {
        
        private final JPDADebugger debugger;
        private final Map<String, JPDAClassType> loadedScripts = new HashMap<>();
        private final Map<String, ScriptBreakpointsHandler> scriptHandlers = new HashMap<>();
        
        ScriptsHandler(JPDADebugger debugger) {
            this.debugger = debugger;
        }
        
        /**
         * 
         * @param scriptName
         * @param jslb the added breakpoint, or <code>null</code> to consider all available breakpoints
         */
        void addScriptName(String scriptName, JSLineBreakpoint jslb) {
            JPDAClassType clazz;
            synchronized (loadedScripts) {
                clazz = loadedScripts.get(scriptName);
            }
            ScriptBreakpointsHandler sbh;
            synchronized (scriptHandlers) {
                sbh = scriptHandlers.get(scriptName);
                if (sbh == null) {
                    sbh = new ScriptBreakpointsHandler(scriptName, debugger, clazz);
                    scriptHandlers.put(scriptName, sbh);
                }
            }
            sbh.addBreakpoints(jslb);
        }

        @Override
        public void breakpointReached(JPDABreakpointEvent event) {
            if (debugger != event.getDebugger()) {
                return ;
            }
            Variable scriptClass = event.getVariable();
            if (!(scriptClass instanceof ClassVariable)) {
                return ;
            }
            //JPDAClassType scriptType = ((ClassVariable) scriptClass).getReflectedType();
            JPDAClassType scriptType;
            try {
                scriptType = (JPDAClassType) scriptClass.getClass().getMethod("getReflectedType").invoke(scriptClass);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException ex) {
                scriptType = null;
            }
            if (scriptType != null) {
                String scriptName = scriptType.getName();
                if (scriptName.startsWith(JSUtils.NASHORN_SCRIPT)) {
                    scriptName = scriptName.substring(JSUtils.NASHORN_SCRIPT.length());
                }
                synchronized (loadedScripts) {
                    loadedScripts.put(scriptName, scriptType);
                }
            }
        }
        
        private void removeBreakpoint(String scriptName, JSLineBreakpoint jslb, boolean removeScript) {
            ScriptBreakpointsHandler sbh;
            synchronized (scriptHandlers) {
                if (removeScript) {
                    sbh = scriptHandlers.remove(scriptName);
                } else {
                    sbh = scriptHandlers.get(scriptName);
                }
            }
            if (removeScript) {
                sbh.destroy();
            } else {
                sbh.removeLineBreakpoint(jslb);
            }
        }
        
        void destroy() {
            synchronized (loadedScripts) {
                loadedScripts.clear();
            }
            synchronized (scriptHandlers) {
                for (ScriptBreakpointsHandler sbh : scriptHandlers.values()) {
                    sbh.destroy();
                }
                scriptHandlers.clear();
            }
        }

    }
    
    private final class ScriptBreakpointsHandler implements JPDABreakpointListener {
        
        private final String scriptName;
        private final JPDADebugger debugger;
        private JPDAClassType clazz;
        private ClassLoadUnloadBreakpoint scriptClassBP;
        private MethodBreakpoint scriptMethodBP;
        private Source source;
        private final Map<JSLineBreakpoint, LineBreakpointHandler> lineBreakpointHandlers = new HashMap<>();
        //private final Set<JSLineBreakpoint> breakpoints = new HashSet<>();
        
        ScriptBreakpointsHandler(String scriptName, JPDADebugger debugger, JPDAClassType clazz) {
            this.scriptName = scriptName;
            this.debugger = debugger;
            this.clazz = clazz;
            if (clazz == null) {
                initScriptClassBP();
            } else {
                createSource();
            }
        }
        
        void initScriptClassBP() {
            // script class load breakpoint so that we know when the script class appears
            scriptClassBP = ClassLoadUnloadBreakpoint.create(JSUtils.NASHORN_SCRIPT+scriptName,
                                                             false,
                                                             ClassLoadUnloadBreakpoint.TYPE_CLASS_LOADED);
            scriptClassBP.setHidden(true);
            scriptClassBP.setSuspend(EventRequest.SUSPEND_EVENT_THREAD);
            scriptClassBP.setSession(debugger);
            scriptClassBP.addJPDABreakpointListener(this);
            DebuggerManager.getDebuggerManager().addBreakpoint(scriptClassBP);
        }
        
        void initScriptMethodBP() {
            // script class method breakpoint so that we know when the script class is actually accessed
            // we can load the source object only after the script class is initialized
            scriptMethodBP = MethodBreakpoint.create(JSUtils.NASHORN_SCRIPT+scriptName, "");
            scriptMethodBP.setHidden(true);
            scriptMethodBP.setSuspend(EventRequest.SUSPEND_EVENT_THREAD);
            scriptMethodBP.setSession(debugger);
            scriptMethodBP.addJPDABreakpointListener(this);
            DebuggerManager.getDebuggerManager().addBreakpoint(scriptMethodBP);
        }
        
        @Override
        public void breakpointReached(JPDABreakpointEvent event) {
            if (scriptClassBP == event.getSource()) {
                Variable scriptClass = event.getVariable();
                if (scriptClass instanceof ClassVariable) {
                    //JPDAClassType scriptType = ((ClassVariable) scriptClass).getReflectedType();
                    JPDAClassType scriptType;
                    try {
                        scriptType = (JPDAClassType) scriptClass.getClass().getMethod("getReflectedType").invoke(scriptClass);
                    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException ex) {
                        scriptType = null;
                    }
                    this.clazz = scriptType;
                    initScriptMethodBP();
                }
            } else if (scriptMethodBP == event.getSource()) {
                DebuggerManager.getDebuggerManager().removeBreakpoint(scriptMethodBP);
                scriptMethodBP = null;
                createSource();
            }
            event.resume();
        }
        
        private void createSource() {
            this.source = Source.getSource(this.clazz);
            if (source != null) {
                /*Collection<JSLineBreakpoint> bpts;
                synchronized (breakpoints) {
                    bpts = new ArrayList<>(breakpoints);
                }
                createSourceLineBreakpoints(bpts);*/
                createSourceLineBreakpoints(null);
            }
            
        }
        
        void addBreakpoints(JSLineBreakpoint jslb) {
            /*synchronized (breakpoints) {
                breakpoints.addAll(jslbs);
            }*/
            if (source != null) {
                createSourceLineBreakpoints(jslb);
            }
        }
        
        /**
         * @param jslb the added breakpoint, or <code>null</code> to consider all available breakpoints
         */
        private void createSourceLineBreakpoints(JSLineBreakpoint jslb) {
            URL url = source.getUrl();
            if (url == null) {
                return ;
            }
            URLEquality urle = new URLEquality(url);
            if (jslb == null) {
                Set<JSLineBreakpoint> bpts;
                synchronized (breakpointsByURL) {
                    bpts = breakpointsByURL.get(urle);
                    if (bpts != null) {
                        bpts = new HashSet<>(bpts);
                    }
                }
                if (bpts != null) {
                    for (JSLineBreakpoint bp : bpts) {
                        URLEquality bpurle = new URLEquality(bp.getURL());
                        if (urle.equals(bpurle)) {
                            LineBreakpointHandler lbh = new LineBreakpointHandler(debugger, bp, source);
                            synchronized (lineBreakpointHandlers) {
                                lineBreakpointHandlers.put(bp, lbh);
                            }
                        }
                    }
                }
            } else {
                URLEquality bpurle = new URLEquality(jslb.getURL());
                if (urle.equals(bpurle)) {
                    LineBreakpointHandler lbh = new LineBreakpointHandler(debugger, jslb, source);
                    synchronized (lineBreakpointHandlers) {
                        lineBreakpointHandlers.put(jslb, lbh);
                    }
                }
            }
        }
        
        void removeLineBreakpoint(JSLineBreakpoint jslb) {
            LineBreakpointHandler lbh;
            synchronized (lineBreakpointHandlers) {
                lbh = lineBreakpointHandlers.remove(jslb);
            }
            if (lbh != null) {
                lbh.destroy();
            }
        }
        
        void destroy() {
            if (scriptClassBP != null) {
                DebuggerManager.getDebuggerManager().removeBreakpoint(scriptClassBP);
            }
            if (scriptMethodBP != null) {
                DebuggerManager.getDebuggerManager().removeBreakpoint(scriptMethodBP);
            }
            synchronized (lineBreakpointHandlers) {
                for (LineBreakpointHandler lbh : lineBreakpointHandlers.values()) {
                    lbh.destroy();
                }
                lineBreakpointHandlers.clear();
            }
        }
    }
    
    private static final class LineBreakpointHandler {
        
        private final JPDADebugger debugger;
        private final JSLineBreakpoint jslb;
        private final Source source;
        private LineBreakpoint lb;
        
        LineBreakpointHandler(JPDADebugger debugger, JSLineBreakpoint jslb, Source source) {
            this.debugger = debugger;
            this.jslb = jslb;
            this.source = source;
            this.lb = createLineBreakpoint();
            DebuggerManager.getDebuggerManager().addBreakpoint(lb);
        }
        
        private LineBreakpoint createLineBreakpoint() {
            int lineNo = jslb.getLineNumber();
            lineNo += source.getContentLineShift();
            LineBreakpoint lb = LineBreakpoint.create("", lineNo);
            lb.setHidden(true);
            lb.setPreferredClassName(source.getClassName());
            lb.setSuspend(JPDABreakpoint.SUSPEND_EVENT_THREAD);
            lb.setSession(debugger);
            return lb;
        }
        
        void destroy() {
            DebuggerManager.getDebuggerManager().removeBreakpoint(lb);
        }
    }
    
    private static final class URLEquality {
        
        private final String protocol;
        private final String host;
        private final int port;
        private final String path;
        
        public URLEquality(URL url) {
            protocol = url.getProtocol().toLowerCase();
            String h = url.getHost();
            if (h != null) {
                h = h.toLowerCase();
            }
            host = h;
            port = url.getPort();
            path = url.getPath();
        }

        @Override
        public int hashCode() {
            return protocol.hashCode() + host.hashCode() + port + path.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof URLEquality)) {
                return false;
            }
            URLEquality ue = (URLEquality) obj;
            return protocol.equals(ue.protocol) &&
                   (host == null && ue.host == null || host != null && host.equals(ue.host)) &&
                   port == ue.port &&
                   (path == null && ue.path == null || path != null && path.equals(ue.path));
        }
        
    }

}
