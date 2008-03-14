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

package org.netbeans.modules.ruby.debugger.breakpoints;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.netbeans.spi.viewmodel.ModelEvent;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.NodeModel;
import org.netbeans.spi.viewmodel.TableModel;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import static org.netbeans.spi.debugger.ui.Constants.BREAKPOINT_ENABLED_COLUMN_ID;

public final class BreakpointModel implements NodeModel, TableModel {
    
    public static final String LINE_BREAKPOINT =
            "org/netbeans/modules/debugger/resources/breakpointsView/Breakpoint";
    public static final String DISABLED_LINE_BREAKPOINT =
            "org/netbeans/modules/debugger/resources/breakpointsView/DisabledBreakpoint";
    
    private List<ModelListener> listeners = new CopyOnWriteArrayList<ModelListener>();
    
    // NodeModel implementation ................................................
    
    public String getDisplayName(Object node) throws UnknownTypeException {
        if (node instanceof RubyBreakpoint) {
            RubyBreakpoint breakpoint = (RubyBreakpoint) node;
            return breakpoint.getFileObject().getNameExt() + ':' +
                    breakpoint.getLineNumber();
        }
        throw new UnknownTypeException(node);
    }
    
    public String getIconBase(Object node) throws UnknownTypeException {
        if (node instanceof RubyBreakpoint) {
            if (!((RubyBreakpoint) node).isEnabled()) {
                return DISABLED_LINE_BREAKPOINT;
            }
            return LINE_BREAKPOINT;
        }
        throw new UnknownTypeException(node);
    }
    
    public String getShortDescription(Object node)
            throws UnknownTypeException {
        if (node instanceof RubyBreakpoint) {
            RubyBreakpoint breakpoint = (RubyBreakpoint) node;
            return breakpoint.getLine().getDisplayName();
        }
        throw new UnknownTypeException(node);
    }
    
    public void addModelListener(ModelListener l) {
        listeners.add(l);
    }
    
    public void removeModelListener(ModelListener l) {
        listeners.remove(l);
    }
    
    // TableModel implementation ......................................
    
    public Object getValueAt(Object node, String columnID) throws
            UnknownTypeException {
        if (node instanceof RubyBreakpoint && BREAKPOINT_ENABLED_COLUMN_ID.equals(columnID)) {
            return Boolean.valueOf(((RubyBreakpoint) node).isEnabled());
        }
        throw new UnknownTypeException(node);
    }
    
    public boolean isReadOnly(Object node, String columnID) throws
            UnknownTypeException {
        if (node instanceof RubyBreakpoint && BREAKPOINT_ENABLED_COLUMN_ID.equals(columnID)) {
            return false;
        }
        throw new UnknownTypeException(node);
    }
    
    public void setValueAt(Object node, String columnID, Object value)
            throws UnknownTypeException {
        if (node instanceof RubyBreakpoint && BREAKPOINT_ENABLED_COLUMN_ID.equals(columnID)) {
            if (((Boolean) value)) {
                ((RubyBreakpoint) node).enable();
            } else {
                ((RubyBreakpoint) node).disable();
            }
        } else {
            throw new UnknownTypeException(node);
        }
    }
    
    // TableModel implementation ......................................
    
    public void fireChanges() {
        for (ModelListener ml : listeners) {
            ml.modelChanged(new ModelEvent.TreeChanged(this));
        }
    }
    
}
