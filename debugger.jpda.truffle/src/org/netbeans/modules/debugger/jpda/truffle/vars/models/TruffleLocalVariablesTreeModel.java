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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.netbeans.modules.debugger.jpda.truffle.access.CurrentPCInfo;
import org.netbeans.modules.debugger.jpda.truffle.access.TruffleAccess;
import org.netbeans.modules.debugger.jpda.truffle.access.TruffleStrataProvider;
import org.netbeans.modules.debugger.jpda.truffle.frames.TruffleStackFrame;
import org.netbeans.modules.debugger.jpda.truffle.vars.TruffleVariable;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import org.netbeans.spi.viewmodel.ModelEvent;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.TreeModelFilter;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.util.WeakListeners;
import org.openide.util.WeakSet;

/**
 *
 * @author Martin
 */
@DebuggerServiceRegistration(path="netbeans-JPDASession/"+TruffleStrataProvider.TRUFFLE_STRATUM+"/LocalsView",  types = TreeModelFilter.class)
public class TruffleLocalVariablesTreeModel extends TruffleVariablesTreeModel {

    private final WeakSet<CurrentPCInfo> cpisListening = new WeakSet<CurrentPCInfo>();
    private final CurrentInfoPropertyChangeListener cpiChL = new CurrentInfoPropertyChangeListener();
    
    public TruffleLocalVariablesTreeModel(ContextProvider lookupProvider) {
        super(lookupProvider);
    }
    
    @Override
    public Object[] getChildren(TreeModel original, Object parent, int from, int to) throws UnknownTypeException {
        if (parent == original.getRoot()) {
            CurrentPCInfo currentPCInfo = TruffleAccess.getCurrentPCInfo(getDebugger());
            if (currentPCInfo != null) {
                synchronized (cpisListening) {
                    if (!cpisListening.contains(currentPCInfo)) {
                        currentPCInfo.addPropertyChangeListener(
                                WeakListeners.propertyChange(cpiChL, currentPCInfo));
                        cpisListening.add(currentPCInfo);
                    }
                }
                TruffleStackFrame selectedStackFrame = currentPCInfo.getSelectedStackFrame();
                //return currentPCInfo.getVars();
                return selectedStackFrame.getVars();
            }
        } else if (parent instanceof TruffleVariable) {
            return ((TruffleVariable) parent).getChildren();
        }
        return original.getChildren(parent, from, to);
    }
    
    private void fireVarsChanged() {
        ModelEvent evt = new ModelEvent.TreeChanged(this);
        for (ModelListener l : listeners) {
            l.modelChanged(evt);
        }
    }

    private class CurrentInfoPropertyChangeListener implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            fireVarsChanged();
        }

    }

}
