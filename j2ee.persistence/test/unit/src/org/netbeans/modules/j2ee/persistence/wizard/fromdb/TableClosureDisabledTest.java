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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import junit.framework.TestCase;
import org.netbeans.modules.j2ee.persistence.wizard.fromdb.Table.DisabledReason;

/**
 *
 * @author Andrei Badea
 */
public class TableClosureDisabledTest extends TestCase {

    private TableProviderImpl provider;
    private TableClosure closure;

    public TableClosureDisabledTest(String testName) {
        super(testName);
    }

    public void setUp() {
        Map<String, Set<String>> tablesAndRefs = new HashMap<String, Set<String>>();
        Map<String, DisabledReason> disabledReasons = new HashMap<String, DisabledReason>();
        Set<String> empty = Collections.emptySet();

        tablesAndRefs.put("ROOM", empty);
        tablesAndRefs.put("STUDENT", empty);
        tablesAndRefs.put("TEACHER", empty);
        tablesAndRefs.put("STUDENT_TEACHER", new HashSet(Arrays.asList(new String[] { "TEACHER", "STUDENT" })));
        tablesAndRefs.put("ZOO1", empty);
        tablesAndRefs.put("ZOO2", empty);
        tablesAndRefs.put("ZOO1_ZOO2", new HashSet(Arrays.asList(new String[] { "ZOO1", "ZOO2" })));

        disabledReasons.put("ROOM", new DisabledReason("Disabled", "Description"));
        disabledReasons.put("STUDENT", new DisabledReason("Disabled", "Description"));
        disabledReasons.put("ZOO1_ZOO2", new DisabledReason("Disabled", "Description"));

        provider = new TableProviderImpl(tablesAndRefs, disabledReasons);
        closure = new TableClosure(provider);
    }

    public void tearDown() {
        closure = null;
    }

    public void testAddAllWithClosureEnabledDoesntAddDisabledTables() {
        closure.addAllTables();


        assertTables(new String[] { "STUDENT_TEACHER", "TEACHER", "ZOO1", "ZOO2" }, closure.getWantedTables());
        assertTables(new String[] { "TEACHER" }, closure.getReferencedTables());
        assertTables(new String[] { "STUDENT_TEACHER", "TEACHER", "ZOO1", "ZOO2" }, closure.getSelectedTables());
        assertTables(new String[] { "ROOM", "STUDENT", "ZOO1_ZOO2" }, closure.getAvailableTables());
    }

    public void testAddAllWithClosureDisabledDoesntAddDisabledTables() {
        closure.setClosureEnabled(false);
        closure.addAllTables();

        assertTables(new String[] { "STUDENT_TEACHER", "TEACHER", "ZOO1", "ZOO2" }, closure.getWantedTables());
        assertTables(new String[] { }, closure.getReferencedTables());
        assertTables(new String[] { "STUDENT_TEACHER", "TEACHER", "ZOO1", "ZOO2" }, closure.getSelectedTables());
        assertTables(new String[] { "ROOM", "STUDENT", "ZOO1_ZOO2" }, closure.getAvailableTables());
    }

    public void testCannotAddDisabledReferencedTable() {
        closure.addTables(Collections.singleton(provider.getTableByName("STUDENT_TEACHER")));

        // STUDENT, which is also referenced, but disabled, was not added
        assertTables(new String[] { "STUDENT_TEACHER" }, closure.getWantedTables());
        assertTables(new String[] { "TEACHER" }, closure.getReferencedTables());
        assertTables(new String[] { "STUDENT_TEACHER", "TEACHER" }, closure.getSelectedTables());
        assertTables(new String[] { "ROOM", "STUDENT", "ZOO1", "ZOO2", "ZOO1_ZOO2" }, closure.getAvailableTables());
    }

    public void testCannotAddDisabledJoinTable() {
        closure.addTables(Collections.singleton(provider.getTableByName("ZOO1")));
        closure.addTables(Collections.singleton(provider.getTableByName("ZOO2")));

        // ZOO1_ZOO2, which is a join table for the added tables, but disabled, was not added
        assertTables(new String[] { "ZOO1", "ZOO2" }, closure.getWantedTables());
        assertTables(new String[] { }, closure.getReferencedTables());
        assertTables(new String[] { "ZOO1", "ZOO2" }, closure.getSelectedTables());
        assertTables(new String[] { "ROOM", "STUDENT", "STUDENT_TEACHER", "TEACHER", "ZOO1_ZOO2" }, closure.getAvailableTables());
    }

    private void assertTables(String[] expected, Set<Table> actual) {
        assertEquals(expected.length, actual.size());
        for (String tableName : expected) {
            assertNotNull(actual.contains(tableName));
        }
    }
}
