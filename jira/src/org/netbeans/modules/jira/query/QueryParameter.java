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

package org.netbeans.modules.jira.query;

import java.awt.Component;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JTextField;

/**
 *
 * @author Tomas Stupka
 */
public abstract class QueryParameter {
    final static ParameterValue[] EMPTY_PARAMETER_VALUE = new ParameterValue[] {new ParameterValue("", "")}; // NOI18N

    static final ParameterValue PV_CONTAINS_ALL_KEYWORDS = new ParameterValue("contains all keywords",  "allwords"); // NOI18N
    static final ParameterValue PV_CONTAINS_ANY_KEYWORDS = new ParameterValue("contains any keywords",  "anywords"); // NOI18N
    static final ParameterValue PV_CONTAINS_NONE_KEYWORDS = new ParameterValue("contains none keywords", "nowords"); // NOI18N

    static final ParameterValue PV_CONTAINS = new ParameterValue("contains", "substring"); // NOI18N
    static final ParameterValue PV_IS = new ParameterValue("is", "exact"); // NOI18N
    static final ParameterValue PV_MATCHES_REGEX = new ParameterValue("matches the regexp", "regexp"); // NOI18N
    static final ParameterValue PV_DOESNT_MATCH_REGEX = new ParameterValue("doesn't match the regexp", "notregexp"); // NOI18N

    static final ParameterValue PV_CONTAINS_ALL_STRINGS = new ParameterValue("contains all of the words/strings", "allwordssubstr"); // NOI18N
    static final ParameterValue PV_CONTAINS_ANY_STRINGS = new ParameterValue("contains any of the words/strings", "anywordssubstr"); // NOI18N
    static final ParameterValue PV_CONTAINS_THE_STRING = new ParameterValue("contains the string", "substring"); // NOI18N
    static final ParameterValue PV_CONTAINS_THE_STRING_CASE = new ParameterValue("contains the string (exact case)", "casesubstring"); // NOI18N
    static final ParameterValue PV_CONTAINS_ALL_WORDS = new ParameterValue("contains all of the words", "allwords"); // NOI18N
    static final ParameterValue PV_CONTAINS_ANY_WORDS = new ParameterValue("contains any of the words", "anywords"); // NOI18N

    static final ParameterValue PV_FIELD_BUG_CREATION = new ParameterValue("[Bug+creation]", "[Bug+creation]"); // NOI18N
    static final ParameterValue PV_FIELD_ALIAS = new ParameterValue("alias", "alias"); // NOI18N
    static final ParameterValue PV_FIELD_ASSIGNED_TO = new ParameterValue("assigned_to", "assigned_to"); // NOI18N
    static final ParameterValue PV_FIELD_LIST_ACCESSIBLE = new ParameterValue("cclist_accessible", "cclist_accessible"); // NOI18N
    static final ParameterValue PV_FIELD_COMPONENT = new ParameterValue("component", "component"); // NOI18N
    static final ParameterValue PV_FIELD_DEADLINE = new ParameterValue("deadline", "deadline"); // NOI18N
    static final ParameterValue PV_FIELD_EVER_CONFIRMED = new ParameterValue("everconfirmed", "everconfirmed"); // NOI18N
    static final ParameterValue PV_FIELD_REP_PLARFORM = new ParameterValue("rep_platform", "rep_platform"); // NOI18N
    static final ParameterValue PV_FIELD_REMAINING_TIME = new ParameterValue("remaining_time", "remaining_time"); // NOI18N
    static final ParameterValue PV_FIELD_WORK_TIME = new ParameterValue("work_time", "work_time"); // NOI18N
    static final ParameterValue PV_FIELD_KEYWORDS = new ParameterValue("keywords", "keywords"); // NOI18N
    static final ParameterValue PV_FIELD_ESTIMATED_TIME = new ParameterValue("estimated_time", "estimated_time"); // NOI18N
    static final ParameterValue PV_FIELD_OP_SYS = new ParameterValue("op_sys", "op_sys"); // NOI18N
    static final ParameterValue PV_FIELD_PRIORITY = new ParameterValue("priority", "priority"); // NOI18N
    static final ParameterValue PV_FIELD_PRODUCT = new ParameterValue("product", "product"); // NOI18N
    static final ParameterValue PV_FIELD_QA_CONTACT = new ParameterValue("qa_contact", "qa_contact"); // NOI18N
    static final ParameterValue PV_FIELD_REPORTER_ACCESSIBLE = new ParameterValue("reporter_accessible", "reporter_accessible"); // NOI18N
    static final ParameterValue PV_FIELD_RESOLUTION = new ParameterValue("resolution", "resolution"); // NOI18N
    static final ParameterValue PV_FIELD_BUG_SEVERITY = new ParameterValue("bug_severity", "bug_severity"); // NOI18N
    static final ParameterValue PV_FIELD_BUG_STATUS = new ParameterValue("bug_status", "bug_status"); // NOI18N
    static final ParameterValue PV_FIELD_SHORT_DESC = new ParameterValue("short_desc", "short_desc"); // NOI18N
    static final ParameterValue PV_FIELD_TARGET_MILESTONE = new ParameterValue("target_milestone", "target_milestone"); // NOI18N
    static final ParameterValue PV_FIELD_BUG_FILE_LOC = new ParameterValue("bug_file_loc", "bug_file_loc"); // NOI18N
    static final ParameterValue PV_FIELD_VERSION = new ParameterValue("version", "version"); // NOI18N
    static final ParameterValue PV_FIELD_VOTES = new ParameterValue("votes", "votes"); // NOI18N
    static final ParameterValue PV_FIELD_STATUS_WHITEBOARD = new ParameterValue("status_whiteboard", "status_whiteboard"); // NOI18N

