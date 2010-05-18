/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.worklist.editor.designview;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import org.netbeans.modules.wlm.model.api.Keyword;
import org.netbeans.modules.wlm.model.api.TKeywords;
import org.netbeans.modules.wlm.model.api.TTask;
import org.netbeans.modules.wlm.model.api.WLMComponent;
import org.netbeans.modules.wlm.model.api.WLMModel;
import org.netbeans.modules.worklist.editor.designview.components.ExUtils;
import org.netbeans.modules.worklist.editor.designview.components.LinkButton;
import org.netbeans.modules.worklist.editor.designview.components.TextFieldEditor;
import org.netbeans.modules.worklist.editor.designview.components.TitledPanel;
import org.netbeans.modules.worklist.editor.nodes.KeywordNode;
import org.netbeans.modules.worklist.editor.nodes.KeywordsNode;
import org.netbeans.modules.worklist.editor.nodes.WLMNodeType;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/**
 *
 * @author anjeleevich
 */
public class KeywordsPanel extends DesignViewPanel  implements Widget,
        FocusListener, ListSelectionListener
{
    private TitledPanel titledPanel;
    
    private Widget widgetParent;

    private AddKeywordAction addKeywordAction;
    private RemoveKeywordAction removeKeywordAction;

    private JButton addKeywordButton;
    private JButton removeKeywordButton;

    private KeywordsTableModel keywordsModel;
    private JTable keywordsTable;
    private JScrollPane keywordsScrollPane;

    private JComponent keywordsHeader;

    public KeywordsPanel(Widget widgetParent, DesignView designView) {
        super(designView);

        ExUtils.setA11Y(this, "KeywordsPanel"); // NOI18N

        setLayout(new BorderLayout());
        setBorder(null);
        setOpaque(false);

        this.widgetParent = widgetParent;

        keywordsModel = new KeywordsTableModel();
        keywordsTable = new JTable(keywordsModel);
        keywordsTable.setTableHeader(null);
        keywordsTable.setRowHeight(20);
        keywordsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        keywordsTable.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE); // NOI18N
        keywordsTable.getSelectionModel().addListSelectionListener(this);
        keywordsTable.addFocusListener(this);
        ExUtils.setA11Y(keywordsTable, KeywordsPanel.class,
                "KeywordsTable"); // NOI18N

        keywordsScrollPane = new JScrollPane(keywordsTable);
        keywordsScrollPane.getViewport().setBackground(Color.WHITE);
        ExUtils.setA11Y(keywordsScrollPane, KeywordsPanel.class,
                "KeywordsScrollPane"); // NOI18N

        addKeywordAction = new AddKeywordAction();
        removeKeywordAction = new RemoveKeywordAction();

        addKeywordButton = new LinkButton(addKeywordAction);
        addKeywordButton.addFocusListener(this);
        ExUtils.setA11Y(addKeywordButton, KeywordsPanel.class,
                "AddKeywordButton"); // NOI18N

        removeKeywordButton = new LinkButton(removeKeywordAction);
        removeKeywordButton.addFocusListener(this);
        ExUtils.setA11Y(removeKeywordButton, KeywordsPanel.class,
                "RemoveKeywordButton"); // NOI18N

        keywordsHeader = Box.createHorizontalBox();
        keywordsHeader.setOpaque(false);
        keywordsHeader.add(Box.createHorizontalGlue()).setFocusable(false);
        keywordsHeader.add(addKeywordButton);
        keywordsHeader.add(removeKeywordButton);
        ExUtils.setA11Y(keywordsHeader, KeywordsPanel.class,
                "KeywordsTableHeader"); // NOI18N

        add(keywordsHeader, BorderLayout.NORTH);
        add(keywordsScrollPane, BorderLayout.CENTER);

        processWLMModelChanged();
    }

    public JComponent getView() {
        if (titledPanel == null) {
            titledPanel = new TitledPanel(getMessage("LBL_KEYWORDS"), // NOI18N
                    this, 0);
        }
        return titledPanel;
    }

    public void processWLMModelChanged() {
        keywordsModel.processWLMModelChanged();
    }

    public Widget getWidget(int index) {
        return keywordsModel.get(index);
    }

    public int getWidgetCount() {
        return keywordsModel.getRowCount();
    }

    public Node getWidgetNode() {
        return new KeywordsNode(getTask(), Children.LEAF, getNodeLookup());
    }

    public void requestFocusToWidget() {
        getDesignView().showBasicPropertiesTab();
    }

    public Widget getWidgetParent() {
        return widgetParent;
    }

    public WLMComponent getWidgetWLMComponent() {
        return getTask();
    }

    public WLMNodeType getWidgetNodeType() {
        return WLMNodeType.KEYWORDS;
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension size = keywordsHeader.getPreferredSize();
        size.height += 70;
        return size;
    }

    @Override
    public Dimension getMaximumSize() {
        return getPreferredSize();
    }

    public void focusGained(FocusEvent e) {
        Object source = e.getSource();
        if (source == addKeywordButton
                || source == removeKeywordButton
                || source == removeKeywordButton
                || source == keywordsHeader
                || source == keywordsScrollPane)
        {
            selectWidget(this);
        } else if (source == keywordsTable) {
            int row = keywordsTable.getSelectedRow();
            if (row >= 0) {
                selectWidget(keywordsModel.get(row));
            } else {
                selectWidget(this);
            }
        }
    }

    public void focusLost(FocusEvent e) {
        // do nothing
    }

    public void valueChanged(ListSelectionEvent e) {
        if (keywordsTable.hasFocus() || keywordsTable.isEditing()) {
            int row = keywordsTable.getSelectedRow();
            if (row >= 0) {
                selectWidget(keywordsModel.get(row));
            } else {
                selectWidget(this);
            }
        }
    }

    private TKeywords getKeywords() {
        return getKeywords(false);
    }

    private TKeywords getKeywords(boolean create) {
        TTask task = getTask();
        TKeywords keywords = task.getKeywords();

        if ((keywords == null) && create) {
            WLMModel model = task.getModel();
            keywords = model.getFactory().createKeywords(model);
            task.setKeywords(keywords);
        }

        return keywords;
    }

    private class KeywordRow implements Widget {
        private Keyword keyword;
        private String content;
        private String expression = "";

        public KeywordRow(Keyword keyword) {
            this.keyword = keyword;
            update();
        }

        public Keyword getKeywordElement() {
            return keyword;
        }

        public String getExpression() {
            return expression;
        }

        public boolean update() {
            String oldContent = this.content;
            String newContent = keyword.getContent();

            boolean sameContent = (oldContent == null)
                    ? (newContent == null)
                    : oldContent.equals(newContent);

            if (sameContent) {
                return false;
            }

            this.content = newContent;
            this.expression = TextFieldEditor.xPathToText(newContent);

            return true;
        }

        public Widget getWidget(int index) {
            throw new IndexOutOfBoundsException("Leaf widget has no children");
        }

        public int getWidgetCount() {
            return 0;
        }

        public Node getWidgetNode() {
            return new KeywordNode(keyword, Children.LEAF, getNodeLookup());
        }

        public void requestFocusToWidget() {
            getDesignView().showBasicPropertiesTab();

            int index = keywordsModel.rows.indexOf(this);

            if (index >= 0) {
                keywordsTable.getSelectionModel().setSelectionInterval(index,
                        index);
            }

            keywordsTable.requestFocusInWindow();
        }

        public Widget getWidgetParent() {
            return KeywordsPanel.this;
        }

        public WLMComponent getWidgetWLMComponent() {
            return keyword;
        }

        public WLMNodeType getWidgetNodeType() {
            return WLMNodeType.KEYWORD;
        }
    }

    private class KeywordsTableModel extends AbstractTableModel {
        private List<KeywordRow> rows = new ArrayList<KeywordRow>();

        public int getRowCount() {
            return rows.size();
        }

        public int getColumnCount() {
            return 1;
        }

        public KeywordRow get(int index) {
            return rows.get(index);
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            return get(rowIndex).getExpression();
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            if (rowIndex < 0 || rowIndex >= getRowCount()) {
                return;
            }

            KeywordRow row = rows.get(rowIndex);
            Keyword keyword = row.getKeywordElement();

            String expression = TextFieldEditor.textToXPath(aValue.toString());

            WLMModel model = getModel();
            if (model.startTransaction()) {
                try {
                    keyword.setContent(expression);
                } finally {
                    model.endTransaction();
                }
            }
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return String.class;
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return true;
        }

        void processWLMModelChanged() {
            TTask task = getTask();

            TKeywords keywords = getKeywords();

            List<Keyword> keywordsList = (keywords == null) ? null :
                    keywords.getKeywords();

            if (keywordsList == null) {
                keywordsList = EMPTY_KEYWORD_LIST;
            }

            Set<Keyword> keywordsSet = new HashSet<Keyword>(keywordsList);

            for (int i = rows.size() - 1; i >= 0; i--) {
                KeywordRow keywordRow = rows.get(i);
                Keyword keyword = keywordRow.getKeywordElement();

                if (keywordsSet.remove(keyword)) {
                    if (keywordRow.update()) {
                        fireTableRowsUpdated(i, i);
                    }
                } else {
                    rows.remove(i);
                    fireTableRowsDeleted(i, i);
                }
            }

            int index = 0;
            for (Keyword keyword : keywordsList) {
                if (keywordsSet.remove(keyword)) {
                    rows.add(index, new KeywordRow(keyword));
                    fireTableRowsInserted(index, index);
                }
                index++;
            }
        }
    }

    private class AddKeywordAction extends AbstractAction {
        public AddKeywordAction() {
            super(getMessage("LBL_ADD_KEYWORD")); // NOI18N
        }

        public void actionPerformed(ActionEvent e) {
            WLMModel model = getModel();
            TTask task = getTask();

            if (model.startTransaction()) {
                try {
                    TKeywords keywords = getKeywords(true);

                    Keyword keyword = model.getFactory().createKeyword(model);
                    keyword.setContent("'NewKeyword'"); // NOI18N

                    keywords.addKeyword(keyword);
                } finally {
                    model.endTransaction();
                }
            }
        }
    }

    private class RemoveKeywordAction extends AbstractAction {
        RemoveKeywordAction() {
            super(getMessage("LBL_REMOVE_KEYWORD")); // NOI18N
        }

        public void actionPerformed(ActionEvent event) {
            int rowIndex = keywordsTable.getSelectedRow();
            if (rowIndex < 0 || rowIndex >= keywordsModel.getRowCount()) {
                return;
            }

            KeywordRow row = keywordsModel.get(rowIndex);

            Keyword keyword = row.getKeywordElement();

            WLMModel model = getModel();
            TKeywords keywords = getKeywords();

            if (model.startTransaction()) {
                try {
                    if (keywords != null) {
                        keywords.removeKeyword(keyword);
                    }
                } finally {
                    model.endTransaction();
                }
            }
        }
    }

    private static String getMessage(String key) {
        return NbBundle.getMessage(KeywordsPanel.class, key);
    }

    private static final List<Keyword> EMPTY_KEYWORD_LIST
            = new AbstractList<Keyword>()
    {
        @Override
        public Keyword get(int index) {
            throw new IndexOutOfBoundsException("List is empty");
        }

        @Override
        public int size() {
            return 0;
        }
    };
}
