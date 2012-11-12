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
package org.netbeans.modules.css.visual;

import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JEditorPane;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.css.indexing.api.CssIndex;
import org.netbeans.modules.css.lib.api.CssParserResult;
import org.netbeans.modules.css.model.api.Body;
import org.netbeans.modules.css.model.api.Declarations;
import org.netbeans.modules.css.model.api.ElementFactory;
import org.netbeans.modules.css.model.api.Media;
import org.netbeans.modules.css.model.api.Model;
import org.netbeans.modules.css.model.api.ModelUtils;
import org.netbeans.modules.css.model.api.ModelVisitor;
import org.netbeans.modules.css.model.api.Rule;
import org.netbeans.modules.css.model.api.Selector;
import org.netbeans.modules.css.model.api.SelectorsGroup;
import org.netbeans.modules.css.model.api.StyleSheet;
import org.netbeans.modules.html.editor.lib.api.HtmlVersion;
import org.netbeans.modules.html.editor.lib.api.model.HtmlModel;
import org.netbeans.modules.html.editor.lib.api.model.HtmlModelFactory;
import org.netbeans.modules.html.editor.lib.api.model.HtmlTag;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.web.common.api.DependenciesGraph;
import org.netbeans.modules.web.common.api.WebUtils;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;

/**
 *
 * @author marekfukala
 */
@NbBundle.Messages({
    "selector.type.class=Class",
    "selector.type.id=Id",
    "selector.type.element=Element",
    "selector.type.compound=Compound",
    "selector.rule.postfix= Rule",
    "none.item=<html><font color=\"777777\">&lt;none&gt;</font></html>",
    "class.selector.descr=Applies to all elements with this style class assigned.\n\nThe selector name starts with dot.",
    "id.selector.descr=Applies just to one single element with this id set.\n\nThe selector name starts with hash sign.",
    "element.selector.descr=Applies to html elements with the selector name.",
    "compound.selector.descr=The Compound selector is used to create styles for a combination of tags or tags that are nested in other tags."
})
public class CreateRulePanel extends javax.swing.JPanel {

    private String selector;
    private String[] SELECTOR_TYPE_DESCRIPTIONS = new String[]{
        Bundle.class_selector_descr(),
        Bundle.id_selector_descr(),
        Bundle.element_selector_descr(),
        Bundle.compound_selector_descr()
    };
    /**
     * Models for stylesheets and at rules comboboxes.
     */
    private DefaultComboBoxModel STYLESHEETS_MODEL, AT_RULES_MODEL, SELECTORS_MODEL;
    private ListModel SELECTORS_LIST_MODEL;
    /**
     * Context of the create rule panel.
     */
    private FileObject context;
    /**
     * Css source {@link Model} for the selected stylesheet.
     */
    private Model selectedStyleSheetModel;

    private Collection<String> TAG_NAMES;
    
