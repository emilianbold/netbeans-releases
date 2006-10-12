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

package org.netbeans.modules.j2ee.persistence.wizard.fromdb;

import java.util.Arrays;
import java.util.Collections;
import junit.framework.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Andrei Badea
 */
public class TableClosureTest extends TestCase {

    private TableProviderImpl provider;
    private TableClosure closure;

    public TableClosureTest(String testName) {
        super(testName);
    }

    public void setUp() {
        Map<String, Set<String>> tablesAndRefs = new HashMap<String, Set<String>>();
        Set<String> empty = Collections.emptySet();

        tablesAndRefs.put("A", Collections.singleton("B"));
        tablesAndRefs.put("B", Collections.singleton("C"));
        tablesAndRefs.put("C", empty);
        tablesAndRefs.put("D", new HashSet(Arrays.asList(new String[] { "B", "F" })));
        tablesAndRefs.put("E", Collections.singleton("A"));
        tablesAndRefs.put("F", empty);
        tablesAndRefs.put("G", Collections.singleton("G"));

        provider = new TableProviderImpl(tablesAndRefs);
        closure = new TableClosure(provider);
    }

    public void tearDown() {
        closure = null;
    }

    public void testAddRemoveWithClosureEnabled() {
        assertTables(new String[] { }, closure.getWantedTables());
        assertTables(new String[] { }, closure.getReferencedTables());
        assertTables(new String[] { }, closure.getSelectedTables());
        assertTables(new String[] { "A", "B", "C", "D", "E", "F", "G" }, closure.getAvailableTables());

        closure.addTables(Collections.singleton(provider.getTableByName("F")));

        assertTables(new String[] { "F" }, closure.getWantedTables());
        assertTables(new String[] { }, closure.getReferencedTables());
        assertTables(new String[] { "F" }, closure.getSelectedTables());
        assertTables(new String[] { "A", "B", "C", "D", "E", "G" }, closure.getAvailableTables());

        closure.addTables(Collections.singleton(provider.getTableByName("D")));

        assertTables(new String[] { "D", "F" }, closure.getWantedTables());
        assertTables(new String[] { "B", "C", "F" }, closure.getReferencedTables());
        assertTables(new String[] { "B", "C", "D", "F" }, closure.getSelectedTables());
        assertTables(new String[] { "A", "E", "G" }, closure.getAvailableTables());

        closure.addTables(Collections.singleton(provider.getTableByName("A")));

        assertTables(new String[] { "A", "D", "F" }, closure.getWantedTables());
        assertTables(new String[] { "B", "C", "F" }, closure.getReferencedTables());
        assertTables(new String[] { "A", "B", "C", "D", "F" }, closure.getSelectedTables());
        assertTables(new String[] { "E", "G" }, closure.getAvailableTables());

        // adding a table which references itself -- should not be included in the referenced
        // tables, since it wouldn't be possible to remove it.
        closure.addTables(Collections.singleton(provider.getTableByName("G")));

        assertTables(new String[] { "A", "D", "F", "G" }, closure.getWantedTables());
        assertTables(new String[] { "B", "C", "F" }, closure.getReferencedTables());
        assertTables(new String[] { "A", "B", "C", "D", "F", "G" }, closure.getSelectedTables());
        assertTables(new String[] { "E" }, closure.getAvailableTables());

        closure.removeTables(Collections.singleton(provider.getTableByName("D")));

        assertTables(new String[] { "A", "F", "G" }, closure.getWantedTables());
        assertTables(new String[] { "B", "C" }, closure.getReferencedTables());
        assertTables(new String[] { "A", "B", "C", "F", "G" }, closure.getSelectedTables());
        assertTables(new String[] { "D", "E" }, closure.getAvailableTables());

        closure.removeTables(Collections.singleton(provider.getTableByName("G")));

        assertTables(new String[] { "A", "F" }, closure.getWantedTables());
        assertTables(new String[] { "B", "C" }, closure.getReferencedTables());
        assertTables(new String[] { "A", "B", "C", "F" }, closure.getSelectedTables());
        assertTables(new String[] { "D", "E", "G" }, closure.getAvailableTables());

        closure.addTables(Collections.singleton(provider.getTableByName("C")));

        assertTables(new String[] { "A", "C", "F" }, closure.getWantedTables());
        assertTables(new String[] { "B", "C" }, closure.getReferencedTables());
        assertTables(new String[] { "A", "B", "C", "F" }, closure.getSelectedTables());
        assertTables(new String[] { "D", "E", "G" }, closure.getAvailableTables());

        closure.removeTables(Collections.singleton(provider.getTableByName("A")));

        assertTables(new String[] { "C", "F" }, closure.getWantedTables());
        assertTables(new String[] { }, closure.getReferencedTables());
        assertTables(new String[] { "C", "F" }, closure.getSelectedTables());
        assertTables(new String[] { "A", "B", "D", "E", "G" }, closure.getAvailableTables());

        closure.removeTables(Collections.singleton(provider.getTableByName("C")));

        assertTables(new String[] { "F" }, closure.getWantedTables());
        assertTables(new String[] { }, closure.getReferencedTables());
        assertTables(new String[] { "F" }, closure.getSelectedTables());
        assertTables(new String[] { "A", "B", "C", "D", "E", "G" }, closure.getAvailableTables());

        closure.removeTables(Collections.singleton(provider.getTableByName("F")));

        assertTables(new String[] { }, closure.getWantedTables());
        assertTables(new String[] { }, closure.getReferencedTables());
        assertTables(new String[] { }, closure.getSelectedTables());
        assertTables(new String[] { "A", "B", "C", "D", "E", "F", "G" }, closure.getAvailableTables());

        closure.addTables(new HashSet<Table>(Arrays.asList(new Table[] {
            provider.getTableByName("A"),
            provider.getTableByName("B"),
            provider.getTableByName("D"),
        })));

        assertTables(new String[] { "A", "B", "D" }, closure.getWantedTables());
        assertTables(new String[] { "B", "C", "F" }, closure.getReferencedTables());
        assertTables(new String[] { "A", "B", "C", "D", "F" }, closure.getSelectedTables());
        assertTables(new String[] { "E", "G" }, closure.getAvailableTables());

        // can't remove B, it is referenced
        closure.removeTables(Collections.singleton(provider.getTableByName("B")));

        assertTables(new String[] { "A", "B", "D" }, closure.getWantedTables());
        assertTables(new String[] { "B", "C", "F" }, closure.getReferencedTables());
        assertTables(new String[] { "A", "B", "C", "D", "F" }, closure.getSelectedTables());
        assertTables(new String[] { "E", "G" }, closure.getAvailableTables());

        closure.removeTables(new HashSet<Table>(Arrays.asList(new Table[] {
            provider.getTableByName("A"),
            provider.getTableByName("D"),
        })));

        assertTables(new String[] { "B" }, closure.getWantedTables());
        assertTables(new String[] { "C" }, closure.getReferencedTables());
        assertTables(new String[] { "B", "C" }, closure.getSelectedTables());
        assertTables(new String[] { "A", "D", "E", "F", "G" }, closure.getAvailableTables());

        closure.removeTables(Collections.singleton(provider.getTableByName("B")));

        assertTables(new String[] { }, closure.getWantedTables());
        assertTables(new String[] { }, closure.getReferencedTables());
        assertTables(new String[] { }, closure.getSelectedTables());
        assertTables(new String[] { "A", "B", "C", "D", "E", "F", "G" }, closure.getAvailableTables());
    }

