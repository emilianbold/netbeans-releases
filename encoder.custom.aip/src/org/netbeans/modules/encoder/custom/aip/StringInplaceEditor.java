/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.encoder.custom.aip;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.beans.PropertyEditor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import org.openide.explorer.propertysheet.InplaceEditor;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.explorer.propertysheet.PropertyModel;

/**
 * An InplaceEditor implementation for editing string values.  The reason for
 * creating this implementation instead of using the default is that the
 * default one causes the container window to be closed everytime the value
 * is accepted.
 *
 * @author Jun Xu
 */
public class StringInplaceEditor implements InplaceEditor {
    
    protected final JTextField mJTextField;
    protected PropertyEditor mPropertyEditor;
    
    private final List<ActionListener> mActionListeners =
            Collections.synchronizedList(new LinkedList<ActionListener>());
    private final ActionListener mJTextFieldActionListener =
            new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            mJTextField.selectAll();
            if (mPropertyModel != null) {
                try {
                    mPropertyModel.setValue(getValue());
                } catch (InvocationTargetException ex) {
                }
            }

            // InplaceEditor is supposed to fire the COMMAND_SUCCESS.
            // But making following notification causes the default button
            // being "clicked", which causes the container window being closed.
            //ActionListener[] listeners = mActionListeners.toArray(new ActionListener[0]);
            //ActionEvent evt = new ActionEvent(StringInplaceEditor.this, 0, COMMAND_SUCCESS);
            //for (int i = 0; i < listeners.length; i++) {
            //    listeners[i].actionPerformed(evt);
            //}
        }
    };
    
    private PropertyModel mPropertyModel;
            
    /** Creates a new instance of StringInplaceEditor */
    public StringInplaceEditor() {
        mJTextField = new JTextField();
    }

    public void connect(PropertyEditor propertyEditor, PropertyEnv propertyEnv) {
        mPropertyEditor = propertyEditor;
        reset();
        mJTextField.addActionListener(mJTextFieldActionListener);
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                mJTextField.selectAll();
            }
        });
    }

    public JComponent getComponent() {
        return mJTextField;
    }

    public void clear() {
        mPropertyModel = null;
        mPropertyEditor = null;
        mJTextField.removeActionListener(mJTextFieldActionListener);
    }

    public Object getValue() {
        return mJTextField.getText();
    }

    public void setValue(Object object) {
        if (object == null) {
            mJTextField.setText(""); //NOI18N
            return;
        }
        mJTextField.setText(object.toString());
    }

    public boolean supportsTextEntry() {
        return true;
    }

    public void reset() {
        if (mPropertyEditor != null) {
            setValue(mPropertyEditor.getValue());
        }
    }

    public void addActionListener(ActionListener actionListener) {
        mActionListeners.add(actionListener);
    }

    public void removeActionListener(ActionListener actionListener) {
        mActionListeners.remove(actionListener);
    }

    public KeyStroke[] getKeyStrokes() {
        return new KeyStroke[]{KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0)};
    }

    public PropertyEditor getPropertyEditor() {
        return mPropertyEditor;
    }

    public PropertyModel getPropertyModel() {
        return mPropertyModel;
    }

    public void setPropertyModel(PropertyModel propertyModel) {
        mPropertyModel = propertyModel;
    }

    public boolean isKnownComponent(Component component) {
        return mJTextField == component;
    }
}
