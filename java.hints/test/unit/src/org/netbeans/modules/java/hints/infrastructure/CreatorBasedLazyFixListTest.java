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
package org.netbeans.modules.java.hints.infrastructure;

import com.sun.source.util.TreePath;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.java.hints.spi.ErrorRule;
import org.netbeans.modules.java.hints.spi.ErrorRule.Data;
import org.netbeans.spi.editor.hints.Fix;

/**
 *
 * @author Jan Lahoda
 */
public class CreatorBasedLazyFixListTest extends HintsTestBase {
    
    public CreatorBasedLazyFixListTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testCancel() throws Exception {
        prepareTest("Simple");
        
        final int[] calledCount = new int[1];
        final boolean[] cancel = new boolean[1];
        final CreatorBasedLazyFixList[] list = new CreatorBasedLazyFixList[1];
        
        list[0] = new CreatorBasedLazyFixList(null, "", 0, Collections.singleton((ErrorRule) new ErrorRule() {
            public Set getCodes() {
                throw new UnsupportedOperationException("Not supported yet.");
            }
            
            public List<Fix> run(CompilationInfo compilationInfo,
                    String diagnosticKey, int offset, TreePath treePath,
                    Data data) {
                calledCount[0]++;
                if (cancel[0]) {
                    list[0].cancel();
                }
                
                return Collections.<Fix>emptyList();
            }
            
            public void cancel() {
                //expected&ignored for now.
            }
            
            public String getId() {
                throw new UnsupportedOperationException("Not supported yet.");
            }
            
            public String getDisplayName() {
                throw new UnsupportedOperationException("Not supported yet.");
            }
            
            public String getDescription() {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        }), new HashMap<Class, Data>());
        
        cancel[0] = true;
        
        list[0].compute(info);
        
        assertEquals(1, calledCount[0]);
        
        list[0].compute(info);
        
        assertEquals(2, calledCount[0]);
        
        cancel[0] = false;
        
        list[0].compute(info);
        
        assertEquals(3, calledCount[0]);
        
        list[0].compute(info);
        
        assertEquals(3, calledCount[0]);
    }

    @Override
    protected String testDataExtension() {
        return "org/netbeans/test/java/hints/";
    }
    
    @Override
    protected boolean createCaches() {
        return false;
    }
    
}
