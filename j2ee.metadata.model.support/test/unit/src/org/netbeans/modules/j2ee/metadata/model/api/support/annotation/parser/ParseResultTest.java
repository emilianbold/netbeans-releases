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

package org.netbeans.modules.j2ee.metadata.model.api.support.annotation.parser;

import java.util.HashMap;
import java.util.Map;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Andrei Badea
 */
public class ParseResultTest extends NbTestCase {

    public ParseResultTest(String testName) {
        super(testName);
    }

    public void testGetChecksType() {
        Map<String, Object> values = new HashMap<String, Object>();
        values.put("foo", "fooValue");
        values.put("bar", null);
        ParseResult result = new ParseResult(values);
        assertEquals("fooValue", result.get("foo", String.class));
        assertEquals("fooValue", result.get("foo", CharSequence.class));
        try {
            result.get("foo", Integer.class);
            fail();
        } catch (IllegalStateException e) {}
        assertNull(result.get("bar", String.class));
        assertNull(result.get("bar", Integer.class));
        result.get("foo", Integer.class);
    }
}
