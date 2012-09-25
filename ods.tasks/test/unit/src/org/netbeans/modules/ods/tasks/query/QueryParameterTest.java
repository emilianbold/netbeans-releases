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
import java.util.HashSet;
import java.util.Set;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JTextField;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.ods.tasks.query.QueryParameters.CheckBoxParameter;
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
    private final static String[] VALUES = new String[] {VALUE1, VALUE2, VALUE3, VALUE4};

    public QueryParameterTest(String arg0) {
        super(arg0);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testComboParameterEnabled() {
        JComboBox combo = new JComboBox();
        ComboParameter cp = new QueryParameters.ComboParameter(combo, QueryParameters.Column.COMMENT);
        assertTrue(combo.isEnabled());
        cp.setEnabled(false);
        assertFalse(combo.isEnabled());
        
        cp.setAlwaysDisabled(true);
        cp.setEnabled(true);
        assertFalse(combo.isEnabled());
    }
    
    public void testComboParametersValues() {
        JComboBox combo = new JComboBox();
        ComboParameter cp = new QueryParameters.ComboParameter(combo, QueryParameters.Column.COMMENT);
        assertEquals(QueryParameters.Column.COMMENT, cp.getColumn());
        assertNull(combo.getSelectedItem());
        assertEquals((String)null, cp.getValues());
        cp.populate(toString(VALUES));
        cp.setValues(VALUE3);

        Object item = combo.getSelectedItem();
        assertNotNull(item);
        assertEquals(VALUE3, item);

        String v = cp.getValues();
        assertEquals(VALUE3, v);

        combo.setSelectedItem(VALUE4);
        assertEquals(VALUE4, cp.getValues());
    }

    public void testListParameterEnabled() {
        JList list = new JList();
        ListParameter lp = new ListParameter(list, QueryParameters.Column.COMMENT);
        assertTrue(list.isEnabled());
        lp.setEnabled(false);
        assertFalse(list.isEnabled());
        
        lp.setAlwaysDisabled(true);
        lp.setEnabled(true);
        assertFalse(list.isEnabled());
    }
    
    public void testListParameters() {
        JList list = new JList();
        ListParameter lp = new ListParameter(list, QueryParameters.Column.COMMENT);
        assertEquals(QueryParameters.Column.COMMENT, lp.getColumn());
        assertEquals(-1, list.getSelectedIndex());
        lp.populate(toString(VALUES));
        lp.setValues(VALUE1 + "," + VALUE3);

        Object[] items = list.getSelectedValues();
        assertNotNull(items);
        assertEquals(2, items.length);
        Set<String> s = new HashSet<String>();
        for (Object i : items) s.add((String) i);
        if(!s.contains(VALUE1)) fail("mising parameter [" + VALUE1 + "]");
        if(!s.contains(VALUE3)) fail("mising parameter [" + VALUE3 + "]");

        String v = lp.getValues();
        String[] values = v.split(",");
        assertEquals(2, values.length);
        s.clear();
        for (String value : values) s.add(value);
        if(!s.contains(VALUE1)) fail("mising parameter [" + VALUE1 + "]");
        if(!s.contains(VALUE3)) fail("mising parameter [" + VALUE3 + "]");

        list.setSelectedValue(VALUE4, false);
        assertEquals(lp.getValues(), VALUE4);
    }
    
    public void testTextFieldParameterEnabled() {
        JTextField text = new JTextField();
        TextFieldParameter tp = new TextFieldParameter(text, QueryParameters.Column.COMMENT);
        assertTrue(text.isEnabled());
        tp.setEnabled(false);
        assertFalse(text.isEnabled());
        
        tp.setAlwaysDisabled(true);
        tp.setEnabled(true);
        assertFalse(text.isEnabled());
    }

    public void testTextFieldParameter() throws UnsupportedEncodingException {
        JTextField text = new JTextField();
        TextFieldParameter tp = new TextFieldParameter(text, QueryParameters.Column.COMMENT);
        assertEquals(QueryParameters.Column.COMMENT, tp.getColumn());
        assertEquals("", text.getText());

        tp.setValues(VALUE2);
        assertEquals(VALUE2, text.getText());

        String parameterValue = "NewValue";
        text.setText(parameterValue);
        assertEquals(parameterValue, tp.getValues());
        assertEquals(parameterValue, text.getText());

    }

    public void testCheckBoxParameterEnabled() {
        JCheckBox checkbox = new JCheckBox();
        CheckBoxParameter cp = new CheckBoxParameter(checkbox, QueryParameters.Column.COMMENT);
        assertTrue(checkbox.isEnabled());
        cp.setEnabled(false);
        assertFalse(checkbox.isEnabled());
        
        cp.setAlwaysDisabled(true);
        cp.setEnabled(true);
        assertFalse(checkbox.isEnabled());
    }
    
    public void testCheckBoxParameter() {
        JCheckBox checkbox = new JCheckBox();
        CheckBoxParameter cp = new CheckBoxParameter(checkbox, QueryParameters.Column.COMMENT);
        assertEquals(QueryParameters.Column.COMMENT, cp.getColumn());
        assertFalse(checkbox.isSelected());

        cp.setValues(Boolean.TRUE.toString());
        assertTrue(checkbox.isSelected());
        assertEquals(Boolean.TRUE.toString(), cp.getValues());

        cp.setValues(Boolean.FALSE.toString());
        assertFalse(checkbox.isSelected());
        assertEquals(Boolean.FALSE.toString(), cp.getValues());
    }

    private String toString(String[] array) {
        StringBuilder sb = new StringBuilder();
        for (String s : array) {
            if(sb.length() > 0) {
                sb.append(",");
            }
            sb.append(s);
        }
        return sb.toString();
    }
}
