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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-200? Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.web.jsf.wizards;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.editor.ext.html.parser.api.AstNode;
import org.netbeans.modules.html.editor.api.gsf.HtmlParserResult;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser.Result;
import org.netbeans.modules.web.api.webmodule.WebProjectConstants;
import org.netbeans.modules.web.jsf.dialogs.BrowseFolders;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author Petr Pisl
 */

public class TemplateClientPanelVisual extends javax.swing.JPanel implements HelpCtx.Provider {
    
    private WizardDescriptor wizardDescriptor;
    
    private final Set/*<ChangeListener>*/ listeners = new HashSet(1);

    private final static String NAME_SPACE = "http://java.sun.com/jsf/facelets";    //NOI18N
    private final static String TAG_NAME = "ui:insert";    //NOI18N
    private final static String VALUE_NAME = "name";    //NOI18N

    /** Creates new form TemplateClientPanel */
    public TemplateClientPanelVisual(WizardDescriptor wizardDescriptor) {
        initComponents();
        this.wizardDescriptor = wizardDescriptor;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        bgRootTag = new javax.swing.ButtonGroup();
        jrbHtml = new javax.swing.JRadioButton();
        jrbComposition = new javax.swing.JRadioButton();
        jlRootTag = new javax.swing.JLabel();
        jlTemplate = new javax.swing.JLabel();
        jtfTemplate = new javax.swing.JTextField();
        jbBrowse = new javax.swing.JButton();

        bgRootTag.add(jrbHtml);
        jrbHtml.setSelected(true);
        jrbHtml.setText("<html>&lt;html&gt;</html>");
        jrbHtml.setActionCommand("html");
        jrbHtml.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jrbHtml.setMargin(new java.awt.Insets(0, 0, 0, 0));

        bgRootTag.add(jrbComposition);
        jrbComposition.setText("<ui:composition>");
        jrbComposition.setActionCommand("composition");
        jrbComposition.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jrbComposition.setMargin(new java.awt.Insets(0, 0, 0, 0));

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/web/jsf/wizards/Bundle"); // NOI18N
        jlRootTag.setText(bundle.getString("LBL_RootTag")); // NOI18N

        jlTemplate.setText(bundle.getString("LBL_SelectTemplate")); // NOI18N

        jtfTemplate.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jtfTemplateKeyReleased(evt);
            }
        });

        jbBrowse.setText(bundle.getString("LBL_Browse")); // NOI18N
        jbBrowse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbBrowseActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jlTemplate)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(138, 138, 138)
                        .addComponent(jtfTemplate, javax.swing.GroupLayout.DEFAULT_SIZE, 313, Short.MAX_VALUE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jbBrowse))
            .addGroup(layout.createSequentialGroup()
                .addComponent(jlRootTag)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jrbComposition)
                    .addComponent(jrbHtml, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(250, 250, 250))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jlTemplate)
                    .addComponent(jbBrowse)
                    .addComponent(jtfTemplate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jlRootTag)
                    .addComponent(jrbHtml, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jrbComposition)
                .addContainerGap(33, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    
    private void jtfTemplateKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jtfTemplateKeyReleased
        //validateTemplate(new File(jtfTemplate.getText()));
        templateData = Collections.EMPTY_SET;
        fireChangeEvent();
    }//GEN-LAST:event_jtfTemplateKeyReleased
    
    private void jbBrowseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbBrowseActionPerformed

        Sources sources = (Sources) Templates.getProject(wizardDescriptor).getLookup().lookup(org.netbeans.api.project.Sources.class);
        SourceGroup[] sourceGroups = sources.getSourceGroups(WebProjectConstants.TYPE_DOC_ROOT);
        
        org.openide.filesystems.FileObject fo = BrowseFolders.showDialog(sourceGroups);
        if (fo != null) {
            File file = FileUtil.toFile(fo);
            jtfTemplate.setText(file.getAbsolutePath());
            templateData = Collections.EMPTY_SET;
        }
        fireChangeEvent();
    }//GEN-LAST:event_jbBrowseActionPerformed
    
    protected boolean validateTemplate() {
        if (templateData != null && templateData.size() != 0) {
            return true;
        }
        String message = null;
        String name = jtfTemplate.getText();
        if (name == null || "".equals(name)) {
            message = "MSG_NoTemplateSelected"; //NOI18N
        } else {
            File file = new File (name);
            if (file.exists()) {
                if (!file.isDirectory()){
                    FileObject fo = FileUtil.toFileObject(file);
                    Source source = Source.create(fo);
                    final int startOffset = 0;
                    templateData = new ArrayList<String>();
                    try {
                        ParserManager.parse(Collections.singleton(source), new UserTask() {

                            @Override
                            public void run(ResultIterator resultIterator) throws Exception {
                                Result result = resultIterator.getParserResult(startOffset);
                                if (result.getSnapshot().getMimeType().equals("text/html")) {
                                    HtmlParserResult htmlResult = (HtmlParserResult)result;
                                    if (htmlResult.getNamespaces().containsKey(NAME_SPACE)) {
                                        List<AstNode> foundNodes = findValue(htmlResult.root(NAME_SPACE).children(), TAG_NAME, new ArrayList<AstNode>());

                                        for (AstNode node : foundNodes) {
                                            AstNode.Attribute attr = node.getAttribute(VALUE_NAME);
                                            if (attr !=null) {
                                                String value = attr.unquotedValue();
                                                if (value != null && !"".equals(value)) {   //NOI18N
                                                    templateData.add(value);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        });
                    } catch (ParseException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                    if (templateData == null || templateData.size()==0) {
                              message = "MSG_NoFaceletsTemplate"; //NOI18N
                    }
                } else {
                    message = "MSG_TemplateHasToBeFile";   //NO18N
                }
            } else {
                message = "MSG_EneterExistingTemplate";    //NOI18N
            }
        }
        if (message != null){
            wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,        //NOI18N
                    NbBundle.getMessage(TemplateClientPanelVisual.class, message));
        }
        return (message == null);
    }

    private List<AstNode> findValue(List<AstNode> nodes, String tagName, List<AstNode> foundNodes) {
        if (nodes == null) {
            return foundNodes;
        }
        for (int i = 0; i < nodes.size(); i++) {
            AstNode node = nodes.get(i);
            if (tagName.equals(node.name())) {
                foundNodes.add(node);
            } else {
                foundNodes = findValue(node.children(), tagName, foundNodes);
            }

        }
        return foundNodes;
    }
    
    public HelpCtx getHelpCtx() {
        return new HelpCtx(TemplateClientPanelVisual.class);
    }
    
    public InputStream getTemplateClient(){
        String path = "org/netbeans/modules/web/jsf/facelets/resources/templates/";  //NOI18N
        path = path + bgRootTag.getSelection().getActionCommand() + "TemplateClient.template";          //NOI18N
        InputStream is = this.getClass().getClassLoader().getResourceAsStream(path);
        return is;
    }

    Collection<String> templateData = Collections.EMPTY_SET;
    
    public Collection<String> getTemplateData(){
        return templateData;
    }
    
    public FileObject getTemplate(){
        FileObject template = null;
        String name = jtfTemplate.getText();
        if (name != null && !"".equals(name)){
            File file = new File (name);
            template = FileUtil.toFileObject(file);
        }
        return template;
    }
    
    protected void addChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }
    
    protected void removeChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }
    
    protected final void fireChangeEvent() {
        Iterator it;
        synchronized (listeners) {
            it = new HashSet(listeners).iterator();
        }
        ChangeEvent ev = new ChangeEvent(this);
        while (it.hasNext()) {
            ((ChangeListener)it.next()).stateChanged(ev);
        }
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup bgRootTag;
    private javax.swing.JButton jbBrowse;
    private javax.swing.JLabel jlRootTag;
    private javax.swing.JLabel jlTemplate;
    private javax.swing.JRadioButton jrbComposition;
    private javax.swing.JRadioButton jrbHtml;
    private javax.swing.JTextField jtfTemplate;
    // End of variables declaration//GEN-END:variables
    
}
