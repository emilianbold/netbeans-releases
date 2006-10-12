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

import java.util.Collections;
import java.util.List;
import java.util.Set;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.ui.Util;

/**
 * Tests DefaultHighlightManager class.
 *
 * @author Nathan Fiedler
 */
public class DefaultHighlightManagerTest extends TestCase {
    private Schema schema;
    private SchemaModel model;
    
    public DefaultHighlightManagerTest(String testName) {
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
     * Test highlight group management.
     */
    public void testHighlightGroups() {
        DefaultHighlightManager instance = new DefaultHighlightManager();
        HighlightGroup group = new HighlightGroup("type");
        group.addHighlight(new TestHighlight(schema, "a"));
        group.addHighlight(new TestHighlight(schema, "b"));
        group.addHighlight(new TestHighlight(schema, "c"));
        instance.addHighlightGroup(group);
        List<HighlightGroup> groups = instance.getHighlightGroups("type");
        assertTrue(groups.contains(group));
        assertEquals(1, groups.size());
        instance.removeHighlightGroup(group);
        groups = instance.getHighlightGroups("type");
        assertFalse(groups.contains(group));
        assertEquals(0, groups.size());
    }

    /**
     * Test highlight listener management.
     */
    public void testHighlightListeners() {
	DefaultHighlightManager instance = new DefaultHighlightManager();
        TestLighter l1 = new TestLighter();
        instance.addHighlighted(l1);
        TestLighter l2 = new TestLighter();
        instance.addHighlighted(l2);
        instance.removeHighlighted(l1);
        instance.removeHighlighted(l2);
    }

    private static class TestLighter implements Highlighted {

        public Set<Component> getComponents() {
            return Collections.emptySet();
        }

        public void highlightAdded(Highlight hl) {
        }

        public void highlightRemoved(Highlight hl) {
        }
    }
}