    public CreateRulePanel() {
        SELECTORS_LIST_MODEL = new AbstractListModel() {
            @Override
            public int getSize() {
                return 4;
            }

            @Override
            public Object getElementAt(int i) {
                switch(i) {
                    case 0:
                        return Bundle.selector_type_class();
                    case 1:
                        return Bundle.selector_type_id();
                    case 2:
                        return Bundle.selector_type_element();
                    case 3:
                        return Bundle.selector_type_compound();
                    default:
                        throw new IllegalStateException();
                }
            }
        };
        
        STYLESHEETS_MODEL = new DefaultComboBoxModel();
        AT_RULES_MODEL = new DefaultComboBoxModel();
        SELECTORS_MODEL = new DefaultComboBoxModel();

        initComponents();

        selectorTypeList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                //update the description
                int index = selectorTypeList.getSelectedIndex();
                descriptionPane.setText(SELECTOR_TYPE_DESCRIPTIONS[index]);

                //disable editing mode for html elements
                selectorCB.setEditable(index != 2);
                
                //update the separator's title
                selectorTypeLabel.setText(selectorTypeList.getSelectedValue().toString() + Bundle.selector_rule_postfix());
                
                updateSelectorsModel();

            }
        });
        selectorTypeList.setSelectedIndex(0);

        selectorCB.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                String item = e.getItem().toString();
                if (item.isEmpty()) {
                    return;
                }
                switch (selectorTypeList.getSelectedIndex()) {
                    case 0:
                        //class
                        if (item.charAt(0) != '.') {
                            item = '.' + item;
                        }
                        break;
                    case 1:
                        //id
                        if (item.charAt(0) != '#') {
                            item = '#' + item;
                        }
                        break;
                    case 2:
                        //element
                        break;
                    case 3:
                        //compound
                        break;
                    default:
                        throw new IllegalStateException();
                }
                selectorSet(item);

            }
        });

    }

    public void setContext(FileObject context) {
        this.context = context;
        updateModels();
    }

    private void updateModels() {
        //update selectors model
        updateSelectorsModel();

        //update stylesheets combobox model
        updateStyleSheetsModel();

        //create css model for the selected stylesheet
        updateCssModel(context);

        if (selectedStyleSheetModel == null) {
            //no css code to perform on
            return;
        }

        //update at rules model
        updateAtRulesModel();
    }

    private void updateStyleSheetsModel() {
        try {
            STYLESHEETS_MODEL.removeAllElements();
            Project project = FileOwnerQuery.getOwner(context);
            if (project == null) {
                return;
            }
            CssIndex index = CssIndex.create(project);
            for (FileObject file : index.getAllIndexedFiles()) {
                if ("text/css".equals(file.getMIMEType())) {
                    STYLESHEETS_MODEL.addElement(file);
                }
            }

            if (STYLESHEETS_MODEL.getIndexOf(context) != -1) {
                //the context may be the html file itself
                STYLESHEETS_MODEL.setSelectedItem(context);
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

    }

    private void updateCssModel(FileObject file) {
        try {
            Source source = Source.create(file);
            ParserManager.parse(Collections.singleton(source), new UserTask() {
                @Override
                public void run(ResultIterator resultIterator) throws Exception {
                    resultIterator = WebUtils.getResultIterator(resultIterator, "text/css");
                    if (resultIterator != null) {
                        CssParserResult result = (CssParserResult) resultIterator.getParserResult();
                        selectedStyleSheetModel = Model.getModel(result);
                    }
                }
            });
        } catch (ParseException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private void updateAtRulesModel() {
        AT_RULES_MODEL.removeAllElements();

        AT_RULES_MODEL.addElement(null);
        selectedStyleSheetModel.runReadTask(new Model.ModelTask() {
            @Override
            public void run(StyleSheet styleSheet) {
                ModelVisitor visitor = new ModelVisitor.Adapter() {
                    @Override
                    public void visitMedia(Media media) {
                        String displayName = selectedStyleSheetModel.getElementSource(media.getMediaQueryList()).toString();
                        AT_RULES_MODEL.addElement(new MediaItem(displayName, media));
                    }
                };
                styleSheet.accept(visitor);
            }
        });

        atRuleCB.setEnabled(AT_RULES_MODEL.getSize() > 1);
    }

    /**
     * call outside of AWT thread, it does some I/Os
     */
    public void applyChanges() {
        if (selector == null) {
            //no value set
            return;
        }

        //called if the dialog is confirmed
        selectedStyleSheetModel.runWriteTask(new Model.ModelTask() {
            @Override
            public void run(StyleSheet styleSheet) {

                ElementFactory factory = selectedStyleSheetModel.getElementFactory();
                Selector s = factory.createSelector(selector);
                SelectorsGroup sg = factory.createSelectorsGroup(s);
                Declarations ds = factory.createDeclarations();
                Rule rule = factory.createRule(sg, ds);

                Media media = getSelectedMedia();
                if (media == null) {
                    //add to the body
                    Body body = styleSheet.getBody();
                    if (body == null) {
                        //create body if empty file
                        body = factory.createBody();
                        styleSheet.setBody(body);
                    }
                    styleSheet.getBody().addRule(rule);
                } else {
                    //add to the media
                    media.addRule(rule);
                }

                try {
                    selectedStyleSheetModel.applyChanges();
                    selectTheRuleInEditorIfOpened(selectedStyleSheetModel, rule);
                } catch (Exception /*ParseException, IOException, BadLocationException*/ ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        });
    }

    private void selectTheRuleInEditorIfOpened(final Model omodel, final Rule orule) throws DataObjectNotFoundException, ParseException {
        FileObject file = omodel.getLookup().lookup(FileObject.class);
        DataObject dobj = DataObject.find(file);
        final EditorCookie ec = dobj.getLookup().lookup(EditorCookie.class);
        //first get instance of the new model so we can resolve the element's positions
        final AtomicInteger ruleOffset = new AtomicInteger(-1);
        Source source = Source.create(file);
        ParserManager.parse(Collections.singleton(source), new UserTask() {
            @Override
            public void run(ResultIterator resultIterator) throws Exception {
                resultIterator = WebUtils.getResultIterator(resultIterator, "text/css");
                if (resultIterator != null) {
                    CssParserResult result = (CssParserResult) resultIterator.getParserResult();
                    final Model model = Model.getModel(result);
                    model.runReadTask(new Model.ModelTask() {
                        @Override
                        public void run(StyleSheet styleSheet) {
                            ModelUtils utils = new ModelUtils(model);
                            Rule match = utils.findMatchingRule(omodel, orule);
                            if (match != null) {
                                ruleOffset.set(match.getStartOffset());
                            }
                        }
                    });
                }
            }
        });
        if (ruleOffset.get() == -1) {
            return;
        }
        Mutex.EVENT.readAccess(new Runnable() {
            @Override
            public void run() {
                JEditorPane[] openedPanes = ec.getOpenedPanes();
                if (openedPanes != null && openedPanes.length > 0) {
                    JEditorPane pane = openedPanes[0];
                    pane.setCaretPosition(ruleOffset.get());
                }
            }
        });

    }

    private void selectorSet(String selector) {
        this.selector = selector;
    }

    private Media getSelectedMedia() {
        Object selected = atRuleCB.getSelectedItem();
        if (selected == null) {
            return null;
        }
        return ((MediaItem) selected).getMedia();
    }

    private void updateSelectorsModel() {
        SELECTORS_MODEL.removeAllElements();
        switch (selectorTypeList.getSelectedIndex()) {
            case 0:
                //class
                //TODO possibly show the current html elements' class
                break;
            case 1:
                //id
                //TODO possibly show the current html elements' id
                break;
            case 2:
                //element
                for(String tag : getTagNames()) {
                    SELECTORS_MODEL.addElement(tag);
                }
                break;
            case 3:
                //compound
                //TODO possibly show the current html elements' elements path
                break;
        }

    }
    
    private Collection<String> getTagNames() {
        if (TAG_NAMES == null) {
            TAG_NAMES = new TreeSet<String>();
            HtmlModel model = HtmlModelFactory.getModel(HtmlVersion.HTML5);
            for (HtmlTag tag : model.getAllTags()) {
                TAG_NAMES.add(tag.getName());
            }
        }
        return TAG_NAMES;
    }

    private ListCellRenderer createAtRulesRenderer() {
        return new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value == null) {
                    setText(Bundle.none_item());
                } else {
                    setText(((MediaItem) value).getDisplayName());
                }
                return c;
            }
        };
    }

    private ListCellRenderer createStylesheetsRenderer() {
        return new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                FileObject file = (FileObject) value;
                String fileNameExt = file.getNameExt();
                setText(fileNameExt);

//                if(file.equals(context)) {
//                    StringBuilder sb = new StringBuilder();
//                    sb.append("<html><body><b>"); //NOI18N
//                    sb.append(fileNameExt);
//                    sb.append("</b></body></html>"); //NOI18N
//                    setText(sb.toString());
//                } else {
//                    setText(fileNameExt);
//                }
                return c;
            }
        };
    }

    private static class MediaItem {

        private Media media;
        private String displayName;

        public MediaItem(String displayName, Media media) {
            this.displayName = displayName;
            this.media = media;
        }

        public Media getMedia() {
            return media;
        }

        public String getDisplayName() {
            return displayName;
        }
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
        jSeparator1 = new javax.swing.JSeparator();
        selectorTypeLabel = new javax.swing.JLabel();

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(CreateRulePanel.class, "CreateRulePanel.jLabel1.text")); // NOI18N

        jSplitPane1.setDividerLocation(140);
        jSplitPane1.setDividerSize(4);

        selectorTypeList.setModel(SELECTORS_LIST_MODEL);
        selectorTypeList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane1.setViewportView(selectorTypeList);

        jSplitPane1.setLeftComponent(jScrollPane1);

        descriptionPane.setEditable(false);
        descriptionPane.setEnabled(false);
        jScrollPane2.setViewportView(descriptionPane);

        jSplitPane1.setRightComponent(jScrollPane2);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(CreateRulePanel.class, "CreateRulePanel.jLabel2.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(CreateRulePanel.class, "CreateRulePanel.jLabel3.text")); // NOI18N

        styleSheetCB.setModel(STYLESHEETS_MODEL);
        styleSheetCB.setRenderer(createStylesheetsRenderer());
        styleSheetCB.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                styleSheetCBItemStateChanged(evt);
            }
        });

        atRuleCB.setModel(AT_RULES_MODEL);
        atRuleCB.setRenderer(createAtRulesRenderer());

        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, org.openide.util.NbBundle.getMessage(CreateRulePanel.class, "CreateRulePanel.jLabel4.text")); // NOI18N

        selectorCB.setEditable(true);
        selectorCB.setModel(SELECTORS_MODEL);

        org.openide.awt.Mnemonics.setLocalizedText(selectorTypeLabel, null);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jSplitPane1)
                    .add(layout.createSequentialGroup()
                        .add(jLabel1)
                        .add(0, 0, Short.MAX_VALUE))
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jLabel2)
                            .add(jLabel3)
                            .add(jLabel4))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(styleSheetCB, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(atRuleCB, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(selectorCB, 0, 427, Short.MAX_VALUE)))
                    .add(layout.createSequentialGroup()
                        .add(selectorTypeLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jSeparator1)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jSplitPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 88, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(jSeparator1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 10, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(selectorTypeLabel))
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
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void styleSheetCBItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_styleSheetCBItemStateChanged
        FileObject file = (FileObject) STYLESHEETS_MODEL.getSelectedItem();
        //create css model for the selected stylesheet
        updateCssModel(file);
        //update at rules model
        updateAtRulesModel();
    }//GEN-LAST:event_styleSheetCBItemStateChanged
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox atRuleCB;
    private javax.swing.JTextPane descriptionPane;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JComboBox selectorCB;
    private javax.swing.JLabel selectorTypeLabel;
    private javax.swing.JList selectorTypeList;
    private javax.swing.JComboBox styleSheetCB;
    // End of variables declaration//GEN-END:variables
}
