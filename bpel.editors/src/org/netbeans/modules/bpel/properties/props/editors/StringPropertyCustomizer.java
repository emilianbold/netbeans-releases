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
package org.netbeans.modules.bpel.properties.props.editors;

import java.beans.FeatureDescriptor;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyEditor;
import javax.swing.JPanel;
import org.netbeans.modules.soa.ui.form.ReusablePropertyCustomizer;
import org.netbeans.modules.bpel.properties.props.PropertyVetoError;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.nodes.Node;

/**
 *
 * @author nk160297
 */
public class StringPropertyCustomizer extends JPanel
        implements PropertyChangeListener, ReusablePropertyCustomizer {
    
    private PropertyEditor myPropertyEditor;
    private PropertyEnv myPropertyEnv;
    
    /** Creates new form StringPropertyCustomizer */
    public StringPropertyCustomizer() {
        super();
        //
        initComponents();
    }
    
    public synchronized void init(
            PropertyEnv propertyEnv, PropertyEditor propertyEditor) {
        assert propertyEnv != null && propertyEditor != null : "Wrong params"; // NOI18N
        //
        if (myPropertyEnv == propertyEnv) {
            return; // Prevent repeated initialization
        }
        //
        if (myPropertyEnv != null) {
            myPropertyEnv.removePropertyChangeListener(this);
        }
        //
        myPropertyEnv = propertyEnv;
        myPropertyEditor = propertyEditor;
        //
        myPropertyEnv.addPropertyChangeListener(this);
        //
        // The Ok button will not work without the following line!!!
        myPropertyEnv.setState(PropertyEnv.STATE_NEEDS_VALIDATION);
        //
        String value = propertyEditor.getAsText();
        if (value == null) {
            fldText.setText("");
        } else {
            fldText.setText(value);
        }
        //
        FeatureDescriptor desc = myPropertyEnv.getFeatureDescriptor();
        if (desc instanceof Node.Property){
            Node.Property prop = (Node.Property)desc;
            boolean editable = prop.canWrite();
            //
            if (editable) {
                Boolean canEditAsText = (Boolean)prop.getValue("canEditAsText");
                if (canEditAsText != null) {
                    editable = canEditAsText.booleanValue();
                }
            }
            //
            fldText.setEditable(editable);
        }
    }
    
    public void propertyChange(PropertyChangeEvent event) {
        if (PropertyEnv.PROP_STATE.equals(event.getPropertyName()) &&
                event.getNewValue() == PropertyEnv.STATE_VALID) {
            try {
                String currText = fldText.getText();
                myPropertyEditor.setAsText(currText);
            } catch (PropertyVetoError ex) {
                myPropertyEnv.setState(PropertyEnv.STATE_NEEDS_VALIDATION);
                PropertyVetoError.defaultProcessing(ex);
            }
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        jScrollPane1 = new javax.swing.JScrollPane();
        fldText = new javax.swing.JTextArea();

        fldText.setColumns(20);
        fldText.setRows(5);
        jScrollPane1.setViewportView(fldText);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 484, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 160, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextArea fldText;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables
    
}
