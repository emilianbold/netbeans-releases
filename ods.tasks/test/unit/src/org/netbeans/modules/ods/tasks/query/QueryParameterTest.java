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

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JTextField;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.ods.tasks.query.QueryParameters.CheckBoxParameter;
import org.netbeans.modules.ods.tasks.query.QueryParameters.CheckedTextFieldParameter;
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

    public void testCheckedTextParameterEnabled() {
        JCheckBox chk = new JCheckBox();
        JTextField txt = new JTextField();
        CheckedTextFieldParameter cp = new QueryParameters.CheckedTextFieldParameter(new Column[] {QueryParameters.Column.COMMENT}, txt, chk);
        assertTrue(chk.isEnabled());
        cp.setEnabled(false);
        assertFalse(chk.isEnabled());
    }
    
    public void testCheckedTextValues() {
        JCheckBox chk = new JCheckBox();
        JTextField txt = new JTextField();
        CheckedTextFieldParameter cp = new QueryParameters.CheckedTextFieldParameter(new Column[] {QueryParameters.Column.COMMENT}, txt, chk);
        
        assertEquals("", txt.getText());
        assertFalse(chk.isSelected());
        assertNull(cp.getCriteria());
        
        cp.populate(Collections.singleton(VALUE3));
        assertEquals("", txt.getText());
        assertFalse(chk.isSelected());
        assertNull(cp.getCriteria());
        
        cp.setValues(Collections.singleton(VALUE3));
        assertTrue(chk.isSelected());
        assertEquals("comment CONTAINS '" + VALUE3 + "'", cp.getCriteria().toQueryString());

        cp.setValues(null);
        assertFalse(chk.isSelected());
        assertEquals(VALUE3, txt.getText());
        assertNull(cp.getCriteria());
        
        txt.setText(VALUE4);
        chk.setSelected(true);
        assertEquals("comment CONTAINS '" + VALUE4 + "'", cp.getCriteria().toQueryString());
        
        chk.setSelected(false);
        assertNull(cp.getCriteria());
    }
    
    public void testMultipleCheckedTextValues() {
        
        JTextField txt = new JTextField();

        JCheckBox chk1 = new JCheckBox();
        JCheckBox chk2 = new JCheckBox();
        CheckedTextFieldParameter cp = new QueryParameters.CheckedTextFieldParameter(new Column[] {QueryParameters.Column.COMMENT, QueryParameters.Column.DESCRIPTION}, txt, chk1, chk2);
        
        assertEquals("", txt.getText());
        assertFalse(chk1.isSelected());
        assertFalse(chk2.isSelected());
        assertNull(cp.getCriteria());
        
        cp.populate(Arrays.asList(new Object[] {VALUE3, VALUE3}));
        assertEquals("", txt.getText());
        assertFalse(chk1.isSelected());
        assertFalse(chk2.isSelected());
        assertNull(cp.getCriteria());
        assertEquals("", txt.getText());
        
        cp.setValues(Arrays.asList(new Object[] {VALUE3, null}));
        assertTrue(chk1.isSelected());
        assertEquals("comment CONTAINS '" + VALUE3 + "'", cp.getCriteria().toQueryString());
        assertFalse(chk2.isSelected());

        cp.setValues(Arrays.asList(new Object[] {VALUE3, VALUE3}));
        assertTrue(chk1.isSelected());
        assertEquals("comment CONTAINS '" + VALUE3 + "' OR description CONTAINS '" + VALUE3 + "'", cp.getCriteria().toQueryString());
        assertTrue(chk2.isSelected());
        
        cp.setValues(null);
        assertFalse(chk1.isSelected());
        assertFalse(chk2.isSelected());
        assertNull(cp.getCriteria());
        assertEquals(VALUE3, txt.getText());
        
        cp.setValues(Arrays.asList(new Object[] {null, null}));
        assertFalse(chk1.isSelected());
        assertFalse(chk2.isSelected());
        assertNull(cp.getCriteria());
        assertEquals("", txt.getText());
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
        cp.setValues(VALUE3);

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
        lp.setValues(VALUE1, VALUE3);

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

}
