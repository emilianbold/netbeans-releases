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

package org.netbeans.modules.ods.tasks.query;

import com.tasktop.c2c.server.common.service.domain.criteria.ColumnCriteria;
import com.tasktop.c2c.server.common.service.domain.criteria.Criteria;
import com.tasktop.c2c.server.common.service.domain.criteria.CriteriaBuilder;
import com.tasktop.c2c.server.common.service.domain.criteria.NaryCriteria;
import com.tasktop.c2c.server.tasks.domain.Keyword;
import com.tasktop.c2c.server.tasks.domain.TaskUserProfile;
import java.awt.Component;
import java.lang.String;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JTextField;
import javax.swing.ListModel;
import org.netbeans.modules.ods.tasks.util.C2CUtil;
import org.openide.util.Exceptions;

/**
 *
 * @author Tomas Stupka
 */
public class QueryParameters {
    
    public enum Column {
        ASSIGNEE("assignee"), // NOI18N
        COMMENT("comment"), // NOI18N 
        COMMENTER("commentAuthor"), // NOI18N
        COMPONENT("componentName"), // NOI18N
        CREATOR("reporter"), // NOI18N
        CREATION("creationDate"), // NOI18N
        DESCRIPTION("description"), // NOI18N
        ITERATION("iteration"), // NOI18N
        KEYWORDS("keywords"), // NOI18N
        MODIFICATION("modificationDate"), // NOI18N
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
        
        Column(String columnName) {
            this.columnName = columnName;
        }

        @Override
        public String toString() {
            return columnName;
        }
        
    }
    
    private final EnumMap<Column, Parameter> map = new EnumMap<Column, Parameter>(Column.class);

    QueryParameters() { }
        
    Parameter get(Column c) {
        return (Parameter) map.get(c);
    }
    
    ByPeopleParameter getByPeopleParameter() {
        return (ByPeopleParameter) map.get(Column.CREATOR);
    }
    
    Collection<Parameter> getAll() {
        return map.values();
    }
    
     void addParameter(Column c, JList list) {
        map.put(c, new ListParameter(list, c));
    }
    
     void addParameter(Column c, JComboBox combo) {
        map.put(c, new ComboParameter(c, combo));
    }
    
    void addParameter(Column c, JTextField txt) {
        map.put(c, new TextFieldParameter(c, txt));
    }
    
    void addParameter(Column[] columns, JTextField txt, JCheckBox... chk) {
        // XXX kind of a hack to store under the first column name
        map.put(columns[0], new CheckedTextFieldParameter(columns, txt, chk));
    }
            
    void createByPeopleCriteria(JList list, JCheckBox creatorCheckField, JCheckBox ownerCheckField, JCheckBox commenterCheckField, JCheckBox ccCheckField) {
        map.put(Column.CREATOR, new ByPeopleParameter(list, creatorCheckField, ownerCheckField, commenterCheckField, ccCheckField));
    }
    
    void createByDateCriteria(JComboBox cbo, JTextField fromField, JTextField toField) {
        map.put(Column.CREATION, new ByDateParameter(cbo, fromField, toField));
    }
    
    static interface Parameter {
        void populate(Collection values);
        void setEnabled(boolean b);
        Criteria getCriteria();
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
            CriteriaBuilder cb = new CriteriaBuilder();
            for (Object v : values) {
                if(cb.result == null) {
                    cb.column(getColumn().toString(), Criteria.Operator.EQUALS, valueToString(v));
                } else {
                    cb.or(getColumn().toString(), Criteria.Operator.EQUALS, valueToString(v));
                }
            }
            return cb.toCriteria();
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
        
        @Override
        public void populate(Collection values) {
            combo.setModel(new DefaultComboBoxModel(values.toArray()));
        }
        
        public void setValues(Collection values) {
            if(values == null) {
                combo.setSelectedIndex(-1);
                return;
            }

            assert values.size() == 1;
            if(values.isEmpty()) {
                return;
            }

            // need the index as the given ParameterValue might have a different displayName
            int idx = ((DefaultComboBoxModel)combo.getModel()).getIndexOf(values.iterator().next());
            if(idx != -1) {
                combo.setSelectedIndex(idx);
            } 
        }
        
        @Override
        public void setEnabled(boolean b) {
            combo.setEnabled(b);
        }
        
        @Override
        public Criteria getCriteria() {
            return getCriteria(getValues());
        }        
    }

    static class ListParameter extends AbstractParameter {
        
        private final JList list;
        
        public ListParameter(JList list, Column column) {
            super(column);
            this.list = list;
            list.setModel(new DefaultListModel());
            list.setCellRenderer(new ParameterRenderer());
        }
        
