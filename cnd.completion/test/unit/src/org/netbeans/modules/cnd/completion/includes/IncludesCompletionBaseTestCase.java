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

import org.netbeans.modules.cnd.completion.cplusplus.ext.CompletionBaseTestCase;
import org.netbeans.modules.cnd.completion.cplusplus.ext.CompletionTestPerformer;

/**
 *
 * @author Vladimir Voskresensky
 */
public class IncludesCompletionBaseTestCase extends CompletionBaseTestCase {
    
    /**
     * Creates a new instance of IncludesCompletionBaseTestCase
     */
    public IncludesCompletionBaseTestCase(String testName) {
        super(testName, true);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        System.setProperty("cnd.completion.includes.trace", "true");
    }
    
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        System.setProperty("cnd.completion.includes.trace", "false");
    } 
    
    @Override
    protected CompletionTestPerformer createTestPerformer() {
        return new IncludesCompletionTestPerformer();
    }    
}