    public void testClosureEnabledDisabled() {
        closure.setClosureEnabled(false);

        closure.addTables(new HashSet<Table>(Arrays.asList(new Table[] {
            provider.getTableByName("A"),
            provider.getTableByName("B"),
            provider.getTableByName("D"),
        })));

        assertTables(new String[] { "A", "B", "D" }, closure.getWantedTables());
        assertTables(new String[] { }, closure.getReferencedTables());
        assertTables(new String[] { "A", "B", "D" }, closure.getSelectedTables());
        assertTables(new String[] { "C", "E", "F", "G" }, closure.getAvailableTables());

        closure.addTables(new HashSet<Table>(Arrays.asList(new Table[] {
            provider.getTableByName("F"),
            provider.getTableByName("G"),
        })));

        assertTables(new String[] { "A", "B", "D", "F", "G" }, closure.getWantedTables());
        assertTables(new String[] { }, closure.getReferencedTables());
        assertTables(new String[] { "A", "B", "D", "F", "G" }, closure.getSelectedTables());
        assertTables(new String[] { "C", "E" }, closure.getAvailableTables());

        closure.setClosureEnabled(true);

        assertTables(new String[] { "A", "B", "D", "F", "G" }, closure.getWantedTables());
        assertTables(new String[] { "B", "C", "F" }, closure.getReferencedTables());
        assertTables(new String[] { "A", "B", "C", "D", "F", "G" }, closure.getSelectedTables());
        assertTables(new String[] { "E" }, closure.getAvailableTables());

        closure.setClosureEnabled(false);

        assertTables(new String[] { "A", "B", "D", "F", "G" }, closure.getWantedTables());
        assertTables(new String[] { }, closure.getReferencedTables());
        assertTables(new String[] { "A", "B", "D", "F", "G" }, closure.getSelectedTables());
        assertTables(new String[] { "C", "E" }, closure.getAvailableTables());

        closure.removeTables(new HashSet<Table>(Arrays.asList(new Table[] {
            provider.getTableByName("A"),
            provider.getTableByName("D"),
            provider.getTableByName("F"),
        })));

        assertTables(new String[] { "B", "G" }, closure.getWantedTables());
        assertTables(new String[] { }, closure.getReferencedTables());
        assertTables(new String[] { "B", "G" }, closure.getSelectedTables());
        assertTables(new String[] { "A", "C", "D", "E", "F" }, closure.getAvailableTables());

        closure.removeTables(new HashSet<Table>(Arrays.asList(new Table[] {
            provider.getTableByName("B"),
            provider.getTableByName("G"),
        })));

        assertTables(new String[] { }, closure.getWantedTables());
        assertTables(new String[] { }, closure.getReferencedTables());
        assertTables(new String[] { }, closure.getSelectedTables());
        assertTables(new String[] { "A", "B", "C", "D", "E", "F", "G" }, closure.getAvailableTables());
    }

