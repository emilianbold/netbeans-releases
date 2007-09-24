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
package org.netbeans.modules.cnd.completion.includes;

import org.netbeans.modules.cnd.test.BaseTestCase;

/**
 *
 * @author Vladimir Voskresensky
 */
public class CCIncludesAuxTestCase extends BaseTestCase {
    
    /**
     * Creates a new instance of CCIncludesAuxTestCase
     */
    public CCIncludesAuxTestCase(String testName) {
        super(testName);
    }
    
    public void testTextShrinking() throws Exception {
        String text = "/very/long/path/to/include/dir";
        CsmIncludeCompletionItem item = new CsmIncludeCompletionItem(0, 0, text, "on/Unix/system", "", false, true, false);
        String shrinked = item.getRightText(true, "/");
        System.err.println("shrinked is " + shrinked);
        assertEquals("/very/long/.../Unix/system", shrinked);
        text = "C:\\very\\long\\path\\to\\include\\dir";
        item = new CsmIncludeCompletionItem(0, 0, text, "on\\Windows\\system", "", false, true, false);
        shrinked = item.getRightText(true, "\\");
        System.err.println("shrinked is " + shrinked);
        assertEquals("C:\\very\\long\\...\\Windows\\system", shrinked);
    }
}
