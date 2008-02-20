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

public class ContextProviderWrapper {
    
    private ContextProvider contextProvider;
    
    // per IDE session
    private static BreakpointModel breakpointModel;
    private static SessionsTableModelFilter sessionsModel;

    // per Ruby session
    private RubySession rubySession;
    private CallStackModel callStackModel;
    private VariablesModel variablesModel;
    private WatchesModel watchesModel;
    private ThreadsModel threadsModel;
    
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
        ContextProviderWrapper.getBreakpointModel().fireChanges();
        ContextProviderWrapper.getSessionsModel().fireChanges();
    }

    public static SessionsTableModelFilter getSessionsModel() {
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
    
    public static BreakpointModel getBreakpointModel() {
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
