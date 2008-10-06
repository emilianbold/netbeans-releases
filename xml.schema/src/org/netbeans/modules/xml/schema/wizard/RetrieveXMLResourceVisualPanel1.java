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

package org.netbeans.modules.xml.schema.wizard;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.BadLocationException;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.xml.retriever.XMLCatalogProvider;
import org.netbeans.modules.xml.retriever.catalog.Utilities.DocumentTypesEnum;
import org.netbeans.modules.xml.xam.ui.ProjectConstants;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.openide.WizardValidationException;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.TemplateWizard;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.nodes.NodeAcceptor;
import org.openide.nodes.NodeOperation;
import org.openide.util.NbBundle;


public final class RetrieveXMLResourceVisualPanel1 extends JPanel implements DocumentListener{
    static final long serialVersionUID = 91839812;
    private static final String USER_PREF_SAVE_LOCATION_KEY = "USER_PREF_SAVE_LOCATION_KEY";
    private static final String USER_PREF_LOCAL_FILE_DIR_KEY = "USER_PREF_LOCAL_FILE_DIR_KEY";
    private RetrieveXMLResourceWizardPanel1 enclosingClass;
    private String schemaFileType = "retrieveSchemaResource";
    private String wsdlFileType = "retrieveWSDLResource";
    
    private String selectedSaveRootFolder = null;
    /**
     * Creates new form RetrieveXMLResourceVisualPanel1
     */
    public RetrieveXMLResourceVisualPanel1(RetrieveXMLResourceWizardPanel1 enclosure) {
        enclosingClass = enclosure;               
        initComponents();
        lfsSourceFileLocationTextField.getDocument().addDocumentListener(this);
        URLFileLocationTextField.getDocument().addDocumentListener(this);
        tpLocationTextField.getDocument().addDocumentListener(this);
        
    }
    
    public String getName() {
        return NbBundle.getMessage(RetrieveXMLResourceVisualPanel1.class, "LBL_step_description");//noi18n
    }

