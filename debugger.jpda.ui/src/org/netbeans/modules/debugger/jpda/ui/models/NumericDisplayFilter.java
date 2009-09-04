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

package org.netbeans.modules.debugger.jpda.ui.models;

import java.lang.reflect.InvocationTargetException;
import org.netbeans.spi.debugger.ui.Constants;
import org.netbeans.spi.viewmodel.*;
import org.netbeans.api.debugger.jpda.*;
import org.openide.util.Exceptions;
import org.openide.util.actions.Presenter;
import org.openide.util.NbBundle;

import javax.swing.*;
import java.util.*;
import java.awt.event.ActionEvent;

/**
 * Implements the "Display As Decimal/Hexadecimal/Octal/Binary/Char"
 * option for numeric variables.
 * Provides the popup action and filters displayed values.
 *
 * @author Maros Sandor, Jan Jancura
 */
public class NumericDisplayFilter implements TableModelFilter, 
NodeActionsProviderFilter, Constants {

    private final Map<Variable, NumericDisplaySettings>   variableToDisplaySettings = new HashMap<Variable, NumericDisplaySettings>();
    private HashSet     listeners;

    
    // TableModelFilter ........................................................

    public Object getValueAt (
        TableModel original, 
        Object node, 
        String columnID
    ) throws UnknownTypeException {
        if ( (columnID == Constants.WATCH_VALUE_COLUMN_ID ||
              columnID == Constants.WATCH_TO_STRING_COLUMN_ID ||
              columnID == Constants.LOCALS_VALUE_COLUMN_ID ||
              columnID == Constants.LOCALS_TO_STRING_COLUMN_ID) && 
            node instanceof Variable && 
            isIntegralType ((Variable) node)
        ) {
            Variable var = (Variable) node;
            NumericDisplaySettings nds = variableToDisplaySettings.get (var);
            if (nds == null && var instanceof Field) {
                Variable parent = null;
                try {
                    java.lang.reflect.Method pvm = var.getClass().getMethod("getParentVariable");
                    pvm.setAccessible(true);
                    parent = (Variable) pvm.invoke(var);
                } catch (IllegalAccessException ex) {
                } catch (IllegalArgumentException ex) {
                } catch (InvocationTargetException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (NoSuchMethodException ex) {
                } catch (SecurityException ex) {
                }
                nds = variableToDisplaySettings.get(parent);
            }
            return getValue(var, nds);
        }
        return original.getValueAt (node, columnID);
    }

    public boolean isReadOnly (
        TableModel original, 
        Object node, 
        String columnID
    ) throws UnknownTypeException {
        return original.isReadOnly(node, columnID);
    }

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
            node instanceof Variable && 
            isIntegralType ((Variable) node) &&
            value instanceof String
        ) {
            Variable var = (Variable) node;
            value = setValue (
                var, 
                (NumericDisplaySettings) variableToDisplaySettings.get (var),
                (String) value
            );
        }
        original.setValueAt(node, columnID, value);
    }

    public void addModelListener (ModelListener l) {
        HashSet newListeners = (listeners == null) ? 
            new HashSet () : (HashSet) listeners.clone ();
        newListeners.add (l);
        listeners = newListeners;
    }

    public void removeModelListener (ModelListener l) {
        if (listeners == null) return;
        HashSet newListeners = (HashSet) listeners.clone();
        newListeners.remove (l);
        listeners = newListeners;
    }

    
    // NodeActionsProviderFilter ...............................................

    public void performDefaultAction (
        NodeActionsProvider original, 
        Object node
    ) throws UnknownTypeException {
        original.performDefaultAction (node);
    }

    public Action[] getActions (
        NodeActionsProvider original, 
        Object node
    ) throws UnknownTypeException {
        if (!(node instanceof Variable)) return original.getActions(node);
        Action [] actions;
        try {
            actions = original.getActions(node);
        } catch (UnknownTypeException e) {
            actions = new Action[0];
        }
        List myActions = new ArrayList();
        if (node instanceof Variable) {
            Variable var = (Variable) node;
            if (isIntegralTypeOrArray(var)) {
                myActions.add(new DisplayAsAction((Variable) node));
            }
        }
        myActions.addAll(Arrays.asList(actions));
        return (Action[]) myActions.toArray(new Action[myActions.size()]);
    }

    
    // other methods ...........................................................
    
    private static int getChar(String toString) {
        // Remove the surrounding apostrophes first:
        toString = toString.substring(1, toString.length() - 1);
        char c = toString.charAt(0);
        return c & 0xFFFF;
    }
    
    private Object getValue (Variable var, NumericDisplaySettings settings) {
        if (settings == null) return var.getValue ();
        String type = var.getType ();
        try {
            switch (settings.getDisplayAs ()) {
            case NumericDisplaySettings.DECIMAL:
                if ("char".equals(type)) {
                    int c = getChar(var.getValue());
                    return Integer.toString(c);
                } else {
                    return var.getValue ();
                }
            case NumericDisplaySettings.HEXADECIMAL:
                if ("int".equals (type))
                    return "0x" + Integer.toHexString (
                        Integer.parseInt (var.getValue ())
                    );
                else
                if ("short".equals (type)) {
                    String rv = Integer.toHexString(Short.parseShort(var.getValue()));
                    if (rv.length() > 4) rv = rv.substring(rv.length() - 4, rv.length());
                    return "0x" + rv;
                } else if ("byte".equals(type)) {
                    String rv = Integer.toHexString(Byte.parseByte(var.getValue()));
                    if (rv.length() > 2) rv = rv.substring(rv.length() - 2, rv.length());
                    return "0x" + rv;
                } else if ("char".equals(type)) {
                    int c = getChar(var.getValue());
                    return "0x" + Integer.toHexString(c);
                } else {//if ("long".equals(type)) {
                    return "0x" + Long.toHexString (
                        Long.parseLong (var.getValue ())
                    );
                }
            case NumericDisplaySettings.OCTAL:
                if ("int".equals (type))
                    return "0" + Integer.toOctalString (
                        Integer.parseInt (var.getValue ())
                    );
                else
                if ("short".equals(type)) {
                    String rv = Integer.toOctalString(Short.parseShort(var.getValue()));
                    if (rv.length() > 5) rv = rv.substring(rv.length() - 5, rv.length());
                    return "0" + (rv.charAt(0) == '0' ? "1" : "") + rv;
                } else
                if ("byte".equals(type)) {
                    String rv = Integer.toOctalString(Byte.parseByte(var.getValue()));
                    if (rv.length() > 3) rv = "1" + rv.substring(rv.length() - 2, rv.length());
                    return "0" + rv;
                } else if ("char".equals(type)) {
                    int c = getChar(var.getValue());
                    return "0" + Integer.toOctalString(c);
                } else {//if ("long".equals(type)) {
                    return "0" + Long.toOctalString (
                        Long.parseLong (var.getValue ())
                    );
                }
            case NumericDisplaySettings.BINARY:
                if ("int".equals(type))
                    return Integer.toBinaryString(Integer.parseInt(var.getValue()));
                else if ("short".equals(type)) {
                    String rv = Integer.toBinaryString(Short.parseShort(var.getValue()));
                    if (rv.length() > 16) rv = rv.substring(rv.length() - 16, rv.length());
                    return rv;
                } else if ("byte".equals(type)) {
                    String rv = Integer.toBinaryString(Byte.parseByte(var.getValue()));
                    if (rv.length() > 8) rv = rv.substring(rv.length() - 8, rv.length());
                    return rv;
                } else if ("char".equals(type)) {
                    int c = getChar(var.getValue());
                    return Integer.toBinaryString(c);
                } else {//if ("long".equals(type)) {
                    return Long.toBinaryString (Long.parseLong (var.getValue ()));
                }
            case NumericDisplaySettings.CHAR:
                if ("char".equals(type)) {
                    return var.getValue ();
                }
                return "'" + new Character (
                    (char) Integer.parseInt (var.getValue ())
                ) + "'";
            default:
                return var.getValue ();
            }
        } catch (NumberFormatException nfex) {
            return nfex.getLocalizedMessage();
        }
    }

    private Object setValue (Variable var, NumericDisplaySettings settings, String origValue) {
        if (settings == null) return origValue;
        String type = var.getType ();
        try {
            switch (settings.getDisplayAs ()) {
            case NumericDisplaySettings.BINARY:
                if ("int".equals(type))
                    return Integer.toString(Integer.parseInt(origValue, 2));
                else if ("short".equals(type)) {
                    return Short.toString(Short.parseShort(origValue, 2));
                } else if ("byte".equals(type)) {
                    return Byte.toString(Byte.parseByte(origValue, 2));
                } else if ("char".equals(type)) {
                    return "'"+Character.toString((char) Integer.parseInt(origValue, 2))+"'";
                } else {//if ("long".equals(type)) {
                    return Long.toString(Long.parseLong(origValue, 2))+"l";
                }
            default:
                return origValue;
            }
        } catch (NumberFormatException nfex) {
            return nfex.getLocalizedMessage();
        }
    }
    
    private boolean isIntegralType (Variable v) {
        if (!VariablesTreeModelFilter.isEvaluated(v)) {
            return false;
        }
        
        String type = v.getType ();
        return "int".equals (type) || 
            "char".equals (type) || 
            "byte".equals (type) || 
            "long".equals (type) || 
            "short".equals (type);
    }

    private boolean isIntegralTypeOrArray(Variable v) {
        if (!VariablesTreeModelFilter.isEvaluated(v)) {
            return false;
        }

        String type = removeArray(v.getType());
        return "int".equals (type) ||
            "char".equals (type) ||
            "byte".equals (type) ||
            "long".equals (type) ||
            "short".equals (type);
    }

    private static String removeArray(String type) {
        if (type.length() > 0 && type.endsWith("[]")) { // NOI18N
            return type.substring(0, type.length() - 2);
        } else {
            return type;
        }
    }

    private String localize(String s) {
        return NbBundle.getBundle(NumericDisplayFilter.class).getString(s);
    }

    private class DisplayAsAction extends AbstractAction 
    implements Presenter.Popup {

        private Variable variable;
        private String type;

        public DisplayAsAction(Variable variable) {
            this.variable = variable;
            this.type = removeArray(variable.getType());
        }

        public void actionPerformed(ActionEvent e) {
        }

        public JMenuItem getPopupPresenter() {
            JMenu displayAsPopup = new JMenu 
                (localize ("CTL_Variable_DisplayAs_Popup"));

            JRadioButtonMenuItem decimalItem = new JRadioButtonMenuItem (
                new AbstractAction (
                    localize ("CTL_Variable_DisplayAs_Decimal")
                ) {
                    public void actionPerformed (ActionEvent e) {
                        onDisplayAs (NumericDisplaySettings.DECIMAL);
                    }
                }
            );
            JRadioButtonMenuItem hexadecimalItem = new JRadioButtonMenuItem (
                new AbstractAction (
                    localize ("CTL_Variable_DisplayAs_Hexadecimal")
                ) {
                    public void actionPerformed (ActionEvent e) {
                        onDisplayAs (NumericDisplaySettings.HEXADECIMAL);
                    }
                }
            );
            JRadioButtonMenuItem octalItem = new JRadioButtonMenuItem (
                new AbstractAction (
                    localize ("CTL_Variable_DisplayAs_Octal")
                ) {
                    public void actionPerformed (ActionEvent e) {
                        onDisplayAs (NumericDisplaySettings.OCTAL);
                    }
                }
            );
            JRadioButtonMenuItem binaryItem = new JRadioButtonMenuItem (
                new AbstractAction (
                    localize ("CTL_Variable_DisplayAs_Binary")
                ) {
                    public void actionPerformed (ActionEvent e) {
                        onDisplayAs (NumericDisplaySettings.BINARY);
                    }
                }
            );
            JRadioButtonMenuItem charItem = new JRadioButtonMenuItem (
                new AbstractAction (
                    localize ("CTL_Variable_DisplayAs_Character")
                ) {
                    public void actionPerformed (ActionEvent e) {
                        onDisplayAs (NumericDisplaySettings.CHAR);
                    }
                }
            );

            NumericDisplaySettings lds = (NumericDisplaySettings) 
                variableToDisplaySettings.get (variable);
            if (lds != null) {
                switch (lds.getDisplayAs ()) {
                case NumericDisplaySettings.DECIMAL:
                    decimalItem.setSelected (true);
                    break;
                case NumericDisplaySettings.HEXADECIMAL:
                    hexadecimalItem.setSelected (true);
                    break;
                case NumericDisplaySettings.OCTAL:
                    octalItem.setSelected (true);
                    break;
                case NumericDisplaySettings.BINARY:
                    binaryItem.setSelected (true);
                    break;
                case NumericDisplaySettings.CHAR:
                    charItem.setSelected (true);
                    break;
                }
            } else {
                if ("char".equals(type)) {
                    charItem.setSelected(true);
                } else {
                    decimalItem.setSelected (true);
                }
            }

            displayAsPopup.add (decimalItem);
            displayAsPopup.add (hexadecimalItem);
            displayAsPopup.add (octalItem);
            displayAsPopup.add (binaryItem);
            displayAsPopup.add (charItem);
            return displayAsPopup;
        }

        private void onDisplayAs (int how) {
            NumericDisplaySettings lds = (NumericDisplaySettings) 
                variableToDisplaySettings.get (variable);
            if (lds == null) {
                if ("char".equals(type)) {
                    lds = new NumericDisplaySettings 
                        (NumericDisplaySettings.CHAR);
                } else {
                    lds = new NumericDisplaySettings 
                        (NumericDisplaySettings.DECIMAL);
                }
            }
            if (lds.getDisplayAs () == how) return;
            variableToDisplaySettings.put 
                (variable, new NumericDisplaySettings (how));
            fireModelChanged ();
        }
        
        private void fireModelChanged () {
            if (listeners == null) return;
            ModelEvent evt = new ModelEvent.TableValueChanged(this, variable, null);
            for (Iterator i = listeners.iterator (); i.hasNext ();) {
                ModelListener listener = (ModelListener) i.next ();
                listener.modelChanged (evt);
            }
        }
    }

    
    private static class NumericDisplaySettings {

        public static final int DECIMAL        = 0;
        public static final int HEXADECIMAL    = 1;
        public static final int OCTAL          = 2;
        public static final int BINARY         = 3;
        public static final int CHAR           = 4;

        private int displayAs;

        public NumericDisplaySettings (int displayAs) {
            this.displayAs = displayAs;
        }

        public int getDisplayAs () {
            return displayAs;
        }
    }

}
