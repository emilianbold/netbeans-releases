/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

import org.netbeans.spi.debugger.ui.Constants;
import org.openide.util.actions.Presenter;
import org.openide.util.NbBundle;

import javax.swing.*;
import java.util.*;
import java.awt.event.ActionEvent;
import org.netbeans.spi.viewmodel.ModelEvent;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.NodeActionsProvider;
import org.netbeans.spi.viewmodel.NodeActionsProviderFilter;
import org.netbeans.spi.viewmodel.TableModel;
import org.netbeans.spi.viewmodel.TableModelFilter;
import org.netbeans.spi.viewmodel.UnknownTypeException;

/**
 * Implements the "Display As Decimal/Hexadecimal/Octal/Binary/Char"
 * option for numeric variables.
 * Provides the popup action and filters displayed values.
 * (Copied from JPDA)
 *
 * @author Maros Sandor, Jan Jancura, Martin Entlicher
 */
public class NumericDisplayFilter implements TableModelFilter, 
NodeActionsProviderFilter, Constants {

    enum NumericDisplaySettings { AUTO, DECIMAL, HEXADECIMAL, OCTAL, BINARY, CHAR, TIME }

    // we need separate collections for locals and watches
    private final Map<String, NumericDisplaySettings> variableToDisplaySettings
            = new HashMap<String, NumericDisplaySettings>();
    private final Map<String, NumericDisplaySettings> watchToDisplaySettings
            = new HashMap<String, NumericDisplaySettings>();
    private HashSet<ModelListener> listeners;

    private Map<String, NumericDisplaySettings> getNodeCollection(AbstractVariable var) {
        if (var instanceof GdbWatchVariable) {
            return watchToDisplaySettings;
        }
        if (var instanceof AbstractVariable.AbstractField) {
            return getNodeCollection(((AbstractVariable.AbstractField)var).getAncestor());
        }
        return variableToDisplaySettings;
    }

    private NumericDisplaySettings getDisplaySettings(AbstractVariable var) {
        return getNodeCollection(var).get(var.getFullName(true));
    }

    private void setDisplaySettings(AbstractVariable var, NumericDisplaySettings nds) {
        getNodeCollection(var).put(var.getFullName(true), nds);
    }
    
    // TableModelFilter ........................................................

    @Override
    public Object getValueAt (
        TableModel original, 
        Object node, 
        String columnID
    ) throws UnknownTypeException {
        if ( (columnID == Constants.WATCH_VALUE_COLUMN_ID ||
              columnID == Constants.WATCH_TO_STRING_COLUMN_ID ||
              columnID == Constants.LOCALS_VALUE_COLUMN_ID ||
              columnID == Constants.LOCALS_TO_STRING_COLUMN_ID) && 
            node instanceof AbstractVariable
        ) {
            AbstractVariable var = (AbstractVariable) node;
            NumericDisplaySettings nds = getDisplaySettings(var);
            if (nds != null && nds != NumericDisplaySettings.AUTO) {
                return getValue(var, nds);
            }
//            if (nds == null && var instanceof AbstractVariable.AbstractField) {
//                AbstractVariable.AbstractField field = (AbstractVariable.AbstractField) var;
//                nds = variableToDisplaySettings.get(field.getAncestor().getFullName(true));
//            }
        }
        return original.getValueAt(node, columnID);
    }

    @Override
    public boolean isReadOnly (
        TableModel original, 
        Object node, 
        String columnID
    ) throws UnknownTypeException {
        return original.isReadOnly(node, columnID);
    }

    @Override
    public void setValueAt (
        TableModel original, 
        Object node, 
        String columnID, 
        Object value
    ) throws UnknownTypeException {
        if ( (columnID == Constants.WATCH_VALUE_COLUMN_ID ||
              columnID == Constants.WATCH_TO_STRING_COLUMN_ID ||
              columnID == Constants.LOCALS_VALUE_COLUMN_ID ||
              columnID == Constants.LOCALS_TO_STRING_COLUMN_ID) && 
            node instanceof AbstractVariable &&
            value instanceof String
        ) {
            AbstractVariable var = (AbstractVariable) node;
            NumericDisplaySettings nds = getDisplaySettings(var);
            if (nds != null && nds != NumericDisplaySettings.AUTO) {
                value = formatValue((String) value, nds);
            }
        }
        original.setValueAt(node, columnID, value);
    }

    @Override
    public void addModelListener (ModelListener l) {
        @SuppressWarnings("unchecked")
        HashSet<ModelListener> newListeners = (listeners == null) ?
            new HashSet<ModelListener>() : (HashSet<ModelListener>)listeners.clone ();
        newListeners.add (l);
        listeners = newListeners;
    }

    @Override
    public void removeModelListener (ModelListener l) {
        if (listeners == null) return;
        @SuppressWarnings("unchecked")
        HashSet<ModelListener> newListeners = (HashSet<ModelListener>) listeners.clone();
        newListeners.remove (l);
        listeners = newListeners;
    }

    
    // NodeActionsProviderFilter ...............................................

    @Override
    public void performDefaultAction (
        NodeActionsProvider original, 
        Object node
    ) throws UnknownTypeException {
        original.performDefaultAction (node);
    }

    @Override
    public Action[] getActions (
        NodeActionsProvider original, 
        Object node
    ) throws UnknownTypeException {
        if (!(node instanceof AbstractVariable)) {
            return original.getActions(node);
        }
        AbstractVariable var = (AbstractVariable) node;
        if (getDisplaySettings(var) != null || isIntegralType(var)) {
            Action [] actions;
            try {
                actions = original.getActions(node);
            } catch (UnknownTypeException e) {
                actions = new Action[0];
            }
            List<Action> myActions = new ArrayList<Action>();
            myActions.add(new DisplayAsAction(var));
            myActions.addAll(Arrays.asList(actions));
            return myActions.toArray(new Action[myActions.size()]);
        } else {
            return original.getActions(node);
        }
    }

    
    // other methods ...........................................................
    
    private static int getChar(String val) {
        // Remove the surrounding apostrophes first:
        if (val.length() >= 3 && val.charAt(0) == '\'' && val.charAt(2) == '\'') {
            return val.charAt(1);
        } else if (!val.isEmpty()) {
            return val.charAt(0);
        }
        return 0;
    }
    
    private Object getValue(AbstractVariable var, NumericDisplaySettings settings) {
        try {
            switch (settings) {
                case DECIMAL:
                    return Long.toString(getValue(var));
                case HEXADECIMAL:
                    return "0x" + Long.toHexString(getValue(var)); //NOI18N
                case OCTAL:
                    return "0" + Long.toOctalString(getValue(var)); //NOI18N
                case BINARY:
                    return Long.toBinaryString(getValue(var));
                case CHAR:
                    return "'" + (char) getValue(var) + "'"; //NOI18N
    //            case TIME:
    //                if ("long".equals(type)) {
    //                    return new Date(Long.parseLong(var.getValue ())).toString();
    //                }
                default:
                    return var.getValue ();
            }
        } catch (NumberFormatException nfex) {
            return nfex.getLocalizedMessage();
        }
    }

    private String formatValue(String origValue, NumericDisplaySettings settings) {
        if (origValue.isEmpty()) return origValue;
        try {
            switch (settings) {
            case BINARY:
                return Long.toString(Long.parseLong(origValue, 2));
            case CHAR:
                return String.valueOf(getChar(origValue));
            default:
                return origValue;
            }
        } catch (NumberFormatException nfex) {
            return nfex.getLocalizedMessage();
        }
    }

    private static long getValue(AbstractVariable v) throws NumberFormatException {
        return Long.decode(v.getValue()).longValue();
    }
    
    private static boolean isIntegralType(AbstractVariable v) {
        try {
            getValue(v);
            return true;
        } catch (Exception e) {
            return false;
        }
//        String type = v.getType();
//        return isIntegralType(type);
    }

//    private boolean isIntegralTypeOrArray(AbstractVariable v) {
//        String type = removeArray(v.getType());
//        return isIntegralType(type);
//    }

//    private static boolean isIntegralType(String type) {
//        if (type == null) {
//            return false;
//        }
//        return type.endsWith(" int") ||
//                type.endsWith(" char") ||
//                type.endsWith(" byte") ||
//                type.endsWith(" long") ||
//                type.endsWith(" short");
//    }

//    private static String removeArray(String type) {
//        if (type == null) {
//            return type;
//        }
//        if (type.length() > 0 && type.endsWith("[]")) { // NOI18N
//            return type.substring(0, type.length() - 2);
//        } else {
//            return type;
//        }
//    }

    private class DisplayAsAction extends AbstractAction 
    implements Presenter.Popup {

        private AbstractVariable variable;
        //private String type;

        public DisplayAsAction(AbstractVariable variable) {
            this.variable = variable;
            //this.type = variable.getType();
        }

        @Override
        public void actionPerformed(ActionEvent e) {
        }

        @Override
        public JMenuItem getPopupPresenter() {
            JMenu displayAsPopup = new JMenu 
                (NbBundle.getMessage(NumericDisplayFilter.class, "CTL_Variable_DisplayAs_Popup"));

            JRadioButtonMenuItem autoItem = new DisplayAsMenuItem (
                    "CTL_Variable_DisplayAs_Auto",       // NOI18N
                    NumericDisplaySettings.AUTO
            );
            JRadioButtonMenuItem decimalItem = new DisplayAsMenuItem (
                    "CTL_Variable_DisplayAs_Decimal",       // NOI18N
                    NumericDisplaySettings.DECIMAL
            );
            JRadioButtonMenuItem hexadecimalItem = new DisplayAsMenuItem (
                    "CTL_Variable_DisplayAs_Hexadecimal",   // NOI18N
                    NumericDisplaySettings.HEXADECIMAL
            );
            JRadioButtonMenuItem octalItem = new DisplayAsMenuItem (
                    "CTL_Variable_DisplayAs_Octal",         // NOI18N
                    NumericDisplaySettings.OCTAL
            );
            JRadioButtonMenuItem binaryItem = new DisplayAsMenuItem (
                    "CTL_Variable_DisplayAs_Binary",        // NOI18N
                    NumericDisplaySettings.BINARY
            );
            JRadioButtonMenuItem charItem = new DisplayAsMenuItem (
                    "CTL_Variable_DisplayAs_Character",     // NOI18N
                    NumericDisplaySettings.CHAR
            );
            JRadioButtonMenuItem timeItem = new DisplayAsMenuItem (
                    "CTL_Variable_DisplayAs_Time",          // NOI18N
                    NumericDisplaySettings.TIME
            );

            NumericDisplaySettings lds = getDisplaySettings(variable);
            if (lds != null) {
                switch (lds) {
                case AUTO:
                    autoItem.setSelected (true);
                    break;
                case DECIMAL:
                    decimalItem.setSelected (true);
                    break;
                case HEXADECIMAL:
                    hexadecimalItem.setSelected (true);
                    break;
                case OCTAL:
                    octalItem.setSelected (true);
                    break;
                case BINARY:
                    binaryItem.setSelected (true);
                    break;
                case CHAR:
                    charItem.setSelected (true);
                    break;
                case TIME:
                    timeItem.setSelected (true);
                    break;
                }
            } else {
                autoItem.setSelected (true);
            }

            displayAsPopup.add (autoItem);
            displayAsPopup.add (decimalItem);
            displayAsPopup.add (hexadecimalItem);
            displayAsPopup.add (octalItem);
            displayAsPopup.add (binaryItem);
            displayAsPopup.add (charItem);
//            if ("long".equals(type)) {
//                displayAsPopup.add (timeItem);
//            }
            return displayAsPopup;
        }

        private void onDisplayAs(NumericDisplaySettings how) {
            NumericDisplaySettings lds = getDisplaySettings(variable);
            if (lds == null) {
                lds = NumericDisplaySettings.AUTO;
            }
            if (lds == how) return;
            setDisplaySettings(variable, how);
            fireModelChanged();
        }
        
        private void fireModelChanged() {
            if (listeners == null) return;
            ModelEvent evt = new ModelEvent.TableValueChanged(this, variable, null);
            for (ModelListener listener : listeners) {
                listener.modelChanged (evt);
            }
        }

        private class DisplayAsMenuItem extends JRadioButtonMenuItem {

            public DisplayAsMenuItem(final String message, final NumericDisplaySettings as) {
                super(new AbstractAction(NbBundle.getMessage(NumericDisplayFilter.class, message)) {
                    @Override
                        public void actionPerformed (ActionEvent e) {
                            onDisplayAs (as);
                        }
                    });
            }

        }

    }

    
}
