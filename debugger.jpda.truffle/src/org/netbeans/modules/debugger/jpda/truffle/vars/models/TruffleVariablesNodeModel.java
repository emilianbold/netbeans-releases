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

import java.awt.datatransfer.Transferable;
import java.io.IOException;
import java.util.List;
import org.netbeans.api.debugger.jpda.Field;
import org.netbeans.api.debugger.jpda.InvalidExpressionException;
import org.netbeans.api.debugger.jpda.JPDAClassType;
import org.netbeans.api.debugger.jpda.ObjectVariable;
import org.netbeans.api.debugger.jpda.Super;
import org.netbeans.api.debugger.jpda.This;
import org.netbeans.api.debugger.jpda.Variable;
import org.netbeans.modules.debugger.jpda.truffle.access.TruffleStrataProvider;
import org.netbeans.modules.debugger.jpda.truffle.vars.TruffleVariable;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import org.netbeans.spi.debugger.DebuggerServiceRegistrations;
import org.netbeans.spi.viewmodel.ExtendedNodeModel;
import org.netbeans.spi.viewmodel.ExtendedNodeModelFilter;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.NodeModel;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.util.datatransfer.PasteType;

/**
 *
 */
@DebuggerServiceRegistrations({
    @DebuggerServiceRegistration(path="netbeans-JPDASession/"+TruffleStrataProvider.TRUFFLE_STRATUM+"/LocalsView",  types=ExtendedNodeModelFilter.class),
    @DebuggerServiceRegistration(path="netbeans-JPDASession/"+TruffleStrataProvider.TRUFFLE_STRATUM+"/ResultsView", types=ExtendedNodeModelFilter.class),
    @DebuggerServiceRegistration(path="netbeans-JPDASession/"+TruffleStrataProvider.TRUFFLE_STRATUM+"/ToolTipView", types=ExtendedNodeModelFilter.class),
    @DebuggerServiceRegistration(path="netbeans-JPDASession/"+TruffleStrataProvider.TRUFFLE_STRATUM+"/WatchesView", types=ExtendedNodeModelFilter.class),
})
public class TruffleVariablesNodeModel implements ExtendedNodeModelFilter {

    @Override
    public boolean canRename(ExtendedNodeModel original, Object node) throws UnknownTypeException {
        return original.canRename(node);
    }

    @Override
    public boolean canCopy(ExtendedNodeModel original, Object node) throws UnknownTypeException {
        return original.canCopy(node);
    }

    @Override
    public boolean canCut(ExtendedNodeModel original, Object node) throws UnknownTypeException {
        return original.canCut(node);
    }

    @Override
    public Transferable clipboardCopy(ExtendedNodeModel original, Object node) throws IOException, UnknownTypeException {
        return original.clipboardCopy(node);
    }

    @Override
    public Transferable clipboardCut(ExtendedNodeModel original, Object node) throws IOException, UnknownTypeException {
        return original.clipboardCut(node);
    }

    @Override
    public PasteType[] getPasteTypes(ExtendedNodeModel original, Object node, Transferable t) throws UnknownTypeException {
        return original.getPasteTypes(node, t);
    }

    @Override
    public void setName(ExtendedNodeModel original, Object node, String name) throws UnknownTypeException {
        original.setName(node, name);
    }

    @Override
    public String getIconBaseWithExtension(ExtendedNodeModel original, Object node) throws UnknownTypeException {
        if (node instanceof TruffleVariable) {
            String name = ((TruffleVariable) node).getName();
            if ("this".equals(name)) {
                return original.getIconBaseWithExtension(EmptyThis.INSTANCE);
            } else {
                return original.getIconBaseWithExtension(EmptyVar.INSTANCE);
            }
        }
        return original.getIconBaseWithExtension(node);
    }

    @Override
    public String getDisplayName(NodeModel original, Object node) throws UnknownTypeException {
        if (node instanceof TruffleVariable) {
            return ((TruffleVariable) node).getName();
        } else {
            return original.getDisplayName(node);
        }
    }

    @Override
    public String getIconBase(NodeModel original, Object node) throws UnknownTypeException {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public String getShortDescription(NodeModel original, Object node) throws UnknownTypeException {
        if (node instanceof TruffleVariable) {
            TruffleVariable var = (TruffleVariable) node;
            return var.getName() + " = " + var.getValue();
        }
        return original.getShortDescription(node);
    }

    @Override
    public void addModelListener(ModelListener l) {
    }

    @Override
    public void removeModelListener(ModelListener l) {
    }
    
    private static final class EmptyThis implements This {
        
        static This INSTANCE = new EmptyThis();

        @Override
        public String getToStringValue() throws InvalidExpressionException { return "empty"; }

        @Override
        public Variable invokeMethod(String methodName, String signature, Variable[] arguments) throws NoSuchMethodException, InvalidExpressionException {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public int getFieldsCount() { return  0; }

        @Override
        public Field getField(String name) { return null; }

        @Override
        public Field[] getFields(int from, int to) { return null; }

        @Override
        public Field[] getAllStaticFields(int from, int to) { return null; }

        @Override
        public Field[] getInheritedFields(int from, int to) { return null; }

        @Override
        public List<ObjectVariable> getReferringObjects(long maxReferrers) throws UnsupportedOperationException {
            return null;
        }

        @Override
        public Super getSuper() { return null; }

        @Override
        public JPDAClassType getClassType() { return null; }

        @Override
        public long getUniqueID() { return 0l; }

        @Override
        public String getType() { return "empty"; }

        @Override
        public String getValue() { return ""; }

        @Override
        public Object createMirrorObject() { return null; }
        
    }
    
    private static final class EmptyVar implements Variable {
        
        static Variable INSTANCE = new EmptyVar();

        @Override
        public String getType() { return "empty"; }

        @Override
        public String getValue() { return ""; }

        @Override
        public Object createMirrorObject() { return null; }
        
    }
    
}
