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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.ods.tasks.query;

import com.tasktop.c2c.server.common.service.domain.criteria.ColumnCriteria;
import com.tasktop.c2c.server.common.service.domain.criteria.Criteria;
import com.tasktop.c2c.server.common.service.domain.criteria.Criteria.Operator;
import com.tasktop.c2c.server.common.service.domain.criteria.NaryCriteria;
import com.tasktop.c2c.server.tasks.domain.Component;
import com.tasktop.c2c.server.tasks.domain.Iteration;
import com.tasktop.c2c.server.tasks.domain.Keyword;
import com.tasktop.c2c.server.tasks.domain.Milestone;
import com.tasktop.c2c.server.tasks.domain.Priority;
import com.tasktop.c2c.server.tasks.domain.Product;
import com.tasktop.c2c.server.tasks.domain.TaskResolution;
import com.tasktop.c2c.server.tasks.domain.TaskSeverity;
import com.tasktop.c2c.server.tasks.domain.TaskStatus;
import com.tasktop.c2c.server.tasks.domain.TaskUserProfile;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JTextField;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.ods.tasks.query.QueryParameters.ByDateParameter;
import org.netbeans.modules.ods.tasks.query.QueryParameters.ByPeopleParameter;
import org.netbeans.modules.ods.tasks.query.QueryParameters.CheckBoxParameter;
import org.netbeans.modules.ods.tasks.query.QueryParameters.ByTextParameter;
import org.netbeans.modules.ods.tasks.query.QueryParameters.Column;
import org.netbeans.modules.ods.tasks.query.QueryParameters.ComboParameter;
import org.netbeans.modules.ods.tasks.query.QueryParameters.ListParameter;
import org.netbeans.modules.ods.tasks.query.QueryParameters.TextFieldParameter;

/**
 *
 * @author tomas
 */
public class QueryParameterTest extends NbTestCase {

    private final static String VALUE1 = "value1";
    private final static String VALUE2 = "value2";
    private final static String VALUE3 = "value3";
    private final static String VALUE4 = "value4";
    private final static List<String> VALUES = Arrays.asList(VALUE1, VALUE2, VALUE3, VALUE4);

