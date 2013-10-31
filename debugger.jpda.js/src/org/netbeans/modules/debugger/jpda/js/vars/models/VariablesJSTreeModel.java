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

package org.netbeans.modules.debugger.jpda.js.vars.models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.api.debugger.jpda.ClassVariable;
import org.netbeans.api.debugger.jpda.JPDAClassType;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.LocalVariable;
import org.netbeans.api.debugger.jpda.This;
import org.netbeans.modules.debugger.jpda.js.JSUtils;
import org.netbeans.modules.debugger.jpda.js.vars.JSThis;
import org.netbeans.modules.debugger.jpda.js.vars.JSVariable;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import org.netbeans.spi.debugger.DebuggerServiceRegistrations;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.TreeModelFilter;
import org.netbeans.spi.viewmodel.UnknownTypeException;

/**
 *
 * @author Martin
 */
@DebuggerServiceRegistrations({
    @DebuggerServiceRegistration(path="netbeans-JPDASession/JS/LocalsView",  types = TreeModelFilter.class),
    @DebuggerServiceRegistration(path="netbeans-JPDASession/JS/ResultsView", types = TreeModelFilter.class),
    @DebuggerServiceRegistration(path="netbeans-JPDASession/JS/ToolTipView", types = TreeModelFilter.class),
    @DebuggerServiceRegistration(path="netbeans-JPDASession/JS/WatchesView", types = TreeModelFilter.class),
})
public class VariablesJSTreeModel implements TreeModelFilter {
    
    private final JPDADebugger debugger;
    
    public VariablesJSTreeModel(ContextProvider lookupProvider) {
        debugger = lookupProvider.lookupFirst(null, JPDADebugger.class);
    }

    @Override
    public Object getRoot(TreeModel original) {
        return original.getRoot();
    }

    @Override
    public Object[] getChildren(TreeModel original, Object parent, int from, int to) throws UnknownTypeException {
        if (parent instanceof JSVariable) {
            return ((JSVariable) parent).getChildren();
        }
        Object[] children = original.getChildren(parent, from, to);
        List<Object> newChildren = new ArrayList<>();
        List<JSVariable> scopeVars = null;
        Map<String, LocalVariable> localVarsByName = new HashMap<>();
        JSVariable thiz = null;
        for (int i = 0; i < children.length; i++) {
            Object ch = children[i];
            if (ch instanceof LocalVariable) {
                LocalVariable lv = (LocalVariable) ch;
                String name = lv.getName();
                if (JSUtils.VAR_THIS.equals(name)) {
                    ch = createThisVar(lv);
                    thiz = (JSVariable) ch;
                    if (ch == null) {
                        continue;
                    }
                } else if (JSUtils.VAR_SCOPE.equals(name)) {
                    //newChildren.addAll(createScopeVars(lv));
                    scopeVars = createScopeVars(lv);
                    continue;
                } else if (name.startsWith(":")) {
                    continue;
                }
                localVarsByName.put(name, lv);
            }
            if (ch instanceof JPDAClassType ||
                ch instanceof ClassVariable) {
                continue;
            }
            newChildren.add(ch);
        }
        if (scopeVars != null) {
            Collections.reverse(scopeVars);
            for (JSVariable sv : scopeVars) {
                String name = sv.getKey();
                LocalVariable lv = localVarsByName.get(name);
                if (lv != null) {
                    newChildren.remove(lv);
                }
                newChildren.add(0, sv);
            }
            if (thiz != null) {
                newChildren.remove(thiz);
                newChildren.add(0, thiz);
            }
        }
        return newChildren.toArray();
    }

    @Override
    public int getChildrenCount(TreeModel original, Object node) throws UnknownTypeException {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean isLeaf(TreeModel original, Object node) throws UnknownTypeException {
        if (node instanceof JSVariable) {
            return !((JSVariable) node).isExpandable();
        }
        return original.isLeaf(node);
    }

    @Override
    public void addModelListener(ModelListener l) {
        
    }

    @Override
    public void removeModelListener(ModelListener l) {
        
    }
    
    private JSVariable createThisVar(LocalVariable lv) {
        return JSThis.create(debugger, lv);
    }
    
    private List<JSVariable> createScopeVars(LocalVariable lv) {
        return Arrays.asList(JSVariable.createScopeVars(debugger, lv));
    }
    
}
