/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.common.method.impl;

import java.util.Arrays;
import java.util.List;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.j2ee.common.method.MethodModel;
import org.netbeans.modules.j2ee.common.method.impl.ParametersPanel.ParamsTableModel;

/**
 *
 * @author Martin Adamek
 */
public class ParametersPanelTest extends NbTestCase {
    
    public ParametersPanelTest(String testName) {
        super(testName);
    }
    
    public void testParamsTableModel() {
        ParamsTableModel model = new ParamsTableModel(Arrays.asList(new MethodModel.Variable[] {
            MethodModel.Variable.create("java.lang.String", "name", false),
            MethodModel.Variable.create("java.lang.String", "address", true),
        }));
        assertEquals(3, model.getColumnCount());
        // column names
        assertEquals("Name", model.getColumnName(0));
        assertEquals("Type", model.getColumnName(1));
        assertEquals("Final", model.getColumnName(2));
        // everything should be editable
        assertTrue(model.isCellEditable(0, 0));
        assertTrue(model.isCellEditable(0, 1));
        assertTrue(model.isCellEditable(0, 2));
        // 3rd column should be rendered as check box
        assertEquals(Boolean.class, model.getColumnClass(2));
        // check set values
        assertEquals("name", model.getValueAt(0, 0));
        assertEquals("java.lang.String", model.getValueAt(0, 1));
        assertEquals(false, model.getValueAt(0, 2));
        // change values
        model.setValueAt("type", 0, 0);
        model.setValueAt("java.lang.Long", 0, 1);
        model.setValueAt(false, 0, 2);
        assertEquals("type", model.getValueAt(0, 0));
        assertEquals("java.lang.Long", model.getValueAt(0, 1));
        assertEquals(false, model.getValueAt(0, 2));
        // check configured parameters
        List<MethodModel.Variable> parameters = model.getParameters();
        assertEquals(2, parameters.size());
        MethodModel.Variable parameter = parameters.get(0);
        assertEquals("type", parameter.getName());
        assertEquals("java.lang.Long", parameter.getType());
        assertEquals(false, parameter.getFinalModifier());
    }
    
}
