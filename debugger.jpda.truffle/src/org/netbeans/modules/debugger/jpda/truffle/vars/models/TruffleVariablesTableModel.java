/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */

package org.netbeans.modules.debugger.jpda.truffle.vars.models;

import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.JPDAWatch;
import org.netbeans.api.debugger.jpda.Variable;
import org.netbeans.modules.debugger.jpda.truffle.access.TruffleStrataProvider;
import org.netbeans.modules.debugger.jpda.truffle.vars.TruffleVariable;
import org.netbeans.modules.debugger.jpda.truffle.vars.TruffleVariableImpl;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import org.netbeans.spi.debugger.DebuggerServiceRegistrations;
import static org.netbeans.spi.debugger.ui.Constants.LOCALS_TO_STRING_COLUMN_ID;
import static org.netbeans.spi.debugger.ui.Constants.LOCALS_TYPE_COLUMN_ID;
import static org.netbeans.spi.debugger.ui.Constants.LOCALS_VALUE_COLUMN_ID;
import static org.netbeans.spi.debugger.ui.Constants.WATCH_TO_STRING_COLUMN_ID;
import static org.netbeans.spi.debugger.ui.Constants.WATCH_TYPE_COLUMN_ID;
import static org.netbeans.spi.debugger.ui.Constants.WATCH_VALUE_COLUMN_ID;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.TableHTMLModel;
import org.netbeans.spi.viewmodel.TableHTMLModelFilter;
import org.netbeans.spi.viewmodel.TableModel;
import org.netbeans.spi.viewmodel.TableModelFilter;
import org.netbeans.spi.viewmodel.UnknownTypeException;

/**
 *
 */
@DebuggerServiceRegistrations({
    @DebuggerServiceRegistration(path="netbeans-JPDASession/"+TruffleStrataProvider.TRUFFLE_STRATUM+"/LocalsView",  types = TableModelFilter.class),
    @DebuggerServiceRegistration(path="netbeans-JPDASession/"+TruffleStrataProvider.TRUFFLE_STRATUM+"/ResultsView", types = TableModelFilter.class),
    @DebuggerServiceRegistration(path="netbeans-JPDASession/"+TruffleStrataProvider.TRUFFLE_STRATUM+"/ToolTipView", types = TableModelFilter.class),
    @DebuggerServiceRegistration(path="netbeans-JPDASession/"+TruffleStrataProvider.TRUFFLE_STRATUM+"/WatchesView", types = TableModelFilter.class)
})
public class TruffleVariablesTableModel implements TableModelFilter, TableHTMLModelFilter {
    
    private final JPDADebugger debugger;
    
    public TruffleVariablesTableModel(ContextProvider contextProvider) {
        debugger = contextProvider.lookupFirst(null, JPDADebugger.class);
    }

    @Override
    public Object getValueAt(TableModel original, Object node, String columnID) throws UnknownTypeException {
        TruffleVariable tv = null;
        if (node instanceof JPDAWatch) {// && !isEnabled((JPDAWatch) node)) {
            Object orig = original.getValueAt(node, columnID); // Call in any case because of error displaying
            if (node instanceof Variable) {
                tv = TruffleVariableImpl.get((Variable) node);
            }
            if (tv == null) {
                return orig;
            }
        }
        if (node instanceof TruffleVariable) {
            tv = (TruffleVariable) node;
        }
        if (tv != null) {
            switch (columnID) {
                case LOCALS_TYPE_COLUMN_ID:
                case WATCH_TYPE_COLUMN_ID:
                    return tv.getType();
                case LOCALS_VALUE_COLUMN_ID:
                case WATCH_VALUE_COLUMN_ID:
                    return tv.getValue();
                case LOCALS_TO_STRING_COLUMN_ID:
                case WATCH_TO_STRING_COLUMN_ID:
                    Object var = tv.getValue();
                    return String.valueOf(var);
            }
        }
        return original.getValueAt(node, columnID);
    }

    @Override
    public boolean isReadOnly(TableModel original, Object node, String columnID) throws UnknownTypeException {
        if (node instanceof TruffleVariable) {
            return true;
        }
        return original.isReadOnly(node, columnID);
    }

    @Override
    public void setValueAt(TableModel original, Object node, String columnID, Object value) throws UnknownTypeException {
        original.setValueAt(node, columnID, value);
    }

    @Override
    public boolean hasHTMLValueAt(TableHTMLModel original, Object node, String columnID) throws UnknownTypeException {
        if (node instanceof TruffleVariable) {
            return false;
        }
        return original.hasHTMLValueAt(node, columnID);
    }

    @Override
    public String getHTMLValueAt(TableHTMLModel original, Object node, String columnID) throws UnknownTypeException {
        return original.getHTMLValueAt(node, columnID);
    }

    @Override
    public void addModelListener(ModelListener l) {
    }

    @Override
    public void removeModelListener(ModelListener l) {
    }

}
