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

import com.tasktop.c2c.server.common.service.domain.criteria.Criteria;
import com.tasktop.c2c.server.common.service.domain.criteria.CriteriaBuilder;
import com.tasktop.c2c.server.tasks.domain.Keyword;
import java.awt.Component;
import java.lang.String;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
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

/**
 *
 * @author Tomas Stupka
 */
public class QueryParameters {

    
    public enum Column {
        COMMENT("comment"), // NOI18N 
        COMPONENT("componentName"), // NOI18N
        DESCRIPTION("description"), // NOI18N
        ITERATION("iteration"), // NOI18N
        PRODUCT("productName"), // NOI18N
        RELEASE("release"), // NOI18N
        RESOLUTION("resolution"), // NOI18N
        SEVERITY("severity"), // NOI18N
        STATUS("status"), // NOI18N
        SUMMARY("summary"), // NOI18N
        PRIORITY("priority"), // NOI18N
        TASK_TYPE("tasktype"), // NOI18N
        TAGS("keywords"); // NOI18N
            
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
    
//     Parameter get(String columnName) {
//        return get(Column.valueOf(columnName));
//    }
    
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
        map.put(columns[0], new CheckedTextFieldParameter(columns, txt, chk));
    }
            
//    <T extends QueryParameter> T createQueryParameter(Class clazz, Component c, String attribute) {
//        try {
//            Constructor constructor = clazz.getConstructor(c.getClass(), String.class);
//            T t= constructor.newInstance(c, attribute);
//            parameters.add(t);
//            return t),
//        } catch (Exception ex) {
//            C2C.LOG.log(Level.SEVERE, attribute, ex)),
//        }
//        return null;
//    }
    
    static abstract class Parameter {
        
        protected boolean alwaysDisabled = false;
        private final Column column;
        
        public Parameter(Column column) {
            this.column = column;
        }
        public Column getColumn() {
            return column;
        }

        void setValues(Object... values) {
            setValues(Arrays.asList(values));
        }
        
        void populate(Object... values) {
            populate(Arrays.asList(values));
        }
        
        abstract void setValues(Collection values);
        abstract void populate(Collection values);
        abstract Collection getValues(); // XXX get rid of this!!!
         
        Criteria getCriteria() {
            Collection values = getValues();
            if(values == null) {
                return null;
            }
            CriteriaBuilder cb = new CriteriaBuilder();
            for (Object v : values) {
                if(cb.result == null) {
                    cb.column(getColumn().toString(), Criteria.Operator.EQUALS, v.toString());
                } else {
                    cb.or(getColumn().toString(), Criteria.Operator.EQUALS, v.toString());
                }
            }
            return cb.toCriteria();
        }
        
        void setAlwaysDisabled(boolean bl) {
            this.alwaysDisabled = bl;
            setEnabled(false); // true or false, who cares. this is only to trigger the state change
        }

        abstract void setEnabled(boolean b);

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
    
    static class ComboParameter extends Parameter {
        private final JComboBox combo;
        public ComboParameter(Column column, JComboBox combo) {
            super(column);
            this.combo = combo;
            combo.setModel(new DefaultComboBoxModel());
            combo.setRenderer(new ComboParameterRenderer());
        }
        
        @Override
        public Collection getValues() {
            Object item = combo.getSelectedItem();
            return item != null ? Collections.singleton(item) : null;
        }
        
        @Override
        public void populate(Collection values) {
            combo.setModel(new DefaultComboBoxModel(values.toArray()));
        }
        
        @Override
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
        void setEnabled(boolean b) {
            combo.setEnabled(alwaysDisabled ? false : b);
        }
    }

    static class ListParameter extends Parameter {
        
        private final JList list;
        
        public ListParameter(JList list, Column column) {
            super(column);
            this.list = list;
            list.setModel(new DefaultListModel());
        }
        
        @Override
        public Collection getValues() {
            Object[] values = list.getSelectedValues();
            if(values == null || values.length == 0) {
                return null; //EMPTY_PARAMETER_VALUE;
            }
            List ret = new ArrayList();
            for (int i = 0; i < values.length; i++) {
                ret.add(values[i]);
            }
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
        
        @Override
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
        void setEnabled(boolean  b) {
            list.setEnabled(alwaysDisabled ? false : b);
        }
    }

    static class TextFieldParameter extends Parameter {
        private final JTextField txt;
        public TextFieldParameter(Column column, JTextField txt) {
            super(column);
            this.txt = txt;
        }
        @Override
        public Collection<String> getValues() {
            String value = txt.getText();
            if(value == null || value.equals("")) { // NOI18N
                return null; 
            }
            return Collections.singleton(value);
        }
        
