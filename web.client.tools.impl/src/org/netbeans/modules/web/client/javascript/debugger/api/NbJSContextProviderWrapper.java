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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.web.client.javascript.debugger.api;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.Session;
import org.netbeans.modules.web.client.javascript.debugger.NbJSDebuggerEngineProvider;
import org.netbeans.modules.web.client.javascript.debugger.models.NbJSBreakpointModel;
import org.netbeans.modules.web.client.javascript.debugger.models.NbJSCallStackModel;
import org.netbeans.modules.web.client.javascript.debugger.models.NbJSSessionsModel;
import org.netbeans.modules.web.client.javascript.debugger.models.NbJSThreadsModel;
import org.netbeans.modules.web.client.javascript.debugger.models.NbJSVariablesModel;
import org.netbeans.modules.web.client.javascript.debugger.models.NbJSWatchesModel;
import org.netbeans.modules.web.client.tools.api.JSToNbJSLocationMapper;
import org.netbeans.modules.web.client.tools.api.NbJSToJSLocationMapper;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.DebuggerEngineProvider;
import org.netbeans.spi.viewmodel.TableModel;
import org.netbeans.spi.viewmodel.TableModelFilter;
import org.netbeans.spi.viewmodel.TreeModel;

/**
 * Copied from ruby.debugger module.
 * 
 * @author Sandip V. Chitale <sandipchitale@netbeans.org>
 */
public class NbJSContextProviderWrapper {
    
    private static Map<ContextProvider, WeakReference<NbJSContextProviderWrapper>> instances =
        new WeakHashMap<ContextProvider, WeakReference<NbJSContextProviderWrapper>>();
    
    public synchronized static NbJSContextProviderWrapper getContextProviderWrapper(ContextProvider contextProvider) {
        WeakReference<NbJSContextProviderWrapper> contextProviderWrapperRef = instances.get(contextProvider);
        NbJSContextProviderWrapper contextProviderWrapper = 
                contextProviderWrapperRef != null ? contextProviderWrapperRef.get() : null;
        
        if (contextProviderWrapper == null) {
            contextProviderWrapper = new NbJSContextProviderWrapper(contextProvider);
            instances.put(contextProvider, new WeakReference<NbJSContextProviderWrapper>(contextProviderWrapper));
        }
        return contextProviderWrapper;
    }
    
    private ContextProvider contextProvider;
    
    // per IDE session
    private static NbJSBreakpointModel breakpointModel;
    private static NbJSSessionsModel sessionsModel;

    // per JS session
    private NbJSDebugger debugger;
    private NbJSToJSLocationMapper nbJSToJSLocation;
    private JSToNbJSLocationMapper jsToNbJSLocation;
    private NbJSCallStackModel callStackModel;
    private NbJSVariablesModel variablesModel;
    private NbJSWatchesModel watchesModel;
    private NbJSThreadsModel threadsModel;
    
    private NbJSContextProviderWrapper(final ContextProvider contextProvider) {
        this.contextProvider = contextProvider;
    }
    
    private static <T> List<? extends T> debugLookup(String folder, Class<T> clazz) {
        return DebuggerManager.getDebuggerManager().lookup(folder, clazz);
    }
    
    static <T> T lookupFirst(final ContextProvider cp, Class<T> clazz) {
        return cp.lookupFirst(null, clazz);
    }
    
    public void fireModelChanges() {
        // refresh views
        getThreadsModel().fireChanges();
        getCallStackModel().fireTreeChanges();
        getVariablesModel().fireChanges();
        getWatchesModel().fireChanges();
        NbJSContextProviderWrapper.getBreakpointModel().fireChanges();
        NbJSContextProviderWrapper.getSessionsModel().fireChanges();
    }
   
    public static NbJSBreakpointModel getBreakpointModel() {
        if (breakpointModel == null) {
            List<? extends TableModel> tableModels = NbJSContextProviderWrapper.debugLookup("BreakpointsView", TableModel.class);
            for (TableModel model : tableModels) {
                if (model instanceof NbJSBreakpointModel) {
                    breakpointModel = (NbJSBreakpointModel) model;
                    break;
                }
            }
        }
        return breakpointModel;
    }
    
    public static NbJSSessionsModel getSessionsModel() {
        if (sessionsModel == null) {
            List<? extends TableModelFilter> tableModels = NbJSContextProviderWrapper.debugLookup("SessionsView", TableModelFilter.class);
            for (TableModelFilter model : tableModels) {
                if (model instanceof NbJSSessionsModel) {
                    sessionsModel = (NbJSSessionsModel) model;
                    break;
                }
            }
        }
        return sessionsModel;
    }
    
    public Session getSession() {
    	return NbJSContextProviderWrapper.lookupFirst(contextProvider, Session.class);
    }
    
    public NbJSDebuggerEngineProvider getNbJSDebuggerEngineProvider() {
        for (DebuggerEngineProvider engineProvider : contextProvider.lookup(null, DebuggerEngineProvider.class)) {
            if (engineProvider instanceof NbJSDebuggerEngineProvider) {
                return (NbJSDebuggerEngineProvider)engineProvider;
            }
        }
        
        return null;
    }
    
    public NbJSDebugger getNbJSDebugger() {
        if (debugger == null) {
            debugger = NbJSContextProviderWrapper.lookupFirst(contextProvider, NbJSDebugger.class);
        }
        return debugger;
    }
    
    public NbJSToJSLocationMapper getNbJSToJSLocation() {
        if (nbJSToJSLocation == null) {
             nbJSToJSLocation = NbJSContextProviderWrapper.lookupFirst(contextProvider, NbJSToJSLocationMapper.class);
        }
    	return nbJSToJSLocation;
    }
    
    public JSToNbJSLocationMapper getJSToNbJSLocation() {
        if (jsToNbJSLocation == null) {
        	jsToNbJSLocation = NbJSContextProviderWrapper.lookupFirst(contextProvider, JSToNbJSLocationMapper.class);
        }
    	return jsToNbJSLocation;
    }
    
    public NbJSThreadsModel getThreadsModel() {
        if (threadsModel == null) {
            threadsModel = (NbJSThreadsModel) contextProvider.lookupFirst("ThreadsView", TreeModel.class);
        }
        return threadsModel;
    }
    
    public NbJSCallStackModel getCallStackModel() {
        if (callStackModel == null) {
            callStackModel = (NbJSCallStackModel) contextProvider.lookupFirst("CallStackView", TreeModel.class);
        }
        return callStackModel;
    }
    
    public NbJSVariablesModel getVariablesModel() {
        if (variablesModel == null) {
            variablesModel = (NbJSVariablesModel) contextProvider.lookupFirst("LocalsView", TreeModel.class);
        }
        return variablesModel;
    }
    
    public NbJSWatchesModel getWatchesModel() {
        if (watchesModel == null) {
            watchesModel = (NbJSWatchesModel) contextProvider.lookupFirst("WatchesView", TreeModel.class);
        }
        return watchesModel;
    }
    
}
