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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.BadLocationException;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.modules.css.editor.api.CssCslParserResult;
import org.netbeans.modules.css.live.LiveUpdater;
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
import org.netbeans.modules.web.common.api.WebUtils;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;

/**
 *
 * @author marekfukala
 */
@NbBundle.Messages({
    "none.item=<html><font color=\"777777\">&lt;none&gt;</font></html>",
    "class.selector.descr=Applies to all elements with this style class assigned.\n\nThe selector name starts with dot.",
    "id.selector.descr=Applies just to one single element with this id set.\n\nThe selector name starts with hash sign.",
    "element.selector.descr=Applies to html elements with the selector name.",
    "compound.selector.descr="
})
public class CreateRulePanel extends javax.swing.JPanel {

    private RuleEditorPanel ruleEditorPanel;
    private String selector;
    private String[] SELECTOR_TYPE_DESCRIPTIONS = new String[]{
        Bundle.class_selector_descr(),
        Bundle.id_selector_descr(),
        Bundle.element_selector_descr(),
        Bundle.compound_selector_descr()
    };

    /**
     * Creates new form AddRuleDialog
     */
    public CreateRulePanel(RuleEditorPanel ruleEditorPanel) {
        this.ruleEditorPanel = ruleEditorPanel;

        initComponents();

        selectorTypeList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                //update the description
                int index = selectorTypeList.getSelectedIndex();
                descriptionPane.setText(SELECTOR_TYPE_DESCRIPTIONS[index]);

                //disable editing mode for html elements
                selectorCB.setEditable(index != 2);

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

    /**
     * call outside of AWT thread, it does some I/Os
     */
    public void applyChanges() {
        if (selector == null) {
            //no value set
            return;
        }

        final Model model = ruleEditorPanel.getModel();
        //called if the dialog is confirmed
        model.runWriteTask(new Model.ModelTask() {
            @Override
            public void run(StyleSheet styleSheet) {

                ElementFactory factory = model.getElementFactory();
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
                    model.applyChanges();
                    selectTheRuleInEditorIfOpened(model, rule);
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
                    CssCslParserResult result = (CssCslParserResult) resultIterator.getParserResult();
                    final Model model = result.getModel();
                    model.runReadTask(new Model.ModelTask() {
                        @Override
                        public void run(StyleSheet styleSheet) {
                            ModelUtils utils = new ModelUtils(model);
                            Rule match = utils.findMatchingRule(omodel, orule);
                            if(match != null) {
                                ruleOffset.set(match.getStartOffset());
                            }
                        }
                    });
                }
            }
        });
        if(ruleOffset.get() == -1) {
            return ;
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

    private ComboBoxModel createStylesheetsModel() {
        return new DefaultComboBoxModel(new Object[]{getFile().getNameExt()});
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

    private ComboBoxModel createAtRulesModel() {
        final Model model = ruleEditorPanel.getModel();

        //adding just medias for now
        //TODO, add page, font-face, ...
        final List<MediaItem> medias = new ArrayList<MediaItem>();
        medias.add(null);

        model.runReadTask(new Model.ModelTask() {
            @Override
            public void run(StyleSheet styleSheet) {
                ModelVisitor visitor = new ModelVisitor.Adapter() {
                    @Override
                    public void visitMedia(Media media) {
                        String displayName = model.getElementSource(media.getMediaQueryList()).toString();
                        medias.add(new MediaItem(displayName, media));
                    }
                };
                styleSheet.accept(visitor);
            }
        });


        return new DefaultComboBoxModel(medias.toArray());
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

    private FileObject getFile() {
        Model cssModel = ruleEditorPanel.getModel();
        return cssModel.getLookup().lookup(FileObject.class);
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

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(CreateRulePanel.class, "CreateRulePanel.jLabel1.text")); // NOI18N

        jSplitPane1.setDividerLocation(140);
        jSplitPane1.setDividerSize(4);

        selectorTypeList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Class", "ID", "Element Type", "Compound" };
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

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(CreateRulePanel.class, "CreateRulePanel.jLabel2.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(CreateRulePanel.class, "CreateRulePanel.jLabel3.text")); // NOI18N

        styleSheetCB.setModel(createStylesheetsModel());
        styleSheetCB.setEnabled(false);

        atRuleCB.setModel(createAtRulesModel());
        atRuleCB.setRenderer(createAtRulesRenderer());

        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, org.openide.util.NbBundle.getMessage(CreateRulePanel.class, "CreateRulePanel.jLabel4.text")); // NOI18N

        selectorCB.setEditable(true);
        selectorCB.setModel(createSelectorModel());

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
                            .add(selectorCB, 0, 366, Short.MAX_VALUE))))
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
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox atRuleCB;
    private javax.swing.JTextPane descriptionPane;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JComboBox selectorCB;
    private javax.swing.JList selectorTypeList;
    private javax.swing.JComboBox styleSheetCB;
    // End of variables declaration//GEN-END:variables
}
