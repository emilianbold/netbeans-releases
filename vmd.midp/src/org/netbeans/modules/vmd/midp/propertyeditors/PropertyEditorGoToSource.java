/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.vmd.midp.propertyeditors;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyEditor;
import java.lang.ref.WeakReference;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.properties.DesignPropertyEditor;
import org.netbeans.modules.vmd.midp.actions.GoToSourceSupport;
import org.openide.explorer.propertysheet.InplaceEditor;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.explorer.propertysheet.PropertyModel;
import org.openide.util.NbBundle;

/**
 *
 * @author Anton Chechel
 */
public class PropertyEditorGoToSource extends DesignPropertyEditor implements ActionListener {

    private WeakReference<DesignComponent> component;
    private ButtonInplaceEditor inplaceEditor;

    private PropertyEditorGoToSource() {
    }

    public static PropertyEditorGoToSource createInstance() {
        return new PropertyEditorGoToSource();
    }

    @Override
    public Boolean canEditAsText() {
        return true;
    }

    @Override
    public boolean canWrite() {
        return true;
    }

    @Override
    public void init(DesignComponent component) {
        this.component = new WeakReference<DesignComponent>(component);
    }

    @Override
    public boolean supportsCustomEditor() {
        return false;
    }

    @Override
    public boolean supportsDefaultValue() {
        return false;
    }

    @Override
    public String getAsText() {
        return ""; // NOI18N
    }

    @Override
    public InplaceEditor getInplaceEditor() {
        if (inplaceEditor == null) {
            inplaceEditor = new ButtonInplaceEditor(this);
            JButton button = (JButton) inplaceEditor.getComponent();
            button.addActionListener(this);
        }
        return inplaceEditor;
    }

    @Override
    public void paintValue(Graphics gfx, Rectangle box) {
        JComponent _component = inplaceEditor.getComponent();
        _component.setSize(box.width, box.height);
        _component.doLayout();
        _component.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        Graphics g = gfx.create(box.x, box.y, box.width, box.height);
        _component.setOpaque(false);
        _component.paint(g);
        g.dispose();
    }

    public void actionPerformed(ActionEvent e) {
        if (component == null || component.get() == null) {
            return;
        }

        GoToSourceSupport.goToSourceOfComponent(component.get());
    }

    @Override
    public boolean isPaintable() {
        return true;
    }

    private static class ButtonInplaceEditor implements InplaceEditor {

        private JButton button;
        private DesignPropertyEditor propertyEditor;
        private PropertyModel model;

        public ButtonInplaceEditor(DesignPropertyEditor propertyEditor) {
            this.propertyEditor = propertyEditor;
            button = new JButton(NbBundle.getMessage(PropertyEditorGoToSource.class, "LBL_GOTO_STR")); // NOI18N
        }

        public void connect(PropertyEditor pe, PropertyEnv env) {
        }

        public JComponent getComponent() {
            //button.setBorder(BorderFactory.createEmptyBorder(0, 3, 0, 0));
            return button;
        }

        public void clear() {
        }

        public Object getValue() {
            return propertyEditor.getValue();
        }

        public void setValue(Object o) {
        }

        public boolean supportsTextEntry() {
            return true;
        }

        public void reset() {
        }

        public void addActionListener(ActionListener al) {
        }

        public void removeActionListener(ActionListener al) {
        }

        public KeyStroke[] getKeyStrokes() {
            return new KeyStroke[0];
        }

        public PropertyEditor getPropertyEditor() {
            return propertyEditor;
        }

        public PropertyModel getPropertyModel() {
            return model;
        }

        public void setPropertyModel(PropertyModel pm) {
            this.model = model;
        }

        public boolean isKnownComponent(Component c) {
            return true;
        }
    }
}
