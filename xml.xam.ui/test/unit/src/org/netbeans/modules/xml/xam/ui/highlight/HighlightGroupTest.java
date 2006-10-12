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

import java.util.Set;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.xam.ui.Util;

/**
 * Tests HighlightGroup class.
 *
 * @author Nathan Fiedler
 */
public class HighlightGroupTest extends TestCase {
    private Schema schema;
    private SchemaModel model;
    
    public HighlightGroupTest(String testName) {
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
     * Test of type property.
     */
    public void testType() {
        String expResult = "type";
        HighlightGroup instance = new HighlightGroup(expResult);
        String result = instance.getType();
        assertEquals(expResult, result);
    }

    /**
     * Test of showing property.
     */
    public void testShowing() {
        HighlightGroup instance = new HighlightGroup("type");
        assertFalse(instance.isShowing());
        instance.setShowing(true);
        assertTrue(instance.isShowing());
        instance.setShowing(false);
        assertFalse(instance.isShowing());
    }

    /**
     * Test of highlights management.
     */
    public void testHighlights() {
        Highlight hl = new TestHighlight(schema, "a");
        HighlightGroup instance = new HighlightGroup("type");
        instance.addHighlight(hl);
        Set<Highlight> set = instance.highlights();
        assertEquals(1, set.size());
        hl = new TestHighlight(schema, "b");
        instance.addHighlight(hl);
        set = instance.highlights();
        assertEquals(2, set.size());
    }
}