    static final ParameterValue[] PV_TEXT_SEARCH_VALUES =  new ParameterValue[] {
        PV_CONTAINS_ALL_STRINGS,
        PV_CONTAINS_ANY_STRINGS,
        PV_CONTAINS_THE_STRING,
        PV_CONTAINS_THE_STRING_CASE,
        PV_CONTAINS_ALL_WORDS,
        PV_CONTAINS_ANY_WORDS,
        PV_MATCHES_REGEX,
        PV_DOESNT_MATCH_REGEX
    };
    static final ParameterValue[] PV_KEYWORDS_VALUES =  new ParameterValue[] {
        PV_CONTAINS_ALL_KEYWORDS,
        PV_CONTAINS_ANY_KEYWORDS,
        PV_CONTAINS_NONE_KEYWORDS
    };
    static final ParameterValue[] PV_PEOPLE_VALUES =  new ParameterValue[] {
        PV_CONTAINS,
        PV_IS,
        PV_MATCHES_REGEX,
        PV_DOESNT_MATCH_REGEX
    };
    static final ParameterValue[] PV_LAST_CHANGE =  new ParameterValue[] {
        PV_FIELD_BUG_CREATION,
        PV_FIELD_ALIAS,
        PV_FIELD_ASSIGNED_TO,
        PV_FIELD_LIST_ACCESSIBLE,
        PV_FIELD_COMPONENT,
        PV_FIELD_DEADLINE,
        PV_FIELD_EVER_CONFIRMED,
        PV_FIELD_REP_PLARFORM,
        PV_FIELD_REMAINING_TIME,
        PV_FIELD_WORK_TIME,
        PV_FIELD_KEYWORDS,
        PV_FIELD_ESTIMATED_TIME,
        PV_FIELD_OP_SYS,
        PV_FIELD_PRIORITY,
        PV_FIELD_PRODUCT,
        PV_FIELD_QA_CONTACT,
        PV_FIELD_REPORTER_ACCESSIBLE,
        PV_FIELD_RESOLUTION,
        PV_FIELD_BUG_SEVERITY,
        PV_FIELD_BUG_STATUS,
        PV_FIELD_SHORT_DESC,
        PV_FIELD_TARGET_MILESTONE,
        PV_FIELD_BUG_FILE_LOC,
        PV_FIELD_VERSION,
        PV_FIELD_VOTES,
        PV_FIELD_STATUS_WHITEBOARD
    };

    private final String parameter;
    protected boolean alwaysDisabled = false;
    public QueryParameter(String parameter) {
        this.parameter = parameter;
    }
    public String getParameter() {
        return parameter;
    }
    abstract ParameterValue[] getValues();
    abstract void setValues(ParameterValue[] pvs);    
    void setAlwaysDisabled(boolean bl) {
        this.alwaysDisabled = bl;
        setEnabled(false); // true or false, who cares. this is only to trigger the state change
    }
    abstract void setEnabled(boolean b);

    public StringBuffer get() {
        StringBuffer sb = new StringBuffer();
        ParameterValue[] values = getValues();
        for (ParameterValue pv : values) {
            sb.append("&"); // NOI18N
            sb.append(getParameter());
            sb.append("="); // NOI18N
            sb.append(pv.getValue());
        }
        return sb;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("["); // NOI18N
        sb.append(get());
        sb.append("]"); // NOI18N
        return sb.toString();
    }

    static class ComboParameter extends QueryParameter {
        private final JComboBox combo;
        public ComboParameter(JComboBox combo, String parameter) {
            super(parameter);
            this.combo = combo;
            combo.setModel(new DefaultComboBoxModel());
        }
        @Override
        public ParameterValue[] getValues() {
            ParameterValue value = (ParameterValue) combo.getSelectedItem();
            return value != null ? new ParameterValue[] { value } : EMPTY_PARAMETER_VALUE;
        }
        public void setParameterValues(ParameterValue[] values) {
            combo.setModel(new DefaultComboBoxModel(values));
        }
        @Override
        public void setValues(ParameterValue[] values) {
            assert values.length < 2;
            if(values.length == 0) return;
            ParameterValue pv = values[0];
            
            // need the index as the given ParameterValue might have a different displayName
            int idx = ((DefaultComboBoxModel)combo.getModel()).getIndexOf(pv);
            if(idx != -1) {
                combo.setSelectedIndex(idx);
            }
        }
        @Override
        void setEnabled(boolean b) {
            combo.setEnabled(alwaysDisabled ? false : b);
        }
    }

