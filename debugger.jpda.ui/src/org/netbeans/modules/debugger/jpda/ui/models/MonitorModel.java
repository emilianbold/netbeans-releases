/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.debugger.jpda.ui.models;

import com.sun.jdi.ObjectCollectedException;
import com.sun.jdi.VMDisconnectedException;

import javax.swing.Action;
import org.netbeans.api.debugger.jpda.InvalidExpressionException;

import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.api.debugger.jpda.ObjectVariable;
import org.netbeans.spi.viewmodel.ComputingException;
import org.netbeans.spi.viewmodel.NoInformationException;
import org.netbeans.spi.viewmodel.NodeActionsProvider;
import org.netbeans.spi.viewmodel.NodeModel;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.TreeModelFilter;
import org.netbeans.spi.viewmodel.TreeModelListener;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.util.NbBundle;


/**
 * @author   Jan Jancura
 */
public class MonitorModel implements TreeModelFilter, NodeModel, 
NodeActionsProvider {

    public static final String CONTENDED_MONITOR =
        "org/netbeans/modules/debugger/resources/allInOneView/ContendedMonitor"; // NOI18N
    public static final String OWNED_MONITORS =
        "org/netbeans/modules/debugger/resources/allInOneView/OwnedMonitors"; // NOI18N
    public static final String MONITOR =
        "org/netbeans/modules/debugger/resources/allInOneView/Monitor"; // NOI18N

    
    // TreeView impl............................................................
    
    public Object getRoot (TreeModel model) {
        return model.getRoot ();
    }
    
    public Object[] getChildren (
        TreeModel   model, 
        Object      o, 
        int         from, 
        int         to
    ) throws NoInformationException, ComputingException, UnknownTypeException {
        if (o instanceof JPDAThread) {
            try {
                JPDAThread t = (JPDAThread) o;
                ObjectVariable contended = t.getContendedMonitor ();
                ObjectVariable[] owned = t.getOwnedMonitors ();
                int i = 0;
                Object[] os = new Object [to - from];
                if ( (contended != null) &&
                     (from  == 0) && (to > 0)
                ) os [i++] = new ContendedMonitor (contended);
                if ( (owned.length > 0) &&
                     ( ((contended != null) && (from < 2) && (to > 1)) ||
                       ((contended == null) && (from == 0) && (to > 0))
                     )
                ) os [i++] = new OwnedMonitors (owned);
                return os;
            } catch (ObjectCollectedException e) {
            } catch (VMDisconnectedException e) {
            }
            return new Object [0];
        }
        if (o instanceof OwnedMonitors) {
            OwnedMonitors om = (OwnedMonitors) o;
            Object[] fo = new Object [to - from];
            System.arraycopy (om.variables, from, fo, 0, to - from);
            return fo;
        }
        return model.getChildren (o, from, to);
    }
    
    public int getChildrenCount (
        TreeModel   model, 
        Object      o
    ) throws NoInformationException, ComputingException, UnknownTypeException {
        if (o instanceof JPDAThread) {
            try {
                JPDAThread t = (JPDAThread) o;
                ObjectVariable contended = t.getContendedMonitor ();
                ObjectVariable[] owned = t.getOwnedMonitors ();
                int i = 0;
                if (contended != null) i++;
                if (owned.length > 0) i++;
                return i;
            } catch (ObjectCollectedException e) {
            } catch (VMDisconnectedException e) {
            }
            return 0;
        }
        if (o instanceof OwnedMonitors) {
            return ((OwnedMonitors) o).variables.length;
        }
        return model.getChildrenCount (o);
    }
    
    public boolean isLeaf (TreeModel model, Object o) 
    throws UnknownTypeException {
        if (o instanceof JPDAThread) {
            try {
                JPDAThread t = (JPDAThread) o;
                return t.getContendedMonitor () == null &&
                       t.getOwnedMonitors ().length == 0;
            } catch (ObjectCollectedException e) {
            } catch (VMDisconnectedException e) {
            }
            return true;
        }
        if (o instanceof OwnedMonitors)
            return false;
        if (o instanceof ContendedMonitor)
            return true;
        if (o instanceof ObjectVariable)
            return true;
        return model.isLeaf (o);
    }
    
    
    // NodeModel impl...........................................................
    
    public String getDisplayName (Object o) throws UnknownTypeException {
        if (o instanceof ContendedMonitor) {
            ObjectVariable v = ((ContendedMonitor) o).variable;
            return java.text.MessageFormat.format(NbBundle.getBundle(MonitorModel.class).getString(
                    "CTL_MonitorModel_Column_ContendedMonitor"), new Object [] { v.getType(), v.getValue() });
        } else
        if (o instanceof OwnedMonitors) {
            return NbBundle.getBundle(MonitorModel.class).getString("CTL_MonitorModel_Column_OwnedMonitors");
        } else
        if (o instanceof ObjectVariable) {
            ObjectVariable v = (ObjectVariable) o;
            return java.text.MessageFormat.format(NbBundle.getBundle(MonitorModel.class).getString(
                    "CTL_MonitorModel_Column_Monitor"), new Object [] { v.getType(), v.getValue() });
        } else
        throw new UnknownTypeException (o);
    }
    
    public String getShortDescription (Object o) throws UnknownTypeException {
        if (o instanceof ContendedMonitor) {
            ObjectVariable v = ((ContendedMonitor) o).variable;
            try {
                return "(" + v.getType () + ") " + v.getToStringValue ();
            } catch (InvalidExpressionException ex) {
                return ex.getLocalizedMessage ();
            }
        } else
        if (o instanceof OwnedMonitors) {
            return null;
        } else
        if (o instanceof ObjectVariable) {
            ObjectVariable v = (ObjectVariable) o;
            try {
                return "(" + v.getType () + ") " + v.getToStringValue ();
            } catch (InvalidExpressionException ex) {
                return ex.getLocalizedMessage ();
            }
        } else
        throw new UnknownTypeException (o);
    }
    
    public String getIconBase (Object o) throws UnknownTypeException {
        if (o instanceof ContendedMonitor) {
            return CONTENDED_MONITOR;
        } else
        if (o instanceof OwnedMonitors) {
            return OWNED_MONITORS;
        } else
        if (o instanceof ObjectVariable) {
            return MONITOR;
        } else
        throw new UnknownTypeException (o);
    }

    public void addTreeModelListener (TreeModelListener l) {
    }

    public void removeTreeModelListener (TreeModelListener l) {
    }
    
    
    // NodeActionsProvider impl.................................................
    
    public Action[] getActions (Object o) throws UnknownTypeException {
        if (o instanceof ContendedMonitor) {
            return new Action [0];
        } else
        if (o instanceof OwnedMonitors) {
            return new Action [0];
        } else
        if (o instanceof ObjectVariable) {
            return new Action [0];
        } else
        throw new UnknownTypeException (o);
    }
    
    public void performDefaultAction (Object o) throws UnknownTypeException {
        if (o instanceof ContendedMonitor) {
            return;
        } else
        if (o instanceof OwnedMonitors) {
            return;
        } else
        if (o instanceof ObjectVariable) {
            return;
        } else
        throw new UnknownTypeException (o);
    }
    
    
    // innerclasses ............................................................
    
    private static class OwnedMonitors {
        ObjectVariable[] variables;
        
        OwnedMonitors (ObjectVariable[] vs) {
            variables = vs;
        }
    }
    
    private static class ContendedMonitor {
        ObjectVariable variable;
        
        ContendedMonitor (ObjectVariable v) {
            variable = v;
        }
    }
}
