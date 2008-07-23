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

package org.netbeans.modules.web.client.javascript.debugger.models;

import static org.netbeans.spi.debugger.ui.Constants.LOCALS_TO_STRING_COLUMN_ID;
import static org.netbeans.spi.debugger.ui.Constants.LOCALS_TYPE_COLUMN_ID;
import static org.netbeans.spi.debugger.ui.Constants.LOCALS_VALUE_COLUMN_ID;

import java.awt.datatransfer.Transferable;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.netbeans.modules.web.client.javascript.debugger.api.NbJSContextProviderWrapper;
import org.netbeans.modules.web.client.javascript.debugger.api.NbJSDebugger;
import org.netbeans.modules.web.client.tools.javascript.debugger.api.JSCallStackFrame;
import org.netbeans.modules.web.client.tools.javascript.debugger.api.JSDebuggerEvent;
import org.netbeans.modules.web.client.tools.javascript.debugger.api.JSDebuggerEventListener;
import org.netbeans.modules.web.client.tools.javascript.debugger.api.JSDebuggerState;
import org.netbeans.modules.web.client.tools.javascript.debugger.api.JSObject;
import org.netbeans.modules.web.client.tools.javascript.debugger.api.JSProperty;
import org.netbeans.modules.web.client.tools.javascript.debugger.api.JSValue;
import org.netbeans.modules.web.client.tools.javascript.debugger.api.JSDebuggerState.State;
import org.netbeans.modules.web.client.tools.javascript.debugger.api.JSPrimitive;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.viewmodel.ExtendedNodeModel;
import org.netbeans.spi.viewmodel.ModelEvent;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.TableModel;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;
import org.openide.util.datatransfer.PasteType;

