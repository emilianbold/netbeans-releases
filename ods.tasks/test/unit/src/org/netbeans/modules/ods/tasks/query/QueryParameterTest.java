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

import com.tasktop.c2c.server.tasks.domain.TaskUserProfile;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JTextField;
import org.netbeans.junit.NbTestCase;
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
    private final static Collection<String> VALUES = Arrays.asList(VALUE1, VALUE2, VALUE3, VALUE4);

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
        
        cp.populate(Collections.singleton(VALUE3));
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
        assertEquals("description CONTAINS '" + VALUE4 + "'", cp.getCriteria().toQueryString());

        cp.setValues(null, false, false);
        assertFalse(chk1.isSelected());
        assertFalse(chk2.isSelected());
        assertEquals("", txt.getText());
        assertNull(cp.getCriteria());
        
        txt.setText(VALUE4);
        chk2.setSelected(true);
        assertEquals("description CONTAINS '" + VALUE4 + "'", cp.getCriteria().toQueryString());
        
        chk1.setSelected(false);
        chk2.setSelected(false);
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

        tp.setValues(VALUE2);
        assertEquals(VALUE2, text.getText());

        String parameterValue = "NewValue";
        text.setText(parameterValue);
        assertNull(tp.getCriteria()); // not implemented yet
        assertEquals(parameterValue, text.getText());

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
        
        Collection<TaskUserProfile> users = getUsers();
        cp.populateList(users);
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
    

    private Collection<TaskUserProfile> getUsers() {
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
    
}
