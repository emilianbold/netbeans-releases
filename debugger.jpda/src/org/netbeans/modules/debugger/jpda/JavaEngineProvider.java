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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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

package org.netbeans.modules.debugger.jpda;

import java.awt.Component;
import java.beans.DesignMode;
import java.beans.beancontext.BeanContextChildComponentProxy;
import java.util.prefs.Preferences;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.spi.debugger.DebuggerEngineProvider;
import org.netbeans.spi.debugger.ContextProvider;
import org.openide.util.NbPreferences;
import org.openide.util.RequestProcessor;
import org.openide.windows.WindowManager;


/**
 * Represents one debugger plug-in - one Debugger Implementation.
 * Each Debugger Implementation can add support for debugging of some
 * language or environment to the IDE.
 *
 * @author Jan Jancura
 */
public class JavaEngineProvider extends DebuggerEngineProvider {

    private DebuggerEngine.Destructor   desctuctor;
    private Session                     session;
    private RequestProcessor            jpdaRP = new RequestProcessor("JPDA Debugger", 5);
    
    public JavaEngineProvider (ContextProvider contextProvider) {
        session = contextProvider.lookupFirst(null, Session.class);
    }
    
    public String[] getLanguages () {
        return new String[] {"Java"};
    }

    public String getEngineTypeID () {
        return JPDADebugger.ENGINE_ID;
    }
    
    public Object[] getServices () {
        Object[] components = getUIComponentProxies();
        Object[] services = new Object[components.length + 1];
        System.arraycopy(components, 0, services, 0, components.length);
        services[components.length] = jpdaRP;
        return services;
    }
    
    static Object[] getUIComponentProxies() {
        
        class ComponentProxy implements BeanContextChildComponentProxy, DesignMode {
            private String name;
            private boolean openByDefault;
            ComponentProxy(String name, boolean openByDefault) {
                this.name = name;
                this.openByDefault = openByDefault;
            }
            public Component getComponent() {
                return WindowManager.getDefault().findTopComponent(name);
            }
            public boolean isDesignTime() {
                return openByDefault;
            }
            public void setDesignTime(boolean designTime) {
                throw new UnsupportedOperationException("Not supported.");
            }
        }

        class WatchesComponentProxy extends ComponentProxy {
            WatchesComponentProxy(String name, boolean openByDefault) {
                super(name, openByDefault);
            }
            @Override
            public boolean isDesignTime() {
                Preferences preferences = NbPreferences.forModule(ContextProvider.class).node("variables_view"); // NOI18N
                return !preferences.getBoolean("show_watches", true); // NOI18N
            }
        }

        return new Object [] {
            new ComponentProxy("localsView", true),
            new WatchesComponentProxy("watchesView", true),
            new ComponentProxy("breakpointsView", true),
            new ComponentProxy("debugging", true),
            // Initially closed components
            new ComponentProxy("evaluator", false),
            new ComponentProxy("resultsView", false),
            new ComponentProxy("callstackView", false),
            new ComponentProxy("sessionsView", false),
            new ComponentProxy("sources", false),
            new ComponentProxy("threadsView", false),
            new ComponentProxy("classes", false),
        };
    }
    
    public void setDestructor (DebuggerEngine.Destructor desctuctor) {
        this.desctuctor = desctuctor;
    }
    
    public DebuggerEngine.Destructor getDestructor () {
        return desctuctor;
    }
    
    public Session getSession () {
        return session;
    }

    RequestProcessor getRequestProcessor() {
        return jpdaRP;
    }
}

