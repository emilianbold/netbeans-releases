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

package org.netbeans.modules.debugger.jpda.ui.models;

import com.sun.jdi.AbsentInformationException;
import org.netbeans.api.debugger.jpda.CallStackFrame;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.modules.debugger.jpda.ui.models.SourcesModel.AbstractColumn;
import org.netbeans.spi.debugger.ContextProvider;

import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.util.NbBundle;

/**
 *
 * @author martin
 */
public class DebuggingTreeModel implements TreeModel {
    
    private JPDADebugger debugger;
    
    public DebuggingTreeModel(ContextProvider lookupProvider) {
        debugger = lookupProvider.lookupFirst(null, JPDADebugger.class);
    }

    public Object[] getChildren(Object parent, int from, int to) throws UnknownTypeException {
        if (parent == ROOT) {
            return debugger.getAllThreads().toArray();
        }
        if (parent instanceof JPDAThread) {
            JPDAThread t = (JPDAThread) parent;
            try {
                return t.getCallStack();
            } catch (AbsentInformationException aiex) {
                return new Object[0];
            }
        }
        if (parent instanceof CallStackFrame) {
            return new Object[0];
        }
        throw new UnknownTypeException(parent.toString());
    }

    public int getChildrenCount(Object node) throws UnknownTypeException {
        if (node instanceof CallStackFrame) {
            return 0;
        }
        if (node instanceof JPDAThread) {
            if (!((JPDAThread) node).isSuspended()) {
                return 0;
            }
        }
        return Integer.MAX_VALUE;
    }

    public Object getRoot() {
        return ROOT;
    }

    public boolean isLeaf(Object node) throws UnknownTypeException {
        if (node instanceof CallStackFrame) {
            return true;
        }
        if (node instanceof JPDAThread) {
            if (!((JPDAThread) node).isSuspended()) {
                return true;
            }
        }
        return false;
    }

    public void addModelListener(ModelListener l) {
        
    }

    public void removeModelListener(ModelListener l) {
        
    }


    
    /**
     * Defines model for one table view column. Can be used together with 
     * {@link org.netbeans.spi.viewmodel.TreeModel} for tree table view representation.
     */
    public static class DefaultDebuggingColumn extends AbstractColumn {

        /**
         * Returns unique ID of this column.
         *
         * @return unique ID of this column
         */
        public String getID () {
            return "DefaultDebuggingColumn";
        }

        /** 
         * Returns display name of this column.
         *
         * @return display name of this column
         */
        public String getDisplayName () {
            return NbBundle.getBundle (DebuggingTreeModel.class).
                getString ("CTL_Debugging_Column_Name_Name");
        }

        public Character getDisplayedMnemonic() {
            return new Character(NbBundle.getBundle(DebuggingTreeModel.class).getString 
                ("CTL_Debugging_Column_Name_Name_Mnc").charAt(0));
        }

        /**
         * Returns tooltip for given column.
         *
         * @return  tooltip for given node
         */
        public String getShortDescription () {
            return NbBundle.getBundle (DebuggingTreeModel.class).getString
                ("CTL_Debugging_Column_Name_Desc");
        }

        /**
         * Returns type of column items.
         *
         * @return type of column items
         */
        public Class getType () {
            return null;
        }
    }
    
    /**
     * Defines model for one table view column. Can be used together with 
     * {@link org.netbeans.spi.viewmodel.TreeModel} for tree table view representation.
     */
    public static class DebuggingSuspendColumn extends AbstractColumn {

        /**
         * Returns unique ID of this column.
         *
         * @return unique ID of this column
         */
        public String getID () {
            return "suspend";
        }

        /** 
         * Returns display name of this column.
         *
         * @return display name of this column
         */
        public String getDisplayName () {
            return NbBundle.getBundle (DebuggingTreeModel.class).getString 
                ("CTL_Debugging_Column_Suspend_Name");
        }

        public Character getDisplayedMnemonic() {
            return new Character(NbBundle.getBundle(DebuggingTreeModel.class).getString 
                ("CTL_Debugging_Column_Suspend_Name_Mnc").charAt(0));
        }

        /**
         * Returns type of column items.
         *
         * @return type of column items
         */
        public Class getType () {
            return Boolean.TYPE;
        }

        /**
         * Returns tooltip for given column. Default implementation returns 
         * <code>null</code> - do not use tooltip.
         *
         * @return  tooltip for given node or <code>null</code>
         */
        public String getShortDescription () {
            return NbBundle.getBundle (DebuggingTreeModel.class).getString 
                ("CTL_Debugging_Column_Suspend_Desc");
        }

        /**
         * True if column should be visible by default. Default implementation 
         * returns <code>true</code>.
         *
         * @return <code>true</code> if column should be visible by default
         */
        public boolean initiallyVisible () {
            return true;
        }
    }
    
}
