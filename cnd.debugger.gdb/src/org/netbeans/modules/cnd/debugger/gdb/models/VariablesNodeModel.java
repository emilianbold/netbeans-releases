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

import java.awt.datatransfer.Transferable;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import org.netbeans.api.debugger.Watch;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.viewmodel.ModelEvent;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.netbeans.modules.cnd.debugger.gdb.Field;
import org.netbeans.modules.cnd.debugger.gdb.Variable;
import org.netbeans.spi.viewmodel.ExtendedNodeModel;
import org.openide.util.datatransfer.PasteType;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/*
 * VariablesNodeModel.java
 *
 * @author Nik Molchanov (copied from Jan Jancura's JPDA implementation)
 */
public class VariablesNodeModel implements ExtendedNodeModel {
    
    public static final String FIELD =
            "org/netbeans/modules/debugger/resources/watchesView/Field.gif"; // NOI18N
    public static final String LOCAL =
            "org/netbeans/modules/debugger/resources/localsView/LocalVariable.gif"; // NOI18N
    public static final String WATCH =
            "org/netbeans/modules/debugger/resources/watchesView/watch_16.png"; // NOI18N
    public static final String ERROR =
            "org/netbeans/modules/cnd/debugger/common/resources/error_small_16.png"; // NOI18N
    
    private final RequestProcessor evaluationRP = new RequestProcessor();
    private final Collection<ModelListener> modelListeners = new HashSet<ModelListener>();
    
    // Localizable messages
    private final String LC_NoInfo = NbBundle.getMessage(VariablesNodeModel.class, "CTL_No_Info"); // NOI18N
    private final String LC_NoCurrentThreadVar = NbBundle.getMessage(VariablesNodeModel.class,
            "NoCurrentThreadVar"); // NOI18N
    private final String LC_LocalsModelColumnNameName = NbBundle.getMessage(VariablesNodeModel.class,
            "CTL_LocalsModel_Column_Name_Name"); // NOI18N
    private final String LC_LocalsModelColumnNameDesc = NbBundle.getMessage(VariablesNodeModel.class,
            "CTL_LocalsModel_Column_Name_Desc"); // NOI18N
    
    // Non-localized magic strings
    private static final String strNoInfo = "NoInfo"; // NOI18N
    private static final String strSubArray = "SubArray"; // NOI18N
    private static final String strNoCurrentThread = "No current thread"; // NOI18N
    
    public VariablesNodeModel(ContextProvider lookupProvider) {
    }
    
    public String getDisplayName(Object o) throws UnknownTypeException {
        if (o == TreeModel.ROOT) {
            return LC_LocalsModelColumnNameName;
        }
        if (o instanceof Variable) {
            return ((Variable) o).getName();
        }
        
        String str = o.toString();
        if (str.equals(strNoInfo)) {
            return LC_NoInfo;
        }
        if (str.equals(strNoCurrentThread)) {
            return LC_NoCurrentThreadVar;
        }
        if (str.startsWith(strSubArray)) {
            int index = str.indexOf('-');
            //int from = Integer.parseInt(str.substring(8, index));
            //int to = Integer.parseInt(str.substring(index + 1));
            return NbBundle.getMessage(VariablesNodeModel.class,
                    "CTL_LocalsModel_Column_Name_SubArray", // NOI18N
                    str.substring(8, index), str.substring(index + 1));
        }
        throw new UnknownTypeException(o);
    }
    
    private final Map<Object, String> shortDescriptionMap = new HashMap<Object, String>();
    
    public String getShortDescription(final Object o) throws UnknownTypeException {
        synchronized (shortDescriptionMap) {
            Object shortDescription = shortDescriptionMap.remove(o);
            if (shortDescription instanceof String) {
                return (String) shortDescription;
            } else if (shortDescription instanceof UnknownTypeException) {
                throw (UnknownTypeException) shortDescription;
            }
        }
        testKnown(o);
        // Called from AWT - we need to postpone the work...
        evaluationRP.post(new Runnable() {
            public void run() {
                String shortDescription = getShortDescriptionSync(o);
                if (shortDescription != null && shortDescription.length() > 0) {
                    synchronized (shortDescriptionMap) {
                        shortDescriptionMap.put(o, shortDescription);
                    }
                    fireModelChange(new ModelEvent.NodeChanged(VariablesNodeModel.this,
                            o, ModelEvent.NodeChanged.SHORT_DESCRIPTION_MASK));
                }
            }
        });
        return ""; // NOI18N
    }
    
