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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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
package org.netbeans.modules.javacard.project.customizer;

import com.sun.javacard.filemodels.ParseErrorHandler;
import com.sun.javacard.filemodels.WebXmlModel;
import org.netbeans.modules.javacard.common.JCConstants;
import org.netbeans.modules.javacard.project.JCProject;
import org.netbeans.modules.javacard.project.ui.CheckboxListView;
import org.netbeans.modules.javacard.project.ui.FileModelFactory;
import org.netbeans.modules.javacard.project.ui.NodeCheckObserver;
import org.netbeans.spi.project.ui.support.ProjectCustomizer.Category;
import org.openide.awt.Mnemonics;
import org.openide.explorer.ExplorerManager;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.HelpCtx;

/**
 * Customizer for servlets
 *
 * @author Tim Boudreau
 */
public class WebCustomizer extends AllClassesOfTypeExplorerPanel implements DocumentListener, PropertyChangeListener, NodeCheckObserver, FocusListener, ActionListener {

    private WebProjectProperties props;
    private final Category category;

    /** Creates new form WebCustomizer */
    public WebCustomizer(WebProjectProperties props, Category category) {
        super("javax.servlet.http.HttpServlet", NbBundle.getMessage(WebCustomizer.class, //NOI18N
                "WAIT_MSG")); //NOI18N
        this.category = category;
        initComponents();
        if (props != null) {
            setProperties(props);
        }
        ((CheckboxListView) servletList).setNodeCheckObserver(this);
        ((CheckboxListView) servletList).setCheckboxesVisible(false);
        for (Component c : getComponents()) {
            if (c instanceof AbstractButton) {
                AbstractButton b = (AbstractButton) c;
                Mnemonics.setLocalizedText(b, b.getText());
            } else if (c instanceof JLabel) {
                JLabel l = (JLabel) c;
                Mnemonics.setLocalizedText(l, l.getText());
            } else if (c instanceof JTextComponent) {
                c.addFocusListener(this);
            }
        }
        mgr.addPropertyChangeListener(this);
        mappingField.getDocument().addDocumentListener(this);
        nameField.getDocument().addDocumentListener(this);
        displayNameField.getDocument().addDocumentListener(this);
        unlockPanel1.setVisible(false);
        unlockPanel1.addActionListener(l);
        HelpCtx.setHelpIDString(this, "org.netbeans.modules.javacard.WebPanel"); //NOI18N
    }

    private void change(Document document) {
        if (inUpdateFields) {
            return;
        }
        Node[] nodes = mgr.getSelectedNodes();
        if (nodes.length == 1) {
            Node n = nodes[0];
            if (document == nameField.getDocument()) {
                n.setValue(FileModelFactory.SERVLET_NAME, nameField.getText());
            } else if (document == mappingField.getDocument()) {
                n.setValue(FileModelFactory.SERVLET_MAPPING, mappingField.getText());
            }
        }
        updatePropsUiModel();
    }

