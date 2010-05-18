/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.collab.ui.beaninfo;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.util.*;
import javax.swing.*;

import org.openide.explorer.propertysheet.*;

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
