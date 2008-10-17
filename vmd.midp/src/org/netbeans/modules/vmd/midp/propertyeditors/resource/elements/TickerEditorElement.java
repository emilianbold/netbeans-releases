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
package org.netbeans.modules.vmd.midp.propertyeditors.resource.elements;

import org.netbeans.modules.vmd.midp.propertyeditors.api.resource.element.PropertyEditorResourceElement;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.swing.JComponent;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.api.model.TypeID;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.components.resources.TickerCD;
import org.netbeans.modules.vmd.midp.propertyeditors.CleanUp;

/**
 *
 * @author Anton Chechel
 */
public class TickerEditorElement extends PropertyEditorResourceElement implements DocumentListener, CleanUp {

    private long componentID;
    private boolean doNotFireEvent;

    public TickerEditorElement() {
        initComponents();
        tickerTextField.getDocument().addDocumentListener(this);
    }

    public void clean(DesignComponent component) {
        tickerLabel = null;
        tickerTextField = null;
        this.removeAll();
    }

    public JComponent getJComponent() {
        return this;
    }

    public TypeID getTypeID() {
        return TickerCD.TYPEID;
    }

    public List<String> getPropertyValueNames() {
        return Arrays.asList(TickerCD.PROP_STRING);
    }

    public void setDesignComponentWrapper(final DesignComponentWrapper wrapper) {
        if (wrapper == null) {
            // UI stuff
            setText(null);
            setAllEnabled(false);
            return;

        }



        this.componentID = wrapper.getComponentID();
        final String[] _tickerText = new String[1];

        final DesignComponent component = wrapper.getComponent();
        if (component != null) {
            // existing component
            if (!component.getType().equals(getTypeID())) {
                throw new IllegalArgumentException("Passed component must have typeID " + getTypeID() + " instead passed " + component.getType()); // NOI18N
            }

            this.componentID = component.getComponentID();
            component.getDocument().getTransactionManager().readAccess(new Runnable() {

                public void run() {
                    PropertyValue propertyValue = component.readProperty(TickerCD.PROP_STRING);
                    if (!isPropertyValueAUserCodeType(propertyValue)) {
                        _tickerText[0] = MidpTypes.getString(propertyValue);
                    }

                }
            });
        }

        if (wrapper.hasChanges()) {
            Map<String, PropertyValue> changes = wrapper.getChanges();
            for (String propertyName : changes.keySet()) {
                final PropertyValue propertyValue = changes.get(propertyName);
                if (TickerCD.PROP_STRING.equals(propertyName)) {
                    _tickerText[0] = MidpTypes.getString(propertyValue);
                }

            }
        }

        // UI stuff
        setAllEnabled(true);
        setText(_tickerText[0]);
    }

    private synchronized void setText(String text) {
        doNotFireEvent = true;
        tickerTextField.setText(text);
        doNotFireEvent =
                false;
    }

    private void setAllEnabled(boolean isEnabled) {
        tickerLabel.setEnabled(isEnabled);
        tickerTextField.setEnabled(isEnabled);
    }

    public void insertUpdate(DocumentEvent e) {
        if (tickerTextField.hasFocus()) {
            textChanged();
        }

    }

    public void removeUpdate(DocumentEvent e) {
        if (tickerTextField.hasFocus()) {
            textChanged();
        }

    }

    public void changedUpdate(DocumentEvent e) {
    }

    private synchronized void textChanged() {
//        if (isShowing() && !doNotFireEvent) {
        if (!doNotFireEvent) {
            fireElementChanged(componentID, TickerCD.PROP_STRING, MidpTypes.createStringValue(tickerTextField.getText()));
        }

    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        tickerLabel = new javax.swing.JLabel();
        tickerTextField = new javax.swing.JTextField();

        tickerLabel.setLabelFor(tickerTextField);
        org.openide.awt.Mnemonics.setLocalizedText(tickerLabel, org.openide.util.NbBundle.getMessage(TickerEditorElement.class, "TickerEditorElement.tickerLabel.text")); // NOI18N
        tickerLabel.setEnabled(false);

        tickerTextField.setEnabled(false);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(2, 2, 2)
                .add(tickerLabel)
                .addContainerGap())
            .add(tickerTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 320, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(tickerLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(tickerTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(110, Short.MAX_VALUE))
        );

        tickerTextField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(TickerEditorElement.class, "ACSN_Ticker")); // NOI18N
        tickerTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TickerEditorElement.class, "ACSD_Ticker")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel tickerLabel;
    private javax.swing.JTextField tickerTextField;
    // End of variables declaration//GEN-END:variables
}