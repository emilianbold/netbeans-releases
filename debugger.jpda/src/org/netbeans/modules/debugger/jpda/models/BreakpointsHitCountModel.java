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
package org.netbeans.modules.debugger.jpda.models;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.JToolTip;
import org.netbeans.api.debugger.LazyActionsManagerListener;
import org.netbeans.api.debugger.Properties;
import org.netbeans.api.debugger.jpda.JPDABreakpoint;
import org.netbeans.modules.debugger.jpda.breakpoints.BreakpointImpl;
import org.netbeans.modules.debugger.jpda.breakpoints.BreakpointsEngineListener;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import org.netbeans.spi.debugger.ui.ColumnModelRegistration;
import org.netbeans.spi.viewmodel.ColumnModel;
import org.netbeans.spi.viewmodel.ModelEvent;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.TableModel;
import org.netbeans.spi.viewmodel.TableModelFilter;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.util.NbBundle;
import org.openide.util.WeakSet;

/**
 * Displays the breakpoints hit count.
 * 
 * @author Martin Entlicher
 */
@DebuggerServiceRegistration(path="netbeans-JPDASession/BreakpointsView", types={ TableModelFilter.class })
public class BreakpointsHitCountModel implements TableModelFilter, BreakpointImpl.HitCountListener {
    
    public static final String HC_COLUMN_ID = "HitCountBreakCount";             // NOI18N
    
    private final List<ModelListener> listeners = new CopyOnWriteArrayList<ModelListener>();
    private final ContextProvider lookupProvider;
    private BreakpointsEngineListener breakpointsEngineListener;
    private final Set<BreakpointImpl> bpImplListeningOn = new WeakSet<BreakpointImpl>();
    
    public BreakpointsHitCountModel(ContextProvider lookupProvider) {
        this.lookupProvider = lookupProvider;
    }

    @Override
    public Object getValueAt(TableModel original, Object node, String columnID) throws UnknownTypeException {
        if (HC_COLUMN_ID.equals(columnID)) {
            if (node instanceof JPDABreakpoint) {
                JPDABreakpoint b = (JPDABreakpoint) node;
                BreakpointImpl bpImpl = getBPImpl(b);
                if (bpImpl == null) {
                    return NbBundle.getMessage(BreakpointsHitCountModel.class, "MSG_HitCountUnknown");
                }
                int hc = bpImpl.getCurrentHitCount();
                int bc = bpImpl.getCurrentBreakCounts();
                boolean isHCFilterSet = b.getHitCountFilter() != 0;
                int hctb = bpImpl.getHitCountsTillBreak();
                if (isHCFilterSet && hctb >= 0) {
                    //return hc2String(hc) + " / " + hc2String(bc) + " / " + hc2String(hctb);
                    return NbBundle.getMessage(BreakpointsHitCountModel.class,
                                               "MSG_HitCountWithBreakCountToBreak",
                                               hc2String(hc), hc2String(bc), hc2String(hctb));
                } else {
                    if (hc == bc) {
                        return hc2String(hc);
                    } else {
                        //return hc2String(hc) + " / " + hc2String(bc);
                        return NbBundle.getMessage(BreakpointsHitCountModel.class,
                                                   "MSG_HitCountWithBreakCount",
                                                   hc2String(hc), hc2String(bc));
                    }
                }
            } else if (node instanceof JToolTip) {
                node = ((JToolTip) node).getClientProperty("getShortDescription");     // NOI18N
                if (node instanceof JPDABreakpoint) {
                    JPDABreakpoint b = (JPDABreakpoint) node;
                    BreakpointImpl bpImpl = getBPImpl(b);
                    if (bpImpl == null) {
                        return NbBundle.getMessage(BreakpointsHitCountModel.class, "TT_HitCountUnknown");
                    }
                    int hc = bpImpl.getCurrentHitCount();
                    int bc = bpImpl.getCurrentBreakCounts();
                    int hctb = bpImpl.getHitCountsTillBreak();
                    String tt;
                    if (hctb >= 0) {
                        tt = NbBundle.getMessage(BreakpointsHitCountModel.class,
                                                 "TT_HitCountToBreak",
                                                 hc2String(hc), hc2String(bc), hc2String(hctb));
                    } else {
                        tt = NbBundle.getMessage(BreakpointsHitCountModel.class,
                                                 "TT_HitCount",
                                                 hc2String(hc), hc2String(bc));
                    }
                    return tt;
                } else {
                    throw new UnknownTypeException(node);
                }
            } else {
                throw new UnknownTypeException(node);
            }
        } else {
            return original.getValueAt(node, columnID);
        }
    }
    
