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

package org.netbeans.modules.xml.xam.ui.highlight;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.ui.Util;

/**
 * Tests Highlight class.
 *
 * @author Nathan Fiedler
 */
public class HighlightTest extends TestCase {
    private Schema schema;
    private SchemaModel model;
    
    public HighlightTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        model = Util.loadSchemaModel(Util.PO_XSD);
        schema = model.getSchema();
    }

    @Override
    protected void tearDown() throws Exception {
    }

    /**
     * Test of getSchemaComponent method, of class Highlight.
     */
    public void testGetSchemaComponent() {
        Highlight instance = new TestHighlight(schema, "type");
        Component result = instance.getComponent();
        assertEquals(schema, result);
    }

    /**
     * Test of getType method, of class Highlight.
     */
    public void testGetType() {
	String expResult = "type";
        Highlight instance = new TestHighlight(schema, expResult);
        String result = instance.getType();
        assertEquals(expResult, result);
    }
}
