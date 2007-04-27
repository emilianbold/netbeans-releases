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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.api.lexer;

import java.util.Set;
import org.netbeans.junit.NbTestCase;
import org.netbeans.lib.lexer.lang.TestChangingTokenId;

/**
 * Test of Language class.
 *
 * @author Miloslav Metelka
 */
public class LanguageTest extends NbTestCase {

    public LanguageTest(String name) {
        super(name);
    }

    public void testRefresh() {
        Language<TestChangingTokenId> lang = TestChangingTokenId.language();
        Set<TestChangingTokenId> ids = lang.tokenIds();
        assertEquals(1, ids.size());
        assertTrue(ids.contains(TestChangingTokenId.WHITESPACE));
        Set<String> cats = lang.tokenCategories();
        assertTrue(cats.isEmpty());
        
        // Refresh
        TestChangingTokenId.change(); // Calls lang.refresh()
        
        Set<TestChangingTokenId> changedIds = lang.tokenIds();
        assertEquals(2, changedIds.size());
        assertTrue(changedIds.contains(TestChangingTokenId.WHITESPACE));
        assertTrue(changedIds.contains(TestChangingTokenId.ADDED_AFTER_CHANGE));
        Set<String> changedCats = lang.tokenCategories();
        assertTrue(changedCats.contains("test"));
    }

}
