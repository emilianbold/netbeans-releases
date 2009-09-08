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

package org.netbeans.modules.cnd.debugger.gdb.models;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.netbeans.api.debugger.Session;
import org.netbeans.modules.cnd.debugger.common.models.BoldVariablesTableModelFilterFirst;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.viewmodel.NodeModel;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.util.NbBundle;

import org.netbeans.modules.cnd.debugger.gdb.GdbCallStackFrame;
import org.netbeans.modules.cnd.debugger.gdb.GdbDebugger;
import org.netbeans.modules.cnd.debugger.gdb.GdbVariable;


/**
 * @author   Gordon Prieur (copied from Jan Jancura's JPDA implementation)
 */
public class CallStackNodeModel implements NodeModel {

    public static final String CALL_STACK =
        "org/netbeans/modules/debugger/resources/callStackView/NonCurrentFrame"; // NOI18N
    public static final String CURRENT_CALL_STACK =
        "org/netbeans/modules/debugger/resources/callStackView/CurrentFrame"; // NOI18N

    private final GdbDebugger debugger;
    private final Session session;
    private final List<ModelListener> listeners = new CopyOnWriteArrayList<ModelListener>();
    
    
    public CallStackNodeModel(ContextProvider lookupProvider) {
        debugger = lookupProvider.lookupFirst(null, GdbDebugger.class);
        session = lookupProvider.lookupFirst(null, Session.class);
        new Listener(this, debugger);
    }
    
    public String getDisplayName(Object o) throws UnknownTypeException {
        if (o == TreeModel.ROOT) {
            return NbBundle.getBundle(CallStackNodeModel.class).getString("CTL_CallstackModel_Column_Name_Name"); // NOI18N
        } else if (o instanceof GdbCallStackFrame) {
            GdbCallStackFrame sf = (GdbCallStackFrame) o;
            GdbCallStackFrame ccsf = debugger.getCurrentCallStackFrame();
            if (ccsf != null && ccsf.equals(sf)) { 
                return BoldVariablesTableModelFilterFirst.toHTML(getCSFName(session, sf, false),
			true, false, null);
	    }
            return getCSFName(session, sf, false);
        } else if ("No current thread" == o) { // NOI18N
            return NbBundle.getMessage(CallStackNodeModel.class, "NoCurrentThread"); // NOI18N
        } else if ("Thread is running" == o) { // NOI18N
            return NbBundle.getMessage(CallStackNodeModel.class, "ThreadIsRunning"); // NOI18N
        } else {
	    throw new UnknownTypeException(o);
	}
    }
    
    public String getShortDescription(Object o) throws UnknownTypeException {
        if (o == TreeModel.ROOT) {
            return NbBundle.getBundle(CallStackNodeModel.class).getString("CTL_CallstackModel_Column_Name_Desc"); // NOI18N
        } else if (o instanceof GdbCallStackFrame) {
            GdbCallStackFrame sf = (GdbCallStackFrame) o;
            return getCSFName(session, sf, true);
        } else if ("No current thread" == o) { // NOI18N
            return NbBundle.getMessage(CallStackNodeModel.class, "NoCurrentThread"); // NOI18N
        } else if ("Thread is running" == o) { // NOI18N
            return NbBundle.getMessage(CallStackNodeModel.class, "ThreadIsRunning"); // NOI18N
        } else {
	    throw new UnknownTypeException (o);
	}
    }
    
    public String getIconBase(Object node) throws UnknownTypeException {
        if (node instanceof String) {
	    return null;
	}
        if (node instanceof GdbCallStackFrame) {
            GdbCallStackFrame ccsf = debugger.getCurrentCallStackFrame();
            if (ccsf != null && ccsf.equals(node)) {
		return CURRENT_CALL_STACK;
	    }
            return CALL_STACK;
        }
        throw new UnknownTypeException(node);
    }

    /** 
     *
     * @param l the listener to add
     */
    public void addModelListener(ModelListener l) {
        listeners.add(l);
    }

    /** 
     *
     * @param l the listener to remove
     */
    public void removeModelListener(ModelListener l) {
        listeners.remove(l);
    }
    
    private void fireTreeChanged() {
        for (ModelListener listener : listeners) {
            listener.modelChanged(null);
        }
    }

    /** 
     * Gets Call Stack Frame name.
     * Logic scheme: 
     * By default return function name and filename:line.
     * If function name is not available, return address and filename:line.
     * If function name and address are not available, return filename.
     *
     * @param s Session
     * @param csf Call Stack Frame
     * @param l A boolean flag to define filename format (l=true means fullname)
     * @return Call Stack Frame name.
     */
    public static String getCSFName(Session s, GdbCallStackFrame csf, boolean useFullName) {
        String csfName;
        String functionName = csf.getFunctionName();
        
        if (functionName != null && !functionName.equals("??")) { // NOI18N
            // By default use function name
            csfName = functionName;

            // add arguments
            Collection<GdbVariable> args = csf.getArguments();
            if (args != null) {
                StringBuilder sb = new StringBuilder(csfName);
                sb.append('(');
                for (Iterator<GdbVariable> iter = args.iterator(); iter.hasNext();) {
                    GdbVariable arg = iter.next();
                    sb.append(arg.getName());
                    sb.append('=');
                    sb.append(arg.getValue());
                    if (iter.hasNext()) {
                        sb.append(',');
                    }
                }
                sb.append(')');
                csfName = sb.toString();
            }
        } else if (csf.getAddr() != null) {  
            //If function name is not available, use address
            csfName= NbBundle.getMessage(CallStackNodeModel.class,
			"CTL_CallstackModel_Msg_Format", csf.getAddr()); // NOI18N
	} else {
            csfName = ""; // NOI18N
        }     
        // add filename:line, if no functionName available use full path name.
        int ln = csf.getLineNumber();
        if (csfName.length() == 0) {
            String fileName = useFullName ? csf.getFullname() : csf.getFileName();
            if (ln < 0) {
                if (fileName == null) {
                    csfName = "??"; // NOI18N
                } else {
                    csfName = fileName;
                }
            }
        } else {
            String fileName = csf.getFileName();
            if (fileName != null && ln >= 0) {
                csfName = NbBundle.getMessage(CallStackNodeModel.class, "FMT_StackFrame", // NOI18N
                        new Object[] { csfName, fileName, String.valueOf(ln) });
            }
	}
        return csfName;
    }
            
    
    // innerclasses ............................................................
    
    /**
     * Listens on DebuggerManager on PROP_CURRENT_ENGINE, and on 
     * currentTreeModel.
     */
    private static class Listener implements PropertyChangeListener {
        
        private final WeakReference<CallStackNodeModel> ref;
        private final GdbDebugger debugger;
        
        private Listener(CallStackNodeModel rm, GdbDebugger debugger) {
            ref = new WeakReference<CallStackNodeModel>(rm);
            this.debugger = debugger;
            debugger.addPropertyChangeListener(GdbDebugger.PROP_CURRENT_CALL_STACK_FRAME, this);
        }
        
        private CallStackNodeModel getModel() {
            CallStackNodeModel rm = ref.get();
            if (rm == null) {
                debugger.removePropertyChangeListener(GdbDebugger.PROP_CURRENT_CALL_STACK_FRAME, this);
            }
            return rm;
        }
        
        public void propertyChange(PropertyChangeEvent e) {
            CallStackNodeModel rm = getModel();
            if (rm == null) {
		return;
	    }
            rm.fireTreeChanged();
        }
    }
}