    static class ListParameter extends QueryParameter {
        private final JList list;
        public ListParameter(JList list, String parameter) {
            super(parameter);
            this.list = list;
            list.setModel(new DefaultListModel());
        }
        @Override
        public ParameterValue[] getValues() {
            Object[] values = list.getSelectedValues();
            if(values == null || values.length == 0) {
                return EMPTY_PARAMETER_VALUE;
            }
            ParameterValue[] ret = new ParameterValue[values.length];
            for (int i = 0; i < values.length; i++) {
                ret[i] = (ParameterValue) values[i];
            }
            return ret;
        }        
        public void setParameterValues(List<ParameterValue> values) {
            setParameterValues(values.toArray(new ParameterValue[values.size()]));
        }
        public void setParameterValues(ParameterValue[] values) {
            DefaultListModel m = new DefaultListModel();
            for (ParameterValue pv : values) {
                m.addElement(pv);
            }
            list.setModel(m);
        }

        @Override
        public void setValues(ParameterValue[] values) {
            if(values.length == 0) return;            
            list.clearSelection();
            int[] selection = new int[values.length];
            for (int i = 0; i < values.length; i++) {
                selection[i] = ((DefaultListModel)list.getModel()).indexOf(values[i]);
            }
            list.setSelectedIndices(selection);
            int idx = selection.length > 0 ? selection[0] : -1;
            if(idx > -1) list.scrollRectToVisible(list.getCellBounds(idx, idx));
        }
        @Override
        void setEnabled(boolean  b) {
            list.setEnabled(alwaysDisabled ? false : b);
        }
    }

    static class TextFieldParameter extends QueryParameter {
        private final JTextField txt;
        public TextFieldParameter(JTextField txt, String parameter) {
            super(parameter);
            this.txt = txt;
        }
        @Override
        public ParameterValue[] getValues() {
            String value = txt.getText();
            if(value == null || value.equals("")) { // NOI18N
                return EMPTY_PARAMETER_VALUE;
            }
            String[] split = value.split(" "); // NOI18N
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < split.length; i++) {
                String s = split[i];
                sb.append(s);
                if(i < split.length - 1) sb.append("+"); // NOI18N
            }
            String v = sb.toString();
            return new ParameterValue[] { new ParameterValue(v, v) };
        }
        @Override
        public void setValues(ParameterValue[] pvs) {
            assert pvs.length < 2;
            if(pvs.length == 0 || pvs[0] == null) {
                return;
            }
            txt.setText(pvs[0].getValue().replace("+", " ")); // NOI18N
        }
        @Override
        void setEnabled(boolean  b) {
            txt.setEnabled(alwaysDisabled ? false : b);
        }
    }

    static class CheckBoxParameter extends QueryParameter {
        private ParameterValue[] selected = new ParameterValue[] {new ParameterValue("1")}; // NOI18N
        private final JCheckBox chk;
        public CheckBoxParameter(JCheckBox chk, String parameter) {
            super(parameter);
            this.chk = chk;
        }
        @Override
        public ParameterValue[] getValues() {
            return chk.isSelected() ? selected : EMPTY_PARAMETER_VALUE;
        }
        @Override
        public void setValues(ParameterValue[] pvs) {
            assert pvs.length < 2;
            if(pvs.length == 0 || pvs[0] == null) {
                return;
            }
            chk.setSelected(pvs[0].getValue().equals("1")); // NOI18N
        }
        @Override
        void setEnabled(boolean  b) {
            chk.setEnabled(alwaysDisabled ? false : b);
        }
    }

    static class ParameterValue {
        private final String displayName;
        private final String value;
        private String toString;
        public ParameterValue(String value) {
            this(value, value);
        }
        public ParameterValue(String displayName, String value) {
            assert displayName != null;
            assert value != null;
            this.displayName = displayName;
            this.value = value;
        }
        public String getDisplayName() {
            return displayName;
        }
        public String getValue() {
            return value;
        }
        @Override
        public String toString() {
            if(toString == null) {
                StringBuffer sb = new StringBuffer();
                sb.append(displayName);
                sb.append("["); // NOI18N
                sb.append(value);
                sb.append("]"); // NOI18N
                toString = sb.toString();
            }
            return toString;
        }
        @Override
        public boolean equals(Object obj) {
            if(obj instanceof ParameterValue) {
                ParameterValue pv = (ParameterValue) obj;
                return value.equals(pv.value);
            }
            return false;
        }
        @Override
        public int hashCode() {
            return value.hashCode();
        }
    }

    static class ParameterValueCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            if(value instanceof ParameterValue) value = ((ParameterValue)value).getDisplayName();
            return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        }
    }   
}