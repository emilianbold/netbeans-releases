/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.collab.ui.beaninfo;

import org.openide.explorer.propertysheet.*;
import org.openide.nodes.*;
import org.openide.util.*;

import java.awt.*;
import java.awt.event.*;

import java.beans.*;

import java.text.*;

import java.util.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;

import org.netbeans.modules.collab.*;
import org.netbeans.modules.collab.core.Debug;


/**
 *
 * @author  Todd Fast, todd.fast@sun.com
 */
public class PasswordEditor extends PropertyEditorSupport implements ExPropertyEditor {
    ////////////////////////////////////////////////////////////////////////////
    // Class fields
    ////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////
    // Instance fields
    ////////////////////////////////////////////////////////////////////////////
    private PropertyEnv env;
    private String paintableString;

    /**
     *
     *
     */
    public PasswordEditor() {
        super();
    }

    /**
     *
     *
     */
    public void attachEnv(PropertyEnv propertyEnv) {
        env = propertyEnv;

        // Register component for in-place editing
        env.registerInplaceEditorFactory(
            new InplaceEditor.Factory() {
                public InplaceEditor getInplaceEditor() {
                    if (editor == null) {
                        editor = new PasswordInplaceEditor();
                    }

                    return editor;
                }

                private PasswordInplaceEditor editor;
            }
        );
    }

    /**
     *
     *
     */
    public String[] getTags() {
        return null;
    }

    /**
     *
     *
     */
    public void setValue(Object value) {
        paintableString = null;
        super.setValue(value);
    }

    /**
     *
     *
     */
    public String getAsText() {
        // We must do this to prevent the tooltop from showing the value
        return null;
    }

    /**
     *
     *
     */
    public void setAsText(String value) {
        setValue(value.trim());
    }

    /**
     *
     *
     */
    public boolean isPaintable() {
        return true;
    }

    /**
     *
     *
     */
    protected String getPaintableString() {
        if (getValue() == null) {
            return ""; // NOI18N
        } else {
            String password = getValue().toString();
            StringBuffer result = new StringBuffer(password.length());

            for (int i = 0; i < password.length(); i++)
                result.append("*"); // NOI18N

            return result.toString();
        }

        //		return NbBundle.getMessage(PasswordEditor.class,
        //			"PROP_PasswordEditor_ClickToEdit");
    }

    /**
     *
     *
     */
    public void paintValue(Graphics g, Rectangle rectangle) {
        // This method will get called all the time, so cache the paintable
        // string instead of recalculating over and over again
        if (paintableString == null) {
            paintableString = getPaintableString();
        }

        FontMetrics metrics = g.getFontMetrics();
        g.drawString(
            paintableString, rectangle.x,
            rectangle.y + ((rectangle.height - metrics.getHeight()) / 2) + metrics.getAscent()
        );
    }

    /**
     *
     *
     */
    public boolean supportsCustomEditor() {
        return false;
    }

    /**
     * Create new panel for this property editor.
         *
     * @return the visual component for editing the property
     */
    public java.awt.Component getCustomEditor() {
        return null;
    }

    /**
     *
     *
     */
    public String getJavaInitializationString() {
        return "\"" + getAsText() + "\"";
    }

    //	/**
    //	 *
    //	 *
    //	 */
    //	public void propertyChange(PropertyChangeEvent e)
    //	{
    //		if (ExPropertyEditor.PROP_VALUE_VALID.equals(e.getPropertyName()))
    //		{
    //			Boolean isObjValid = (Boolean)e.getNewValue();
    //			setOkButtonEnabled(isObjValid.booleanValue());
    //		}
    //	}
    //
    //
    //	/**
    //	 *
    //	 *
    //	 */
    //	public void setOkButtonEnabled(boolean state) 
    //	{
    //		if (env != null)
    //		{
    //			env.setState(state ? PropertyEnv.STATE_VALID 
    //							   : PropertyEnv.STATE_INVALID);
    //		}
    //	}
    ////////////////////////////////////////////////////////////////////////////
    // Inner class
    ////////////////////////////////////////////////////////////////////////////

    /**
     *
     *
     */
    public class PasswordInplaceEditor extends Object implements InplaceEditor {
        private PropertyEditor propertyEditor;
        private PropertyModel propertyModel;
        private PropertyEnv propertyEnv;
        private JPasswordField component;
        private java.util.List listeners = new ArrayList();
        private final KeyStroke KS_ENTER = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
        private final KeyStroke KS_ESC = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        private final KeyStroke[] KEYSTROKES = new KeyStroke[] { KS_ENTER, KS_ESC };

        /**
         *
         *
         */
        public void connect(PropertyEditor propertyEditor, PropertyEnv propertyEnv) {
            this.propertyEditor = propertyEditor;
            this.propertyEnv = propertyEnv;
        }

        /**
         *
         *
         */
        public PropertyEditor getPropertyEditor() {
            return propertyEditor;
        }

        /**
         *
         *
         */
        public PropertyModel getPropertyModel() {
            return propertyModel;
        }

        /**
         *
         *
         */
        public void setPropertyModel(PropertyModel value) {
            propertyModel = value;
        }

        /**
         *
         *
         */
        public void addActionListener(ActionListener listener) {
            listeners.add(listener);
        }

        /**
         *
         *
         */
        public void removeActionListener(ActionListener listener) {
            listeners.remove(listener);
        }

        /**
         *
         *
         */
        public JComponent getComponent() {
            if (component != null) {
                return component;
            }

            component = new JPasswordField();
            component.getKeymap().addActionForKeyStroke(
                KS_ENTER,
                new AbstractAction() {
                    public void actionPerformed(ActionEvent event) {
                        fireSuccessEvent();
                    }
                }
            );
            component.getKeymap().addActionForKeyStroke(
                KS_ESC,
                new AbstractAction() {
                    public void actionPerformed(ActionEvent event) {
                        fireFailureEvent();
                    }
                }
            );

            return component;
        }

        /**
         *
         *
         */
        public void clear() {
            component = null;
        }

        /**
         *
         *
         */
        protected void fireSuccessEvent() {
            ActionEvent event = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, InplaceEditor.COMMAND_SUCCESS);
            fireActionEvent(event);
        }

        /**
         *
         *
         */
        protected void fireFailureEvent() {
            ActionEvent event = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, InplaceEditor.COMMAND_FAILURE);
            fireActionEvent(event);
        }

        /**
         *
         *
         */
        protected void fireActionEvent(ActionEvent event) {
            for (int i = 0; i < listeners.size(); i++) {
                try {
                    ((ActionListener) listeners.get(i)).actionPerformed(event);
                } catch (Exception e) {
                    Debug.debugNotify(e);
                }
            }
        }

        /**
         *
         *
         */
        public boolean isKnownComponent(Component component) {
            return component == this.component;
        }

        /**
         *
         *
         */
        public KeyStroke[] getKeyStrokes() {
            return KEYSTROKES;
        }

        /**
         *
         *
         */
        public Object getValue() {
            if (component != null) {
                return component.getText();
            } else {
                return null;
            }
        }

        /**
         *
         *
         */
        public void setValue(Object value) {
            String stringValue = (value instanceof String) ? (String) value : value.toString();

            if (component != null) {
                component.setText(stringValue);
            }
        }

        /**
         *
         *
         */
        public void reset() {
            if (component != null) {
                component.setText(getPropertyEditor().getAsText());
            }
        }

        /**
         *
         *
         */
        public boolean supportsTextEntry() {
            return true;
        }
    }
}
