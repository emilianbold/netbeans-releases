/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.ruby.debugger;

import java.util.List;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.modules.ruby.debugger.breakpoints.BreakpointModel;
import org.netbeans.modules.ruby.debugger.model.CallStackModel;
import org.netbeans.modules.ruby.debugger.model.SessionsTableModelFilter;
import org.netbeans.modules.ruby.debugger.model.ThreadsModel;
import org.netbeans.modules.ruby.debugger.model.VariablesModel;
import org.netbeans.modules.ruby.debugger.model.WatchesModel;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.viewmodel.TableModel;
import org.netbeans.spi.viewmodel.TableModelFilter;
import org.netbeans.spi.viewmodel.TreeModel;

/**
 * @author Martin Krauskopf
 */
public class ContextProviderWrapper {
    
    private ContextProvider contextProvider;
    
    private RubySession rubySession;
    private BreakpointModel breakpointModel;
    private CallStackModel callStackModel;
    private VariablesModel variablesModel;
    private WatchesModel watchesModel;
    private ThreadsModel threadsModel;
    private SessionsTableModelFilter sessionsModel;
    
    public ContextProviderWrapper(final ContextProvider contextProvider) {
        this.contextProvider = contextProvider;
    }
    
    @SuppressWarnings("unchecked")
    private static <T> List<T> debugLookup(String folder, Class<T> clazz) {
        return (List<T>) DebuggerManager.getDebuggerManager().lookup(folder, clazz);
    }
    
    @SuppressWarnings("unchecked")
    static <T> T lookupFirst(final ContextProvider cp, Class<T> clazz) {
        return (T) cp.lookupFirst(null, clazz);
    }
    
    public void fireModelChanges() {
        // refresh views
        getThreadsModel().fireChanges();
        getCallStackModel().fireChanges();
        getVariablesModel().fireChanges();
        getWatchesModel().fireChanges();
        getBreakpointModel().fireChanges();
        getSessionsModel().fireChanges();
    }
    
    public RubySession getRubySession() {
        if (rubySession == null) {
            rubySession = ContextProviderWrapper.lookupFirst(contextProvider, RubySession.class);
        }
        return rubySession;
    }
    
    public ThreadsModel getThreadsModel() {
        if (threadsModel == null) {
            threadsModel = (ThreadsModel) contextProvider.lookupFirst("ThreadsView", TreeModel.class);
        }
        return threadsModel;
    }
    
    public SessionsTableModelFilter getSessionsModel() {
        if (sessionsModel == null) {
            List<TableModelFilter> tableModels = ContextProviderWrapper.debugLookup("SessionsView", TableModelFilter.class);
            for (TableModelFilter model : tableModels) {
                if (model instanceof SessionsTableModelFilter) {
                    sessionsModel = (SessionsTableModelFilter) model;
                    break;
                }
            }
        }
        return sessionsModel;
    }
    
    public BreakpointModel getBreakpointModel() {
        if (breakpointModel == null) {
            List<TableModel> tableModels = ContextProviderWrapper.debugLookup("BreakpointsView", TableModel.class);
            for (TableModel model : tableModels) {
                if (model instanceof BreakpointModel) {
                    breakpointModel = (BreakpointModel) model;
                    break;
                }
            }
        }
        return breakpointModel;
    }
    
    public CallStackModel getCallStackModel() {
        if (callStackModel == null) {
            callStackModel = (CallStackModel) contextProvider.lookupFirst("CallStackView", TreeModel.class);
        }
        return callStackModel;
    }
    
    public VariablesModel getVariablesModel() {
        if (variablesModel == null) {
            variablesModel = (VariablesModel) contextProvider.lookupFirst("LocalsView", TreeModel.class);
        }
        return variablesModel;
    }
    
    public WatchesModel getWatchesModel() {
        if (watchesModel == null) {
            watchesModel = (WatchesModel) contextProvider.lookupFirst("WatchesView", TreeModel.class);
        }
        return watchesModel;
    }
    
}
