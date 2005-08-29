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

package org.netbeans.modules.j2ee.ddloaders.web.multiview;

import org.netbeans.modules.j2ee.dd.api.web.JspPropertyGroup;
import org.netbeans.modules.j2ee.ddloaders.web.*;
import org.netbeans.modules.xml.multiview.ui.*;
import org.netbeans.modules.xml.multiview.Error;
import org.netbeans.modules.xml.multiview.Utils;
import org.openide.util.NbBundle;
import org.netbeans.api.project.SourceGroup;

/**
 * @author  mkuchtiak
 */
public class JspPGPanel extends SectionInnerPanel implements java.awt.event.ItemListener {
    private JspPropertyGroup group;
    private DDDataObject dObj;
    /** Creates new form JspPGPanel */
    public JspPGPanel(SectionView sectionView, DDDataObject dObj,JspPropertyGroup group) {
        super(sectionView);
        this.dObj=dObj;
        this.group=group;
        initComponents();
        
        dispNameTF.setText(group.getDefaultDisplayName());
        addModifier(dispNameTF);
        
        Utils.makeTextAreaLikeTextField(descriptionTA,dispNameTF);
        descriptionTA.setText(group.getDefaultDescription());
        addModifier(descriptionTA);        
        
        pageEncodingTF.setText(group.getPageEncoding());
        addModifier(pageEncodingTF);
        
        jCheckBox1.setSelected(group.isElIgnored());
        jCheckBox1.addItemListener(this);
        jCheckBox2.setSelected(group.isScriptingInvalid());
        jCheckBox2.addItemListener(this);
        jCheckBox3.setSelected(group.isIsXml());
        jCheckBox3.addItemListener(this);
        
        urlPatternsTF.setText(DDUtils.urlPatternList(group.getUrlPattern()));
        addValidatee(urlPatternsTF);
        
        preludeTF.setText(DDUtils.urlPatternList(group.getIncludePrelude()));
        addModifier(preludeTF);
        
        codaTF.setText(DDUtils.urlPatternList(group.getIncludeCoda()));
        addModifier(codaTF);
        
        LinkButton linkButton = new LinkButton(this, group, "url_patterns"); //NOI18N
        java.awt.GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 0, 0);
        linkButton.setText(NbBundle.getMessage(JspPGPanel.class, "LBL_goToSources"));
        linkButton.setMnemonic(NbBundle.getMessage(JspPGPanel.class, "LBL_goToSource_mnem").charAt(0));
        add(linkButton, gridBagConstraints);
        
        linkButton = new LinkButton(this, group, "preludes"); //NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 0, 0);
        linkButton.setText(NbBundle.getMessage(JspPGPanel.class, "LBL_goToSources"));
        linkButton.setMnemonic(NbBundle.getMessage(JspPGPanel.class, "LBL_goToSource_mnem1").charAt(0));
        add(linkButton, gridBagConstraints);

