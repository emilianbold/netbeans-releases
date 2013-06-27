/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.odcs.tasks.query;

import com.tasktop.c2c.server.common.service.domain.criteria.ColumnCriteria;
import com.tasktop.c2c.server.common.service.domain.criteria.Criteria;
import com.tasktop.c2c.server.common.service.domain.criteria.Criteria.Operator;
import com.tasktop.c2c.server.common.service.domain.criteria.NaryCriteria;
import com.tasktop.c2c.server.tasks.domain.AbstractReferenceValue;
import com.tasktop.c2c.server.tasks.domain.Keyword;
import com.tasktop.c2c.server.tasks.domain.Product;
import com.tasktop.c2c.server.tasks.domain.TaskUserProfile;
import java.awt.Component;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JTextField;
import javax.swing.ListModel;
import org.netbeans.modules.odcs.tasks.ODCS;
import org.netbeans.modules.odcs.tasks.query.Bundle;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomas Stupka
 */
public class QueryParameters {

    @NbBundle.Messages({"LBL_Created=Created", "LBL_Updated=Updated"})
    public enum Column {
        ASSIGNEE("assignee"), // NOI18N
        COMMENT("comment"), // NOI18N 
        COMMENTER("commentAuthor"), // NOI18N
        COMPONENT("componentName"), // NOI18N
        CREATOR("reporter"), // NOI18N
        CREATION("creationDate", Bundle.LBL_Created()), // NOI18N
        DESCRIPTION("description"), // NOI18N
        ITERATION("iteration"), // NOI18N
        KEYWORDS("keywords"), // NOI18N
        MODIFICATION("modificationDate", Bundle.LBL_Updated()), // NOI18N
        PRODUCT("productName"), // NOI18N
        RELEASE("release"), // NOI18N
        RESOLUTION("resolution"), // NOI18N
        SEVERITY("severity"), // NOI18N
        STATUS("status"), // NOI18N
        SUMMARY("summary"), // NOI18N
        PRIORITY("priority"), // NOI18N
        TASK_TYPE("tasktype"), // NOI18N
        WATCHER("watcher"); // NOI18N
            
        
        private String columnName;
        private String displayName;
        
        Column(String columnName) {
            this.columnName = columnName;
        }
        
        Column(String columnName, String displayName) {
            this(columnName);
            this.displayName = displayName;
        }

        @Override
        public String toString() {
            return columnName;
        }
        
        public String getColumnName() {
            return columnName;
        }
        
        String getDisplayName() {
            return displayName != null ? displayName : columnName;
        }
        
        static Column forColumnName(String name) {
            for (Column c : Column.values()) {
                if(c.getColumnName().equals(name)) {
                    return c;
                }
            }
            return null;
        }
    }
    
    private final EnumMap<Column, Parameter> map = new EnumMap<Column, Parameter>(Column.class);

    QueryParameters() { }

    TextFieldParameter getTextFieldParameter(Column c) {
        return (TextFieldParameter) map.get(c);
    }
    
    ComboParameter getComboParameter(Column c) {
        return (ComboParameter) map.get(c);
    }
    
    ListParameter getListParameter(Column c) {
        return (ListParameter) map.get(c);
    }
    
    ByPeopleParameter getByPeopleParameter() {
        return (ByPeopleParameter) map.get(Column.CREATOR);
    }
    
    ByDateParameter getByDateParameter() {
        return (ByDateParameter) map.get(Column.CREATION);
    }
    
    Collection<Parameter> getAll() {
        return new HashSet<Parameter>(map.values()); 
    }
    
    ListParameter createParameter(Column c, JList list) {
        ListParameter listParameter = new ListParameter(list, c);
        map.put(c, listParameter);
        return listParameter;
    }
    
     ComboParameter createParameter(Column c, JComboBox combo) {
        ComboParameter comboParameter = new ComboParameter(c, combo);
        map.put(c, comboParameter);
        return comboParameter;
    }
    
    TextFieldParameter createParameter(Column c, JTextField txt) {
        TextFieldParameter textFieldParameter = new TextFieldParameter(c, txt);
        map.put(c, textFieldParameter);
        return textFieldParameter;
    }
    
    ByTextParameter createByTextParameter(JTextField txt, JCheckBox chkSummary, JCheckBox chkDescription) {
        ByTextParameter byTextParameter = new ByTextParameter(txt, chkSummary, chkDescription);
        map.put(Column.SUMMARY, byTextParameter);
        map.put(Column.DESCRIPTION, byTextParameter);
        map.put(Column.COMMENT, byTextParameter);
        return byTextParameter;
    }
            