        @Override
        void setValues(Object... values) {
            String s = (String) values[0];
            if(s == null) {
                s = "";
            }
            txt.setText(s); // NOI18N
        }
                
        @Override
        public void setValues(Collection b) {
            throw new UnsupportedOperationException();
        }
        
        @Override
        void setEnabled(boolean  b) {
            txt.setEnabled(alwaysDisabled ? false : b);
        }

        @Override
        void populate(Collection value) {
            setValues(value);
        }

        @Override
        Criteria getCriteria() {
            return null;
        }
    }

    static class CheckedTextFieldParameter extends Parameter {
        
        private final JTextField txt;
        private final JCheckBox[] chks;
        
        public CheckedTextFieldParameter(Column[] columns, JTextField txt, JCheckBox... chk) {
            super(columns[0]); // XXX hack
            this.txt = txt;
            this.chks = chk;
        }
        
        @Override
        public Collection<String> getValues() {
            boolean noneSelected = true;
            List<String> ret = new LinkedList<String>();
            for (JCheckBox chk : chks) {
                if(chk.isSelected()) {
                    noneSelected = false;
                    String value = txt.getText();
                    if(value != null && !value.trim().isEmpty()) { // NOI18N
                        ret.add(value);
                    }
                }
            }
            if(noneSelected) {
                return null;
            }
            return ret;
        }
        
        @Override
        void setValues(Object... values) {
            // XXX hack
            for (int i = 0; i < values.length; i++) {
                if(values == null) {
                    chks[i].setSelected(false);
                } else {
                    String s = (String) values[0];
                    txt.setText(s); 
                    chks[i].setSelected(true);
                }
            }
        }
                
        @Override
        public void setValues(Collection b) {
            throw new UnsupportedOperationException();
        }
        
        @Override
        void setEnabled(boolean  b) {
//            txt.setEnabled(alwaysDisabled ? false : b); // might be shared with other parameters
            for (JCheckBox chk : chks) {
                chk.setEnabled(alwaysDisabled ? false : b);
            }
        }

        void populate(Object... value) {
            // do nothing
        }
        
        @Override
        void populate(Collection value) {
            // do nothing
        }

        @Override
        Criteria getCriteria() {
            String s = txt.getText();
            if( s == null || s.trim().isEmpty()) {
                return null;
            }
            CriteriaBuilder cb = new CriteriaBuilder();
            boolean noneSelected = true;
            for (JCheckBox chk : chks) {
                if(chk.isSelected()) {
                    noneSelected = false;
                    if(cb.result == null) {
                        cb.column(getColumn().toString(), Criteria.Operator.STRING_CONTAINS, s);
                    } else {
                        cb.or(getColumn().toString(), Criteria.Operator.STRING_CONTAINS, s);
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
    
    static class CheckBoxParameter extends Parameter {
        private boolean selected = true; 
        private final JCheckBox chk;
        public CheckBoxParameter(JCheckBox chk, Column column) {
            super(column);
            this.chk = chk;
        }
        @Override
        public Collection getValues() {
            return Collections.singleton(chk.isSelected());
        }
        
        @Override
        void setValues(Object... values) {
            assert values.length == 1;
            if(values.length == 0) {
                return;
            }
            chk.setSelected((Boolean) values[0]); // NOI18N
        }
                
        @Override
        public void setValues(Collection b) {
            throw new UnsupportedOperationException();
        }
        
        @Override
        void setEnabled(boolean  b) {
            chk.setEnabled(alwaysDisabled ? false : b);
        }

        @Override
        void populate(Collection b) {
            // do nothing
        }

        @Override
        Criteria getCriteria() {
            return null;
        }
    }

    private static class ComboParameterRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            if(value instanceof Keyword) {
                return super.getListCellRendererComponent(list, ((Keyword)value).getName(), index, isSelected, cellHasFocus); 
            }
            return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus); 
        }
        
    }

//    public static class SimpleQueryParameter extends QueryParameter {
//        private final String[] values),
//
//        public SimpleQueryParameter(CfcTaskAttribute attribute, String[] values) {
//            super(attribute)),
//            this.values(values),
//        }
//
//        @Override
//        String getValues() {
//            if(values == null || values.length == 0) {
//                return null), //EMPTY_PARAMETER_VALUE),),
//            }
//            ParameterValue[] ret(new ParameterValue[values.length]),
//            for (int i(0), i < values.length), i++) {
//                ret[i](new ParameterValue(values[i])),
//            }
//            return ret),
//        }
//
//        @Override
//        void setValues(String values) {
//            // not interested
//        }
//
//        @Override
//        void setEnabled(boolean  b) {
//            // interested
//        }
//    }
}
