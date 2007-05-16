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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.api.editor.mimelookup.test;

import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author vita
 */
public class MockMimeLookupTest extends NbTestCase {

    public MockMimeLookupTest(String name) {
        super(name);
    }

    public void testSetInstances() {
        MockServices.setServices(MockMimeLookup.class);
        
        MimePath mimePath = MimePath.parse("text/x-whatever");
        MockMimeLookup.setInstances(mimePath, "hi!");
        assertEquals("setInstances works", "hi!", MimeLookup.getLookup(mimePath).lookup(String.class));
        MockMimeLookup.setInstances(mimePath, "bye!");
        assertEquals("modified lookup works", "bye!", MimeLookup.getLookup(mimePath).lookup(String.class));
        MockMimeLookup.setInstances(mimePath);
        assertEquals("cleared lookup works", null, MimeLookup.getLookup(mimePath).lookup(String.class));
        
    }
}