    private BreakpointImpl getBPImpl(JPDABreakpoint b) {
        BreakpointsEngineListener bel = getBreakpointsEngineListener();
        if (bel == null) {
            return null;
        }
        BreakpointImpl bpImpl = bel.getBreakpointImpl(b);
        if (bpImpl != null) {
            synchronized (bpImplListeningOn) {
                if (!bpImplListeningOn.contains(bpImpl)) {
                    bpImpl.addHitCountListener(this);
                    bpImplListeningOn.add(bpImpl);
                }
            }
        }
        return bpImpl;
    }
    
    private static final String hc2String(int hc) {
        return (hc >= 0) ? Integer.toString(hc) : "?";
    }

    @Override
    public void setValueAt(TableModel original, Object node, String columnID, Object value) throws UnknownTypeException {
        original.setValueAt(node, columnID, value);
    }

    @Override
    public boolean isReadOnly(TableModel original, Object node, String columnID) throws UnknownTypeException {
        if (HC_COLUMN_ID.equals(columnID)) {
            return true;
        } else {
            return original.isReadOnly(node, columnID);
        }
    }
    
    @Override
    public void addModelListener(ModelListener l) {
        listeners.add(l);
    }

    @Override
    public void removeModelListener(ModelListener l) {
        listeners.remove(l);
    }
    
    private void fireTableModelEvent(Object node) {
        ModelEvent me = new ModelEvent.TableValueChanged(this, node, HC_COLUMN_ID);
        for (ModelListener l : listeners) {
            l.modelChanged(me);
        }
    }
    
    private BreakpointsEngineListener getBreakpointsEngineListener() {
        synchronized (this) {
            if (breakpointsEngineListener != null) {
                return breakpointsEngineListener;
            }
        }
        List<? extends LazyActionsManagerListener> lamls = lookupProvider.lookup(null, LazyActionsManagerListener.class);
        for (LazyActionsManagerListener laml : lamls) {
            if (laml instanceof BreakpointsEngineListener) {
                BreakpointsEngineListener bel = (BreakpointsEngineListener) laml;
                synchronized (this) {
                    if (breakpointsEngineListener == null) {
                        breakpointsEngineListener = bel;
                    }
                }
                return bel;
            }
        }
        return null;
    }

    @Override
    public void hitCountChanged(JPDABreakpoint bp) {
        fireTableModelEvent(bp);
    }
    
    
    @ColumnModelRegistration(path="netbeans-JPDASession/BreakpointsView")
    public static final class HCColumn extends ColumnModel {
        
        private final Properties properties = Properties.getDefault ().
            getProperties ("debugger").getProperties ("views").getProperties(HC_COLUMN_ID);

        @Override
        public String getID() {
            return HC_COLUMN_ID;
        }

        @Override
        public String getDisplayName() {
            return NbBundle.getMessage(BreakpointsHitCountModel.class, "TTL_HC_Column");
        }

        @Override
        public String getShortDescription() {
            return NbBundle.getMessage(BreakpointsHitCountModel.class, "TTL_HC_Column_Tooltip");
        }

        @Override
        public int getColumnWidth() {
            return properties.getInt("columnWidth", 80);
        }

        @Override
        public void setColumnWidth(int newColumnWidth) {
            properties.setInt("columnWidth", newColumnWidth);
        }

        @Override
        public int getCurrentOrderNumber() {
            return properties.getInt("currentOrderNumber", super.getCurrentOrderNumber());
        }

        @Override
        public void setCurrentOrderNumber(int newOrderNumber) {
            properties.setInt("currentOrderNumber", newOrderNumber);
        }

        @Override
        public boolean isSortable() {
            return true;
        }

        @Override
        public boolean isSorted() {
            return properties.getBoolean("sorted", super.isSorted());
        }

        @Override
        public boolean isSortedDescending() {
            return properties.getBoolean("sortedDescending", super.isSortedDescending());
        }

        @Override
        public void setSorted(boolean sorted) {
            properties.setBoolean("sorted", sorted);
        }

        @Override
        public void setSortedDescending(boolean sortedDescending) {
            properties.setBoolean("sortedDescending", sortedDescending);
        }

        @Override
        public boolean isVisible() {
            return properties.getBoolean("visible", false);
        }

        @Override
        public void setVisible(boolean visible) {
            properties.setBoolean("visible", visible);
        }
        
        @Override
        public Class getType() {
            return String.class;
        }
        
    }
}