        private Collection getValues() {
            Object[] values = list.getSelectedValues();
            if(values == null || values.length == 0) {
                return null; //EMPTY_PARAMETER_VALUE;
            }
            List ret = new ArrayList();
            ret.addAll(Arrays.asList(values));
            return ret;
        }
        
        @Override
        public void populate(Collection values) {
            DefaultListModel m = new DefaultListModel();
            for (Object o : values) {
                m.addElement(o);
            }
            list.setModel(m);
        }
        
        public void setValues(Collection values) {
            list.clearSelection();
            if(values.isEmpty()) {
                return;
            }                                        
            List<Integer> selectionList = new LinkedList<Integer>();
            for (Object o : values) {
                ListModel model = list.getModel();
                // need case sensitive compare
                for(int j = 0; j < model.getSize(); j++) {
                    if(o.equals(model.getElementAt(j))) {
                        selectionList.add(j);
                        break;
                    }
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
        }
        
        @Override
        public void setEnabled(boolean  b) {
            list.setEnabled(b);
        }

        @Override
        public Criteria getCriteria() {
            return getCriteria(getValues());
        }
    }

    static class TextFieldParameter extends AbstractParameter {
        
        private final JTextField txt;
        
        public TextFieldParameter(Column column, JTextField txt) {
            super(column);
            this.txt = txt;
        }
        
        void setValues(String s) {
            txt.setText(s); 
        }
        
        @Override
        public void setEnabled(boolean  b) {
            txt.setEnabled(b);
        }

        @Override
        public void populate(Collection value) {
            setValues((String)value.iterator().next());
        }

        @Override
        public Criteria getCriteria() {
            return null;
        }
    }

    static class CheckedTextFieldParameter implements Parameter {
        
        private final JTextField txt;
        private final JCheckBox[] chks;
        private final Column[] columns;
        
        public CheckedTextFieldParameter(Column[] columns, JTextField txt, JCheckBox... chks) {
            assert columns.length == chks.length : "lenght of columns must be the same as lenght of checkboxes"; // NOI18N
            this.columns = columns;
            this.txt = txt;
            this.chks = chks;
        }
        
        public void setValues(Collection values) {
            if(values == null) {
                for (int i = 0; i < chks.length; i++) {
                    chks[i].setSelected(false);
                }
            } else {
                assert values.size() == chks.length : "lenght of values must be the same as lenght of checkboxes"; // NOI18N
                boolean allNull = true;
                int i = 0;
                for (Object v : values) {
                    String s = (String) v;
                    if(s == null) {
                        chks[i].setSelected(false);
                    } else {
                        allNull = false;
                        txt.setText(s); 
                        chks[i].setSelected(true);
                    }
                    i++;
                }
                if(allNull) {
                    txt.setText(""); // NOI18N
                }
            }
        }
        
        @Override
        public void setEnabled(boolean  b) {
            for (JCheckBox chk : chks) {
                chk.setEnabled(b);
            }
            txt.setEnabled(b);
        }

        @Override
        public void populate(Collection value) {
            // do nothing
        }

        @Override
        public Criteria getCriteria() {
            String s = txt.getText();
            if( s == null || s.trim().isEmpty()) {
                return null;
            }
            CriteriaBuilder cb = new CriteriaBuilder();
            boolean noneSelected = true;
            for (int i = 0; i < chks.length; i++) {
                if(chks[i].isSelected()) {
                    noneSelected = false;
                    if(cb.result == null) {
                        cb.column(columns[i].toString(), Criteria.Operator.STRING_CONTAINS, s);
                    } else {
                        cb.or(columns[i].toString(), Criteria.Operator.STRING_CONTAINS, s);
                    }
                } 
            }
            if(noneSelected) {
                return null;
            } else {
                return cb.toCriteria();
            }
        }
    }
    
    static class ByPeopleParameter implements Parameter {
        
        private final JList list;
        private final JCheckBox creatorCheckField;
        private final JCheckBox ownerCheckField;
        private final JCheckBox commenterCheckField;
        private final JCheckBox ccCheckField;
        
        public ByPeopleParameter(JList list, JCheckBox creatorCheckField, JCheckBox ownerCheckField, JCheckBox commenterCheckField, JCheckBox ccCheckField) {
            this.list = list;
            this.creatorCheckField = creatorCheckField;
            this.ownerCheckField = ownerCheckField;
            this.commenterCheckField = commenterCheckField;
            this.ccCheckField = ccCheckField;
        }
        
        public void setValues(Collection<TaskUserProfile> values, boolean creator, boolean owner, boolean commenter, boolean cc) {
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
        }
        
        @Override
        public void setEnabled(boolean  b) {
            creatorCheckField.setEnabled(b);
            ownerCheckField.setEnabled(b);
            commenterCheckField.setEnabled(b);
            ccCheckField.setEnabled(b);
            list.setEnabled(b);
        }

        @Override
        public void populate(Collection values) {
            // XXX
        }
        
        public void populateList(Collection<TaskUserProfile> values) {
            DefaultListModel model = new DefaultListModel();
            for (TaskUserProfile v : values) {
                model.addElement(v);
            }
            list.setModel(model);
        }

        @Override
        public Criteria getCriteria() {
            Object[] values = list.getSelectedValues();
            if(values == null || values.length == 0) {
                return null;
            }
    
            List<Criteria> criteria = new LinkedList<Criteria>();
            addUserCriteria(creatorCheckField, Column.CREATOR, values, criteria);
            addUserCriteria(ownerCheckField, Column.ASSIGNEE, values, criteria);
            addUserCriteria(commenterCheckField, Column.COMMENTER, values, criteria);
            addUserCriteria(ccCheckField, Column.WATCHER, values, criteria);

            return criteria.isEmpty() ? null : new NaryCriteria(Criteria.Operator.OR, criteria.toArray(new Criteria[criteria.size()]));
        }

        private void addUserCriteria(JCheckBox chk, Column c, Object[] values, List<Criteria> l) {
            if(chk.isSelected()) {
                List<Criteria> criteria = new LinkedList<Criteria>();
                for (Object value : values) {
                    if(value instanceof TaskUserProfile) {
                        criteria.add(new ColumnCriteria(c.toString(), Criteria.Operator.EQUALS, ((TaskUserProfile)value).getLoginName()));
                    }
                }
                l.addAll(criteria);
            }
        }
    }
    
    static class ByDateParameter implements Parameter {
        
        private final JComboBox cbo;
        private final JTextField fromField;
        private final JTextField toField;
        
        public ByDateParameter(JComboBox cbo, JTextField fromField, JTextField toField) {
            this.cbo = cbo;
            this.fromField = fromField;
            this.toField = toField;
            
            DefaultComboBoxModel model = new DefaultComboBoxModel(new Column[] {Column.CREATION, Column.MODIFICATION});
            cbo.setModel(model);
        }
        
        public void setValues(Column c, String from, String to) {
            if(c != null) {
                cbo.setSelectedItem(c);
            } else {
                cbo.setSelectedIndex(-1);
            }
            fromField.setText(from);
            toField.setText(to);
        }
        
        @Override
        public void setEnabled(boolean  b) {
            cbo.setEnabled(b);
            fromField.setEnabled(b);
            toField.setEnabled(b);
        }

        @Override
        public void populate(Collection values) {
            // XXX
        }

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd"); // NOI18N
        @Override
        public Criteria getCriteria() {
            Column c = (Column) cbo.getSelectedItem();
            if(c == null) {
                return null;
            }
            
            Date dateFrom;
            try {
                dateFrom = df.parse(fromField.getText());
            } catch (ParseException ex) {
                Exceptions.printStackTrace(ex);
                return null;
            }
            Date dateTo;
            try {
                String to = toField.getText();
                if(to != null && !to.trim().isEmpty()) {
                    dateTo = df.parse(to);
                } else {
                    dateTo = new Date(System.currentTimeMillis());
                }
            } catch (ParseException ex) {
                Exceptions.printStackTrace(ex);
                return null;
            }
            
            ColumnCriteria dateGreaterThan = new ColumnCriteria(c.toString(), Criteria.Operator.GREATER_THAN, dateFrom);
            ColumnCriteria dateLessThan = new ColumnCriteria(c.toString(), Criteria.Operator.LESS_THAN, dateTo);
            return new NaryCriteria(Criteria.Operator.AND, dateGreaterThan, dateLessThan);
        }
    }
    
    static class CheckBoxParameter extends AbstractParameter {
        private final JCheckBox chk;
        public CheckBoxParameter(JCheckBox chk, Column column) {
            super(column);
            this.chk = chk;
        }
        
        public void setValues(Object... values) {
            assert values.length == 1;
            if(values.length == 0) {
                return;
            }
            chk.setSelected((Boolean) values[0]); // NOI18N
        }
                
        public void setValues(Collection b) {
            throw new UnsupportedOperationException();
        }
        
        @Override
        public void setEnabled(boolean  b) {
            chk.setEnabled(b);
        }

        @Override
        public void populate(Collection b) {
            // do nothing
        }

        @Override
        public Criteria getCriteria() {
            return null;
        }
    }

    private static class ParameterRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            return super.getListCellRendererComponent(list, valueToString(value), index, isSelected, cellHasFocus); 
        }
    }
    
    private static String valueToString(Object value) {
        if(value == null) {
            return ""; // NOI18N
        }
        if(value instanceof Keyword) {
            return ((Keyword) value).getName(); 
        }
        return value.toString();
    }

}