    public QueryParameterTest(String arg0) {
        super(arg0);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testByTextParameterEnabled() {
        JCheckBox chk1 = new JCheckBox();
        JCheckBox chk2 = new JCheckBox();
        JTextField txt = new JTextField();
        ByTextParameter cp = new QueryParameters.ByTextParameter(txt, chk1, chk2);
        assertTrue(chk1.isEnabled());
        assertTrue(chk2.isEnabled());
        assertTrue(txt.isEnabled());
        cp.setEnabled(false);
        assertFalse(chk1.isEnabled());
        assertFalse(chk2.isEnabled());
        assertFalse(txt.isEnabled());
    }
    
    public void testCheckedTextValues() {
        JCheckBox chk1 = new JCheckBox();
        JCheckBox chk2 = new JCheckBox();
        JTextField txt = new JTextField();
        ByTextParameter cp = new QueryParameters.ByTextParameter(txt, chk1, chk2);
        
        assertEquals("", txt.getText());
        assertFalse(chk1.isSelected());
        assertFalse(chk2.isSelected());
        assertNull(cp.getCriteria());
        
        cp.setValues(VALUE3, true, false);
        assertTrue(chk1.isSelected());
        assertFalse(chk2.isSelected());
        assertEquals("summary CONTAINS '" + VALUE3 + "'", cp.getCriteria().toQueryString());
        
        cp.setValues(VALUE4, false, true);
        assertFalse(chk1.isSelected());
        assertTrue(chk2.isSelected());
        assertEquals("description CONTAINS '" + VALUE4 + "' OR comment CONTAINS '" + VALUE4 + "'", cp.getCriteria().toQueryString());

        cp.setValues(null, false, false);
        assertFalse(chk1.isSelected());
        assertFalse(chk2.isSelected());
        assertEquals("", txt.getText());
        assertNull(cp.getCriteria());
        
        txt.setText(VALUE4);
        chk2.setSelected(true);
        assertEquals("description CONTAINS '" + VALUE4 + "' OR comment CONTAINS '" + VALUE4 + "'", cp.getCriteria().toQueryString());
        
        chk1.setSelected(false);
        chk2.setSelected(false);
        assertNull(cp.getCriteria());
    }
    
    public void testAddByTextCriteria() {
        JCheckBox chk1 = new JCheckBox();
        JCheckBox chk2 = new JCheckBox();
        JTextField txt = new JTextField();
        ByTextParameter p = new QueryParameters.ByTextParameter(txt, chk1, chk2);
        
        ColumnCriteria cc1 = new ColumnCriteria(Column.SUMMARY.getColumnName(), Criteria.Operator.STRING_CONTAINS, VALUE1);
        p.addCriteriaValue(Criteria.Operator.OR, cc1);
        assertTrue(chk1.isSelected());
        assertFalse(chk2.isSelected());
        assertEquals(VALUE1, txt.getText());
        assertEquals(cc1, p.getCriteria());
        
        ColumnCriteria cc2 = new ColumnCriteria(Column.DESCRIPTION.getColumnName(), Criteria.Operator.STRING_CONTAINS, VALUE2);
        ColumnCriteria cc3 = new ColumnCriteria(Column.COMMENT.getColumnName(), Criteria.Operator.STRING_CONTAINS, VALUE2);
        p.addCriteriaValue(Criteria.Operator.OR, cc2);
        assertTrue(chk1.isSelected());
        assertTrue(chk2.isSelected());
        assertEquals(VALUE2, txt.getText());
        assertEquals(
            new NaryCriteria(
                Criteria.Operator.OR, 
                new ColumnCriteria(Column.SUMMARY.getColumnName(), Criteria.Operator.STRING_CONTAINS, VALUE2), 
                cc2,
                cc3), 
            p.getCriteria());
        
        p.addCriteriaValue(Criteria.Operator.OR, cc3);
        assertTrue(chk1.isSelected());
        assertTrue(chk2.isSelected());
        assertEquals(VALUE2, txt.getText());
        assertEquals(
            new NaryCriteria(
                Criteria.Operator.OR, 
                new ColumnCriteria(Column.SUMMARY.getColumnName(), Criteria.Operator.STRING_CONTAINS, VALUE2), 
                cc2,
                cc3), 
            p.getCriteria());
    }

    public void testByTextCleared() {
        JCheckBox chk1 = new JCheckBox();
        JCheckBox chk2 = new JCheckBox();
        JTextField txt = new JTextField();
        ByTextParameter cp = new QueryParameters.ByTextParameter(txt, chk1, chk2);
        
        chk1.setSelected(true);
        chk2.setSelected(true);
        txt.setText(VALUE1);
        
        cp.clearValues();
        
        assertFalse(chk1.isSelected());
        assertFalse(chk2.isSelected());
        assertEquals("", txt.getText());
        
        assertNull(cp.getCriteria());
    }
            
    public void testComboParameterEnabled() {
        JComboBox combo = new JComboBox();
        ComboParameter cp = new QueryParameters.ComboParameter(QueryParameters.Column.COMMENT, combo);
        assertTrue(combo.isEnabled());
        cp.setEnabled(false);
        assertFalse(combo.isEnabled());
    }
    
    public void testComboParametersValues() {
        JComboBox combo = new JComboBox();
        ComboParameter cp = new QueryParameters.ComboParameter(QueryParameters.Column.COMMENT, combo);
        assertEquals(QueryParameters.Column.COMMENT, cp.getColumn());
        assertNull(combo.getSelectedItem());
        assertEquals((String)null, cp.getValues());
        cp.populate(VALUES);
        cp.setValues(Collections.singleton(VALUE3));

        Object item = combo.getSelectedItem();
        assertNotNull(item);
        assertEquals(VALUE3, item);
        
        assertEquals(VALUE3, cp.getValues().iterator().next());

        combo.setSelectedItem(VALUE4);
        assertEquals(VALUE4, cp.getValues().iterator().next());
    }
    
    public void testComboCleared() {
        JComboBox combo = new JComboBox();
        ComboParameter cp = new QueryParameters.ComboParameter(QueryParameters.Column.COMMENT, combo);
        
        combo.setModel(new DefaultComboBoxModel(VALUES.toArray(new String[VALUES.size()])));
        combo.setSelectedIndex(1);
        
        cp.clearValues();
        assertEquals(-1, combo.getSelectedIndex());
        assertNull(cp.getCriteria());
    }

    public void testListParameterEnabled() {
        JList list = new JList();
        ListParameter lp = new ListParameter(list, QueryParameters.Column.COMMENT);
        assertTrue(list.isEnabled());
        lp.setEnabled(false);
        assertFalse(list.isEnabled());
    }
    
    public void testListParameters() {
        JList list = new JList();
        ListParameter lp = new ListParameter(list, QueryParameters.Column.COMMENT);
        assertEquals(QueryParameters.Column.COMMENT, lp.getColumn());
        assertEquals(-1, list.getSelectedIndex());
        lp.populate(VALUES);
        lp.setValues(Arrays.asList(new String[] {VALUE1, VALUE3}));

        Object[] items = list.getSelectedValues();
        assertNotNull(items);
        assertEquals(2, items.length);
        Set<String> s = new HashSet<String>();
        for (Object i : items) s.add((String) i);
        if(!s.contains(VALUE1)) {
            fail("mising parameter [" + VALUE1 + "]");
        }
        if(!s.contains(VALUE3)) {
            fail("mising parameter [" + VALUE3 + "]");
        }
        
        assertEquals("comment = '" + VALUE1 + "' OR comment = '" + VALUE3 + "'", lp.getCriteria().toQueryString());
        
        list.setSelectedValue(VALUE4, false);
        assertEquals("comment = '" + VALUE4 + "'", lp.getCriteria().toQueryString());
    }
    
    public void testAddListCriteria() {
        JList list = new JList();
        QueryParameters qp = new QueryParameters();
        ListParameter lp = qp.createParameter(Column.COMMENT, list);
        assertEquals(QueryParameters.Column.COMMENT, lp.getColumn());
        assertEquals(-1, list.getSelectedIndex());
        lp.populate(VALUES);
        
        ColumnCriteria cc1 = new ColumnCriteria(Column.COMMENT.getColumnName(), VALUE1);
        lp.addCriteriaValue(Criteria.Operator.OR, cc1);
        List<Object> values = Arrays.asList(list.getSelectedValues());
        assertEquals(1, values.size());
        assertTrue(values.contains(VALUE1));
        assertEquals(cc1, lp.getCriteria());
        
        ColumnCriteria cc2 = new ColumnCriteria(Column.COMMENT.getColumnName(), VALUE3);
        lp.addCriteriaValue(Criteria.Operator.OR, cc2);
        values = Arrays.asList(list.getSelectedValues());
        assertEquals(2, values.size());
        assertTrue(values.contains(VALUE1));
        assertTrue(values.contains(VALUE3));
        
        assertEquals(new NaryCriteria(Criteria.Operator.OR, cc1, cc2), lp.getCriteria());
    }
    
    public void testListParameterCleared() {
        JList list = new JList();
        QueryParameters qp = new QueryParameters();
        ListParameter lp = qp.createParameter(Column.COMMENT, list);
        
        DefaultListModel model = new DefaultListModel();
        for (String v : VALUES) {
            model.addElement(v);
        }
        list.setModel(model);
        list.setSelectedIndex(1);
        
        lp.clearValues();
        assertEquals(-1, list.getSelectedIndex());
        assertNull(lp.getCriteria());
    }
    
    public void testTextFieldParameterEnabled() {
        JTextField text = new JTextField();
        TextFieldParameter tp = new TextFieldParameter(QueryParameters.Column.COMMENT, text);
        assertTrue(text.isEnabled());
        tp.setEnabled(false);
        assertFalse(text.isEnabled());
    }

    public void testTextFieldParameter() throws UnsupportedEncodingException {
        JTextField text = new JTextField();
        TextFieldParameter tp = new TextFieldParameter(QueryParameters.Column.COMMENT, text);
        assertEquals(QueryParameters.Column.COMMENT, tp.getColumn());
        assertEquals("", text.getText());

        tp.setValue(VALUE2);
        assertEquals(VALUE2, text.getText());

        String parameterValue = "NewValue";
        text.setText(parameterValue);
        assertNull(tp.getCriteria()); // not implemented yet
        assertEquals(parameterValue, text.getText());

    }

    public void testTextParameterCleared() {
        JTextField text = new JTextField();
        TextFieldParameter tp = new TextFieldParameter(QueryParameters.Column.COMMENT, text);

        text.setText(VALUE2);
        
        tp.clearValues();
        assertEquals("", text.getText());
        assertNull(tp.getCriteria());
    }
    
    public void testCheckBoxParameterEnabled() {
        JCheckBox checkbox = new JCheckBox();
        CheckBoxParameter cp = new CheckBoxParameter(checkbox, QueryParameters.Column.COMMENT);
        assertTrue(checkbox.isEnabled());
        cp.setEnabled(false);
        assertFalse(checkbox.isEnabled());
    }
    
    public void testCheckBoxParameter() {
        JCheckBox checkbox = new JCheckBox();
        CheckBoxParameter cp = new CheckBoxParameter(checkbox, QueryParameters.Column.COMMENT);
        assertEquals(QueryParameters.Column.COMMENT, cp.getColumn());
        assertFalse(checkbox.isSelected());

        cp.setValues(true);
        assertTrue(checkbox.isSelected());
        assertNull(cp.getCriteria());  // not implemented yet

        cp.setValues(false);
        assertFalse(checkbox.isSelected());
        assertNull(cp.getCriteria()); // not implemented yet
    }

    public void testCheckBoxParameterCleared() {
        JCheckBox checkbox = new JCheckBox();
        CheckBoxParameter cp = new CheckBoxParameter(checkbox, QueryParameters.Column.COMMENT);

        checkbox.setSelected(true);
        
        cp.clearValues();
        assertFalse(checkbox.isSelected());
        assertNull(cp.getCriteria());
    }
    
    public void testByPeopleParameterEnabled() {
        JCheckBox chk1 = new JCheckBox();
        JCheckBox chk2 = new JCheckBox();
        JCheckBox chk3 = new JCheckBox();
        JCheckBox chk4 = new JCheckBox();
        JList list = new JList();
        ByPeopleParameter cp = new ByPeopleParameter(list, chk1, chk2, chk3, chk4);
        assertTrue(chk1.isEnabled());
        assertTrue(chk2.isEnabled());
        assertTrue(chk3.isEnabled());
        assertTrue(chk4.isEnabled());
        assertTrue(list.isEnabled());
        cp.setEnabled(false);
        assertFalse(chk1.isEnabled());
        assertFalse(chk2.isEnabled());
        assertFalse(chk3.isEnabled());
        assertFalse(chk4.isEnabled());
        assertFalse(list.isEnabled());
    }
    
    public void testByPeopleValues() {
        JCheckBox chk1 = new JCheckBox();
        JCheckBox chk2 = new JCheckBox();
        JCheckBox chk3 = new JCheckBox();
        JCheckBox chk4 = new JCheckBox();
        JList list = new JList();
        ByPeopleParameter cp = new ByPeopleParameter(list, chk1, chk2, chk3, chk4);
        
        assertEquals(-1, list.getSelectedIndex());
        assertFalse(chk1.isSelected());
        assertFalse(chk2.isSelected());
        assertFalse(chk3.isSelected());
        assertFalse(chk4.isSelected());
        assertNull(cp.getCriteria());
        
        List<TaskUserProfile> users = getUsers();
        cp.populatePeople(users);
        assertEquals(-1, list.getSelectedIndex());
        assertFalse(chk1.isSelected());
        assertFalse(chk2.isSelected());
        assertFalse(chk3.isSelected());
        assertFalse(chk4.isSelected());
        assertNull(cp.getCriteria());
        
        TaskUserProfile u1 = getUsers().iterator().next();
        getUsers().iterator().next();
        TaskUserProfile u3 = getUsers().iterator().next();
        
        cp.setValues(Arrays.asList(new TaskUserProfile[] {u1, u3}), true, true, false, false);
        assertTrue(chk1.isSelected());
        assertTrue(chk2.isSelected());
        assertFalse(chk3.isSelected());
        assertFalse(chk4.isSelected());
        assertEquals("reporter = '" + u1.getLoginName() + "' OR assignee = '" + u3.getLoginName() + "'", cp.getCriteria().toQueryString());

        cp.setValues(null, false, false, false, false);
        assertFalse(chk1.isSelected());
        assertFalse(chk2.isSelected());
        assertFalse(chk3.isSelected());
        assertFalse(chk4.isSelected());
        assertEquals(-1, list.getSelectedIndex());
        assertNull(cp.getCriteria());
        
        list.setSelectedIndex(0);
        chk1.setSelected(true);
        assertEquals("reporter = '" + u1.getLoginName() + "'", cp.getCriteria().toQueryString());
        
        chk1.setSelected(false);
        assertNull(cp.getCriteria());
    }
    
    public void testAddByPeopleCriteria() {
        JCheckBox chk1 = new JCheckBox();
        JCheckBox chk2 = new JCheckBox();
        JCheckBox chk3 = new JCheckBox();
        JCheckBox chk4 = new JCheckBox();
        JList list = new JList();
        ByPeopleParameter p = new ByPeopleParameter(list, chk1, chk2, chk3, chk4);
        
        List<TaskUserProfile> users = getUsers();
        p.populatePeople(getUsers());
        
        TaskUserProfile user0 = users.get(0);
        ColumnCriteria cc1 = new ColumnCriteria(Column.ASSIGNEE.getColumnName(), user0.getLoginName());
        p.addCriteriaValue(Criteria.Operator.OR, cc1);
        assertFalse(chk1.isSelected());
        assertTrue(chk2.isSelected());
        assertFalse(chk3.isSelected());
        assertFalse(chk4.isSelected());
        assertEquals(user0, list.getSelectedValue());
        assertEquals(cc1, p.getCriteria());
        
        TaskUserProfile user1 = users.get(1);
        ColumnCriteria cc2 = new ColumnCriteria(Column.ASSIGNEE.getColumnName(), user1.getLoginName());
        p.addCriteriaValue(Criteria.Operator.OR, cc2);
        assertFalse(chk1.isSelected());
        assertTrue(chk2.isSelected());
        assertFalse(chk3.isSelected());
        assertFalse(chk4.isSelected());
        assertTrue(Arrays.asList(list.getSelectedValues()).contains(user1));
        assertTrue(Arrays.asList(list.getSelectedValues()).contains(user0));
        assertEquals(new NaryCriteria(Operator.OR, cc1, cc2), p.getCriteria());        
        
        ColumnCriteria cc3 = new ColumnCriteria(Column.CREATOR.getColumnName(), user1.getLoginName());
        p.addCriteriaValue(Criteria.Operator.OR, cc3);
        assertTrue(chk1.isSelected());
        assertTrue(chk2.isSelected());
        assertFalse(chk3.isSelected());
        assertFalse(chk4.isSelected());
        assertTrue(Arrays.asList(list.getSelectedValues()).contains(user1));
        assertTrue(Arrays.asList(list.getSelectedValues()).contains(user0));
        
        List<Criteria> critList = ((NaryCriteria)p.getCriteria()).getSubCriteria();
        assertTrue(critList.contains(new NaryCriteria(Operator.OR, cc1, cc2)));
        ColumnCriteria cc3_1 = new ColumnCriteria(Column.CREATOR.getColumnName(), user0.getLoginName()); // union of all previous user additions
        assertTrue(critList.contains(new NaryCriteria(Operator.OR, cc3_1, cc3)));
        
        ColumnCriteria cc4 = new ColumnCriteria(Column.WATCHER.getColumnName(), user1.getLoginName());
        p.addCriteriaValue(Criteria.Operator.OR, cc4);
        assertTrue(chk1.isSelected());
        assertTrue(chk2.isSelected());
        assertFalse(chk3.isSelected());
        assertTrue(chk4.isSelected());
        assertTrue(Arrays.asList(list.getSelectedValues()).contains(user1));
        assertTrue(Arrays.asList(list.getSelectedValues()).contains(user0));
        
        critList = ((NaryCriteria)p.getCriteria()).getSubCriteria();
        assertTrue(critList.contains(new NaryCriteria(Operator.OR, cc1, cc2)));
        assertTrue(critList.contains(new NaryCriteria(Operator.OR, cc3_1, cc3)));
        ColumnCriteria cc4_1 = new ColumnCriteria(Column.WATCHER.getColumnName(), user0.getLoginName()); // union of all previous user additions
        assertTrue(critList.contains(new NaryCriteria(Operator.OR, cc4_1, cc4)));
        
        ColumnCriteria cc5 = new ColumnCriteria(Column.COMMENTER.getColumnName(), user1.getLoginName());
        p.addCriteriaValue(Criteria.Operator.OR, cc5);
        assertTrue(chk1.isSelected());
        assertTrue(chk2.isSelected());
        assertTrue(chk3.isSelected());
        assertTrue(chk4.isSelected());
        assertTrue(Arrays.asList(list.getSelectedValues()).contains(user1));
        assertTrue(Arrays.asList(list.getSelectedValues()).contains(user0));
        
        critList = ((NaryCriteria)p.getCriteria()).getSubCriteria();
        assertTrue(critList.contains(new NaryCriteria(Operator.OR, cc1, cc2)));
        assertTrue(critList.contains(new NaryCriteria(Operator.OR, cc3_1, cc3)));
        assertTrue(critList.contains(new NaryCriteria(Operator.OR, cc4_1, cc4)));
        ColumnCriteria cc5_1 = new ColumnCriteria(Column.COMMENTER.getColumnName(), user0.getLoginName()); // union of all previous user additions
        assertTrue(critList.contains(new NaryCriteria(Operator.OR, cc5_1, cc5)));
        
    }
    
    public void testByPeopleParameterCleared() {
        JCheckBox chk1 = new JCheckBox();
        JCheckBox chk2 = new JCheckBox();
        JCheckBox chk3 = new JCheckBox();
        JCheckBox chk4 = new JCheckBox();
        JList list = new JList();
        ByPeopleParameter p = new ByPeopleParameter(list, chk1, chk2, chk3, chk4);
        
        chk1.setSelected(true);
        chk2.setSelected(true);
        chk3.setSelected(true);
        chk4.setSelected(true);
        
        DefaultListModel model = new DefaultListModel();
        for (String v : VALUES) {
            model.addElement(v);
        }
        list.setModel(model);
        list.setSelectedIndex(1);
        
        p.clearValues();
        
        assertFalse(chk1.isSelected());
        assertFalse(chk2.isSelected());
        assertFalse(chk3.isSelected());
        assertFalse(chk4.isSelected());
        assertEquals(-1, list.getSelectedIndex());
        assertNull(p.getCriteria());
    }
    
    public void testByDateParameterEnabled() {
        JComboBox combo = new JComboBox();
        JTextField from = new JTextField();
        JTextField to = new JTextField();
        ByDateParameter cp = new ByDateParameter(combo, from, to);
        
        assertTrue(combo.isEnabled());
        assertTrue(from.isEnabled());
        assertTrue(to.isEnabled());
        cp.setEnabled(false);
        assertFalse(combo.isEnabled());
        assertFalse(from.isEnabled());
        assertFalse(to.isEnabled());
    }
    
    public void testByDateValues() throws ParseException {
        JComboBox combo = new JComboBox();
        JTextField from = new JTextField();
        JTextField to = new JTextField();
        ByDateParameter p = new ByDateParameter(combo, from, to);
        
        assertEquals(0, combo.getSelectedIndex());
        assertTrue(from.getText().trim().isEmpty());
        assertTrue(to.getText().trim().isEmpty());
        assertNull(p.getCriteria());
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String fromString = "2012-10-01";
        String toString = "2012-10-10";
        
        p.setValues(Column.CREATION, fromString, null);
        assertEquals(Column.CREATION, combo.getSelectedItem());
        assertEquals(fromString, from.getText());
        assertEquals("", to.getText());
        assertEquals(new ColumnCriteria(Column.CREATION.getColumnName(), Operator.GREATER_THAN, sdf.parse(fromString)), p.getCriteria());
        
        p.setValues(Column.MODIFICATION, null, toString);
        assertEquals(Column.MODIFICATION, combo.getSelectedItem());
        assertEquals("", from.getText());
        assertEquals(toString, to.getText());
        assertEquals(new ColumnCriteria(Column.MODIFICATION.getColumnName(), Operator.LESS_THAN, sdf.parse(toString)), p.getCriteria());
    }
    
    public void testAddByCreateDateCriteria() throws ParseException {
        JComboBox combo = new JComboBox();
        JTextField from = new JTextField();
        JTextField to = new JTextField();
        ByDateParameter p = new ByDateParameter(combo, from, to);
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String fromString = "2012-10-01";
        String toString = "2012-10-10";

        ColumnCriteria fromCriteria = new ColumnCriteria(Column.CREATION.getColumnName(), Operator.GREATER_THAN, sdf.parse(fromString));
 
        p.addCriteriaValue(Operator.AND, fromCriteria);
        assertEquals(Column.CREATION, combo.getSelectedItem());
        assertEquals(fromString, from.getText());
        assertEquals("", to.getText());
        assertEquals(fromCriteria, p.getCriteria());
        
        ColumnCriteria toCriteria = new ColumnCriteria(Column.CREATION.getColumnName(), Operator.LESS_THAN, sdf.parse(toString));
 
        p.addCriteriaValue(Operator.AND, toCriteria);
        assertEquals(Column.CREATION, combo.getSelectedItem());
        assertEquals(fromString, from.getText());
        assertEquals(toString, to.getText());
        assertEquals(new NaryCriteria(Operator.AND, fromCriteria, toCriteria), p.getCriteria());
    }
    
    public void testAddByModifyDateCriteria() throws ParseException {
        JComboBox combo = new JComboBox();
        JTextField from = new JTextField();
        JTextField to = new JTextField();
        ByDateParameter p = new ByDateParameter(combo, from, to);
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String fromString = "2012-10-01";
        String toString = "2012-10-10";
        
        ColumnCriteria fromCriteria = new ColumnCriteria(Column.MODIFICATION.getColumnName(), Operator.GREATER_THAN, sdf.parse(fromString));
 
        p.addCriteriaValue(Operator.AND, fromCriteria);
        assertEquals(Column.MODIFICATION, combo.getSelectedItem());
        assertEquals(fromString, from.getText());
        assertEquals("", to.getText());
        assertEquals(fromCriteria, p.getCriteria());
        
        ColumnCriteria toCriteria = new ColumnCriteria(Column.MODIFICATION.getColumnName(), Operator.LESS_THAN, sdf.parse(toString));
 
        p.addCriteriaValue(Operator.AND, toCriteria);
        assertEquals(Column.MODIFICATION, combo.getSelectedItem());
        assertEquals(fromString, from.getText());
        assertEquals(toString, to.getText());
        assertEquals(new NaryCriteria(Operator.AND, fromCriteria, toCriteria), p.getCriteria());
    }
    
    public void testByDateParameterCleared() {
        JComboBox combo = new JComboBox();
        JTextField from = new JTextField();
        JTextField to = new JTextField();
        ByDateParameter p = new ByDateParameter(combo, from, to);
        
        combo.setSelectedIndex(1);
        from.setText("2012-10-01");
        to.setText("2012-10-10");
        
        p.clearValues();
        
        assertEquals(-1, combo.getSelectedIndex());
        assertTrue(from.getText().isEmpty());
        assertEquals(Bundle.LBL_Now(), to.getText());
    }
    
    public void testProductCriteria() {
        testListCriteria(Column.PRODUCT, getProducts());        
    }
    
    public void testComponentCriteria() {
        testListCriteria(Column.COMPONENT, getComponents());
    }
    
    public void testSeverityCriteria() {
        testListCriteria(Column.SEVERITY, getSeverities());
    }
    
    public void testReleaseCriteria() {
        testListCriteria(Column.RELEASE, getReleases());
    }
    
    public void testPriorityCriteria() {
        testListCriteria(Column.PRIORITY, getPriorities());
    }
    
    public void testStatusCriteria() {
        testListCriteria(Column.STATUS, getStatuses());
    }
    
    public void testResolutionCriteria() {
        testListCriteria(Column.RESOLUTION, getResolutions());
    }
    
    public void testTypeCriteria() {
        testListCriteria(Column.TASK_TYPE, getTypes());
    }
    
    public void testIterationCriteria() {
        testListCriteria(Column.ITERATION, getIterations());
    }
    
    public void testKeywordCriteria() {
        testListCriteria(Column.KEYWORDS, getKeywords());
    }
    
    private void testListCriteria(Column c, List values) {
        QueryParameters qp = new QueryParameters();
        NaryCriteria actuallCriteria = initParameter(qp, c, values);
        assertListCriteria(qp.getListParameter(c), actuallCriteria);
    }
    
    public void testComposedCriteria() throws ParseException {
        QueryParameters qp = new QueryParameters();
        
        // list parameters
        NaryCriteria productCriteria = initParameter(qp, Column.PRODUCT, getProducts());
        NaryCriteria componentCriteria = initParameter(qp, Column.COMPONENT, getComponents());
        NaryCriteria severityCriteria = initParameter(qp, Column.SEVERITY, getSeverities());
        NaryCriteria releaseCriteria = initParameter(qp, Column.RELEASE, getReleases());
        NaryCriteria priorityCriteria = initParameter(qp, Column.PRIORITY, getPriorities());
        NaryCriteria statusCriteria = initParameter(qp, Column.STATUS, getStatuses());
        NaryCriteria resolutionCriteria = initParameter(qp, Column.RESOLUTION, getResolutions());
        NaryCriteria typeCriteria = initParameter(qp, Column.TASK_TYPE, getTypes());
        NaryCriteria iterationCriteria = initParameter(qp, Column.ITERATION, getIterations());
        NaryCriteria keywordCriteria = initParameter(qp, Column.KEYWORDS, getIterations());
        
        // byText parameter
        ByTextParameter byTextParameter = qp.createByTextParameter(new JTextField(), new JCheckBox(), new JCheckBox());
        ColumnCriteria summaryCriteria = new ColumnCriteria(Column.SUMMARY.getColumnName(), Operator.STRING_CONTAINS, "test");
        ColumnCriteria descriptionCriteria = new ColumnCriteria(Column.DESCRIPTION.getColumnName(), Operator.STRING_CONTAINS, "test");
        ColumnCriteria commentCriteria = new ColumnCriteria(Column.COMMENT.getColumnName(), Operator.STRING_CONTAINS, "test");
        NaryCriteria byTextCriteria = new NaryCriteria(Operator.OR, summaryCriteria, descriptionCriteria);
        
        // byDate parameter
        ByDateParameter byDateParameter = qp.createByDateParameter(new JComboBox(), new JTextField(), new JTextField());
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String fromString = "2012-10-01";
        String toString = "2012-10-10";
        
        ColumnCriteria fromCriteria = new ColumnCriteria(Column.MODIFICATION.getColumnName(), Operator.GREATER_THAN, sdf.parse(fromString));
        ColumnCriteria toCriteria = new ColumnCriteria(Column.MODIFICATION.getColumnName(), Operator.LESS_THAN, sdf.parse(toString));
        NaryCriteria byDateCriteria = new NaryCriteria(Operator.AND, fromCriteria, toCriteria);
        
        // byPeople parameter
        ByPeopleParameter byPeopleParameter = qp.createByPeopleParameter(new JList(), new JCheckBox(), new JCheckBox(), new JCheckBox(), new JCheckBox());
        List<TaskUserProfile> users = getUsers();
        byPeopleParameter.populatePeople(users);
        ColumnCriteria assigneeUser0Criteria = new ColumnCriteria(Column.ASSIGNEE.getColumnName(), users.get(0).getLoginName());
        ColumnCriteria assigneeUser1Criteria = new ColumnCriteria(Column.ASSIGNEE.getColumnName(), users.get(1).getLoginName());
        ColumnCriteria watcherUser0Criteria = new ColumnCriteria(Column.WATCHER.getColumnName(), users.get(0).getLoginName());
        ColumnCriteria watcherUser1Criteria = new ColumnCriteria(Column.WATCHER.getColumnName(), users.get(1).getLoginName());
        ColumnCriteria commenterUser0Criteria = new ColumnCriteria(Column.COMMENTER.getColumnName(), users.get(0).getLoginName());
        ColumnCriteria commenterUser1Criteria = new ColumnCriteria(Column.COMMENTER.getColumnName(), users.get(1).getLoginName());
        ColumnCriteria creatorUser0Criteria = new ColumnCriteria(Column.CREATOR.getColumnName(), users.get(0).getLoginName());
        ColumnCriteria creatorUser1Criteria = new ColumnCriteria(Column.CREATOR.getColumnName(), users.get(1).getLoginName());
        NaryCriteria byPeopleCriteria = 
            new NaryCriteria(
                Operator.OR, 
                new NaryCriteria(Operator.OR, assigneeUser0Criteria, assigneeUser1Criteria),
                new NaryCriteria(Operator.OR, watcherUser0Criteria, watcherUser1Criteria),
                new NaryCriteria(Operator.OR, commenterUser0Criteria, commenterUser1Criteria),
                new NaryCriteria(Operator.OR, creatorUser0Criteria, creatorUser1Criteria));
        
        // set criteria for all params in one shot
        qp.setCriteriaValues(
            new NaryCriteria(
                Criteria.Operator.AND, 
                productCriteria,
                componentCriteria,
                severityCriteria,
                releaseCriteria,
                priorityCriteria,
                statusCriteria,
                resolutionCriteria,
                typeCriteria,
                iterationCriteria,
                keywordCriteria,
                byTextCriteria,
                byDateCriteria,
                byPeopleCriteria));
        
        // assert that all list parametrs where properly preset
        assertListCriteria(qp.getListParameter(Column.PRODUCT), productCriteria);
        assertListCriteria(qp.getListParameter(Column.COMPONENT), componentCriteria);
        assertListCriteria(qp.getListParameter(Column.SEVERITY), severityCriteria);
        assertListCriteria(qp.getListParameter(Column.RELEASE), releaseCriteria);
        assertListCriteria(qp.getListParameter(Column.PRIORITY), priorityCriteria);
        assertListCriteria(qp.getListParameter(Column.STATUS), statusCriteria);
        assertListCriteria(qp.getListParameter(Column.RESOLUTION), resolutionCriteria);
        assertListCriteria(qp.getListParameter(Column.TASK_TYPE), typeCriteria);
        assertListCriteria(qp.getListParameter(Column.ITERATION), iterationCriteria);
        assertListCriteria(qp.getListParameter(Column.KEYWORDS), keywordCriteria);
        
        // assert that byText parameter was properly preset
        NaryCriteria nc = (NaryCriteria) byTextParameter.getCriteria();
        List<Criteria> subcriteria = nc.getSubCriteria();
        assertEquals(3, subcriteria.size());
        assertTrue(subcriteria.contains(summaryCriteria));
        assertTrue(subcriteria.contains(descriptionCriteria));        
        assertTrue(subcriteria.contains(commentCriteria));        
        
        // assert that byDate parameter was properly preset
        nc = (NaryCriteria) byDateParameter.getCriteria();
        subcriteria = nc.getSubCriteria();
        assertEquals(2, subcriteria.size());
        assertTrue(subcriteria.contains(fromCriteria));
        assertTrue(subcriteria.contains(toCriteria));        
        
        // assert that byPeople parameter was properly preset
        nc = (NaryCriteria) byPeopleParameter.getCriteria();
        List<Criteria> narySubcriteria = nc.getSubCriteria();
        assertEquals(4, narySubcriteria.size());
        List<Criteria> c = new LinkedList<Criteria>();
        for (Criteria sc : narySubcriteria) {
            c.addAll(((NaryCriteria)sc).getSubCriteria());
        }
                
        assertTrue(c.contains(assigneeUser0Criteria));
        assertTrue(c.contains(assigneeUser1Criteria));
        assertTrue(c.contains(watcherUser0Criteria));
        assertTrue(c.contains(watcherUser1Criteria));
        assertTrue(c.contains(commenterUser0Criteria));
        assertTrue(c.contains(commenterUser1Criteria));
        assertTrue(c.contains(creatorUser0Criteria));
        assertTrue(c.contains(creatorUser1Criteria));
        
    }

    private NaryCriteria initParameter(QueryParameters qp, Column c, List values) {
        ListParameter p = qp.createParameter(c, new JList());
        p.populate(values);
        NaryCriteria criteria = 
            new NaryCriteria(
                Criteria.Operator.OR, 
                new ColumnCriteria(c.getColumnName(), toString(values.get(0))),
                new ColumnCriteria(c.getColumnName(), toString(values.get(2))));
        qp.setCriteriaValues(criteria);
        return criteria;
    }

    private void assertListCriteria(ListParameter p, Criteria actuallCriteria) {
        List<Criteria> expectedCriteria = ((NaryCriteria)p.getCriteria()).getSubCriteria();
        assertTrue(actuallCriteria instanceof NaryCriteria);
        NaryCriteria nc = (NaryCriteria) actuallCriteria;
        assertEquals(nc.getSubCriteria().size(), expectedCriteria.size());
        assertEquals(nc.getOperator(), Operator.OR);
        for (Criteria ec : expectedCriteria) {
            boolean found = false;
            for (Criteria ac : nc.getSubCriteria()) {
                if(((ColumnCriteria)ec).getColumnName().equals(((ColumnCriteria)ac).getColumnName()) &&
                   ((ColumnCriteria)ec).getColumnValue().equals(((ColumnCriteria)ac).getColumnValue())) 
                {
                    found = true;
                    break;
                }
            }
            if(!found) {
                fail("there was no criteria for column [" + ((ColumnCriteria)ec).getColumnName() + "] with value [" + ((ColumnCriteria)ec).getColumnValue() + "]");
            }
        }
    }
    
    private List<Product> getProducts() {
        List<Product> ret = new LinkedList<Product>();
        Product p1 = new Product();
        p1.setName("product1");
        ret.add(p1);
        Product p2 = new Product();
        p2.setName("product2");
        ret.add(p2);
        Product p3 = new Product();
        p3.setName("product3");
        ret.add(p3);
        return ret;
    }
    
    private List<Component> getComponents() {
        List<Component> ret = new LinkedList<Component>();
        Component c1 = new Component();
        c1.setName("component1");
        ret.add(c1);
        Component c2 = new Component();
        c2.setName("component2");
        ret.add(c2);
        Component c3 = new Component();
        c3.setName("component3");
        ret.add(c3);
        return ret;
    }
    
    private List<TaskSeverity> getSeverities() {
        List<TaskSeverity> ret = new LinkedList<TaskSeverity>();
        TaskSeverity ts1 = new TaskSeverity();
        ts1.setValue("severity1");
        ts1.setSortkey((short)1);
        ret.add(ts1);
        TaskSeverity ts2 = new TaskSeverity();
        ts2.setValue("severity2");
        ts2.setSortkey((short)2);
        ret.add(ts2);
        TaskSeverity ts3 = new TaskSeverity();
        ts3.setValue("severity3");
        ts3.setSortkey((short)3);
        ret.add(ts3);
        return ret;
    }
    
    private List<Milestone> getReleases() {
        List<Milestone> ret = new LinkedList<Milestone>();
        Milestone m1 = new Milestone();
        m1.setValue("milestone1");
        m1.setSortkey((short)1);
        ret.add(m1);
        Milestone m2 = new Milestone();
        m2.setValue("milestone2");
        m2.setSortkey((short)2);
        ret.add(m2);
        Milestone m3 = new Milestone();
        m3.setValue("milestone3");
        m3.setSortkey((short)3);
        ret.add(m3);
        return ret;
    }
    
    private List<Priority> getPriorities() {
        List<Priority> ret = new LinkedList<Priority>();
        Priority p1 = new Priority();
        p1.setValue("priority1");
        p1.setSortkey((short)1);
        ret.add(p1);
        Priority p2 = new Priority();
        p2.setValue("priority2");
        p2.setSortkey((short)2);
        ret.add(p2);
        Priority p3 = new Priority();
        p3.setValue("priority3");
        p3.setSortkey((short)3);
        ret.add(p3);
        return ret;
    }
    
    private List<TaskStatus> getStatuses() {
        List<TaskStatus> ret = new LinkedList<TaskStatus>();
        TaskStatus s1 = new TaskStatus();
        s1.setValue("status1");
        s1.setSortkey((short)1);
        ret.add(s1);
        TaskStatus s2 = new TaskStatus();
        s2.setValue("status2");
        s2.setSortkey((short)2);
        ret.add(s2);
        TaskStatus s3 = new TaskStatus();
        s3.setValue("status3");
        s3.setSortkey((short)3);
        ret.add(s3);
        return ret;
    }
    
    private List<String> getTypes() {
        List<String> ret = new LinkedList<String>();
        ret.add("type1");
        ret.add("type2");
        ret.add("type3");
        return ret;
    }
    
    private List<Iteration> getIterations() {
        List<Iteration> ret = new LinkedList<Iteration>();
        Iteration i1 = new Iteration();
        i1.setValue("iteration1");
        i1.setSortkey((short)1);
        ret.add(i1);
        Iteration i2 = new Iteration();
        i2.setValue("iteration2");
        i2.setSortkey((short)2);
        ret.add(i2);
        Iteration i3 = new Iteration();
        i3.setValue("iteration3");
        i3.setSortkey((short)3);
        ret.add(i3);
        return ret;
    }
    
    private List<Keyword> getKeywords() {
        List<Keyword> ret = new LinkedList<Keyword>();
        Keyword k1 = new Keyword();
        k1.setName("keyword1");
        ret.add(k1);
        Keyword k2 = new Keyword();
        k2.setName("keyword2");
        ret.add(k2);
        Keyword k3 = new Keyword();
        k3.setName("keyword3");
        ret.add(k3);
        return ret;
    }
    
    private List<TaskResolution> getResolutions() {
        List<TaskResolution> ret = new LinkedList<TaskResolution>();
        TaskResolution r1 = new TaskResolution();
        r1.setValue("resolution1");
        r1.setSortkey((short)1);
        ret.add(r1);
        TaskResolution r2 = new TaskResolution();
        r2.setValue("resolution2");
        r2.setSortkey((short)2);
        ret.add(r2);
        TaskResolution r3 = new TaskResolution();
        r3.setValue("resolution3");
        r3.setSortkey((short)3);
        ret.add(r3);
        return ret;
    }
    
    private List<TaskUserProfile> getUsers() {
        List<TaskUserProfile> ret = new LinkedList<TaskUserProfile>();
        TaskUserProfile user = new TaskUserProfile();
        user.setLoginName("user1");
        user.setRealname("First User");
        ret.add(user);
        user = new TaskUserProfile();
        user.setLoginName("user2");
        user.setRealname("Second User");
        ret.add(user);
        user = new TaskUserProfile();
        user.setLoginName("user3");
        user.setRealname("Third User");
        ret.add(user);
        
        return ret;
    }

    private String toString(Object o) {
        return QueryParameters.toString(o);
    }
}