    public void testAddRemoveAll() {
        closure.addAllTables();

        assertTables(new String[] { "A", "B", "C", "D", "E", "F", "G" }, closure.getWantedTables());
        assertTables(new String[] { "A", "B", "C", "F" }, closure.getReferencedTables());
        assertTables(new String[] { "A", "B", "C", "D", "E", "F", "G" }, closure.getSelectedTables());
        assertTables(new String[] { }, closure.getAvailableTables());

        closure.setClosureEnabled(false);

        assertTables(new String[] { "A", "B", "C", "D", "E", "F", "G" }, closure.getWantedTables());
        assertTables(new String[] { }, closure.getReferencedTables());
        assertTables(new String[] { "A", "B", "C", "D", "E", "F", "G" }, closure.getSelectedTables());
        assertTables(new String[] { }, closure.getAvailableTables());

        closure.setClosureEnabled(true);

        assertTables(new String[] { "A", "B", "C", "D", "E", "F", "G" }, closure.getWantedTables());
        assertTables(new String[] { "A", "B", "C", "F" }, closure.getReferencedTables());
        assertTables(new String[] { "A", "B", "C", "D", "E", "F", "G" }, closure.getSelectedTables());
        assertTables(new String[] { }, closure.getAvailableTables());

        closure.removeAllTables();

        assertTables(new String[] { }, closure.getWantedTables());
        assertTables(new String[] { }, closure.getReferencedTables());
        assertTables(new String[] { }, closure.getSelectedTables());
        assertTables(new String[] { "A", "B", "C", "D", "E", "F", "G" }, closure.getAvailableTables());

        closure.setClosureEnabled(false);
        closure.addAllTables();

        assertTables(new String[] { "A", "B", "C", "D", "E", "F", "G" }, closure.getWantedTables());
        assertTables(new String[] { }, closure.getReferencedTables());
        assertTables(new String[] { "A", "B", "C", "D", "E", "F", "G" }, closure.getSelectedTables());
        assertTables(new String[] { }, closure.getAvailableTables());

        closure.removeAllTables();

        assertTables(new String[] { }, closure.getWantedTables());
        assertTables(new String[] { }, closure.getReferencedTables());
        assertTables(new String[] { }, closure.getSelectedTables());
        assertTables(new String[] { "A", "B", "C", "D", "E", "F", "G" }, closure.getAvailableTables());
    }

    private void assertTables(String[] expected, Set<Table> actual) {
        assertEquals(expected.length, actual.size());
        for (String tableName : expected) {
            assertNotNull(actual.contains(tableName));
        }
    }
}
