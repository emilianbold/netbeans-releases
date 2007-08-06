/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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

/**
 * @author Martin Krauskopf
 */
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
        RubySession session = (RubySession) e.lookupFirst (null, RubySession.class);
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
