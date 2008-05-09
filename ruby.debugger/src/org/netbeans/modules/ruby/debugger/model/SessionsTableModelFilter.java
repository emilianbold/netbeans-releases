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

package org.netbeans.modules.ruby.debugger.model;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.Session;
import org.netbeans.modules.ruby.debugger.RubySession;
import org.netbeans.spi.viewmodel.ModelEvent;
import org.netbeans.spi.viewmodel.TableModel;
import org.netbeans.spi.viewmodel.TableModelFilter;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.util.NbBundle;
import static org.netbeans.spi.debugger.ui.Constants.SESSION_STATE_COLUMN_ID;
import static org.netbeans.spi.debugger.ui.Constants.SESSION_LANGUAGE_COLUMN_ID;
import static org.netbeans.spi.debugger.ui.Constants.SESSION_HOST_NAME_COLUMN_ID;

public final class SessionsTableModelFilter implements TableModelFilter {

    private final List<ModelListener> listeners;

    public SessionsTableModelFilter() {
        listeners = new CopyOnWriteArrayList<ModelListener>();
    }

    public Object getValueAt(TableModel original, Object row, String columnID) throws UnknownTypeException {
        if (row instanceof Session && isRubySession((Session) row)) {
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

    private static boolean isRubySession(final Session s) {
        DebuggerEngine e = s.getCurrentEngine();
        if (e == null) {
            return false;
        }
        return e.lookupFirst(null, RubySession.class) != null;
    }

    private String getSessionState(final Session s) {
        DebuggerEngine e = s.getCurrentEngine();
        if (e == null) {
            return getMessage("MSG_Session.State.Starting");
        }
        RubySession session = e.lookupFirst(null, RubySession.class);
        String state;
        switch (session.getState()) {
            case RUNNING:
                state = getMessage("MSG_Session.State.Running");
                break;
            case STARTING:
                state = getMessage("MSG_Session.State.Starting");
                break;
            case STOPPED:
                state = getMessage("MSG_Session.State.Stopped");
                break;
            default:
                state = null;
        }
        return state;
    }

    public void addModelListener(ModelListener l) {
        listeners.add(l);
    }

    public void removeModelListener(ModelListener l) {
        listeners.remove(l);
    }

    public void fireChanges() {
        for (ModelListener listener : listeners) {
            listener.modelChanged(new ModelEvent.TreeChanged(this));
        }
    }

    private static String getMessage(final String key) {
        return NbBundle.getMessage(SessionsTableModelFilter.class, key);
    }
}