        linkButton = new LinkButton(this, group, "codas"); //NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 5, 0);
        linkButton.setText(NbBundle.getMessage(JspPGPanel.class, "LBL_goToSources"));
        linkButton.setMnemonic(NbBundle.getMessage(JspPGPanel.class, "LBL_goToSource_mnem2").charAt(0));
        add(linkButton, gridBagConstraints);
    }
    public javax.swing.JComponent getErrorComponent(String errorId) {
        if ("url_patterns".equals(errorId)) return urlPatternsTF;
        return null;
    }
    
    public void documentChanged(javax.swing.text.JTextComponent comp, String value) {
        if (comp==urlPatternsTF) {
            String val = (String)value;
            if (val.length()==0) {
                getSectionView().getErrorPanel().setError(new Error(Error.TYPE_FATAL, Error.MISSING_VALUE_MESSAGE, "URL Pattern",urlPatternsTF));
                return;
            }
            getSectionView().getErrorPanel().clearError();
        }
    }

    public void setValue(javax.swing.JComponent source, Object value) {
        if (source==urlPatternsTF) {
            String text = (String)value;
            // change servlet-mappings
            setUrlPatterns(text);
        } else if (source==dispNameTF) {
            String text = (String)value;
            group.setDisplayName(text.length()==0?null:text);
            SectionPanel enclosingPanel = getSectionView().findSectionPanel(group);
            enclosingPanel.setTitle(((PagesMultiViewElement.PagesView)getSectionView()).getJspGroupTitle(group));
            enclosingPanel.getNode().setDisplayName(((PagesMultiViewElement.PagesView)getSectionView()).getJspGroupNodeName(group));
        } else if (source==descriptionTA) {
            String text = (String)value;
            group.setDescription(text.length()==0?null:text);
        } else if (source==pageEncodingTF) {
            String text = (String)value;
            group.setPageEncoding(text.length()==0?null:text);
        } else if (source==preludeTF) {
            String text = (String)value;
            setPreludes(text);
        } else if (source==codaTF) {
            String text = (String)value;
            setCodas(text);
        }
    }
    
    private void setUrlPatterns(String text) {
        String[] urlPatterns = DDUtils.getStringArray(text);
        group.setUrlPattern(urlPatterns);
        SectionPanel enclosingPanel = getSectionView().findSectionPanel(group);
        enclosingPanel.setTitle(((PagesMultiViewElement.PagesView)getSectionView()).getJspGroupTitle(group));
    }
    private void setPreludes(String text) {
        String[] preludes = DDUtils.getStringArray(text);
        group.setIncludePrelude(preludes);
    }
    private void setCodas(String text) {
        String[] codas = DDUtils.getStringArray(text);
        group.setIncludeCoda(codas);
    }
    public void rollbackValue(javax.swing.text.JTextComponent source) {
        if (source==urlPatternsTF) {
            urlPatternsTF.setText(DDUtils.urlPatternList(group.getUrlPattern()));
        }
    }
    
    public void linkButtonPressed(Object obj, String id) {
        String text=null;
        if ("url_patterns".equals(id)) { 
            text = urlPatternsTF.getText();
        } else if ("preludes".equals(id)) {
            text = preludeTF.getText();
        } else if ("codas".equals(id)) {
            text = codaTF.getText();
        }
        java.util.StringTokenizer tok = new java.util.StringTokenizer(text,",");
        DDUtils.openEditorForFiles(dObj,tok);
    }
    
    
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        dispNameLabel = new javax.swing.JLabel();
        dispNameTF = new javax.swing.JTextField();
        descriptionLabel = new javax.swing.JLabel();
        descriptionTA = new javax.swing.JTextArea();
        urlPatternsLabel = new javax.swing.JLabel();
        urlPatternsTF = new javax.swing.JTextField();
        browseButton1 = new javax.swing.JButton();
        hintUrlPatterns = new javax.swing.JLabel();
        pageEncodingLabel = new javax.swing.JLabel();
        pageEncodingTF = new javax.swing.JTextField();
        jCheckBox1 = new javax.swing.JCheckBox();
        jCheckBox2 = new javax.swing.JCheckBox();
        jCheckBox3 = new javax.swing.JCheckBox();
        preludeLabel = new javax.swing.JLabel();
        preludeTF = new javax.swing.JTextField();
        browseButton2 = new javax.swing.JButton();
        codaLabel = new javax.swing.JLabel();
        codaTF = new javax.swing.JTextField();
        browseButton3 = new javax.swing.JButton();
        filler = new javax.swing.JPanel();

        setLayout(new java.awt.GridBagLayout());

        dispNameLabel.setDisplayedMnemonic(org.openide.util.NbBundle.getMessage(JspPGPanel.class, "LBL_displayName_mnem").charAt(0));
        dispNameLabel.setLabelFor(dispNameTF);
        dispNameLabel.setText(org.openide.util.NbBundle.getMessage(JspPGPanel.class, "LBL_displayName"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 0, 10);
        add(dispNameLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        add(dispNameTF, gridBagConstraints);

        descriptionLabel.setDisplayedMnemonic(org.openide.util.NbBundle.getMessage(JspPGPanel.class, "LBL_description_mnem").charAt(0));
        descriptionLabel.setLabelFor(descriptionTA);
        descriptionLabel.setText(org.openide.util.NbBundle.getMessage(JspPGPanel.class, "LBL_description"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 0, 10);
        add(descriptionLabel, gridBagConstraints);

        descriptionTA.setRows(3);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        add(descriptionTA, gridBagConstraints);

        urlPatternsLabel.setDisplayedMnemonic(org.openide.util.NbBundle.getMessage(JspPGPanel.class, "LBL_urlPatterns_mnem").charAt(0));
        urlPatternsLabel.setLabelFor(urlPatternsTF);
        urlPatternsLabel.setText(org.openide.util.NbBundle.getMessage(JspPGPanel.class, "LBL_urlPatterns"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 0, 10);
        add(urlPatternsLabel, gridBagConstraints);

        urlPatternsTF.setColumns(40);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        add(urlPatternsTF, gridBagConstraints);

        browseButton1.setMnemonic(org.openide.util.NbBundle.getMessage(JspPGPanel.class, "LBL_browse_mnem").charAt(0));
        browseButton1.setText(org.openide.util.NbBundle.getMessage(JspPGPanel.class, "LBL_browse"));
        browseButton1.setMargin(new java.awt.Insets(0, 14, 0, 14));
        browseButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseButton1ActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 5, 0, 0);
        add(browseButton1, gridBagConstraints);

        hintUrlPatterns.setText(org.openide.util.NbBundle.getMessage(JspPGPanel.class, "HINT_urlPatterns"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(hintUrlPatterns, gridBagConstraints);

        pageEncodingLabel.setDisplayedMnemonic(org.openide.util.NbBundle.getMessage(JspPGPanel.class, "LBL_pageEncoding_mnem").charAt(0));
        pageEncodingLabel.setLabelFor(pageEncodingTF);
        pageEncodingLabel.setText(org.openide.util.NbBundle.getMessage(JspPGPanel.class, "LBL_pageEncoding"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 0, 10);
        add(pageEncodingLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        add(pageEncodingTF, gridBagConstraints);

        jCheckBox1.setMnemonic(org.openide.util.NbBundle.getMessage(JspPGPanel.class, "CHB_ignoreEL_mnem").charAt(0));
        jCheckBox1.setText(org.openide.util.NbBundle.getMessage(JspPGPanel.class, "CHB_ignoreEL"));
        jCheckBox1.setActionCommand("Expression Language Ignored");
        jCheckBox1.setOpaque(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        add(jCheckBox1, gridBagConstraints);

        jCheckBox2.setMnemonic(org.openide.util.NbBundle.getMessage(JspPGPanel.class, "CHB_ignoreScripting_mnem").charAt(0));
        jCheckBox2.setText(org.openide.util.NbBundle.getMessage(JspPGPanel.class, "CHB_ignoreScripting"));
        jCheckBox2.setOpaque(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        add(jCheckBox2, gridBagConstraints);

        jCheckBox3.setMnemonic(org.openide.util.NbBundle.getMessage(JspPGPanel.class, "CHB_xmlSyntax_mnem").charAt(0));
        jCheckBox3.setText(org.openide.util.NbBundle.getMessage(JspPGPanel.class, "CHB_xmlSyntax"));
        jCheckBox3.setOpaque(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        add(jCheckBox3, gridBagConstraints);

        preludeLabel.setDisplayedMnemonic(org.openide.util.NbBundle.getMessage(JspPGPanel.class, "LBL_includePrelude_mnem").charAt(0));
        preludeLabel.setLabelFor(preludeTF);
        preludeLabel.setText(org.openide.util.NbBundle.getMessage(JspPGPanel.class, "LBL_includePrelude"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 0, 10);
        add(preludeLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        add(preludeTF, gridBagConstraints);

        browseButton2.setMnemonic(org.openide.util.NbBundle.getMessage(JspPGPanel.class, "LBL_browse_mnem1").charAt(0));
        browseButton2.setText(org.openide.util.NbBundle.getMessage(JspPGPanel.class, "LBL_browse"));
        browseButton2.setMargin(new java.awt.Insets(0, 14, 0, 14));
        browseButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseButton2ActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 5, 0, 0);
        add(browseButton2, gridBagConstraints);

        codaLabel.setDisplayedMnemonic(org.openide.util.NbBundle.getMessage(JspPGPanel.class, "LBL_includeCoda_mnem").charAt(0));
        codaLabel.setLabelFor(codaTF);
        codaLabel.setText(org.openide.util.NbBundle.getMessage(JspPGPanel.class, "LBL_includeCoda"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 5, 10);
        add(codaLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 0);
        add(codaTF, gridBagConstraints);

        browseButton3.setMnemonic(org.openide.util.NbBundle.getMessage(JspPGPanel.class, "LBL_browse_mnem2").charAt(0));
        browseButton3.setText(org.openide.util.NbBundle.getMessage(JspPGPanel.class, "LBL_browse"));
        browseButton3.setMargin(new java.awt.Insets(0, 14, 0, 14));
        browseButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseButton3ActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 5, 5, 0);
        add(browseButton3, gridBagConstraints);

        filler.setOpaque(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        add(filler, gridBagConstraints);

    }
    // </editor-fold>//GEN-END:initComponents

    private void browseButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseButton3ActionPerformed
        // TODO add your handling code here:
        // TODO add your handling code here:
        try {
            SourceGroup[] groups = DDUtils.getDocBaseGroups(dObj);
            org.openide.filesystems.FileObject fo = BrowseFolders.showDialog(groups);
            if (fo!=null) {
                String fileName = "/"+DDUtils.getResourcePath(groups,fo,'/',true);
                String oldValue = codaTF.getText();
                if (fileName.length()>0) {
                    String newValue = DDUtils.addItem(oldValue,fileName,false);
                    if (!oldValue.equals(newValue)) {
                        dObj.modelUpdatedFromUI();
                        codaTF.setText(newValue);
                        setCodas(newValue);
                    }
                }
            }
        } catch (java.io.IOException ex) {}
    }//GEN-LAST:event_browseButton3ActionPerformed

    private void browseButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseButton2ActionPerformed
        // TODO add your handling code here:
        try {
            SourceGroup[] groups = DDUtils.getDocBaseGroups(dObj);
            org.openide.filesystems.FileObject fo = BrowseFolders.showDialog(groups);
            if (fo!=null) {
                String fileName = "/"+DDUtils.getResourcePath(groups,fo,'/',true);
                String oldValue = preludeTF.getText();
                if (fileName.length()>0) {
                    String newValue = DDUtils.addItem(oldValue,fileName,false);
                    if (!oldValue.equals(newValue)) {
                        dObj.modelUpdatedFromUI();
                        preludeTF.setText(newValue);
                        setPreludes(newValue);
                    }
                }
            }
        } catch (java.io.IOException ex) {}
    }//GEN-LAST:event_browseButton2ActionPerformed

    private void browseButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseButton1ActionPerformed
        // TODO add your handling code here:
        try {
            SourceGroup[] groups = DDUtils.getDocBaseGroups(dObj);
            org.openide.filesystems.FileObject fo = BrowseFolders.showDialog(groups);
            if (fo!=null) {
                String fileName = "/"+DDUtils.getResourcePath(groups,fo,'/',true);
                String oldValue = urlPatternsTF.getText();
                if (fileName.length()>0) {
                    String newValue = DDUtils.addItem(oldValue,fileName,false);
                    if (!oldValue.equals(newValue)) {
                        dObj.modelUpdatedFromUI();
                        urlPatternsTF.setText(newValue);
                        setUrlPatterns(newValue);
                        getSectionView().checkValidity();
                    }
                }
            }
        } catch (java.io.IOException ex) {}
    }//GEN-LAST:event_browseButton1ActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton browseButton1;
    private javax.swing.JButton browseButton2;
    private javax.swing.JButton browseButton3;
    private javax.swing.JLabel codaLabel;
    private javax.swing.JTextField codaTF;
    private javax.swing.JLabel descriptionLabel;
    private javax.swing.JTextArea descriptionTA;
    private javax.swing.JLabel dispNameLabel;
    private javax.swing.JTextField dispNameTF;
    private javax.swing.JPanel filler;
    private javax.swing.JLabel hintUrlPatterns;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JCheckBox jCheckBox2;
    private javax.swing.JCheckBox jCheckBox3;
    private javax.swing.JLabel pageEncodingLabel;
    private javax.swing.JTextField pageEncodingTF;
    private javax.swing.JLabel preludeLabel;
    private javax.swing.JTextField preludeTF;
    private javax.swing.JLabel urlPatternsLabel;
    private javax.swing.JTextField urlPatternsTF;
    // End of variables declaration//GEN-END:variables
    
    public void itemStateChanged(java.awt.event.ItemEvent evt) {                                            
        // TODO add your handling code here:
        dObj.modelUpdatedFromUI();
        if (evt.getSource() == jCheckBox1) {
            group.setElIgnored(jCheckBox1.isSelected());
        } else if (evt.getSource() == jCheckBox2) {
            group.setScriptingInvalid(jCheckBox2.isSelected());
        } else if (evt.getSource() == jCheckBox3) {
            group.setIsXml(jCheckBox3.isSelected());
        }
    }
    /** This will be called before model is changed from this panel
     */
    protected void signalUIChange() {
        dObj.modelUpdatedFromUI();
    }
}
