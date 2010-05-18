/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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

package org.netbeans.modules.web.client.javascript.debugger.models;

import static org.netbeans.spi.debugger.ui.Constants.SESSION_HOST_NAME_COLUMN_ID;
import static org.netbeans.spi.debugger.ui.Constants.SESSION_LANGUAGE_COLUMN_ID;
import static org.netbeans.spi.debugger.ui.Constants.SESSION_STATE_COLUMN_ID;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.Session;
import org.netbeans.modules.web.client.javascript.debugger.api.NbJSDebugger;
import org.netbeans.modules.web.client.tools.javascript.debugger.api.JSDebuggerEvent;
import org.netbeans.modules.web.client.tools.javascript.debugger.api.JSDebuggerEventListener;
import org.netbeans.spi.viewmodel.ModelEvent;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.TableModel;
import org.netbeans.spi.viewmodel.TableModelFilter;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;

public final class NbJSSessionsModel implements TableModelFilter, JSDebuggerEventListener  {

    private final List<ModelListener> listeners;

    public NbJSSessionsModel() {
        listeners = new CopyOnWriteArrayList<ModelListener>();
    }

    public Object getValueAt(TableModel original, Object row, String columnID) throws UnknownTypeException {
        if (row instanceof Session && isJSDebuggerSession((Session) row)) {
            if (SESSION_STATE_COLUMN_ID.equals(columnID)) {
                return getSessionState((Session) row);
            } else if (SESSION_LANGUAGE_COLUMN_ID.equals(columnID)) {
                return row;
            } else if (SESSION_HOST_NAME_COLUMN_ID.equals(columnID)) {
                return ((Session) row).getLocationName();
            } else {
                throw new UnknownTypeException(row);
            }
        }
        return original.getValueAt(row, columnID);
    }

    public boolean isReadOnly(TableModel original, Object row, String columnID) throws UnknownTypeException {
        return original.isReadOnly(row, columnID);
    }

    public void setValueAt(TableModel original, Object row, String columnID, Object value) throws UnknownTypeException {
        original.setValueAt(row, columnID, value);
    }

    private static boolean isJSDebuggerSession(final Session s) {
        DebuggerEngine e = s.getCurrentEngine();
        if (e == null) {
            return false;
        }
        return e.lookupFirst(null, NbJSDebugger.class) != null;
    }
    
    private Map<NbJSDebugger,Boolean> map = new WeakHashMap<NbJSDebugger, Boolean>();
    
    private Object getSessionState(final Session s) {
        NbJSDebugger debugger = s.getCurrentEngine().lookupFirst(null, NbJSDebugger.class);;
        if (debugger == null) {
            return getMessage("MSG_Session.State.Starting");
        }
        
        /* Joelle: I don't need to remove the listener because it should get automatically garbage collected.   */
        synchronized (this) {
            if (!(map.containsKey(debugger) && map.get(debugger).booleanValue())) {
                debugger.addJSDebuggerEventListener(WeakListeners.create(JSDebuggerEventListener.class, this, debugger));
                map.put(debugger, Boolean.TRUE);
            }
        }
        return debugger.getState();
    }

    public void addModelListener(ModelListener l) {
        listeners.add(l);
    }

    public void removeModelListener(ModelListener l) {
        listeners.remove(l);
    }

    private static String getMessage(final String key) {
        return NbBundle.getMessage(NbJSSessionsModel.class, key);
    }
    
    public void fireChanges() {
        for ( ModelListener modelListener : listeners){
        modelListener.modelChanged(new ModelEvent.TreeChanged(this));
        }
    }
    
    public void fireTableValueChanges(Object source, Object node, String columnID) {
        for ( ModelListener modelListener : listeners){
            modelListener.modelChanged(new ModelEvent.TableValueChanged(source, node, columnID));
       }
    }
    
    public void onDebuggerEvent(JSDebuggerEvent debuggerEvent) {
        fireChanges();
        
    }
}