    private String getShortDescriptionSync(Object o) {
        if (o == TreeModel.ROOT) {
            return LC_LocalsModelColumnNameDesc;
        } else if (o instanceof Variable) {
            Variable v = (Variable) o;
            if (o instanceof Field && v.getType().length() == 0 && v.getValue().equals("...")) { // NOI18N
                return NbBundle.getMessage(VariablesNodeModel.class, "LBL_TruncatedByGdb"); // NOI18N
            }
            return "(" + v.getType() + ") " + v.getValue(); // NOI18N
        }
        
        String str = o.toString();
        if (str.startsWith(strSubArray)) {
            int index = str.indexOf('-');
            return NbBundle.getMessage(VariablesNodeModel.class,
                    "CTL_LocalsModel_Column_Descr_SubArray", // NOI18N
                    str.substring(8, index), str.substring(index + 1));
        } else if (str.equals(strNoInfo)) {
            return LC_NoInfo;
        } else if (str.equals(strNoCurrentThread)) {
            return LC_NoCurrentThreadVar;
        } else {
            return ""; // NOI18N
        }
    }
    
    private void testKnown(Object o) throws UnknownTypeException {
        if (o == TreeModel.ROOT || o instanceof Variable) {
            return;
        }
        String str = o.toString();
        if (str.startsWith(strSubArray) || str.equals(strNoInfo) || str.equals(strNoCurrentThread)) {
            return;
        }
        throw new UnknownTypeException(o);
    }
    
    public String getIconBase(Object o) throws UnknownTypeException {
        throw new UnsupportedOperationException();
    }

    public String getIconBaseWithExtension(Object node) throws UnknownTypeException {
        if (node == TreeModel.ROOT || node instanceof GdbWatchVariable || node instanceof Watch) {
            return WATCH;
        } else if (node instanceof Field) {
            if (node instanceof AbstractVariable.ErrorField) {
                return ERROR;
            } else {
                return FIELD;
            }
        } else if (node instanceof AbstractVariable) {
            return LOCAL;
        } else {
            // FIXME - I think the following is obsolete, but FCS code freeze is in 4
            // days so I'm not willing to remove this code. Better to check in the future
            // (or just leave it).
            String str = node.toString();
            if (str.startsWith(strSubArray)) {
                return LOCAL;
            } else if (str.equals(strNoInfo) || str.equals(strNoCurrentThread)) {
                return null;
            }
        }
        throw new UnknownTypeException(node);
    }
    
    public void addModelListener(ModelListener l) {
        synchronized (modelListeners) {
            modelListeners.add(l);
        }
    }
    
    public void removeModelListener(ModelListener l) {
        synchronized (modelListeners) {
            modelListeners.remove(l);
        }
    }
    
    private void fireModelChange(ModelEvent me) {
        Object[] listeners;
        synchronized (modelListeners) {
            listeners = modelListeners.toArray();
        }
        for (int i = 0; i < listeners.length; i++) {
            ((ModelListener) listeners[i]).modelChanged(me);
        }
    } 
    
    // implement methods from ExtendedNodeModel
    
    public boolean canRename(Object node) throws UnknownTypeException {
        return false;
    }

    public boolean canCopy(Object node) throws UnknownTypeException {
        return false;
    }

    public boolean canCut(Object node) throws UnknownTypeException {
        return false;
    }

    public Transferable clipboardCopy(Object node) throws IOException, UnknownTypeException {
        throw new UnsupportedOperationException();
    }

    public Transferable clipboardCut(Object node) throws IOException,
                                                         UnknownTypeException {
        throw new UnsupportedOperationException();
    }

    public PasteType[] getPasteTypes(Object node, Transferable t) throws UnknownTypeException {
        return null;
    }

    public void setName(Object node, String name) throws UnknownTypeException {
        throw new UnsupportedOperationException();
    }
}
