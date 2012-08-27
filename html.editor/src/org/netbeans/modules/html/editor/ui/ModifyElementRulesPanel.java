/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.html.editor.ui;

import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.css.indexing.api.CssIndex;
import org.netbeans.modules.html.editor.api.actions.AbstractSourceElementAction;
import org.netbeans.modules.html.editor.lib.api.HtmlVersion;
import org.netbeans.modules.html.editor.lib.api.elements.Attribute;
import org.netbeans.modules.html.editor.lib.api.elements.OpenTag;
import org.netbeans.modules.html.editor.lib.api.model.HtmlModel;
import org.netbeans.modules.html.editor.lib.api.model.HtmlModelFactory;
import org.netbeans.modules.html.editor.lib.api.model.HtmlTag;
import org.netbeans.modules.web.common.api.DependenciesGraph;
import org.netbeans.modules.web.common.spi.ProjectWebRootQuery;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author marekfukala
 */
@NbBundle.Messages({
    "none.item=<font color=\"777777\">&lt;none&gt;</font>",
    "class.selector.descr=Applies to all elements with this style class assigned.\n\nThe selector name starts with dot.",
    "id.selector.descr=Applies just to one single element with this id set.\n\nThe selector name starts with hash sign."
})
public class ModifyElementRulesPanel extends javax.swing.JPanel {

    private AbstractSourceElementAction.SourceElementHandle sourceHandle;
    private int state = 0; //0 == class, 1 == id
    //index data
    private Collection<FileObject> linkedStyleSheets;
    private Map<FileObject, Collection<String>> files2classes;
    private Map<FileObject, Collection<String>> files2ids;
    private Map<String, Collection<FileObject>> classes2files;
    private Map<String, Collection<FileObject>> ids2files;
    //UI
    private String[] SELECTOR_TYPE_DESCRIPTIONS = new String[]{
        Bundle.class_selector_descr(),
        Bundle.id_selector_descr(),};
    //already set attributes
    private Attribute clz, id;
    //and their names
    private String clzName, idName;
    private String originalClzName, originalIdName;

