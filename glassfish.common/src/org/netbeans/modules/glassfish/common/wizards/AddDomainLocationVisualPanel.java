// <editor-fold defaultstate="collapsed" desc=" License Header ">
/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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
//</editor-fold>

package org.netbeans.modules.glassfish.common.wizards;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.FileFilter;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.DefaultComboBoxModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.glassfish.common.GlassfishInstance;
import org.openide.util.NbBundle;

/**
 *
 * @author  vbk
 */
public class AddDomainLocationVisualPanel extends javax.swing.JPanel {

    private transient final List<ChangeListener> listeners; 

    /** Creates new form AddDomainLocationVisualPanel */
    public AddDomainLocationVisualPanel() {
        listeners = new CopyOnWriteArrayList<ChangeListener>();
        initComponents();
        setName(NbBundle.getMessage(AddDomainLocationVisualPanel.class, "TITLE_DomainLocation"));
        //domainField.getEditor().getEditorComponent().addKeyListener(arg0);
    }
    
    void initModels(String gfRoot) {
        // Put the choices into the combo box...
        DefaultComboBoxModel model = new DefaultComboBoxModel();
        File domainsDir = new File(gfRoot, GlassfishInstance.DEFAULT_DOMAINS_FOLDER); // NOI18N
        File candidates[] = domainsDir.listFiles(new FileFilter() {
            public boolean accept(File dir) {
                File logsDir = new File(dir, "logs");
                return logsDir.canWrite();
            }
            
        });
        if (null != candidates) {
            for (File f : candidates) {
                model.addElement(f.getName());
            }
        }
        domainField.setModel(model);
        domainField.getEditor().getEditorComponent().addKeyListener(new KeyListener() {

            public void keyTyped(KeyEvent arg0) {
                //throw new UnsupportedOperationException("Not supported yet.");
            }

            public void keyPressed(KeyEvent arg0) {
                //throw new UnsupportedOperationException("Not supported yet.");
            }

            public void keyReleased(KeyEvent arg0) {
                //throw new UnsupportedOperationException("Not supported yet.");
                fireChangeEvent();
            }
        });
        domainField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                domainField.getEditor().setItem(domainField.getSelectedItem());
                fireChangeEvent();
            }
        });
    }
    
    String getDomainField() {
        return (String) domainField.getEditor().getItem();  //getSelectedItem();
    }

    /**
     * 
     * @param l 
     */
    public void addChangeListener(ChangeListener l) {
        listeners.add(l);
    }
    
    /**
     * 
     * @param l 
     */
    public void removeChangeListener(ChangeListener l ) {
        listeners.remove(l);
    }

    private void fireChangeEvent() {
        ChangeEvent ev = new ChangeEvent(this);
        for(ChangeListener listener: listeners) {
            listener.stateChanged(ev);
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        domainFieldLabel = new javax.swing.JLabel();
        domainField = new javax.swing.JComboBox();
        explanationLabel = new javax.swing.JLabel();

        org.openide.awt.Mnemonics.setLocalizedText(domainFieldLabel, org.openide.util.NbBundle.getMessage(AddDomainLocationVisualPanel.class, "AddDomainLocationVisualPanel.domainFieldLabel.text")); // NOI18N

        domainField.setEditable(true);
        domainField.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        explanationLabel.setText(org.openide.util.NbBundle.getMessage(AddDomainLocationVisualPanel.class, "AddDomainLocationVisualPanel.explanationLabel.text")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(domainFieldLabel)
                .add(18, 18, 18)
                .add(domainField, 0, 256, Short.MAX_VALUE))
            .add(explanationLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 320, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(domainFieldLabel)
                    .add(domainField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(explanationLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 119, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox domainField;
    private javax.swing.JLabel domainFieldLabel;
    private javax.swing.JLabel explanationLabel;
    // End of variables declaration//GEN-END:variables

}