public class NbJSVariablesModel implements TreeModel, ExtendedNodeModel,
		TableModel, JSDebuggerEventListener {
	
	public static final String LOCAL = "org/netbeans/modules/debugger/resources/localsView/local_variable_16.png"; // NOI18N
	public static final String CLASS = "org/netbeans/modules/debugger/resources/watchesView/SuperVariable.gif"; // NOI18N

	protected final NbJSDebugger debugger;

    
    private PropertyChangeListener propertyChangeListener;
    
    protected final List<ModelListener> listeners = new CopyOnWriteArrayList<ModelListener>();
	private static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];


	public NbJSVariablesModel(ContextProvider contextProvider) {
		debugger = NbJSContextProviderWrapper.getContextProviderWrapper(contextProvider).getNbJSDebugger();
		propertyChangeListener = new PropertyChangeListenerImpl();
        debugger.addPropertyChangeListener(WeakListeners.propertyChange(propertyChangeListener, debugger));
	}

	// TreeModel implementation ................................................

	public Object getRoot() {
		return ROOT;
	}

	public Object[] getChildren(Object parent, int from, int to)
			throws UnknownTypeException {

		// TODO: why this is called when #getChildrenCount() return 0?
		if (!debugger.isSessionSuspended()) {
			return EMPTY_OBJECT_ARRAY;
		}
		
		JSCallStackFrame selectedFrame = debugger.getSelectedFrame();

		if (parent == ROOT) {
		    if (selectedFrame != null) {
		        return new Object[] {
		                selectedFrame.getScope(),
		                selectedFrame.getThis()
		                };
		    }
		    return EMPTY_OBJECT_ARRAY; 
		} else if (parent instanceof JSProperty) {
			JSProperty property = (JSProperty) parent;
			JSValue value = property.getValue();
			if (value instanceof JSObject) {
				final JSObject object = (JSObject) value;
				JSProperty[] properties  = object.getProperties();
                return properties;
			}
			return EMPTY_OBJECT_ARRAY;
		} else {
			throw new UnknownTypeException(parent);
		}
	}

	public boolean isLeaf(Object node) throws UnknownTypeException {
		if (node == ROOT) {
			return false;
		} else if (node instanceof JSProperty) {
			JSProperty property = (JSProperty) node;
			JSValue value = property.getValue();
			if (value instanceof JSObject) {
			    return false;
			} else {
				return true;
			}
		} else {
			throw new UnknownTypeException(node);
		}
	}

	private boolean isSessionSuspended() {
		if (debugger.getState().getState() == State.SUSPENDED) {
			return true;
		}
		return false;
	}

	public int getChildrenCount(Object parent) throws UnknownTypeException {
		if (isSessionSuspended()) {
			return 0;
		}
		if (parent == ROOT) {
		    JSCallStackFrame selectedFrame = debugger.getSelectedFrame();
		    if (selectedFrame != null) {
    			// Return scope and this.
    			return 2;
		    }
		    return 0;
		} else if (parent instanceof JSProperty) {
			final JSProperty property = (JSProperty) parent;
			JSValue value = property.getValue();
			if (value instanceof JSObject) {
				return ((JSObject) value).getProperties().length;
			} else {
				return 0;
			}
		} else {
			throw new UnknownTypeException(parent);
		}
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

	// NodeModel implementation ................................................

	public String getDisplayName(Object node) throws UnknownTypeException {
		String displayName;
		if (node == ROOT) {
			displayName = getMessage("CTL_VariablesModel.Column.Name.Name");
		} else if (node instanceof JSProperty) {
			displayName = ((JSProperty) node).getName();
			assert displayName != null : "null name for the JavaScript Variable: "
					+ node;
		} else {
			assert node != null : "null node passed to VariablesModel.getDisplayName()";
			throw new UnknownTypeException(node);
		}
		return displayName;
	}

	public String getIconBase(Object node) throws UnknownTypeException {
	    throw new UnsupportedOperationException();
	}

	public String getIconBaseWithExtension(Object node)
			throws UnknownTypeException {
		assert node != ROOT;
		if (node instanceof JSProperty) {
			JSProperty property = (JSProperty) node;
			if (property.getFullName().equals(".")) {
				return CLASS;
			} else {
				return LOCAL;
			}
		} else {
			throw new UnknownTypeException(node);
		}
	}

	private static String getMessage(final String key) {
		return NbBundle.getMessage(NbJSVariablesModel.class, key);
	}

	public String getShortDescription(Object node) throws UnknownTypeException {
		if (node == ROOT) {
			return getMessage("CTL_VariablesModel.Column.Name.Desc");
		} else if (node instanceof JSProperty) {
			JSProperty property = (JSProperty) node;
			JSValue value = property.getValue();
			return value.getTypeOf().getTypeDisplayName() + ":"
					+ value.getDisplayValue();
		} else {
			throw new UnknownTypeException(node);
		}
	}

	// TableModel implementation ...............................................

	public Object getValueAt(Object node, String columnID)
			throws UnknownTypeException {
		if (node == ROOT) {
			return "";
		} else if (node instanceof JSProperty) {
			JSProperty property = ((JSProperty) node);
			JSValue value = property.getValue();
			if (LOCALS_VALUE_COLUMN_ID.equals(columnID)) {
			    return value.getDisplayValue();			    
			} else if (LOCALS_TYPE_COLUMN_ID.equals(columnID)) {
			    String displayValue = value.getTypeOf().getTypeDisplayName();
			    if (value instanceof JSObject) {
                    displayValue += " " + getValueAt(node, LOCALS_TO_STRING_COLUMN_ID); // NOI18N
                }
                return displayValue;
			} else if (LOCALS_TO_STRING_COLUMN_ID.equals(columnID)) {
			    if (value instanceof JSObject) {
	                return "{" + ((JSObject)value).getClassName() + "}";
			    }
                return "";
            }
		}
		throw new UnknownTypeException(node);
	}

	public boolean isReadOnly(Object node, String columnID)
			throws UnknownTypeException {
            if (LOCALS_VALUE_COLUMN_ID.equals(columnID) && node instanceof JSProperty) {
                JSProperty property = (JSProperty) node;
                if (property.getValue() instanceof JSPrimitive ||
                    property.getValue().getTypeOf() == JSValue.TypeOf.STRING) {
                    return false;
                }
            }
            return true; 
	}

	public void setValueAt(Object node, String columnID, Object value)
			throws UnknownTypeException {
            if (LOCALS_VALUE_COLUMN_ID.equals(columnID) && node instanceof JSProperty) {
                JSProperty property = (JSProperty) node;
                if (property.getValue() instanceof JSPrimitive ||
                       property.getValue().getTypeOf() == JSValue.TypeOf.STRING) {
                    if(property.setValue(value.toString())) {
                        fireChanges();
                    }
                    return;
               }
            }             
            throw new UnknownTypeException(node);
	}

	public boolean canRename(Object node) throws UnknownTypeException {
		return false;
	}

	public boolean canCopy(Object node) throws UnknownTypeException {
		return false;
	}

	public boolean canCut(Object node) throws UnknownTypeException {
		return false;
	}

	public Transferable clipboardCopy(Object node) throws IOException,
			UnknownTypeException {
		throw new UnsupportedOperationException("Not supported yet."); // NOI18N
	}

	public Transferable clipboardCut(Object node) throws IOException,
			UnknownTypeException {
		throw new UnsupportedOperationException("Not supported yet."); // NOI18N
	}

	public PasteType[] getPasteTypes(Object node, Transferable t)
			throws UnknownTypeException {
		return null;
	}

	public void setName(Object node, String name) throws UnknownTypeException {
		throw new UnsupportedOperationException("Not supported yet."); // NOI18N
	}

	private boolean staleState = true;
	protected boolean isStaleState() {
		return staleState;
	}
	private void setStaleState(boolean staleState) {
		this.staleState = staleState;
	}

	public void onDebuggerEvent(JSDebuggerEvent debuggerEvent) {
		JSDebuggerState jsDebuggerState = debuggerEvent.getDebuggerState();
		NbJSDebugger debugger = (NbJSDebugger) debuggerEvent.getSource();

		switch (jsDebuggerState.getState()) {
		case SUSPENDED:
			setStaleState(false);
			break;
		case RUNNING:
		case STARTING:
			setStaleState(true);
			break;
		case DISCONNECTED:
			debugger.removeJSDebuggerEventListener(this);
			break;
		default:
		}

	}
	
	private class PropertyChangeListenerImpl implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getPropertyName().equals(NbJSDebugger.PROPERTY_SELECTED_FRAME)) {
                fireChanges();
            }
        }
    }
}