    public void validateFiles(String sourceFiles) throws WizardValidationException{
         String[] urls = sourceFiles.split(",");
            for (int i=0;i<urls.length;i++) {
                String urlString=urls[i].trim();
                if (urlString.length()==0) continue;
                
                File file = new File(urlString);
                if (!file.exists()) {
                    file = null;
                }
                if (file == null) {
                    URL url = null;
                try {
                    url = new java.net.URL(urlString);
                } catch (MalformedURLException e) {
                    String errorString = NbBundle.getMessage(RetrieveXMLResourceVisualPanel1.class, "INVALID_SCHEMA_FILE", urlString);
                    throw new WizardValidationException(lfsSourceFileLocationTextField, errorString, errorString);
                }
                try {
                    file = new File(url.toURI());
                } catch (URISyntaxException e) {
                    throw new WizardValidationException(lfsSourceFileLocationTextField, e.getMessage(), e.getLocalizedMessage());
                }
           }
        
           if (!file.isFile()) {
                throw new WizardValidationException(lfsSourceFileLocationTextField, "INVALID_SCHEMA_FILE", NbBundle.getMessage(RetrieveXMLResourceVisualPanel1.class, "INVALID_SCHEMA_FILE", urlString));
           }
        
           try {
                File normFile = FileUtil.normalizeFile(file);
                FileObject fo = FileUtil.toFileObject(normFile);
                if (fo == null) {
                    String errorMessage = NbBundle.getMessage(RetrieveXMLResourceVisualPanel1.class, "INVALID_SCHEMA_FILE", urlString);
                    throw new WizardValidationException(lfsSourceFileLocationTextField, errorMessage, errorMessage);
               }            
           } catch (WizardValidationException e) {
               throw e;
           } catch (Throwable e) {
               String errorMessage = NbBundle.getMessage(RetrieveXMLResourceVisualPanel1.class, "INVALID_SCHEMA_FILE", urlString);
               throw new WizardValidationException(lfsSourceFileLocationTextField, errorMessage, errorMessage);
           }
       }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        sourceTypeRadioButtonGroup = new javax.swing.ButtonGroup();
        sourceLabel = new javax.swing.JLabel();
        fromURLButton = new javax.swing.JRadioButton();
        URLFileLocationTextField = new javax.swing.JTextField();
        closureCheckBox = new javax.swing.JCheckBox();
        fromLocalFileButton = new javax.swing.JRadioButton();
        lfsSourceFileLocationTextField = new javax.swing.JTextField();
        lfsBrowseButton = new javax.swing.JButton();
        targetLabel = new javax.swing.JLabel();
        tpLocationLabel = new javax.swing.JLabel();
        tpLocationTextField = new javax.swing.JTextField();
        tpLocationBrowseButton = new javax.swing.JButton();
        overwriteFiles = new javax.swing.JCheckBox();

        setName(org.openide.util.NbBundle.getMessage(RetrieveXMLResourceVisualPanel1.class, "LBL_step_description")); // NOI18N

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/xml/schema/wizard/Bundle"); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(sourceLabel, bundle.getString("LBL_select_source")); // NOI18N

        sourceTypeRadioButtonGroup.add(fromURLButton);
        fromURLButton.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(fromURLButton, bundle.getString("TXT_FromURLResource")); // NOI18N
        fromURLButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        fromURLButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        fromURLButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fromURLButtonPressed(evt);
            }
        });

        URLFileLocationTextField.setText("http://");
        URLFileLocationTextField.setToolTipText(lfsSourceFileLocationTextField.getText());
        URLFileLocationTextField.setNextFocusableComponent(lfsBrowseButton);
        URLFileLocationTextField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                refreshToolTips(evt);
            }
        });

        closureCheckBox.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(closureCheckBox, bundle.getString("LBL_closure_checkbox_label")); // NOI18N
        closureCheckBox.setToolTipText(bundle.getString("TIP_retrieve_closure_tool_tip")); // NOI18N
        closureCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        closureCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        closureCheckBox.setVisible(false);
        closureCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                retrieveCheckBoxEventHandler(evt);
            }
        });

        sourceTypeRadioButtonGroup.add(fromLocalFileButton);
        org.openide.awt.Mnemonics.setLocalizedText(fromLocalFileButton, bundle.getString("TXT_FromLocalFileResource")); // NOI18N
        fromLocalFileButton.setToolTipText(org.openide.util.NbBundle.getMessage(RetrieveXMLResourceVisualPanel1.class, "HINT_Files")); // NOI18N
        fromLocalFileButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        fromLocalFileButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        fromLocalFileButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fromLocalFileButtonActionPerformed(evt);
            }
        });

        lfsSourceFileLocationTextField.setToolTipText(org.openide.util.NbBundle.getMessage(RetrieveXMLResourceVisualPanel1.class, "HINT_Files")); // NOI18N
        lfsSourceFileLocationTextField.setEnabled(false);
        lfsSourceFileLocationTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                lfsSourceFileLocationTextFieldActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(lfsBrowseButton, bundle.getString("LBL_source_browse")); // NOI18N
        lfsBrowseButton.setEnabled(false);
        lfsBrowseButton.setNextFocusableComponent(tpLocationTextField);
        lfsBrowseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                invokeFileChooserForSource(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(targetLabel, bundle.getString("LBL_select_target")); // NOI18N

        tpLocationLabel.setLabelFor(tpLocationTextField);
        org.openide.awt.Mnemonics.setLocalizedText(tpLocationLabel, bundle.getString("LBL_save_file_location")); // NOI18N

        tpLocationTextField.setEditable(false);
        tpLocationTextField.setToolTipText(tpLocationTextField.getText());
        tpLocationTextField.setNextFocusableComponent(tpLocationBrowseButton);
        tpLocationTextField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                refreshToolTips(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(tpLocationBrowseButton, bundle.getString("LBL_target_browse")); // NOI18N
        tpLocationBrowseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                invokeFileChooserForTarget(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(overwriteFiles, bundle.getString("LBL_OVERWIRITE_FILES")); // NOI18N
        overwriteFiles.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        overwriteFiles.setMargin(new java.awt.Insets(0, 0, 0, 0));

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, URLFileLocationTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 488, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                        .add(10, 10, 10)
                        .add(overwriteFiles))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                        .add(10, 10, 10)
                        .add(closureCheckBox))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, sourceLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, fromURLButton)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, targetLabel)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .add(tpLocationLabel)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(tpLocationTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 215, Short.MAX_VALUE))
                            .add(lfsSourceFileLocationTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 308, Short.MAX_VALUE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(lfsBrowseButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(tpLocationBrowseButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, fromLocalFileButton))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(sourceLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(fromURLButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(URLFileLocationTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 21, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(closureCheckBox)
                .add(11, 11, 11)
                .add(fromLocalFileButton)
                .add(5, 5, 5)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lfsBrowseButton)
                    .add(lfsSourceFileLocationTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 21, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(11, 11, 11)
                .add(targetLabel)
                .add(2, 2, 2)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(tpLocationBrowseButton)
                    .add(tpLocationTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(tpLocationLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(overwriteFiles)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        sourceLabel.getAccessibleContext().setAccessibleName(bundle.getString("LBL_select_source")); // NOI18N
        sourceLabel.getAccessibleContext().setAccessibleDescription(bundle.getString("LBL_select_source")); // NOI18N
        fromURLButton.getAccessibleContext().setAccessibleName(bundle.getString("TXT_FromURLResource")); // NOI18N
        fromURLButton.getAccessibleContext().setAccessibleDescription(bundle.getString("TXT_FromURLResource")); // NOI18N
        URLFileLocationTextField.getAccessibleContext().setAccessibleName(bundle.getString("TXT_FromURLResource")); // NOI18N
        URLFileLocationTextField.getAccessibleContext().setAccessibleDescription(bundle.getString("TXT_FromURLResource")); // NOI18N
        closureCheckBox.getAccessibleContext().setAccessibleDescription("Retrieve dependent files");
        fromLocalFileButton.getAccessibleContext().setAccessibleName(bundle.getString("TXT_FromLocalFileResource")); // NOI18N
        fromLocalFileButton.getAccessibleContext().setAccessibleDescription(bundle.getString("TXT_FromLocalFileResource")); // NOI18N
        lfsSourceFileLocationTextField.getAccessibleContext().setAccessibleName(bundle.getString("TXT_FromLocalFileResource")); // NOI18N
        lfsSourceFileLocationTextField.getAccessibleContext().setAccessibleDescription(bundle.getString("TXT_FromLocalFileResource")); // NOI18N
        lfsBrowseButton.getAccessibleContext().setAccessibleName(bundle.getString("A11Y_target_browse")); // NOI18N
        lfsBrowseButton.getAccessibleContext().setAccessibleDescription(bundle.getString("A11Y_target_browse")); // NOI18N
        tpLocationTextField.getAccessibleContext().setAccessibleName(bundle.getString("LBL_save_file_location")); // NOI18N
        tpLocationTextField.getAccessibleContext().setAccessibleDescription(bundle.getString("LBL_save_file_location")); // NOI18N
        tpLocationBrowseButton.getAccessibleContext().setAccessibleName(bundle.getString("A11Y_target_browse")); // NOI18N
        tpLocationBrowseButton.getAccessibleContext().setAccessibleDescription(bundle.getString("A11Y_target_browse")); // NOI18N
        overwriteFiles.getAccessibleContext().setAccessibleName("Overwrite files with same name.");
        overwriteFiles.getAccessibleContext().setAccessibleDescription("Overwrite files with same name.");
    }// </editor-fold>//GEN-END:initComponents
    
    private void fromURLButtonPressed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fromURLButtonPressed
        lfsSourceFileLocationTextField.setEnabled(false);
        URLFileLocationTextField.setEnabled(true);
        lfsBrowseButton.setEnabled(false);
        fireChange();
    }//GEN-LAST:event_fromURLButtonPressed
    
    private void fromLocalFileButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fromLocalFileButtonActionPerformed
        lfsSourceFileLocationTextField.setEnabled(true);
        URLFileLocationTextField.setEnabled(false);
        lfsBrowseButton.setEnabled(true);
        fireChange();
    }//GEN-LAST:event_fromLocalFileButtonActionPerformed
    
    private void refreshToolTips(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_refreshToolTips
        tpLocationTextField.setToolTipText(tpLocationTextField.getText());
        lfsSourceFileLocationTextField.setToolTipText(lfsSourceFileLocationTextField.getText());
    }//GEN-LAST:event_refreshToolTips
    
    private void retrieveCheckBoxEventHandler(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_retrieveCheckBoxEventHandler
        String str = URLFileLocationTextField.getText();
        if(closureCheckBox.isSelected()){
            try{
                String targetLocStr = new File(new URI(new URI(new File(selectedSaveRootFolder).toURI().toString()+"/"+new URL(str).toURI().getSchemeSpecificPart().replace(':', '_')).normalize().toString())).toString();
                tpLocationTextField.setText(targetLocStr);
            } catch(Exception exp){
            }
        }else{
            try{
                String fileName = new URL(str).getFile();
                if(fileName.lastIndexOf("/")!= -1)
                    fileName = fileName.substring(fileName.lastIndexOf("/")+1);
                if(fileName != null)
                    tpLocationTextField.setText(selectedSaveRootFolder+File.separator+fileName);
            }catch(Exception exception){}
        }
    }//GEN-LAST:event_retrieveCheckBoxEventHandler
    
    private void invokeFileChooserForTarget(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_invokeFileChooserForTarget
        Sources srcs = ProjectUtils.getSources(getProject());
        String prjName = ProjectUtils.getInformation((getProject())).getName();
        LogicalViewProvider lvp = (LogicalViewProvider) getProject().getLookup().lookup(LogicalViewProvider.class);
        SourceGroup[] sgs = srcs.getSourceGroups(XMLCatalogProvider.TYPE_RETRIEVED);
        if(sgs == null || sgs.length < 1)
            sgs = srcs.getSourceGroups(ProjectConstants.JAVA_SOURCES_TYPE);
        List<FileObject> fobjs = new ArrayList<FileObject>();
        //get all the srcgrps root FOs
        if( (sgs == null) || (sgs.length <= 0) ){
            fobjs.add(getProject().getProjectDirectory());
        }else{
            for(SourceGroup sg: sgs){
                fobjs.add(sg.getRootFolder());
            }
        }
        //get their nodes
        final List<Node> nodeL = new ArrayList<Node>();
        for(FileObject fo: fobjs){
            Node node = null;
            try{
                node = DataObject.find(fo).getNodeDelegate();
            }catch(Exception e) {
                continue;
            }
            if(node != null){
                node = new FilterNode(node);
                nodeL.add(node);
            }
        }
        //now determine a root node.
        Node node = null;
        Node dummyRootNode = null;
        //if(nodeL.size() > 1){
            //more than 1 node so create a dummy root node
            Node root = lvp.createLogicalView();
            dummyRootNode = new FilterNode(root, new Children.Array());
            Node nodes[] = nodeL.toArray(new Node[nodeL.size()]);
            dummyRootNode.getChildren().add(nodes);
            node = dummyRootNode;
        //}else{
          //  node = nodeL.get(0);
        //}
        final Node finalDummyNode = dummyRootNode;
        Node[] selectedNodes = null;
        try{
            selectedNodes = NodeOperation.getDefault().select(NbBundle.getMessage(RetrieveXMLResourceVisualPanel1.class, "TITLE_choose_target_folder"),
                    "", node,
                    new NodeAcceptor() {
                public boolean acceptNodes(Node[] node) {
                    if( (node == null) || (node.length < 1) )
                        return false;
                    if( (finalDummyNode != null) && (node[0] == finalDummyNode) )
                        return false;
                    DataObject dobj = (DataObject) node[0].getLookup().lookup(DataObject.class);
                    if(dobj.getPrimaryFile().isFolder())
                        return true;
                    return false;
                }
            }
            );
        }catch(Exception e){
            return;
        }
        if((selectedNodes == null) || (selectedNodes[0] == null))
            return;
        DataObject dobj = (DataObject) selectedNodes[0].getLookup().lookup(DataObject.class);
        FileObject fob = dobj.getPrimaryFile();
        String saveFolder = FileUtil.toFile(fob).toString();
        selectedSaveRootFolder = saveFolder;
        if(getSelectedSourceType() == SourceType.LOCAL_FILE){
            String sourceFolderName = lfsSourceFileLocationTextField.getText();
            sourceFolderName = sourceFolderName.substring(sourceFolderName.lastIndexOf(File.separator)+1);
            tpLocationTextField.setText(saveFolder);
        }
        if(getSelectedSourceType() == SourceType.URL_ADDR){
            String sourceURL = lfsSourceFileLocationTextField.getText();
            URI uri = null;
            try {
                uri = new URI(sourceURL);
            } catch (URISyntaxException ex) {
                ex.printStackTrace();
            }
            String domainName = uri.getSchemeSpecificPart();
            domainName = domainName.replace(':','_');
            String saveFolderURIStr = new File(saveFolder).toURI().toString();
            String finalSaveFolder = saveFolderURIStr+"/"+domainName;
            URI tmpURI = null;
            try {
                tmpURI = new URI(finalSaveFolder);
            } catch (URISyntaxException ex) {
                tpLocationTextField.setText("");
                return;
            }
            finalSaveFolder = new File(tmpURI.normalize()).toString();
            tpLocationTextField.setText(finalSaveFolder);
        }
    }//GEN-LAST:event_invokeFileChooserForTarget
    
    private void invokeFileChooserForSource(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_invokeFileChooserForSource
        JFileChooser fileChooser = new JFileChooser();
        
        String str = lfsSourceFileLocationTextField.getText();
        if((str != null) && (str.length() != 0))
            fileChooser.setSelectedFiles(getSchemaFiles(str));
        
        fileChooser.setMultiSelectionEnabled(true);
        fileChooser.setFileSelectionMode(fileChooser.FILES_ONLY);
        fileChooser.setFileFilter(new XSDFilter());
        if(fileChooser.showOpenDialog((Component) evt.getSource()) == fileChooser.APPROVE_OPTION){
            File[] files = fileChooser.getSelectedFiles();
            StringBuffer sb = new StringBuffer();
            for (File f : files) {
                if (sb.length() > 0){
                    sb.append(",");
                }
                sb.append(f.toURI().normalize().toString());
                       
            }
            lfsSourceFileLocationTextField.setText(sb.toString());
        }
    }//GEN-LAST:event_invokeFileChooserForSource

    private void lfsSourceFileLocationTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_lfsSourceFileLocationTextFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_lfsSourceFileLocationTextFieldActionPerformed
    int previouslayoutIndex = 0;
    
    public String getSourceLocation(){
        String str = null;
        if (getSelectedSourceType() == SourceType.LOCAL_FILE) {
            str = lfsSourceFileLocationTextField.getText();
        } else {
            str = URLFileLocationTextField.getText();
        }
        if((str == null) || (str.length() <= 0))
            return null;
        //pref.put(USER_PREF_LOCAL_FILE_DIR_KEY, str);
        if(getSelectedSourceType() == SourceType.LOCAL_FILE){
           return str;
        }
        if(getSelectedSourceType() == SourceType.URL_ADDR){
            return str;
        }
        return null;
    }
    
    public enum SourceType{
        LOCAL_FILE,
        URL_ADDR,
        UDDI_ADDR
    };
    
    public SourceType getSelectedSourceType() {
        SourceType sourceType = SourceType.URL_ADDR;
        if (fromLocalFileButton.isSelected()) {
            sourceType = SourceType.LOCAL_FILE;
        }
        return sourceType;
    }
    
    public String getURLLocation(){
        String str = lfsSourceFileLocationTextField.getText();
        //pref.put(USER_PREF_LOCAL_FILE_DIR_KEY, str);
        if((str != null) && (str.length() > 0)){
            str = new File(str).toURI().toString();
            return str;
        }
        return null;
    }
    
    public DocumentTypesEnum getDocType(){
        if(((TemplateWizard) enclosingClass.getWizardDescriptor()).getTemplate().getName().equals(schemaFileType) )
            return DocumentTypesEnum.schema;
        else 
            return DocumentTypesEnum.wsdl;
    }
    
    public File getSaveLocation(){
        String dir = tpLocationTextField.getText();
        if((dir == null) || (dir.trim().length() == 0))
            dir = null;
        if(dir != null){
            File result = new File(dir);
            return result;
        }
        return null;
    }
    
    public File getSelectedSaveRootFolder(){
        if(selectedSaveRootFolder == null)
            return null;
        return new File(selectedSaveRootFolder);
    }
    
    public boolean retrieveClosure(){
        return closureCheckBox.isSelected();
    }
    
    private Project getProject(){
        Project prj = (Project) enclosingClass.getWizardDescriptor().getProperty(IConstants.CURRENT_PROJECT_KEY);
        return prj;
    }
    
    public void refreshSaveLocation(){
        try {
            DataFolder df =
                    ((TemplateWizard) enclosingClass.getWizardDescriptor()).getTargetFolder();
            String str = FileUtil.toFile(df.getPrimaryFile()).toString();
            selectedSaveRootFolder = str;
            tpLocationTextField.setText(str);
        } catch (IOException ioe) {
            // cannot get the target folder, so just ignore the exception and
            // require the user to specify it
        }
    }
    private final List<ChangeListener> listeners = new ArrayList<ChangeListener>();
    
    public void addChangeListener(ChangeListener l) {
        listeners.add(l);
    }
    
    public void removeChangeListener(ChangeListener l) {
        listeners.remove(l);
    }
    
    private void fireChange() {
        ChangeEvent e = new ChangeEvent(this);
        Iterator it = listeners.iterator();
        while (it.hasNext()) {
            ((ChangeListener)it.next()).stateChanged(e);
        }
    }
    
    public void insertUpdate(DocumentEvent e) {
        changedUpdate(e);
    }
    
    public void removeUpdate(DocumentEvent e) {
        changedUpdate(e);
    }
    
    public void changedUpdate(DocumentEvent e) {
        fireChange();
        if(getSelectedSourceType() == SourceType.URL_ADDR){
            String str = null;
            try {
                str = e.getDocument().getText(0, e.getDocument().getLength());
            } catch (BadLocationException ex) {
                return;
            }
            if(str.trim().equalsIgnoreCase(lfsSourceFileLocationTextField.getText().trim())){
                retrieveCheckBoxEventHandler(null);
            }
        }
    }

    public Boolean shouldOverwrite() {
        return new Boolean(overwriteFiles.isSelected());
    }    
    
    public File[] getSchemaFiles(String schemas) {
            String[] urls = schemas.split(",");
            List<File> infos = new ArrayList<File>();
            for (int i=0;i<urls.length;i++) {
                String urlString=urls[i].trim();
                if (urlString.length()==0) continue;
                String url = null;
                File file = new File(urlString);
                if (file.exists()) {
                    infos.add(file);
                } 
                          
            }
            return infos.toArray(new File[infos.size()]);
        }
      
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField URLFileLocationTextField;
    private javax.swing.JCheckBox closureCheckBox;
    private javax.swing.JRadioButton fromLocalFileButton;
    private javax.swing.JRadioButton fromURLButton;
    private javax.swing.JButton lfsBrowseButton;
    private javax.swing.JTextField lfsSourceFileLocationTextField;
    private javax.swing.JCheckBox overwriteFiles;
    private javax.swing.JLabel sourceLabel;
    private javax.swing.ButtonGroup sourceTypeRadioButtonGroup;
    private javax.swing.JLabel targetLabel;
    private javax.swing.JButton tpLocationBrowseButton;
    private javax.swing.JLabel tpLocationLabel;
    private javax.swing.JTextField tpLocationTextField;
    // End of variables declaration//GEN-END:variables
    

     class XSDFilter extends FileFilter {

    //Accept all directories and xsd/wsdl files.
    public boolean accept(File f) {
        if (f.isDirectory()) {
            return true;
        }
        DocumentTypesEnum docExt = getDocType();
        String extension = getExtension(f);
        if (extension != null) {
            if (extension.equals("xsd") && docExt.equals(DocumentTypesEnum.schema)) {
                    return true;
            } else if(docExt.equals(DocumentTypesEnum.wsdl) && extension.equals("wsdl")) {
                return true;
            } else
                return false;
        }

        return false;
    }

    //The description of this filter
    public String getDescription() {
        DocumentTypesEnum docExt = getDocType();
        if(docExt.equals(DocumentTypesEnum.wsdl))
            return "Only WSDL files";
        
        return "only XSD files";
            
    }
    
    public String getExtension(File f) {
        String ext = null;
        String s = f.getName();
        int i = s.lastIndexOf('.');

        if (i > 0 &&  i < s.length() - 1) {
            ext = s.substring(i+1).toLowerCase();
        }
        return ext;
    }
  }
}
