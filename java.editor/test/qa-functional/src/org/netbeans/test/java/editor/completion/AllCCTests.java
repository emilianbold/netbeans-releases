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

package org.netbeans.test.java.editor.completion;

import org.netbeans.spi.editor.completion.CompletionProvider;

/**
 *
 * @author jp159440
 */
public class AllCCTests extends CompletionTestPerformer{
    
    /** Creates a new instance of AllCCTests */
    public AllCCTests(String name) {
        super(name);
    }
  
    public void testAllSymbols() throws Exception {
        new CompletionTest().test(outputWriter, logWriter, "he", false, getDataDir(),"cp-prj-1", "org/netbeans/test/editor/allcompletion/AllSymbols.java", 28,CompletionProvider.COMPLETION_ALL_QUERY_TYPE);        
    }
    
    public void testFilteredSymbols() throws Exception {
        new CompletionTest().test(outputWriter, logWriter, "AbstractB", false, getDataDir(),"cp-prj-1", "org/netbeans/test/editor/allcompletion/AllSymbols.java", 28,CompletionProvider.COMPLETION_ALL_QUERY_TYPE);        
    }
    
}
