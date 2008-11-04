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

import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.util.Vector;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.viewmodel.NodeModel;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.util.NbBundle;
import org.netbeans.modules.cnd.debugger.gdb.GdbDebugger;


/**
 * @author   Gordon Prieur (copied from CallStackNodeModel)
 */
public class ThreadsNodeModel implements NodeModel {

    public static final String CURRENT_THREAD =
        "org/netbeans/modules/debugger/resources/threadsView/CurrentThread"; // NOI18N
    public static final String RUNNING_THREAD =
        "org/netbeans/modules/debugger/resources/threadsView/RunningThread"; // NOI18N

    private final Vector listeners = new Vector();
    
    public ThreadsNodeModel(ContextProvider lookupProvider) {
        GdbDebugger debugger = (GdbDebugger) lookupProvider.lookupFirst(null, GdbDebugger.class);
        new Listener(this, debugger);
    }
    
    public String getDisplayName(Object o) throws UnknownTypeException {
        if (o == TreeModel.ROOT) {
            return NbBundle.getBundle(ThreadsNodeModel.class).getString("CTL_ThreadsModel_Column_Name_Name"); // NOI18N
        } else if ("No current thread" == o) { // NOI18N
            return NbBundle.getMessage(ThreadsNodeModel.class, "CTL_ThreadsModel_MSG_NoCurrentThread"); // NOI18N
        } else if ("Thread is running" == o) { // NOI18N
            return NbBundle.getMessage(ThreadsNodeModel.class, "CTL_ThreadsModel_MSG_ThreadIsRunning"); // NOI18N
        } else if (o instanceof String) {
            String line = o.toString();
            if (line.startsWith("* ")) { // NOI18N
                return bold(line.substring(2));
            } else {
                return line;
            }
        } else {
	    throw new UnknownTypeException(o);
	}
    }
    
    public String getShortDescription(Object o) throws UnknownTypeException {
        if (o == TreeModel.ROOT) {
            return NbBundle.getBundle(ThreadsNodeModel.class).getString("CTL_ThreadsModel_Column_Name_Desc"); // NOI18N
        } else if ("No current thread" == o) { // NOI18N
            return NbBundle.getMessage(ThreadsNodeModel.class, "CTL_ThreadsModel_MSG_NoCurrentThread"); // NOI18N
        } else if ("Thread is running" == o) { // NOI18N
            return NbBundle.getMessage(ThreadsNodeModel.class, "CTL_ThreadsModel_MSG_ThreadIsRunning"); // NOI18N
        } else {
	    throw new UnknownTypeException (o);
	}
    }
    
    public String getIconBase(Object node) throws UnknownTypeException {
        if (node instanceof String) {
            String row = node.toString();
            if (row.charAt(0) == '*') {
                return CURRENT_THREAD;
            } else {
                return RUNNING_THREAD;
            }
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
        Vector v = (Vector) listeners.clone();
        int i, k = v.size();
        for (i = 0; i < k; i++) {
	    ((ModelListener) v.get(i)).modelChanged(null);
	}
    }
    
    private String bold(String value) {
        return toHTML(value, true, false, null);
    }
    
    public static String toHTML(String text, boolean bold, boolean italics, Color color) {
        if (text == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("<html>"); // NOI18N
        if (bold) { 
            sb.append("<b>"); // NOI18N
        }
        if (italics) {
            sb.append("<i>"); // NOI18N
        }
        if (color != null) {
            sb.append("<font color="); // NOI18N
            sb.append(Integer.toHexString ((color.getRGB () & 0xffffff)));
            sb.append(">"); // NOI18N
        }
        text = text.replaceAll("&", "&amp;"); // NOI18N
        text = text.replaceAll("<", "&lt;"); // NOI18N
        text = text.replaceAll(">", "&gt;"); // NOI18N
        sb.append(text);
        if (color != null) {
            sb.append("</font>"); // NOI18N
        }
        if (italics) {
            sb.append("</i>"); // NOI18N
        }
        if (bold) {
            sb.append("</b>"); // NOI18N
        }
        sb.append("</html>"); // NOI18N
        return sb.toString();
    }
            
    
    // innerclasses ............................................................
    
    /**
     * Listens on DebuggerManager on PROP_CURRENT_ENGINE, and on 
     * currentTreeModel.
     */
    private static class Listener implements PropertyChangeListener {
        
        private WeakReference ref;
        private GdbDebugger debugger;
        
        private Listener(ThreadsNodeModel tnm, GdbDebugger debugger) {
            ref = new WeakReference(tnm);
            this.debugger = debugger;
            debugger.addPropertyChangeListener(GdbDebugger.PROP_CURRENT_CALL_STACK_FRAME, this);
        }
        
        private ThreadsNodeModel getModel() {
            ThreadsNodeModel tnm = (ThreadsNodeModel) ref.get();
            if (tnm == null) {
                debugger.removePropertyChangeListener(GdbDebugger.PROP_CURRENT_CALL_STACK_FRAME, this);
            }
            return tnm;
        }
        
        public void propertyChange(PropertyChangeEvent e) {
            ThreadsNodeModel tnm = getModel();
            if (tnm == null) {
		return;
	    }
            tnm.fireTreeChanged();
        }
    }
}