    /**
     * Creates new form AddRuleDialog
     */
    public ModifyElementRulesPanel(AbstractSourceElementAction.SourceElementHandle sourceHandle) {
        this.sourceHandle = sourceHandle;

        //set the default state of combos according to the already set attributes
        OpenTag openTag = sourceHandle.getOpenTag();
        clz = openTag.getAttribute("class");
        id = openTag.getAttribute("id");

        if (clz != null) {
            if (clz.unquotedValue() != null) {
                originalClzName = clzName = clz.unquotedValue().toString();
            }
        }
        if (id != null) {
            if (id.unquotedValue() != null) {
                originalIdName = idName = id.unquotedValue().toString();
            }
        }

        loadIndexData();

        //UI
        initComponents();

        //behavior
        selectorTypeList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                //update the description
                int index = selectorTypeList.getSelectedIndex();
                descriptionPane.setText(SELECTOR_TYPE_DESCRIPTIONS[index]);
                state = index;
                stateChanged();
            }
        });
        selectorTypeList.setSelectedIndex(0);

        styleSheetCB.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                updateSelectorComboBoxModel(false);
            }
        });

        updateResultCodeSample();
    }
    
    public Attribute getOriginalClassAttribute() {
        return clz;
    }
    
    public Attribute getOriginalIdAttribute() {
        return id;
    }
    
    public String getNewClassAttributeValue() {
        return clzName;
    }
    
    public String getNewIdAttributeValue() {
        return idName;
    }

    private void updateResultCodeSample() {
        StringBuilder sb = new StringBuilder();
        sb.append("<html>");
        sb.append("&lt;");
        sb.append(sourceHandle.getOpenTag().name());

        //rewrite that please!!!!!!!! :-)

        if (originalClzName != null && clzName != null && !originalClzName.equals(clzName)) {
            //changed
            sb.append(' ');
            sb.append("<b>");
            sb.append("class=");
            sb.append(clzName);
            sb.append("</b>");
        } else if (originalClzName == null && clzName != null) {
            //added
            sb.append(' ');
            sb.append("<b>");
            sb.append("class=");
            sb.append(clzName);
            sb.append("</b>");
        } else if (originalClzName != null && clzName == null) {
            //removed
            sb.append(' ');
            sb.append("<s>");
            sb.append("class=");
            sb.append(originalClzName);
            sb.append("</s>");
        } else {
            //no change
            if (clzName != null) {
                sb.append(' ');
                sb.append("class=");
                sb.append(originalClzName);
            }
        }

        if (originalIdName != null && idName != null && !originalIdName.equals(idName)) {
            //changed
            sb.append(' ');
            sb.append("<b>");
            sb.append("id=");
            sb.append(idName);
            sb.append("</b>");
        } else if (originalIdName == null && idName != null) {
            //added
            sb.append(' ');
            sb.append("<b>");
            sb.append("id=");
            sb.append(idName);
            sb.append("</b>");
        } else if (originalIdName != null && idName == null) {
            //removed
            sb.append(' ');
            sb.append("<s>");
            sb.append("id=");
            sb.append(originalIdName);
            sb.append("</s>");
        } else {
            //no change
            if (idName != null) {
                sb.append(' ');
                sb.append("id=");
                sb.append(originalIdName);
            }
        }

        sb.append("&gt;");
        sb.append("</html>");

        resultCodeLabel.setText(sb.toString());
    }

    private void stateChanged() {
        updateSelectorComboBoxModel(true);
    }

    private void updateSelectorComboBoxModel(boolean doB) {
        //A.pre set the selector CB to the already set element name
        //B. pre set the appropriate stylesheet according to whether it contains the already set element
        switch (state) {
            case 0:
                selectorCB.setModel(createClassesModel());
                //A.
                selectorCB.setSelectedItem(clzName);
                //B.
                if (doB) {
                    if (clzName != null) {
                        Collection<FileObject> infiles = classes2files.get(clzName);
                        if (infiles == null) {
                            break;
                        }
                        Collection<FileObject> inlinkedfiles = new ArrayList<FileObject>(infiles);
                        inlinkedfiles.retainAll(linkedStyleSheets);

                        //just take first
                        if (inlinkedfiles.size() > 0) {
                            FileObject ss = inlinkedfiles.iterator().next();
                            styleSheetCB.setSelectedItem(ss);
                        }
                    }
                }
                break;
            case 1:
                selectorCB.setModel(createIdsModel());
                //A.
                selectorCB.setSelectedItem(idName);
                //B.
                if (doB) {
                    if (idName != null) {
                        Collection<FileObject> infiles = ids2files.get(clzName);
                        if (infiles == null) {
                            break;
                        }
                        Collection<FileObject> inlinkedfiles = new ArrayList<FileObject>(infiles);
                        inlinkedfiles.retainAll(linkedStyleSheets);

                        //just take first
                        if (inlinkedfiles.size() > 0) {
                            FileObject ss = inlinkedfiles.iterator().next();
                            styleSheetCB.setSelectedItem(ss);
                        }
                    }
                }
                break;
        }
    }

    private void loadIndexData() {
        try {
            FileObject file = sourceHandle.getFile();
            Project project = FileOwnerQuery.getOwner(file);

            if (project == null) {
                return;
            }

            CssIndex index = CssIndex.create(project);
            DependenciesGraph deps = index.getDependencies(file);

            //all files refered from this file
            Collection<FileObject> refered = deps.getAllReferedFiles();

            linkedStyleSheets = new ArrayList<FileObject>();
            for (FileObject ref : refered) {
                if ("text/css".equals(ref.getMIMEType())) {
                    linkedStyleSheets.add(ref);
                }
            }

            //file->element maps
            files2classes = index.findAllClassDeclarations();
            files2ids = index.findAllIdDeclarations();

            //element->file maps
            classes2files = createReversedMap(files2classes);
            ids2files = createReversedMap(files2ids);

        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

    }

    private static Map<String, Collection<FileObject>> createReversedMap(Map<FileObject, Collection<String>> file2elements) {
        Map<String, Collection<FileObject>> map = new HashMap<String, Collection<FileObject>>();
        for (FileObject file : file2elements.keySet()) {
            for (String element : file2elements.get(file)) {
                Collection<FileObject> files = map.get(element);
                if (files == null) {
                    files = new HashSet<FileObject>();
                }
                files.add(file);
                map.put(element, files);
            }
        }
        return map;
    }

    private ComboBoxModel createClassesModel() {
        Collection<String> classes = new ArrayList<String>();
        classes.add(null);
        FileObject selectedStyleSheet = (FileObject) styleSheetCB.getSelectedItem();
        if (selectedStyleSheet != null) {
            Collection<String> foundInFile = files2classes.get(selectedStyleSheet);
            if (foundInFile != null) {
                classes.addAll(foundInFile);
            }
        }
        return new DefaultComboBoxModel(classes.toArray());
    }

    private ComboBoxModel createIdsModel() {
        Collection<String> ids = new ArrayList<String>();
        ids.add(null);
        FileObject selectedStyleSheet = (FileObject) styleSheetCB.getSelectedItem();
        if (selectedStyleSheet != null) {
            Collection<String> foundInFile = files2ids.get(selectedStyleSheet);
            if (foundInFile != null) {
                ids.addAll(foundInFile);
            }
        }
        return new DefaultComboBoxModel(ids.toArray());
    }

    private ComboBoxModel createStylesheetsModel() {
        return new DefaultComboBoxModel(linkedStyleSheets.toArray());
    }

    private ListCellRenderer createStyleSheetsRenderer() {
        return new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value == null) {
                    setText("<html>"+Bundle.none_item());
                } else {
                    FileObject file = (FileObject) value;
                    FileObject webRoot = ProjectWebRootQuery.getWebRoot(file);

                    String file2string;
                    if (webRoot != null) {
                        file2string = FileUtil.getRelativePath(webRoot, file);
                    } else {
                        file2string = FileUtil.getFileDisplayName(file);
                    }

                    setText(file2string);
                }
                return c;
            }
        };
    }

    private ListCellRenderer createSelectorCellRenderer() {
        return new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

                boolean bold = false;
                String strval = (String) value;
                
                //???
                if(strval != null && strval.trim().isEmpty()) {
                    return c;
                }
                
                switch (state) {
                    case 0:
                        //class
                        if ((strval == null && clzName == null) || (strval != null && strval.equals(clzName))) {
                            bold = true;
                        }
                        break;
                    case 1:
                        //id
                        if ((strval == null && idName == null) || (strval != null && strval.equals(idName))) {
                            bold = true;
                        }
                        break;
                }

                StringBuilder sb = new StringBuilder();
                sb.append("<html>");
                if (bold) {
                    sb.append("<b>");
                }
                sb.append(strval == null ? Bundle.none_item() : strval);
                if (bold) {
                    sb.append("</b>");
                }

                sb.append("</html>");
                
                setText(sb.toString());
                
                return c;
            }
        };
    }

    private ComboBoxModel createSelectorModel() {
        HtmlModel model = HtmlModelFactory.getModel(HtmlVersion.HTML5);
        Collection<String> tagNames = new ArrayList<String>();
        tagNames.add(null);
        for (HtmlTag tag : model.getAllTags()) {
            tagNames.add(tag.getName());
        }
        return new DefaultComboBoxModel(tagNames.toArray());

    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jSplitPane1 = new javax.swing.JSplitPane();
        jScrollPane1 = new javax.swing.JScrollPane();
        selectorTypeList = new javax.swing.JList();
        jScrollPane2 = new javax.swing.JScrollPane();
        descriptionPane = new javax.swing.JTextPane();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        styleSheetCB = new javax.swing.JComboBox();
        atRuleCB = new javax.swing.JComboBox();
        jLabel4 = new javax.swing.JLabel();
        selectorCB = new javax.swing.JComboBox();
        jLabel5 = new javax.swing.JLabel();
        resultCodeLabel = new javax.swing.JLabel();

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(ModifyElementRulesPanel.class, "ModifyElementRulesPanel.jLabel1.text")); // NOI18N

        jSplitPane1.setDividerLocation(120);
        jSplitPane1.setDividerSize(4);

        selectorTypeList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Class", "ID" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        selectorTypeList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane1.setViewportView(selectorTypeList);

        jSplitPane1.setLeftComponent(jScrollPane1);

        descriptionPane.setEditable(false);
        descriptionPane.setEnabled(false);
        jScrollPane2.setViewportView(descriptionPane);

        jSplitPane1.setRightComponent(jScrollPane2);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(ModifyElementRulesPanel.class, "ModifyElementRulesPanel.jLabel2.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(ModifyElementRulesPanel.class, "ModifyElementRulesPanel.jLabel3.text")); // NOI18N

        styleSheetCB.setModel(createStylesheetsModel());
        styleSheetCB.setRenderer(createStyleSheetsRenderer());

        atRuleCB.setEnabled(false);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, org.openide.util.NbBundle.getMessage(ModifyElementRulesPanel.class, "ModifyElementRulesPanel.jLabel4.text")); // NOI18N

        selectorCB.setModel(createSelectorModel());
        selectorCB.setRenderer(createSelectorCellRenderer());
        selectorCB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectorCBActionPerformed(evt);
            }
        });

        jLabel5.setLabelFor(resultCodeLabel);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel5, org.openide.util.NbBundle.getMessage(ModifyElementRulesPanel.class, "ModifyElementRulesPanel.jLabel5.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(resultCodeLabel, org.openide.util.NbBundle.getMessage(ModifyElementRulesPanel.class, "ModifyElementRulesPanel.resultCodeLabel.text")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jSplitPane1)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jLabel1)
                            .add(jLabel4))
                        .add(0, 0, Short.MAX_VALUE))
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jLabel3)
                            .add(jLabel2)
                            .add(jLabel5))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .add(resultCodeLabel)
                                .add(0, 0, Short.MAX_VALUE))
                            .add(selectorCB, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(styleSheetCB, 0, 408, Short.MAX_VALUE)
                            .add(atRuleCB, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jSplitPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 87, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel4)
                    .add(selectorCB, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(styleSheetCB, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel2))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(atRuleCB, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel3))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel5)
                    .add(resultCodeLabel))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void selectorCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectorCBActionPerformed
        //user changed the value
        switch (state) {
            case 0:
                clzName = (String) selectorCB.getSelectedItem();
                break;
            case 1:
                idName = (String) selectorCB.getSelectedItem();
                break;
        }

        updateResultCodeSample();

    }//GEN-LAST:event_selectorCBActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox atRuleCB;
    private javax.swing.JTextPane descriptionPane;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JLabel resultCodeLabel;
    private javax.swing.JComboBox selectorCB;
    private javax.swing.JList selectorTypeList;
    private javax.swing.JComboBox styleSheetCB;
    // End of variables declaration//GEN-END:variables
}