    private void updatePropsUiModel() {
        if (locked) {
            return;
        }
        WebXmlModel m = getModelFromUI();
        String problem = m.getProblem();
        setProblem(problem);
        if (problem == null) {
            props.setWebXmlUiModel(m);
            props.setWebContextPathAndMainUrl (m.defaultServlet(), m.defaultMapping());
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        servletList = new CheckboxListView();
        listLabel = new javax.swing.JLabel();
        nameLabel = new javax.swing.JLabel();
        nameField = new javax.swing.JTextField();
        mappingLabel = new javax.swing.JLabel();
        mappingField = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        displayNameField = new javax.swing.JTextField();
        useAsDefault = new javax.swing.JCheckBox();
        unlockPanel1 = new org.netbeans.modules.javacard.project.customizer.WebEditorUnlockPanel();

        servletList.setBorder(javax.swing.BorderFactory.createLineBorder(javax.swing.UIManager.getDefaults().getColor("controlShadow")));

        listLabel.setLabelFor(servletList);
        listLabel.setText(NbBundle.getMessage(WebCustomizer.class, "WebCustomizer.listLabel.text")); // NOI18N

        nameLabel.setLabelFor(nameField);
        nameLabel.setText(NbBundle.getMessage(WebCustomizer.class, "WebCustomizer.nameLabel.text")); // NOI18N
        nameLabel.setEnabled(false);

        nameField.setText(NbBundle.getMessage(WebCustomizer.class, "WebCustomizer.nameField.text")); // NOI18N
        nameField.setEnabled(false);

        mappingLabel.setLabelFor(mappingField);
        mappingLabel.setText(NbBundle.getMessage(WebCustomizer.class, "WebCustomizer.mappingLabel.text")); // NOI18N
        mappingLabel.setEnabled(false);

        mappingField.setText(NbBundle.getMessage(WebCustomizer.class, "WebCustomizer.mappingField.text")); // NOI18N
        mappingField.setEnabled(false);

        jLabel1.setText(NbBundle.getMessage(WebCustomizer.class, "WebCustomizer.jLabel1.text")); // NOI18N
        jLabel1.setEnabled(false);

        displayNameField.setText(NbBundle.getMessage(WebCustomizer.class, "WebCustomizer.displayNameField.text")); // NOI18N
        displayNameField.setEnabled(false);

        useAsDefault.setText(NbBundle.getMessage(WebCustomizer.class, "WebCustomizer.useAsDefault.text")); // NOI18N
        useAsDefault.setEnabled(false);
        useAsDefault.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);
        useAsDefault.addActionListener(this);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(unlockPanel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 874, Short.MAX_VALUE)
                    .addComponent(useAsDefault)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(displayNameField, javax.swing.GroupLayout.DEFAULT_SIZE, 633, Short.MAX_VALUE))
                    .addComponent(listLabel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 173, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(servletList, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 874, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addComponent(nameLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(nameField, javax.swing.GroupLayout.DEFAULT_SIZE, 161, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(mappingLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(mappingField, javax.swing.GroupLayout.DEFAULT_SIZE, 155, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(displayNameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(listLabel)
                .addGap(11, 11, 11)
                .addComponent(servletList, javax.swing.GroupLayout.PREFERRED_SIZE, 162, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(nameLabel)
                    .addComponent(nameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(mappingLabel)
                    .addComponent(mappingField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(useAsDefault)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(unlockPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }

    // Code for dispatching events from components to event handlers.

    public void actionPerformed(java.awt.event.ActionEvent evt) {
        if (evt.getSource() == useAsDefault) {
            WebCustomizer.this.useAsDefaultActionPerformed(evt);
        }
    }// </editor-fold>//GEN-END:initComponents

    private void useAsDefaultActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_useAsDefaultActionPerformed
        if (inUpdateFields) return;
        Node[] n = mgr.getSelectedNodes();
        boolean sel = useAsDefault.isSelected();
        if (sel) {
            if (n.length == 1 && n[0].getLookup().lookup(String.class) != null) {
                for (Node nd : mgr.getRootContext().getChildren().getNodes()) {
                    nd.setValue(FileModelFactory.DEFAULT, Boolean.FALSE);
                }
            }
            n[0].setValue (FileModelFactory.DEFAULT, Boolean.TRUE);
            String webContextPath = (String) n[0].getValue(FileModelFactory.SERVLET_NAME);
            String mainUrl = (String) n[0].getValue (FileModelFactory.SERVLET_MAPPING);
            props.setWebContextPathAndMainUrl(webContextPath, mainUrl);
        } else {
            n[0].setValue (FileModelFactory.DEFAULT, Boolean.FALSE);
        }
    }//GEN-LAST:event_useAsDefaultActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField displayNameField;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel listLabel;
    private javax.swing.JTextField mappingField;
    private javax.swing.JLabel mappingLabel;
    private javax.swing.JTextField nameField;
    private javax.swing.JLabel nameLabel;
    private javax.swing.JScrollPane servletList;
    private org.netbeans.modules.javacard.project.customizer.WebEditorUnlockPanel unlockPanel1;
    private javax.swing.JCheckBox useAsDefault;
    // End of variables declaration//GEN-END:variables

    synchronized void setProperties(WebProjectProperties props) {
        this.props = props;
        setClassPath(props.getProject().getSourceClassPath());
    }
    private WebXmlModel diskModel;
    private final Object lock = new Object();
    private boolean locked;
    
    private class PEH implements ParseErrorHandler {
        public void handleError(IOException arg0) throws IOException {
            throw arg0;
        }

        public void handleBadAIDError(IllegalArgumentException arg0, String arg1) {
            Logger.getLogger (PEH.class.getName()).log (Level.INFO, "Bad AID in" + //NOI18N
                    " " + arg1, arg0); //NOI18N
        }

        public void unrecognizedElementEncountered(String arg0) throws IOException {
            //do nothing
        }
    }

    @Override
    protected void onSearchBegun() {
        if (!isDisplayable()) {
            return;
        }
        JCProject project = props.getProject();
        FileObject webXml = project.getProjectDirectory().getFileObject(
                JCConstants.WEB_DESCRIPTOR_PATH); //NOI18N

        try {
            InputStream in = webXml.getInputStream();
            WebXmlModel m;
            try {
                m = new WebXmlModel(in, new PEH());
            } finally {
                in.close();
            }
            synchronized (lock) {
                diskModel = m;
            }
            if (m.hasUnknownTags()) {
                //Need to disable component on the event queue
                EventQueue.invokeLater(new Runnable() {

                    public void run() {
                        locked = true;
                        ((CheckboxListView)servletList).setCheckboxesEnabled(false);
                        unlockPanel1.setVisible(true);
                        repaint();
                    }
                });
            }
            if (m.isError()) {
                category.setErrorMessage(NbBundle.getMessage(WebCustomizer.class,
                        "WEB_XML_HAS_ERRORS")); //NOI18N
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    @Override
    protected void onSearchCompleted() {
        Node[] nodes = mgr.getRootContext().getChildren().getNodes();
        if (nodes.length == 0) {
            category.setErrorMessage(NbBundle.getMessage(WebCustomizer.class,
                    "MSG_NO_SERVLETS_FOUND")); //NOI18N
        }
        final WebXmlModel m = getModelFromFile();
        if (m != null && !m.isError()) {
            FileModelFactory.writeTo(m, nodes);
            displayNameField.setEnabled(!locked);
        } else if (m != null && m.isError()) {
            unlockPanel1.setVisible(false);
        }
        for (Node n : nodes) {
            if (Boolean.TRUE.equals(n.getValue(CheckboxListView.SELECTED))) {
                checkedNodes.add(n);
            }
            String mapping = (String) n.getValue(FileModelFactory.SERVLET_MAPPING);
            String name = (String) n.getValue (FileModelFactory.SERVLET_NAME);
            if (name != null && mapping != null && name.equals(props.getWebContextPath())
                    && mapping.equals(props.getServletMapping())) {
                n.setValue (FileModelFactory.DEFAULT, Boolean.TRUE);
            }
        }
        props.setWebXmlFileModel(diskModel);
        ((CheckboxListView) servletList).setCheckboxesVisible(true);
        displayNameField.setText(m.getDisplayName());
        EventQueue.invokeLater(new Runnable() {


            public void run() {
                propertyChange(null);
                updateFields();
                servletList.requestFocus();
                displayNameField.setEnabled(!m.isError() && !locked);
            }
        });
    }


    public void insertUpdate(DocumentEvent e) {
        change(e.getDocument());
    }


    public void removeUpdate(DocumentEvent e) {
        change(e.getDocument());
    }


    public void changedUpdate(DocumentEvent e) {
        change(e.getDocument());
    }


    public void propertyChange(PropertyChangeEvent evt) {
        if (evt == null || ExplorerManager.PROP_SELECTED_NODES.equals(evt.getPropertyName())) {
            Node[] n = mgr.getSelectedNodes();
            boolean enable = false;
            if (n.length == 1) {
                enable = checkedNodes.contains(n[0]);
            }
            WebXmlModel m = getModelFromFile();
            if (m == null || m.isError()) {
                enable = false;
            }
            if (locked) {
                enable = false;
            }
            mappingLabel.setEnabled(enable);
            nameField.setEnabled(enable);
            mappingField.setEnabled(enable);
            nameLabel.setEnabled(enable);
            jLabel1.setEnabled(enable);
            useAsDefault.setEnabled(enable);
            if (evt != null) {
                updateFields();
            }
        }
    }
    private boolean inUpdateFields;

    private void updateFields() {
        inUpdateFields = true;
        try {
            Node[] nodes = mgr.getSelectedNodes();
            if (nodes.length == 0) {
                nameField.setText("");
                mappingField.setText("");
            } else {
                Node n = nodes[0];
                String mapping = (String) n.getValue(FileModelFactory.SERVLET_MAPPING);
                String name = (String) n.getValue(FileModelFactory.SERVLET_NAME);
                nameField.setText(name);
                mappingField.setText(mapping);
                if (mapping != null && name != null && mapping.equals(props.getServletMapping())
                        && name.equals(props.getWebContextPath())){
                    useAsDefault.setSelected(true);
                }
                boolean def = mgr.getRootContext().getChildren().getNodes().length
                    == 1 ? true :
                    Boolean.TRUE.equals(n.getValue(FileModelFactory.DEFAULT));
                useAsDefault.setSelected(def);
            }
        } finally {
            inUpdateFields = false;
        }
    }
    private final Set<Node> checkedNodes = new HashSet<Node>();


    public void onNodeChecked(Node node) {
        checkedNodes.add(node);
        propertyChange(null);
        updatePropsUiModel();
    }


    public void onNodeUnchecked(Node node) {
        checkedNodes.remove(node);
        propertyChange(null);
        updatePropsUiModel();
    }


    public void focusGained(FocusEvent e) {
        ((JTextComponent) e.getComponent()).selectAll();
    }


    public void focusLost(FocusEvent e) {
        //do nothing
    }

    boolean fileHasErrors() {
        WebXmlModel m = getModelFromFile();
        return m == null ? false : m.isError();
    }

    WebXmlModel getModelFromFile() {
        WebXmlModel result;
        synchronized (lock) {
            result = diskModel;
        }
        return result;
    }

    WebXmlModel getModelFromUI() {
        WebXmlModel result = FileModelFactory.webXmlModel(
                mgr.getRootContext().getChildren().getNodes());
        result.setDefaultServlet(nameField.getText().trim());
        result.setDefaultMapping(mappingField.getText().trim());
        result.setDisplayName(displayNameField.getText());
        return result;
    }

    public boolean hasChanges() {
        WebXmlModel fromUi = getModelFromUI();
        WebXmlModel fromFile = getModelFromFile();
        if (fromFile == null || fromFile.isError()) {
            return false;
        }
        return !fromFile.equals(fromUi);
    }

    public boolean isProblem() {
        return category.isValid();
    }

    public void setProblem(String problem) {
        boolean bad = problem == null ? false : problem.trim().length() == 0 ? false : true;
        category.setValid(!bad);
        if (bad) {
            category.setErrorMessage(problem);
        } else {
            category.setErrorMessage("");
        }
    }

    L l = new L();
    private class L implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            locked = false;
            ((CheckboxListView)servletList).setCheckboxesEnabled(true);
            unlockPanel1.setVisible(false);
            displayNameField.setEnabled(true);
            servletList.requestFocus();
            propertyChange(null);
            invalidate();
            revalidate();
            repaint();
        }
    }
}