    ByPeopleParameter createByPeopleParameter(JList list, JCheckBox creatorCheckField, JCheckBox ownerCheckField, JCheckBox commenterCheckField, JCheckBox ccCheckField) {
        ByPeopleParameter byPeopleParameter = new ByPeopleParameter(list, creatorCheckField, ownerCheckField, commenterCheckField, ccCheckField);
        map.put(Column.CREATOR, byPeopleParameter);
        map.put(Column.ASSIGNEE, byPeopleParameter);
        map.put(Column.COMMENTER, byPeopleParameter);
        map.put(Column.WATCHER, byPeopleParameter);
        return byPeopleParameter;
    }
    
    ByDateParameter createByDateParameter(JComboBox cbo, JTextField fromField, JTextField toField) {
        ByDateParameter byDateParameter = new ByDateParameter(cbo, fromField, toField);
        map.put(Column.CREATION, byDateParameter);
        map.put(Column.MODIFICATION, byDateParameter);
        return byDateParameter;
    }

    void setCriteriaValues(Criteria crit) {
        for (Parameter p : getAll()) {
            p.clearValues();
        }
        try {
            List<ParamCriteria> criterias = getCriterias(crit, null);
            ParamCriteria productCrit = null;
            
            Iterator<ParamCriteria> it = criterias.iterator();
            while (it.hasNext()) {
                ParamCriteria pc = it.next();
                if (pc.p == getListParameter(Column.PRODUCT)) {
                    productCrit = pc;
                    it.remove();
                    break;
                }
            }
            // set product first -> any change in the list triggers
            // iteration and component repopulate
            if (productCrit != null) {
                productCrit.p.addCriteriaValue(productCrit.op, productCrit.cc);
            }
            for (ParamCriteria pc : criterias) {
                pc.p.addCriteriaValue(pc.op, pc.cc);
            }
        } finally {
            resetChanged();
        }
    }
    
    public void resetChanged() {
        for (Parameter p : getAll()) {
            p.resetChanged();
        }
    }

    boolean parametersChanged() {
        for (Parameter p : getAll()) {
            if(p.hasChanged()) {
                return true;
            }
        }
        return false;
    }
    
    private List<ParamCriteria> getCriterias(Criteria crit, Operator op) {
        List<ParamCriteria> criterias = new LinkedList<ParamCriteria>();
        if(crit instanceof ColumnCriteria) {
            ColumnCriteria cc = (ColumnCriteria) crit;
            Parameter p = map.get(Column.forColumnName(cc.getColumnName()));
            assert p != null : "Missing parameter for ColumnCriteria [" + cc + "]"; // NOI18N
            if(p != null) {
                criterias.add(new ParamCriteria(p, op, cc));
            } else {
                ODCS.LOG.log(Level.WARNING, "Missing parameter for ColumnCriteria [{0}]", cc); // NOI18N
            }
        } else if(crit instanceof NaryCriteria) {
            NaryCriteria nc = (NaryCriteria)crit;
            List<Criteria> subCrit = (nc).getSubCriteria();
            for (Criteria c : subCrit) {
                criterias.addAll(getCriterias(c, nc.getOperator()));
            }
        } else {
            assert false : "Unexpected Criteria type : " + crit.getClass().getName(); // NOI18N
            ODCS.LOG.log(Level.WARNING, "Unexpected Criteria type : {0}", crit.getClass().getName()); // NOI18N
        }
        return criterias;
    }

    private class ParamCriteria {
        final Parameter p;
        final Operator op;
        final ColumnCriteria cc;
        public ParamCriteria(Parameter p, Operator op, ColumnCriteria cc) {
            this.p = p;
            this.op = op;
            this.cc = cc;
        }
    }
    
    static interface Parameter {
        void setEnabled(boolean b);
        Criteria getCriteria();
        void clearValues();
        void addCriteriaValue(Operator op, ColumnCriteria cc);
        boolean hasChanged();
        void resetChanged();        
    }
    
    static abstract class AbstractParameter implements Parameter {
        
        private final Column column;
        
        public AbstractParameter(Column column) {
            this.column = column;
        }
        public Column getColumn() {
            return column;
        }

        protected Criteria getCriteria(Collection values) {
            if(values == null) {
                return null;
            }
            List<Criteria> criteria = new LinkedList<Criteria>();
            for (Object v : values) {
                criteria.add(new ColumnCriteria(getColumn().getColumnName(), Criteria.Operator.EQUALS, valueToString(v)));
            }
            return toCriteria(criteria, Operator.OR);
        }

