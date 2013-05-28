/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.css.prep.ui.customizer;

import java.awt.EventQueue;
import java.util.List;
import javax.swing.GroupLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.LayoutStyle;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.css.prep.CssPreprocessorType;
import org.netbeans.modules.css.prep.util.CssPreprocessorUtils;
import org.openide.awt.Mnemonics;
import org.openide.util.ChangeSupport;
import org.openide.util.NbBundle;

public class MappingPanel extends JPanel {

    private static final long serialVersionUID = -46413324L;

    private final ChangeSupport changeSupport = new ChangeSupport(this);

    // we must be thread safe
    private volatile String mapping;


    public MappingPanel(CssPreprocessorType type) {
        assert EventQueue.isDispatchThread();
        assert type != null;

        initComponents();
        init(type);
    }

    @NbBundle.Messages({
        "# {0} - preprocessor name",
        "# {1} - file extension",
        "MappingPanel.info=<html>One or more {0} folder to css folder mappings for compiler output, eg. <i>/{1}:/css, /other/{1}:/css/smth<i>",
    })
    private void init(CssPreprocessorType type) {
        // info
        mappingInfoLabel.setText(Bundle.MappingPanel_info(type.getDisplayName(), type.getFileExtension()));
        // listeners
        mappingTextField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                processChange();
            }
            @Override
            public void removeUpdate(DocumentEvent e) {
                processChange();
            }
            @Override
            public void changedUpdate(DocumentEvent e) {
                processChange();
            }
            private void processChange() {
                mapping = mappingTextField.getText();
                fireChange();
            }
        });
    }

    public List<String> getMappings() {
        return CssPreprocessorUtils.decodeMappings(mapping);
    }

    public void setMappings(List<String> mappings) {
        assert EventQueue.isDispatchThread();
        mappingTextField.setText(CssPreprocessorUtils.encodeMappings(mappings));
    }

    public void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }

    void fireChange() {
        changeSupport.fireChange();
    }

    void enablePanel(boolean enabled) {
        mappingTextField.setEnabled(enabled);
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form
     * Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mappingLabel = new JLabel();
        mappingTextField = new JTextField();
        mappingInfoLabel = new JLabel();

        Mnemonics.setLocalizedText(mappingLabel, NbBundle.getMessage(MappingPanel.class, "MappingPanel.mappingLabel.text")); // NOI18N

        Mnemonics.setLocalizedText(mappingInfoLabel, "INFO"); // NOI18N

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(mappingLabel)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(mappingInfoLabel)
                        .addContainerGap())
                    .addComponent(mappingTextField)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(mappingLabel)
                    .addComponent(mappingTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(mappingInfoLabel))
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JLabel mappingInfoLabel;
    private JLabel mappingLabel;
    private JTextField mappingTextField;
    // End of variables declaration//GEN-END:variables

}