        // XXX perhaps parameters should be encoded

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("["); // NOI18N
            sb.append(getColumn().columnName);
            sb.append("]"); // NOI18N
            return sb.toString();
        }
    }
    
    static class ComboParameter extends AbstractParameter {
        
        private final JComboBox combo;
        private int selectedIdx = -1;
        
        public ComboParameter(Column column, JComboBox combo) {
            super(column);
            this.combo = combo;
            combo.setModel(new DefaultComboBoxModel());
            combo.setRenderer(new ParameterRenderer());
        }
        
        public Collection getValues() {
            Object item = combo.getSelectedItem();
            return item != null ? Collections.singleton(item) : null;
        }
        
        public void populate(Collection values) {
            try {
                ArrayList l = new ArrayList(values);
                Collections.sort(l, new ParameterComparator());
                combo.setModel(new DefaultComboBoxModel(l.toArray()));
            } finally {
                resetChanged();
            }
        }
        
        public void setValues(Collection values) {
            try {
                if(values == null) {
                    combo.setSelectedIndex(-1);
                    return;
                }

                assert values.size() == 1;
                if(values.isEmpty()) {
                    combo.setSelectedIndex(-1);
                    return;
                }

                // need the index as the given ParameterValue might have a different displayName
                int idx = ((DefaultComboBoxModel)combo.getModel()).getIndexOf(values.iterator().next());
                if(idx != -1) {
                    combo.setSelectedIndex(idx);
                } 
            } finally {
                resetChanged();
            }
        }

        @Override
        public void clearValues() {
            try {
                combo.setSelectedIndex(-1);
            } finally {
                resetChanged();
            }
        }
        
        @Override
        public void addCriteriaValue(Operator op, ColumnCriteria cc) {
            throw new UnsupportedOperationException("Not supported yet."); // NOI18N
        }
        
        @Override
        public void setEnabled(boolean b) {
            combo.setEnabled(b);
        }
        
        @Override
        public Criteria getCriteria() {
            return getCriteria(getValues());
        }        

        @Override
        public boolean hasChanged() {
            return selectedIdx != combo.getSelectedIndex();
        }

        @Override
        public void resetChanged() {
            selectedIdx = combo.getSelectedIndex();
        }
    }

    static class ListParameter extends AbstractParameter {
        
        private final JList list;
        private int[] selectedIndices = new int[0];
        
        public ListParameter(JList list, Column column) {
            super(column);
            this.list = list;
            list.setModel(new DefaultListModel());
            list.setCellRenderer(new ParameterRenderer());
        }
        
        private Collection getValues() {
            Object[] values = list.getSelectedValues();
            if(values == null || values.length == 0) {
                return null; 
            }
            List ret = new ArrayList();
            ret.addAll(Arrays.asList(values));
            return ret;
        }
        
        public void populate(Collection values) {
            try {
                DefaultListModel m = new DefaultListModel();
                if(values != null) {
                    ArrayList l = new ArrayList(values);
                    Collections.sort(l, new ParameterComparator());
                    for (Object o : l) {
                        m.addElement(o);
                    }
                }
                list.setModel(m);
            } finally {
                resetChanged();
            }   
        }

        @Override
        public void clearValues() {
            try {
                list.clearSelection();
            } finally {
                resetChanged();
            }
        }
        
        @Override
        public void addCriteriaValue(Operator op, ColumnCriteria cc) {
            Object value = cc.getColumnValue();
            if(value == null) {
                return;
            }
            addSelectionInterval(list, value);
        }        
        
        public void setValues(Collection values) {
            try {
                list.clearSelection();
                if(values.isEmpty()) {
                    return;
                }                                        
                List<Integer> selectionList = new LinkedList<Integer>();
                for (Object o : values) {
                    int idx = getItemIndex(list, o);
                    if(idx > -1) {
                        selectionList.add(idx);
                    }
                }
                int[] selection = new int[selectionList.size()];
                int i = 0;
                for (int s : selectionList) {
                    selection[i++] = s;
                }
                list.setSelectedIndices(selection);
                int idx = selection.length > 0 ? selection[0] : -1;
                if(idx > -1) {
                    list.scrollRectToVisible(list.getCellBounds(idx, idx));
                }
            } finally {
                resetChanged();
            }
        }
        
        @Override
        public void setEnabled(boolean  b) {
            list.setEnabled(b);
        }

        @Override
        public Criteria getCriteria() {
            Criteria ret = null;
            try {
                ret = getCriteria(getValues());
                return ret;
            } finally {
                if(ODCS.LOG.isLoggable(Level.FINER)) {
                    ODCS.LOG.log(Level.FINER, "ListParameter {0} returned criteria [{1}]", new Object[] {getColumn().columnName, ret == null ? null : ret.toQueryString()}); // NOI18N        
                }                
            }
        }

        @Override
        public boolean hasChanged() {
            Arrays.sort(selectedIndices);
            int[] currentIndices = list.getSelectedIndices();
            Arrays.sort(currentIndices);
            return !Arrays.equals(currentIndices, selectedIndices);
        }

        @Override
        public void resetChanged() {
            selectedIndices = list.getSelectedIndices();
        }
    }

    static class TextFieldParameter extends AbstractParameter {
        
        private final JTextField txt;
        private String text = ""; // NOI18N
        
        public TextFieldParameter(Column column, JTextField txt) {
            super(column);
            this.txt = txt;
        }
        
        void setValue(String s) {
            txt.setText(s); 
        }

        @Override
        public void clearValues() {
            try {
                txt.setText(""); // NOI18N
            } finally {
                resetChanged();
            }            
        }
        
        @Override
        public void addCriteriaValue(Operator op, ColumnCriteria cc) {
            throw new UnsupportedOperationException("Not supported yet."); // NOI18N
        }
        
        @Override
        public void setEnabled(boolean  b) {
            txt.setEnabled(b);
        }

        public void populate(String txt) {
            setValue(txt);
        }

        @Override
        public Criteria getCriteria() {
            return null;
        }

        @Override
        public boolean hasChanged() {
            return !text.equals(txt.getText());
        }

        @Override
        public void resetChanged() {
            text = txt.getText() + ""; // NOI18N
        }
    }

    static class ByTextParameter implements Parameter {
        
        private final JTextField txt;
        private final JCheckBox chkSummary;
        private final JCheckBox chkDescriptionOrComment;
        private String text = ""; // NOI18N
        private boolean summarySelected;
        private boolean descriptionOrCommentSelected;
        
        public ByTextParameter(JTextField txt, JCheckBox chkSummary, JCheckBox chkDescription) {
            this.txt = txt;
            this.chkSummary = chkSummary;
            this.chkDescriptionOrComment = chkDescription;
        }

        @Override
        public void clearValues() {
            try {
                chkSummary.setSelected(false);
                chkDescriptionOrComment.setSelected(false);
                txt.setText(""); // NOI18N
            } finally {
                resetChanged();
            }            
        }
        
        @Override
        public void addCriteriaValue(Operator op, ColumnCriteria cc) {
            if(cc.getColumnName().equals(Column.SUMMARY.getColumnName())) {
                setValue(txt, chkSummary, cc);
            } else if(cc.getColumnName().equals(Column.DESCRIPTION.getColumnName()) ||
                      cc.getColumnName().equals(Column.COMMENT.getColumnName())) 
            {
                setValue(txt, chkDescriptionOrComment, cc);
            } else {
                assert false : "Not support column name [" + cc.getColumnName() + "] for ByTextParameter ";
            }
        }
        
        private void setValue(JTextField txt, JCheckBox chk, ColumnCriteria cc) {
            Object value = cc.getColumnValue();
            txt.setText(value != null ? (String) value : ""); // NOI18N
            chk.setSelected(true);
        }

        public void setValues(String text, boolean summary, boolean description) {
            try {
                txt.setText(text != null ? text : ""); // NOI18N
                chkSummary.setSelected(summary);
                chkDescriptionOrComment.setSelected(description);
            } finally {
                resetChanged();
            }
        }
        
        @Override
        public void setEnabled(boolean  b) {
            chkSummary.setEnabled(b);
            chkDescriptionOrComment.setEnabled(b);
            txt.setEnabled(b);
        }

        @Override
        public Criteria getCriteria() {
            Criteria ret = null;
            try {
                String s = txt.getText();
                if (s == null || s.trim().isEmpty()) {
                    return ret;
                }
                List<Criteria> criteria = new LinkedList<Criteria>();
                if (chkSummary.isSelected() && chkDescriptionOrComment.isSelected()) {
                    criteria.add(new ColumnCriteria(Column.SUMMARY.columnName, Criteria.Operator.STRING_CONTAINS, s));
                    criteria.add(new ColumnCriteria(Column.DESCRIPTION.columnName, Criteria.Operator.STRING_CONTAINS, s));
                    criteria.add(new ColumnCriteria(Column.COMMENT.columnName, Criteria.Operator.STRING_CONTAINS, s));
                } else if (chkSummary.isSelected()) {
                    criteria.add(new ColumnCriteria(Column.SUMMARY.columnName, Criteria.Operator.STRING_CONTAINS, s));
                } else if (chkDescriptionOrComment.isSelected()) {
                    criteria.add(new ColumnCriteria(Column.DESCRIPTION.columnName, Criteria.Operator.STRING_CONTAINS, s));
                    criteria.add(new ColumnCriteria(Column.COMMENT.columnName, Criteria.Operator.STRING_CONTAINS, s));
                }
                ret = toCriteria(criteria, Criteria.Operator.OR);
                return ret;
            } finally {
                if(ODCS.LOG.isLoggable(Level.FINER)) {
                    ODCS.LOG.log(Level.FINER, "ByTextParameter returned criteria[{0}]", ret == null ? null : ret.toQueryString()); // NOI18N        
                }
            }
        }

        @Override
        public boolean hasChanged() {
            return !text.equals(txt.getText()) ||
                    summarySelected != chkSummary.isSelected() ||
                    descriptionOrCommentSelected != chkDescriptionOrComment.isSelected();
        }

        @Override
        public void resetChanged() {
            text = txt.getText() + ""; // NOI18N
            summarySelected = chkSummary.isSelected();
            descriptionOrCommentSelected = chkDescriptionOrComment.isSelected();
        }
    }
    
    static class ByPeopleParameter implements Parameter {
        
        private final JList list;
        private final JCheckBox creatorCheckField;
        private final JCheckBox ownerCheckField;
        private final JCheckBox commenterCheckField;
        private final JCheckBox ccCheckField;
        private int[] selectedIndices = new int[0];
        private boolean creatorSelected;
        private boolean ownerSelected;
        private boolean commenterSelected;
        private boolean ccSelected;
        
        public ByPeopleParameter(JList list, JCheckBox creatorCheckField, JCheckBox ownerCheckField, JCheckBox commenterCheckField, JCheckBox ccCheckField) {
            this.list = list;
            this.creatorCheckField = creatorCheckField;
            this.ownerCheckField = ownerCheckField;
            this.commenterCheckField = commenterCheckField;
            this.ccCheckField = ccCheckField;
        }
        
        public void setValues(Collection<TaskUserProfile> values, boolean creator, boolean owner, boolean commenter, boolean cc) {
            try {
                creatorCheckField.setSelected(creator);
                ownerCheckField.setSelected(owner);
                commenterCheckField.setSelected(commenter);
                ccCheckField.setSelected(cc);

                list.clearSelection();
                if(values == null || values.isEmpty()) {
                    return;
                }
                List<Integer> selectionList = new LinkedList<Integer>();
                for (int i = 0; i < list.getModel().getSize(); i++) {
                    Object object = list.getModel().getElementAt(i);
                    if(object instanceof TaskUserProfile) {
                        for (TaskUserProfile user : values) {
                            String loginName = user.getLoginName();
                            if(loginName != null && loginName.equals(((TaskUserProfile)object).getLoginName())) {
                                selectionList.add(i);
                            }
                        }
                    }
                }
                if(!selectionList.isEmpty()) {
                    int[] selection = new int[selectionList.size()];
                    int i = 0;
                    for (int s : selectionList) {
                        selection[i++] = s;
                    }
                    list.setSelectedIndices(selection);
                }
            } finally {
                resetChanged();
            }
        }
        
        @Override
        public void setEnabled(boolean  b) {
            creatorCheckField.setEnabled(b);
            ownerCheckField.setEnabled(b);
            commenterCheckField.setEnabled(b);
            ccCheckField.setEnabled(b);
            list.setEnabled(b);
        }

        public void populatePeople(List<TaskUserProfile> values) {
            ArrayList<TaskUserProfile> l = new ArrayList<TaskUserProfile>(values);
            DefaultListModel model = new DefaultListModel();
            if(values != null) {
                Collections.sort(l, new ParameterComparator());
                for (TaskUserProfile v : l) {
                    model.addElement(v);
                }
            }
            list.setModel(model);
        }

        @Override
        public Criteria getCriteria() {
            Criteria ret = null;
            try {
                Object[] values = list.getSelectedValues();
                if(values == null || values.length == 0) {
                    return ret;
                }

                List<Criteria> criteria = new LinkedList<Criteria>();
                addUserCriteria(creatorCheckField, Column.CREATOR, values, criteria);
                addUserCriteria(ownerCheckField, Column.ASSIGNEE, values, criteria);
                addUserCriteria(commenterCheckField, Column.COMMENTER, values, criteria);
                addUserCriteria(ccCheckField, Column.WATCHER, values, criteria);
                ret = toCriteria(criteria, Criteria.Operator.OR);
                return ret;
            } finally {
                if(ODCS.LOG.isLoggable(Level.FINER)) {
                    ODCS.LOG.log(Level.FINER, "ByPeopleParameter returned criteria[{0}]", ret == null ? null : ret.toQueryString()); // NOI18N        
                }
            }
        }

        private void addUserCriteria(JCheckBox chk, Column c, Object[] values, List<Criteria> criteria) {
            if(chk.isSelected()) {
                List<Criteria> l = new LinkedList<Criteria>();
                for (Object value : values) {
                    if(value instanceof TaskUserProfile) {
                        l.add(new ColumnCriteria(c.getColumnName(), Criteria.Operator.EQUALS, ((TaskUserProfile)value).getLoginName()));
                    }
                }
                criteria.add(toCriteria(l, Operator.OR));
            }
        }

        @Override
        public void clearValues() {
            try {
                list.clearSelection();
                creatorCheckField.setSelected(false);
                ownerCheckField.setSelected(false);
                commenterCheckField.setSelected(false);
                ccCheckField.setSelected(false);
            } finally {
                resetChanged();
            }                
        }
        
        @Override
        public void addCriteriaValue(Operator op, ColumnCriteria cc) {
            if(cc.getColumnName().equals(Column.CREATOR.getColumnName())) {
                setValue(list, creatorCheckField, cc);
            } else if(cc.getColumnName().equals(Column.ASSIGNEE.getColumnName())) {
                setValue(list, ownerCheckField, cc);
            } else if(cc.getColumnName().equals(Column.COMMENTER.getColumnName())) {
                setValue(list, commenterCheckField, cc);
            } else if(cc.getColumnName().equals(Column.WATCHER.getColumnName())) {
                setValue(list, ccCheckField, cc);
            } else {
                assert false : "Not supported column name [" + cc.getColumnName() + "] for ByPeopleParameter";
            }
        }

        private void setValue(JList list, JCheckBox chk, ColumnCriteria cc) {
            try {
                addSelectionInterval(list, cc.getColumnValue());
                chk.setSelected(true);
            } finally {
                resetChanged();
            }
        }

        @Override
        public boolean hasChanged() {
            Arrays.sort(selectedIndices);
            int[] currentIndices = list.getSelectedIndices();
            Arrays.sort(currentIndices);
            return !Arrays.equals(selectedIndices, currentIndices) || 
                   creatorSelected != creatorCheckField.isSelected() ||
                   ownerSelected != ownerCheckField.isSelected() ||
                   commenterSelected != commenterCheckField.isSelected() ||
                   ccSelected != ccCheckField.isSelected();
        }

        @Override
        public void resetChanged() {
            selectedIndices = list.getSelectedIndices();
            creatorSelected = creatorCheckField.isSelected();
            ownerSelected = ownerCheckField.isSelected();
            commenterSelected = commenterCheckField.isSelected();
            ccSelected = ccCheckField.isSelected();
        }
    }
    
    static class ByDateParameter implements Parameter {
        
        private final JComboBox cbo;
        private final JTextField fromField;
        private final JTextField toField;
        private int selectedIdx;
        private String fromText = ""; // NOI18N
        private String toText = ""; // NOI18N
        
        public ByDateParameter(JComboBox cbo, JTextField fromField, JTextField toField) {
            this.cbo = cbo;
            this.fromField = fromField;
            this.toField = toField;
            
            DefaultComboBoxModel model = new DefaultComboBoxModel(new Column[] {Column.CREATION, Column.MODIFICATION});
            cbo.setModel(model);
            cbo.setRenderer(new ParameterRenderer());
        }
        
        public void setValues(Column c, String from, String to) {
            try {
                if(c != null) {
                    cbo.setSelectedItem(c);
                } else {
                    cbo.setSelectedIndex(-1);
                }
                fromField.setText(from);
                toField.setText(to);
            } finally {
                resetChanged();
            }  
        }
        
        @Override
        public void setEnabled(boolean  b) {
            cbo.setEnabled(b);
            fromField.setEnabled(b);
            toField.setEnabled(b);
        }

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd"); // NOI18N
        @Override
        public Criteria getCriteria() {
            Criteria ret = null;
            try {
                Column c = (Column) cbo.getSelectedItem();
                if(c == null) {
                    return ret;
                }

                Date dateFrom = null;
                try {
                    dateFrom = getDateFrom();
                } catch (ParseException ex) {
                    ODCS.LOG.log(Level.WARNING, fromField.getText(), ex);
                }

                Date dateTo = null;
                try {
                    dateTo = getDateTo();
                } catch (ParseException ex) {
                    ODCS.LOG.log(Level.WARNING, toField.getText(), ex);
                }

                List<Criteria> criteria = new LinkedList<Criteria>();
                if(dateFrom != null && dateTo != null) {
                    criteria.add(new ColumnCriteria(c.getColumnName(), Criteria.Operator.GREATER_THAN, dateFrom)); 
                    criteria.add(new ColumnCriteria(c.getColumnName(), Criteria.Operator.LESS_THAN, dateTo));

                } else if (dateFrom != null) {
                    criteria.add(new ColumnCriteria(c.getColumnName(), Criteria.Operator.GREATER_THAN, dateFrom));
                } else if (dateTo != null) { 
                    criteria.add(new ColumnCriteria(c.getColumnName(), Criteria.Operator.LESS_THAN, dateTo));
                }
                ret = toCriteria(criteria, Criteria.Operator.AND);
                return ret;
            } finally {
                if(ODCS.LOG.isLoggable(Level.FINER)) {
                    ODCS.LOG.log(Level.FINER, "ByDateParameter returned criteria[{0}]", ret == null ? null :  ret.toQueryString()); // NOI18N        
                }
            }
        }
        
        Date getDateFrom() throws ParseException {
            String txt = fromField.getText();
            if(txt == null || txt.trim().isEmpty()) {
                return null;
            }
            return df.parse(fromField.getText());
        }
        
        Date getDateTo() throws ParseException {
            String to = toField.getText();
            if(to != null && !to.trim().isEmpty() && !Bundle.LBL_Now().equals(to)) {
                return df.parse(to);
            } else {
                return null;
            }
        }

        @Override
        public void clearValues() {
            try {
                cbo.setSelectedIndex(-1);
                fromField.setText(""); // NOI18N
                toField.setText(Bundle.LBL_Now());
            } finally {
                resetChanged();
            }              
        }

        @Override
        public void addCriteriaValue(Operator op, ColumnCriteria cc) {
            if(cc.getColumnName().equals(Column.CREATION.getColumnName())) {
                cbo.setSelectedItem(Column.CREATION);
                setValue(cc);
            } else if(cc.getColumnName().equals(Column.MODIFICATION.getColumnName())) {
                cbo.setSelectedItem(Column.MODIFICATION);
                setValue(cc);
            } else {
                assert false : "Not supported column name [" + cc.getColumnName() + "] for ByPeopleParameter";
            }
        }

        private void setValue(ColumnCriteria cc) {
            if(Operator.GREATER_THAN.equals(cc.getOperator())) {
                fromField.setText(df.format((Date)cc.getColumnValue()));
            } else if (Operator.LESS_THAN.equals(cc.getOperator())) {
                toField.setText(df.format((Date)cc.getColumnValue()));
            } else {
                assert false : "unexpected operator [" + cc.getOperator() + "] in ByDateParameter. ColumnCriteria [" + cc + "]"; // NOI18N
                ODCS.LOG.log(Level.WARNING, "unexpected operator [{0}] in ByDateParameter. ColumnCriteria [{1}]", new Object[] {cc.getOperator(), cc}); // NOI18N
            }
        }

        @Override
        public boolean hasChanged() {
            return selectedIdx != cbo.getSelectedIndex() ||
                   !fromText.equals(fromField.getText()) ||
                   !toText.equals(toField.getText());
        }

        @Override
        public void resetChanged() {
            selectedIdx = cbo.getSelectedIndex();
            fromText = fromField.getText() + ""; // NOI18N
            toText = toField.getText() + ""; // NOI18N
        }
    }
    
    static class CheckBoxParameter extends AbstractParameter {
        private final JCheckBox chk;
        private boolean selected;
        public CheckBoxParameter(JCheckBox chk, Column column) {
            super(column);
            this.chk = chk;
        }
        
        public void setValues(Object... values) {
            assert values.length == 1;
            try {
                if(values.length == 0) {
                    return;
                }
                chk.setSelected((Boolean) values[0]); // NOI18N
            } finally {
                resetChanged();
            }              
        }
                
        public void setValues(Collection b) {
            throw new UnsupportedOperationException();
        }
        
        @Override
        public void setEnabled(boolean  b) {
            chk.setEnabled(b);
        }

        @Override
        public Criteria getCriteria() {
            return null;
        }

        @Override
        public void clearValues() {
            try {
                chk.setSelected(false);
            } finally {
                resetChanged();
            }              
        }
        
        @Override
        public void addCriteriaValue(Operator op, ColumnCriteria cc) {
            throw new UnsupportedOperationException("Not supported yet."); // NOI18N
        }

        @Override
        public boolean hasChanged() {
            return selected != chk.isSelected();
        }

        @Override
        public void resetChanged() {
            selected = chk.isSelected();
        }
    }

    private static class ParameterRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            return super.getListCellRendererComponent(list, valueToString(value), index, isSelected, cellHasFocus); 
        }
    }
    
    private static class ParameterComparator implements Comparator {
        @Override
        public int compare(Object o1, Object o2) {
            if(o1 == null && o2 == null) {
                return 0;
            } else if(o1 == null) {
                return -1;
            } else if(o1 == null) {
                return 1;
            }
            if(o1.getClass() != o2.getClass()) {
                return 0;
            }
            if(o1 instanceof Comparable) {
                return ((Comparable)o1).compareTo((Comparable)o2);
            } else if(o1 instanceof com.tasktop.c2c.server.tasks.domain.Component) {
                return ((com.tasktop.c2c.server.tasks.domain.Component)o1).getName().compareTo(((com.tasktop.c2c.server.tasks.domain.Component)o2).getName());
            } else if(o1 instanceof Product) { 
                return ((Product)o1).getName().compareTo(((Product)o2).getName());
            } else if(o1 instanceof Keyword) { 
                return ((Keyword)o1).getName().compareTo(((Keyword)o2).getName());
            } else if(o1 instanceof com.tasktop.c2c.server.tasks.domain.Component) { 
                return ((com.tasktop.c2c.server.tasks.domain.Component)o1).getName().compareTo(((com.tasktop.c2c.server.tasks.domain.Component)o2).getName());
            }
            return 0;
        }
    }
                
    private static void addSelectionInterval(JList list, Object value) {
        int valueIdx = getItemIndex(list, value);
        if(valueIdx > -1) {
            list.addSelectionInterval(valueIdx, valueIdx);
        }
    }

    private static int getItemIndex(JList list, Object value) {
        int valueIdx = -1;
        ListModel model = list.getModel();
        for(int i = 0; i < model.getSize(); i++) {
            Object o = model.getElementAt(i);
            o = toString(o); 
            if(value.equals(o)) {
                valueIdx = i;
                break;
            }
        }
        return valueIdx;
    }    
    
    private static Criteria toCriteria(List<Criteria> criteria, Operator op) {
        if(criteria == null || criteria.isEmpty()) {
            return null;
        } else if(criteria.size() == 1) {
            return criteria.get(0);
        } else {
            return new NaryCriteria(op, criteria.toArray(new Criteria[criteria.size()]));
        }
    }    
    
    // XXX similar to valueToString
    public static String toString(Object o) {
        if(o == null) {
            return null;
        }
        if(o instanceof TaskUserProfile) {
            return ((TaskUserProfile)o).getLoginName();
        } else if(o instanceof  AbstractReferenceValue) {
            return ((AbstractReferenceValue)o).getValue();
        } else if(o instanceof Product) {
            return ((Product)o).getName();
        } else if(o instanceof Keyword) {
            return ((Keyword)o).getName();
        } else if(o instanceof com.tasktop.c2c.server.tasks.domain.Component) {
            return ((com.tasktop.c2c.server.tasks.domain.Component)o).getName();
        }
        return o.toString();
    }    
    
    private static String valueToString(Object value) {
        if(value == null) {
            return ""; // NOI18N
        }
        if(value instanceof  TaskUserProfile) {
            return ((TaskUserProfile)value).getLoginName();
        } else if(value instanceof  AbstractReferenceValue) {
            return ((AbstractReferenceValue)value).getValue();
        } else if(value instanceof Product) {
            return ((Product)value).getName();
        } else if(value instanceof Keyword) {
            return ((Keyword)value).getName();
        } else if(value instanceof com.tasktop.c2c.server.tasks.domain.Component) {
            return ((com.tasktop.c2c.server.tasks.domain.Component)value).getName();
        } else if(value instanceof Column) {
            return ((Column) value).getDisplayName(); 
        }
        return value.toString();
    }

}
